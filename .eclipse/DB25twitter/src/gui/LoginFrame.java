package gui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    private UserDAO userDao = new UserDAO();

    public LoginFrame() {
        setTitle("Twitter Clone - Login");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); 

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10)); 
        
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0)); 

        JButton loginBtn = new JButton("로그인");
        JButton signUpBtn = new JButton("회원가입"); 
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(signUpBtn);

        loginBtn.addActionListener(this::loginAction);
        signUpBtn.addActionListener(e -> signUpAction());

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        setVisible(true);
    }

    private void loginAction(ActionEvent e) {
        String username = usernameField.getText();
        String pwd = new String(passwordField.getPassword());

        User u = userDao.login(username, pwd);

        if (u != null) {
            JOptionPane.showMessageDialog(this, "✅ 로그인 성공! 닉네임: " + u.getDisplayName());
            new PostFrame(u); 
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "❌ 로그인 실패! 사용자 이름 또는 비밀번호를 확인하세요.");
        }
    }
    
    private void signUpAction() {
        new SignUpFrame();
        this.dispose(); 
    }
}