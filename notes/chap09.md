# chap 09 스프링 MVC 시작하기

## pom.xml

```xml
<dependencies>
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <version>3.1.0</version>
        <scope>provided</scope>
    </dependency>

    <dependency>
        <groupId>javax.servlet.jsp</groupId>
        <artifactId>javax.servlet.jsp-api</artifactId>
        <version>2.3.3</version>
        <scope>provided</scope>
    </dependency>

    <dependency>
        <groupId>javax.servlet.jsp.jstl</groupId>
        <artifactId>jstl-api</artifactId>
        <version>1.2</version>
        <type>jar</type>
    </dependency>

    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>5.3.10</version>
    </dependency>
</dependencies>
```

스프링을 이용해서 웹 어플리케이션 개발하는데 필요한 의존을 설정하였다.

## 이클립스 톰캣 설정

이클립스에서 웹 프로젝트를 테스트하려면 톰캣이나 제티와 같은 웹 서버를 설정해야 한다. 서블릿3.1과 JSP 2.3 버전을 기준으로 톰캣 8/8.5/9 버전을 사용하면 된다.

톰캣 사이트에서 zip이나 tar.gz으로 압축된 톰캣을 다운로드 한 뒤, 다음 절차에 따라 이클립스에 서버를 등록한다.

- `[Window]` -> `[Preferences]` 메뉴 실행
- `Server/Runtime Environments` 선택
- `[Add]` 버튼을 눌러 톰캣 서버 등록

## 스프링 MVC를 위한 설정

스프링 MVC를 실행하는데 필요한 최소 설정이 필요하다. 그 설정은 다음과 같다.

- 스프링 MVC의 주요 설정(HandlerMapping, ViewResolver 등)
- 스프링의 DispatcherServlet 설정

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
스프링 MVC를 사용하려면 다양한 구성 요소를 설정해야 하는데 처음부터 끝까지 이 요소를 직접 구성하게 되면 설정이 매우 복잡해진다. 이런 복잡한 설정을 대신 해주는 것이 `@EnableWebMvc` 애노테이션이다. 

`@EnableWebMvc` 애노테이션은 스프링 MVC 설정을 활성화한다. 그리고 내부적으로 다양한 Bean 설정을 추가해준다. 

`WebMvcConfigurer` 인터페이스는 스프링 MVC의 개별 설정을 조정할 떄 사용한다. `configureDefaultServletHandling()` 메서드와 `configureViewResolvers()`메서드는 `WebMvcConfigurer` 인터페이스에 정의된 메서드로 각각 디폴트 서블릿과 `ViewResolver`와 관련된 설정을 조정한다. 

## web.xml 파일에 DispatcherServlet 설정

스프링 MVC가 웹 요청을 처리하려면 `DispatcherServlet`을 통해서 웹 요청을 받아야 한다. `web.xml` 파일에 DispatcherServlet을 등록하면 된다. `web.xml`은 다음과 같다.

```xml
<?xml version="1.0" encoding="UTF-8"?>

<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
             http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
	version="3.1">

	<!-- DispatcherServelt 등록 -->
	<servlet>
		<servlet-name>dispatcher</servlet-name>
		<servlet-class>
			org.springframework.web.servlet.DispatcherServlet
		</servlet-class>
		<!-- contextClass 초기화 파라미터 설정 -->
		<init-param>
			<param-name>contextClass</param-name>
			<param-value>
				org.springframework.web.context.support.AnnotationConfigWebApplicationContext
			</param-value>
		</init-param>
		<!-- contextConfiguration 초기화 파라미터의 값 지정 -->
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>
				config.MvcConfig
				config.ControllerConfig
			</param-value>
		</init-param>
		<!-- 톰캣과 같은 컨테이너가 웹 어플리케이션 구동 시 이 서블릿을 함께 실행하도록 설정 -->
		<load-on-startup>1</load-on-startup>
	</servlet>

	<!-- 모든 요청을 DispatcherServlet이 처리하도록 서블릿 매핑 설정 -->
	<servlet-mapping>
		<servlet-name>dispatcher</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>

	<!-- HTTP 요청 파라미터의 인코딩 처리를 위한 서블릿 필터 등록 -->
	<filter>
		<filter-name>encodingFilter</filter-name>
		<filter-class>
			org.springframework.web.filter.CharacterEncodingFilter
		</filter-class>
		<init-param>
			<param-name>encoding</param-name>
			<param-value>UTF-8</param-value>
		</init-param>
	</filter>
	<filter-mapping>
		<filter-name>encodingFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

</web-app>
```

## 코드 구현

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

- `@Controller` 애노테이션을 적용한 클래스는 스프링 MVC에서 컨트롤러로 사용한다.

- `@GetMapping` 애노테이션은 메서드가 처리할 요청 경로를 지정한다. 위 코드의 경우 `/hello` 경로로 들어온 요청을 `hello()`메서드를 이용해서 처리한다고 설정했다. 이름에서 알 수 있듯이 HTTP 요청 메서드 중 GET 메서드에 대한 매핑을 설정한다.

- Model 파라미터는 컨트롤러의 처리 결과를 뷰에 전달할 때 사용한다.

- `@RequestParam` 애노테이션은 HTTP 요청 파라미터의 값을 메서드의 파라미터로 전달할 때 사용된다. 위 코드의 경우 name 요청 파라미터의 값을 name 파라미터에 전달한다. 

- `greeting`이라는 모델 속성에 값을 설정한다.

Controller란 웹 요청을 처리하고 그 결과를 뷰에 전달하는 스프링 Bean 객체이다. 스프링 컨트롤러로 사용될 클래스는 `@Controller` 애노테이션을 붙여야 하고, `@GetMapping` 애노테이션이나 `@PostMapping` 애노테이션과 같은 매핑 애노테잇녀을 이용하여 처리할 경로를 지정해줘야 한다.

```java
@Configuration
public class ControllerConfig {
	@Bean
	public HelloController helloController() {
		return new HelloController();
	}
}
```
`HelloController`를 스프링 Bean으로 등록하였다. 그리고 컨트롤러가 생성한 결과를 보여줄 뷰 코드를 구현한다. `src/main/webapp/WEB-INF`폴더에 view폴더를 만들어 view폴더에 `hello.jsp`파일을 추가한다.

```jsp
<%@ page contentType="text/html; charset=utf-8" %>
<!DOCTYPE html>
<html>
<head>
	<title>Hello</title>
</head>
<body>
	인사말:${greeting}
</body>
</html>
```

`${greeting}`표현식은 컨트롤러 구현에서 Model에 추가한 속성의 이름인 `greeting`과 동일하다. 컨트롤러에서 설정한 속성을 뷰 JSP 코드에서 접글할 수 있는 이유는 스프링 MVC 프레임워크가 모델에 추가한 소성을 JSP 코드에서 접근할 수 있게 `HttpServletRequest`에 옮겨주기 때문이다.