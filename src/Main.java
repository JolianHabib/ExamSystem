import java.sql.*;
import java.util.Scanner;


public class Main {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("DB PROJECT\n");
		TeacherManager TM = new TeacherManager();
        int choice;
        do {
            System.out.println("\nWelcome to Exam System");
            System.out.println("1️ - Subject Manager (Questions, Answers, Linking)");
            System.out.println("2️ - Teacher Manager (Add/Delete/Update/Subjects)");
            System.out.println("0️ - Exit");
            System.out.print("Choose option: ");
            choice = scanner.nextInt();
            scanner.nextLine(); 
            switch (choice) {
                case 1:
                    startSubjectManager();  
                    break;
                case 2:
                    TM.teacherMenu();       
                    break;
                case 0:
                    System.out.println("Bye!");
                    break;
                default:
                    System.out.println("Invalid option.");
            }

        } while (choice != 0);
    }
	
	
	public static void startSubjectManager() {
        try (Connection conn = DBConnection.connect()) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("List of Teachers:");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT TID, TName FROM Teacher");
            while (rs.next()) {
                System.out.println("TID: " + rs.getString("TID") + " - Name: " + rs.getString("TName"));
            }
            System.out.print("\nEnter teacher ID: ");
            String tid = scanner.nextLine();
            PreparedStatement check = conn.prepareStatement("SELECT * FROM Teacher WHERE TID = ?");
            check.setString(1, tid);
            ResultSet checkRs = check.executeQuery();
            if (!checkRs.next()) {
                System.out.println("Teacher not found.");
                return;
            }

            PreparedStatement subjectStmt = conn.prepareStatement(
                "SELECT s.SID, s.SName FROM Subject s " +
                "JOIN TeacherTeachSubject tts ON s.SID = tts.SID WHERE tts.TID = ?");
            subjectStmt.setString(1, tid);
            ResultSet subjectRs = subjectStmt.executeQuery();
            System.out.println("\nSubjects taught by this teacher:");
            while (subjectRs.next()) {
                System.out.println("SID: " + subjectRs.getInt("SID") + " - " + subjectRs.getString("SName"));
            }

            System.out.print("Enter Subject ID to manage: ");
            int sid = scanner.nextInt();
            scanner.nextLine();
            QuestionPool QP = new QuestionPool();
            AnswerPool AP = new AnswerPool();
            QuestionAnswerLinker QAL = new QuestionAnswerLinker();
            ExamManager EM = new ExamManager();

            int choice;
            do {
                System.out.println("\n--- Subject Manager ---");
                System.out.println("1️ - Manage Questions");
                System.out.println("2️ - Manage Answers");
                System.out.println("3️ - Link Answers To Questions");
                System.out.println("4 - Manage Exams");
                System.out.println("0️ - Back");
                System.out.print("Choose: ");
                choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        QP.menuQuestion(conn, sid);
                        break;
                    case 2:
                        AP.answerMenu(conn, sid);
                        break;
                    case 3:
                        QAL.menu(conn, sid);
                        break;
                    case 4:
                    	EM.menu(conn, tid, sid);
                    case 0:
                    	break;
                    default: System.out.println("Invalid option.");
                }

            } while (choice != 0);

        } catch (Exception e) {
            System.out.println("Error in Subject Manager:");
            e.printStackTrace();
        }
    }
    
	
}