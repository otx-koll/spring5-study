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
---
## 자바 입출력 함수

- 버퍼를 이용해서 읽고 쓰는 함수
- `버퍼(buffer)`
    - 데이터를 한 곳에서 다른 한 곳으로 전송하는 동안 일시적으로 그 데이터를 보관하는 임시 메모리 영역
    - 입출력 속도 향상을 위해 버퍼 사용
- `BufferedReader`, `BufferedWriter`

### BufferedReader

- Enter만 경계로 인식하고 받은 데이터가 String으로 고정되기 때문에 데이터를 가공하는 작업이 필요한 경우가 많다.

```java
BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
String s = bf.readLine(); // String
int i = Integer.parseInt(bf.readLine()); // Int
```

1. 입력은 readLine() 메서드 이용한다. 리턴시 String으로 값이 고정되기 때문에 다른 타입으로 입력받고싶다면 형변환을 꼭 해주어야 한다.
2. 예외처리를 해줘야한다. try&catch를 활용하여 예외처리를 해줘도 좋지만, 대개 throws IOException을 통하여 작업한다.

```java
StringTokenizer st = new StringTokenizer(s);
int a = Integer.parseInt(st.nextToken()); //첫번째 호출
int b = Integer.parseInt(st.nextToken()); //두번째 호출

String array[] = s.split(" ");
```

읽어들인 데이터는 라인 단위로 나눠지기 때문에 데이터를 가공하려면 따로 작업을 해줘야 한다.

1. StringTokenizer 에 nextToken()함수를 쓰면 readLine()을 통해 입력받은 값을 공백단위로 구분하여 순서대로 호출 가능
2. String.split()함수를 활용하여 공백단위로 끊어서 데이터를 넣고 사용하는 방식

### BufferedWriter

```java
BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
String s = "abcd"; //출력할 문자열
bw.write(s+"\n"); //버퍼에 있는 값 전부 출력
bw.flush(); //남아있는 데이터를 모두 출력시킴
bw.close(); //스트림을 닫음
```

1. 버퍼를 잡아 놓았기 때문에 반드시 flush() / close() 를 반드시 호출하여 뒤처리를 해줘야 한다.
2. bw.write에는 System.out.println()과 같이 자동개행기능이 없기 때문에 개행을 해주어야 할 경우, \n을 통해 따로 처리해야 한다.


## Collection
- 데이터의 집합, 그룹을 의미한다

![Untitled](https://t1.daumcdn.net/cfile/tistory/99B88F3E5AC70FB419)
인터페이스|구현클래스|특징
-|-|-
Set|HashSet, TreeSet|순서를 유지하지 않는 데이터의 집합. 데이터 중복 비허용
List|LinkedList, Vector, ArrayList|순서가 있는 데이터 집합. 데이터 중복 허용
Queue|LinkedList, PriorityQueue|List와 유사
Map|Hashtable, HashMap, TreeMap|키, 값의 쌍으로 이루어진 데이터 집합. 순서는 유지되지 않고, 키 중복 비허용, 값 중복 허용

**1. Set 인터페이스**
- `HashSet` : 가장 빠른 임의 접근 속도, 순서 예측 불가
- `TreeSet` : 정렬 방법을 지정할 수 있다

**2. List 인터페이스**
- `LinkedList`
    - 양방향 포인터 구조로 데이터 위치정보만 수정하면 됨
    - 스택, 큐, 양방향 큐 등을 만들기 위한 용도
- `Vector` : 과거에 대용량 처리를 위해 사용. 내부에서 자동으로 동기화처리가 일어나 성능 별로고 무거워서 잘 안씀
- `ArrayList` : 단방향 포인터 구조. 각 데이터에 대한 인덱스를 가지고 있어 조회 기능에 성능 뛰어남

**3. Map 인터페이스**
- `Hashtable` : HashMap보단 느리지만 동기화 지원하고 null불가
- `HashMap` : 중복과 순서 비허용. null값 가능
- `TreeMap` : 정렬된 순서대로 키, 값을 저장하여 검색이 빠름

### 생성자 vs setter 메서드
- 생성자 방식 : 빈 객체를 생성하는 시점에 모든 의존 객체가 주입된다
- 설정 메서드 방식 : 세터 메서드 이름을 통해 어떤 의존 객체가 주입되는지 알 수 있다.

---

## Annotation

```java
@Configuration // 스프링 설정 클래스
@Bean // 각 메서드마다 한 개의 빈 객체 생성
@Autowired // 해당 타입의 빈을 찾아서 필드에 할당
```

## 두 개 이상의 설정 파일 사용하기

**1. 두 설정 클래스 파일을 설정한다**

```java
@Autowired
private MemberDao memberDao;
@Autowired
private MemberPrinter memberPrinter;
```

```java
ctx = new AnnotaionConfigApplicationContext(AppConf1.class, AppConf2.class);
```

AnnotaionConfigApplicationContext의 생성자의 인자는 가변 인자이기 때문에 설정 클래스 목록을 콤마로 구분해서 전달

**2. @Import 에노테이션 이용**

```java
@Configuration
@Import(AppConf2.class)
public class AppConflmport {
	@Bean
	public MemberDao memberDao() {
		return new MemberDao();
	}
	@Bean
	public MemberPrinter memberPrinter() {
		return new MemberPrinter();
	}
}
```

배열을 이용해서 두 개 이상의 설정 클래스도 지정할 수 있다.

```java
@Configuration
@Import({AppConf1.class, AppConf2.class)
public class AppConflmport {
	
}
```