package ChatRoom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class server {
    public static void main(String[] args) throws Exception {
        //建立服务器ServerSocket
        ServerSocket ss = new ServerSocket(10000);
        //创建服务器端窗口对象
        serverFrame sf = new serverFrame();
        sf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //获取本机屏幕横向分辨率
        int w = Toolkit.getDefaultToolkit().getScreenSize().width;
        //获取本机屏幕纵向分辨率
        int h = Toolkit.getDefaultToolkit().getScreenSize().height;
        //将窗口置中
        sf.setLocation((w - sf.WIDTH) / 2, (h - sf.HEIGHT) / 2);
        //设置客户端窗口为可见
        sf.setVisible(true);
        //打印服务器信息
        sf.logArea.append("Server online(" + ss.getInetAddress().getLocalHost().getHostAddress() + ":" + 10000 + ")...\n\n");
        Integer count = 0; //统计在线的客户端数目

        while (true) {
            //接收客户端Socket
            Socket s = ss.accept();
            count++;
            //提取客户端IP和端口
            String ip = s.getInetAddress().getHostAddress();
            int port = s.getPort();
            //分配用户名
            String clientName = "client" + count.toString();
            //打印服务器日志
            sf.logArea.append(clientName + "(" + ip + ":" + port + ") 已上线\n\n");
            //建立新的服务器线程, 向该线程提供服务器ServerSocket，客户端Socket，客户端IP，端口以及客户端id
            new Thread(new ServerThread(s, ss, ip, port, clientName,sf)).start();
        }
    }
}

class ServerThread implements Runnable {
    //静态ArrayList存储所有uid
    static volatile ArrayList<String> uid_arr = new ArrayList<String>();
    //静态HashMap存储所有uid, ServerThread对象组成的键值对
    static volatile HashMap<String, ServerThread> hm = new HashMap<String, ServerThread>();
    //获取的客户端Socket
    Socket s = null;
    //获取的服务器ServerSocket
    ServerSocket ss = null;
    //获取的客户端IP
    String ip = null;
    //获取的客户端端口
    int port = 0;
    //客户端的id
    String uid = null;
    serverFrame sf;

    public ServerThread(Socket s, ServerSocket ss, String ip, int port, String clientName,serverFrame sf) {
        this.s = s;
        this.ss = ss;
        this.ip = ip;
        this.port = port;
        this.uid = clientName;
        this.sf=sf;
    }

    @Override
    public void run() {
        //将当前客户端uid存入ArrayList
        uid_arr.add(uid);
        //将当前uid和ServerThread对存入HashMap
        hm.put(uid, this);
        try {
            //获取输入流
            InputStream in = s.getInputStream();
            //获取输出流
            OutputStream out = s.getOutputStream();
            //向客户端发送分配的用户名
            String clientName = "ClientInfo/" + uid;
            //sendPacket函数实现以自定义的协议向客户端发送消息
            sendPacket(clientName, out);
            updateOnlineList(out); //通知所有客户端更新在线列表
            //准备缓冲区
            byte[] buf = new byte[2048];
            int len = 0;
            //持续监听并转发客户端消息
            while (true) {
                //读取消息
                len = in.read(buf);
                String msg = new String(buf, 0, len);
                //消息类型：退出或者聊天
                String type = msg.substring(0, msg.indexOf("/"));
                //消息本体：空或者聊天内容
                String content = msg.substring(msg.indexOf("/") + 1);
                //根据消息类型分别处理
                //客户端要退出
                if (type.equals("Exit")) {
                    //更新ArrayList和HashMap, 删除退出的uid和线程
                    uid_arr.remove(uid);
                    hm.remove(uid);
                    //广播更新在线名单
                    updateOnlineList(out);
                    //打印客户端IP和端口以及下线信息
                    sf.logArea.append(clientName + "(" + ip + ":" + port + ") 已下线\n\n");
                    //结束循环，结束该服务线程
                    break;
                }
                //客户端要聊天
                else if (type.equals("Chat")) {
                    //提取收信者地址
                    String receiver = content.substring(0, content.indexOf("/"));
                    //提取聊天内容
                    String word = content.substring(content.indexOf("/") + 1);
                    //
                    if (receiver.equals("group")) {  //群聊
                        for (String id : uid_arr) {
                            if (id.equals(uid)) continue;  //不给自己发
                            out = hm.get(id).s.getOutputStream();
                            String msgSend = "Chat/" + uid + "/" + word;
                            sendPacket(msgSend, out);
                        }
                    } else { //私聊
                        out = hm.get(receiver).s.getOutputStream();
                        String msgSend = "Chat/" + uid + "/" + word;
                        sendPacket(msgSend, out);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public void sendPacket(String message, OutputStream writer) { //已自定义协议向客户端发送消息
        byte[] contentBytes = message.getBytes();// 包体内容
        int contentlength = contentBytes.length;// 包体长度
        String head = String.valueOf(contentlength);// 头部内容
        byte[] headbytes = head.getBytes();// 头部内容字节数组
        byte[] bytes = new byte[headbytes.length + contentlength];//拼接包 包=包头+包体
        int i = 0;
        for (i = 0; i < headbytes.length; i++) {// 包头
            bytes[i] = headbytes[i];
        }
        for (int j = i, k = 0; k < contentlength; k++, j++) {// 包体
            bytes[j] = contentBytes[k];
        }
        try {
            writer.write(bytes);  //发送
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //向所有已连接的客户端更新在线名单
    public void updateOnlineList(OutputStream out) throws Exception {
        for (String tmp_uid : uid_arr) {
            //获取广播收听者的输出流
            out = hm.get(tmp_uid).s.getOutputStream();
            //将当前在线名单以逗号为分割组合成长字符串一次传送
            StringBuilder sb = new StringBuilder("OnlineListUpdate/");
            for (String member : uid_arr) {
                sb.append(member);
                //以逗号分隔uid，除了最后一个
                if (uid_arr.indexOf(member) != uid_arr.size() - 1)
                    sb.append(",");
            }
            sendPacket(sb.toString(), out);
        }
    }
}

//服务器窗口类
class serverFrame extends JFrame {
    //窗口宽度
    final int WIDTH = 650;
    //窗口高度
    final int HEIGHT = 750;
    //时间显示格式
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    //log消息区
    JTextArea logArea = new JTextArea();
    JScrollPane logWindow = new JScrollPane(logArea);
    //添加按钮
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
        //设置各组件的大小和位置
        clsBtn.setBounds(150, 660, 150, 50);
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
                            //退出
                            System.exit(0);
                        } catch (Exception e) {
                        }
                    }
                });
        clsBtn.addActionListener
                (new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent event) {
                         //聊天框清屏
                         logArea.setText("");
                     }
                 }
                );
    }
}