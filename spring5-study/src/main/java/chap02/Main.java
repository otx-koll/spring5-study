package chap02;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
	public static void main(String[] args) {
		// 자바 설정에서 정보를 읽어와 빈 객체를 생성하고 관리
		// 앞서 작성한 AppContext 클래스를 생성자 파라미터로 전달
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppContext.class);
		// getBean() : 빈 객체 검색
		// greeter()메서드가 생성한 greeter 객체를 리턴
		Greeter g = ctx.getBean("greeter", Greeter.class);
		String msg = g.greet("스프링");
		System.out.println(msg);
		ctx.close();
	}
}
