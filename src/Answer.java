


public class Answer {
	private int answerId;
	private String content;
	private boolean correct;
	private int subjectID;
	
	public Answer(int answerId,String content, boolean correct, int subjectID) {
		this.answerId = answerId;
		this.content = content;
		this.correct = correct;
		this.subjectID = subjectID;
	}
	
	public Answer(String content, boolean correct, int subjectID) {
		this(-1, content, correct, subjectID);
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	public String getContent() {
		return this.content;
	}

	public boolean getCorrect() {
		return this.correct;
	}

	@Override
	public String toString() {
		return (this.answerId+")"+this.content + " [" + this.correct + "]");
	}

	public int getAnswerId() {
		return answerId;
	}

	public void setAnswerId(int answerId) {
		this.answerId = answerId;
	}

	public int getSubjectID() {
		return subjectID;
	}

	public void setSubjectID(int subjectID) {
		this.subjectID = subjectID;
	}

}
