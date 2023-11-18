/*

    Socket을 이용한 채팅 클라이언트
    2023. 10. 29. Sun ~

 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ChatClient {

    public static void main(String[] args) throws Exception {
        System.setProperty("file.encoding","UTF-8");
        Login_UI ui = new Login_UI(); // 로그인 UI를 연다.
    }

    public static class ClientThread extends Thread {
        private Login_UI ui;
        private Socket socket;

        public ClientThread(Login_UI ui, Socket socket) {
            this.ui = ui;
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "EUC-KR"));

                String msg;
                // 메시지를 받을 때마다
                while (socket.isConnected() && (msg = in.readLine()) != null) {
                    ui.sendMessage(msg); // 메시지를 출력한다
                }

            } catch(Exception e) {
                System.out.println("예외 발생: " + e.getMessage());
                e.printStackTrace();
            }

        }

    }
}
