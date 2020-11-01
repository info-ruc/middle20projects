package MyQueue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class BrokerServer implements Runnable {
    public static int SERVER_PORT=9999;
    private final Socket socket;
    public BrokerServer(Socket socket){
        this.socket=socket;
    }

    @Override
    public void run(){
        try {
            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out=new PrintWriter(socket.getOutputStream());
            while(true){
                String str=in.readLine();
                if (str==null) {
                    continue;
                }
                System.out.println("接受到原始数据："+str);

                if (str.equals("CONSUME")) {//consume表示需要消费一条消息
                    String meString=Broker.consume();
                    out.println(meString);
                    out.flush();
                }else{
                    //其他情况都表示生产消息放到消息队列中
                    Broker.produce(str);
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(SERVER_PORT);
        while (true) {
            BrokerServer borkerServer=new BrokerServer(server.accept());
            new Thread(borkerServer).start();
        }
    }

}
