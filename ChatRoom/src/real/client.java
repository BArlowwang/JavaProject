package real;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class client implements Runnable {// 客户端
    static Socket socket = null;
    static String name = null;
    Scanner input = new Scanner(System.in);
    static String  reciverName="group";
    public static void main(String[] args) {
        clientFrame clientFrame = new clientFrame();
        //窗口关闭键无效，必须通过退出键退出客户端以便善后
        clientFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //获取本机屏幕横向分辨率
        int w = Toolkit.getDefaultToolkit().getScreenSize().width;
        //获取本机屏幕纵向分辨率
        int h = Toolkit.getDefaultToolkit().getScreenSize().height;
        //将窗口置中
        clientFrame.setLocation((w - clientFrame.WIDTH) / 2, (h - clientFrame.HEIGHT) / 2);
        //设置客户端窗口为可见
        clientFrame.setVisible(true);
        int x = (int) (Math.random() * 100);
        client.name = "client" + x;
       // System.out.println("************客户端" + x + "*************");
        try {
            socket = new Socket("127.0.0.1", 9999);
            clientFrame.jtaChat.append("已经连上服务器了\n");
        } catch (Exception e) {
          //  e.printStackTrace();
            clientFrame.jtaChat.append("服务器未连接！\n");
        }
        client t = new client();
        Read r = new Read(socket);
        Thread print = new Thread(t);
        Thread read = new Thread(r);
        print.start();
        read.start();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            while (true) {
                String msg = input.next();
                out.println(name + "说:" + msg);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Read implements Runnable {
    static Socket socket = null;

    public Read(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
            while (true) {
                System.out.println(in.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class clientFrame extends JFrame {
    //时间显示格式
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
    //窗口宽度
    final int WIDTH = 700;
    //窗口高度
    final int HEIGHT = 700;
    //创建发送按钮
    JButton btnSend = new JButton("发送");
    //创建清除按钮
    JButton btnClear = new JButton("清屏");
    //创建退出按钮
    JButton btnExit = new JButton("退出");
    //创建消息接收者标签
    JLabel lblReceiver = new JLabel("对谁说？");
    //创建文本输入框, 参数分别为行数和列数
    JTextArea jtaSay = new JTextArea();
    //创建聊天消息框
    JTextArea jtaChat = new JTextArea();
    //当前在线列表的列标题
    String[] colTitles = {"网名", "IP", "端口"};
    //当前在线列表的数据
    String[][] rowData = null;
    //创建当前在线列表
    JTable jtbOnline = new JTable
            (
                    new DefaultTableModel(rowData, colTitles) {
                        //表格不可编辑，只可显示
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    }
            );
    //创建聊天消息框的滚动窗
    JScrollPane jspChat = new JScrollPane(jtaChat);
    //创建当前在线列表的滚动窗
    JScrollPane jspOnline = new JScrollPane(jtbOnline);

    //设置默认窗口属性，连接窗口组件
    public clientFrame() {
        //标题
        setTitle("client");
        //大小
        setSize(WIDTH, HEIGHT);
        //不可缩放
        setResizable(false);
        //设置布局:不适用默认布局，完全自定义
        setLayout(null);
        //设置按钮大小和位置
        btnSend.setBounds(20, 600, 100, 60);
        btnClear.setBounds(140, 600, 100, 60);
        btnExit.setBounds(260, 600, 100, 60);
        //设置标签大小和位置
        lblReceiver.setBounds(20, 420, 300, 30);
        //设置按钮文本的字体
        btnSend.setFont(new Font("宋体", Font.BOLD, 18));
        btnClear.setFont(new Font("宋体", Font.BOLD, 18));
        btnExit.setFont(new Font("宋体", Font.BOLD, 18));
        //添加按钮
        this.add(btnSend);
        this.add(btnClear);
        this.add(btnExit);
        //添加标签
        this.add(lblReceiver);
        //设置文本输入框大小和位置
        jtaSay.setBounds(20, 460, 360, 120);
        //设置文本输入框字体
        jtaSay.setFont(new Font("楷体", Font.BOLD, 16));
        //添加文本输入框
        this.add(jtaSay);
        //聊天消息框自动换行
        jtaChat.setLineWrap(true);
        //聊天框不可编辑，只用来显示
        jtaChat.setEditable(false);
        //设置聊天框字体
        jtaChat.setFont(new Font("楷体", Font.BOLD, 16));
        //设置滚动窗的水平滚动条属性:不出现
        jspChat.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //设置滚动窗的垂直滚动条属性:需要时自动出现
        jspChat.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //设置滚动窗大小和位置
        jspChat.setBounds(20, 20, 360, 400);
        //添加聊天窗口的滚动窗
        this.add(jspChat);
        jtbOnline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  //设置只选中一行
        //设置滚动窗的水平滚动条属性:不出现
        jspOnline.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //设置滚动窗的垂直滚动条属性:需要时自动出现
        jspOnline.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //设置当前在线列表滚动窗大小和位置
        jspOnline.setBounds(420, 20, 250, 400);
        //添加当前在线列表
        this.add(jspOnline);

        //添加发送按钮的响应事件
        btnSend.addActionListener
                (new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent event) {
                         //显示最新消息
                         jtaChat.setCaretPosition(jtaChat.getDocument().getLength());
                         try {
                             //有收信人才发送
                             //在聊天窗打印发送动作信息
                             jtaChat.append(client.name + "(" + sdf.format(new Date()) + ")\n:");
                             //显示发送消息
                             jtaChat.append(jtaSay.getText() + "\n\n");
                             //向服务器发送聊天信息
                             OutputStream out = client.socket.getOutputStream();
                             out.write((client.reciverName + "&@#@&" + jtaSay.getText()).getBytes());
                         } catch (Exception e) {
                         } finally {
                             //文本输入框清除
                             jtaSay.setText("");
                         }
                     }
                 }
                );
        //添加清屏按钮的响应事件
        btnClear.addActionListener
                (
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent event) {
                                //聊天框清屏
                                jtaChat.setText("");
                            }
                        }
                );
        //添加退出按钮的响应事件
        btnExit.addActionListener
                (
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent event) {
                                try {
                                    //向服务器发送退出信息
                                    OutputStream out = client.socket.getOutputStream();
                                    out.write("#$$#$#$#".getBytes());
                                    //退出
                                    System.exit(0);
                                } catch (Exception e) {
                                }
                            }
                        }
                );
        //添加在线列表项被鼠标选中的相应事件
        jtbOnline.addMouseListener
                (
                        new MouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent event) {
                                //取得在线列表的数据模型
                                DefaultTableModel tbm = (DefaultTableModel) jtbOnline.getModel();
                                //提取鼠标选中的行作为消息目标，最少一个人，最多全体在线者接收消息
                                int selectedIndex = jtbOnline.getSelectedRow();
                                client.reciverName=(String)tbm.getValueAt(selectedIndex,1);
                                //lblReceiver.setText("发给：" + Client1.uidReceiver.toString());
                            }

                            @Override
                            public void mousePressed(MouseEvent event) {
                            };
                            @Override
                            public void mouseReleased(MouseEvent event) {
                            };

                            @Override
                            public void mouseEntered(MouseEvent event) {
                            };

                            @Override
                            public void mouseExited(MouseEvent event) {
                            };
                        }
                );
    }
}