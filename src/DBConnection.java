
import java.sql.*;

public class DBConnection {
    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");

            String url = "jdbc:postgresql://localhost:5432/EXAMDB";
            String user = "postgres";
            String password = "211613526Jjj";
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to PostgreSQL database.");
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }
}
