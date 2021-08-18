# chap 04 의존 자동 주입

## @Autowired - 의존 자동 주입

필드나 setter 메서드에 붙이면 스프링은 타입이 일치하는 빈 객체를 찾아서 주입한다.

```java
@Bean
public MemberDao memberDao() {
  return new MemberDao();
}

@Bean
public ChangePasswordService changePwdSvc() {
  ChangePasswordService pwdSvc = new ChangePasswordService();
  // pwdSvc.setMemberDao(memberDao());	
  return pwdSvc;
}

```

자동 주입 기능을 사용하면 `@Bean` 메서드에서 의존을 주입하지 않아도 의존 객체가 주입된다.

`@Autowired` 어노테이션은 필드 뿐만 아니라 메서드에도 붙일 수 있다.

```java
@Autowired
public void setMemberDao(MemberDao memberDao) {
	this.memberDao = memberDao;
}

@Autowired
public void setMemberPrinter(MemberPrinter printer) {
	this.printer = printer;
}
```
빈 이름	|@Qualifier 선언|@Qualifier 호출
-|-|-
memberPrinter1|	Example|Example
memberPrinter2| |memberPrinter2

## @Autowired - 중복관계

### 일치하는 Bean이 없는 경우

컨테이너에 등록된 Bean 객체끼리만 상호작용을 시키기 때문에, Exception이 발생하면서 제대로 실행되지 않는다.

### @Autowired을 붙인 주입 대상에 일치하는 빈이 두 개 이상일 경우

Exception이 발생하면서 제대로 실행되지 않는다. 스프링은 독단적으로 하나의 Bean을 선택할 수 있는 능력이 없기 때문에 `@Qualifier` 어노테이션을 사용하여 자동 주입 대상 빈을 한정할 수 있다.

## @Qualifier

같은 타입의 Bean 객체가 있을 경우 해당 아이디를 적어 원하는 Bean이 주입될 수 있도록 하는 어노테이션이다.

```java
@Bean
@Qualifier("Example") // 이 별명으로 호출한다고 선언
public MemberPrinter memberPrinter1() {
	return new MemberPrinter();
}

@Bean // 아무런 별명 설정 안됨
public MemberPrinter memberPrinter2() {
	return new MemberPrinter();
}
```

```java
@Autowired
@Qualifier("Example")
public void setMemberPrinter(MemberPrinter printer) {
	this.printer = printer;
}
```

`@Qualifier` 에노테이션을 통해 설정한 이름과 일치하는 Bean 객체를 찾아 주입을 진행한다.

```java
@Autowired
@Qualifier("memberPrinter2")
public void setMemberPrinter(MemberPrinter printer) {
	this.printer = printer;
}
```

`@Configuration` 파일 쪽에 `@Qualifier` 를 선언하지 않고, 주입 받는 쪽에만 `@Qualifier` 에노테이션을 사용해도 주입이 가능하다. 

## @Autowired - 상속관계

Bean 객체가 서로 상속 관계에 있으면 동일한 Bean 객체로 판단하여 예외를 던진다

```java
public class MemberSummaryPrinter extends MemberPrinter {
  @Override
  public void print(Member member) {
      System.out.printf("회원 정보: 이메일=%s, 이름=%s\n", member.getEmail(), member.getName());
  }
}
```

```java
@Configuration
public class AppCtx {    

	@Bean			
  public MemberPrinter memberPrinter1() {
      return new MemberPrinter();
  }

  @Bean			
  public MemberSummaryPrinter memberPrinter2() {
      return new MemberSummaryPrinter();
  }
}
```

`@Qualifier` 에노테이션을 사용하여 해결 가능

## @Autowired - 주입 강제

`@Autowired` 에노테이션은 주입이 꼭 필요하지 않는 객체에게도 주입을 시도한다. 만약 자동 주입할 Bean이 존재하지 않을 경우, Exception이 발생한다.

Bean 주입이 필수가 아닐 경우엔 `@Autowired` 에노테이션의 required 속성을 false로 지정하면 된다.

```java
public class MemberPrinter {
  private DateTimeFormatter dateTimeFormatter;
  
  public void print(Member member) {
		...
  }
  
  @Autowired(required = false)
  public void setDateFormatter(DateTimeFormatter dateTimeFormatter) {
    this.dateTimeFormatter = dateTimeFormatter;
  }
}
```

`@Autowired(required = false)` 로 지정하게 되면 `DateTimeFormatter` 타입의 빈이 없어도 Exception을 발생하지 않고, `setDateFormatter()` 메서드를 실행하지 않는다.

스프링 5 버전부터는 `@Autowired` 에노테이션의 required 속성을 false로 하는 대신, 자바8의 `Optional`을 사용할 수 있다.

```java
public class MemberPrinter {
  private DateTimeFormatter dateTimeFormatter;
  
  public void print(Member member) {
    ...
  }
  
  @Autowired
  public void setDateFormatter(Optional<DateTimeFormatter> dateTimeFormatter) {
    this.dateTimeFormatter = dateTimeFormatter;
  }
}
```

`@Nullable` 에노테이션을 사용할 수도 있다.

```java
public class MemberPrinter {
  private DateTimeFormatter dateTimeFormatter;
  
  public void print(Member member) {
    ...
  }
  
  @Autowired
  public void setDateFormatter(@Nullable DateTimeFormatter dateTimeFormatter) {
    this.dateTimeFormatter = dateTimeFormatter;
  }
}
```

`@Autowired(required = false)` 와 `@Nullable` 의 차이점은 주입 시도 유무다.

- `@Autowired(required = false)` : 해당하는 Bean이 없는 경우 주입 시도도 안함
- `Optional` : 해당하는 Bean이 없으면 값이 없는 Optional 주입을 시도함
- `@Nullable` : 해당하는 Bean이 없으면 Null을 인자로 주입을 시도함
