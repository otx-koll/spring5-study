# MVC 4 : 날짜 값 변환, @PathVariable, 익셉션 처리

## @DateTimeFormat

스프링은 Long이나 int와 같은 기본 데이터 타입으로의 변환은 기본적으로 처리해주지만 `LocalDateTime` 타입으로의 변환은 추가 설정이 필요하다. `@DateTimeFormat` 에노테이션을 적용하면 된다.

```java
public class ListCommand {

	@DateTimeFormat(pattern = "yyyyMMddHH")
	private LocalDateTime from;
	@DateTimeFormat(pattern = "yyyyMMddHH")
	private LocalDateTime to;

}
```

`LocalDateTime` 값을 원하는 형식으로 출력해주는 커스텀 태그 파일을 작성해야 한다. JSTL이 제공하는 날짜 형식 태그는 자바 8의 `LocalDateTime` 타입은 지원하지 않기 때문에 아래와 같은 태그 파일을 사용하여 `LocalDateTime` 값을 지정한 형식으로 출력한다.

```
<%@ tag body-content="empty" pageEncoding="utf-8" %>
<%@ tag import="java.time.format.DateTimeFormatter" %>
<%@ tag trimDirectiveWhitespaces="true" %>
<%@ attribute name="value" required="true" 
              type="java.time.temporal.TemporalAccessor" %>
<%@ attribute name="pattern" type="java.lang.String" %>
<%
	if (pattern == null) pattern = "yyyy-MM-dd";
%>
<%= DateTimeFormatter.ofPattern(pattern).format(value) %>
```

## MemberDao 클래스 중복 코드 정리

```java
public Member selectByEmail(String email) {
    List<Member> results = jdbcTemplate.query(
            "select * from MEMBER where EMAIL = ?",
            new RowMapper<Member>() {
                @Override
                public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Member member = new Member(
                            rs.getString("EMAIL"),
                            rs.getString("PASSWORD"),
                            rs.getString("NAME"),
                            rs.getTimestamp("REGDATE").toLocalDateTime());
                    member.setId(rs.getLong("ID"));
                    return member;
                }
            }, email);

    return results.isEmpty() ? null : results.get(0);
}

public List<Member> selectAll() {
    List<Member> results = jdbcTemplate.query("select * from MEMBER",
            (ResultSet rs, int rowNum) -> {
                Member member = new Member(
                        rs.getString("EMAIL"),
                        rs.getString("PASSWORD"),
                        rs.getString("NAME"),
                        rs.getTimestamp("REGDATE").toLocalDateTime());
                member.setId(rs.getLong("ID"));
                return member;
            });
    return results;
}
```

다음과 같이 `RowMapper` 객체를 생성하는 부분의 코드가 중복된 것을 볼 수 있다. 중복을 제거하기 위해 임의 객체를 필드에 할당한다.

```java
public class MemberDao {

    private RowMapper<Member> memRowMapper = 
			new RowMapper<Member>() {
				@Override
				public Member mapRow(ResultSet rs, int rowNum)
						throws SQLException {
					Member member = new Member(rs.getString("EMAIL"),
							rs.getString("PASSWORD"),
							rs.getString("NAME"),
							rs.getTimestamp("REGDATE").toLocalDateTime());
					member.setId(rs.getLong("ID"));
					return member;
				}
			};
    
    ... // 생략

    public Member selectByEmail(String email) {
        List<Member> results = jdbcTemplate.query(
                "select * from MEMBER where EMAIL = ?",
            memRowMapper, email);

        return results.isEmpty() ? null : results.get(0);
    }

    public List<Member> selectAll() {
        List<Member> results = jdbcTemplate.query("select * from MEMBER", memRowMapper);
        return results;
    }
}
```

## @PathVariable을 이용한 경로 변수 처리

경로의 일부가 고정되어 있지 않고 달라질 떄 사용할 수 있는 것이 `@PathVariable` 에노테이션이다. `@PathVariable` 에노테이션을 이용하면 가변 경로를 처리할 수 있다.

```java
@GetMapping("/members/{id}")
public String detail(@PathVariable("id") Long memId, Model model) {
    Member member = memberDao.selectById(memId);
    if (member == null) {
        throw new MemberNotFoundException();
    }
    model.addAttribute("member", member);
    return "member/memberDetail";
}
```

매핑 경로에 '{경로변수}'와 같이 중괄호로 둘러 쌓인 부분을 경로 변수라고 한다. 여기에 해당하는 값은 경로 변수 이름을 지정한 `@PathVariable` 파라미터에 전달된다. 

## 컨트롤러 익셉션 처리하기

익셉션 화면이 보이는 것보다 알맞게 익셉션을 처리하여 사용자에게 안내해주는 것이 더 좋다. 이때 사용할 수 있는 것이 `@ExceptionHandler` 에노테이션이다.

같은 컨트롤러에 `@ExceptionHandler` 에노테이션을 적용한 메서드가 존재하면 그 메서드가 익셉션을 처리한다. 아래 코드는 그 예시다.

```java
@ExceptionHandler(TypeMismatchException.class)
public String handleTypeMismatchException() {
    return "member/invalidId";
}
```

요청 매핑 에노테이션 적용 메서드와 마찬가지로 뷰 이름을 리턴할 수 있다. 

익셉션 객체에 대한 정보를 알고 싶다면 메서드의 파라미터로 익셉션 객체를 전달받아 사용하면 된다.

```java
@ExceptionHandler(TypeMismatchException.class)
public String handleTypeMismatchException(TypeMismatchException ex) {
    // ex 사용해서 로그 남기는 등의 작업
    return "member/invalidId";
}
```

## ControllerAdvice를 이용한 공통 익셉션 처리

`@ExceptionHandler` 에노테이션은 해당 컨트롤러에서 발생한 익셉션만을 처리한다. 다수의 컨트롤러에서 같은 타입의 익셉션이 발생할 수도 있는데 이때 처리 코드가 동일한다면 불필요한 코드 중복이 발생될 수 있다.

이때 `@ControllerAdvice` 에노테이션을 이용하면 중복을 없앨 수 있다. 다음은 그 사용 예시이다.

```java
@ControllerAdvice("spring")
public class CommonExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	public String handleRuntimeException() {
		return "error/commonException";
	}
}
```
`@ControllerAdvice` 에노테이션이 적용된 클래스는 지정한 범위의 컨트롤러에 공통으로 사용될 설정을 지정할 수 있다. 위 코드는 "spring" 패키지와 그 하위 패키지에 속한 컨트롤러 클래스를 위한 공통 기능을 정의했다. 

`@ControllerAdvice` 적용 클래스가 동작하려면 해당 클래스를 스프링에 빈으로 등록해야 한다.