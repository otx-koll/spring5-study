# MVC 2 : 메시지, 커맨드 객체 검증

## \<spring:message> 태그로 메시지 출력하기

이때까지 UI은 JSP에 직접 코딩했지만 다음과 같은 문제점이 있다. 코드 관리에 용이하지 못하고, 다국어 지원에 문제가 있다. 보통 사용자 설정에 따라 각 언어에 맞게 문자열을 표시해야 하는데 뷰 코드에 '이메일' 이라고 하드 코딩되어 있으면 언어별로 뷰 코드를 따로 만들어야 하는 상황이 발생한다.

이 문제를 해결하기 위해서는 뷰 코드에 사요할 문자열을 언어별로 파일에 보관하고, 뷰 코드는 언어에 따라 알맞은 파일에서 문자열을 읽어와 출력하는 것이다. 다음과 같은 작업을 하면 사용할 수 있다.

- 문자열을 담은 메시지 작성

- 메시지 파일에서 값을 읽어오는 `MessageSource` 빈을 설정

- JSP 코드에서 `<spring:message>` 태그를 사용해서 메시지 출력

메시지 파일을 보관하기 위해 `src/main/resources`에 `message`폴더를 생성하고 이 폴더에 `label.properties` 파일을 생성한다. 파일을 열면 Text Editor를 사용해서 열게 되는데 한글 문자가 `uC774/uBA54` 같은 유니코드로 표현되어 알아보기 힘들기 때문에 다음과 같은 설정을 한다.

```
[Properties] -> Resource -> Text File Encoding -> UTF-8로 변경
```

아래는 label.properties 코드이다.

```
member.register=회원가입

term=약관
term.agree=약관동의
next.btn=다음단계

member.info=회원정보
email=이메일
name=이름
password=비밀번호
password.confirm=비밀번호 확인
register.btn=가입 완료

register.done=<strong>{0}님 ({1})</strong>, 회원 가입을 완료했습니다.

go.main=메인으로 이동
```


> 스프링은 지역에 상관없이 일관된 방법으로 문자열을 관리할 수 있는 `MessageSource` 인터페이스를 정의한다. `MessageSource`를 잘 설계한다면 국내에서 접근하면 한국어로 메시지를 보여주고, 해외에서 접근하면 영어로 메시지를 보여주는 처리를 할 수 있다. 그 코드는 다음과 같다.

```java
public interface MessageSource {
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

    String getMessage(String code, Object[] args, Locale locale)
        throws NoSuchMessageException;

    ... // 일부 메서드 생략
}
```

`MessageSource` 타입의 Bean을 추가한다. 스프링 설정 중 한 곳에 추가하면 된다.

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    ...
	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource ms = 
				new ResourceBundleMessageSource();
		ms.setBasenames("message.label");
		ms.setDefaultEncoding("UTF-8");
		return ms;
	}
}
```

`basenames`프로퍼티 값으로 `message.label`을 주었는데 이는 `message` 패키지에 속한 `label` 프로퍼티 파일로부터 메시지를 읽어온다고 설정한 것이다. `src/main/resources` 폴더도 클래스 패스에 포함되고 message 폴더는 message 패키지에 대응한다. `setBasenames()` 메서드는 사용할 메시지 프로퍼티 목록을 전달할 수 있다.

반드시 주의할 점은 Bean의 아이디를 `messageSource`로 지정해야 한다. 다른 이름을 사용할 경우 정상적으로 동작하지 않는다.

```html
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<head>
    <title><spring:message code="member.register" /></title>
</head>
<body>
    <h2><spring:message code="member.info" /></h2>
    <form:form action="step3" modelAttribute="registerRequest">
    <p>
        <label><spring:message code="email" />:<br>
        <form:input path="email" />
        <form:errors path="email"/>
        </label>
    </p>
    </form:form>
