
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SubjectPool {

    private List<Subject> subjects = new ArrayList<>();

    public List<Subject> getSubjects() {
        return subjects;
    }

    public Subject getById(int sid) {
        for (Subject s : subjects) {
            if (s.getSid() == sid)
                return s;
        }
        return null;
    }

    public void loadSubjectsFromDB(Connection conn) {
        subjects.clear();
        String sql = "SELECT * FROM Subject";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int sid = rs.getInt("SID");
                String sname = rs.getString("SName");
                subjects.add(new Subject(sid, sname));
            }

        } catch (SQLException e) {
            System.out.println("Error loading subjects: " + e.getMessage());
        }
    }

    public void insertSubject(Connection conn, String name) {
        String sql = "INSERT INTO Subject (SName) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
            System.out.println("Subject added: " + name);
        } catch (SQLException e) {
            System.out.println("Error adding subject: " + e.getMessage());
        }
    }

    public void subjectMenu(Connection conn) {
        loadSubjectsFromDB(conn);
        Scanner sc = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n--- Subject Menu: ---");
            System.out.println("1 - Add Subject");
            System.out.println("2 - Show All Subjects");
            System.out.println("0 - Exit");
            System.out.print("Choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter subject name: ");
                    String name = sc.nextLine();
                    insertSubject(conn, name);
                    loadSubjectsFromDB(conn);
                    break;
                case 2:
                    for (Subject s : subjects)
                        System.out.println(s);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }
    public QuestionPool getQuestionPool(int index) {
        return this.subjects.get(index - 1).getQuestionPool();
    }
    public Subject getBySID(int sid) {
        for (Subject s : subjects) {
            if (s != null && s.getSid() == sid)
                return s;
        }
        return null;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Subject s : subjects) {
            if (s != null)
                sb.append(s.getSid()).append(") ").append(s.getName()).append("\n");
        }
        return sb.toString();
    }

    public AnswerPool getAnswerPool(int index) {
        return this.subjects.get(index - 1).getAnswerPool();
    }

}