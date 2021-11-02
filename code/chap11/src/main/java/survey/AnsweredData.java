package survey;

import java.util.List;

// 설문 항목에 대한 답변과 응답자 정보를 담는 클래스
public class AnsweredData {
	
	// 답변 목록을 저장하기 위함
	private List<String> responses;
	private Respondent res;
	
	public List<String> getResponses() {
		return responses;
	}
	public void setResponses(List<String> responses) {
		this.responses = responses;
	}
	public Respondent getRes() {
		return res;
	}
	public void setRes(Respondent res) {
		this.res = res;
	}
	
}
