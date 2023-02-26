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

## JdbcTemplate - 이름 지정 파라미터 1

## JdbcTemplate - 이름 지정 파라미터 2

## JdbcTemplate - 이름 지정 파라미터 3

## JdbcTemplate - SimpleJdbcInsert

## JdbcTemplate 기능 정리