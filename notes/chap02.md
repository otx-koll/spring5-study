# chap 02 스프링 시작하기

## 1. 스프링 프로젝트 시작

```java
// AppContext.class
@Configuration
public class AppContext {
	@Bean
	public Greeter greeter() {
		Greeter g = new Greeter();
		g.setFormat("%s, 안녕하세요!");
		return g;
	}
}
```

- `@Configuration` : 해당 클래스를 스프링 설정 클래스로 지정
- `@Bean` : 스프링이 관리하는 빈 객체로 등록

```java
// Main.class
AnnotationConfigApplicationContext ctx = 
	new AnnotationConfigApplicationContext(AppContext.class);
Greeter g = ctx.getBean("greeter", Greeter.class);
String msg = g.greet("스프링");
System.out.println(msg);
ctx.close();
```

- `AnnotationConfigApplicationContext` : 자바 설정에서 정보를 읽어와 빈 객체를 생성하고 관리
    - AppContext.class 를 생성자 파라미터로 전달
    - @Bean 설정 정보 읽어와 객체를 생성하고 초기화
    - getBean("@Bean으로 설정한 메소드 명", 반환 할 클래스명)을 통해 bean객체 검색

## 2. 스프링은 객체 컨테이너

- 스프링의 핵심적인 기능은 객체를 생성하고 초기화 하는 것이다
- `AnnotationConfigApplicationContext` : ApplicationContext 인터페이스를 구현한 클래스
- `ApplicationContext` : 빈 객체의 생성, 초기화, 보관, 제거 등을 관리하여 컨테이너(Container)라고도 부른다

### 싱글톤(Singleton) 객체

- 별도로 설정을 하지 않을 경우, 스프링은 한 개의 bean 객체만을 생성한다
- 이 때 빈 객체는 '싱글톤(singleton) 범위를 갖는다'라고 표현한다