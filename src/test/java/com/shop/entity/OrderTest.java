package com.shop.entity;

import com.shop.constant.ItemSellStatus;
import com.shop.repository.ItemRepository;
import com.shop.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class OrderTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @PersistenceContext
    EntityManager em;

    public Item createItem() {
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10_000);
        item.setItemDetail("상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        return item;
    }

    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest() {
        //1. 주문 엔티티 생성
        Order order = new Order();
        //orderRepository.save(order);
        //for문의 역할
        //아이템 기준정보 생성, 그 기준정보로 주문-아이템 생성
        for(int i=0; i<3; i++) {
            //2. 아이템 엔티티 저장
            Item item = createItem();
            itemRepository.save(item);
            //3. 위에서 저장된 아이템으로 주문-아이템(주문과 특정 아이템을 연결해주는 역할) 저장
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setOrder(order);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1_000);
            //orderItemRepository.save(orderItem);
            /* 현재 Order는 저장이 안됨, order 엔티티의 orderItems 리스트에 orderItem 엔티티를 추가 */
            order.getOrderItems().add(orderItem);
        }
        /* 지금 Order는 저장 전에 OrderItem 엔티티를 3개 포함한 상태 */
        orderRepository.saveAndFlush(order);
        em.clear();

        Order savedOrder = orderRepository.findById(order.getId())
                .orElseThrow(EntityNotFoundException::new);
        assertEquals(3, savedOrder.getOrderItems().size());
    }

}