# chap 08 DB 연동

### pom.xml

```xml
<!-- JDBC 연동에 필요한 기능 제공 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
    <version>5.3.9</version>
</dependency>

<!-- DB 커넥션풀 기능 -->
<dependency>
    <groupId>org.apache.tomcat</groupId>
    <artifactId>tomcat-jdbc</artifactId>
    <version>8.5.27</version>
</dependency>

<!-- MySQL 연결에 필요한 JDBC 드라이버 제공 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.22</version>
</dependency>
```

커넥션 풀은 최초 연결에 따른 응답 속도 저하와 동시 접속자가 많을 때 발생하는 부하를 줄이기 위해 사용하는 것이다. 일정 개수의 DB 커넥션을 미리 만들어두는 기법이다.

## DataSource

스프링이 제공하는 DB 연동 기능은 DataSource를 사용해서 DB Connection을 구한다. DB 연동에 사용할 DataSource를 스프링 Bean으로 등록하고, DB 연동 기능을 구현한 빈 객체는 DataSource를 주입받아 사용한다.

아래 코드는 DB연동 기능을 구현한 Bean 객체제 주입하는 DataSource를 설정하는 클래스이다.

```java
@Configuration
public class DbConfig {
	
	@Bean(destroyMethod="close")
	public DataSource dataSource() {
		DataSource ds = new DataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost/spring5fs?characterEncoding=utf8");
		ds.setUsername("spring5");
		ds.setPassword("spring5");
		ds.setInitialSize(2);
		ds.setMaxActive(10);
		return ds;
	}
}
```

## Connection Pool

`org.apache.tomcat.jdbc.pool.DataSource` 클래스는 커넥션 풀 기능을 제공하는 DataSource 구현체다. 주요 설정 메서드는 아래와 같다.

설정 메서드|설명
-|-
setInitialSize(int)|커넥션 풀 초기화 시 커넥션 개수 지정. 기본 값은 10이다.
setMaxActive(int)|커넥션 풀에서 가져올 수 있는 최대 커넥션. 기본 값은 100이다.
setMaxIdle(int)|커넥션 풀에 유지시킬 수 있는 최대 커넥션 개수. 기본값은 `maxActive`와 같다.
setMinIdle(int)|커넥션 풀에 유지시킬 수 있는 최소 커넥션 개수. 기본값은 `initialSize`에서 가져온다.
setMaxWait(int)|커넥션 풀에서 커넥션을 가져올 때 최대 대기 시간. 밀리 초 단위로 지정하며, 기본 값은 30,000밀리초(30초)다.
setMaxAge(long)|커넥션 연결 후 커넥션의 최대 유효 시간. 밀리 초 단위로 지정. 기본 값은 0이다. 0은 유효 시간이 없음을 의미한다.
setValidationQuery(String)|커넥션이 유효한지 검사할 때 사용할 쿼리 지정. 기본 값은 null이다. null은 검사 쿼리가 없음을 의미한다.
setValidationQueryTimeout(int)|검사 쿼리의 최대 실행 시간을 초 단위로 지정. 시간 초과시 실패로 간주한다. 음수일 경우 비활성화 된다. 기본 값은 -1 이다.
setTestOnBorrow(boolean)|풀에서 커넥션을 가져올 때 검사여부 지정. 기본 값은 false
setTestOnReturn(boolean)|풀에 커넥션을 반환할 때 검사 여부 지정. 기본 값은 false
setTestWhileIdle(boolean)|커넥션이 풀에 유휴 상태로 있는 동안 검사할지 여부 지정. 기본 값은 false
setMinEvictableIdleTimeMillis(int)|커넥션 풀에 유휴 상태로 유지할 최소 시간을 밀리초 단위로 지정. `testWhileIdle`이 true일 경우 유휴 시간이 이 값을 초과한 커넥션을 풀에서 제거한다. 기본 값은 60,000밀리초(60초)다.
setTimeBetweenEvictionRunsMillis(int)|커넥션 풀의 유휴 커넥션을 검사할 주기를 밀리초 단위로 지정. 기본 값은 5,000밀리초(5초)다. 이 값은 1초 이하로 설정되면 안된다.

## JdbcTemplate 생성

스프링을 사용하면 DataSource나 Connection, Statement, ResultSet을 직접 사용하지 않고 JdbcTemplate 만을 사용해 편리하게 쿼리를 실행할 수 있다.

```java
public class MemberDao {

    private final JdbcTemplate jdbcTemplate;

    public MemberDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
```

