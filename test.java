/*

    Socket을 이용한 채팅 클라이언트
    2023. 10. 29. Sun

 */

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;

public class test {

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("127.0.0.1", 8888);

        BufferedReader in = null;new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = null;//new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        Chat_UI cui = new Chat_UI(in, out, null);
    }
}