package org.example.server;

import org.example.entity.MessageInfo;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketService extends JFrame implements Runnable {
    /**
     * 服务器监听的端口号
     */
    private static final int PORT=9999;
    /**
     * 消息生成的数量
     */
    private static final int NUM=100;
    /**
     * 服务器向客户端推送消息的间隔
     */
    private static final int SLEEP_TIME=1000;
    /**
     * 客户端编号
     */
    private static  int CLIENT_NO=1;
    private static  int WIDTH=100;
    private static  int HEIGHT=100;
    private static  int X=10;
    private static  int Y=10;

    private Map<Socket,MessageInfo> maps=new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        SocketService socketService=new SocketService();
        socketService.init();
        new Thread(socketService).start();
    }
    private void init(){
        this.setTitle("服务端");
        this.setLayout(null);
        this.setBounds(400,200,1200, 600);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        new Thread(()->{
            while (true){
                try {
                    HEIGHT=100;
                    X=10;
                    Y=10;
                    this.getContentPane().removeAll();
                    System.out.println("在线用户："+maps);
                    if(!maps.values().isEmpty()){
                        int i=1;
                        for (MessageInfo messageInfo : maps.values()) {
                            String body = messageInfo.getBody();
                            JButton label = new JButton(body);
                            if(messageInfo.getStatus()==1){
                                label.setBackground(new Color(0,255,0));
                            }else{
                                label.setBackground(new Color(255,0,0));
                            }
                            label.setFont(new Font(Font.SERIF,Font.BOLD,30));
                            label.setBounds(X,Y,WIDTH,HEIGHT);
                            if(i%5==0){
                                X=10;
                                Y+=HEIGHT+30;
                            }else{
                                X+=WIDTH+30;
                            }
                            this.add(label);
                            this.add(new Label(body));
                            i++;
                        }
                    }else{
                        for (int i = 1; i <=15; i++) {
                            JButton label = new JButton(String.valueOf(i));
                            label.setBackground(new Color(255,0,0));
                            label.setFont(new Font(Font.SERIF,Font.BOLD,30));
                            label.setBounds(X,Y,WIDTH,HEIGHT);
                            if(i%5==0){
                                X=10;
                                Y+=HEIGHT+30;
                            }else{
                                X+=WIDTH+30;
                            }
                            this.add(label);
                        }


                    }
                    this.repaint();

                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();


    }
    @Override
    public void run() {
        try {
            ServerSocket serverSocket=new ServerSocket(PORT);
            System.out.println("服务器启动成功等待客户端连接....");
            while (true){
                Socket accept = serverSocket.accept();
                new Thread(() -> {
                    while (!Thread.currentThread().isInterrupted()){
                        try {
                            InputStream inputStream = accept.getInputStream();
                            ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
                            MessageInfo messageInfo=(MessageInfo)objectInputStream.readObject();
                            maps.put(accept,messageInfo);
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    MessageInfo messageInfo = maps.get(accept);
                    messageInfo.setStatus(0);
                },"read—"+CLIENT_NO).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
