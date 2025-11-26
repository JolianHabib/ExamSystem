

public class SelectionQuestion {
	protected int id;
	protected String content;
	protected Answer[] answers;
	protected String difficulty;
	protected Boolean isValid;
	protected int subjectId;
	private int answerCount = 0;

	public SelectionQuestion(String content, int difficulty, int subjectId) {
		this.isValid = false;
		this.content = content;
		switch (difficulty) {
		case 1: {
			this.difficulty = "Easy";
			break;
		}
		case 2: {
			this.difficulty = "Medium";
			break;
		}
		case 3: {
			this.difficulty = "Hard";
			break;
		}
		}
		this.subjectId=subjectId;
		this.answers = new Answer[12];
		answers[10] = new Answer("More than one answer is true", false,this.subjectId);
		answers[11] = new Answer("All of the answers are false", false,this.subjectId);
	}
	
	public SelectionQuestion(String content, int difficulty) {
		this(content, difficulty, -1);
	}
	
	public SelectionQuestion(String content, String difficulty,int subjectId) {
		this.isValid = false;
		this.content = content;
		this.difficulty = difficulty;
		this.subjectId=subjectId;
		this.answers = new Answer[12];
		answers[10] = new Answer("More than one answer is true", false,this.subjectId);
		answers[11] = new Answer("All of the answers are false", false,this.subjectId);
	}

	public String getContent() {
		return this.content;
	}

	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
	    this.id = id;
	}

	public String getDifficulty() {
		return this.difficulty;
	}

	public int getDifficultyInt() {
		switch (difficulty) {
		case "Easy": {
			return 1;
		}
		case "Medium": {
			return 2;
		}
		case "Hard": {
			return 3;
		}
		}
		return 0;
	}

	public final Answer[] getAnswers() {
		return this.answers;
	}
	
	public Answer getAnswer() {
		return null;
	}
	
	public final Answer getAnswer(int index) {
		return this.answers[index - 1];
	}

	public int getAnswerCount() {
		int count = 0;
		for (int i = 0; i < 10; i++) {
			if ((this.answers[i] != null) && (this.answers[i].getContent() != "")) {
				count++;
			}
		}
		return count;
	}

	public void setAnswers(Answer[] answers) {
		this.answers = answers;
		answers[10] = new Answer("More than one answer is true", multipleTrues(),this.subjectId);
		answers[11] = new Answer("All of the answers are false", allFalse(),this.subjectId);
	}
	
	public void setSubjectId(int subjectId) {
		this.subjectId=subjectId;
	}
	
	public int getSubjectId () {
		return this.subjectId;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setDifficulty(int difficulty) {
		switch (difficulty) {
		case 1: {
			this.difficulty = "Easy";
			break;
		}
		case 2: {
			this.difficulty = "Medium";
			break;
		}
		case 3: {
			this.difficulty = "Hard";
			break;
		}
		}
	}
	
	public void clearAnswers() {
	    this.answers = new Answer[12];
	    this.answerCount = 0;
	}

	public final void addAnwser(Answer answer) {
		for (int i = 0; i < answerCount; i++) {
	        if (this.answers[i] != null && this.answers[i].getAnswerId() == answer.getAnswerId()) {
	            return; 
	        }	
		}
		answers[10].setCorrect(multipleTrues());
		answers[11].setCorrect(allFalse());
		if (answerCount >= 10) {
	        System.out.println("Cannot add more than 10 answers to this question.");
	    }
		 this.answers[answerCount++] = answer;
	}

	public final void deleteAnswer(int index) {
		this.answers[index - 1] = null;
		this.answers = sortArray(this.answers);
	}

	public String toString() {
		this.answers = sortArray(this.answers);
		StringBuffer str = new StringBuffer();
		str.append("Question: " + this.content + "\n");
		str.append("(id: " + this.id + ", difficulty: " + this.difficulty + ")\n");
		for (int i = 0; i < 10; i++) {
			if ((this.answers[i] != null) && (this.answers[i].getContent() != "")) {
				str.append((i + 1) + ")" + this.answers[i].toString() + "\n");
			}
		}
		return str.toString();
	}

	public final Answer[] sortArray(Answer[] answers) {
		for (int i = 0; i < 10; i++) {
			if ((this.answers[i] == null) || (this.answers[i].getContent() == "")) {
				for (int k = i + 1; k < 10; k++) {
					this.answers[k - 1] = this.answers[k];
				}
			}
		}
		this.answers[10].setCorrect(multipleTrues());
		this.answers[11].setCorrect(allFalse());
		return this.answers;
	}

	public final boolean multipleTrues() {
		int counter = 0;
		for (int i = 0; i < 10; i++) {
			if ((this.answers[i] != null) && (this.answers[i].getContent() != "")) {
				if (this.answers[i].getCorrect() == true) {
					counter++;
				}
			}
			if (counter >= 2) {
				return true;
			}
		}
		return false;
	}
	
	public final int getNumOfTrues() {
		int counter = 0;
		for (int i = 0; i < 10; i++) {
			if ((this.answers[i] != null) && (this.answers[i].getContent() != "")) {
				if (this.answers[i].getCorrect() == true) {
					counter++;
				}
			}
		}
		return counter;
	}

	public final boolean allFalse() {
		for (int i = 0; i < 10; i++) {
			if ((this.answers[i] != null) && (this.answers[i].getContent() != "")) {
				if (this.answers[i].getCorrect() == true) {
					return false;
				}
			}
		}
		return true;
	}

	public void setAnswerCount(int answerCount) {
		this.answerCount = answerCount;
	}
	
	public void addAnswer(Answer answer, boolean isCorrect) {
	    if (answerCount >= 10) {
	        System.out.println("Cannot add more than 10 answers to this question.");
	        return;
	    }
	    this.answers[answerCount] = answer;
	    this.answers[answerCount].setCorrect(isCorrect); 
	    answerCount++;
	}

}
