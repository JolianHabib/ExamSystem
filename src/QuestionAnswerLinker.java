
import java.sql.*;
import java.util.Scanner;

public class QuestionAnswerLinker {

    public void menu(Connection conn, int subjectId) {
        Scanner sc = new Scanner(System.in);
        loadQuestionAnswerLinksFromDB(conn, subjectId);
        while (true) {
            System.out.println("\n---- Question-Answer ----:");
            System.out.println("1 - Link answer to question");
            System.out.println("2 - Update link");
            System.out.println("3 - Delete link");
            System.out.println("4 - Print all questions + answers");
            System.out.println("0 - Exit");
            System.out.print("Choice: ");
            int ch = sc.nextInt();
            sc.nextLine();
            switch (ch) {
                case 1: 
                    System.out.print("Enter Question ID: ");
                    int qid = sc.nextInt();
                    System.out.print("Enter Answer ID: ");
                    int aid = sc.nextInt();
                    System.out.print("Is this answer correct? (true/false): ");
                    boolean isCorrect = sc.nextBoolean();
                    linkAnswerToQuestion(conn, qid, aid, isCorrect, subjectId);
                    break;
                
                case 2:
                    System.out.print("Enter Question ID: ");
                    qid = sc.nextInt();
                    System.out.print("Enter Answer ID: ");
                    aid = sc.nextInt();
                    System.out.print("New correctness (true/false): ");
                    boolean c = sc.nextBoolean();
                    updateAnswerLink(conn, qid, aid, c);
                    break;
                
                case 3:
                    System.out.print("Enter Question ID: ");
                    qid = sc.nextInt();
                    System.out.print("Enter Answer ID: ");
                    aid = sc.nextInt();
                    deleteAnswerLink(conn, qid, aid);
                    break;
                case 4:
                	printAllQuestionsWithAnswers(conn, subjectId);
                	break;
                case 0:
                    System.out.println("Exit.");
                    return;
                
                default:
                	System.out.println("Invalid.");
                	break;
            }
        }
    }

