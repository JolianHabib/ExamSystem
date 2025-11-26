

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.sql.*;

public class QuestionPool {
	protected SelectionQuestion[] questions;
	
	public QuestionPool() {
		this.questions = new SelectionQuestion[1];
	}
	
	public SelectionQuestion[] getQuestions() {
		return this.questions;
	}
	
	public SelectionQuestion getById(int qid) {
	    for (SelectionQuestion q : this.questions) {
	        if (q != null && q.getId() == qid) {
	            return q;
	        }
	    }
	    return null;
	}

	
	public SelectionQuestion getQuestion(int index) {
		return this.questions[index - 1];
	}
	
	public void setAnswer(String answer, int index) {
		if (this.questions[index - 1] instanceof OpenQuestion) {
		    ((OpenQuestion)this.questions[index - 1]).setAnswer(answer);
		}
	}
	
	public Boolean hasQuestions() {
		if (this.questions.length > 1) {
			return true;
		}
		return false;
	}

	public Boolean hasOpenQuestions() {
		if (this.questions.length > 1) {
			for (int i = 0; i < this.questions.length; i++) {
				if (this.questions[i] instanceof OpenQuestion) {
					return true;
				}
			}
		}
		return false;
	}

	public Boolean hasSelectionQuestions() {
		if (this.questions.length > 1) {
			for (int i = 0; i < this.questions.length; i++) {
				if ((!(this.questions[i] instanceof OpenQuestion))&&(this.questions[i] != null)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < this.questions.length; i++) {
			if (this.questions[i] != null) {
				str.append("Question " + (i + 1) + ": " + this.questions[i].getContent() + "\n");
			}
		}
		return str.toString();
	}

	public String toStringFull() {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < this.questions.length; i++) {
			if (this.questions[i] != null) {
				str.append(this.questions[i].toString() + "\n");
			}
		}
		return str.toString();
	}

	public String toStringSelection() {
	    StringBuilder str = new StringBuilder();
	    for (int i = 0; i < this.questions.length; i++) {
	        if (this.questions[i] != null ) {
	            str.append("Question ").append(i + 1)
	               .append(": ").append(this.questions[i].getContent())
	               .append(" [Level: ").append(this.questions[i].getDifficulty())
	               .append(", ID: ").append(this.questions[i].getId())
	               .append("]\n");
	        }
	    }
	    return str.toString();
	}

	public void addQuestionLocal(SelectionQuestion question) {
	    this.questions = Arrays.copyOf(this.questions, this.questions.length + 1);
	    this.questions[this.questions.length - 1] = question;
	}
	public SelectionQuestion chooseQuestionFromUser() {
	    Scanner sc = new Scanner(System.in);
	    System.out.println("Available Questions:");
	    for (int i = 0; i < questions.length; i++) {
	        if (questions[i] != null) {
	            System.out.println((i + 1) + ") " + questions[i].getContent() + " (ID = " + questions[i].getId() + ")");
	        }
	    }
	    System.out.print("Choose question number: ");
	    int choice = sc.nextInt();
	    sc.nextLine();
	    return questions[choice - 1];
	}

	public String toStringOpen() {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < this.questions.length; i++) {
			if ((this.questions[i] != null) && (this.questions[i] instanceof OpenQuestion == true)) {
				str.append("Question " + (i + 1) + ": " + this.questions[i].getContent() + "\n");
			}
		}
		return str.toString();
	}

