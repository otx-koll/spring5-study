# MVC 1 : 요청 매핑, 커맨드 객체, 리다이렉트, 폼 태그, 모델

## 요청 매핑 애노테이션

- `@RequestMapping`
- `@GetMapping`
- `@PostMapping`

요청 매핑 애노테이션을 적용한 메서드를 두 개 이상 정의할 수도 있다. 여러 단계를 거쳐 하나의 기능이 완성되는 경우, 관련 요청 경로를 한개의 컨트롤러 클래스에서 처리하면 코드 관리에 용이하다.

```java
@Controller
@RequestMapping("/register")
public class RegisterController { // 각 메서드에 공통되는 경로

	@RequestMapping("/step1") // 공통 경로를 제외한 나머지 경로
	public String handleStep1() {
		return "register/step1";
	}

	@RequestMapping("/step2")
	public String handleStep2() {
		...
	}

}
```

요청 매핑 애노테이션의 경로가 "/register"로 시작한다. 공통된 부분의 경로를 `@RequestMapping` 애노테이션을 클래스에 적용하고, 나머지 경로를 값으로 갖는 요청 매핑 애노테이션을 적용할 수 있다.

### GET과 POST구분



