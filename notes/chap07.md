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
Advice|