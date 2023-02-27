# 데이터 접근 기술 - Querydsl

## Querydsl 소개 1 - 기존 방식의 문제점

## Querydsl 소개 2 - 해결

## Querydsl 설정

### 설정 - Spring 3.0

#### build.gradle

```gradle
dependencies {
    // QueryDSL 추가 1 - 의존성
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"
}

// QueryDSL 추가 2 - 자동 생성된 Q 클래스 gradle clean 으로 제거
clean {
    delete file('src/main/generated')
}
```

### 빌드 옵션

### 옵션 선택1 - Gradle - Q타입 생성 확인 방법

* Gradle IntelliJ 사용법
    * `Gradle -> Tasks -> build -> clean`
    * `Gradle -> Tasks -> other -> compileJava`
* Gradle 콘솔 사용법
    * `./gradlew clean compileJava`
* Q 타입 생성 확인
    * build -> generated -> sources -> annotationProcessor -> java/main 하위에
    * `hello.springdb2.domain.QItem` 이 생성되어 있어야 한다.

#### 옵션 선택2 - IntelliJ IDEA - Q타입 생성 확인 방법

* 빌드
    * Build -> Build Project
    * Build -> Rebuild
    * main()
* Q 타입 생성 확인
    * src/main/generated 하위
    * `hello.springdb2.domain.QItem`이 생성되어 있어야 한다.

### 참고

> 참고<br>
> Q타입은 컴파일 시점에 자동 생성되므로 버전관리(GIT)에 포함하지 않는 것이 좋다.
> IntelliJ IDEA 옵션을 선택하면 Q타입은 src/main/generated 폴더 아래에 생성되기 때문에 여기를 포함하지 않는 것이 좋다.

## Querydsl 적용

### 적용

#### JpaItemRepository V3

```java
@Repository
@Transactional
public class JpaItemRepositoryV3 implements ItemRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    public JpaItemRepositoryV3(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }
}
```

### 분석

#### 공통

* `Querydsl`을 사용하려면 `JPAQueryFactory` 가 필요하다.
* `JPAQueryFactory`는 JPA 쿼리인 JPQL을 만들기 때문에 `EntityManager`가 필요하다.
* 설정 방식은 `JdbcTemplate`을 설정하는 것과 유사하다.
* 참고로 `JPAQueryFactory`를 스프링 빈으로 등록해서 사용해도 된다.

#### findAllOld

```java
public List<Item> findAllOld(ItemSearchCond cond) {
    String itemName = cond.getItemName();
    Integer maxPrice = cond.getMaxPrice();

    QItem item = QItem.item;

    BooleanBuilder builder = new BooleanBuilder();
    if (StringUtils.hasText(itemName)) {
        builder.and(item.itemName.like("%" + itemName + "%"));
    }
    if (maxPrice != null) {
        builder.and(item.price.loe(maxPrice));
    }

    return query.select(item)
            .from(item)
            .where(builder)
            .fetch();
}
```

* `Querydsl`을 사용해서 동적 쿼리 문제를 해결한다.
* `BooleanBuilder`를 사용해서 원하는 `where` 조건들을 넣어주면 된다.
* 이 모든 것을 자바 코드로 작성하기 때문에 동적 쿼리를 매우 편리하게 작성할 수 있다.

#### findAll

```java
@Override
public List<Item> findAll(ItemSearchCond cond) {
    String itemName = cond.getItemName();
    Integer maxPrice = cond.getMaxPrice();

    return query.select(QItem.item)
            .from(QItem.item)
            .where(likeItemName(itemName), maxPrice(maxPrice))
            .fetch();
}

private BooleanExpression likeItemName(String itemName) {
    if (StringUtils.hasText(itemName)) {
        return QItem.item.itemName.like("%" + itemName + "%");
    }
    return null;
}

private BooleanExpression maxPrice(Integer maxPrice) {
    if (maxPrice != null) {
        return QItem.item.price.loe(maxPrice);
    }
    return null;
}
```

* Querydsl에서 `where(A,B)`에 다양한 조건들을 직접 넣을 수 있는데, 이렇게 넣으면 `AND` 조건으로 처리된다.
    * 참고로 `where()`에 `null`을 입력하면 해당 조건은 무시한다.
* 이 코드의 또 다른 장점은 `likeItemName()`, `maxPrice()`를 다른 쿼리를 작성할 때 재사용 할 수 있다는 점이다.
* 쉽게 이야기해서 쿼리 조건을 부분적으로 모듈화 할 수 있다. 자바 코드로 개발하기 때문에 얻을 수 있는 큰 장점이다.

### 설정

#### QueryDslConfig

```java
@Configuration
@RequiredArgsConstructor
public class QueryDslConfig {

    private final EntityManager em;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV3(em);
    }
}
```

#### MainApplication

```java
@Import(QueryDslConfig.class)
@SpringBootApplication(scanBasePackages = "hello.springdb2.controller")
public class SpringDb2Application { ... }
```

## 정리

### Querydsl 장점

* 쿼리 문장에 오타가 있어도 컴파일 시점에 오류를 막을 수 있다.
* 메서드 추출을 통해서 코드를 재사용할 수 있다.
    * 예를 들어서 여기서 만든 `likeItemName(itemName)`, `maxPrice(maxPrice)` 메서드를 다른 쿼리에서도 함께 사용할 수 있다.

Querydsl을 사용해서 자바 코드로 쿼리를 작성하는 장점을 느껴보았을 것이다.
그리고 동적 쿼리 문제도 깔끔하게 해결해보았다.

Querydsl은 이 외에도 수 많은 편리한 기능을 제공한다.
예를 들어서 최적의 쿼리 결과를 만들기 위해서 DTO로 편리하게 조회하는 기능은 실무에서 자주 사용하는 기능이다.
JPA를 사용한다면 스프링 데이터 JPA와 Querydsl은 실무의 다양한 문제를 편리하게 해결하기 위해 선택하는 기본 기술이라 생각한다.

Querydsl에 대한 자세한 내용은 실전! Querydsl 강의를 참고하자