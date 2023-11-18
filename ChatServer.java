/*

    Socket을 이용한 채팅 서버
    2023. 10. 29. Sun ~

 */

// -*- coding: utf-8 -*-'

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.sql.*;
import java.util.*;

public class ChatServer {
    private static int port = 8888;
    private static List<PrintWriter> outlist = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws Exception {
        System.setProperty("file.encoding","UTF-8");
        ServerSocket serverSocket = new ServerSocket(port); // 설정한 포트를 가지고 서버 소켓 ON
        System.out.printf("Server Opended - port %d%n", port);

        while(true) {
            Socket socket = serverSocket.accept(); // 클라이언트가 접속할 때마다 소켓 생성

            // 연결된 클라이언트의 IP를 구함
            InetAddress clientAddress = socket.getInetAddress();
            String IP = clientAddress.getHostAddress();
            System.out.printf("Client Connected - IP %s%n", IP);

            // 클라이언트가 연결되면 멀티 쓰레드로 동작하게 함
            ChatThread ct = new ChatThread(socket, outlist);
            ct.start();
        }

    }

    public static class ChatThread extends Thread {
        private Socket socket;
        private List<PrintWriter> outlist;

        public ChatThread(Socket socket, List outlist) {
            this.socket = socket;
            this.outlist = outlist; // 모든 쓰레드가 outlist라는 똑같은 객체를 공유하게 한다.
        }

        public void run() {
            List<PrintWriter> tmp_outlist = outlist; // 접속이 종료될 때를 대비.

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "EUC-KR"));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "EUC-KR"));

                // 연결될 때마다 그 클라이언트의 PrintWriter을 리스트에 담아줌
                outlist.add(out);

                // DB 연결
                Connection con = null;
                ResultSet rs;
                PreparedStatement psmt;
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    String url = "jdbc:mysql://localhost:3306/chat";
                    String id = "root";
                    String pw = "root";
                    con = DriverManager.getConnection(url, id, pw);
                } catch (SQLException e4) {
                    System.out.println("DB Password Error.");
                    e4.printStackTrace();
                } catch (ClassNotFoundException e5) {
                    System.out.println("DB Connection Error.");
                    e5.printStackTrace();
                }

                String msg = null;
                while ((msg = in.readLine()) != null) {
                    switch(msg.substring(0, 1)) {
                        case "0":
                            String user_id = in.readLine();
                            String user_pass = in.readLine();
                            try {
                                String sql = "select nickname from user where id = ? AND password = ?";

                                psmt = con.prepareStatement(sql);
                                psmt.setString(1, user_id);
                                psmt.setString(2, user_pass);
                                rs = psmt.executeQuery();

                                String nickname = null;
                                while(rs.next())
                                    nickname = rs.getString("nickname"); // 로그인한 id의 닉네임을 가져온다

                                if (nickname == null) { // 닉네임 검색 실패 -> 로그인 실패 시
                                    out.println("SQLERROR");
                                    out.flush();
                                } else { // 로그인 성공 시
                                    out.println(nickname);
                                    out.flush();

                                    rs.close();
                                    psmt.close();
                                    con.close();
                                }
                            } catch (Exception e2) {
                                System.out.println("SQL Connection Error.");
                                e2.printStackTrace();
                            }
                            break;
                        case "1":
                            String user_idreg = in.readLine();
                            String user_passreg = in.readLine();
                            String user_nickreg = in.readLine();

                            try {
                                String sql = "insert into user values(?, ?, ?)";
                                psmt = con.prepareStatement(sql);
                                psmt.setString(1, user_idreg);
                                psmt.setString(2, user_passreg);
                                psmt.setString(3, user_nickreg);
                                psmt.executeUpdate();

                                psmt.close();
                                con.close();

                                out.println("Register Completed");
                                out.flush();
                            } catch (Exception er) {
                                out.println("Duplicated ID");
                                out.flush();
                            }
                            break;
                        case "2": // 메시지 전송
                            // 어느 한 클라이언트에서 메시지를 보내면
                            String sending_msg = in.readLine();
                            for (int i = 0; i < outlist.size(); i++) { // 연결된 모든 클라이언트의 PrintWriter을 통해 메시지를 전달한다.
                                PrintWriter tmp_out = outlist.get(i);
                                tmp_out.println(sending_msg);
                                tmp_out.flush();
                            }
                            break;
                    }
                }
            } catch(Exception e) {
                //System.out.println("예외 발생: " + e.getMessage());
                //e.printStackTrace();
            } finally { // 한 클라이언트가 접속이 종료되면
                outlist = tmp_outlist; // 자기 자신을 outlist에서 제외
                System.out.println("누군가가 접속을 종료하였습니다.");
            }

        }

    }
}

