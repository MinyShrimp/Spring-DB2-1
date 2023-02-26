package hello.springdb2.repository;

import hello.springdb2.domain.Item;
import hello.springdb2.dto.ItemSearchCond;
import hello.springdb2.dto.ItemUpdateDto;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    void update(Long itemId, ItemUpdateDto updateParam);

    Optional<Item> findById(Long id);

    List<Item> findAll(ItemSearchCond cond);
}
