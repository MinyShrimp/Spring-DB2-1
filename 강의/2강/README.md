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

## 테스트 - 데이터 롤백

## 테스트 - @Transaction

## 테스트 - 임베디드 모드 DB

## 테스트 - 스프링 부트와 임베디드 모드