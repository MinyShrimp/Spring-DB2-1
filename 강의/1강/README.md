# 데이터 접근 기술 - 스프링 JDBCTemplate

## JdbcTemplate 소개와 설정

### 소개

SQL을 직접 사용하는 경우에 스프링이 제공하는 JdbcTemplate은 아주 좋은 선택지다.
JdbcTemplate은 JDBC를 매우 편리하게 사용할 수 있게 도와준다.

#### 장점

* 설정의 편리함
    * JdbcTemplate은 spring-jdbc 라이브러리에 포함되어 있는데, 이 라이브러리는 스프링으로 JDBC를 사용할 때 기본으로 사용되는 라이브러리이다.
    * 그리고 별도의 복잡한 설정 없이 바로 사용할 수 있다.
* 반복 문제 해결
    * JdbcTemplate은 템플릿 콜백 패턴을 사용해서, JDBC를 직접 사용할 때 발생하는 대부분의 반복 작업을 대신 처리해준다.
    * 개발자는 SQL을 작성하고, 전달할 파리미터를 정의하고, 응답 값을 매핑하기만 하면 된다.
    * 우리가 생각할 수 있는 대부분의 반복 작업을 대신 처리해준다.
        * 커넥션 획득
        * statement 를 준비하고 실행
        * 결과를 반복하도록 루프를 실행
        * 커넥션 종료, statement , resultset 종료
        * 트랜잭션 다루기 위한 커넥션 동기화
        * 예외 발생시 스프링 예외 변환기 실행

#### 단점

* 동적 SQL을 해결하기 어렵다.

### build.gradle

```gradle
dependencies {
    // ...

    // JdbcTemplate
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'

    // H2 데이터베이스
    runtimeOnly 'com.h2database:h2'
    
    // ...
}
```

* `JdbcTemplate`이 들어있는 `spring-jdbc`가 라이브러리에 포함된다.
    * `implementation 'org.springframework.boot:spring-boot-starter-jdbc'`
* H2 데이터베이스의 클라이언트 라이브러리(`JdbcDriver`)도 추가하자.
    * `runtimeOnly 'com.h2database:h2'`

## JdbcTemplate 적용 1 - 기본

### JdbcTemplateItemRepository V1

#### 기본

```java
package hello.springdb2.repository.jdbctemplate;

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
}
```

* `this.template = new JdbcTemplate(dataSource)`
    * `JdbcTemplate`은 데이터소스(`dataSource`)가 필요하다.
    * `JdbcTemplateItemRepositoryV1()` 생성자를 보면 `dataSource`를 의존 관계 주입 받고 생성자 내부에서 `JdbcTemplate`을 생성한다.
    * 스프링에서는 `JdbcTemplate`을 사용할 때 관례상 이 방법을 많이 사용한다.
    * 물론 `JdbcTemplate`을 스프링 빈으로 직접 등록하고 주입받아도 된다.

#### save()

```java
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
```

* `template.update()`
    * 데이터를 변경할 때는 `update()`를 사용하면 된다.
    * `INSERT`, `UPDATE`, `DELETE` SQL에 사용한다.
    * `template.update()`의 반환 값은 `int`인데, 영향 받은 로우 수를 반환한다
* `KeyHolder`와 `conn.prepareStatement(sql, new String[]{"id"})`를 사용해서
  `id`를 지정해주면 `INSERT`쿼리 실행 이후에 데이터베이스에서 생성된 ID 값을 조회할 수 있다.
* 참고로 뒤에서 설명하겠지만 `JdbcTemplate`이 제공하는 `SimpleJdbcInsert`라는 훨씬 편리한 기능이 있으므로 대략 이렇게 사용한다 정도로만 알아두면 된다.

#### update()

```java
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
```

* `template.update()`
    * 데이터를 변경할 때는 `update()`를 사용하면 된다.
    * `?`에 바인딩할 파라미터를 순서대로 전달하면 된다.
    * 반환 값은 해당 쿼리의 영향을 받은 로우 수 이다.
    * 여기서는 `where id = ?`를 지정했기 때문에 영향 받은 로우수는 최대 1개이다.

#### findById()

```java
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
```

