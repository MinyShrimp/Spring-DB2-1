# 데이터 접근 기술 - Querydsl

## Querydsl 소개 1 - 기존 방식의 문제점

## Querydsl 소개 2 - 해결

## Querydsl 설정

### 설정

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

## Querydsl 적용
