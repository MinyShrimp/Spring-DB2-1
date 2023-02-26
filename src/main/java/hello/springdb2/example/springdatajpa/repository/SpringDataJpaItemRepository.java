package hello.springdb2.example.springdatajpa.repository;

import hello.springdb2.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataJpaItemRepository extends JpaRepository<Item, Long> {

    // Item.itemName Like '%:itemName%'
    List<Item> findByItemNameLike(String itemName);

    // Item.price <= :price
    List<Item> findByPriceLessThanEqual(Integer price);

    // 쿼리 메서드 ( 아래 메서드와 동일한 기능 수행 )
    List<Item> findByItemNameLikeAndPriceLessThanEqual(String itemName, Integer price);

    // 쿼리 직접 실행
    @Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
    List<Item> findItems(
            @Param("itemName") String itemName,
            @Param("price") Integer price
    );
}