* `template.queryForObject()`
    * 결과 로우가 하나일 때 사용한다.
    * `RowMapper`는 데이터베이스의 반환 결과인 `ResultSet`을 객체로 변환한다.
    * 결과가 없으면 `EmptyResultDataAccessException`예외가 발생한다.
    * 결과가 둘 이상이면 `IncorrectResultSizeDataAccessException`예외가 발생한다.
* `ItemRepository.findById()`인터페이스는 결과가 없을 때 `Optional`을 반환해야 한다.
    * 따라서 결과가 없으면 예외를 잡아서 `Optional.empty`를 대신 반환하면 된다.

#### queryForObject()

```java
<T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException;
```

#### findAll()

```java
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
```

* `template.query()`
    * 결과가 하나 이상일 때 사용한다.
    * `RowMapper`는 데이터베이스의 반환 결과인 `ResultSet`을 객체로 변환한다.
    * 결과가 없으면 빈 컬렉션을 반환한다.
    * 동적 쿼리에 대한 부분은 바로 다음에 다룬다.

#### query()

```java
<T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException;
```

#### itemRowMapper()

```java
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
```

* `ResultSet`의 결과값을 받을 함수를 리턴하는 Mapper 함수를 만들었다.

## JdbcTemplate 적용 2 - 동적 쿼리 문제

### findAll

```java
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
```

사용자가 검색하는 값에 따라서 실행하는 SQL이 동적으로 달려져야 한다.

#### 검색 조건이 없음

```sql
select id, item_name, price, quantity from item;
```

#### 상품명으로 검색

```sql
select id, item_name, price, quantity from item
    where item_name like concat('%',?,'%');
```

#### 최대 가격으로 검색

```sql
select id, item_name, price, quantity from item
    where price <= ?;
```

#### 상품명, 최대 가격 둘 다 검색

```sql
select id, item_name, price, quantity from item
    where item_name like concat('%',?,'%') and price <= ?;
```

### 으 ~ 악 !

결과적으로 4가지 상황에 따른 SQL을 동적으로 생성해야 한다.
동적 쿼리가 언듯 보면 쉬워 보이지만, 막상 개발해보면 생각보다 다양한 상황을 고민해야 한다.
예를 들어서 어떤 경우에는 `where`를 앞에 넣고 어떤 경우에는 `and`를 넣어야 하는지 등을 모두 계산해야 한다.
그리고 각 상황에 맞추어 파라미터도 생성해야 한다.
물론 실무에서는 이보다 훨씬 더 복잡한 동적 쿼리들이 사용된다.

참고로 나중에 설명할 `MyBatis`의 가장 큰 장점은 SQL을 직접 사용할 때 동적 쿼리를 쉽게 작성할 수 있다는 점이다.

## JdbcTemplate 적용 3 - 구성과 실행

### 예제

#### JdbcTemplateV1Config

```java
@Configuration
@RequiredArgsConstructor
public class JdbcTemplateV1Config {
    private final DataSource dataSource;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JdbcTemplateItemRepositoryV1(dataSource);
    }
}
```

#### MainApplication

```java
//@Import(MemoryConfig.class)
@Import(JdbcTemplateV1Config.class)
@SpringBootApplication(scanBasePackages = "hello.springdb2.controller")
public class SpringDb2Application { ... }
```

#### application.properties

```properties
# PROFILE
spring.profiles.active = local

# DataSource
spring.datasource.url = jdbc:h2:tcp://localhost/~/test
spring.datasource.username = sa
```

* 이렇게 설정만 하면 스프링 부트가 해당 설정을 사용해서 커넥션 풀과 `DataSource`, `트랜잭션 매니저`를 스프링 빈으로 자동 등록한다.

#### 결과

```
HikariPool-1 - Starting...
HikariPool-1 - Added connection conn0: url=jdbc:h2:tcp://localhost/~/test user=SA
HikariPool-1 - Start completed.
```

### 로그 추가

```properties
# JdbcTemplate SQL Log
logging.level.org.springframework.jdbc = debug
```

* `JdbcTemplate`이 실행하는 SQL 로그를 확인하려면 `application.properties`에 다음을 추가하면 된다.
* `main`, `test`설정이 분리되어 있기 때문에 둘다 확인하려면 두 곳에 모두 추가해야 한다.

