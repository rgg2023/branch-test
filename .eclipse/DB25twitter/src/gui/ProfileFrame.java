package gui;

import dao.UserDAO;
import dao.PostDAO;
import dao.CommentDAO;
import model.User;
import model.Post;
import model.Comment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

public class ProfileFrame extends JFrame {
    private User loggedInUser;
    private UserDAO userDao;
    private PostDAO postDao;
    private CommentDAO commentDao;
    private PostFrame mainPostFrame; // 👈 PostFrame 인스턴스
    
    // 설정 탭에서 사용할 컴포넌트
    private JTextField usernameField, emailField, displayNameField;
    private JTextArea bioArea;
    private JLabel profileImagePreview; // 사진 미리보기 레이블

    public ProfileFrame(User user, PostFrame mainFrame) {
        this.loggedInUser = user;
        this.mainPostFrame = mainFrame;
        this.userDao = new UserDAO();
        this.postDao = new PostDAO(); 
        this.commentDao = new CommentDAO();
        this.mainPostFrame = mainFrame; // 👈 인스턴스 저장

        setTitle("내 프로필 - " + user.getDisplayName());
        setSize(500, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. 프로필 정보 및 통계 영역 (NORTH)
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // 2. 탭 영역 (CENTER)
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("📝 내 트윗", new JScrollPane(createMyPostsPanel()));
        tabbedPane.addTab("⚙️ 설정", createSettingsPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // 3. 닫기 버튼
        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> dispose());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(closeBtn);
        add(southPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
    
    // ... (createHeaderPanel, createMyPostsPanel 등의 메서드 구현은 이전과 동일합니다.) ...
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 1. 프로필 이미지 영역
        JLabel profileImageLabel = new JLabel();
        profileImageLabel.setPreferredSize(new Dimension(80, 80)); 
        profileImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        profileImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        if (loggedInUser.getProfileImagePath() != null && !loggedInUser.getProfileImagePath().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(loggedInUser.getProfileImagePath());
                Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                profileImageLabel.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                profileImageLabel.setText("📷");
            }
        } else {
            profileImageLabel.setText("👤"); 
        }
        
        // 2. 사용자 이름/ID/Bio
        JPanel userInfoPanel = new JPanel(new GridLayout(3, 1));
        userInfoPanel.setBorder(new EmptyBorder(0, 5, 0, 0)); 

        JLabel nameLabel = new JLabel(loggedInUser.getDisplayName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        JLabel idLabel = new JLabel("@" + loggedInUser.getUserId(), SwingConstants.LEFT);
        idLabel.setForeground(Color.GRAY);
        JTextArea bioArea = new JTextArea(loggedInUser.getBio() != null ? loggedInUser.getBio() : "자기소개 없음");
        bioArea.setEditable(false);
        bioArea.setBackground(header.getBackground());
        bioArea.setLineWrap(true);
        
        userInfoPanel.add(nameLabel);
        userInfoPanel.add(idLabel);
        userInfoPanel.add(bioArea);
        
        JPanel topRowPanel = new JPanel(new BorderLayout());
        topRowPanel.add(profileImageLabel, BorderLayout.WEST);
        topRowPanel.add(userInfoPanel, BorderLayout.CENTER);
        
        header.add(topRowPanel, BorderLayout.NORTH);

        // 3. 통계 및 생성일
        JPanel statPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월");
        String createdDate = sdf.format(loggedInUser.getCreatedAt());
        JLabel dateLabel = new JLabel("가입일: " + createdDate);
        dateLabel.setForeground(Color.DARK_GRAY);
        
        int followingCount = userDao.getFollowingCount(loggedInUser.getUserId());
        int followerCount = userDao.getFollowerCount(loggedInUser.getUserId());
        int postCount = userDao.getPostCount(loggedInUser.getUserId());
        
        JLabel followingLabel = new JLabel("<html><b>" + followingCount + "</b> <span style='color:gray'>팔로잉</span></html>");
        JLabel followerLabel = new JLabel("<html><b>" + followerCount + "</b> <span style='color:gray'>팔로워</span></html>");
        JLabel postCountLabel = new JLabel("<html><b>" + postCount + "</b> <span style='color:gray'>트윗</span></html>"); 
        
        statPanel.add(dateLabel);
        statPanel.add(new JLabel(""));
        statPanel.add(followingLabel);
        statPanel.add(followerLabel);
        statPanel.add(postCountLabel);
        statPanel.add(new JLabel("")); 

        header.add(statPanel, BorderLayout.CENTER);
        
        return header;
    }
    
    private JPanel createMyPostsPanel() {
        // ... (내 트윗 목록 패널 생성 로직 - PostFrame과 유사) ...
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        List<Post> posts = postDao.getPostsByUserId(loggedInUser.getUserId());
        
        if (posts.isEmpty()) {
            panel.add(Box.createVerticalStrut(20));
            JLabel emptyLabel = new JLabel("작성한 게시글이 없습니다.", SwingConstants.CENTER);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(emptyLabel);
            panel.add(Box.createVerticalGlue());
        } else {
            for (Post p : posts) {
                JPanel postItem = new JPanel(new BorderLayout(5, 5));
                postItem.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 5, 10)
                ));
                
                postItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                postItem.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        mainPostFrame.showPostDetail(p); // 메인 프레임의 상세 보기 기능 호출
                    }
                });

                JTextArea content = new JTextArea(p.getContent());
                content.setEditable(false);
                content.setLineWrap(true);
                content.setBackground(postItem.getBackground());
                
                // 하단 상호작용 영역 (좋아요, 답글 수)
                JPanel interactionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
                
                // 좋아요 버튼 및 레이블 (단순 표시용)
                JLabel likeLabel = new JLabel(p.getNumOfLikes() + " ❤️");
                JLabel commentCountLabel = new JLabel("💬 답글 " + p.getNumOfComments()); 
                
                interactionPanel.add(likeLabel);
                interactionPanel.add(commentCountLabel);
                
                postItem.add(content, BorderLayout.CENTER);
                postItem.add(interactionPanel, BorderLayout.SOUTH);
                
                panel.add(postItem);
            }
        }
        return panel;
    }
    
    private JPanel createSettingsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // 1. 프로필 사진 미리보기 및 선택
        profileImagePreview = new JLabel();
        profileImagePreview.setPreferredSize(new Dimension(80, 80)); 
        profileImagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JButton selectImageBtn = new JButton("프로필 사진 선택...");
        selectImageBtn.addActionListener(e -> selectProfileImage());
        
        JPanel imageControlPanel = new JPanel(new BorderLayout(10, 0));
        imageControlPanel.add(profileImagePreview, BorderLayout.WEST);
        imageControlPanel.add(selectImageBtn, BorderLayout.CENTER);
        
        // 초기 이미지 로드 (loggedInUser의 현재 경로 사용)
        updateImagePreview(loggedInUser.getProfileImagePath());

        // 2. 폼 패널
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        
        usernameField = new JTextField(loggedInUser.getUsername());
        emailField = new JTextField(loggedInUser.getEmail());
        displayNameField = new JTextField(loggedInUser.getDisplayName());
        
        formPanel.add(new JLabel("사용자 ID:"));
        formPanel.add(new JLabel(loggedInUser.getUserId())); 
        
        formPanel.add(new JLabel("Username (고유):"));
        formPanel.add(usernameField); 
        
        formPanel.add(new JLabel("이메일:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("표시 이름 (닉네임):"));
        formPanel.add(displayNameField);

        // Bio 영역
        bioArea = new JTextArea(5, 20);
        bioArea.setText(loggedInUser.getBio() != null ? loggedInUser.getBio() : "");
        bioArea.setLineWrap(true);
        JScrollPane bioScrollPane = new JScrollPane(bioArea);
        
        JPanel bioPanel = new JPanel(new BorderLayout(0, 5));
        bioPanel.add(new JLabel("자기소개 (Bio):"), BorderLayout.NORTH);
        bioPanel.add(bioScrollPane, BorderLayout.CENTER);
        
        // 폼 통합
        JPanel formContainer = new JPanel(new BorderLayout(10, 10));
        formContainer.add(imageControlPanel, BorderLayout.NORTH);
        formContainer.add(formPanel, BorderLayout.CENTER);

        mainPanel.add(formContainer, BorderLayout.NORTH);
        mainPanel.add(bioPanel, BorderLayout.CENTER);
        
        // 버튼 패널
        JButton saveBtn = new JButton("프로필 정보 저장");
        JButton changePwdBtn = new JButton("비밀번호 변경"); 
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveBtn);
        buttonPanel.add(changePwdBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        saveBtn.addActionListener(e -> saveProfile());
        changePwdBtn.addActionListener(e -> changePassword());
        
        return mainPanel;
    }
    
    private void updateImagePreview(String path) {
        if (path != null && !path.isEmpty() && new File(path).exists()) {
             try {
                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                profileImagePreview.setIcon(new ImageIcon(img));
                profileImagePreview.setText("");
            } catch (Exception e) {
                profileImagePreview.setText("📷");
                profileImagePreview.setIcon(null);
            }
        } else {
            profileImagePreview.setText("👤");
            profileImagePreview.setIcon(null);
        }
    }
    
    private void selectProfileImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("프로필 사진 선택");
        // 이미지 파일 필터 추가는 생략
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            loggedInUser.setProfileImagePath(path); // 모델에 경로 임시 저장
            updateImagePreview(path); // 미리보기 갱신
        }
    }
    
    private void saveProfile() {
        // User 객체의 필드 업데이트
        loggedInUser.setUsername(usernameField.getText().trim());
        loggedInUser.setEmail(emailField.getText().trim());
        loggedInUser.setDisplayName(displayNameField.getText().trim());
        loggedInUser.setBio(bioArea.getText().trim());
        // profileImagePath는 selectProfileImage에서 이미 모델에 저장됨
        
        boolean success = userDao.updateProfile(loggedInUser);

        if (success) {
            JOptionPane.showMessageDialog(this, "✅ 프로필이 성공적으로 업데이트되었습니다.");
            
            // 메인 피드 갱신
            if (mainPostFrame != null) {
                mainPostFrame.refreshTimeline();
            }
            dispose();
            new ProfileFrame(loggedInUser, mainPostFrame); // 갱신된 정보로 프레임 다시 열기
        } else {
            JOptionPane.showMessageDialog(this, "❌ 프로필 업데이트 실패. Username 중복 등을 확인하세요.");
        }
    }

    private void changePassword() {
        // 기존 비밀번호 변경 로직 (생략)
        // ...
        // 성공 시:
        // if (success) {
        //     JOptionPane.showMessageDialog(this, "✅ 비밀번호가 성공적으로 변경되었습니다.");
        //     loggedInUser.setPwd(newPwd); 
        //     if (mainPostFrame != null) mainPostFrame.refreshTimeline();
        // }
    }
    
    private void replyToPost(Post parentPost) {
        // ... (내 트윗 목록에서 답글 다는 로직은 ProfileFrame에서만 사용되므로 그대로 유지) ...
        // ... (성공 시) ...
        // if (success) {
        //     dispose();
        //     new ProfileFrame(loggedInUser, mainPostFrame);
        // }
    }
}