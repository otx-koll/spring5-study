# JSON 응답과 요청 처리

## JSON

웹 페이지에서 Ajax를 이용하여 서버 API를 호출하는 사이트가 많다. 이 API는 웹 요청에 대한 응답으로 HTML 대신 JSON이나 XML을 사용한다. `JSON(JavaScript Object Notation)`은 간단한 형식을 갖는 문자열로 데이터 교환에 주로 사용한다. 아래는 JSON 형식으로 표현한 데이터의 예시이다.

```json
{
    "name" : "유관순",
    "birthday" : "1902-12-16",
    "age" : 17,
    "related" : ["남동순", "류예도"],
    "edu" : [
        {
            "title" : "이화학당보통과",
            "year" : 1916
        },
        {
            "title" : "이화학당고등과",
            "year" : 1916
        },
        {
            "title" : "이화학당고등과",
            "year" : 1919
        }
    ]
}
```

중괄호를 사용해서 객체를 표현한다. 객체는 (이름, 값) 쌍을 갖는다. 이름과 값은 콜론`:`으로 구분한다. 값에는 다음과 같은 것들이 올 수 있다.

- 문자열, 숫자, boolean, null
- 배열
- 다른 객체

배열은 대괄호로 표현한다. 대괄호 안에 콤마로 구분한 값 목록을 갖는다. 위 코드에서 `related` 배열은 문자열 값 목록을 갖고 있고 `edu` 배열은 객체를 값 목록으로 갖고 있다.

## Jackson 의존 설정

`Jasckson`은 자바 객체와 JSON 형식 문자열 간의 변환을 처리하는 라이브러리이다. 스프링에서 사용하려면 `Jackson` 라이브러리를 추가하면 된다. 

```xml
<!-- Jackson core와 Jackson Annotation 의존 추가 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.9.4</version>
</dependency>
<!-- java8 date/time 지원 위한 Jackson 모듈 -->
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
    <version>2.9.4</version>
</dependency>
```

자바 객체와 JSON 간의 변환을 처리한다.

```java
public class Person {
    private String name;
    private int age;

    // ..get/set 메서드
}
```

```json
{
    "name" : "이름",
    "age" : 10
}
```

`Jackson`은 프로퍼티의 이름과 값을 JSON객체의 (이름, 값) 쌍으로 사용한다. Person 객체의 name 프로퍼티 값이 "이름"이라고 할 때 생성되는 JOSN 형식 데이터는 이름이 "name"이고 값이 "이름"인 데이터를 갖는다.

## @RestController로 JSON 형식 응답

스프링 MVC에서 JSON 형식으로 데이터를 응답하는 방법은 `Controller` 에노테이션 대신에 `@RestController` 에노테이션을 사용하면 된다. 

이 에노테이션을 붙이면 스프링 MVC는 요청 매핑 에노테이션을 붙인 메서드가 리턴한 객체를 알맞은 형식으로 변환해서 응답 데이터로 전송한다. 이때 클래스 패스에 `Jackson`이 존재하면 JSON 형식의 문자열로 변환해서 응답한다. 

