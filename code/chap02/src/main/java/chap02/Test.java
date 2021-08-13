package chap02;

import java.time.LocalDateTime;

public class Test {
	public static void main(String[] args) {
		LocalDateTime currentDateTime = LocalDateTime.now();
		// 결과 : 2021-08-13T22:53:03.351797600
		LocalDateTime targetDateTime = LocalDateTime.of(2021, 3, 20, 22, 33, 44, 5555);
		// 결과 : 2021-03-20T22:33:44.000005555
		
		System.out.println(currentDateTime);
		System.out.println(targetDateTime);
	}
}
