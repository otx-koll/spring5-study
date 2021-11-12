# MVC 3 : 세션, 인터셉터, 쿠키

## 컨트롤러에서 HttpSession 사용하기

로그인 상태를 유지하는 방법은 `HttpSession`을 이용하거나 `Cookie`를 이용하는 방법이 있다. `Httpsession`을 사용하려면 다음 두 가지 방법 중 한가지를 적용해야 한다.

- 요청 매핑 에노테이션 적용 메서드에 `HttpSession` 파라미터를 추가한다.
- 요청 매핑 에노테이션 적용 메서드에 `HttpServletRequest` 파라미터를 추가하고 `HttpServletRequest`를 이용해서 `HttpSession`을 구한다.

아래는 첫 번째 방법을 사용한 코드의 예시이다.

```java
@PostMapping
public String form(LoginCommand loginCommand, Errors errors, HttpSession session) {
    ... // session을 사용하는 코드
}
```
두 번째 방법은 아래 코드처럼 `HttpServletRequest`의 `getSession()` 메서드를 이용하는 것이다.

```java
@PostMapping
public String submit(LoginCommand loginCommand, Errors errors, HttpServletRequest req) {
    HttpSession session = req.getSession();
    ... // session을 사용하는 코드
}
```

첫 번째 방법은 항상 `HttpSession`을 생성하지만 두 번째 방법은 필요 시점에만 `HttpSession`을 생성할 수 있다.

로그아웃을 위한 컨트롤러 클래스는 `HttpSession`을 제거하면 된다. 

```java
@Controller
public class LogoutController {

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/main";
	}

}
```

`invalidate()` 함수는 세션을 없애고 세션에 속해 있는 값들을 모두 없앤다. 

## 인터셉터 사용하기

로그인을 하지 않은 상태에서 비밀번호 변경 폼을 요청하면 로그인 화면으로 이동시키는 것이 더욱 보안성있다.

따라서 `HttpSession`에 `authInfo`객체가 존재하는지 검사하고, 존재하지 않으면 로그인 경로로 리다이렉트하도록 할 수 있다.

```java
@GetMapping
public String form(@ModelAttribute("command") ChangePwdCommand pwdCmd) {
    AuthInfo authInfo = (AuthInfo) session.getAttribute("authInfo");
    if (authInfo == null) {
        return "redirect:/login";
    }
    return "edit/changePwdForm";
}
```

그러나 실제 웹에서는 비밀번호 변경 기능 외에 더 많은 기능에 로그인 여부를 확인해야 한다. 따라서 각각의 컨트롤러 코드마다 세션 확인 코드를 삽입하는 것은 많은 코드 중복을 일으킨다.

다수의 컨트롤러에 동일한 기능을 적용해야 할 때 사용하는 것이 `HandlerInterceptor`이다.

## HandlerInterceptor

`HandlerInterceptor` 인터페이스를 사용하면 다음 세 시점에 공통 기능을 넣을 수 있다.

- 컨트롤러 실행 전
- 컨트롤러 실행 후, 아직 뷰를 실행하기 전
- 뷰를 실행한 이후

세 시점을 처리하기 위해 다음 메서드를 정의한다.

```java
boolean preHandle( // 컨트롤러 실행 전
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler) throws Exception;

void postHandle( // 컨트롤러 실행 후
    HttpServletRequest request, 
    HttpServletResponse response, 
    Object handler,
    ModelAndView modelAndView) throws Exceptionl;

void afterCompletion( // 뷰를 실행 후
    HttpServletRequest request, 
    HttpServletResponse response, 
    Object handler, 
    Exception ex) throws Exception;
```

`preHandle()` 메서드에서는 컨트롤러 실행 전에 필요한 기능을 구현할 때 사용한다. 이 메서드를 사용하면 로그인하지 않았을 때 컨트롤러를 실행하지 않게 하거나, 컨트롤러를 실행하기 전 컨트롤러에서 필요로 하는 정보를 생성할 수 있다.

`postHandle()` 메서드는 컨트롤러 실행 후에 추가 기능을 구현할 때 사용한다. 컨트롤러가 익셉션을 발생하면 이 메서드는 실행하지 않는다.

`afterCompletion()` 메서드는 뷰를 실행 후의 기능을 구현할 때 사용한다. 컨트롤러 실행 이후에 예기치 않게 발생한 익셉셔을 로그로 남긴다거나 실행 시간을 기록하는 등의 후처리를 하기에 적합한 메서드이다.

따라서 비밀번호 변경 기능에 접근 시 `HandlerInterceptor`를 사용하면 로그인 여부에 따라 로그인 폼으로 보내거나 컨트롤러를 실행하도록 구현가능하다. `HttpSession`에 `authInfo` 속성이 존재하지 않으면 지정한 경로로 리다이렉트하도록 구현하였다.

