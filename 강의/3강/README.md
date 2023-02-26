# 데이터 접근 기술 - MyBatis

## MyBatis 소개

### 소개

MyBatis는 앞서 설명한 JdbcTemplate보다 더 많은 기능을 제공하는 **SQL Mapper**이다.

* 기본적으로 JdbcTemplate이 제공하는 대부분의 기능을 제공한다.
* JdbcTemplate과 비교해서 MyBatis의 가장 매력적인 점
    * SQL을 **XML**에 편리하게 작성할 수 있다.
    * **동적 쿼리**를 매우 편리하게 작성할 수 있다.

### 비교 - 기본

#### JdbcTemplate

```java
String sql = "update item " +
        "set item_name=:itemName, price=:price, quantity=:quantity " +
        "where id=:id";
```

#### MyBatis

```xml
<update id="update">
    update item
    set item_name=#{itemName},
        price=#{price},
        quantity=#{quantity}
    where id = #{id}
</update>
```

MyBatis는 XML에 작성하기 때문에 라인이 길어져도 문자 더하기에 대한 불편함이 없다.

### 비교 - 동적 쿼리

#### JdbcTemplate

```java
String sql = "select id, item_name, price, quantity from item";
SqlParameterSource param = new BeanPropertySqlParameterSource(cond);

// 동적 쿼리
// 1. itemName 이나 maxPrice 에 값이 있는 경우
if (StringUtils.hasText(itemName) || maxPrice != null) {
    sql += " where";

    boolean andFlag = false;
    // 2. itemName 에 값이 있는 경우
    if (StringUtils.hasText(itemName)) {
        sql += " item_name like concat('%', :itemName, '%')";
        andFlag = true;
    }

    // 3. maxPrice 에 값이 있는 경우
    if (maxPrice != null) {
        // 3-1. itemName, maxPrice 둘 다 값이 있는 경우
        if (andFlag) {
            sql += " and";
        }
        sql += " price <= :maxPrice";
    }
}

// 4. 최종 sql 문 출력
log.info("sql = {}", sql);
return template.query(sql, param, itemRowMapper());
```

#### MyBatis

```xml
<select id="findAll" resultType="Item">
    select id, item_name, price, quantity
    from item
    <where>
        <if test="itemName != null and itemName != ''">
            and item_name like concat('%',#{itemName},'%')
        </if>
        <if test="maxPrice != null">
            and price &lt;= #{maxPrice}
        </if>
    </where>
</select>
```

JdbcTemplate은 자바 코드로 직접 동적 쿼리를 작성해야 한다.
반면에 MyBatis는 동적 쿼리를 매우 편리하게 작성할 수 있는 다양한 기능들을 제공해준다.

### 비교 - 설정

JdbcTemplate은 스프링에 내장된 기능이고, 별도의 설정없이 사용할 수 있다는 장점이 있다.
반면에 MyBatis는 약간의 설정이 필요하다.

### 정리

프로젝트에서 동적 쿼리와 복잡한 쿼리가 많다면 MyBatis를 사용하고, 단순한 쿼리들이 많으면 JdbcTemplate을 선택해서 사용하면 된다.
물론 둘을 함께 사용해도 된다. 하지만 MyBatis를 선택했다면 그것으로 충분할 것이다.

> **참고**<br>
> 강의에서는 MyBatis의 기능을 하나하나를 자세하게 다루지는 않는다.
> MyBatis를 왜 사용하는지, 그리고 주로 사용하는 기능 위주로 다룰 것이다.
> 그래도 이 강의를 듣고 나면 MyBatis로 개발을 할 수 있게 되고 추가로 필요한 내용을 공식 사이트에서 찾아서 사용할 수 있게 될 것이다.
>
> MyBatis는 기능도 단순하고 또 공식 사이트가 한글로 잘 번역되어 있어서 원하는 기능을 편리하게 찾아볼 수 있다.
> * [공식 사이트](https://mybatis.org/mybatis-3/ko/index.html)

## MyBatis 설정

## MyBatis 적용 1 - 기본

## MyBatis 적용 2 - 설정과 실행

## MyBatis 적용 3 - 분석

## MyBatis 기능 정리 1 - 동적 쿼리

## MyBatis 기능 정리 2 - 기타 기능
