package MyQueue;

import java.io.IOException;
import java.net.UnknownHostException;
public class ConsumeClient {
    public static void main(String[] args) throws UnknownHostException, IOException {
        MqClient mq=new MqClient();
        String mes=mq.consume();
        System.out.println("获取的消息为："+mes);
    }
}
