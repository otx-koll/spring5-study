# chap 07 AOP 프로그래밍

## 프로젝트 준비

pom.xml 파일에 의존 추가

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.7</version>
    <scope>runtime</scope>
</dependency>
```

> 양의 정수 n의 계승은 n!으로 표현되며 n!은 1부터 n까지 숫자의 곱을 의미