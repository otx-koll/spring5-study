# chap 07 AOP 프로그래밍

## 프로젝트 준비

pom.xml 파일에 의존 추가

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.7</version>
    <scope>runtime</scope>
</dependency>
```
## 프록시와 AOP

```java
public interface Calculator {
	public long factorial(long num);
}
```

`Calculator`클래스는 계승을 구하기 위한 인터페이스이다.

```java
public class ImpeCalculator implements Calculator {
	@Override
	public long factorial(long num) {
		long result = 1;
		for(long i = 1; i <= num; i++) {
			result *= i;
		}
		return result;
	}	
}
```

`ImpeCalculator` 클래스는 for 문을 이용해서 계승 값을 구했다. 

```java
public class RecCalculator implements Calculator {
	@Override
	public long factorial(long num) {
		if (num == 0)
			return 1;
		else
			return num * factorial(num - 1);
	}	
}
```

`RecCalculator`클래스는 재귀호출을 이용해서 계승을 구한다. 

이 때 각각의 클래스들의 실행 시간을 출력하려면 어떻게 해야 할까? 메서드의 시작과 끝에서 시간을 구하고 두 시간의 차이를 출력하는 것이다. 

`ImpeCalculator`를 다음과 같이 수정하면 된다.

```java
public class ImpeCalculator implements Calculator {
	@Override
	public long factorial(long num) {
		long start = System.currentTimeMillis();
		long result = 1;
		for(long i = 1; i <= num; i++) {
			result *= i;
		}
		long end = System.currentTimeMillis();
		System.out.printf("ImpeCalculator.factorial(%d) 실행 시간 = %d\n",
	                	num, (end - start));
		return result;
	}	
}
```

`RecCalculator`클래스는 다음과 같이 수정하면 된다.

```java
public class RecCalculator implements Calculator {
	@Override
	public long factorial(long num) {
            long start = System.currentTimeMillis();
            try {
                if (num == 0)
                    return 1;
                else
                    return num * factorial(num - 1);
            } finally {
                long end = System.currentTimeMillis();
                System.out.printf("RecCalculator .factorial(%d) 실행 시간 = %d\n",
                            num, (end - start));
		}
	}	
}
```

그런데 만약 실행 시간을 밀리초 단위가 아니라 나노초 단위로 구해야 한다면 기존의 코드들을 모두 수정해야하며, 코드 중복이 일어날 수도 있다. 

이때 코드의 유지보수와 코드의 중복을 막고자 **프록시 객체**가 등장하였다.

```java
public class ExeTimeCalculator implements Calculator {
	private Calculator delegate;
	
	public ExeTimeCalculator(Calculator delegate) {
		this.delegate = delegate;
	}

