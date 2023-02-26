# 프로젝트 구조 설명

## MemoryConfig

```java

@Configuration
public class MemoryConfig {
    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new MemoryItemRepository();
    }
}
```

## TestDataInit

```java

@Slf4j
@RequiredArgsConstructor
public class TestDataInit {
    private final ItemRepository itemRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        log.info("test data init");
        itemRepository.save(new Item("itemA", 10000, 10));
        itemRepository.save(new Item("itemB", 20000, 20));
    }
}
```

* `@EventListener(ApplicationReadyEvent.class)`
    * 스프링 컨테이너가 완전히 초기화를 다 끝내고, 실행 준비가 되었을 때 발생하는 이벤트이다.
    * 스프링이 이 시점에 해당 애노테이션이 붙은 `initData()` 메서드를 호출해준다.
    * 참고로 이 기능 대신 `@PostConstruct`를 사용할 경우 AOP 같은 부분이 아직 다 처리되지 않은 시점에 호출될 수 있기 때문에, 간혹 문제가 발생할 수 있다.
        * 예를 들어서 `@Transactional`과 관련된 AOP가 적용되지 않은 상태로 호출될 수 있다.
    * `@EventListener(ApplicationReadyEvent.class)`는 AOP를 포함한 스프링 컨테이너가 완전히 초기화 된 이후에 호출되기 때문에 이런 문제가 발생하지 않는다.

## MainApplication

```java

@Import(MemoryConfig.class)
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

* `@Import("MemoryConfig.class")`
    * `MemoryConfig`를 설정파일로 사용한다.
* `scanBasePackages = "hello.springdb2.controller"`
    * 컨트롤러만 컴포넌트 스캔을 진행하고 나머지는 `MemoryConfig`를 통해 직접 빈을 등록한다.
* `@Profile("local")`
    * 특정 프로필의 경우에만 해당 스프링 빈을 등록한다.

## 프로필

```properties
# PROFILE
spring.profiles.active = local
```

[공식 Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)

스프링 로딩 시점에 spring.profiles.active 속성을 읽어서 프로필로 사용한다.

이 프로필은 로컬, 운영 환경, 테스트 실행 등등 다양한 환경에 따라서 다른 설정을 할 때 사용하는 정보이다.

예를 들어서 로컬 PC에서는 로컬 PC에 설치된 데이터베이스에 접근해야 하고, 운영 횐경에서는 운영 데이터베이스에 접근해야 한다면 서로 설정 정보가 달라야 한다.
심지어 환경에 따라서 다른 스프링 빈을 등록해야 할 수 도 있다. 프로필을 사용하면 이런 문제를 깔끔하게 해결할 수 있다.

```
## 프로필을 설정한 경우
The following 1 profile is active: "local"

## 프로필을 설정하지 않은 경우
No active profile set, falling back to 1 default profile: "default"
```