`Member` 클래스를 Bean으로 등록한다.

```java
@Configuration
public class AppCtx {
    
    @Bean
    public MemberDao memberDao() {
        return new MemberDao(dataSource());
    }
}
```

`@Repository` 에노테이션을 붙히면 Bean으로 등록하는 과정을 생략할 수 있다.

```java
@Repository
public class MemberDao {

    private final JdbcTemplate jdbcTemplate;

    public MemberDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
```

## JdbcTemplate 조회 쿼리 실행

`JdbcTemplate` 클래스는 SELECT 쿼리 실행을 위한 `query()` 메서드를 제공한다. 자주 사용되는 쿼리 메서드는 다음과 같다.

- `List<T> query(String sql, RowMapper<T> rowMapper)`
- `List<T> query(String sql, Object[] args, RowMapper<T> rowMapper)`
- `List<T> query(String sql, RowMapper<T> rowMapper, Object... args)`

`query()` 메서드가 수행되면 `RowMapper`를 통해 `ResultSet`의 결과를 자바 객체로 변환할 수 있다.

동일한 RowMapper 구현을 여러 곳에 사용한다면 아래 코드를 이용하여 코드 중복을 막을 수 있다.

```java
// RowMapper를 구현할 클래스를 작성
public class MemberRowMapper implements RowMapper<Member> {
	Member member = new Member(
				rs.getString("EMAIL"),
				rs.getString("PASSWORD"),
				rs.getString("NAME"),
				rs.getTimestamp("REGDATE").toLocalDateTime());
		member.setId(rs.getLong("ID"));
		return member;
	}
}
---
// MemberRowMapper 객체 생성
List<Member> reulsts = jdbcTemplate.query(
	"select * from MEMBER where EMAIL = ? and NAME = ?",
	new MemberRowMapper(),
	email, name);
```

람다식을 사용하여 간결하게 할 수도 있다.

```java
(ResultSet rs, int rowNum) -> {
	Member member = new Member(
			rs.getString("EMAIL"),
			rs.getString("PASSWORD"),
			rs.getString("NAME"),
			rs.getTimestamp("REGDATE").toLocalDateTime());
	member.setId(rs.getLong("ID"));
	return member;
```

결과가 1행인 경우 `queryForObject()` 메서드를 사용할 수 있다.

```java
public int count() {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from MEMBER", Integer.class);
		return count;
	}
```

`queryForObject()` 메서드를 사용하려면 쿼리 실행 결과는 반드시 한 행이어야 한다. 만약 쿼리 실행 결과 행이 없거나 두 개 이상이면 `IncorrectResultSizeDataAccessException` 이 발생하고, 행의 개수가 0이면 하위 클래스인 `EmptyResultDataAccessException`이 발생한다. 따라서 결과 행이 정확히 1행이 아니라면 `queryForObject()` 메서드 대신 `query()` 메서드를 사용해야 한다.

## JdbcTemplate 변경 쿼리 실행

INSERT, UPDATE, DELETE 쿼리는 `update()` 메서드를 사용한다.

- `int update(String sql)`
- `int update(String sql, Object... args)`

`update()` 메서드는 쿼리 실행 결과로 변경된 행의 개수를 리턴한다.

```java
public void update(Member member) {
		jdbcTemplate.update(
				"update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
				member.getName(), member.getPassword(), member.getEmail());
	}
```

`PreparedStatement`의 set 메서드를 사용하여 직접 인덱스 파라미터의 값을 설정할 수 있다. 

```java
jdbcTemplate.update(new PreparedStatementCreater() {
	@Override
	public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
		// 파라미터로 전달받은 Connection을 이용하여 PreparedStatement 생성
		PreparedStatement pstmt = con.prepareStatement(
			"insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) values (?, ?, ?, ?)");
		// 인덱스 파라미터의 값 설정
		pstmt.setString(1, member.getEmail());
		pstmt.setString(2, member.getPassword());
		pstmt.setString(3, member.getName());
		pstmt.setTimestamp(4, Timestamp.valueOf(member.getRegisterDateTime()));
		// 생성한 PreparedStatement 객체 리턴
		return pstmt;
	}
});
```

INSERT 쿼리 실행 시 `KeyHoler` 를 이용하여 자동 생성 키 값을 구할 수 있다. 

MySQL의 `AUTO_INCREMENT` 칼럼은 행이 추가되면 자동으로 값이 할당되는 칼럼이다. 

