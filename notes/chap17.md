# 프로필과 프로퍼티 파일

## 프로필

처음부터 개발 목적 설정과 실 서비스 목적의 설정을 구분해서 작성하는 것이 `프로필`이다. 프로필은 논리적인 이름으로 설정 집합에 프로필을 지정할 수 있다. 스프링 컨테이너는 설정 집합 중에서 지정한 이름을 사용하는 프로필을 선택하고 해당 프로필에 속한 설정을 이용해 컨테이너를 초기화할 수 있다. 

예를 들어 로컬 개발 환경을 위한 `DataSource` 설정을 `dev`프로필로 지정하고 실 서비스 환경을 위한 `DataSource` 설정을 `real` 프로필로 지정한 뒤, `dev`프로필을 사용하여 스프링 컨테이너를 초기화할 수 있다. 

### @Configuration 설정에서 프로필 사용

프로필을 지정하려면 `@Profile` 에노테이션을 이용하면 된다.

```java
@Configuration
@Profile("dev")
public class DsDevConfig {

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		DataSource ds = new DataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		... // 생략
	}
}
```

스프링 컨테이너를 초기화할 때 `dev` 프로필을 활성화하면 `DsDevConfig` 클래스를 설정으로 사용한다.

`dev`가 아닌 `real` 프로필을 활성했을 때는 `@Profile` 에노테이션의 값으로 `real`을 지정한다.

```java
@Configuration
@Profile("dev")
public class DsRealConfig {

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		DataSource ds = new DataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
        ... // 생략
	}
}
```

`DsDevConfig`클래스와 `DsRealConfig` 클래스는 둘 다 이름이 `dataSource`인 `DataSource` 타입의 빈을 설정한다. 어떤 빈을 사용할지는 활성화 여부에 따라 달라진다. 

특정 프로필을 선택하려면 컨테이너를 초기화하기 전에 `setActiveProfiles()` 메서드를 사용해서 프로필을 선택한다.

```java
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.getEnvironment().setActiveProfiles("dev");
		context.register(MemberConfig.class, DsDevConfig.class, DsRealConfig.class);
		context.refresh();
```

`getEnvironment()` 메서드는 스프링 실행 환경을 설정하는데 사용되는 `Environment`를 리턴한다. `Environment`는 `setActiveProfiles()` 메서드를 사용해서 사용할 프로필을 선택할 수 있다. 

프로필을 사용할 때는 설정 정보를 전달하기 전에 어떤 프로필을 사용할지 먼저 지정해줘야 한다. `register()` 메서드로 설정 파일 목록을 지정해주고, `refresh()` 메서드를 실행하여 컨테이너를 초기화한다. 이 순서를 반드시 지켜줘야 익셉션이 발생하지 않는다. 

만약 두 개 이상의 프로필을 활성화하고 싶다면 아래와 같이 각 프로필 이름을 메서드에 파라미터로 전달한다.

```java
context.getEnvironment().setActiveProfiles("dev", "mysql");
```

프로필을 선택하는 또 다른 방법은 `spring.profiles.active` 시스템 프로퍼티에 사용할 프로필 값을 지정하는 것이다.명령행에서 -D 옵션을 이용하거나 `System.setPropert()`를 이용해서 지정할 수 있다. 

```java
java -Dspring.profiles.active=dev main.Main
```

위와 같이 설정하면 `setActiveProfiles()` 메서드를 이용하지 않아도 활성화할 수 있다. 

```java
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
    MemberConfig.class, DsDevConfig.class, DsRealConfig.class);
```

### @Configuration을 이용한 프로필 설정

중첩 클래스를 이용해서 프로필 설정을 한 곳으로 모을 수 있다.

```java
@Configuration
public class MemberConfigWithProfile {
	@Autowired
	private DataSource dataSource;

	@Bean
	public MemberDao memberDao() {
		return new MemberDao(dataSource);
	}

	@Configuration
	@Profile("dev")
	public static class DsDevConfig {

		@Bean(destroyMethod = "close")
		public DataSource dataSource() {
			DataSource ds = new DataSource();
			ds.setDriverClassName("com.mysql.jdbc.Driver");
            ds.setUrl("jdbc:mysql://localhost/spring5fs?characterEncoding=utf8");
            ... // 생략
		}
	}

	@Configuration
	@Profile("real")
	public static class DsRealConfig {

		@Bean(destroyMethod = "close")
		public DataSource dataSource() {
			DataSource ds = new DataSource();
			ds.setDriverClassName("com.mysql.jdbc.Driver");
            ds.setUrl("jdbc:mysql://realdb/spring5fs?characterEncoding=utf8");
            ... // 생략
		}
	}

}
```

