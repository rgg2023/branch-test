package model;

import java.sql.Timestamp;

public class User {
    private String userId;
    private String pwd;
    private String username;
    private String email;
    private String displayName;
    private String bio;
    private Timestamp createdAt;
    private String profileImagePath;

    public User(String userId, String username, String pwd, String email,
    		String displayName, String bio, Timestamp createdAt, String profileImagePath) {
        this.userId = userId;
        this.pwd = pwd;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.bio = bio;
        this.createdAt = createdAt;
        this.profileImagePath = profileImagePath;
    }

    // 기본 생성자
    public User() {}

    // Getter / Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPwd() { return pwd; }
    public void setPwd(String pwd) { this.pwd = pwd; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public String getProfileImagePath() {
        return profileImagePath;
    }
    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
    
}