```java
public void insert(Member member) {
	// 자동 생성된 키 값을 구해주는 KeyHolder 구현 클래스
	KeyHolder keyHolder = new GeneratedKeyHolder();
	jdbcTemplate.update(new PreparedStatementCreator() {
		@Override
		public PreparedStatement createPreparedStatement(Connection con)
				throws SQLException {
			PreparedStatement pstmt = con.prepareStatement(
					"insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) " +
					"values (?, ?, ?, ?)",
					new String[] { "ID" });
			pstmt.setString(1, member.getEmail());
			pstmt.setString(2, member.getPassword());
			pstmt.setString(3, member.getName());
			pstmt.setTimestamp(4,
					Timestamp.valueOf(member.getRegisterDateTime()));
			return pstmt;
		}
	}, keyHolder);
	Number keyValue = keyHolder.getKey();
	member.setId(keyValue.longValue());
}
```

`JdbcTemplate`의 `update()` 메서드는 `PreparedStatement`를 실행한 후 자동 생성된 키 값을 `KeyHolder` 에 보관한다. `intValue()`, `longValue()` 등의 메서드를 사용하여 원하는 타입의 값으로 바꿀 수 있다.

람다식을 사용하여 코드 간결

```java
jdbcTemplate.update((Connection con) -> {
		PreparedStatement pstmt = con.prepareStatement(
				"insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) " +
				"values (?, ?, ?, ?)",
				new String[] { "ID" });
		pstmt.setString(1, member.getEmail());
		pstmt.setString(2, member.getPassword());
		pstmt.setString(3, member.getName());
		pstmt.setTimestamp(4,
				Timestamp.valueOf(member.getRegisterDateTime()));
		return pstmt;
}, keyHolder);
```

## 스프링의 익셉션 변환 처리

`DataAccessException`은 스프링이 제공하는 익셉션 타입으로 데이터 연결에 문제가 있을 때 스프링 모듈이 발생시킨다. 스프링은 왜 `SQLException`을 그대로 전파하지 않고 `DataAccessException`으로 변환하는 이유가 무엇일까?

주된 이유는 연동 기술에 상관 없이 동일하게 익셉션을 처리할 수 있도록 하기 위함이다.

```java
// JDBC 연동 코드 익셉션
try {
    ...
} catch (SQLException ex) {
    ...
}

// Hibernate 연동 코드 익셉션
try {
    ...
} catch (HibernateException ex) {
    ...
}

// JPA 연동 코드 익셉션
try {
    ...
} catch (PersistenceException ex) {
    ...
}
```

스프링은 JDBC 뿐만 아니라 JPA, 하이버네이트 등 다양한 연동 기능을 지원하고 있다. 그런데 각각의 구현 기술마다 익셉션을 다르게 처리해야한다면 유지보수가 어려울 것이다. 스프링이 이 단점을 해결하기 위해 DataAccessException으로 자동 변환을 진행함으로써 구현 기술에 상관없이 동일한 코드로 익셉션을 처리할 수 있게 된다.

```java
// DB 연동 코드
try {
    ...
} catch (DataAccessException ex) {
    ...
}
```
`DataAccessException`는 `RuntimeException`에 속하므로, 예외처리가 필요한 경우에만 익셉션을 처리해주면 된다.

## 트랜잭션 처리

데이터베이스는 기본적으로 ACID(원자성, 일관성, 독립성, 지속성)이 지켜져야만 한다. ACID를 지키기 위해 데이터베이스 변경작업을 하나의 단위(트랜잭션)으로 구분하고, 해당 트랜잭션 내에 묶인 쿼리 중 하나라도 반영에 실패할 경우 작업 전체의 실패로 간주하고 트랜잭션 전체를 되돌려야 한다.

트랜잭션은 두 개 이상의 쿼리를 한 작업으로 실행해야 할 때 사용하는 것이다. 

되돌리는 행위를 롤백(rollback), 전체(트랜잭션) 반영 성공시 DB에 실제로 반영하는 행위를 커밋(commit)이라고 부른다.

JDBC를 이용한 코드에서도 Connection의 메서드들을 통해 트랜잭션을 관리해주어야 한다.

