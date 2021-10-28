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

## DispatcherServlet과 스프링 컨테이너



## @Controller를 위한 HandlerMapping과 HandlerAdapter






