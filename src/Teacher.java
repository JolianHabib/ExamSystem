
public class Teacher {
    private String tid;
    private String tname;
    private String phoneNumber;
    private String employmentDate;

    public Teacher(String tid, String tname, String phoneNumber, String employmentDate) {
        this.tid = tid;
        this.tname = tname;
        this.phoneNumber = phoneNumber;
        this.employmentDate = employmentDate;
    }

    public String getTid() {
        return tid;
    }

    public String getTname() {
        return tname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmploymentDate() {
        return employmentDate;
    }

    @Override
    public String toString() {
        return "TID: " + tid + ", Name: " + tname + ", Phone: " + phoneNumber + ", Date: " + employmentDate;
    }
}