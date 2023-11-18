/*

    Socket을 이용한 채팅 클라이언트 UI
    2023. 10. 29. Sun ~

 */

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;

class Reg_UI extends JFrame { // 회원가입 UI
    public BufferedReader in;
    public PrintWriter out;

    public void set_UI() {
        // ID 입력창
        JLabel id_label = new JLabel("ID : ");
        id_label.setBounds(56, 15, 80, 40);
        add(id_label);
        JTextField id_text = new JTextField();
        id_text.setBounds(80, 22, 100, 30);
        add(id_text);

        // Password 입력창
        JLabel password_label = new JLabel("Password : ");
        password_label.setBounds(10, 60, 80, 40);
        add(password_label);
        JTextField password_text = new JTextField();
        password_text.setBounds(80, 67, 100, 30);
        add(password_text);

        // Nickname 입력창
        JLabel nick_label = new JLabel("Nickname : ");
        nick_label.setBounds(12, 105, 80, 40);
        add(nick_label);
        JTextField nick_text = new JTextField();
        nick_text.setBounds(80, 112, 100, 30);
        add(nick_text);

        // 회원가입 버튼
        JButton Reg_but = new JButton("Register");
        Reg_but.setBounds(23, 157, 150, 40);
        add(Reg_but);

        this.setTitle("Login");
        this.setSize(210, 250);
        this.setLayout(null);

        this.setVisible(true);

        // 회원가입 버튼 눌렀을 때
        Reg_but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String user_id = id_text.getText();
                String user_pass = password_text.getText();
                String user_nick = nick_text.getText();
                String reg_msg = "1\r\n" + user_id + "\r\n" + user_pass + "\r\n" + user_nick;

                if (user_id.length() < 1 || user_pass.length() < 1 || user_nick.length() < 1) {
                    JOptionPane.showMessageDialog(null, "Fill in all the blanks", "Log in Error", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                out.println(reg_msg);
                out.flush();
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        JOptionPane.showMessageDialog(null, msg, "Register", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                } catch (Exception er) {
                    System.out.println("Register Connection Error");
                }
            }
        });
    }

    public Reg_UI(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
        set_UI();
    }
}

class Login_UI extends JFrame { // 로그인 UI

    Chat_UI cui;
    Socket socket;

    public BufferedReader in;
    public PrintWriter out;

    public Socket getSocket() {
        return socket;
    }

    public void set_UI(){
        // ID 입력창
        JLabel id_label = new JLabel("ID : ");
        id_label.setBounds(84, 30, 80, 40);
        add(id_label);
        JTextField id_text = new JTextField();
        id_text.setBounds(120, 37, 100, 30);
        add(id_text);

        // Password 입력창
        JLabel password_label = new JLabel("Password : ");
        password_label.setBounds(40, 80, 80, 40);
        add(password_label);
        JTextField password_text = new JTextField();
        password_text.setBounds(120, 87, 100, 30);
        add(password_text);

        // IP 입력창
        JTextField IP_text = new JTextField();
        IP_text.setBounds(40, 130, 120, 30);
        IP_text.setText("127.0.0.1");
        IP_text.setHorizontalAlignment(JTextField.CENTER);
        add(IP_text);

        // Port 입력창
        JTextField Port_text = new JTextField();
        Port_text.setBounds(165, 130, 55, 30);
        Port_text.setText("8888");
        Port_text.setHorizontalAlignment(JTextField.CENTER);
        add(Port_text);

        // 회원가입 버튼
        JButton Reg_but = new JButton("Register");
        Reg_but.setBounds(35, 180, 90, 30);
        add(Reg_but);

        // 로그인 버튼
        JButton Login_but = new JButton("Login");
        Login_but.setBounds(135, 180, 90, 30);
        add(Login_but);

        this.setTitle("Login");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 320);
        this.setLayout(null);

        this.setVisible(true);

        // 버튼 누르는 action
        // 로그인 버튼
        Login_but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String user_id = id_text.getText();
                String user_pass = password_text.getText();
                String login_msg = "0\r\n" + user_id + "\r\n" + user_pass;

                String ip = IP_text.getText();
                int port = Integer.parseInt(Port_text.getText());

                try {
                    socket = new Socket(ip, port);

                    // 소켓에 연결 후 쓰고 읽을 버퍼 클래스들 선언
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "EUC-KR"));
                    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "EUC-KR"));
                } catch (Exception er) {
                    System.out.println("Socket Connection Error");
                }

                // 채팅이 시작되면 채팅 작업을 멀티 스레드로 동작시킨다.
                ChatClient.ClientThread ct = new ChatClient.ClientThread(Login_UI.this, socket); // 소켓에 연결되면 쓰레드로 동작하게 한다
                ct.start();

                out.println(login_msg);
                out.flush();
                try {
                    String nickname;
                    while ((nickname = in.readLine()) != null) {
                        if (nickname.equals("SQLERROR")) {
                            JOptionPane.showMessageDialog(null, "Please check the id or password.", "Log in Error", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        } else {
                            cui = new Chat_UI(in, out, nickname); // 채팅 UI를 오픈한다.
                            Login_UI.this.dispose();
                            return;
                        }
                    }
                } catch (Exception er) {
                    System.out.println("Login Connection Error");
                }
            }
        });

        // 회원가입 버튼을 누르면
        Reg_but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String ip = IP_text.getText();
                int port = Integer.parseInt(Port_text.getText());

                try {
                    socket = new Socket(ip, port);

                    // 소켓에 연결 후 쓰고 읽을 버퍼 클래스들 선언
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "EUC-KR"));
                    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "EUC-KR"));
                } catch (Exception er) {
                    System.out.println("Socket Connection Error");
                }

                Reg_UI rui = new Reg_UI(in, out); // 회원가입 UI를 연다.
            }
        });
    }

    // 서버로부터 메시지를 브로드캐스팅 받을 때
    public void sendMessage(String msg) {
        cui.textArea.append ( msg + "\n");
    }

    public Login_UI() {
        set_UI();
    }
}

class Chat_UI extends JFrame implements ActionListener { // 채팅 UI
    private BufferedReader in;
    PrintWriter out;
    private String name;

    // 문자열 입력창
    public  JTextField textField;
    // 출력 UI
    public JTextArea textArea;

    public void set_UI() {
        this.setTitle("Chat");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 500);

        textArea = new JTextArea(20, 40); // 20행, 40열 크기의 JTextArea 생성
        textArea.setEditable(false);

        // JScrollPane 추가
        JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane);

        // 스크롤바를 항상 아래로 유지
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        textField = new JTextField(30);
        textField.addActionListener(this);
        add(textField, BorderLayout.PAGE_END);

        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = textField.getText();
        textField.setText("");

        try {
            //String sdm = new String(msg.getBytes("EUC-KR"), "EUC-KR");
            out.println("2\r\n" + name + " : " + msg);
            out.flush();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public Chat_UI(BufferedReader in, PrintWriter out, String name) {
        this.in = in;
        this.out = out;
        this.name = name;
        set_UI();
    }
}

public class ChatUI {
    public static void main(String[] args) {
        System.setProperty("file.encoding","UTF-8");
    }
}
