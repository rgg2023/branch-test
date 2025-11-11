import java.sql.*;
import java.util.Scanner;
import java.util.UUID;

public class LikePost {
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

            // 로그인된 사용자 (예: 현호)
            String loginUserId = "u001";  // 실제 user.user_id
            System.out.println("✅ Login successful: " + loginUserId);

            // 좋아요할 게시글 입력받기
            System.out.print("Enter post_id to like: ");
            String postId = sc.nextLine();

            // 이미 좋아요한 적 있는지 확인
            String checkSql = "SELECT * FROM post_like WHERE post_id = ? AND liker_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, postId);
            checkStmt.setString(2, loginUserId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("⚠️ You've already liked this post!");
            } else {
                // 좋아요 추가
                String lId = "l" + UUID.randomUUID().toString().substring(0, 6);
                String insertSql = "INSERT INTO post_like (l_id, post_id, liker_id) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, lId);
                insertStmt.setString(2, postId);
                insertStmt.setString(3, loginUserId);
                insertStmt.executeUpdate();

                // posts의 좋아요 수 증가
                String updateSql = "UPDATE posts SET num_of_likes = num_of_likes + 1 WHERE post_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, postId);
                int rows = updateStmt.executeUpdate();

                if (rows > 0) {
                    System.out.println("❤️ Post " + postId + " liked successfully!");
                } else {
                    System.out.println("⚠️ No such post found.");
                }
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