#### 결과

```
Executing prepared SQL query
Executing prepared SQL statement [select id, item_name, price, quantity from item where id = ?]
Fetching JDBC Connection from DataSource
```

## JdbcTemplate - 이름 지정 파라미터 1

### 순서대로 바인딩

#### 기존 코드

```java
String sql = "update item set item_name=?, price=?, quantity=? where id=?";
template.update(sql,
        updateParam.getItemName(),
        updateParam.getPrice(),
        updateParam.getQuantity(),
        itemId
);
```

* 여기서는 `itemName`, `price`, `quantity`가 SQL에 있는 `?` 에 순서대로 바인딩 된다.
* 따라서 순서만 잘 지키면 문제가 될 것은 없다. 그런데 문제는 변경시점에 발생한다.

#### 누군가 순서 변경

```java
String sql = "update item set item_name=?, quantity=?, price=? where id=?";
template.update(sql,
        updateParam.getItemName(),
        updateParam.getPrice(),
        updateParam.getQuantity(),
        itemId
);
```

* 결과적으로 `price`와 `quantity`가 바뀌는 매우 심각한 문제가 발생한다.
* 이럴일이 없을 것 같지만, 실무에서는 파라미터가 10~20개가 넘어가는 일도 아주 많다.
* 그래서 미래에 필드를 추가하거나, 수정하면서 이런 문제가 충분히 발생할 수 있다.

#### 명심하자

> 개발을 할 때는 코드를 몇줄 줄이는 편리함도 중요하지만,
> 모호함을 제거해서 코드를 명확하게 만드는 것이 유지보수 관점에서 매우 중요하다.

### 이름 지정 바인딩

`JdbcTemplate`은 이런 문제를 보완하기 위해 `NamedParameterJdbcTemplate`라는 이름을 지정해서 파라미터를 바인딩 하는 기능을 제공한다.

#### JdbcTemplateItemRepository V2

```java
/**
 * NamedParameterJdbcTemplate
 * SqlParameterSource
 * - BeanPropertySqlParameterSource
 * - MapSqlParameterSource
 * Map
 * <p>
 * BeanPropertyRowMapper
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {
    private final NamedParameterJdbcTemplate template;

    public JdbcTemplateItemRepositoryV2(
            DataSource dataSource
    ) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }
}
```

* `this.template = new NamedParameterJdbcTemplate(dataSource)`
    * `NamedParameterJdbcTemplate`도 내부에 `dataSource`가 필요하다.
    * `JdbcTemplateItemRepositoryV2` 생성자를 보면 의존관계 주입은 `dataSource`를 받고 내부에서 `NamedParameterJdbcTemplate`을 생성해서 가지고 있다.
    * 스프링에서는 `JdbcTemplate`관련 기능을 사용할 때 관례상 이 방법을 많이 사용한다.
* 물론 `NamedParameterJdbcTemplate`을 스프링 빈으로 직접 등록하고 주입받아도 된다.

#### save()

```java
@Override
public Item save(Item item) {
    String sql = "insert into item(item_name, price, quantity) values(:itemName, :price, :quantity)";

    SqlParameterSource param = new BeanPropertySqlParameterSource(item);
    KeyHolder keyHolder = new GeneratedKeyHolder();
    template.update(sql, param, keyHolder);

    item.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
    return item;
}
```

* SQL에서 다음과 같이 `?` 대신에 `:파라미터이름`을 받는 것을 확인할 수 있다.
* 추가로 `NamedParameterJdbcTemplate`은 데이터베이스가 생성해주는 키를 매우 쉽게 조회하는 기능도 제공해준다

## JdbcTemplate - 이름 지정 파라미터 2

### 이름 지정 파라미터

* 파라미터를 전달하려면 Map 처럼 key, value 데이터 구조를 만들어서 전달해야 한다.
    * `key`: `:파리이터이름`으로 지정한, 파라미터의 이름
    * `value`: 해당 파라미터의 값
* 다음 코드를 보면 이렇게 만든 파라미터(`param`)를 전달하는 것을 확인할 수 있다.
    * `template.update(sql, param, keyHolder);`
