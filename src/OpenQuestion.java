

public class OpenQuestion extends SelectionQuestion {
	private Answer answer;

	public OpenQuestion(String content, Integer difficulty, Answer answer, int subjectId) {
		super(content, difficulty, subjectId);
		this.answer = answer;
	}
	
	public OpenQuestion(String content, Integer difficulty, Answer answer) {
	    this(content, difficulty, answer, -1);  
	}
	
	public OpenQuestion(String content, String difficulty, Answer answer, int subjectId) {
		super(content, difficulty, subjectId);
		this.answer = answer;
	}
	
	public OpenQuestion(String content, String difficulty, int subjectId) {
	    super(content, difficulty, subjectId);
	    this.answer = new Answer("", true, subjectId); 
	}

	@Override
	public Answer getAnswer() {
		return this.answer;
	}

	public void setAnswer(String answer) {
		this.answer.setContent(answer);
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("Question: " + this.content + "\n");
		str.append("(id: " + this.id + ", difficulty: " + this.difficulty + ")\n");
		str.append("Anwser: " + this.answer.getContent() + "\n");
		return str.toString();
	}
}
