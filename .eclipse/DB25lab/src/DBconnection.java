
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/twitter";
    private static final String USER = "root";
    private static final String PASSWORD = "12345";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
