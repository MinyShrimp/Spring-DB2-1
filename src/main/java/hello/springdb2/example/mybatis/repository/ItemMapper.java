package hello.springdb2.example.mybatis.repository;

import hello.springdb2.domain.Item;
import hello.springdb2.dto.ItemSearchCond;
import hello.springdb2.dto.ItemUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ItemMapper {
    void save(Item item);

    void update(
            @Param("id") Long id,
            @Param("updateParam") ItemUpdateDto updateParam
    );

    List<Item> findAll(ItemSearchCond cond);

    Optional<Item> findById(Long id);
}
