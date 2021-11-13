# 간단한 웹 어플리케이션의 구조

## 구성 요소

- 프론트 서블릿
- 컨트롤러 + 뷰
- 서비스
- DAO

`프론트 서블릿`은 웹 브라우저의 모든 요청을 받은 창구 역할이다. 스프링 MVC에서는 `DispatcherServlet`이 프론트 서블릿의 역할을 수행한다. 

`컨트롤러`의 주요 역할은 다음과 같다.

- 클라이언트가 요구한 기능을 실행
- 응답 결과를 생성하는데 필요한 모델 생성
- 응답 결과를 생성할 뷰 선택

`서비스`는 기능의 로직을 구현한다.  

DB연동이 필요하면 `DAO`를 사용한다. `DAO`는 `Data Access Object`의 약자로 DB와 웹 어플리케이션 간에 데이터를 이동시켜주는 역할을 한다.

부가적인 로직이 없는 경우엔 컨트롤러에서 직접 DAO를 사용하기도 한다.

## 서비스의 구현

여러 단계를 거치는 로직은 트랜잭션 범위에서 실행한다. 예를 들어 비밀번호 변경 기능을 수행한다고 한다.

- DB에서 비밀번호를 변경할 회원의 데이터를 구함
- 존재하지 않으면 익셉션 발생
- 회원 데이터의 비밀번호 변경
- 변경 내역을 DB에 반영

```java
@Transactional
public void changePassword(String email, String oldPwd, String newPwd) {
    Member member = memberDao.selectByEmail(email);
    if (member == null)
        throw new MemberNotFoundException();
    
    member.changePassword(oldPwd, newPwd);
    memberDao.update(member);
}
```

회원가입 기능은 다음과 같이 필요한 데이터를 담고 있는 별도의 클래스를 파라미터로 사용했다.

```java
public void regist(RegisterRequest req)
```

필요한 데이터를 전달 받기 위해 별도 타입을 만들면 스프링 MVC의 커맨드 객체로 해당 타입을 사용할 수 있어 편하다. 아래 코드는 회원 가입 요청을 처리하는 컨트롤러 클래스이다.

```java
@PostMapping("/register/step3")
public String handleStep3(RegisterRequest regReq, Errors errors) {
    ...
    memberRegisterService.regist(regReq);
    ...
}
```

서비스 메서드는 기능을 실행한 후에 결과를 알려줘야 한다. 결과는 크게 리턴 값을이용한 정상 결과와 익셉션을 이용한 비정상 결과로 알려준다. 다음 예제와 같다.

```java
public class AuthService {
    public AuthInfo authenticate(String email, String password) {
        Member member = memberDao.selectByEmail(email);
        if (member == null) {
            throw new WrongIdPasswordException();
        }
        if (!member.matchPassword(password)) {
            throw new WrongIdPasswordException();
        }
        return new AuthInfo(member.getId(),
                member.getEmail(),
                member.getName());
    }
}
```
`AuthService` 클래스의 `authenticate()` 메서드를 보면 리턴 타입으로 `AuthInfo`를 사용하고 있다. 인증에 성공할 경우 인증 정보를 담고 있는 `AuthInfo` 객체를 리턴해서 정상적으로 실행되었음을 알려준다. 리턴 타입이 void인 경우 익셉션이 발생하지 않은 것이 정상적으로 실행된 것을 의미한다.

`authenticate()` 메서드는 인증 대상 회원이 존재하지 않거나 비밀번호가 일치하지 않는 경우 `WrongIdPasswordException`을 발생시킨다. 따라서 이 메서드가 익셉션을 발생하면 인증에 실패했다는 것이다. 

## 컨트롤러에서의 DAO 접근

단순한 로직같은 것들은 컨트롤러에서 간단히 구현하곤 한다.

```java
@RequestMapping("/members/detail/{id}")
public String detail(@PathVariable("id") Long id, Model model) {
    // 사실상 DAO를 직접 호출하는 것과 동일
    Member member = memberDao.selectByEmail(id);
    if (member == null) {
        return "member/notFound";
    }
    model.addAttribute("member", member);
    return "member/memberDetail";
}
```

## 패키지 구성

웹 요청을 처리 하기 위한 영역에는 컨트롤러 클래스와 관련 클래스들이 위치한다. 커맨드 객체의 값을 검증하기 위한 `Validator`도 웹 요청 처리 영역에 위치할 수 있는데 관점에 따라 `Validator`를 제공 영역에 위치시킬 수 있다. 

기능 제공 영역에는 기능 제공을 위해 필요한 서비스, DAO, 그리고 `Member`와 같은 모델 클래스가 위치한다. 

서비스와 관련된 클래스 개수가 많다면 서비스를 위한 패키지를 구분하여 코드를 체계적으로 관리할 수 있다. 