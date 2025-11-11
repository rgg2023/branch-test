import java.sql.*;
import java.util.Scanner;

public class WriteComment {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // DB 연결
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/twitter",
                "root", "12345"
            );
            System.out.println("✅ Database connected successfully!");

            // 로그인된 사용자 (예: u001)
            String writerId = "u001"; // 실제 로그인한 사용자로 변경

            // 댓글 작성 대상 게시글
            System.out.print("📌 Enter Post ID to comment on: ");
            String postId = sc.nextLine();

            // 댓글 내용 입력
            System.out.print("💬 Enter comment content: ");
            String content = sc.nextLine();

            // 댓글 ID 생성 (예: c001, c002 등)
            String newCommentId = "c" + System.currentTimeMillis();

            // INSERT SQL
            String sql = "INSERT INTO comment (comment_id, content, writer_id, post_id, num_of_likes) VALUES (?, ?, ?, ?, 0)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newCommentId);
            pstmt.setString(2, content);
            pstmt.setString(3, writerId);
            pstmt.setString(4, postId);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Comment added successfully! (ID: " + newCommentId + ")");
            } else {
                System.out.println("❌ Failed to add comment.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
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