중첩된 `@Configuration` 설정을 사용할 때 중첩 클래스는 반드시 `static`이어야 한다.

### 다수 프로필 설정

스프링 설정은 두 개 이상의 프로필 이름을 가질 수 있다.

```java
@Profile("dev,test")
```

프로필 값을 지정할 때 다음 코드처럼 느낌표(!)를 사용할 수도 있다.

```java
@Profile("!real")
```

느낌표를 붙인 프로필이 활성화되지 않았을 때 사용한다는 것을 의미한다. 보통 `!프로필` 형식은 특정 프로필이 사용되지 않을 때 기본으로 사용할 설정을 지정하는 용도이다.

### 어플리케이션에서 프로필 설정

`web.xml`에서 아래와 같이 `spring.prifiles.active` 초기화 파라미터를 이용해서 프로필을 선택할 수 있다.

```xml
<servlet>
    ...
    <init-param>
        <param-name>spring.profiles.active</param-name>
        <param-value>dev</param-value>
    </init-param>
    ...
</servlet>
```

## 프로퍼티 파일을 이용한 프로퍼티 설정

스프링은 외부의 프로퍼티 파일을 이용해 스프링 빈을 설정하는 방법을 제공한다. 예를 들어 아래와 같은 파일이 있다고 한다.

```
dv.driver=com.mysql.jdbc.Driver
db.url=jdbc:mysql://localhost/spring5fs?charactorEncoding=utf8
db.user=spring5
db.password=spring5
```

위 프로퍼티 값을 자바 설정에서 사용할 수 있고, 설정 일부를 외부 프로퍼티 파일을 사용해서 변경할 수 있다.

### @Configuration 에노테이션 이용 자바 설정에서의 프로퍼티 사용

자바 설정에서 프로퍼티 파일을 사용하려면 두 가지를 설정해야 한다.

- `PropertySourcesPlaceholderConfigurer` 빈 설정
- `@Value` 에노테이션으로 프로퍼티 값 사용

먼저 `PropertySourcesPlaceholderConfigurer` 클래스를 빈으로 등록한다.

```java
@Configuration
public class PropertyConfig {

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		configurer.setLocations(
				new ClassPathResource("db.properties"),
				new ClassPathResource("info.properties"));
		return configurer;
	}

}
```

`setLocations()` 프로퍼티 파일 목록을 인자로 전달받는다. `db.properties` 파일이 클래스 패스에 위치하고 있다면 `ClassPathResource` 클래스를 이용하여 프로퍼티 파일 정보를 전달한다.

`PropertySourcesPlaceholderConfigurer` 타입 빈을 설정하는 메서드가 `static` 메서드인데 그 이유는 특수한 목적의 빈이기 때문에 정적 메서드로 지정하지 않으면 원하는 방식으로 동작하지 않는다.

`PropertySourcesPlaceholderConfigurer` 타입은 `setLocations()` 메서드로 전달받은 프로퍼티 파일 목록 정보를 읽어와 필요할 때 사용한다. 이를 위한 것이 `@Value` 에노테이션이다.

```java
@Configuration
public class DsConfigWithProp {
    @Value("${db.driver}")
    private String driver;
    @Value("${db.url}")
    private String jdbcUrl;
    @Value("${db.user}")
    private String user;
    @Value("${db.password}")
    private String password;

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		DataSource ds = new DataSource();
		ds.setDriverClassName(driver);
		ds.setUrl(jdbcUrl);
		ds.setUsername(user);
		ds.setPassword(password);
        ... // 생략
	}
}
```

`PropertySourcesPlaceholderConfigurer`는 `${구분자}` 형식의 플레이스홀더의 값과 일치하는 프로퍼티 값으로 치환해준다.

### 빈 클래스에서 사용하기

빈으로 사용할 클래스에도 `@Value` 에노테이션을 붙일 수 있다.

```java
public class Info {
    @Value("${info.version}")
	private String version;

	public void printInfo() {
		System.out.println("version = " + version);
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
```

`@Value` 에노테이션을 붙이면 플레이스홀더에 해당하는 프로퍼티를 필드에 할당한다. set 메서드에 적용할 수도 있다.

```java
public class Info {

	private String version;

	public void printInfo() {
		System.out.println("version = " + version);
	}

	@Value("${info.version}")
	public void setVersion(String version) {
		this.version = version;
	}

}
```