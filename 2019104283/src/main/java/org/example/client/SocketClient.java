package org.example.client;


import org.example.entity.MessageInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketClient extends JFrame {
    /**
     * 服务器监听的端口号
     */
    private static final int PORT=9999;
    /**
     * 服务器的ip
     */
    private static final String IP="127.0.0.1";
    private Socket socket= null;
    public static void main(String[] args) throws Exception {
        SocketClient socketClient = new SocketClient();
        socketClient.init();

    }
    private void init() throws IOException {


        JTextField jTextField = new JTextField();
        jTextField.setBounds(90,10,120,30);
        this.add(jTextField);

        JLabel jLabel=new JLabel("客户端昵称：");
        jLabel.setBounds(10,10,120,30);
        this.add(jLabel);

        JButton jButtonStatus=new JButton(jTextField.getText());
        jButtonStatus.setBounds(80,100,100,100);
        jButtonStatus.setBackground(new Color(255,0,0));
        this.add(jButtonStatus);

        JButton jButton=new JButton("登录");
        jButton.setBounds(220,10,60,30);
        jButton.addActionListener(e -> {
            try {
                if(socket==null||socket.isClosed()){
                    if(jTextField.getText().isEmpty()){
                        JOptionPane.showMessageDialog(this,"请输入名称！");
                    }else{
                        socket = new Socket(IP,PORT);
                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        MessageInfo messageInfo = new MessageInfo();
                        messageInfo.setBody(jTextField.getText());
                        messageInfo.setStatus(1);
                        outputStream.writeObject(messageInfo);
                        jButtonStatus.setBackground(new Color(0,255,0));
                        jButtonStatus.setText(jTextField.getText());
                    }
                }else{
                    JOptionPane.showMessageDialog(this,"您已经连接服务器了不需要再登录！");
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        this.add(jButton);




        this.setTitle("客户端");
        this.setLayout(null);
        this.setBounds(800,300,400, 300);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);


    }
}
