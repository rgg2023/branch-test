import java.sql.*;
import java.util.*;

public class FollowUser {
    public static void main(String[] args) {
        Connection conn = null;
        Scanner sc = new Scanner(System.in);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost/twitter";
            String user = "root";
            String passwd = "12345";
            conn = DriverManager.getConnection(url, user, passwd);
            System.out.println("✅ Database connected successfully!");

            // 로그인된 사용자 (예시)
            String loginUserId = "u001";  // 현호
            System.out.println("✅ Login successful: " + loginUserId);

            // 팔로우할 대상 ID 입력받기
            System.out.print("Enter user_id to follow: ");
            String targetUserId = sc.nextLine();

            if (loginUserId.equals(targetUserId)) {
                System.out.println("⚠️ You cannot follow yourself!");
                return;
            }

            // 이미 팔로우했는지 확인
            String checkSql = "SELECT * FROM follower WHERE user_id = ? AND follower_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, targetUserId);
            checkStmt.setString(2, loginUserId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("⚠️ You already follow this user!");
            } else {
                // follower 테이블 추가
                String fId1 = "f" + UUID.randomUUID().toString().substring(0, 6);
                String insertFollower = "INSERT INTO follower (f_id, user_id, follower_id) VALUES (?, ?, ?)";
                PreparedStatement pstmt1 = conn.prepareStatement(insertFollower);
                pstmt1.setString(1, fId1);
                pstmt1.setString(2, targetUserId);
                pstmt1.setString(3, loginUserId);
                pstmt1.executeUpdate();

                // following 테이블 추가
                String fId2 = "f" + UUID.randomUUID().toString().substring(0, 6);
                String insertFollowing = "INSERT INTO following (f_id, user_id, follower_id) VALUES (?, ?, ?)";
                PreparedStatement pstmt2 = conn.prepareStatement(insertFollowing);
                pstmt2.setString(1, fId2);
                pstmt2.setString(2, loginUserId);
                pstmt2.setString(3, targetUserId);
                pstmt2.executeUpdate();

                System.out.println("🤝 You are now following user: " + targetUserId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    System.out.println("🔒 Database connection closed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
