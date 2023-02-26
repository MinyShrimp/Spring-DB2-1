package hello.springdb2.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ItemSearchCond {
    private final String itemName;
    private final Integer maxPrice;

    @JsonCreator
    public ItemSearchCond(
            @JsonProperty("itemName") String itemName,
            @JsonProperty("maxPrice") Integer maxPrice
    ) {
        this.itemName = itemName;
        this.maxPrice = maxPrice;
    }
}
