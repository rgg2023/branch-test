package model;

import java.sql.Timestamp;

public class Post {
    private String postId;
    private String writerId;
    private String displayName;
    private String content;
    private int numOfLikes;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int numsOfViews;
    private int numOfComments; // ✅ 댓글 수 추가
    private String writerProfileImagePath; // ✅ 작성자 프로필 이미지 경로 추가

    // ----------------------------------------------------
    // 생성자 (10개 인자 - 모든 필드 초기화)
    // ----------------------------------------------------
    public Post(String postId, String writerId, String displayName, String content, 
                int numOfLikes, Timestamp createdAt, Timestamp updatedAt, 
                int numsOfViews, int numOfComments, String writerProfileImagePath) {
        this.postId = postId;
        this.writerId = writerId;
        this.displayName = displayName;
        this.content = content;
        this.numOfLikes = numOfLikes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.numsOfViews = numsOfViews;
        this.numOfComments = numOfComments;
        this.writerProfileImagePath = writerProfileImagePath;
    }

    // ----------------------------------------------------
    // 생성자 (글쓰기용 - DB 생성 시 자동 필드는 제외, 7개 인자)
    // ----------------------------------------------------
    // PostFrame.writePost()에서 사용됨
    public Post(String postId, String writerId, String displayName, String content, 
                int numOfLikes, Timestamp createdAt, Timestamp updatedAt, 
                int numsOfViews, int numOfComments) {
        this(postId, writerId, displayName, content, numOfLikes, createdAt, updatedAt, numsOfViews, numOfComments, null); // 이미지 경로는 null로 초기화
    }

    // ----------------------------------------------------
    // Getter 및 Setter
    // ----------------------------------------------------

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getWriterId() {
        return writerId;
    }

    public void setWriterId(String writerId) {
        this.writerId = writerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getNumOfLikes() {
        return numOfLikes;
    }

    public void setNumOfLikes(int numOfLikes) {
        this.numOfLikes = numOfLikes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getNumsOfViews() {
        return numsOfViews;
    }

    public void setNumsOfViews(int numsOfViews) {
        this.numsOfViews = numsOfViews;
    }

    public int getNumOfComments() {
        return numOfComments;
    }

    public void setNumOfComments(int numOfComments) {
        this.numOfComments = numOfComments;
    }

    public String getWriterProfileImagePath() { // ✅ Getter 추가
        return writerProfileImagePath;
    }

    public void setWriterProfileImagePath(String writerProfileImagePath) { // ✅ Setter 추가
        this.writerProfileImagePath = writerProfileImagePath;
    }
}