package MyQueue;

import java.io.IOException;
import java.util.Scanner;

public class ProduceClient {
    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);
        System.out.print("输入你的消息：");
        String msg = s.nextLine();
        MqClient client=new MqClient();
        client.produce(msg);
    }
}
