package hello.springdb2.domain;

import hello.springdb2.dto.ItemUpdateDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Item {
    private Long id;
    private String itemName;
    private Integer price;
    private Integer quantity;

    public Item(
            String itemName,
            Integer price,
            Integer quantity
    ) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void update(
            ItemUpdateDto dto
    ) {
        this.itemName = dto.getItemName();
        this.price = dto.getPrice();
        this.quantity = dto.getQuantity();
    }
}
