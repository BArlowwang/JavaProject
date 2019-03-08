package real;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class server implements Runnable {// 服务端
    static List<Socket> socketList = new ArrayList<Socket>();
    // 读取 In
    static Socket socket = null;
    static ServerSocket serverSocket = null;

    public server() {// 构造方法
        try {
            serverSocket = new ServerSocket(9999);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //创建客户端窗口对象
        serverFrame sframe = new serverFrame();
        //窗口关闭键无效，必须通过退出键退出客户端以便善后
        sframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //获取本机屏幕横向分辨率
        int w = Toolkit.getDefaultToolkit().getScreenSize().width;
        //获取本机屏幕纵向分辨率
        int h = Toolkit.getDefaultToolkit().getScreenSize().height;
        //将窗口置中
        sframe.setLocation((w - sframe.WIDTH) / 2, (h - sframe.HEIGHT) / 2);
        //设置客户端窗口为可见
        sframe.setVisible(true);
       // Scanner input = new Scanner(System.in);
        sframe.logArea.append("                   ************服务端*************\n端口9999等待被连接......\n");
       server t = new server();
        int count = 0;
        while (true) {
            try {
                socket = serverSocket.accept();
                count++;
                sframe.logArea.append("第" + count + "个客户已连接");
                socketList.add(socket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Print p = new Print(socket);
            Thread read = new Thread(t);
            Thread print = new Thread(p);
            read.start();
            print.start();
        }
    }

    @Override
    public void run() {
        // 重写run方法
        try {
            Thread.sleep(1000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
            while (true) {
                String jieshou = in.readLine();
                System.out.println(jieshou);
                for (int i = 0; i < socketList.size(); i++) {
                    Socket socket = socketList.get(i);
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    if (socket != this.socket) {
                        out.println(jieshou);
                    } else {
                        out.println("(你)" + jieshou);
                    }
                    out.flush();
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}

class Print implements Runnable {
    static List<Socket> socketList = new ArrayList<Socket>();
    Scanner input = new Scanner(System.in);

    public Print(Socket s) {// 构造方法
        try {
            socketList.add(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            while (true) {
                String msg = input.next();
                for (int i = 0; i < socketList.size(); i++) {
                    Socket socket = socketList.get(i);
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    // System.out.println("对客户端说：");
                    out.println("服务端说：" + msg);
                    out.flush();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}

class serverFrame extends JFrame {
    //时间显示格式
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    //窗口宽度
    final int WIDTH = 650;
    //窗口高度
    final int HEIGHT = 750;
    JTextArea logArea = new JTextArea();
    JScrollPane logWindow = new JScrollPane(logArea);
    JButton clsBtn = new JButton("清屏");
    JButton exitBtn = new JButton("关闭");

    public serverFrame() {
        //标题
        setTitle("服务器日志");
        setSize(WIDTH, HEIGHT); //设置大小
        //不可缩放
        setResizable(false);
        //设置布局:不适用默认布局，完全自定义
        setLayout(null);
        clsBtn.setBounds(150, 660, 150, 50);
        // clsBtn.setEnabled(true);
        exitBtn.setBounds(350, 660, 150, 50);
        logArea.setLineWrap(true);
        logArea.setSize(600, 600);
        Insets s = new Insets(20, 20, 20, 20);
        logArea.setMargin(s);
        //日志框不可编辑，只用来显示
        logArea.setEditable(false);
        //设置日志显示字体
        logArea.setFont(new Font("楷体", Font.BOLD, 16));
        //设置滚动窗的水平滚动条属性:不出现
        logWindow.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //设置滚动窗的垂直滚动条属性:需要时自动出现
        logWindow.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //设置滚动窗大小和位置
        logWindow.setBounds(0, 0, 650, 650);
        //添加聊天窗口的滚动窗
        this.add(logWindow);
        this.add(exitBtn);
        this.add(clsBtn);
//添加退出按钮的响应事件
        exitBtn.addActionListener
                (new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        try {
                            //向服务器发送退出信息
                            //    OutputStream out = Client1.s.getOutputStream();
                            //     out.write("Exit/".getBytes());
                            //退出
                            System.exit(0);
                        } catch (Exception e) {
                        }
                    }
                });
        clsBtn.addActionListener
                (new ActionListener()
                 {
                     @Override
                     public void actionPerformed(ActionEvent event)
                     {
                         //聊天框清屏
                         logArea.setText("");
                     }
                 }
                );
    }
}