    public void loadQuestionAnswerLinksFromDB(Connection conn, int subjectId) {
    	String sql = """
    		    SELECT Q.QID, Q.QuestionText, Q.IsOpenQuestion,
    		           QA.AID, A.AnswerText, QA.ISAnswerCorrect
    		    FROM Question Q
    		    LEFT JOIN QuestionAnswer QA ON Q.QID = QA.QID
    		    LEFT JOIN Answer A ON QA.AID = A.AID
    		    WHERE Q.SID = ?
    		    ORDER BY Q.QID
    		    """;
            
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, subjectId);
            ResultSet rs = stmt.executeQuery();
            int currentQID = -1;
            while (rs.next()) {
                int qid = rs.getInt("QID");
                String qText = rs.getString("QuestionText");
                boolean isOpen = rs.getBoolean("IsOpenQuestion");
                if (qid != currentQID) {
                    System.out.println("\nQuestion: " + qText + " (QID: " + qid + ")");
                    currentQID = qid;
                }
                int aid = rs.getInt("AID");
                String aText = rs.getString("AnswerText");
                boolean correct = rs.getBoolean("ISAnswerCorrect");
                
                if (aid > 0) {
                    System.out.println("  Answer (AID: " + aid + "): " + aText + " [Correct: " + correct + "]");
                }
            }

        } catch (SQLException e) {
            System.out.println("Failed to load data: " + e.getMessage());
        }
    }

    public void linkAnswerToQuestion(Connection conn, int qid, int aid, boolean isCorrect, int subjectId) {
        if (!checkSameSubject(conn, qid, aid, subjectId)) {
            System.out.println("Question and Answer do not belong to this subject.");
            return;
        }

        if (isOpenQuestion(conn, qid) && hasAnswerLinkedBefore(conn, qid)) {
            System.out.println("Open question already has an answer.");
            return;
        }

        if (!isOpenQuestion(conn, qid) && getAnswerCount(conn, qid) >= 10) {
            System.out.println("Closed question already has 10 answers.");
            return;
        }

        String sql = "INSERT INTO QuestionAnswer (QID, AID, ISAnswerCorrect) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, qid);
            stmt.setInt(2, aid);
            stmt.setBoolean(3, isCorrect);
            stmt.executeUpdate();
            System.out.println("Link created.");
        } catch (SQLException e) {
            System.out.println("Failed to link: " + e.getMessage());
        }
    }

    public void updateAnswerLink(Connection conn, int qid, int aid, boolean isCorrect) {
        String sql = "UPDATE QuestionAnswer SET ISAnswerCorrect = ? WHERE QID = ? AND AID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isCorrect);
            stmt.setInt(2, qid);
            stmt.setInt(3, aid);
            int updated = stmt.executeUpdate();
            if (updated > 0)
                System.out.println("Link updated.");
            else
                System.out.println("Link not found.");
        } catch (SQLException e) {
            System.out.println("Failed to update link: " + e.getMessage());
        }
    }

    public void deleteAnswerLink(Connection conn, int qid, int aid) {
        String sql = "DELETE FROM QuestionAnswer WHERE QID = ? AND AID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, qid);
            stmt.setInt(2, aid);
            int deleted = stmt.executeUpdate();
            if (deleted > 0)
                System.out.println("Link deleted.");
            else
                System.out.println("Link not found.");
        } catch (SQLException e) {
            System.out.println("Failed to delete link: " + e.getMessage());
        }
    }

    private boolean isOpenQuestion(Connection conn, int qid) {
        String sql = "SELECT IsOpenQuestion FROM Question WHERE QID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, qid);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean hasAnswerLinkedBefore(Connection conn, int qid) {
        String sql = "SELECT COUNT(*) FROM QuestionAnswer WHERE QID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, qid);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private int getAnswerCount(Connection conn, int qid) {
        String sql = "SELECT COUNT(*) FROM QuestionAnswer WHERE QID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, qid);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private boolean checkSameSubject(Connection conn, int qid, int aid, int subjectId) {
    	String sql = """
    		    SELECT Q.SID AS qsid, A.SID AS asid
    		    FROM Question Q, Answer A
    		    WHERE Q.QID = ? AND A.AID = ?
    		    """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, qid);
            stmt.setInt(2, aid);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt("qsid") == subjectId && rs.getInt("asid") == subjectId;
        } catch (SQLException e) {
            return false;
        }
    }
    public void printAllQuestionsWithAnswers(Connection conn, int subjectId) {
    	String sql = """
    		    SELECT Q.QID, Q.QuestionText, Q.IsOpenQuestion,
    		           QA.AID, A.AnswerText, QA.ISAnswerCorrect
    		    FROM Question Q
    		    LEFT JOIN QuestionAnswer QA ON Q.QID = QA.QID
    		    LEFT JOIN Answer A ON QA.AID = A.AID
    		    WHERE Q.SID = ?
    		    ORDER BY Q.QID
    		    """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, subjectId);
            ResultSet rs = stmt.executeQuery();
            int lastQid = -1;
            while (rs.next()) {
                int qid = rs.getInt("QID");
                String qText = rs.getString("QuestionText");
                boolean isOpen = rs.getBoolean("IsOpenQuestion");
                if (qid != lastQid) {
                    System.out.println("\nQuestion ID " + qid + ": " + qText);
                    lastQid = qid;
                }

                int aid = rs.getInt("AID");
                String aText = rs.getString("AnswerText");
                boolean correct = rs.getBoolean("ISAnswerCorrect");

                if (aid != 0 && aText != null) {
                    System.out.println("  Answer ID " + aid + ": " + aText + " [Correct: " + correct + "]");
                }
            }

        } catch (SQLException e) {
            System.out.println("Failed to print questions and answers: " + e.getMessage());
        }
    }

}
