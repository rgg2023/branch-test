package gui;

import dao.PostDAO;
import dao.CommentDAO;
import dao.FollowDAO;
import model.Post;
import model.Comment;
import model.User;
import util.TimeFormatter;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class PostFrame extends JFrame {
    private User loginUser;
    private JPanel timelinePanel;
    private JTextField postField;
    private PostDAO postDAO;
    private CommentDAO commentDAO;
    private FollowDAO followDAO;
    
    private JFrame postDetailFrame; // 트윗 상세 화면 프레임

    public PostFrame(User user) {
        this.loginUser = user;
        postDAO = new PostDAO();
        commentDAO = new CommentDAO();
        followDAO = new FollowDAO();

        setTitle("Mini Twitter - " + user.getDisplayName());
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 글 목록 패널
        timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(timelinePanel);
        add(scrollPane, BorderLayout.CENTER);

        // 글쓰기 패널
        JPanel writePanel = new JPanel(new BorderLayout(5, 5));
        writePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        postField = new JTextField();
        JButton postButton = new JButton("글쓰기");
        postButton.addActionListener(e -> writePost());
        writePanel.add(postField, BorderLayout.CENTER);
        writePanel.add(postButton, BorderLayout.EAST);
        
        // 버튼 이름 "👤 프로필"로 변경
        JButton profileBtn = new JButton("👤 프로필");
        profileBtn.addActionListener(e -> {
            new ProfileFrame(loginUser, this); // ProfileFrame에 인스턴스 전달
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(writePanel, BorderLayout.CENTER);
        topPanel.add(profileBtn, BorderLayout.WEST);
        
        add(topPanel, BorderLayout.NORTH);
        
        refreshTimeline();
        setVisible(true);
    }

    // 글쓰기
    private void writePost() {
        String content = postField.getText().trim();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "글 내용을 입력하세요!");
            return;
        }

        String postId = "p" + System.currentTimeMillis();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        // Post 생성자: 10개 인자 (DB에서 자동 처리되는 값 제외하고 나머지 기본값 설정)
        Post post = new Post(
                postId,
                loginUser.getUserId(),
                loginUser.getDisplayName(),
                content,
                0, // numOfLikes
                now, // createdAt
                now, // updatedAt
                0, // numsOfViews
                0, // numOfComments
                loginUser.getProfileImagePath() // writerProfileImagePath
        );

        boolean success = postDAO.writePost(post);
        if (success) {
            postField.setText("");
            refreshTimeline();
        } else {
            JOptionPane.showMessageDialog(this, "글 작성 실패!");
        }
    }

    // 글 목록 갱신 (팔로우 기반 로직 사용)
    public void refreshTimeline() {
        timelinePanel.removeAll();

        // 팔로우 기반 타임라인 로드 
        List<Post> posts = postDAO.getTimelinePosts(loginUser.getUserId());

        for (Post p : posts) {
            // 1. 포스트 전체 컨테이너
            JPanel postContainer = new JPanel(new BorderLayout());

            // 2. 게시글 본문 패널 (클릭 가능하도록 설정)
            JPanel postPanel = new JPanel(new BorderLayout(5, 5));
            postPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY), 
                BorderFactory.createEmptyBorder(10, 10, 5, 10) 
            ));
            
            // 트윗 클릭 시 상세 화면 표시 (리스너 등록)
            postPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            postPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getButton() == java.awt.event.MouseEvent.BUTTON1) { 
                        showPostDetail(p);
                    }
                }
            });
            
         // 💡 Post 객체에서 Timestamp를 가져와 TimeFormatter를 사용합니다.
            String relativeTime = TimeFormatter.formatRelativeTime(p.getCreatedAt());

         // 기존 writerLabel 옆에 시간을 추가하도록 HTML 수정
            JLabel writerLabel = new JLabel(
                "<html><b>" + p.getDisplayName() + "</b> " + 
                "<span style='color:gray;'>@" + p.getWriterId() + " · " + relativeTime + "</span></html>" // 👈 ✅ 시간 추가
            );
            writerLabel.setFont(new Font(writerLabel.getFont().getName(), Font.PLAIN, 14));

            JPanel writerInfoPanel = new JPanel(new BorderLayout(5, 0));

            // 프로필 이미지 표시 로직
            JLabel profilePicLabel = new JLabel();
            profilePicLabel.setPreferredSize(new Dimension(30, 30));
            profilePicLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (p.getWriterProfileImagePath() != null && !p.getWriterProfileImagePath().isEmpty()) {
                try {
                    ImageIcon icon = new ImageIcon(p.getWriterProfileImagePath());
                    Image img = icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                    profilePicLabel.setIcon(new ImageIcon(img));
                } catch (Exception e) {
                    profilePicLabel.setText("👤"); 
                }
            } else {
                profilePicLabel.setText("👤"); 
            }

            JPanel nameAndFollowPanel = new JPanel(new BorderLayout());
            nameAndFollowPanel.add(writerLabel, BorderLayout.CENTER); 

            // 팔로우 버튼 로직
            if (!p.getWriterId().equals(loginUser.getUserId())) {
                JButton followButton = new JButton();
                boolean isFollowing = followDAO.isFollowing(loginUser.getUserId(), p.getWriterId());
                
                followButton.setText(isFollowing ? "✔️ 언팔로우" : "➕ 팔로우");
                followButton.addActionListener(e -> {
                    followDAO.toggleFollow(loginUser.getUserId(), p.getWriterId()); 
                    refreshTimeline();
                });
                nameAndFollowPanel.add(followButton, BorderLayout.EAST);
            }
            
            writerInfoPanel.add(profilePicLabel, BorderLayout.WEST);
            writerInfoPanel.add(nameAndFollowPanel, BorderLayout.CENTER);
            
            // 4. 내용 영역
            JTextArea contentArea = new JTextArea(p.getContent());
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setEditable(false);
            contentArea.setBackground(postPanel.getBackground());
            contentArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            
            // 5. 좋아요 및 답글 버튼 영역
         // 5. 좋아요 및 답글 버튼 영역
            boolean alreadyLiked = postDAO.isLikedBy(p.getPostId(), loginUser.getUserId());
            
            // 🚨 수정: 좋아요 버튼에 이모지 사용 및 폰트 크기 조정
            JButton likeButton = new JButton(alreadyLiked ? "❤️" : "♡");
            likeButton.setFont(new Font("Arial", Font.BOLD, 18)); // 아이콘처럼 크게 보이도록 폰트 크기 설정
            
            // 버튼의 여백과 크기를 조정하여 아이콘처럼 보이도록 처리
            likeButton.setMargin(new Insets(2, 2, 2, 2));
            likeButton.setPreferredSize(new Dimension(40, 30)); // 크기 고정
            
            JLabel likeLabel = new JLabel(String.valueOf(p.getNumOfLikes()));

            likeButton.addActionListener(e -> {
                boolean likedNow = postDAO.toggleLike(p.getPostId(), loginUser.getUserId());
                p.setNumOfLikes(likedNow ? p.getNumOfLikes() + 1 : p.getNumOfLikes() - 1);
                likeLabel.setText(String.valueOf(p.getNumOfLikes()));
                
                // 🚨 수정: 버튼 텍스트를 이모지 아이콘으로 갱신
                likeButton.setText(likedNow ? "❤️" : "♡"); 
            });

            // 좋아요 목록 우클릭
            likeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            likeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getButton() == java.awt.event.MouseEvent.BUTTON1 || e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                         showLikersList(p);
                    }
                }
            });

            // 답글 수 표시 레이블
            JLabel commentCountLabel = new JLabel("💬 " + p.getNumOfComments()); 
            commentCountLabel.setFont(new Font("Arial", Font.BOLD, 14)); // 폰트 크기 조정
            // 게시글 답글 버튼 
         // 게시글 답글 버튼 (버튼 텍스트를 아이콘 스타일로 변경)
            JButton replyToPostBtn = new JButton("↩️ 답글"); 
            replyToPostBtn.setFont(new Font("Arial", Font.PLAIN, 12)); // 일반 텍스트 크기 유지 (버튼 역할 강조)
            replyToPostBtn.addActionListener(ev -> {
                String replyText = JOptionPane.showInputDialog(PostFrame.this, p.getDisplayName() + "님에게 답글을 작성하세요:");
                
                if (replyText != null && !replyText.trim().isEmpty()) { 
                    Comment reply = new Comment(
                        "r" + System.currentTimeMillis(), 
                        replyText,
                        loginUser.getUserId(),
                        p.getPostId(),
                        0,
                        null,
                        loginUser.getDisplayName()
                    );
                    boolean success = commentDAO.addComment(reply);
                    if (success) {
                        refreshTimeline(); // 메인 피드 갱신
                    } else {
                        JOptionPane.showMessageDialog(PostFrame.this, "답글 작성 실패!");
                    }
                }
            });

         // bottomPanel 조립
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         // 좋아요 버튼과 카운터를 나란히 배치
            bottomPanel.add(likeButton);
            bottomPanel.add(likeLabel); 
            
            // 답글 아이콘과 버튼을 나란히 배치
            bottomPanel.add(commentCountLabel); 
            bottomPanel.add(replyToPostBtn); // 답글 버튼은 여전히 텍스트로 제공

            // postPanel 조립
            postPanel.add(writerInfoPanel, BorderLayout.NORTH);
            postPanel.add(contentArea, BorderLayout.CENTER);
            postPanel.add(bottomPanel, BorderLayout.SOUTH);

            postContainer.add(postPanel, BorderLayout.NORTH); 
            
            timelinePanel.add(postContainer);
        }

        timelinePanel.revalidate();
        timelinePanel.repaint();
    }
    
    /**
     * 게시글 상세 (댓글 목록) 프레임을 보여줍니다. (트윗 클릭 시 호출)
     */
    public void showPostDetail(Post post) {
        if (postDetailFrame != null) {
            postDetailFrame.dispose();
        }
        
        // 상세 프레임은 PostFrame 인스턴스에 종속되지 않음
        postDetailFrame = new JFrame("트윗 상세 - " + post.getDisplayName());
        postDetailFrame.setSize(500, 500);
        postDetailFrame.setLocationRelativeTo(this);
        postDetailFrame.setLayout(new BorderLayout());

        // 1. 원본 게시글 정보 패널
        JPanel postPanel = new JPanel(new BorderLayout(5, 5));
        postPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        postPanel.add(new JLabel("<html><b>" + post.getDisplayName() + "</b> <span style='color:gray;'>@" + post.getWriterId() + "</span></html>"), BorderLayout.NORTH);
        JTextArea contentArea = new JTextArea(post.getContent());
        contentArea.setEditable(false);
        postPanel.add(contentArea, BorderLayout.CENTER);
        
        // 2. 댓글 목록 패널
        JPanel commentListPanel = new JPanel();
        commentListPanel.setLayout(new BoxLayout(commentListPanel, BoxLayout.Y_AXIS));

        List<Comment> comments = commentDAO.getCommentsByPost(post.getPostId());
        
        if (comments.isEmpty()) {
            commentListPanel.add(new JLabel("댓글이 없습니다."));
        } else {
            // 댓글들을 재귀적으로 표시 (답글 포함)
            displayCommentsForDetail(commentListPanel, comments, null, 10, post.getPostId());
        }

        JScrollPane commentScrollPane = new JScrollPane(commentListPanel);
        commentScrollPane.setBorder(null);

        postDetailFrame.add(postPanel, BorderLayout.NORTH);
        postDetailFrame.add(commentScrollPane, BorderLayout.CENTER);
        
        // 3. 닫기 버튼
        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> postDetailFrame.dispose());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(closeBtn);
        postDetailFrame.add(southPanel, BorderLayout.SOUTH);
        
        postDetailFrame.setVisible(true);
    }
    
    // 댓글 / 답글 표시 (상세 화면 전용 재귀 메서드)
    private void displayCommentsForDetail(JPanel parentPanel, List<Comment> comments, String parentId, int indent, String postId) {
        for (Comment c : comments) {
             if ((c.getParentCommentId() == null && parentId == null) || 
                 (c.getParentCommentId() != null && c.getParentCommentId().equals(parentId))) {
                
                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(5, indent, 5, 10)); 
                
                JLabel writerLabel = new JLabel(
                    "<html><b>" + c.getDisplayName() + "</b> <span style='color:gray;'>@" + c.getWriterId() + "</span>:</html>"
                );
                writerLabel.setFont(new Font(writerLabel.getFont().getName(), Font.PLAIN, 12));
                
                JTextArea contentArea = new JTextArea(c.getContent());
                contentArea.setEditable(false);
                contentArea.setBackground(panel.getBackground()); 
                
                // 답글 달기 버튼 (상세 보기에서도 답글 작성 가능)
                JButton replyBtn = new JButton("↩️ 답글");
                replyBtn.setFont(new Font(replyBtn.getFont().getName(), Font.PLAIN, 10));
                replyBtn.addActionListener(ev -> {
                    String replyText = JOptionPane.showInputDialog(postDetailFrame, c.getDisplayName() + "님에게 답글 내용을 입력하세요:");
                    if (replyText != null && !replyText.trim().isEmpty()) { 
                        Comment reply = new Comment(
                                "c" + System.currentTimeMillis(),
                                replyText,
                                loginUser.getUserId(),
                                postId, 
                                0,
                                c.getCommentId(), 
                                loginUser.getDisplayName()
                        );
                        boolean success = commentDAO.addComment(reply);
                        
                        if (success) {
                            // 상세 화면 갱신
                            postDetailFrame.dispose();
                            Post updatedPost = postDAO.getPostById(postId); 
                            
                            if (updatedPost != null) {
                                showPostDetail(updatedPost); 
                            }
                            refreshTimeline(); // 메인 피드 갱신
                        } else {
                             JOptionPane.showMessageDialog(postDetailFrame, "답글 작성 실패!");
                        }
                    }
                });
                
                JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                bottomPanel.add(replyBtn);

                panel.add(writerLabel, BorderLayout.NORTH);
                panel.add(contentArea, BorderLayout.CENTER);
                panel.add(bottomPanel, BorderLayout.SOUTH);
                
                parentPanel.add(panel);

                displayCommentsForDetail(parentPanel, comments, c.getCommentId(), indent + 20, postId);
            }
        }
    }
    
    private void showLikersList(Post post) {
        // PostDAO에 getLikersDisplayName 메서드가 있다고 가정
        List<String> likers = postDAO.getLikersDisplayName(post.getPostId()); 
        
        if (likers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이 게시글에 좋아요를 누른 사람이 없습니다.");
            return;
        }

        String likersText = String.join("\n", likers);
        JTextArea textArea = new JTextArea("좋아요를 누른 사용자:\n" + likersText);
        textArea.setEditable(false);
        
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "좋아요 목록 (" + post.getNumOfLikes() + "명)", JOptionPane.PLAIN_MESSAGE);
    }
}