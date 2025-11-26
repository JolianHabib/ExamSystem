
import java.sql.*;
import java.util.*;

public class AnswerPool {
    private Answer[] answers;

    public AnswerPool() {
        this.answers = new Answer[0];
    }

    public Answer[] getAnswers() {
        return this.answers;
    }

    public Answer getAnswer(int index) {
        return this.answers[index - 1];
    }

    public Boolean hasAnswers() {
        return this.answers.length > 0;
    }

    public String toString() {
		StringBuffer str = new StringBuffer();
        for (Answer a : this.answers) {
            if (a != null && !a.getContent().isEmpty()) {
                str.append("Answer ").append(a.getAnswerId()).append(": ").append(a.getContent()).append("\n");
            }
        }
        return str.toString();
    }

    public int addAnswerToDB(Connection conn, String content, boolean correct, int subjectID) {
        int generatedId = -1;
        String sql = "INSERT INTO Answer (AnswerText, Correct, SID) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, new String[] { "aid" })) {
            stmt.setString(1, content);
            stmt.setBoolean(2, correct);
            stmt.setInt(3, subjectID);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserting answer failed.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                } else 
                	throw new SQLException("No ID obtained.");
            }
        } catch (SQLException e) {
            System.out.println("Error inserting answer: " + e.getMessage());
        }

        if (generatedId != -1) {
            Answer newAnswer = new Answer(generatedId, content, correct, subjectID);
            this.answers = Arrays.copyOf(this.answers, this.answers.length + 1);
            this.answers[this.answers.length - 1] = newAnswer;
        }
        return generatedId;
    }


    public void loadAnswersFromDB(Connection conn) {
        String sql = "SELECT AID, AnswerText, Correct, SID FROM Answer";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Answer> list = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("AID");
                String text = rs.getString("AnswerText");
                boolean correct = rs.getBoolean("Correct");
                int sid = rs.getInt("SID");
                list.add(new Answer(id, text, correct, sid));
            }
            this.answers = list.toArray(new Answer[0]);
        } catch (SQLException e) {
            System.out.println("Error loading answers: " + e.getMessage());
        }
    }

    public boolean deleteAnswerFromDB(Connection conn, int answerId) {
        String sql = "DELETE FROM Answer WHERE AID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, answerId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                loadAnswersFromDB(conn);
                return true;
            } else {
                System.out.println("No answer found with AID: " + answerId);
            }
        } catch (SQLException e) {
            System.out.println("Error deleting answer: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteAnswerFromUser(Connection conn) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter answer ID to delete: ");
        int answerId = sc.nextInt();
        return deleteAnswerFromDB(conn, answerId);
    }

    public boolean updateAnswerInDB(Connection conn, int answerId, String newContent, boolean newCorrect, int newSubjectID) {
        String sql = "UPDATE Answer SET AnswerText = ?, Correct = ?, SID = ? WHERE AID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newContent);
            stmt.setBoolean(2, newCorrect);
            stmt.setInt(3, newSubjectID);
            stmt.setInt(4, answerId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                for (Answer a : this.answers) {
                    if (a.getAnswerId() == answerId) {
                        a.setContent(newContent);
                        a.setCorrect(newCorrect);
                        a.setSubjectID(newSubjectID);
                        break;
                    }
                }
                return true;
            } else {
                System.out.println("No answer found with AID: " + answerId);
            }
        } catch (SQLException e) {
            System.out.println("Error updating answer: " + e.getMessage());
        }
        return false;
    }

    public void addAnswerLocal(Answer answer) {
        this.answers = Arrays.copyOf(this.answers, this.answers.length + 1);
        this.answers[this.answers.length - 1] = answer;
    }

    public Answer chooseAnswerFromUser(int subjectID) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Available Answers (only from Subject ID = " + subjectID + "):");
        int count = 0;
        for (int i = 0; i < answers.length; i++) {
            if (answers[i] != null && answers[i].getSubjectID() == subjectID) {
                System.out.println((i + 1) + ") " + answers[i].getContent() + " (ID = " + answers[i].getAnswerId() + ")");
                count++;
            }
        }

        if (count == 0) {
            System.out.println("No answers found for this subject.");
            return null;
        }

        System.out.print("Choose answer number: ");
        int choice = sc.nextInt();
        sc.nextLine();
        if (answers[choice - 1].getSubjectID() != subjectID) {
            System.out.println("Selected answer does not match subject!");
            return null;
        }

        return answers[choice - 1];
    }

    public Answer getById(int aid) {
        for (Answer a : this.answers) {
            if (a != null && a.getAnswerId() == aid) {
                return a;
            }
        }
        return null;
    }

    public boolean updateAnswerInDBFromUser(Connection conn) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter answer ID to update: ");
        int answerId = sc.nextInt();
        sc.nextLine();
        Answer existingAnswer = getById(answerId);
        if (existingAnswer == null) {
            System.out.println("Answer with ID " + answerId + " not found.");
            return false;
        }

        System.out.print("Enter new content (leave empty to keep \"" + existingAnswer.getContent() + "\"): ");
        String newContent = sc.nextLine();
        if (newContent.trim().isEmpty()) {
            newContent = existingAnswer.getContent();
        }

        System.out.print("Enter new subject ID (leave empty to keep " + existingAnswer.getSubjectID() + "): ");
        String sidStr = sc.nextLine();
        int newSubjectID = existingAnswer.getSubjectID();
        if (!sidStr.trim().isEmpty()) {
            try {
                newSubjectID = Integer.parseInt(sidStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid subject ID. Keeping existing.");
            }
        }

        return updateAnswerInDB(conn, answerId, newContent, false, newSubjectID);
    }
    
    public void loadAnswersFromDB(Connection conn, int subjectID) {
        String sql = "SELECT AID, AnswerText, Correct, SID FROM Answer WHERE SID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, subjectID);
            ResultSet rs = stmt.executeQuery();
            List<Answer> list = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("AID");
                String text = rs.getString("AnswerText");
                boolean correct = rs.getBoolean("Correct");
                int sid = rs.getInt("SID");
                list.add(new Answer(id, text, correct, sid));
            }
            this.answers = list.toArray(new Answer[0]);

        } catch (SQLException e) {
            System.out.println("Error loading answers for subject " + subjectID + ": " + e.getMessage());
        }
    }

    public void answerMenu(Connection conn, int subjectID) {
        Scanner sc = new Scanner(System.in);
        boolean exit = false;
        loadAnswersFromDB(conn, subjectID);
        while (!exit) {
            System.out.println("\n--- Answer Menu for Subject " + subjectID + " ---");
            System.out.println("1 - Add new answer");
            System.out.println("2 - Update answer");
            System.out.println("3 - Delete answer");
            System.out.println("4 - Show all answers");
            System.out.println("0 - Exit");
            System.out.print("Choose option: ");
            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {
                case 1: {
                    System.out.print("Enter answer content: ");
                    String content = sc.nextLine();
                    System.out.print("Is it correct? (true/false): ");
                    boolean correct = sc.nextBoolean();
                    sc.nextLine();
                    int newId = addAnswerToDB(conn, content, correct, subjectID);
                    if (newId != -1) {
                        System.out.println("Answer added with ID: " + newId);
                    } else {
                        System.out.println("Failed to add answer.");
                    }
                    break;
                }
                
                case 2:{
                    System.out.print("Enter answer ID to update: ");
                    int aid = sc.nextInt();
                    sc.nextLine();
                    Answer answer = getById(aid);
                    if (answer == null || answer.getSubjectID() != subjectID) {
                        System.out.println("This answer does not belong to the current subject.");
                        break;
                    }
                    
                    System.out.print("Enter new content (leave empty to keep \"" + answer.getContent() + "\"): ");
                    String content = sc.nextLine();
                    if (content.isEmpty()) content = answer.getContent();

                    System.out.print("Is it correct? (true/false): ");
                    boolean correct = sc.nextBoolean();
                    sc.nextLine();

                    boolean updated = updateAnswerInDB(conn, aid, content, correct, subjectID);
                    System.out.println(updated ? "Answer updated." : "Update failed.");
                    break;
                }
                
                case 3: {
                    System.out.print("Enter answer ID to delete: ");
                    int aid = sc.nextInt();
                    sc.nextLine();
                    Answer answer = getById(aid);
                    if (answer == null || answer.getSubjectID() != subjectID) {
                        System.out.println("This answer does not belong to the current subject.");
                        break;
                    }
                    boolean deleted = deleteAnswerFromDB(conn, aid);
                    System.out.println(deleted ? "Answer deleted." : "Delete failed.");
                    break;
                }
                case 4 : {
                    System.out.println(this);
                    break;
                }
                case 0: {
                    exit = true;
                    break;
                }
                default: {
                    System.out.println("Invalid option.");
                    break;
                }
            }
        }
    }
}
