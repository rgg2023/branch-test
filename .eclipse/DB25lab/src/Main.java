import java.sql.*;

public class Main {
    public static void main(String[] args) {
        Connection conn = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost/twitter";
            String user = "root", passwd = "12345";
            conn = DriverManager.getConnection(url, user, passwd);
            System.out.println("✅ Database connected successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ----------- 1️⃣ Login Function -----------
        String username = "kimhh";
        String password = "pass123";

        boolean loggedIn = false;

        try {
            String sql = "SELECT * FROM user WHERE username=? AND pwd=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("✅ Login successful: " + rs.getString("display_name"));
                loggedIn = true;
            } else {
                System.out.println("❌ Login failed. Invalid username or password.");
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

     // -----------  Write Post Function -----------
        if (loggedIn) {
            try {
                // New post information
                String postId = "p004";       // New post ID
                String writerId = "u001";     // Logged-in user ID
                String content = "Learning SQL JOINs!"; // Post content

                // SQL to insert new post
                String sql = "INSERT INTO posts (post_id, content, writer_id, num_of_likes, created_at, updated_at, nums_of_views) " +
                             "VALUES (?, ?, ?, 0, NOW(), NOW(), 0)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, postId);
                pstmt.setString(2, content);
                pstmt.setString(3, writerId);

                // Execute update
                int rowsInserted = pstmt.executeUpdate();

                if (rowsInserted > 0)
                    System.out.println("New post (" + postId + ") created successfully.");
                else
                    System.out.println("Failed to create post.");

                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // ----------- 3️⃣ Close Connection -----------
        try {
            if (conn != null && !conn.isClosed()) conn.close();
            System.out.println("🔒 Database connection closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
