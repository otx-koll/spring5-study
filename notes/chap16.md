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

### @JsonIgnore를 이용한 제외 처리

보호가 필요한 데이터는 응답 결과에 포함시키면 안되므로 `@JsaonIgnore` 에노테이션을 이용해서 제외시킬 수 있다. 포함시키지 않을 대상에 `@JsaonIgnore` 에노테이션을 붙인다.

```java
public class Member {

	private Long id;
	private String email;
	@JsonIgnore
	private String password;
	private String name;
    private LocalDateTime registerDateTime;
}
```

### @JsonFormat을 이용한 날짜 형식 변환 

`@JsonFormat` 에노테이션을 사용하면 날짜나 시간 값을 특정한 형식으로 표현할 수 있다. 만약 ISO-8601 형식으로 변환하고 싶으면 아래와 같이 shape 속성 값으로 `Shape.STRING`을 갖는 `@JsonFormmat` 에노테이션을 변환 대상에 적용하면 된다.

```java
@JsonFormat(shape = Shape.STRING) // ISO-8601 형식으로 변환
private LocalDateTime registerDateTime;
```

원하는 형식으로 변환해서 출력하고 싶으면 `@JsonFormmat` 에노테이션의 pattern 속성을 사용한다. 아래 코드는 pattern 속성의 사용 예이다.

```java
@JsonFormat(pattern = "yyyyMMddHHmmss")
private LocalDateTime registerDateTime;
```

### 날짜 형식 변환 처리

모든 대상에 동일한 변환 규칙을 적용할 수도 있다. `@JsonFormmat` 에노테이션을 사용하지 않고 `Jackson`의 변환 규칙을 모든 날짜 타입에 적용하려면 스프링 MVC 설정을 변경해야 한다.