* 이름 지정 바인딩에서 자주 사용하는 파라미터의 종류는 크게 3가지가 있다.
    * `Map`
    * `SqlParameterSource`
        * `MapSqlParameterSource`
        * `BeanPropertySqlParameterSource`

#### Map

```java
Map<String, Object> param = Map.of("id", id);
Item item = template.queryForObject(sql, param, itemRowMapper());
```

#### MapSqlParameterSource

```java
SqlParameterSource param = new MapSqlParameterSource()
        .addValue("itemName", updateParam.getItemName())
        .addValue("price", updateParam.getPrice())
        .addValue("quantity", updateParam.getQuantity())
        .addValue("id", itemId);
```

* Map 과 유사한데, **SQL 타입을 지정**할 수 있는 등 SQL에 좀 더 특화된 기능을 제공한다.
* `SqlParameterSource`인터페이스의 구현체이다.
* `MapSqlParameterSource`는 **메서드 체인**을 통해 편리한 사용법도 제공한다.

#### BeanPropertySqlParameterSource

```java
SqlParameterSource param = new BeanPropertySqlParameterSource(item);
KeyHolder keyHolder = new GeneratedKeyHolder();
template.update(sql, param, keyHolder);
```

* **자바빈 프로퍼티 규약**을 통해서 자동으로 파라미터 객체를 생성한다.
    * 예) (`getXxx()` -> `xxx`, `getItemName()` -> `itemName`)
* 예를 들어서 `getItemName()`, `getPrice()`가 있으면 다음과 같은 데이터를 자동으로 만들어낸다.
    * `key = itemName, value = 상품명 값`
    * `key = price, value = 가격 값`

`BeanPropertySqlParameterSource`를 항상 사용할 수 있는 것은 아니다.
예를 들어서 `update()`에서는 SQL에 `:id` 를 바인딩 해야 하는데, `update()`에서 사용하는 `ItemUpdateDto`에는 `itemId`가 없다.
따라서 `BeanPropertySqlParameterSource`를 사용할 수 없고, 대신에 `MapSqlParameterSource`를 사용했다.

### BeanPropertyRowMapper

```java
private RowMapper<Item> itemRowMapper() {
    return BeanPropertyRowMapper.newInstance(Item.class);
}
```

`BeanPropertyRowMapper`는 `ResultSet`의 결과를 받아서 자바빈 규약에 맞추어 데이터를 변환한다.
예를 들어서 데이터베이스에서 조회한 결과가 `select id, price` 라고 하면 다음과 같은 코드를 작성해준다. (실제로는 **리플렉션**을 사용한다.)

```java
Item item = new Item();
item.setId(rs.getLong("id"));
item.setPrice(rs.getInt("price"));
```

데이터베이스에서 조회한 결과 이름을 기반으로 `setId()`, `setPrice()`처럼 자바빈 프로퍼티 규약에 맞춘 메서드를 호출하는 것이다.

#### 별칭

그런데 `select item_name`의 경우 `setItem_name()`이라는 메서드가 없기 때문에 골치가 아프다.
이런 경우 개발자가 조회 SQL을 다음과 같이 고치면 된다.

* `select item_name as itemName`

별칭 `as`를 사용해서 SQL 조회 결과의 이름을 변경하는 것이다.
실제로 이 방법은 자주 사용된다.
특히 데이터베이스 컬럼 이름과 객체 이름이 완전히 다를 때 문제를 해결할 수 있다.

예를 들어서 데이터베이스에는 `member_name`이라고 되어 있는데 객체에 `username`이라고 되어 있다면 다음과 같이 해결할 수 있다.

* `select member_name as username`

이렇게 데이터베이스 컬럼 이름과 객체의 이름이 다를 때 별칭(`as`)을 사용해서 문제를 많이 해결한다.
`JdbcTemplate`은 물론이고, `MyBatis`같은 기술에서도 자주 사용된다.

#### 관례의 불일치

자바 객체는 카멜(`camelCase`) 표기법을 사용한다. `itemName`처럼 중간에 낙타 봉이 올라와 있는 표기법이다.
반면에 관계형 데이터베이스에서는 주로 언더스코어를 사용하는 `snake_case`표기법을 사용한다.
`item_name`처럼 중간에 언더스코어를 사용하는 표기법이다.

