package spring;

public class DuplicateMemberException extends RuntimeException {
	// 동일한 이메일을 갖고 있는 회원이 이미 존재할 때, MemberRegisterService가 발생시키는 익셉션 타입

	public DuplicateMemberException(String message) {
		super(message);
	}
	
}
