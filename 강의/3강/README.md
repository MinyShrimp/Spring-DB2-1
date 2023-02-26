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

### 설정

#### build.gradle

```gradle
dependencies {
    // MyBatis
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.1'
}
```

다음과 같은 라이브러리가 추가된다.

![img.png](img.png)

* `mybatis-spring-boot-starter`
    * MyBatis를 스프링 부트에서 편리하게 사용할 수 있게 시작하는 라이브러리
* `mybatis-spring-boot-autoconfigure`
    * MyBatis와 스프링 부트 설정 라이브러리
* `mybatis-spring`
    * MyBatis와 스프링을 연동하는 라이브러리
* `mybatis`
    * MyBatis 라이브러리

#### application.properties

```properties
# MyBatis
mybatis.type-aliases-package = hello.springdb2.domain
logging.level.hello.springdb2.repository.mybatis = trace
mybatis.configuration.map-underscore-to-camel-case = true
```

> main, test 둘다 적용해주자!

* `mybatis.type-aliases-package`
    * 마이바티스에서 타입 정보를 사용할 때는 패키지 이름을 적어주어야 하는데, 여기에 명시하면 패키지 이름을 생략할 수 있다.
    * 지정한 패키지와 그 하위 패키지가 자동으로 인식된다.
    * 여러 위치를 지정하려면 `,`, `;` 로 구분하면 된다.
* `mybatis.configuration.map-underscore-to-camel-case = true`
    * JdbcTemplate의 `BeanPropertyRowMapper`에서 처럼 언더바를 카멜로 자동 변경해주는 기능을 활성화 한다.
    * 바로 다음에 설명하는 관례의 불일치 내용을 참고하자.
    * 기본값은 false이다.
* `logging.level.hello.itemservice.repository.mybatis = trace`
    * MyBatis에서 실행되는 쿼리 로그를 확인할 수 있다.

#### 관례의 불일치

자바 객체에는 주로 카멜(`camelCase`) 표기법을 사용한다.
`itemName`처럼 중간에 낙타 봉이 올라와 있는 표기법이다.

반면에 관계형 데이터베이스에서는 주로 언더스코어를 사용하는 `snake_case`표기법을 사용한다.
`item_name`처럼 중간에 언더스코어를 사용하는 표기법이다.

이렇게 관례로 많이 사용하다 보니 `map-underscore-to-camel-case` 기능을 활성화 하면 언더스코어 표기법을 카멜로 자동 변환해준다.
따라서 DB에서 `select item_name`으로 조회해도 객체의 `itemName ( setItemName() )` 속성에 값이 정상 입력된다.

정리하면 해당 옵션을 켜면 `snake_case`는 자동으로 해결되니 그냥 두면 되고,
컬럼 이름과 객체 이름이 완전히 다른 경우에는 조회 SQL에서 별칭을 사용하면 된다.

* 표기법 차이
    * DB: `select item_name`
    * 객체:  `name`
* 별칭을 통한 해결방안
    * `select item_name as name`

## MyBatis 적용 1 - 기본

## MyBatis 적용 2 - 설정과 실행

## MyBatis 적용 3 - 분석

## MyBatis 기능 정리 1 - 동적 쿼리

## MyBatis 기능 정리 2 - 기타 기능