이 부분을 관례로 많이 사용하다 보니 `BeanPropertyRowMapper`는 언더스코어 표기법을 카멜로 자동 변환해준다.
따라서 `select item_name`으로 조회해도 `setItemName()`에 문제 없이 값이 들어간다.

정리하면 `snake_case`는 자동으로 해결되니 그냥 두면 되고, 컬럼 이름과 객체 이름이 완전히 다른 경우에는 조회 SQL에서 별칭을 사용하면 된다.

## JdbcTemplate - 이름 지정 파라미터 3

### JdbcTemplateV2Config

```java
@Configuration
@RequiredArgsConstructor
public class JdbcTemplateV2Config {
    private final DataSource dataSource;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JdbcTemplateItemRepositoryV2(dataSource);
    }
}
```

### MainApplication

```java
//@Import(JdbcTemplateV1Config.class)
@Import(JdbcTemplateV2Config.class)
@SpringBootApplication(scanBasePackages = "hello.springdb2.controller")
public class SpringDb2Application {

    public static void main(String[] args) {
        SpringApplication.run(SpringDb2Application.class, args);
    }

    @Bean
    @Profile("local")
    public TestDataInit testDataInit(
            ItemRepository itemRepository
    ) {
        return new TestDataInit(itemRepository);
    }
}
```

### BeanPropertyRowMapper Log

```
Mapping column 'ID' to property 'id' of type 'java.lang.Long'
Mapping column 'ITEM_NAME' to property 'itemName' of type 'java.lang.String'
Mapping column 'PRICE' to property 'price' of type 'java.lang.Integer'
Mapping column 'QUANTITY' to property 'quantity' of type 'java.lang.Integer'
```

## JdbcTemplate - SimpleJdbcInsert

### SimpleJdbcInsert

#### JdbcTemplateItemRepository V3

```java
/**
 * SimpleJdbcInsert
 */
@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV3 implements ItemRepository {
    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcTemplateItemRepositoryV3(
            DataSource dataSource
    ) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("item")
                .usingGeneratedKeyColumns("id")
                .usingColumns("item_name", "price", "quantity"); // 생략 가능
    }

    @Override
    public Item save(Item item) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        Number key = jdbcInsert.executeAndReturnKey(param);
        item.setId(key.longValue());
        
        return item;
    }
    
    // ...
}
```

* `this.jdbcInsert = new SimpleJdbcInsert(dataSource)`
* 생성자를 보면 의존관계 주입은 `dataSource`를 받고 내부에서 `SimpleJdbcInsert`을 생성해서 가지고 있다.
    * 스프링에서는 JdbcTemplate 관련 기능을 사용할 때 관례상 이 방법을 많이 사용한다.
* 물론 `SimpleJdbcInsert`을 스프링 빈으로 직접 등록하고 주입받아도 된다.

#### SimpleJdbcInsert

```java
this.jdbcInsert = new SimpleJdbcInsert(dataSource)
        .withTableName("item")
        .usingGeneratedKeyColumns("id")
        .usingColumns("item_name", "price", "quantity"); // 생략 가능
```

* `withTableName`
    * 데이터를 저장할 테이블 명을 지정한다.
* `usingGeneratedKeyColumns`
    * key 를 생성하는 PK 컬럼 명을 지정한다.
* `usingColumns`
    * INSERT SQL에 사용할 컬럼을 지정한다. 특정 값만 저장하고 싶을 때 사용한다.
    * 생략할 수 있다.

`SimpleJdbcInsert`는 생성 시점에 데이터베이스 테이블의 메타 데이터를 조회한다.
따라서 어떤 컬럼이 있는지 확인 할 수 있으므로 `usingColumns`을 생략할 수 있다.
만약 특정 컬럼만 지정해서 저장하고 싶다면 `usingColumns`를 사용하면 된다.

#### save()

```java
@Override
public Item save(Item item) {
    SqlParameterSource param = new BeanPropertySqlParameterSource(item);
    Number key = jdbcInsert.executeAndReturnKey(param);
    item.setId(key.longValue());
    
    return item;
}
```