```java
try {
    Connection conn = DriverManager.getConnection(jdbcUrl, user, pw);
    conn.setAutoCommit(false);  // 트랜잭션 범위 시작 지점
    ...쿼리 실행
    conn.commit();  // 트랜잭션 범위 종료 지점 및 커밋
} catch(DataAccessException ex) {
    if (conn != null) {
        try {
            conn.rollback(); // 트랜잭션 작업들 중 에러 발생시 롤백
        } catch (DataAccessException ex) {
        }
    }
} finally {
    if (conn != null) {
        try {
            conn.close();
        } catch (DataAccessException ex) {
        }
    }
}
```
스프링이 제공하는 `@Transactional` 어노테이션을 사용하면 매우 간단하게 트랜잭션을 관리해줄 수 있다.

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
`@Transactional` 에노테이션이 붙은 `changePassword()`메서드를 동일한 트랜잭션 범위에서 실행한다. 따라서 `memberDao.selectByEmail()`에서 실행하는 쿼리와 `member.changePassword()`에서 실행하는 쿼리는 한 트랙잭션에 묶인다.

`@Transactional` 에노테이션이 제대로 동작하려면 다음의 두 가지 내용을 스프링 설정에 추가해야 한다.

- `PlatformTransactionManager` Bean 설정
- `@Transactional` 어노테이션 활성화

```java
@Configuration
@EnableTransactionManagement
public class AppCtx {

    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
        ... 생략
        return ds;
    }

    @Bean
    public PlatformTransactionManager transactionManaber() {
        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(dataSource());
        return tm;
    }
}
```

`PlatformTransactionManager`는 스프링이 제공하는 트랜잭션 매니저 인터페이스다. 위 코드에서 살펴볼 수 있듯 `setDataSource()` 메서드를 통해 트랜잭션 연동에 사용할 DataSource를 지정한다.

`@EnableTransactionManagement` 어노테이션은 `@Transactional` 어노테이션이 붙은 메서드를 트랜잭션 범위에서 실행하는 기능을 활성화 시킨다.

트랜잭션이 시작되고 커밋되었는지 확인하는 방법은 스프링이 출력하는 로그 메시지를 보는 것이다. 로그 메시지를 출력하기 위해 `logback`를 사용하면 된다. pom.xml파일에 `logback` 모듈을 추가하면 된다.

```xml
<dependency>
	<groupId>org.slf4j</groupId>
	<artifactId>slf4j-api</artifactId>
	<version>1.7.32</version>
</dependency>

<dependency>
	<groupId>ch.qos.logback</groupId>
	<artifactId>logback-classic</artifactId>
	<version>1.2.3</version>
</dependency>
```

`slf4j`는 로그를 남기기 위한 공통 인터페이스이고, `logback`은 slf4j의 구현체이자 스프링 부트의 기본 로그 객체이다.

## @Transactional 과 프록시

트랜잭션도 AOP를 활용하는 공통 기능 중 하나이다. 스프링은 `@Transactional` 애노테이션을 이용해서 트랜잭션을 처리 하기 위해 내부적으로 AOP를 사용한다. 스프링에서 AOP는 프록시를 통해 구현된다는 것을 기억한다면 트랜잭션 처리도 프록시를 통해서 이루어진다는 것을 유추할 수 있다. 

`@EnableTransactionManagement` 애노테이션을 사용하면 스프링은 `@Transactional` 애노테이션이 적용된 Bean 객체를 찾아서 알맞은 프록시 객체를 생성한다. 

`@Transactional` 적용 메서드는 `RuntimeException`이 발생하면 롤백을 시도한다. 앞서 `JdbcTemplate`이 DB 연동 과정에 문제가 있으면 `DataAccessException`을 발생시킨다고 했다. `DataAccessException` 도 `RuntimeException` 중 하나이므로, `JdbcTemplate`의 기능을 실행하는 도중에도 `DataAccessException`이 발생하면 트랜잭션 프록시 객체가 롤백을 시도한다.

`SQLException`은 `RuntimeException`이 아니므로 롤백을 수행하지 않는다. 별도로 수행하도록 설정하고 싶을 경우 `@Transactional` 애노테이션에 옵션을 부여하면 된다.

```java
@Transactional(rollbackFor = SQLException.class)
public void someMethod() {
	...
}
```
`rollbackFor`와 반대 설정인 것은 `noRollbackFor` 속성이다. 이 속성은 지정한 익셉션이 발생해도 롤백시키지 않고 커밋할 익셉션 타입을 지정할 때 사용한다.

```java
@Transactional(noRollbackFor = DataAccessException.class)
public void someMethod() {
	...
}
```

## @Transactional의 주요 속성

