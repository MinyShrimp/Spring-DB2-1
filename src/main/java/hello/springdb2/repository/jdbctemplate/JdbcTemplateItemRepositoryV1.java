package hello.springdb2.repository.jdbctemplate;

import hello.springdb2.domain.Item;
import hello.springdb2.dto.ItemSearchCond;
import hello.springdb2.dto.ItemUpdateDto;
import hello.springdb2.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC Template
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {
    private final JdbcTemplate template;

    public JdbcTemplateItemRepositoryV1(
            DataSource dataSource
    ) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "insert into item(item_name, price, quantity) values(?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, item.getItemName());
            ps.setInt(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            return ps;
        }, keyHolder);

        item.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=?, price=?, quantity=? where id=?";
        template.update(sql,
                updateParam.getItemName(),
                updateParam.getPrice(),
                updateParam.getQuantity(),
                itemId
        );
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id = ?";
        try {
            Item item = template.queryForObject(sql, itemRowMapper(), id);
            return Optional.of(Objects.requireNonNull(item));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        String sql = "select id, item_name, price, quantity from item";
        List<Object> param = new ArrayList<>();

        // 동적 쿼리
        // 1. itemName 이나 maxPrice 에 값이 있는 경우
        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";

            boolean andFlag = false;

            // 2. itemName 에 값이 있는 경우
            if (StringUtils.hasText(itemName)) {
                sql += " item_name like concat('%', ?, '%')";
                param.add(itemName);
                andFlag = true;
            }

            // 3. maxPrice 에 값이 있는 경우
            if (maxPrice != null) {
                // 3-1. itemName, maxPrice 둘 다 값이 있는 경우
                if (andFlag) {
                    sql += " and";
                }
                sql += " price <= ?";
                param.add(maxPrice);
            }
        }

        // 4. 최종 sql 문 출력
        log.info("sql = {}", sql);
        return template.query(sql, itemRowMapper(), param.toArray());
    }

    private RowMapper<Item> itemRowMapper() {
        return (rs, rowNum) -> {
            Item item = new Item(
                    rs.getString("item_name"),
                    rs.getInt("price"),
                    rs.getInt("quantity")
            );
            item.setId(rs.getLong("id"));
            return item;
        };
    }
}