</body>
```

`<spring:message>` 커스텀 태그를 사용하기 위해 태그 라이브러리 설정을 추가하고, 태그를 이용해서 메시지를 출력한다. `<spring:message>` 태그의 code 값은 앞서 작성한 프로퍼티 파일의 프로퍼티 이름과 일치한다. 

앞서 작성한 label.properties 코드를 보면 아래와 같은 프로퍼티를 포함한다.

```
register.done=<strong>{0}님 ({1})</strong>, 회원 가입을 완료했습니다.
```

이 프로퍼티는 값 부분에 {0}을 포함한다. {0}은 인덱스 값을 표시한 것으로, `MessageSource`의 `getMessage()` 메서드는 인덱스 기반 변수를 전달하기 위해 `Object` 배열 타입의 파라미터를 사용한다. 

```java
String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

String getMessage(String code, Object[] args, Locale locale)
```

위 메서드를 사용해서 `messageSource` 빈을 직접 실행한다면 다음과 같이 `Object` 배열을 생성하여 인덱스 기반 변수값을 전달할 수 있다.

```java
Object[] args = new Object[1];
args[0] = "자바";
messageSource.getMessage("register.done", args, Locale.KOREA);
```

`<spring:message>` 태그를 사용하면 arguments 속성을 사용해서 두 가지 방법으로 인덱스 기반 변수값을 전달한다. 
```html
<spring:message code="register.done">
    <spring:argument value="${registerRequest.name}" />
    <spring:argument value="${registerRequest.email}" />
</spring:message>
```
```html
<spring:message code="register.done" 
                arguments="${registerRequest.name}, ${registerRequest.email}">
</spring:message>
```

## 커맨드 객체의 값 검증과 에러 메시지 처리

만약 비정상 값을 입력해도 동작하는 문제가 있다면 입력한 값에 대한 검증 처리를 해야한다. 

그리고 사용자에게 입력 값에 대한 실패 이유를 알려줘야 할 필요가 있다. 스프링은 이 두 가지 문제를 처리하기 위하 다음 방법을 제공한다.

- 커맨드 객체를 검증하고 결과를 에러 코드로 저장

-  JSP에서 에러 코드로부터 메시지 출력

### 에러 코드 처리

객체 검증할 때 사용하는 `Validator` 인터페이스는 다음과 같다.

```java
boolean supports(Class<?> clazz) {
    // 인스턴스가 검증 대상 타입인지 확인
}
void validate(Object target, Errors errors) {
    // 실질적인 검증 작업
}
```

```java
public class RegisterRequestValidator implements Validator {
	private static final String emailRegExp = 
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
			"[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private Pattern pattern;

