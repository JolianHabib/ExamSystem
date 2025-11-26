
public class Exam {
    private int eid;
    private int sid;
    private String tid;
    private String creationDate;

    public Exam(int eid, int sid, String tid, String creationDate) {
        this.eid = eid;
        this.sid = sid;
        this.tid = tid;
        this.creationDate = creationDate;
    }

    public int getEid(){
    	return eid;
    }
    public int getSid() {
    	return sid;
    }
    public String getTid() {
    	return tid;
    }
    public String getCreationDate() {
    	return creationDate;
    }

    @Override
    public String toString() {
        return "Exam ID: " + eid + " - Subject ID: " + sid + " - Teacher ID: " + tid + " - Date: " + creationDate;
    }
}