아래 코드는 모든 날짜 타입을 ISO-8601 형식으로 변환하기 위한 설정 코드이다.

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    ... // 생략

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
                .json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .simpleDateFormat("yyyy-MM-dd HH:mm:ss") // Date를 위한 변환 패턴
                .build();
        converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
    }

}
```
`extendMessageConverters`는 등록된 `HttpMessageConverter` 목록을 파라미터로 받는다. 

미리 등록된 `HttpMessageConverter`에는 `Jackson`을 이용하는 것도 포함되어 있기 때문에 새로 생성한 `HttpMessageConverter`는 목록의 제일 앞에 위치시켜야 한다. 그래야 가장 먼저 적용된다. 이를 위해 `converters.add`에서 0번 인덱스에 추가했다. 

`objectMapper`는 JSON으로 변환할 떄 사용할 `ObjectMapper`를 생성한다. `Jackson2ObjectMapperBuilder`는 `ObjectMapper`를 보다 쉽게 생성할 수 있도록 스프링이 제공하는 클래스이다. `featuresToDisable`는 유닉스 타임 스탬프로 출력되는 기능을 비활성화한다. 비활성화하면 날짜 타입이 ISO-8601 형식으로 출력된다.

새로 생성한 `ObjectMApper`를 사용하는 `MappingJackson2HttpMessageConverter` 객체를 `converters`의 첫 번째 항목으로 등록하면 설정이 끝난다.

```java
@Override
public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
            .json()
            .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(formatter))
            .build();
    converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
}
```

모든 `LocalDateTime` 타입에 대해 ISO-8601 형식 대신 원하는 패턴을 설정하고 싶다면 `serializerByType()` 메서드를 이용하여 직접 설정하면 된다. 

## @RequestBody로 JSON 요청 처리

커맨드 객체에 `@RequestBody` 에노테이션을 붙이면 JSON 형식으로 전송된 요청 데이터를 커맨드 객체로 전달받을 수 있다. 다음은 예제 코드이다.

```java
@PostMapping("/api/members")
public void newMember(
    @RequestBody @Valid RegisterRequest regReq,
    HttpServletResponse response) throws IOException {
    try {
        Long newMemberId = registerService.regist(regReq);
        response.setHeader("Location","/api/members/" + newMemberId);
        response.setStatus(HttpServletResponse.SC_CREATED);
    } catch (DuplicateMemberException dupEx) {
        response.sendError(HttpServletResponse.SC_CONFLICT);
    }
}
```

스프링 MVC가 JSON 형식으로 전송된 데이터를 올바르게 처리하려면 요청 컨텐츠 타입이 application/json이어야 한다. `newMember()` 메서드는 회원 가입을 정상적으로 처리하면 응답코드로 201(CREATED)을 전송한다. 중복된 ID를 전송한 경우 응답 상태 코드로 409(CONFLICT)를 리턴한다. 

### JSON 데이터의 날짜 형식 다루기

특정 패턴을 가진 문자열을 `LocalDateTime`이나 `Date` 타입으로 변환하고 싶다면 `@JsonFormat` 에노테이션의 pattern 속성을 사용해서 패턴을 지정한다.

해당 타입을 갖는 모든 속성에 적용하고 싶다면 스프링 MVC 설정을 추가하면 된다.

```java
@Override
public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
            .json()
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(formatter))
            .simpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .build();
    converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
}
```
`deserializerByType()`는 JSON 데이터를 `LocalDateTime` 타입으로 변환할 때 사용할 패턴을 지정한다.

### 요청 객체 검증하기

`@Valid` 에노테이션이나 별도 `Validator`를 이용해서 JSON 형식으로 전송한 데이터를 변환한 객체도 동일한 방식으로 검증할 수 있다. `Validator`를 사용할 경우 다음과 같이 직접 상태 코드를 처리해야 한다.

```java
@PostMapping("/api/members")
public void newMember(
        @RequestBody RegisterRequest regReq, Errors errors, 
        HttpServletResponse response) throws IOException {
    try {
        new RegisterRequestValidator().validate(regReq, errors);
        if (errors.hasErrors()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
       ...
    } catch (DuplicateMemberException dupEx) {
        response.sendError(HttpServletResponse.SC_CONFLICT);
    }
}
```

## ResponseEntity로 객체 리턴하고 응답 코드 지정하기

상태 코드를 지정하기 위해 `HttpServletResponse`의 `setStatus()` 메서드와 `sendError()`메서드를 사용했다. 

```java
@GetMapping("/api/members/{id}")
public Member member(@PathVariable Long id, HttpServletResponse response) throws IOException {
    Member member = memberDao.selectById(id);
    if (member == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }
    return member;
}
```
`HttpServletResponse`을 이용해서 404 응답을 하면 JSON 형식이 아닌 HTML로 응답 결과를 제공한다. API를 호출하는 프로그램 입장에서 JSON 응답과 HTML 응답을 모두 처리하는 것은 부담스럽다. 따라서 404나 500와 같이 처리에 실패했을 경우 JSON 형식의 응답 데이터를 전송해야 일관된 방법으로 응답을 처리할 수 있다. 

모두 JSON 응답을 전송하는 방법은 `ResponseEntity`이다. 우선 에러 상황일 때 응답으로 사용할 클래스를 다음과 같이 작성한다.

```java
public class ErrorResponse {
	private String message;

	public ErrorResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
```
`ResponseEntity`를 이용하면 `member()` 메서드를 다음과 같이 구현할 수 있다.

```java
@GetMapping("/api/members/{id}")
public ResponseEntity<Object> member(@PathVariable Long id) {
    Member member = memberDao.selectById(id);
    if (member == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("no member"));
    }
    return ResponseEntity.status(HttpStatus.OK).body(member);
}
```
리턴 타입이 `ResponseEntity`이면 `ResponseEntity`의 body로 지정한 객체를 사용해서 변환처리한다. 

`ErrorResponse`객체를 body로 지정했으므로 member가 null이면 `ErrorResponse`를 JSON으로 변환한다. 위 응답 상태코드는 404(NOT_FOUND), 200(OK)이다.

`ResponseEntity`를 생성하는 기본 방법은 status와 body를 이용해서 상태 코드와 JSON으로 변환할 객체를 지정하는 것이다.

```java
ResponseEntity.status(상태코드).body(객체)
```

200(OK) 응답 코드와 몸체 데이터를 생성할 경우 다음과 같이 `ok()`메서드를 이용해서 생성할 수도 있다.

```java
ResponseEntity.ok(member)
```

몸체 내용이 없다면 body를 지정하지 않고 `bulid()`로 바로 생성한다.

```java
ResponseEntity.status(HttpStatus.NOT_FOUND).build()
```

몸체 내용이 없을 경우 `status()` 메서드 대신 다음과 같이 관련 메서드를 사용해도 된다. 
```java
ResponseEntity.notFound().build()
```

몸체가 없을 때 `status()` 대신 사용할 수 있는 메서드는 다음과 같다.

- `noContent()` : 204
- `badRequest()` : 400
- `notFound()` : 404

`newMember()` 메서드를 `ResponseEntity`로 구현하면 다음과 같다. `ResponseEntity.created()` 메서드에 `Location` 헤더로 전달할 URI를 전달하면 된다.

```java
@PostMapping("/api/members")
public ResponseEntity<Object> newMember(
        @RequestBody @Valid RegisterRequest regReq)
    try {
        Long newMemberId = registerService.regist(regReq);
        URI uri = URI.create("/api/members/" + newMemberId);
        return ResponseEntity.created(uri).build();
    } catch (DuplicateMemberException dupEx) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
```

### @ExceptionHandler 적용 메서드에서 ResponseEntity로 응답하기

한 메서드에 정상 응답과 에러 응답을 `ResponseBody`로 생성하면 코드가 중복될 수 있다. 

```java
@GetMapping("/api/members/{id}")
public ResponseEntity<Object> member(@PathVariable Long id) {
    Member member = memberDao.selectById(id);
    if (member == null) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("no member"));
    }
    return ResponseEntity.ok(member);
}
```
member가 존재하지 않을 때 HTML 에러 응답 대신 JSON 응답을 제공하기 위해 `ResponseEntity`를 사용했다. 그런데 회원이 존재하지 않을 때 404 상태 코드를 응답해야 하는 기능이 많다면 에러 응답을 위해 `ResponseEntity`를 생성하는 코드가 여러 곳에 중복된다.

이럴 때 `@ExceptionHandler` 에노테이션을 적용한 메서드에서 에러 응답을 처리하도록 구현하면 중복을 없앨 수 있다. 

```java
@GetMapping("/api/members/{id}")
public Member member(@PathVariable Long id) {
    Member member = memberDao.selectById(id);
    if (member == null) {
        throw new MemberNotFoundException();
    }
    return member;
}