	@Override
	public long factorial(long num) {
		long start = System.nanoTime();
		long result = delegate.factorial(num);
		long end = System.nanoTime();
		
		System.out.printf("%s.factorial(%d) 실행 시간 = %d\n", 
                        delegate.getClass().getSimpleName(), num, (end - start));
		return result;
	}
}
```

`ExeTimeCalculator`클래스는 생성자를 통해 다른 `Calculator` 객체를 전달받아 `delegate`필드에 할당하고, `factorial()` 메서드에서 `delegate.factorial()` 메서드를 실행한다. 

`ExeTimeCalculator`클래스를 사용하면 아래와 같은 방법으로 실행 시간을 측정할 수 있다.

```java
public class MainProxy {
	public static void main(String[] args) {
		ExeTimeCalculator ttCal1 = new ExeTimeCalculator(new ImpeCalculator());
		System.out.println(ttCal1.factorial(20));
		
		ExeTimeCalculator ttCal2 = new ExeTimeCalculator(new RecCalculator());
		System.out.println(ttCal2.factorial(20));
	}
}
```

```java
ImpeCalculator.factorial(20) 실행 시간 = 2300
2432902008176640000
RecCalculator.factorial(20) 실행 시간 = 2600
2432902008176640000
```

위 결과로 다음을 알 수 있다.

- 기존 코드를 변경하지 않고 실행 시간을 출력할 수 있다.
- 실행 시간을 구하는 코드의 중복을 제거했다. 나노초 대신 밀리초를 사용하여 실행 시간을 구하고 싶다면 `ExeTimeCalculator` 클래스만 변경하면 된다.

**프록시**는 핵심 기능을 구현하지 않는 대신 여러 객체에 공통으로 적용할 수 있는 기능을 구현한다. 

이렇게 공통 기능 구현과 핵심 기능 구현을 분리하는 것이 **AOP**의 핵심이다.

## AOP

AOP의 기본 개념은 핵심 기능에 공통 기능을 삽입하는 것이다.

핵심 기능에 공통 기능을 삽입하는 방법에는 3가지가 있다.

1. 컴파일 시점에 코드에 공통 기능을 삽입
2. 클래스 로딩 시점에 바이트 코드에 공통 기능 삽입
3. 런타임에 프록시 객체를 생성해서 공통 기능을 삽입

1, 2번 방법은 `AspectJ`와 같은 AOP 전용 도구를 사용하여 적용할 수 있다.

스프링이 제공하는 AOP 방식은 프록시를 이용한 세 번째 방식이다. 프록시 방식은 앞서 살펴본 것처럼 중간에 프록시 객체를 생성해준다. 따라서 `ExeTimeCalculator`와 같은 프록시 클래스를 직접 구현할 필요가 없다. 단지 공통 기능을 구현한 클래스만 알맞게 구현하면 된다.

## AOP 주요 용어

용어|의미
-|-
Advice|언제 공통 관심 기능을 핵심 로직에 적용할 지를 정의
Joinpoint|Advice가 적용 가능한 지점
Pointcut|Advice가 적용되는 지점
Weaving|Advice를 핵심 로직 코드에 적용하는 것
Aspect|공통으로 적용되는 기능

### Advice 종류
종류|설명
-|-
Before Advice|대상 객체 메서드 호출 전에 공통 기능 실행
After Returning Advice|대상 객체 메서드가 익셉션 없이 실행 된 이후 공통 기능 실행
After Throwing Advice|대상 객체 메서드를 실행하는 도중 익셉션 발생 시 공통 기능 실행
After Advice|익셉션 발생 여부 상고나없이 대상 객체 메서드 실행 후 공통 기능 실행
Around Advice|대상 객체 메서드 실행 전, 후 또는 익셉션 발생 시점에 공통 기능 실행

이 중에서 제일 자주 사용되는 것은 `Aroung Advice`이다.

## 스프링 AOP 구현
- Aspect로 사용할 클래스에 `@Aspect` 에노테이션을 붙인다.
- `@Pointcut` 에노테이션으로 공통 기능을 적용할 지점을 정의한다.
- 공통 기능을 구현한 메서드에 `@Around` 에노테이션을 적용한다.

### Aspect
```java
@Aspect
public class ExeTimeAspect {

	@Pointcut("execution(public * chap07..*(..))")
	private void publicTarget() {
	}

	@Around("publicTarget()")
	public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.nanoTime();
		try {
			Object result = joinPoint.proceed();
			return result;
		} finally {
			long finish = System.nanoTime();
			Signature sig = joinPoint.getSignature();
			System.out.printf("%s.%s(%s) 실행 시간 : %d ns\n",
					joinPoint.getTarget().getClass().getSimpleName(),
					sig.getName(), Arrays.toString(joinPoint.getArgs()),
					(finish - start));
		}
	}

}
```
`@Aspect` 에노테이션을 적용하여 Aspect로 사용할 클래스임을 지정하였다.

`@Pointcut` 에노테이션으로 공통 기능을 적용할 대상을 설정한다.

`@Around` 에노테이션은 [Around Advice](#Advice-종류)를 설정한다. `@Around`에노테이션 값이 "publicTarget()"인데 이는 publicTarget() 메서드에 정의한 `Pointcut`에 공통 기능을 적용한다는 것을 의미한다.

`ProceedingJoinPoint` 타입 파라미터는 프록시 대상 객체의 메서드를 호출할 때 사용한다. `proceed()` 메서드를 사용해서 실제 대상 객체의 메서드를 호출한다. 

### Configuration
```java
@Configuration
@EnableAspectJAutoProxy
public class AppCtx {
	
