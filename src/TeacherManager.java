
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class TeacherManager {

    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        String query = "SELECT * FROM Teacher";
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             while (rs.next()) {
                String tid = rs.getString("TID");
                String tname = rs.getString("TName");
                String phone = rs.getString("PhoneNumber");
                String date = rs.getString("EmploymentDate");

                Teacher t = new Teacher(tid, tname, phone, date);
                teachers.add(t);
             }

        } catch (Exception e) {
            System.out.println("Error reading teachers:");
            e.printStackTrace();
        }

        return teachers;
    }

    public void addTeacher(Teacher t) {
        if (teacherExists(t.getTid())) {
            System.out.println("Teacher with TID " + t.getTid() + " already in the system.");
            return;
        }
        String query = "INSERT INTO Teacher (TID, TName, PhoneNumber, EmploymentDate) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, t.getTid());
            stmt.setString(2, t.getTname());
            stmt.setString(3, t.getPhoneNumber());
            stmt.setDate(4, Date.valueOf(t.getEmploymentDate()));
            stmt.executeUpdate();
            System.out.println("Teacher added: " + t.getTname());
        } catch (Exception e) {
            System.out.println("Error adding teacher:");
            e.printStackTrace();
        }
    }

    public boolean teacherExists(String tid) {
        String query = "SELECT 1 FROM Teacher WHERE TID = ?";
        try (Connection conn = DBConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tid);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            System.out.println("Error checking if teacher exists:");
            e.printStackTrace();
            return false;
        }
    }

    public void addTeacherFromUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Teacher ID (9 digits): ");
        String tid = scanner.nextLine();
        if (teacherExists(tid)) {
            System.out.println("Teacher with TID " + tid + " already in the system.");
            return;
        }
        System.out.print("Enter teacher's name: ");
        String name = scanner.nextLine();
        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine();
        if (phone.isEmpty()) {
            phone = null;
        }
        System.out.print("Enter employment date (yyyy-mm-dd): ");
        String date = scanner.nextLine();
        Teacher te = new Teacher(tid, name, phone, date);
        addTeacher(te);
    }
    
    public void deleteTeacher(String tid) {
        String query = "DELETE FROM Teacher WHERE TID = ?";
        try (Connection conn = DBConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tid);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0)
                System.out.println("Teacher with ID " + tid + " deleted.");
            else
                System.out.println("No teacher found with TID " + tid + ".");
        } catch (Exception e) {
            System.out.println("Error deleting teacher:");
            e.printStackTrace();
        }
    }
    
    public void updateTeacher() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter ID of the teacher to update: ");
        String tid = scanner.nextLine();
        if (!teacherExists(tid)) {
            System.out.println("Teacher with TID " + tid + " does not exist.");
            return;
        }
        System.out.print("New name (leave empty to skip): ");
        String name = scanner.nextLine();
        System.out.print("New phone number (leave empty to skip): ");
        String phone = scanner.nextLine();
        System.out.print("New employment date (yyyy-mm-dd) (leave empty to skip): ");
        String date = scanner.nextLine();
        StringBuilder query = new StringBuilder("UPDATE Teacher SET ");
        List<String> updates = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (!name.isEmpty()) {
            updates.add("TName = ?");
            params.add(name);
        }
        if (!phone.isEmpty()) {
            updates.add("PhoneNumber = ?");
            params.add(phone);
        }
        if (!date.isEmpty()) {
            updates.add("EmploymentDate = ?");
            params.add(Date.valueOf(date));
        }

        if (updates.isEmpty()) {
            System.out.println("No fields to update.");
            return;
        }
        query.append(String.join(", ", updates)).append(" WHERE TID = ?");
        params.add(tid);
        try (Connection conn = DBConnection.connect();
            PreparedStatement stmt = conn.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            int affected = stmt.executeUpdate();
            if (affected > 0)
                System.out.println("Teacher updated successfully.");
            else
                System.out.println("Update failed.");

        } catch (Exception e) {
            System.out.println("Error updating teacher:");
            e.printStackTrace();
        }
    }

    public void addSubjectToTeacher() {
        try (Connection conn = DBConnection.connect();) {
            Scanner sc = new Scanner(System.in);
            System.out.println("List of teachers:");
            Statement teacherStmt = conn.createStatement();
            ResultSet rsTeachers = teacherStmt.executeQuery("SELECT TID, TName FROM Teacher");
            while (rsTeachers.next()) {
                System.out.println("TID: " + rsTeachers.getString("TID") +
                                   " - Name: " + rsTeachers.getString("TName"));
            }
            System.out.print("Enter teacher ID: ");
            String teacherId = sc.nextLine();
            PreparedStatement checkTeacher = conn.prepareStatement(
                "SELECT * FROM Teacher WHERE TID = ?");
            checkTeacher.setString(1, teacherId);
            ResultSet teacherResult = checkTeacher.executeQuery();
            if (!teacherResult.next()) {
                System.out.println("Teacher not found.");
                return;
            }

            System.out.println("Available Subjects:");
            Statement subjectStmt = conn.createStatement();
            ResultSet rsSubjects = subjectStmt.executeQuery("SELECT SID, SName FROM Subject");
            while (rsSubjects.next()) {
                System.out.println("SID: " + rsSubjects.getInt("SID") +
                                   " - Name: " + rsSubjects.getString("SName"));
            }

            System.out.print("Enter Subject ID to assign: ");
            int subjectId = sc.nextInt();

            PreparedStatement checkLink = conn.prepareStatement(
                "SELECT * FROM TeacherTeachSubject WHERE TID = ? AND SID = ?");
            checkLink.setString(1, teacherId);
            checkLink.setInt(2, subjectId);
            ResultSet linkResult = checkLink.executeQuery();
            if (linkResult.next()) {
                System.out.println("This subject is already assigned to the teacher.");
                return;
            }

            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO TeacherTeachSubject (TID, SID) VALUES (?, ?)");
            insertStmt.setString(1, teacherId);
            insertStmt.setInt(2, subjectId);
            insertStmt.executeUpdate();

            System.out.println("Subject added to teacher successfully.");
        } catch (SQLException e) {
            System.out.println("Error in assigning subject to teacher:");
            e.printStackTrace();
        }
    }

    public void teacherMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n--- Teacher Management Menu: ---");
            System.out.println("1️ - Add Teacher");
            System.out.println("2️ - Delete Teacher");
            System.out.println("3️ - Update Teacher");
            System.out.println("4 - Add Subject To Teacher");
            System.out.println("5 - Show All Teachers");
            System.out.println("0️ - Exit");
            System.out.print("Choose option: ");

            choice = scanner.nextInt();
            scanner.nextLine(); 

            switch (choice) {
                case 1:
                    addTeacherFromUser();
                    break;
                case 2:
                    System.out.print("Enter ID to delete: ");
                    String tidToDelete = scanner.nextLine();
                    deleteTeacher(tidToDelete);
                    break;
                case 3:
                    updateTeacher();
                    break;
                case 4:
                    addSubjectToTeacher();
                    break;
                case 5:
                    System.out.println("\nAll Teachers:");
                    for (Teacher t : getAllTeachers()) {
                        System.out.println(t);
                    }
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Incorrect input, try again.");
            }
        } while (choice != 0);
    }

}