```java
public class AuthCheckInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler) throws Exception {
		HttpSession session = request.getSession(false);
		if (session != null) {
			Object authInfo = session.getAttribute("authInfo");
			if (authInfo != null) {
				return true;
			}
		}
		response.sendRedirect(request.getContextPath() + "/login");
		return false;
	}

}
```

`HttpSession`에 `authInfo`속성이 존재하면 true를 리턴한다. 존재하지 않으면 리다이렉트 응답을 실행한 뒤, false를 리턴한다. `request.getContextPath()`는 현재 컨텍스트 경로를 리턴한다. 예를 들어 경로가 `http://localhost:8080/chap13`이면 컨텍스트 경로는 `/chap13`이 된다. 

## HandlerInterceptor

`HandlerInterceptor`를 어디에 적용할지 설정해야 한다. `MvcConfig` 설정 클래스에 추가하도록 하자.

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(authCheckInterceptor())
			.addPathPatterns("/edit/**")
			.excludePathPatterns("/edit/help/**");
	}

	@Bean
	public AuthCheckInterceptor authCheckInterceptor() {
		return new AuthCheckInterceptor();
	}

}
```
`addInterceptors()`메서드는 `InterceptorRegistrantion` 객체를 리턴하는데 `addPathPatterns()` 메서드는 인터셉터를 적용할 경로 패턴을 지정한다. 이 경로는 Ant 경로 패턴을 사용한다. 지정한 경로 패턴 중 일부를 제외하고 싶다면 `excludePathPatterns()` 메서드를 사용하면 된다.

> Ant 경로 패턴은 *, **, ?의 세 가지 특수 문자를 이용해서 경로를 표현한다.
\* : 0개 또는 그 이상의 글자
? : 1개 글자
** : 0개 또는 그 이상의 폴더 경로

## 컨트롤러에서 쿠키 사용하기

사용자의 편의를 위해 아이디를 기억해놨다가 다음에 로그인할 때 아이디를 자동으로 넣어줄 수 있는 기능을 구현할 때 쿠키를 사용한다. 

스프링 MVC에서 쿠키를 사용하는 방법은 `@CookieValue` 에노테이션을 사용하는 것이다. 

```java
@GetMapping
public String form(LoginCommand loginCommand,
        @CookieValue(value = "REMEMBER", required = false) Cookie rCookie) {
    if (rCookie != null) {
        loginCommand.setEmail(rCookie.getValue());
        loginCommand.setRememberEmail(true);
    }
    return "login/loginForm";
}
```

`@CookieValue` 에노테이션의 value 속성은 쿠키의 이름을 지정한다. 이름이 REMEBER인 쿠키를 Cookie 타입으로 전달받는다. 지정한 이름을 쿠키가 존재하지 않을수도 있다면 `required` 속성값을 false로 지정한다. 

`required` 속성의 기본 값은 true이다. `required`가 true인 상태에서 지정한 이름을 가진 쿠키가 존재하지 않으면 스프링 MVC는 익셉션을 발생시킨다.

REMEMEBER 쿠키가 존재하면 쿠키의 값을 읽어와 커맨드 객체의 email 프로퍼티 값을 설정한다. 

실제 REMEMBER 쿠키를 생성하는 부분은 로그인을 처리하는 `submit()` 메서드이다. 쿠키를 생성하려면 `HttpServletResponse` 객체를 추가해야 한다.

```java
@PostMapping
public String submit(
        LoginCommand loginCommand, Errors errors, HttpSession session,
        HttpServletResponse response) {
    new LoginCommandValidator().validate(loginCommand, errors);
    if (errors.hasErrors()) {
        return "login/loginForm";
    }
    try {
        AuthInfo authInfo = authService.authenticate(
                loginCommand.getEmail(),
                loginCommand.getPassword());
        
        session.setAttribute("authInfo", authInfo);

        Cookie rememberCookie = 
                new Cookie("REMEMBER", loginCommand.getEmail());
        rememberCookie.setPath("/");
        if (loginCommand.isRememberEmail()) {
            rememberCookie.setMaxAge(60 * 60 * 24 * 30);
        } else {
            rememberCookie.setMaxAge(0);
        }
        response.addCookie(rememberCookie);

        return "login/loginSuccess";
    } catch (WrongIdPasswordException e) {
        errors.reject("idPasswordNotMatching");
        return "login/loginForm";
    }
}
```

로그인에 성공하면 이메일을 선택했는지 여부에 따라 30일동안 유지되는 쿠키를 생성하거나 바로 삭제되는 쿠키를 생성한다. 

쿠키의 유효시간은 `setMaxAge()`를 이용하여 설정할 수 있는데 매개변수에 '60'을 넣어주게 된다면 쿠키는 60초, 즉 1분동안의 유효시간이 되는 것이다. 따라서 위 코드의 `60 * 60 * 24 * 30`은 30일인 것이다.
