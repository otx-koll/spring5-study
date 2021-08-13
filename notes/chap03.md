# chap 03 스프링 DI

## DI(Dependency Injection)

- 의존 주입
- `의존` : 변경에 의해 영향을 받는 관계를 말한다
- 의존하는 객체를 직접 생성하는 대신 의존 객체를 전달받는 방식

의존 객체를 직접 생성하는 방식과 달리 의존 객체를 주입하는 방식은 코드가 더 길어진다. 그렇다면 왜 굳이 생성자를 통해 의존하는 객체를 주입하는 것인가? 이유는 **변경의 유연함** 때문이다.

## DI와 의존 객체 변경의 유연함

**1. 의존 객체를 직접 생성하는 방식**

```java
public class MemberRegisterSevice {
	private MemberDao memberDao = new MemberDao();
	...
}
///////////////////////////////////
public class ChangePasswordSevice {
	private MemberDao memberDao = new MemberDao();
	...
}
```

MemberDao 클래스는 회원 데이터를 DB에 저장한다고 가정한다. 이 상태에서 회원 정보의 빠른 조회를 위해 캐시를 적용해야하는 상황 발생하여 MemberDao 클래스를 상속받은 CachedMemberDao 클래스를 만들었다.

```java
public class CachedMemberDao extends MemberDao {
	...
}
```

CachedMemberDao를 사용하려면 MemberRegisterSevice 클래스와 ChangePasswordSevice 클래스를 다음과 같이 변경해야 한다.

```java
// 변경 전
private MemberDao memberDao = new MemberDao();

// 변경 후
private MemberDao memberDao = new CachedMemberDao();
```

**2. 생성자를 통해서 의존 객체를 주입 받는 방식**

```java
public class MemberRegisterSevice {
	private MemberDao memberDao;
	public MemberRegisterSevice(MemberDao memberDao) {
		this.memberDao = memberDao;
	}
}

public class ChangePasswordSevice {
	private MemberDao memberDao;
	public ChangePasswordSevice(MemberDao memberDao) {
		this.memberDao = memberDao;
	}
}
```

두 클래스의 객체를 생성하는 코드는 다음과 같다

```java
// 변경 전
MemberDao memberDao = new MemberDao();
MemberRegisterSevice regSvc = new MemberRegisterSevice(memberDao);
ChangePasswordSevice pwdSvc = new ChangePasswordSevice(memberDao);
```

MemberDao를 CachedMemberDao로 수정해보자면

```java
// 변경 후
MemberDao memberDao = new CachedMemberDao();
MemberRegisterSevice regSvc = new MemberRegisterSevice(memberDao);
ChangePasswordSevice pwdSvc = new ChangePasswordSevice(memberDao);
```

한 곳만 변경하면 된다. 이처럼 변경할 코드가 한 곳으로 집중되는 것을 알 수 있다.

## RuntimeException

- 실행 중에 발생하는 RuntimeException
- 시스템 환경적으로 input 값이 잘못된 경우, 의도적으로 프로그래머가 잡아내기 위한 조건등에 부합할 때 발생되게 만든다.
- 예를들어 메소드 test에 인자 a가 0인 경우, 프로그램이 더이상 동작하지 않게 하고 싶다면 RuntimeException을 발생시키면 된다.

    ```java
    public static void test(int a) {
    	if(a == 0) 
    		throw new RuntimeException("a는 0이 되선 안된다.");
    }
    ```

## LocalDateTime

- 날짜와 시간 정보 모두가 필요할 때 사용

```java
LocalDateTime currentDateTime = LocalDateTime.now();
// 결과 : 2021-08-13T22:53:03.351797600
LocalDateTime targetDateTime = LocalDateTime.of(2021, 3, 20, 22, 33, 44, 5555);
// 결과 : 2021-03-20T22:33:44.000005555
```