### 결과 로그

```
The following parameters are used for call INSERT INTO item (item_name, price, quantity) VALUES(?, ?, ?) 
with: [
  org.springframework.jdbc.core.SqlParameterValue@7401ba83, 
  org.springframework.jdbc.core.SqlParameterValue@21376c3, 
  org.springframework.jdbc.core.SqlParameterValue@27c3f35d
]
```

## JdbcTemplate 기능 정리

### 주요 기능

* `JdbcTemplate`
    * 순서 기반 파라미터 바인딩을 지원한다.
* `NamedParameterJdbcTemplate`
    * 이름 기반 파라미터 바인딩을 지원한다. (권장)
* `SimpleJdbcInsert`
    * INSERT SQL을 편리하게 사용할 수 있다.
* `SimpleJdbcCall`
    * 스토어드 프로시저를 편리하게 호출할 수 있다.

> 참고
> 스토어드 프로시저를 사용하기 위한 `SimpleJdbcCall`에 대한 자세한 내용은 다음 스프링 공식 메뉴얼을 참고하자.
> * [공식 Docs](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#jdbc-simple-jdbc-call-1)

### JdbcTemplate 사용법 정리

> [공식 Docs](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#jdbc-JdbcTemplate)

JdbcTemplate에 대한 사용법은 스프링 공식 메뉴얼에 자세히 소개되어 있다.
여기서는 스프링 공식 메뉴얼이 제공하는 예제를 통해 JdbcTemplate의 기능을 간단히 정리해보자.

### 조회

#### 단건 조회 - 숫자

```java
int rowCount = jdbcTemplate.queryForObject("select count(*) from t_actor", Integer.class);
```

#### 단건 조회 - 숫자, 파라미터 바인딩

```java
int countOfActorsNamedJoe = jdbcTemplate.queryForObject(
        "select count(*) from t_actor where first_name = ?", Integer.class, "Joe"
);
```

#### 단건 조회 - 문자

```java
String lastName = jdbcTemplate.queryForObject(
        "select last_name from t_actor where id = ?", String.class, 1212L
);
```

#### 단건 조회 - 객체

```java
Actor actor = jdbcTemplate.queryForObject(
        "select first_name, last_name from t_actor where id = ?",
        (resultSet, rowNum) -> {
            Actor newActor = new Actor();
            newActor.setFirstName(resultSet.getString("first_name"));
            newActor.setLastName(resultSet.getString("last_name"));
            return newActor;
        },
        1212L
);
```

#### 목록 조회 - 객체

```java
List<Actor> actors = this.jdbcTemplate.query(
        "select first_name, last_name from t_actor",
        (resultSet, rowNum) -> {
            Actor actor = new Actor();
            actor.setFirstName(resultSet.getString("first_name"));
            actor.setLastName(resultSet.getString("last_name"));
            return actor;
        }
);
```

#### 목록 조회 - 객체

```java
private final RowMapper<Actor> actorRowMapper = (resultSet, rowNum) -> {
    Actor actor = new Actor();
    actor.setFirstName(resultSet.getString("first_name"));
    actor.setLastName(resultSet.getString("last_name"));
    return actor;
};

public List<Actor> findAllActors() {
    return this.jdbcTemplate.query("select first_name, last_name from t_actor", actorRowMapper);
}
```

### 변경

#### 등록

```java
this.jdbcTemplate.update(
        "insert into t_actor (first_name, last_name) values (?, ?)",
        "Leonor", "Watling"
);
```

#### 수정

```java
this.jdbcTemplate.update(
        "update t_actor set last_name = ? where id = ?",
        "Banjo", 5276L
);
```

#### 삭제

```java
this.jdbcTemplate.update(
        "delete from t_actor where id = ?",
        Long.valueOf(actorId)
);
```

### 기타 기능

#### DDL

```java
this.jdbcTemplate.execute("create table mytable (id integer, name varchar(100))");
```

#### 스토어드 프로시저

```java
this.jdbcTemplate.update(
        "call SUPPORT.REFRESH_ACTORS_SUMMARY(?)",
        Long.valueOf(unionId)
);
```

## 정리