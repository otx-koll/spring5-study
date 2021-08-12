## Framework

- 애플리케이션 개발에 바탕이 되는 템플릿과 같은 역할을 하는 클래스들과 인터페이스의 집합

## Spring Framework

- 자바 플랫폼을 위한 오픈소스 애플리케이션 프레임워크
- 경량 컨테이너로 자바 객체를 담고 직접 관리한다. 이는 Spring이 `IOC` 기반의 Framework임을 의미
- 주요 특징
    - 의존 주입(Dependency Injection : DI)
    - AOP(Aspect-Oriented Programming) 지원
    - MVC 웹 프레임워크 제공
    - JDBC, JPA 연동, 선언적 트랜잭션 처리 등 DB 연동 지원

### IOC(Inversion of Control)

- 제어의 역전
- 기존 사용자가 모든 작업을 제어하던 것을 특별한 객체에 모든 것을 위임하여 객체의 생성부터 생명주기 등 모든 객체에 대한 제어권이 넘어 간 것

### DI와 DL

- DL(Dependency  Lookup) - 의존성 검색
    - 개발자들이 컨테이너에서 제공하는 API 이용하여 사용하고자 하는 빈 검색
- DI(Dependency Injection) - 의존성 주입
    - 각 클래스 사이에 필요로 하는 의존관계를 빈 설정 정보를 바탕으로 컨테이너가 자동으로 연결

### AOP(Aspect Oriented Programming)

- 관점 지향 프로그래밍
- 중복되는 코드를 한꺼번에 제거 가능
- 효율적인 유지보수 및 재활용성 극대화

### MVC (Model2)

- Model View Controller 구조로 사용자와 인터페이스와 비지니스 로직을 분리하여 개발하는 것
1. Model
    - 데이터 처리를 담당하는 부분
    - Service영역과 DAO 영역으로 나뉜다
    - View와 Controller 정보를 갖고있어서는 안된다
2. View
    - 사용자 Interface 담당
    - Controller를 통해 모델에 데이터에 대한 시각화를 담당
    - 요청을 보낼 Controller의 정보만 알고 있어야 한다
3. Controller
    - View에 받은 요청을 가공하여 Model에 전달 후, 결과를 View로 넘겨주는 역할
    - 모든 요청 에러와 모델 에러 처리
---
## Maven
- 자바 프로젝트의 빌드를 자동으로 해주는 도구
- 개발자가 xml에 작성한 프로젝트 정보를 토대로 컴파일하고 라이브러리를 연결하는 등의 작업을 해주는 도구
- Maven 서버를 통해 라이브러리를 다운받아 설정하는 작업도 수행한다
- 스프링의 메이븐 프로젝트에서 pom.xml파일이 설정 정보를 관리한다

### pom.xml

```xml
<!-- xml에서 사용할 속성들 -->
<properties>
    <java-version>1.8</java-version>
    <org.springframework-version>5.3.9</org.springframework-version>
</properties>

<!-- 프로젝트에서 사용할 라이브러리 정보 -->
<dependencies>
<!-- spring-context -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>${org.springframework-version}</version>
    </dependency>
</dependencies>
```

- spring-context : 스프링 프레임워크의 context 정보들을 제공하는 설정 파일

## Annotation(@)

- 자바에서 코드 사이에 주석처럼 쓰이며, 특별한 의미, 기능을 수행하도록 하는 기술
- 컴파일러에게 코드 작성 문법 에러를 체크하도록 정보 제공
- 빌드나 배치시 코드를 자동으로 생성할 수 있도록 정보 제공
- 실행시 특정 기능을 실행하도록 정보 제공
