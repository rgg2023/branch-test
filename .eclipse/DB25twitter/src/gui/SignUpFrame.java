package gui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;

public class SignUpFrame extends JFrame {
    private JTextField userIdField;
    private JPasswordField passwordField;
    private JTextField usernameField;
    private JTextField emailField;
    private JTextField displayNameField;
    private JTextArea bioArea;

    private UserDAO userDao = new UserDAO();

    public SignUpFrame() {
        setTitle("Twitter Clone - 회원가입");
        setSize(400, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); 

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10)); 

        userIdField = new JTextField(20);
        passwordField = new JPasswordField(20);
        usernameField = new JTextField(20);
        emailField = new JTextField(20);
        displayNameField = new JTextField(20);
        
        bioArea = new JTextArea(3, 20);
        bioArea.setLineWrap(true);
        JScrollPane bioScrollPane = new JScrollPane(bioArea);

        formPanel.add(new JLabel("사용자 ID (로그인 ID):"));
        formPanel.add(userIdField);
        formPanel.add(new JLabel("비밀번호:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Username (DB ID):"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("이메일:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("표시 이름 (닉네임):"));
        formPanel.add(displayNameField);

        JPanel bioPanel = new JPanel(new BorderLayout(0, 5));
        bioPanel.add(new JLabel("자기소개 (Bio, 선택 사항):"), BorderLayout.NORTH);
        bioPanel.add(bioScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton registerBtn = new JButton("가입 완료");
        JButton cancelBtn = new JButton("취소");
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(bioPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        registerBtn.addActionListener(this::registerAction);
        cancelBtn.addActionListener(e -> cancelAction());

        add(mainPanel);
        setVisible(true);
    }

    private void registerAction(ActionEvent e) {
        String userId = userIdField.getText().trim();
        String pwd = new String(passwordField.getPassword());
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String displayName = displayNameField.getText().trim();
        String bio = bioArea.getText().trim();

        if (userId.isEmpty() || pwd.isEmpty() || username.isEmpty() || email.isEmpty() || displayName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ 모든 필수 필드를 채워주세요.");
            return;
        }

        User newUser = new User(
                userId,
                pwd, 
                username,
                email,
                displayName,
                bio,
                new Timestamp(System.currentTimeMillis()),
                null // 👈 새로 추가: 가입 시 프로필 사진 경로 없음
            );
        boolean success = userDao.register(newUser);

        if (success) {
            JOptionPane.showMessageDialog(this, "🎉 회원가입이 성공적으로 완료되었습니다! 로그인해 주세요.");
            new LoginFrame();
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "❌ 회원가입 실패. 사용자 ID나 Username이 이미 존재할 수 있습니다.");
        }
    }
    
    private void cancelAction() {
        new LoginFrame();
        this.dispose();
    }
}