속성|타입|설명
-|-|-
value|String|트랜잭션을 관리할 때 사용할 `PlatformTranscationManager` 빈의 이름을 저장한다. 기본값은 " "이다.
propagation|Propagation|트랜잭션 전파 타입을 지정한다. 기본값은 `Propagation.REQUIRED`이다.
isolation|Isolation|트랜잭션 격리 레벨을 지정한다. 기본값은 `Isolation.DEFAULT`이다.
timeout|int|트랝개션 제한 시간을 지정한다. 기본값은 -1로 이 경우 데이터베이스의 타임아웃 시간을 사용한다. 초 단위로 지정

`@Transactional` 애노테이션의 value 속성값이 없으면 등록된 Bean 중에서 타입이 `PlatformTranscationManager`인 Bean을 사용한다. 

### Propagation 열거 타입의 주요 값

값|설명
-|-
REQUIRED|메서드를 수행하는 데 트랜잭션이 필요하다는 것을 의미한다. 현재 진행중인 트랜잭션이 존재하면 해당 트랜잭션을 사용. 존재하지 않으면 새로운 트랜잭션 생성
MANDATORY|메서드를 수행 하는 데 트랜잭션 필요. REQUIRED와 달리 진행 중인 트랜잭션 존재하지 않을 경우 익셉션 발생
REQUIRES_NEW|항상 새로운 트랜잭션 시작. 진행 중인 트랜잭션 존재할 경우, 기존 트랜잭션을 일시 중지하고 새로운 트랜잭션 시작. 새로 시작된 트랜잭션이 종료된 뒤에 기존 트랜잭션이 계속된다.
SUPPORTS|메서드가 트랜잭션을 필요로 하지는 않지만, 진행 중인 트랜잭션이 존재하면 트랜잭션을 사용한다는 것을 의미. 진행 중인 트랜잭션이 존재하지 않더라도 메서드는 정상 동작함
NOT_SUPPORTED|메서드가 트랜잭션을 필요로 하지 않음을 의미. SUPPORTS와 달리 진행 중인 트랜잭션이 존재할 경우 메서드가 실행되는 동안 트랜잭션은 일시 중지되고 메서드 실행디 종료된 후에 트랜잭션을 계속 진행
NEVER|메서드가 트랜잭션을 필요로 하지 않는다. 만약 진행 중인 트랜잭션이 존재하면 익셉션 발생
NESTED|진행 중인 트랜잭션이 존재하면 기존 트랜잭션에 중첩된 트랜잭션에서 메서드를 실행. 진행 중인 트랜잭션이 존재하지 않으면 REQUIRED와 동일하게 동작한다.

### Isolation 열거 타입에 정의된 값

값|설명
-|-
DEFAULT|기본 설정 사용
READ_UNCOMMITTED|다른 트랜잭션이 커밋하지 않은 데이터를 읽을 수 있다.
READ_COMMITTED|다른 트랜잭션이 커밋한 데이터를 읽을 수 있다.
REPEATABLE_READ|처음에 읽어 온 데이터와 두 번째 읽어 온 데이터가 동일한 값을 갖는다.
SERIALIZABLE|동일한 데이터에 대해서 동시에 두 개 이상의 트랜잭션을 수행할 수 없다.

## 트랜잭션 전파
```java
public class AppleService {
    private BananaService bananaService;

    @Transactional
    public void applePie() {
        bananaService.bananaIceCream();
    }
}
```
```java
public class BananaService {

    @Transactional
    public void bananaIceCream() {
        ...
    }
}
```

위 코드들을 살펴보면 `AppleService.applePie()`와 `BananaService.bananaIceCream()` 메서드 모두 `@Transactional` 어노테이션을 가지고 있다. 그러나 `AppleService.applePie()`가 호출되면 `BananaService.bananaIceCream()`도 함께 호출되게 된다. 이럴 경우 트랜잭션이 어떻게 생성될까?

`@Transactional` 어노테이션의 propagation 속성 기본 값은 REQUIRED 이다. 현재 진행중인 트랜잭션이 존재하면 해당 트랜잭션을 사용하고, 존재하지 않으면 그 때 새로운 트랜잭션을 생성한다는 이야기다.

즉, `AppleService.applePie()` 쪽에서 만들어진 트랜잭션을 기준으로 삼고 작업을 진행한다.

만약 트랜잭션 전파를 무시하고 매번 새로운 트랜잭션 기준점을 생성하고 싶다면 propagation 속성 값을 REQUIRES_NEW 로 설정하면 된다.