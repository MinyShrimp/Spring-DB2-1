package hello.springdb2.domain;

import hello.springdb2.dto.ItemUpdateDto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", length = 10)
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

    public void update(
            ItemUpdateDto dto
    ) {
        this.itemName = dto.getItemName();
        this.price = dto.getPrice();
        this.quantity = dto.getQuantity();
    }
}
