package dao;

import model.Post;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    // 💡 Helper: ResultSet을 Post 객체로 매핑하는 통합 메서드 (10개 필드)
    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        // Post 생성자 순서: postId, writerId, displayName, content, numOfLikes, 
        // createdAt, updatedAt, numsOfViews, numOfComments, writerProfileImagePath
        
        return new Post(
            rs.getString("post_id"),
            rs.getString("writer_id"),
            rs.getString("display_name"),
            rs.getString("content"),
            rs.getInt("num_of_likes"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at"),
            rs.getInt("nums_of_views"),
            rs.getInt("comment_count"), // ✅ 댓글 수
            rs.getString("profile_image_path") // ✅ 프로필 경로
        );
    }

    // ----------------------------------------------------
    // C(Create) - 게시글 작성
    // ----------------------------------------------------
    public boolean writePost(Post post) {
        String sql = "INSERT INTO posts (post_id, writer_id, content, created_at, updated_at) " +
                     "VALUES (?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, post.getPostId());
            pstmt.setString(2, post.getWriterId());
            pstmt.setString(3, post.getContent());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ----------------------------------------------------
    // R(Read) - 타임라인 로드 (팔로우 기반)
    // ----------------------------------------------------
    public List<Post> getTimelinePosts(String userId) {
        List<Post> posts = new ArrayList<>();
        
        String sql = "SELECT p.*, u.display_name, u.profile_image_path, " +
                     "COUNT(c.comment_id) AS comment_count " +
                     "FROM posts p " +
                     "JOIN user u ON p.writer_id = u.user_id " +
                     "LEFT JOIN comments c ON p.post_id = c.post_id " +
                     "WHERE p.writer_id IN ( " +
                     "    SELECT user_id FROM following WHERE follower_id = ? " +
                     ") OR p.writer_id = ? " +
                     // ✅ GROUP BY 절 완성
                     "GROUP BY p.post_id, p.writer_id, u.display_name, u.profile_image_path, p.content, p.num_of_likes, p.created_at, p.updated_at, p.nums_of_views " + 
                     "ORDER BY p.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            pstmt.setString(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }
    
    // ----------------------------------------------------
    // R(Read) - 단일 게시글 로드 (상세 보기/갱신용)
    // ----------------------------------------------------
    public Post getPostById(String postId) {
        String sql = "SELECT p.*, u.display_name, u.profile_image_path, " +
                     "COUNT(c.comment_id) AS comment_count " +
                     "FROM posts p " +
                     "JOIN user u ON p.writer_id = u.user_id " +
                     "LEFT JOIN comments c ON p.post_id = c.post_id " +
                     "WHERE p.post_id = ? " +
                     "GROUP BY p.post_id, p.writer_id, u.display_name, u.profile_image_path, p.content, p.num_of_likes, p.created_at, p.updated_at, p.nums_of_views"; // ✅ GROUP BY 절 추가
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, postId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPost(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------
    // U(Update) - 좋아요/취소 토글
    // ----------------------------------------------------
    public boolean toggleLike(String postId, String userId) {
        if (isLikedBy(postId, userId)) {
            String deleteSql = "DELETE FROM likes WHERE post_id = ? AND user_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setString(1, postId);
                pstmt.setString(2, userId);
                
                if (pstmt.executeUpdate() > 0) {
                    updatePostLikeCount(postId, -1);
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String insertSql = "INSERT INTO likes (post_id, user_id) VALUES (?, ?)"; // liked_at 제외
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, postId);
                pstmt.setString(2, userId);
                
                if (pstmt.executeUpdate() > 0) {
                    updatePostLikeCount(postId, 1);
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // 좋아요 여부 확인
    public boolean isLikedBy(String postId, String userId) {
        String sql = "SELECT 1 FROM likes WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            pstmt.setString(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // posts 테이블의 좋아요 수 업데이트
    private void updatePostLikeCount(String postId, int change) {
        String sql = "UPDATE posts SET num_of_likes = num_of_likes + ?, updated_at = NOW() WHERE post_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, change);
            pstmt.setString(2, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 좋아요 목록 로드 (Likers List)
    public List<String> getLikersDisplayName(String postId) {
        List<String> likers = new ArrayList<>();
        String sql = "SELECT u.display_name " +
                     "FROM likes l JOIN user u ON l.user_id = u.user_id " +
                     "WHERE l.post_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, postId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    likers.add(rs.getString("display_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return likers;
    }
    
    // D(Delete) - 게시글 삭제
    public boolean deletePost(String postId) {
        String sql = "DELETE FROM posts WHERE post_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 특정 사용자가 작성한 게시글만 가져옵니다. (댓글 수와 프로필 경로 포함)
     */
    public List<Post> getPostsByUserId(String userId) {
        List<Post> posts = new ArrayList<>();
        
        // SQL 쿼리: 특정 사용자의 글만 필터링하고 댓글 수, 프로필 경로를 JOIN
        String sql = "SELECT p.*, u.display_name, u.profile_image_path, COUNT(c.comment_id) AS comment_count " +
                     "FROM posts p " + 
                     "JOIN user u ON p.writer_id = u.user_id " +
                     "LEFT JOIN comments c ON p.post_id = c.post_id " +
                     "WHERE p.writer_id = ? " + // ✅ 특정 사용자 ID로 필터링
                     // GROUP BY 절 완성
                     "GROUP BY p.post_id, p.writer_id, u.display_name, u.profile_image_path, p.content, p.num_of_likes, p.created_at, p.updated_at, p.nums_of_views " + 
                     "ORDER BY p.created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId); // 사용자 ID 바인딩
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Post 객체로 매핑 (mapResultSetToPost 헬퍼 메서드가 있다면 그걸 사용)
                    // Post 생성자 순서에 맞춰 10개 인자를 전달
                    Post post = new Post(
                        rs.getString("post_id"),
                        rs.getString("writer_id"),
                        rs.getString("display_name"), 
                        rs.getString("content"),
                        rs.getInt("num_of_likes"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at"),
                        rs.getInt("nums_of_views"),
                        rs.getInt("comment_count"), 
                        rs.getString("profile_image_path") // 프로필 경로
                    );
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }
}