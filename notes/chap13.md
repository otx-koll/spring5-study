# MVC 3 : 세션, 인터셉터, 쿠키

## 컨트롤러에서 HttpSession 사용하기

로그인 상태를 유지하는 방법은 `HttpSession`을 이용하거나 `Cookie`를 이용하는 방법이 있다. `Httpsession`을 사용하려면 다음 두 가지 방법 중 한가지를 적용해야 한다.

- 요청 매핑 에노테이션 적용 메서드에 `HttpSession` 파라미터를 추가한다.
- 요청 매핑 에노테이션 적용 메서드에 `HttpServletRequest` 파라미터를 추가하고 `HttpServletRequest`를 이용해서 `HttpSession`을 구한다.

아래는 첫 번째 방법을 사용한 코드의 예시이다.

```java
@PostMapping
public String form(LoginCommand loginCommand, Errors errors, HttpSession session) {
    ... // session을 사용하는 코드
}
```



