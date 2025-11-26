
public class Subject {
    private int sid;
    private String name;
    private QuestionPool questionPool;  
    private AnswerPool answerPool;

    public Subject(int sid, String name) {
        this.sid = sid;
        this.name = name;
        this.questionPool = new QuestionPool();
        this.answerPool = new AnswerPool();
    }

    public int getSid() {
        return sid;
    }

    public String getName() {
        return name;
    }

    public QuestionPool getQuestionPool() {
        return questionPool;
    }

    public AnswerPool getAnswerPool() {
        return answerPool;
    }

    public void setQuestionPool(QuestionPool qp) {
        this.questionPool = qp;
    }

    public void setAnswerPool(AnswerPool ap) {
        this.answerPool = ap;
    }

    public String toString() {
        return sid + ") " + name;
    }
}
