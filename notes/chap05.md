# chap 05 컴포넌트 스캔
## @Component

Class를 Bean으로 등록하기 위한 에노테이션이다.

```java
@Component
public class MemberDao {
	...
}

@Component("infoPrinter")
public class MemberInfoPrinter {
	...
}
```

`@Component`의 이름을 별도로 지정해 주지 않아도 자동으로 Student의 이름을 갖는다. 

## @ComponentScan

`@Component`와 `@Service`, `@Repository`, `@Controller`, `@Configuration`, `@Aspect`이 붙은 클래스 Bean들을 찾아서 Context에 bean등록을 해주는 에노테이션이다.

```java
@Configuration
@ComponentScan(basePackages = {"spring"})
public class AppCtx {
    ...
}
```

`@ComponentScan(basePackages = {"spring"})` 속성은 스캔 대상 패키지 목록을 지정한다. `{"spring"}` 은 spring 패키지와 하위 패키지에 속한 클래스를 스캔 대상으로 설정한다. 

### @ComponentScan - excludeFilters

`excludeFilters` 속성을 사용하면 스캔할 때 특정 대상을 자동 등록 대상에서 제외할 수 있다.

```java
@Configuration
@ComponentScan(basePackages = {"spring"},
    excludeFilters = @Filter(type = FilterType.REGEX, pattern = "spring\\..*Dao"))
public class AppCtxWithExclude {
    ...
}
```

`@Filter` 에노테이션의 type 속성 값으로 `FilterType.REGEX` 를 주었다. 이는 정규표현식을 사용해서 제외 대상을 지정한다는 것을 의미한다. 

pattern 속성은 FilterType에 적용할 값을 설정한다. 위 코드는 "spring."으로 시작하고 Dao로 끝나는 정규표현식을 지정했으므로 spring.MemberDao 클래스를 컴포넌트 스캔 대상에서 제외시킨다.

```java
@Configuration
@ComponentScan(basePackages = {"spring"},
    excludeFilters = @Filter(type = FilterType.ASPECTJ, pattern = "spring.*Dao"))
public class AppCtx {
    ...
}
```

위 코드처럼 `FilterType.ASPECTJ`를 필터 타입으로 설정할 수도 있다. 이 타입을 사용하면 정규표현식 대신에 AspectJ 패턴을 사용해서 대상을 지정한다. 

AspectJ 패턴이 동작하려면 의존 대상에 aspectjweaver 모듈을 추가해야 한다.

```xml
<dependency>
	<groupId>org.aspectj</groupId>
	<artifactId>aspectjweaver</artifactId>
	<version>1.8.13</version>
</dependency>
```

```java
@Retention(RUNTIME)
@Target(TYPE)
public @@interface NoProduct {}

@Retention(RUNTIME)
@Target(TYPE)
public @@interface ManualBean {}
```

특정 에노테이션을 붙인 타입을 컴포넌트 대상에서 제외할 수도 있다. 예를 들어 위 코드의 @NoProduct나 @ManualBean 에노테이션을 붙인 클래스를 제외하고 싶다고 하자.

```java
@Configuration
@ComponentScan(basePackages = {"spring", "spring2"},
		excludeFilters = @Filter(type = FilterType.ANNOTATION, 
														 classes= { NoProduct.class, ManualBean.class} ))
public class AppCtxWithExclude {
	@Bean
	public MemberDao memerDao() {
		return new MemberDao();
	}
}
```

type 속성 값으로 `FilterType.ANNOTATION` 을 사용하면 classes 속성에 필터로 사용할 에노테이션 타입을 값으로 준다. 

```java
@Configuration
@ComponentScan(basePackages = {"spring"},
		excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
														 classes = MemberDao.class ))
public class AppCtxWithExclude{
```

특정 타입이나 그 하위 타입을 컴포넌트 스캔 대상에서 제외하려면 `ASSIGNABLE_TYPE` 을 FilterType으로 사용한다.

### excludeFilters 여러개일 경우

`@ComponentScan`의 `excludeFilters` 속성에 배열을 사용해서 `@Filter` 목록을 전달하면 된다.

```java
@Configuration
@ComponentScan(basePackages = {"spring"},
		excludeFilters = {
			@Filter(type = FilterType.ANNOTATION, classes = ManualBean.class ),
			@Filter(type = FilterType.REGEX, pattern = "spring2\\..*")
		})
```

## 컴포넌트 스캔에 따른 충돌 처리

### Bean 이름 충돌

```java
@Configuration
@ComponentScan(basePackages = {"spring", "spring2"})
public class AppCtx {
    ...
}
```

위와 같이 서로 다른 패키지에 같은 이름을 가진 Bean 클래스가 2개 존재하고, 2개의 클래스 모두 `@Component` 에노테이션을 갖고 있을 경우 Exception이 발생한다. 이 때문에 @Component 어노테이션에 이름을 지정하는 등의 방법으로 충돌을 피해야 한다.

### 수동 등록한 Bean과 충돌

```java
@Component
public class MemberDao {
    ...
}
```

```java
@Configuration
@ComponentScan(basePackages = {"spring"})
public class AppCtx {
    
    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }
}
```

스캔할 때 사용하는 Bean 이름과 수동 등록 Bean 이름이 같은 경우에도 충돌이 발생한다. 이 때는 Exception이 발생하지 않고 수동으로 등록된 Bean이 우선권을 가진다.

> Bean 주입에서는 @Autowired가 수동보다 우선권을 가졌지만, Bean 등록에서는 반대의 양상을 보인다.