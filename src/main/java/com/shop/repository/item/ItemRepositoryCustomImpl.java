package com.shop.repository.item;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.shop.entity.QItem.item;
import static com.shop.entity.QItemImg.itemImg;

@RequiredArgsConstructor
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private BooleanExpression regDtsAfter(String searchDateType) {
        /* saerchDateType 화면 ==> "all", "1d", "1w", "1m", "6m" */
        LocalDateTime now = LocalDateTime.now();

        if(StringUtils.equals(searchDateType, "1d")) {
            now = now.minusDays(1);
        }else if(StringUtils.equals(searchDateType, "1w")) {
            now = now.minusWeeks(1);
        }else if(StringUtils.equals(searchDateType, "1m")) {
            now = now.minusMonths(1);
        }else if(StringUtils.equals(searchDateType, "6m")) {
            now = now.minusMonths(6);
        }else if(StringUtils.equals(searchDateType, "all") || searchDateType == null) {
            return null;
        }

        return item.regTime.after(now);
    }

    private BooleanExpression searchSellStatusEq(ItemSellStatus itemSellStatus) {
        if(itemSellStatus == null) {
            return null;
        }
        return item.itemSellStatus.eq(itemSellStatus);
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery) {
        // 무엇을 기준으로(searchBy) 검색할 키워드(searchQuery)
        // searchBy 화면 ==> "itemNm", "createdBy"
        if(StringUtils.equals(searchBy, "itemNm")) {
            return item.itemNm.like("%" + searchQuery + "%");
        }else if(StringUtils.equals(searchBy, "createdBy")) {
            return item.createdBy.like("%" + searchQuery + "%");
        }
        return null;
    }

    @Override
    public Page<Item> getAdminPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        /*
        목적 : item 테이블에서 검색 조건에 맞는 결과를 페이지 단위로 조회
        조건 1. searchDateType에 따라 검색 기간 설정
        조건 2. searchSellStatus에 따라 상품 판매 상태(SOLD_OUT, SELL) 설정
        조건 3. searchBy + searchQuery에 따라 검색 키워드 설정
        ==> item_id를 기준으로 내림차순, pageable 기준에 따른 페이징된 결과 반환

        SELECT FROM item
        WHERE 조건1 AND 조건2 AND 조건3
        ORDER BY item_id DESC
        LIMIT, OFFSET . . .

        Page(인터페이스)-PageImpl(구현체)
        PageImpl
        ㄴcontent : List<T>
        ㄴtotalCount : 총 페이지 수
        ㄴnumber : 페이지 번호
        */

        List<Item> content = jpaQueryFactory
                .selectFrom(item)
                .where(regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery())) // 조건1~3 중 null이 들어가는 경우 ==> 무시
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        /*
        SELECT count(*)
        FROM item
        WHERE 조건1 AND 조건2 AND 조건3
        */

        Long totalCount = jpaQueryFactory
                .select(Wildcard.count)
                .from(item)
                .where(regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
                .fetchOne();
        //COUNT 쿼리된 totalCount 결과는 null이 될 수 있음
        //==> null을 안전하게 다루기 위해 Optional 타입으로 한번 감싼다
        Optional<Long> total = Optional.ofNullable(totalCount);

        //total ==> Optional 타입을 이용해 Null을 안전하게 처리한다
        return new PageImpl<>(content, pageable, total.orElse(0L));
    }

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        List<MainItemDto> content = jpaQueryFactory
                .select(Projections.fields(MainItemDto.class,
                        item.id,
                        item.itemNm,
                        item.itemDetail,
                        item.price,
                        itemImg.imgUrl
                        ))
                .from(itemImg)
                .innerJoin(itemImg.item)
                .where(itemImg.repImgYn.eq("Y"))
                .where(searchByLike("itemNm", itemSearchDto.getSearchQuery()))
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = jpaQueryFactory
                .select(Wildcard.count)
                .from(itemImg)
                .innerJoin(itemImg.item)
                .where(itemImg.repImgYn.eq("Y"))
                .where(searchByLike("itemNm", itemSearchDto.getSearchQuery()))
                .fetchOne();

        Optional<Long> total = Optional.ofNullable(totalCount);
        return new PageImpl<>(content, pageable, total.orElse(0L));
    }

}