	@Bean
	public ExeTimeAspect exeTimeCalculator() {
		return new ExeTimeAspect();
	}
	
	@Bean
	public Calculator calculator() {
		return new RecCalculator();
	}
}
```

`@Aspect` 에노테이션을 붙인 클래스를 공통 기능으로 적용하려면 `@EnableAspectJAutoProxy` 에노테이션을 `@Configuration` 설정 클래스에 붙여야 한다. 이 에노테이션을 추가하면 스프링은 `@Aspect` 에노테이션이 붙은 빈 객체를 찾아서 빈 객체의 `@Pointcut`, `@Around` 설정을 사용한다. 

### 실행 결과
```java
public class MainAspect {
	
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtx.class);
		
		Calculator cal = ctx.getBean("calculator", Calculator.class);
		long fiveFact = cal.factorial(5);
		System.out.println("cal.factorial(5) = " + fiveFact);
		System.out.println(cal.getClass().getName());
		ctx.close();
	}
	
}
```

```java
RecCalculator.factorial([5]) 실행 시간 : 26500 ns
cal.factorial(5) = 120
jdk.proxy2.$Proxy18
```

첫 번째 줄은 `ExeTimeAspect` 클래스의 `measure()` 메서드가 출력한 것이다.

세 번째 줄은 `MainAspect` 클래스의 `cal.getClass().getName();`에서 출력한 코드이다. 코드를 보면 `Calculator` 타입이 `RecCalculator` 클래스가 아니고 `$Proxy18`임을 알 수 있다. 즉, 이 타입은 스프링이 생성한 프록시 타입이다.

만약 AOP를 적용하지 않았다면 리턴한 객체는 프록시가 아닌 `RecCalculator` 타입이다. `AppCtx` 클래스에서 `exeTimeAspect()` 메서드를 주석처리 후, 다시 `MainAspect` 클래스를 실행해보면 다음과 같은 메시지가 출력 된다.

```java
cal.factorial(5) = 120
chap07.RecCalculator
```



### ProceedingJoinPoint 메서드

`Around Advice`에서 사용할 공통 기능 메서드는 대부분 파라미터로 전달받은 `ProceedingJoinPoint`의 `proceed()` 메서드만 호출하면 된다. 

호출되는 대상 객체에 대한 정보, 실행되는 메서드에 대한 정보, 메서드를 호출 할 때 전달된 인자에 대한 정보가 필요할 때가 있다. 이때 `ProceedingJoinPoint` 인터페이스는 다음 메서드를 제공한다.

**ProceedingJoinPoint 인터페이스 제공 메서드**

메서드|설명
-|-
Signature getSignature()|호출되는 메서드에 대한 정보를 구한다.
Object getTarget()|대상 객체를 구한다.
Object[] getArgs()|파라미터 목록을 구한다.

**org.aspectj.lang.Signature 인터페이스 제공 메서드**
메서드|설명
-|-
String getName()|호출되는 메서드의 이름을 구한다.
String toLongString()|호출되는 메서드를 완전하게 표현한 문장을 구한다. (메서드의 리턴 타입, 파라미터 타입 모두 표시됨)
String toShortString()|호출되는 메서드를 축약해서 표현한 문장을 구한다. (기본 구현은 메서드의 이름만 구함)

**프록시 생성 방식**
Bean 객체가 인터페이스를 상속할 때 인터페이스가 아닌 클래스를 이용하여 프록시를 생성하고자 한다면 `@EnableAspectJAutoProxy(proxyTargetClass = true)`

**execution 명시자 표현식**

execution 명시자는 Advice를 적용할 메서드를 지정할 때 사용한다. 기본 형식은 아래와 같다.

```java
execution(수식어패턴? 리턴타입패턴 클래스이름패턴?메서드일므패턴(파라미터패턴))
```

- 수식어패턴은 생략 가능하며 public, protected  등이 온다.
- 리턴타입패턴은 리턴 타입을 명시한다.
- 클래스이름패턴과 메서드일므패턴은 클래스 이름 및 메서드 이름을 패턴으로 명시한다.
- 파라미터패턴은 매칭될 파라미터에 대해서 명시한다.
- 각 패턴은 `*` 을 이용하여 모든 값을 표현할 수 있고, `..*점 두 개)` 를 이용하여 0개 이상이라는 의미를 표현할 수 있다.

예|설명
-|-
execution(public void set*(..))|리턴 타입이 void, 메서드 이름 set으로 시작, 파라미터 0개 이상인 메서드 호출, 파타미터 부분에 '..'을 사용하여 파라미터 0개 이상
execution(* chap07.*.*())|chap07 패키지의 타입에 속한 파라미터가 없는 모든 메서드 호출
execution(* chap07..*.*(..))|chap07 패키지 및 하위 패키지에 있는, 파라미터가 0개 이상인 메서드 호출, 패키지 부분에 '..'을 사용하여 해당 패키지 또는 하위 패키지 표현
execution(Long chap07 .Calculator.factorial(..))|리턴 타입이 Long인 Calculator 타입의 factorial() 메서드 호출
execution(* get*(*))|이름이 get으로 시작하고 파라미터가 한 개인 메서드 호출
execution(* get*(*, *))|이름이 get으로 시작하고 파라미터가 두 개인 메서드 호출
execution(* read*(Integer, ..))|메서드 이름이 read로 시작, 첫 번째 파라미터 타입이 Integer, 한 개 이상의 파라미터를 갖는다

## Advice 적용 순서 지정
`@Order` 에노테이션을 사용하면 적용 순서를 지정할 수 있다. `@Aspect`에노테이션과 함께 `@Order`에노테이션을 클래스에 붙이면 `@Order`에노테이션에 지정한 값에 따라 적용 순서를 결정한다.

`@Order` 에노테이션의 값이 작으면 먼저 적용하고, 크면 나중에 적용한다. 예를 들어 아래와 같이 두 `Aspect` 클래스에 `@Order`에노테이션을 적용했다고 하자.

```java
@Aspect
@Order(1)
public class ExeTimeAspect {
	...
}
```
```java
@Aspect
@Order(2)
public class CacheAspect {
	...
}
```
원래 `CacheAspect` 프록시가 먼저 적용됐었지만 이렇게 적용시키면 `ExeTImeAspect` 프록시가 먼저 적용된다.

## @Around의 Pointcut 설정과 @Pointcut 재사용
`@Pointcut`에노테이션이 아닌 `@Around`에노테이션에 execution 명시자를 직접 지정할 수도 있다.

```java
@Aspect
public class CacheAspect {
	@Around("execution(public * chap07..*(..))")
	public Object executie(ProceedingJoinPoint joinPoint) throws Throwable {
		...
	}
}
```
만약 같은 Pointcut을 여러 Advice가 함께 사용한다면 곹오 Pointcut을 재사용할 수도 있다. ExeTimeAspect코드를 다시 보자.
```java
@Aspect
public class ExeTimeAspect {

	@Pointcut("execution(public * chap07..*(..))")
	private void publicTarget() {
	}

	@Around("publicTarget()")
	public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
		...
	}
```
다른 클래스에 위치한 `@Around`에노테이션에서 `publicTarget()`메서드의 Pointcut을 사용하고 싶다면 private에서 public으로 바꾸면 된다.

그리고 해당 `Pointcut`의 완전한 클래스 이름을 포함한 메서드 이름을 `@Around`에노테이션에서 사용하면 된다.

```java
@Aspect
public class CacheAspect {

	@Around("aspect.ExeTimeAspect.publicTarget()")
	public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
		...
	}
```
같은 패키지에 위치한다면 패키지 이름 없이 간단한 클래스 이름으로 설정할 수 있다.

이처럼 여러 Aspect에서 공통으로 사용하는 Pointcut이 있다면 별도 클래스에 Pointcut을 정의하고, 각 Aspect 클래스에서 해당 Pointcut을 사용하도록 구성하면 Pointcut 관리가 편해진다.