	public RegisterRequestValidator() {
		pattern = Pattern.compile(emailRegExp);
		System.out.println("RegisterRequestValidator#new(): " + this);
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return RegisterRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		System.out.println("RegisterRequestValidator#validate(): " + this);
		RegisterRequest regReq = (RegisterRequest) target;
		if (regReq.getEmail() == null || regReq.getEmail().trim().isEmpty()) {
			errors.rejectValue("email", "required");
		} else {
			Matcher matcher = pattern.matcher(regReq.getEmail());
			if (!matcher.matches()) {
				errors.rejectValue("email", "bad");
			}
		}
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "required");
		ValidationUtils.rejectIfEmpty(errors, "password", "required");
		ValidationUtils.rejectIfEmpty(errors, "confirmPassword", "required");
		if (!regReq.getPassword().isEmpty()) {
			if (!regReq.isPasswordEqualToConfirmPassword()) {
				errors.rejectValue("confirmPassword", "nomatch");
			}
		}
	}
}
```
- `supports()` 메서드는 파라미터로 전달받은 clazz 객체가 RegisterRequest 클래스로 타입 변환이 가능한지 확인한다. 
- `validate()` 메서드는 두 개의 파라미터를 갖는데 targer 파라미터는 검사 대상 객체이고 errors 파라미터는 검사 결과 에러 코드를 설정하기 위한 객체이다. validate() 메서드는 보통 다음과 같이 구현한다.

	- 검사 대상 객체의 특정 프로퍼티나 상태가 올바른지 검사
	- 올바르지 않으면 Error의 `rejectValue()` 메서드를 이용해서 에러 코드 저장

`rejectValue()` 메서드는 첫 번째 파라미터로 프로퍼티의 이름을 전달받고, 두 번쨰 파라미터로 에러 코드를 전달받는다. 

```java
ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "required");
```

`Validatetionutils` 클래스는 객체의 값 검증 코드를 간결하게 작성할 수 있도록 도와준다. 검사 대상 객체의 "name" 프로퍼티가 null이거나 공백문자로만 되어 있는 경우 "name" 프로퍼티의 에러 코드로 "required"를 추가한다. 위 코드는 아래 코드와 동일하다.

```java
String name = regReq.getName();
if (name == null || name.trim().isEmpty()) {
	errors.rejectValue("name", "required");
}
```

### 커맨드 객체의 에러 메시지 출력

Errors에 에러 코드를 추가하면 `<form:errors>`태그를 사용해서 에러에 해당하는 메시지를 출력할 수 있다.

```html
<label><spring:message code="email" />:<br>
<form:input path="email" />
<form:errors path="email"/>
</label>
```

`label.properties`에 다음 메시지를 추가한다.

```
required=필수항목입니다.
bad.email=이메일이 올바르지 않습니다.
duplicate.email=중복된 이메일입니다.
nomatch.confirmPassword=비밀번호와 확인이 일치하지 않습니다.
```

`<form:errors>` 태그의 주요 속성은 다음과 같다.

태그|속성
-|-
element| 각 에러 메시지를 출력할 떄 사용할 HTML 태그. 기본 값은 span이다.
delimiter | 각 에러 메시지를 구분할 떄 사용하는 HTML 태그. 기본 값은 `<br/>`이다.

## Bean Validation을 이용한 값 검증 처리

`@Valid`에노테이션을 사용하면 `Validator` 작성 없이 애노테이션만으로 커맨드 객체의 값 검증을 처리할 수 있다.

Bean Validation이 제공하는 에노테이션을 이용해서 커맨드 객체의 값을 검증하는 방법은 다음과 같다.

- Bean Validation과 관련된 의존을 설정에 추가
- 커맨드 객체에 `@NotNull`, `@Digits`등의 에노테이션을 이용해서 검증 규칙을 설정

첫 번째로 해야 할 작업은 Bean Validation에 관련 의존을 추가하는 것이다. Hibernate Validator를 사용할 것이기 때문에 pom.xml에 의존설정을 추가한다.

```xml
<dependency>
	<groupId>javax.validation</groupId>
	<artifactId>validation-api</artifactId>
	<version>1.1.0.Final</version>
</dependency>
<dependency>
	<groupId>org.hibernate</groupId>
	<artifactId>hibernate-validator</artifactId>
	<version>5.4.2.Final</version>
</dependency>
```

Bean Validation과 프로바이더가 제공하는 에노테이션을 이용해서 값 검증 규칙을 설정할 수 있다.

```java
public class RegisterRequest {
	@NotBlank
	@Email
	private String email;
	@Size(min = 6)
	private String password;
	@NotEmpty
	private String confirmPassword;
	@NotEmpty
	private String name;
```

그 다음으로는 Bean Validation 에노테이션을 적용한 커맨드 객체를 검증할 수 있는 `OptionalValidatorFactoryBean` 클래스를 Bean으로 등록한다.

`@EnableWebMvc` 에노테이션을 사용하면 `OptionalValidatorFactoryBean`을 글로벌 범위 Validator로 등록한다.

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
	...
}
```

그 다음 `@Valid`에노테이션을 붙여서 글로벌 범위 Validator로 검증한다.

```java
@PostMapping("/register/step3")
public String handleStep3(@Valid RegisterRequest regReq, Errors errors) {
	...
}
```
아래와 같이 Validator를 따로 설정하면 글로벌 범위 Validator로 사용하지 않는다.

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
	@Override
	public Validator getValidator() {
		return new RegisterRequestValidator();
	}
}
```
메시지 프로퍼티 파일에 규칙에 맞게 에러 메시지를 등록하면 기본 에러 메시지 대신 원하는 에러 메시지를 출력할 수 있다. `label.properties`에 다음과 같이 추가한다.

```
NotBlank=필수 항목입니다. 공백 문자는 허용하지 않습니다.
NotEmpty=필수 항목입니다.
Size.password=암호 길이는 6자 이상이어야 합니다.
Email=올바른 이메일 주소를 입력해야 합니다.
```