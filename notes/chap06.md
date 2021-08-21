# chap 06 빈 라이프사이클과 범위

## 컨테이너 초기화와 종료

```java
// 1. 컨테이너 초기화
AnnotationConfigApplicationContext ctx =
	new AnnotationConfigApplicationContext(AppContext.class);

// 2. 컨테이너에서 빈 객체를 구해서 사용
Greeter g= ctx.getBean("greeter", Greeter.class);
String msg = g.greet("스프링");
System.out.println(msg);

// 3. 컨테이너 종료
ctx.close();
```

- 컨테이너 초기화 → 빈 객체의 생성, 의존 주입, 초기화
- 컨테이너 종료 → 빈 객체의 소멸

## Bean 객체의 라이프 사이클

```java
객체 생성 -> 의존 설정 -> 초기화 -> 소멸
```

스프링 컨테이너는 bean 객체의 라이프 사이클을 관리한다. 

스프링 컨테이너 초기화 단계에서 컨테이너는 bean 객체를 생성하고 의존 설정을 진행한다.

모든 의존 주입이 완료되면 bean 객체의 초기화를 수행한다. Bean 객체를 초기화하는 단계에서 별도의 동작을 수행시키고 싶을 경우, `InitializingBean` 인터페이스를 구현한다.

스프링 컨테이너가 종료될 때 컨테이너는 Bean 객체의 소멸을 관리한다. 이 때도 별도의 동작을 수행시키고 싶을 경우 `DisposableBean` 인터페이스를 구현한다.

### Bean 객체의 초기화와 소멸 메서드

```java
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}

public interface DisposableBean {
    void destroy() throws Exception;
}
```