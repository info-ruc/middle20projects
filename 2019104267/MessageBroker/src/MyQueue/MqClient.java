package MyQueue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MqClient {
    //生产者
    @SuppressWarnings("unused")
    public static void produce(String message) throws IOException {
        @SuppressWarnings("resource")
        Socket socket = new Socket(InetAddress.getLocalHost(),BrokerServer.SERVER_PORT);
        PrintWriter out=new PrintWriter(socket.getOutputStream());
        out.println(message);
        out.flush();
    }

    //消费消息ss
    public static String consume() throws UnknownHostException, IOException{
        @SuppressWarnings("resource")
        Socket socket = new Socket(InetAddress.getLocalHost(),BrokerServer.SERVER_PORT);
        BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out=new PrintWriter(socket.getOutputStream());
        //先向消息列队发送字符串“CONSUME”表示消费
        out.println("CONSUME");
        out.flush();
        //再从消息列队获取一条消息
        String message=in.readLine();
        return message;
    }
}