@ExceptionHandler(MemberNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNoData() {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("no member"));
}
```
`handleNoData()`는 404 상태 코드와 `ErrorResponse` 객체를 몸체로 갖는 `ResponseEntity`를 리턴한다. 따라서 `MemberNotFoundException`이 발생하면 상태 코드가 404이고 몸체가 JOSN 형식인 응답을 전송한다.

`@RestControllerAdvice` 에노테이션을 이용하면 에러 처리 코드를 별도 클래스로 분리할 수도 있다. `@ControllerAdvice` 에노테이션과 동일하지만 `@RestControllerAdvice`는 `@RestController` 에노테이션과 동일하게 응답을 JSON이나 XML과 같은 형식으로 변환한다. 

```java
@RestControllerAdvice("controller")
public class ApiExceptionAdvice {

	@ExceptionHandler(MemberNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoData() {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse("no member"));
	}

}
```

`@RestControllerAdvice` 에노테이션을 사용하면 에러 처리 코드가 한 곳에 모여 에러 응답을 효과적으로 관리할 수 있게 된다.

### @Valid 에러 결과를 JSON으로 응답하기

`@Valid` 에노테이션을 붙인 커맨드 객체가 값 검증에 실패하면 400 상태 코드를 응답한다. 문제는 `HttpServletResponse`를 이용해서 상태 코드를 응답했을 때와 같이 HTML 응답을 전송한다. HTML 응답 대신 JSON 형식 응답을 제공하고 싶다면 `Errors` 타입 파라미터를 추가해서 직접 에러 응답을 생성하면 된다.

```java
@PostMapping("/api/members")
public ResponseEntity<Object> newMember(
        @RequestBody @Valid RegisterRequest regReq, Errors errors) {
    if (errors.hasErrors()) {
        String errorCodes = errors.getAllErrors()
                .stream()
                .map(error -> error.getCodes()[0])
                .collect(Collectors.joining(","));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("errorCodes = " + errorCodes));
    }
    ... // 생략
}
```

`hasErrors()` 메서드를 이용하여 검증 에러가 존재하는지 확인한다. 존재한다면 `getAllErrors()` 메서드로 모든 에러 정보를 구하고 각 에러 코드 값을 연결한 문자열을 생성해서 `errorCodes` 변수에 할당한다. 

`@RequestBody` 에노테이션을 붙인 경우 `@Valid` 에노테이션을 붙인 객체 검증에 실패했을 때 `Errors` 타입 파라미터가 존재하지 않으면 `MethodArgumentNotValidException`가 발생한다. 따라서 `@ExceptionHandler`을 이용해서 에러 응답을 생성할 수 있다.

```java
@RestControllerAdvice("controller")
public class ApiExceptionAdvice {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleBindException(MethodArgumentNotValidException ex) {
		String errorCodes = ex.getBindingResult().getAllErrors()
				.stream()
				.map(error -> error.getCodes()[0])
				.collect(Collectors.joining(","));
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse("errorCodes = " + errorCodes));
	}

}
```