# chap 10 스프링 MVC 프레임워크 동작 방식

## 스프링 MVC 핵심 구성 요소

![mvc](https://blog.kakaocdn.net/dn/wkWpx/btqygTHO37x/xKmnXPI6Gk3HQiveuyc9K0/img.png)

1. 그림 중앙에 위치한 `DispatcherServlet`은 모든 연결을 담당한다. 웹 브라우저로부터 요청이 들어오면 `DispatcherServlet`은 그 요청을 처리하기 위한 컨트롤러 객체를 검색한다.

2. `HandlerMapping`이라는 Bean 객체에게 컨트롤러 검색을 요청한다. `HandlerMapping`은 클라이언트의 요청 경로를 이용해서 이를 처리한 컨트롤러 Bean 객체를 `DispatcherServlet`에 전달한다. `@Controller`, `Controller` 인터페이스, `HttpRequestHandler` 인터페이스를 동일한 방식으로 처리하기 위해 중간에 사용되는 것이 `HandlerAdapter` 빈이다.

3. `HandlerMapping`이 찾아준 컨트롤러 객체를 처리할 수 있는 `HandelerAdapter` 빈에게 요청 처리를 위임한다.

4. 컨트롤러의 알맞은 메서드를 호출해서

5. 결과를 리턴하고

6. 그 결과를 `ModelAndView`로 변환하여 `DispatcherServlet`에 리턴한다. 

7. 결과를 보여줄 뷰를 찾기 위해 `ViewResolver` 빈 객체를 사용한다. `ModelAndView`는 컨트롤러가 리턴한 뷰 이름을 담고 있는데 `ViewResolver`는 이 뷰 이름에 해당하는 `View` 객체를 찾거나 생성하여 리턴한다.

8. `ViewResolver`가 리턴한 `View`객체에게 응답 결과 생성을 요청한다.

## WebMvcConfigurer 인터페이스

`DispatcherServlet`는 웹 브라우저의 요청을 처리할 핸들러 객체를 찾기 위해 `HandlerMapping`을 사용하고 핸들러를 실행하기 위해 `HandlerAdapter`를 사용한다. 하지만 `@EnableWebMvc`애노테이션만 추가해줘도 다양한 스프링 Bean 설정을 추가해줄 수 있다.

```java
@Configuration
@EnableWebMvc
public class MvcConfig {
    ...
}
```

`@EnableWebMvc` 애노테이션을 사용하면 `@Controller` 애노테이션을 붙인 컨트롤러를 위한 설정을 생성한다. 그리고 `WebMvcConfigurer` 타입의 Bean을 이용하여 MVC 설정을 추가로 생성할 수 있다. 

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/view/", ".jsp");
	}

}
```

`@Configuration` 애노테이션을 붙인 클래스는 컨테이너에 Ban으로 등록된다. `@EnableWebMvc` 애노테이션을 사용하면 `WebMvcConfigurer` 타입인 Bean 객체의 메서드를 호출해서 MVC 설정을 추가한다. 

## JSP를 위한 ViewResolver

컨트롤러 처리 결과를 JSP를 이용해 생성하기 위해 다음 코드를 작성한다.

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/view/", ".jsp");
	}

}
```

`DispatcherServlet`은 컨트롤러의 실행 결과를 `HandlerAdapter`를 통해서 `ModelAndView`형태로 받는다고 했다. Model에 담긴 값은 View 객체에 Map 형식으로 전달된다. 

```java
@Controller
public class HelloController {

	@GetMapping("/hello")
	public String hello(Model model, @RequestParam(value="name", required=false) String name) {
		model.addAttribute("greeting", "안녕하세요, " + name);
		return "hello";
	}
	
}
```
`greeting`키를 갖는 Map 객체를 View 객체에 전달한다. View 객체는 전달받은 Map 객체에 담긴 값을 이용해서 알맞은 응답 결과를 출력한다. `InternalResourceView`는 Map 객체에 담겨 있는 키 값을 `request.setAttribute()`를 이용해서 request의 속성에 저장한다. 그 다음 해당 경로의 JSP를 실행한다. 

JSP는 다음과 같이 모델에 지정한 속성 이름을 사용해서 값을 사용할 수 있다.

```html
<%-- JSP 코드에서 모델의 속성 이름을 사용해서 값 접근 -->
인사말:${greeting}
```

`@EnableWebMvc` 애노테이션을 사용하지 않아도 스프링 MVC를 사용할 수 있다. 다만 `EnableWebMvc` 애노테이션과 `WebMvcConfigurer` 인터페이스를 사용할 때보다 설정해야 할 Bean이 많아진다. 다음 코드와 같다.

```java
@Configuration
public class MvcConfig {

	@Bean
	public HandlerMapping handlerMapping() {
		RequestMappingHandlerMapping hm = new RequestMappingHandlerMapping();
		hm.setOrder(0);
		return hm;
	}

	@Bean
	public HandlerAdapter handlerAdapter() {
		RequestMappingHandlerAdapter ha = new RequestMappingHandlerAdapter();
		return ha;
	}

	@Bean
	public HandlerMapping simpleHandlerMapping() {
		SimpleUrlHandlerMapping hm = new SimpleUrlHandlerMapping();
		Map<String, Object> pathMap = new HashMap<>();
		pathMap.put("/**", defaultServletHandler());
		hm.setUrlMap(pathMap);
		return hm;
	}

	@Bean
	public HttpRequestHandler defaultServletHandler() {
		DefaultServletHttpRequestHandler handler = new DefaultServletHttpRequestHandler();
		return handler;
	}

	@Bean
	public HandlerAdapter requestHandlerAdapter() {
		HttpRequestHandlerAdapter ha = new HttpRequestHandlerAdapter();
		return ha;
	}

	@Bean
	public ViewResolver viewResolver() {
		InternalResourceViewResolver vr = new InternalResourceViewResolver();
		vr.setPrefix("/WEB-INF/view/");
		vr.setSuffix(".jsp");
		return vr;
	}

}
```