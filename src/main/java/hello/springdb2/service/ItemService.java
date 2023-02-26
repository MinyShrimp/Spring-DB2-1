package hello.springdb2.service;

import hello.springdb2.domain.Item;
import hello.springdb2.dto.ItemAddDto;
import hello.springdb2.dto.ItemSearchCond;
import hello.springdb2.dto.ItemUpdateDto;

import java.util.List;
import java.util.Optional;

public interface ItemService {
    Item save(ItemAddDto dto);

    void update(Long itemId, ItemUpdateDto updateParam);

    Optional<Item> findById(Long id);

    List<Item> findItems(ItemSearchCond cond);
}
