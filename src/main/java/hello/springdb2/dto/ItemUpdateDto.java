package hello.springdb2.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ItemUpdateDto {
    private final String itemName;
    private final Integer price;
    private final Integer quantity;

    @JsonCreator
    public ItemUpdateDto(
            @JsonProperty("itemName") String itemName,
            @JsonProperty("price") Integer price,
            @JsonProperty("quantity") Integer quantity
    ) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
