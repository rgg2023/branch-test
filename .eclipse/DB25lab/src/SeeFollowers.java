import java.sql.*;

public class SeeFollowers {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            // DB 연결
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/twitter", "root", "12345");
            System.out.println("✅ Database connected successfully!");

            String currentUserId = "u001"; // 로그인된 사용자 ID (예시)

            // followers 조회 쿼리
            String sql = 
                "SELECT u.user_id, u.display_name " +
                "FROM follower f " +
                "JOIN user u ON f.follower_id = u.user_id " +
                "WHERE f.user_id = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("📋 Followers of " + currentUserId + ":");
            boolean hasFollowers = false;
            while (rs.next()) {
                hasFollowers = true;
                System.out.println("- " + rs.getString("display_name") + " (" + rs.getString("user_id") + ")");
            }
            if (!hasFollowers) {
                System.out.println("(No followers found)");
            }

            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null && !conn.isClosed()) conn.close();
                System.out.println("🔒 Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
