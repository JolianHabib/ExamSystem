
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ExamManager {
	
	public void addQuestionToExam(Connection conn) {
	    Scanner sc = new Scanner(System.in);
	    System.out.print("Enter Exam ID (EID): ");
	    int eid = sc.nextInt();
	    System.out.print("Enter Question ID (QID): ");
	    int qid = sc.nextInt();
	    String sql = "INSERT INTO ExamQuestion (EID, QID) VALUES (?, ?)";
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, eid);
	        stmt.setInt(2, qid);
	        stmt.executeUpdate();
	        System.out.println("Question added to exam.");
	    } catch (SQLException e) {
	        System.out.println("Failed to add question: " + e.getMessage());
	    }
	}
	
	public void deleteExam(Connection conn) {
	    Scanner sc = new Scanner(System.in);
	    System.out.print("Enter Exam ID (EID) to delete: ");
	    int eid = sc.nextInt();

	    String sql = "DELETE FROM Exam WHERE EID = ?";
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, eid);
	        int rows = stmt.executeUpdate();
	        if (rows > 0)
	            System.out.println("Exam deleted.");
	        else
	            System.out.println("Exam not found.");
	    } catch (SQLException e) {
	        System.out.println("Failed to delete exam: " + e.getMessage());
	    }
	}

	public void removeQuestionFromExam(Connection conn) {
	    Scanner sc = new Scanner(System.in);
	    System.out.print("Enter Exam ID (EID): ");
	    int eid = sc.nextInt();
	    System.out.print("Enter Question ID (QID) to remove: ");
	    int qid = sc.nextInt();

	    String sql = "DELETE FROM ExamQuestion WHERE EID = ? AND QID = ?";
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, eid);
	        stmt.setInt(2, qid);
	        int rows = stmt.executeUpdate();
	        if (rows > 0)
	            System.out.println("Question removed from exam.");
	        else
	            System.out.println("Question not linked to exam.");
	    } catch (SQLException e) {
	        System.out.println("Failed to remove question: " + e.getMessage());
	    }
	}

	public void createExam(Connection conn, String teacherId, int subjectId) {
	    Scanner sc = new Scanner(System.in);
	    String insertExamSQL = "INSERT INTO Exam (SID, TID) VALUES (?, ?) RETURNING EID, CreatDate";
	    int eid = -1;

	    try (PreparedStatement stmt = conn.prepareStatement(insertExamSQL)) {
	        stmt.setInt(1, subjectId);
	        stmt.setString(2, teacherId);
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            eid = rs.getInt("EID");
	            String date = rs.getString("CreatDate");
	            System.out.println("Exam created. EID: " + eid + " - Date: " + date);
	        }
	    } catch (SQLException e) {
	        System.out.println("Failed to create exam: " + e.getMessage());
	        return;
	    }

	    List<Integer> availableQIDs = new ArrayList<>();
	    String qSQL = "SELECT QID, QuestionText FROM Question WHERE SID = ?";
	    try (PreparedStatement stmt = conn.prepareStatement(qSQL)) {
	        stmt.setInt(1, subjectId);
	        ResultSet rs = stmt.executeQuery();

	        System.out.println("\nAvailable Questions:");
	        while (rs.next()) {
	            int qid = rs.getInt("QID");
	            String text = rs.getString("QuestionText");
	            availableQIDs.add(qid);
	            System.out.println("[" + qid + "] " + text);
	        }
	    } catch (SQLException e) {
	        System.out.println("Failed to load questions: " + e.getMessage());
	        return;
	    }

	    while (true) {
	        System.out.print("\nEnter QID to add to exam: ");
	        int qid = sc.nextInt();
	        sc.nextLine();

	        if (!availableQIDs.contains(qid)) {
	            System.out.println("Invalid QID for this subject.");
	            continue;
	        }

	        try (PreparedStatement insert = conn.prepareStatement(
	                "INSERT INTO ExamQuestion (EID, QID) VALUES (?, ?)")) {
	            insert.setInt(1, eid);
	            insert.setInt(2, qid);
	            insert.executeUpdate();
	            System.out.println("Question added.");
	        } catch (SQLException e) {
	            System.out.println("Failed to add question: " + e.getMessage());
	        }

	        System.out.print("Add another question? (yes/no): ");
	        String answer = sc.nextLine().trim().toLowerCase();
	        if (!answer.equals("yes")) break;
	    }
	}

	public  void printExams(Connection conn, String teacherId, int subjectId) {
	    String sql = """
	    		SELECT E.EID, E.CreatDate, T.TName
	    	    FROM Exam E
	    	    JOIN Teacher T ON E.TID = T.TID
	    	    WHERE E.TID = ? AND E.SID = ?
	    	    ORDER BY E.EID
	    		""";
	    
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, teacherId);
	        stmt.setInt(2, subjectId);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            int eid = rs.getInt("EID");
	            String date = rs.getString("CreatDate");
	            String teacherName = rs.getString("TName");

	            System.out.println("\nExam ID: " + eid + " - Date: " + date + " - Created by: " + teacherName);
	            printQuestionsInExam(conn, eid);
	        }

	    } catch (SQLException e) {
	        System.out.println("Failed to print exams: " + e.getMessage());
	    }
	}
	public void printQuestionsInExam(Connection conn, int eid) {
		String sql = """
			    SELECT Q.QID, Q.QuestionText, Q.IsOpenQuestion, A.AnswerText
			    FROM ExamQuestion EQ
			    JOIN Question Q ON EQ.QID = Q.QID
			    LEFT JOIN QuestionAnswer QA ON Q.QID = QA.QID
			    LEFT JOIN Answer A ON QA.AID = A.AID
			    WHERE EQ.EID = ?
			    ORDER BY Q.QID, A.AnswerText
			    """;
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, eid);
	        ResultSet rs = stmt.executeQuery();

	        int lastQid = -1;
	        boolean isOpen = false;

	        while (rs.next()) {
	            int qid = rs.getInt("QID");
	            String qText = rs.getString("QuestionText");
	            boolean currentIsOpen = rs.getBoolean("IsOpenQuestion");
	            String aText = rs.getString("AnswerText");

	            if (qid != lastQid) {
	                System.out.println("\n  Q: " + qText);
	                isOpen = currentIsOpen;
	                lastQid = qid;
	            }

	            if (!isOpen && aText != null) {
	                System.out.println("    - " + aText);
	            }
	        }

	    } catch (SQLException e) {
	        System.out.println("Failed to print exam questions: " + e.getMessage());
	    }
	}
	public void printAllExamsBySubject(Connection conn, int subjectId) {
		String sql = """
			    SELECT E.EID, E.CreatDate, T.TName
			    FROM Exam E
			    JOIN Teacher T ON E.TID = T.TID
			    WHERE E.SID = ?
			    ORDER BY E.EID
			    """;
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, subjectId);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            int eid = rs.getInt("EID");
	            String date = rs.getString("CreatDate");
	            String teacherName = rs.getString("TName");

	            System.out.println("\nExam ID: " + eid + " - Date: " + date + " - Created by: " + teacherName);
	            printQuestionsInExam(conn, eid);
	        }

	    } catch (SQLException e) {
	        System.out.println("Failed to load all exams: " + e.getMessage());
	    }
	}
	public void menu(Connection conn, String teacherId, int subjectId) {
	    Scanner sc = new Scanner(System.in);
	    int choice;

	    do {
	        System.out.println("\n--- Exam Management Menu: ---");
	        System.out.println("1️ - Create new exam");
	        System.out.println("2️ - Add question to existing exam");
	        System.out.println("3️ - Delete an exam");
	        System.out.println("4️ - Show all exams for this teacher + subject");
	        System.out.println("5️ - Remove question from exam");
	        System.out.println("6️ - Show all exams for this subject (all teachers)");
	        System.out.println("0️ - Exit");
	        System.out.print("Choose: ");
	        choice = sc.nextInt();
	        sc.nextLine();

	        switch (choice) {
	            case 1:
	            	createExam(conn, teacherId, subjectId);
	            	break;
	            case 2:
	            	addQuestionToExam(conn);
	            	break;
	            case 3:
	            	deleteExam(conn);
	            	break;
	            case 4:
	            	printExams(conn, teacherId, subjectId);
	            	break;
	            case 5:
	            	removeQuestionFromExam(conn);
	            	break;
	            case 6:
	            	printAllExamsBySubject(conn, subjectId);
	            	break;
	            case 0:
	            	System.out.println("Exit.");
	            	break;
	            default:
	            	System.out.println("Invalid option.");
	            	break;
	        }

	    } while (choice != 0);
	}

}
