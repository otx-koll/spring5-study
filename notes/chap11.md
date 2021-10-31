# MVC 1 : 요청 매핑, 커맨드 객체, 리다이렉트, 폼 태그, 모델

## 요청 매핑 애노테이션

- `@RequestMapping`
- `@GetMapping`
- `@PostMapping`

요청 매핑 애노테이션을 적용한 메서드를 두 개 이상 정의할 수도 있다. 여러 단계를 거쳐 하나의 기능이 완성되는 경우, 관련 요청 경로를 한개의 컨트롤러 클래스에서 처리하면 코드 관리에 용이하다.

```java
@Controller
@RequestMapping("/register")
public class RegisterController { // 각 메서드에 공통되는 경로

	@RequestMapping("/step1") // 공통 경로를 제외한 나머지 경로
	public String handleStep1() {
		return "register/step1";
	}

}
```

요청 매핑 애노테이션의 경로가 `/register`로 시작한다. 공통된 부분의 경로를 `@RequestMapping` 애노테이션을 클래스에 적용하고, 나머지 경로를 값으로 갖는 요청 매핑 애노테이션을 적용할 수 있다.

### GET과 POST구분

주로 폼을 전송할 때 POST 방식을 사용하는데 스프링 MVC는 별도 설정이 없으면 GET과 POST 방식에 상관없이 `@RequestMapping`에 지정한 경로와 일치하는 요청을 처리한다.

스프링 4.3버전 이전에는 다음 코드처럼 `@RequestMapping` 애노테이션의 method 속성을 사용해서 HTTP 방식을 제한했다.

```java
@Controller
public class LoginController() {
    
    @RequestMapping(value="/member/login", method=RequestMethod.GET)
    public String form() {
        ...
    }

    @RequestMapping(value="/member/login", method=RequestMethod.POST)
    public String login() {
        ...
    }
}
```

아래는 스프링 4.3버전에 추가된 `@GetMapping`애노테이션과 `@PostMapping` 애노테이션을 이용한 코드이다.

```java
@Controller
public class LoginController() {
    
    @GetMapping("/member/login")
    public String form() {
        ...
    }

    @PostMapping("/member/login")
    public String login() {
        ...
    }
}
```

### 요청 파라미터 접근

`@RequestParam` 애노테이션을 사용하여 요청 파라미터에 접근할 수 있다. 요청 파라미터 개수가 몇 개 안 되면 이 애노테이션을 사용하여 간단하게 요청 파라미터의 값을 구할 수 있다.

```java
@Controller
public class RegisterController {
	...
	@PostMapping("/register/step2")
	public String handleStep2(@RequestParam(value="agree", defaultValue="false") Boolean agreeVal) {
		if (!agree)
			return "register/step1";
		return "register/step2";
	}
	
}
```

`@RequestParam` 애노테이션의 속성은 다음과 같다.

속성|타입|설명
-|-|-
value|String|HTTP 요청 파라미터의 이름을 지정
required|boolean|필수 여부 지정. 이 값이 true이면서 해당 요청 파라미터에 값이 없으면 익셉션 발생. 기본값은 true이다.
defaultValue|String|요청 파라미터가 값이 없을 때 사용할 문자열 값을 지정한다. 기본값은 없다.

표에 따르면 위 코드는 `agree` 요청 파라미터의 값을 읽어와 `agreeVal` 파라미터에 할당한다. 요청 파라미터의 값이 없으면 `false` 문자열을 값으로 사용한다.

## 리다이렉트 처리

POST 방식만을 사용하는 클래스는 GET 방식 요청은 처리하지않아 브라우저에 직접 주소를 입력하게 되면 405 상태 코드를 응답한다. 이때 에러 화면보단 알맞은 경로로 리다이렉트 하는 편이 더 좋다.

컨트롤러에서 특정 페이지로 리다이렉트 하는 방법은 `redirect:경로`식으로 리턴하면 된다. 예시 코드는 다음과 같다.

```java
@Controller
public class RegisterController {
	...
    
	@GetMapping("/register/step2")
	public String handleStep2Get() {
		return "redirect:/register/step1";
	}

}
```

## 커맨드 객체를 이용하여 요청 파라미터 사용하기

스프링은 요청 파라미터의 값을 커맨드 객체에 담아주는 기능을 제공한다. 예를 들어 이름이 name인 요청 파라미터의 값을 커맨드 객체의 `setName()` 메서드를 사용해서 커맨드 객체에 전달하는 기능을 제공한다. 

커맨드 객체는 다음과 같이 요청 매핑 애노테이션이 적용된 메서드의 파라미터에 위치한다.

```java
@PostMapping("/register/step3")
public String handleStep3(RegisterRequest regReq) {
    ...
}
```
`handleStep3()` 메서드는 `MemberRegisterService`를 이용해서 회원 가입을 처리한다. 회원가입에 성공하면 뷰 이름으로 `register/step3`을 리턴하고, 동일한 이메일 주소를 가진 회원 데이터가 존재하면 뷰 이름으로 `register/step2`를 리턴해서 다시 폼을 보여준다. 