	public void loadQuestionsFromDB(Connection conn, int subjectID) {
	    String sql = "SELECT QID, QuestionText, QDefault, SID, IsOpenQuestion FROM Question WHERE SID = ?";
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, subjectID);
	        ResultSet rs = stmt.executeQuery();
	        List<SelectionQuestion> list = new ArrayList<>();
	        while (rs.next()) {
	            int qid = rs.getInt("QID");
	            String text = rs.getString("QuestionText");
	            String level = rs.getString("QDefault");
	            int sid = rs.getInt("SID");
	            boolean isOpen = rs.getBoolean("IsOpenQuestion");
	            SelectionQuestion q;
	            if (isOpen) {
	                q = new OpenQuestion(text, level, sid);
	            } else {
	                q = new SelectionQuestion(text, level, sid);
	            }
	            q.setId(qid);
	            list.add(q);
	        }
	        this.questions = list.toArray(new SelectionQuestion[0]);
	    } catch (SQLException e) {
	        System.out.println("Error loading questions: " + e.getMessage());
	      }
	}

	public int addQuestionToDB(Connection conn, SelectionQuestion question) {
	    int generatedId = -1;
	    String sql = "INSERT INTO Question (QuestionText, QDefault, SID, IsOpenQuestion) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement stmt = conn.prepareStatement(sql, new String[] { "qid" })) {
	        stmt.setString(1, question.getContent());
	        stmt.setString(2, question.getDifficulty());
	        stmt.setInt(3, question.getSubjectId());
	        stmt.setBoolean(4, (question instanceof OpenQuestion));
	        System.out.println("Inserting: " + question.getContent() + " - " + question.getDifficulty() + " - " + question.getSubjectId());
	        int affectedRows = stmt.executeUpdate();
	        if (affectedRows == 0) {
	            throw new SQLException("Inserting question failed. ");
	        }
	        try (ResultSet rs = stmt.getGeneratedKeys()) {
	            if (rs.next()) {
	                generatedId = rs.getInt(1);
	                question.setId(generatedId); 
	                System.out.println("Generated ID: " + generatedId);
	            } else {
	                System.out.println("No ID returned by the DB.");
	            }
	        }
	        
	        this.questions = Arrays.copyOf(this.questions, this.questions.length + 1);
	        this.questions[this.questions.length - 1] = question;
	        
	    } catch (SQLException e) {
	        System.out.println("Error inserting question: " + e.getMessage());
	        e.printStackTrace();
	    }

	    return generatedId;
	}

	public void insertQuestionFromUser(Connection conn, QuestionPool questionPool) {
	    Scanner sc = new Scanner(System.in);
	    System.out.println("Is it an open question? (yes/no)");
	    String type = sc.nextLine().trim().toLowerCase();
	    System.out.print("Enter the question text: ");
	    String content = sc.nextLine();
	    System.out.print("Enter difficulty (1-Easy, 2-Medium, 3-Hard): ");
	    int difficulty = sc.nextInt();
	    sc.nextLine();
	    System.out.print("Enter Subject ID: ");
	    int sid = sc.nextInt();
	    sc.nextLine();
	    int id = -1;
	    if (type.equals("yes")) {
	        System.out.print("Enter the correct answer: ");
	        String answerText = sc.nextLine();
	        OpenQuestion oq = new OpenQuestion(content, difficulty, new Answer(answerText, true, sid), sid);
	        id = questionPool.addQuestionToDB(conn, oq);
	        oq.setId(id);
	        System.out.println("\nOpen Question inserted:");
	        System.out.println(oq.toString());
	    } else {
	        SelectionQuestion sq = new SelectionQuestion(content, difficulty, sid);
	        id = questionPool.addQuestionToDB(conn, sq);
	        sq.setId(id);
	        System.out.println("\nSelection Question inserted:");
	        System.out.println(sq.toString());
	    }

	    if (id == -1) {
	        System.out.println("Question inserted,but ID was not retrieved.");
	    } else {
	        System.out.println("Question added with ID: " + id);
	    }
	}

	public void updateQuestion(Connection conn, QuestionPool questionPool) {
	    Scanner sc = new Scanner(System.in);
	    System.out.print("Enter question ID to update: ");
	    int qid = sc.nextInt();
	    sc.nextLine();
	    SelectionQuestion q = null;
	    for (SelectionQuestion question : questionPool.getQuestions()) {
	        if (question != null && question.getId() == qid) {
	            q = question;
	            break;
	        }
	    }
	    if (q == null) {
	        System.out.println("Question not found.");
	        return;
	    }
	    System.out.print("New content (leave empty to keep): ");
	    String newContent = sc.nextLine();
	    if (newContent.isEmpty()) {
	        newContent = q.getContent();
	    } else {
	        q.setContent(newContent);
	    }

	    System.out.print("New difficulty (1-3, 0 to skip): ");
	    int newDiff = sc.nextInt();
	    sc.nextLine();
	    q.setDifficulty(newDiff);
	    String newDiffStr = q.getDifficulty();
	    String sql = "UPDATE Question SET QuestionText = ?, QDefault = ? WHERE QID = ?";
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, newContent);
	        stmt.setString(2, newDiffStr);
	        stmt.setInt(3, qid);
	        int rows = stmt.executeUpdate();
	        if (rows > 0) {
	            System.out.println("Question updated successfully in DB.");
	        } else {
	            System.out.println("Failed to update question in DB.");
	        }
	    } catch (SQLException e) {
	        System.out.println("Error updating question: " + e.getMessage());
	    }
	}

	public void deleteQuestion(Connection conn, QuestionPool questionPool) {
	    Scanner sc = new Scanner(System.in);
		System.out.print("Enter question ID to delete: ");
        int qid = sc.nextInt();
        sc.nextLine();
        String sql = "DELETE FROM Question WHERE QID = ?";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, qid);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                SelectionQuestion[] qArr = questionPool.getQuestions();
                for (int i = 0; i < qArr.length; i++) {
                    if (qArr[i] != null && qArr[i].getId() == qid) {
                        qArr[i] = null;
                    }
                }
                questionPool.questions = java.util.Arrays.stream(qArr)
                        .filter(q -> q != null)
                        .toArray(SelectionQuestion[]::new);
                System.out.println("Question deleted.");
            } else {
                System.out.println("Question not found in DB.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting question: " + e.getMessage());
        }
    }

	public void menuQuestion(Connection conn, int subjectID) {
	    this.loadQuestionsFromDB(conn,subjectID ); 
	    int choice = -1;
	    Scanner sc = new Scanner(System.in);
	    while (choice != 0) {
	        System.out.println("\n---- Question Manager (Subject ID = " + subjectID + ") ----");
	        System.out.println("1 - Insert new question");
	        System.out.println("2 - Update question");
	        System.out.println("3 - Delete question");
	        System.out.println("4 - Show all questions");
	        System.out.println("0 - Exit");
	        System.out.print("Your choice: ");
	        choice = sc.nextInt(); sc.nextLine();
	        switch (choice) {
	            case 1:
	            	insertQuestionFromUser(conn, this);
	            	break;
	            case 2:
	            	updateQuestion(conn, this);
	            	break;
	            
	            case 3:
	            	deleteQuestion(conn, this);
	            	break;
	            case 4: 
	            	System.out.println(this.toStringSelection());
	            	break;
	            case 0:
	            	System.out.println("Exiting question manager.");
	            	break;
	            default:
	            	System.out.println("Invalid choice.");
	            	break;
	        }
	    }
	}

}