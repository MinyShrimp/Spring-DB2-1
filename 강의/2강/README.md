# 데이터 접근 기술 - 테스트

## 테스트 - 데이터베이스 연동

### test/application.properties

```properties
# PROFILE
spring.profiles.active = test

# DataSource
spring.datasource.url = jdbc:h2:tcp://localhost/~/test
spring.datasource.username = sa

# JdbcTemplate SQL Log
logging.level.org.springframework.jdbc = debug
```

## 테스트 - 데이터베이스 분리

### 분리

H2 데이터베이스를 용도에 따라 2가지로 구분하면 된다.

* `jdbc:h2:tcp://localhost/~/test`
    * local에서 접근하는 서버 전용 데이터베이스
* `jdbc:h2:tcp://localhost/~/testcase`
    * test 케이스에서 사용하는 전용 데이터베이스

#### Database 생성

* 데이터베이스 서버를 종료하고 다시 실행한다.
* 사용자명은 sa 입력
* JDBC URL에 다음 입력,
* `jdbc:h2:~/testcase` (최초 한번)
* `~/testcase.mv.db` 파일 생성 확인
* 이후부터는 `jdbc:h2:tcp://localhost/~/testcase` 이렇게 접속

#### test/application.properties 변경

```properties
# PROFILE
spring.profiles.active = test

# DataSource
spring.datasource.url = jdbc:h2:tcp://localhost/~/testcase
spring.datasource.username = sa
```

### 테스트 실행

* `findItems()` 테스트만 단독으로 실행해보자
* 처음에는 실행에 성공한다.
* 그런데 같은 `findItems()` 테스트를 다시 실행하면 테스트에 실패한다.

* 테스트를 2번째 실행할 때 실패하는 이유는 `testcase` 데이터베이스에 접속해서 item 테이블의 데이터를 확인하면 알 수 있다.
* 처음 테스트를 실행할 때 저장한 데이터가 계속 남아있기 때문에 두번째 테스트에 영향을 준 것이다.
* 이 문제는 save() 같은 다른 테스트가 먼저 실행되고 나서 findItems() 를 실행할 때도 나타난다.
    * 다른 테스트에서 이미 데이터를 추가했기 때문이다. 결과적으로 테스트 데이터가 오염된 것이다.
* 이 문제를 해결하려면 각각의 테스트가 끝날 때 마다 해당 테스트에서 추가한 데이터를 삭제해야 한다.
    * 그래야 다른 테스트에 영향을 주지 않는다.

#### 테스트의 중요한 원칙

* 테스트는 다른 테스트와 격리해야 한다.
* 테스트는 반복해서 실행할 수 있어야 한다.

물론 테스트가 끝날 때 마다 추가한 데이터에 DELETE SQL 을 사용해도 되겠지만, 이 방법도 궁극적인 해결책은 아니다.
만약 테스트 과정에서 데이터를 이미 추가했는데,
테스트가 실행되는 도중에 예외가 발생하거나 애플리케이션이 종료되어 버려서 테스트 종료 시점에 DELETE SQL 을 호출하지 못할 수 도 있다!
그러면 결국 데이터가 남아있게 된다.

## 테스트 - 데이터 롤백

## 테스트 - @Transaction

## 테스트 - 임베디드 모드 DB

## 테스트 - 스프링 부트와 임베디드 모드