```java
@Controller
public class RegisterController {
	
	private MemberRegisterService memberRegisterService;
	
	public void setMemberRegisterService(MemberRegisterService memberRegisterService) {
		 this.memberRegisterService = memberRegisterService;
	}
	
	...
	
	@PostMapping("/register/step3")
	public String handleStep3(RegisterRequest regReq) {
		try {
			memberRegisterService.regist(regReq);
			return "register/step3";
		} catch (DuplicateMemberException e) {
			return "register/step2";
		}
	}
}
```
`RegisterController` 클래스는 `MemberRegisterService` 타입의 빈을 의존하므로 `ControllerConfig` 파일에 아래 코드와 같이 의존 주입을 설정한다.

```java
@Configuration
public class ControllerConfig {
	
	@Autowired
	private MemberRegisterService memberRegSvc;
	
	@Bean
	public RegisterController registerController() {
		RegisterController controller = new RegisterController();
		controller.setMemberRegisterService(memberRegSvc);
		return controller;
	}
}
```

뷰 jsp 코드에서 커맨드 객체를 사용해서 정보를 표시할 수도 있다. 

```html
<p>${registerRequest.name}님 회원가입을 축하합니다.</p>
```

`registerRequest`가 커맨드 객체에 접근할 때 사용한 속성 이름이다. 스프링 MVC는 커맨드 객체의 (첫 글자를 소문자로 바꾼)클래스 이름과 동일한 속성 이름을 사용하여 커맨드 객체를 뷰에 전달한다. 커맨드 객체 클래스 이름이 `RegisterRequest`인 경우 `registerRequest`라는 이름을 사용해서 커맨드 객체에 접근할 수 있다.

만약 속성 이름을 변경하고 싶다면 커맨드 객체로 사용할 파라미터에 `@ModelAttribute` 애노테이션을 적용하면 된다. 다음 코드와 같다.

```java
@PostMapping("/register/step3")
public String handleStep3(@ModelAttribute("formData") RegisterRequest regReq) {
    ...
}
```

## 커맨드 객체와 스프링 폼 연동

만약 회원 정보 입력 폼에서 중복된 이메일 주소를 입력하면 텅 빈 폼을 보여준다. 비어 있는 폼을 다시 입력해야하는 불편함이 따른다. 다시 폼을 보여줄 때 커맨드 객체의 값을 폼에 채워주면 불편함을 해소할 수 있다.

```html
<input type="text" name="email" id="email" value="${registerRequest.email}">
```

스프링 MVC가 제공하는 커스텀 태그를 사용하면 좀 더 간단히 커맨드 객체의 값을 출력할 수 있다. `<form:form>` 태그와 `<form:input>`태그를 제공하고 있다.

```html
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

...

<body>
    <form:form action="step3" modelAttribute="registerRequest">
        <label>이메일
            <form:input path="email"/>
        </label>
        <label>비밀번호
            <form:password path="password"/>
        </label>
    </form:form>
</body>
```

- `action` : `<form>` 태그와 `action` 속성과 동일한 값을 사용한다.

- `modelAttribute` : 커맨드 객체의 속성 이름을 지정한다. 

`<form:form>` 태그를 사용하려면 커맨드 객체가 존재해야 한다. `step2.jsp`에서 `<form:form>`태그를 사용하기 때문에 `step1.jsp`에서 `step2.jsp`로 넘어오는 단계에서 이름이 `registerRequest`인 객체를 모델에 넣어야 태그가 정상 동작한다.

```java
@PostMapping("/register/step2")
public String handleStep2(@RequestParam(value="agree", defaultValue="false") Boolean agree, Model model) {
    if (!agree)
        return "register/step1";
    model.addAttribute("registerRequest", new RegisterRequest());
    return "register/step2";
}
```

## 컨트롤러 구현 없는 경로 매핑

step3.jsp 코드를 보면 다음 코드와 같다.

```html
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
...
<p><a href="<c:url value='/main'/>">[첫 화면 이동]</a></p>
```

step3.jsp는 회원 가입 완료 후 첫 화면으로 이동할 수 있는 링크를 보여준다. 이를 위한 컨트롤러 클래스는 특별히 처리할 것이 없기 때문에 단순히 뷰 이름만 리턴하면 된다.

```java
@Controller
public class MainController {
    @RequestMapping("/main")
    public String main() {
        return "main";
    }
}
```
 위 코드는 요청 경로와 뷰 이름을 연결해주는 것에 불과하다. 단순 연결을 위해 특별한 로직이 없는 컨트롤러 클래스를 만드는 것은 성가신 일이다. `WebMvcConfigurer` 인터페이스의 `addViewControllers()`메서드를 사용하면 성가신 것을 없앨 수 있다. 

 ```java
@Override
public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/main").setViewName("main");
}
 ```

`/main` 요청 경로에 대해 뷰 이름으로 main을 사용한다고 설정한다. 다음 `MvcConfig`파일에 아래 코드를 추가한다. 

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    ...
    @Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/main").setViewName("main");
	}

}
```

