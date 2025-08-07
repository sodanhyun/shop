package com.shop.repository;

import com.shop.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByItemNm(String itemNm);
    /*
    SELECT *
    FROM item
    WHERE item_nm = ?
    */
    List<Item> findByItemNmOrItemDetail(String itemNm, String itemDetail);
    /*
    SELECT *
    FROM item
    WHERE item_nm = ? OR item_detail = ?
    */
    List<Item> findByPriceLessThan(Integer price);
    /*
    SELECT *
    FROM item
    WHERE price < ?
    */
    List<Item> findByPriceLessThanOrderByPriceDesc(Integer price);
    /*
    SELECT *
    FROM item
    WHERE price < ?
    ORDER BY price DESC
    */

    // FROM 절 뒤에 들어가는 이름은 엔티티 CLASS 명
    @Query("SELECT i FROM Item i WHERE i.itemDetail LIKE %:itemDetail% ORDER BY i.price DESC")
    List<Item> findByItemDetail(@Param("itemDetail") String itemDetail);

    @Query(value = "SELECT * FROM item i WHERE i.item_detail LIKE %:itemDetail% ORDER BY i.price DESC",
            nativeQuery = true)
    List<Item> findByItemDetailByNative(String itemDetail);
}
