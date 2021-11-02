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


