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
Bean 객체가 `InitializingBean` 인터페이스를 구현하면 컨테이너가 초기화 과정에서 `afterPropertiesSet()` 메서드를 자동으로 실행하면서 Bean 객체를 초기화한다.

Bean 객체가 `DisposableBean` 인터페이스를 구현하면 컨테이너가 소멸 과정에서 `destroy()` 메서드를 자동으로 실행하면서 Bean 객체를 소멸시킨다.

대표적인 예시로 데이터베이스 커넥션 풀, 채팅 클라이언트 등등이 있다.

```java
public class Client implements InitializingBean, DisposableBean {
    
    private String host;

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Client.afterPropertiesSet() 실행");
    }

    public void send() {
        System.out.println("Client.send() to " + host);
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("Client.destroy() 실행");
    }
}
```

```java
@Configuration
public class AppCtx {

    @Bean
    public Client client() {
        Client client = new Client();
        client.setHost("host");
        return client;
    }
}
```

```java
public class Main {
    public static void main(String[] args) throws IOException {
        AbstractApplicationContext ctx = 
            new AnnotationConfigApplicationContext(AppCtx.class);

            Client client = ctx.getBean(Client.class);
            client.send();

            ctx.close();
    }
}
```

```java
AbstractApplicationContext prepareRefresh...
정보: Refreshing o....AnnotationConfigApplicationContext@5cb0d902: startup...
Client.afterPropertiesSet() 실행
Client.send() to host
AbstractApplicationContext doClose
정보: Closing o....AnnotationConfigApplicationContext@5cb0d902: startup...
Client.destroy() 실행
```

컨테이너는 Bean 객체의 생성(`Refreshing`)을 마무리한 후 `afterPropertiesSet()` 메서드를 수행시키고, 소멸(`Closing`) 가장 마지막에 `destroy()` 메서드를 수행시켰다.

특히 `destroy()`의 경우 `ctx.close()` 가 수행되지 않았다면 Bean 객체의 소멸 과정도 수행되지 않았을 것임을 유추할 수 있다.

### **Bean 객체의 커스텀 초기화/소멸 메서드**

모든 클래스가 `InitializingBean.afterPropertiesSet()`와 `DisposableBean.destroy()`를 구현할 수 있는 것은 아니다. 외부에서 제공 받는 클래스를 스프링 Bean 객체로 설정하는 경우 두 인터페이스를 구현할 수 없다.

이럴 경우 `@Bean` 어노테이션에 `initMethod`/`destroyMethod` 속성을 지정하는 방법으로 커스텀 초기화/소멸 메서드를 설정해줄 수 있다.

```java
public class CustomClient {
    
    private String host;

    public void setHost(String host) {
        this.host = host;
    }

    public void connect() {
        System.out.println("CustomClient.connect() 실행");
    }

    public void send() {
        System.out.println("CustomClient.send() to " + host);
    }

    public void close() {
        System.out.println("CustomClient.close() 실행");
    }
}
```

```java
@Configuration
public class AppCtx {

    @Bean(initMethod = "connect", destroyMethod = "close")
    public CustomClient customClient() {
        CustomClient customClient = new CustomClient();
        customClient.setHost("host");
        return customClient;
    }
}
```

```java
AbstractApplicationContext prepareRefresh...
정보: Refreshing o....AnnotationConfigApplicationContext@5cb0d902: startup...
CustomClient.connect() 실행
CustomClient.send() to host
AbstractApplicationContext doClose
정보: Closing o....AnnotationConfigApplicationContext@5cb0d902: startup...
CustomClient.close() 실행
```

또한 초기화 메서드를 `@Configuration`의 Bean 등록 메서드에서 직접 호출할 수도 있다.

```java
@Configuration
public class AppCtx {

    @Bean(destroyMethod = "close")
    public CustomClient customClient() {
        CustomClient customClient = new CustomClient();
        customClient.setHost("host");
        customClient.connect();
        return customClient;
    }
}
```

주의할 점은 이미 `InitializingBean`를 구현한 Bean 객체를 초기화하는 과정에선 `afterPropertiesSet()`를 호출하지 않도록 해야한다는 것이다.

```java
@Configuration
public class AppCtx {

    @Bean
    public Client client() {    // 이미 InitializingBean 구현되어 있음
        Client client = new Client();
        client.setHost("host");
        client.afterPropertiesSet();
        return client;
    }
}
```

위 코드와 같은 상황에선 `afterPropertiesSet()` 메서드가 총 2회 호출되게 된다.