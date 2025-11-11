package dao;

import model.User;
import util.DBConnection;
import java.sql.*;
import java.util.List; // List는 필요 없으나, 만약을 위해 import 유지

public class UserDAO {

    // 💡 Helper: 단일 User 객체를 ResultSet에서 생성
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        // 🚨 model/User.java의 생성자 순서와 일치해야 합니다.
        // 현재 model/User.java에는 7개 인자 생성자만 정의되어 있고, 
        // profileImagePath는 Setter로 처리하거나 8개 인자 생성자가 필요합니다.
        // 여기서는 profileImagePath를 포함하는 8개 인자 생성자가 있다고 가정합니다.
        
        return new User(
            rs.getString("user_id"),
            rs.getString("pwd"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("display_name"),
            rs.getString("bio"),
            rs.getTimestamp("created_at_datetime"),
            rs.getString("profile_image_path") // 👈 8번째 인자 (DB에서 로드)
        );
    }
    
    // ----------------------------------------------------
    // 핵심 기능 (로그인 / 회원가입)
    // ----------------------------------------------------

    public User login(String username, String pwd) {
        String sql = "SELECT *, profile_image_path FROM user WHERE username=? AND pwd=?"; // 👈 profile_image_path 추가
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, pwd);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User loggedInUser = createUserFromResultSet(rs);
                    loggedInUser.setPwd(null); // 보안: 비밀번호 정보 제거
                    return loggedInUser;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean register(User user) {
        // DB 스키마에 profile_image_path가 NOT NULL이 아니라면 이 쿼리가 작동해야 합니다.
        String sql = "INSERT INTO user (user_id, pwd, username, email, display_name, bio, created_at_datetime, profile_image_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPwd());
            pstmt.setString(3, user.getUsername());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getDisplayName());
            pstmt.setString(6, user.getBio());
            pstmt.setString(7, user.getProfileImagePath()); // 👈 profileImagePath 바인딩
            
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }
    
    // ----------------------------------------------------
    // 프로필 및 비밀번호 업데이트
    // ----------------------------------------------------

    // 🔹 사용자 정보 업데이트 (ProfileFrame에서 사용)
    public boolean updateProfile(User user) {
        // profile_image_path 업데이트 포함
        String sql = "UPDATE user SET username = ?, email = ?, display_name = ?, bio = ?, profile_image_path = ? WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getDisplayName());
            pstmt.setString(4, user.getBio());
            pstmt.setString(5, user.getProfileImagePath()); 
            pstmt.setString(6, user.getUserId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 🔹 비밀번호 업데이트
    public boolean updatePassword(String userId, String currentPwd, String newPwd) {
        String checkSql = "SELECT pwd FROM user WHERE user_id = ? AND pwd = ?";
        String updateSql = "UPDATE user SET pwd = ? WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();) {
            // 1단계: 현재 비밀번호 확인
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, userId);
                checkStmt.setString(2, currentPwd);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) return false;
                }
            }
            
            // 2단계: 새 비밀번호로 업데이트
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newPwd);
                updateStmt.setString(2, userId);
                return updateStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ----------------------------------------------------
    // 통계 기능
    // ----------------------------------------------------

    public int getFollowingCount(String userId) {
        String sql = "SELECT COUNT(*) FROM following WHERE follower_id = ?";
        // ... (로직 생략: 기존과 동일) ...
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getFollowerCount(String userId) {
        String sql = "SELECT COUNT(*) FROM following WHERE user_id = ?";
        // ... (로직 생략: 기존과 동일) ...
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
    
    public int getPostCount(String userId) {
        String sql = "SELECT COUNT(*) FROM posts WHERE writer_id = ?";
        // ... (로직 생략: 기존과 동일) ...
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}