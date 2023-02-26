package hello.springdb2.repository.memory;

import hello.springdb2.domain.Item;
import hello.springdb2.dto.ItemSearchCond;
import hello.springdb2.dto.ItemUpdateDto;
import hello.springdb2.repository.ItemRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MemoryItemRepository implements ItemRepository {
    private static final Map<Long, Item> STORE = new HashMap<>();
    private static long sequence = 0L;

    @Override
    public Item save(
            Item item
    ) {
        item.setId(++sequence);
        STORE.put(item.getId(), item);
        return item;
    }

    @Override
    public void update(
            Long itemId,
            ItemUpdateDto updateParam
    ) {
        Item findItem = STORE.get(itemId);
        findItem.update(updateParam);
    }

    @Override
    public Optional<Item> findById(
            Long id
    ) {
        return Optional.ofNullable(STORE.get(id));
    }

    @Override
    public List<Item> findAll(
            ItemSearchCond cond
    ) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        return STORE.values().stream()
                .filter(item -> ObjectUtils.isEmpty(itemName) || item.getItemName().contains(itemName))
                .filter(item -> ObjectUtils.isEmpty(maxPrice) || item.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    public void clearStore() {
        STORE.clear();
    }
}
