package hello.springdb2.service;

import hello.springdb2.domain.Item;
import hello.springdb2.dto.ItemAddDto;
import hello.springdb2.dto.ItemSearchCond;
import hello.springdb2.dto.ItemUpdateDto;
import hello.springdb2.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceV1 implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public Item save(
            ItemAddDto dto
    ) {
        return itemRepository.save(new Item(
                dto.getItemName(), dto.getPrice(), dto.getQuantity()
        ));
    }

    @Override
    public void update(
            Long itemId,
            ItemUpdateDto updateParam
    ) {
        itemRepository.update(itemId, updateParam);
    }

    @Override
    public Optional<Item> findById(
            Long id
    ) {
        return itemRepository.findById(id);
    }

    @Override
    public List<Item> findItems(
            ItemSearchCond cond
    ) {
        return itemRepository.findAll(cond);
    }
}
