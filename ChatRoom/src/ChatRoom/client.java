package ChatRoom;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class client {
    //建立客户端Socket
    static Socket s = null;
    //消息接收者uid
    static String uidReceiver = "group";
    //自身id
    static String uid = null;

    public static void main(String[] args) {
        //创建客户端窗口对象
        ClientFrame cframe = new ClientFrame();
        //窗口关闭键无效，必须通过退出键退出客户端以便善后
        cframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //获取本机屏幕横向分辨率
        int w = Toolkit.getDefaultToolkit().getScreenSize().width;
        //获取本机屏幕纵向分辨率
        int h = Toolkit.getDefaultToolkit().getScreenSize().height;
        //将窗口置中
        cframe.setLocation((w - cframe.WIDTH) / 2, (h - cframe.HEIGHT) / 2);
        //设置客户端窗口为可见
        cframe.setVisible(true);
        try {
            //连接服务器
            s = new Socket(InetAddress.getLocalHost(), 10000);
            //启动接收线程，用于处理服务器发送的消息
            new ReceiveThread(s, cframe).start();
        } catch (Exception e) {
            cframe.jtaChat.append("服务器挂了.....\n");
            e.printStackTrace();
        }
    }

    static class ReceiveThread extends Thread {
        public static final int PACKET_HEAD_LENGTH = 2;//包头长度
        ClientFrame cframe; //窗口，此线程中需要向窗口输出消息
        private Socket socket;
        private volatile byte[] bytes = new byte[0];  //缓冲区

        public ReceiveThread(Socket socket, ClientFrame cframe) {
            this.socket = socket;
            this.cframe = cframe;
        }

        private void dealMsg(String msg) {  //处理服务器消息 msg:服务器消息
            /* 消息类型：
             * ClientInfo：表示服务器发送的是分配给该客户端的id
             * OnlineListUpdate :更新在线列表
             * Chat:聊天消息
             */
            String type = msg.substring(0, msg.indexOf("/")); //消息体与消息头之间用 / 分割
            //消息本体：更新后的名单、聊天内容或者客户端id
            String content = msg.substring(msg.indexOf("/") + 1);
            //根据消息类型分别处理
            if (type.equals("ClientInfo")) {
                uid = content; //设置自身id
                //向窗口服务器分配的id以及欢迎消息
                cframe.jtaChat.append("system(" + cframe.sdf.format(new Date()) + "):\n连接服务器成功！您的用户名是：" + uid + "\n\n");
                cframe.jtaChat.setCaretPosition(cframe.jtaChat.getDocument().getLength());
            } else if (type.equals("OnlineListUpdate")) { //更新在线列表
                //提取在线列表的数据模型
                DefaultTableModel tbm = (DefaultTableModel) cframe.jtbOnline.getModel();
                //清除在线名单列表
                tbm.setRowCount(0);
                //更新在线名单
                String[] onlinelist = content.split(",");  //在线客户端uid之间用 , 分隔  例如 client1,client2
                //逐一添加当前在线者
                for (String member : onlinelist) {
                    if (!member.equals(uid)) { //如果是自身则不添加至列表
                        String[] tmp = new String[3];
                        tmp[0] = member;
                        tmp[1] = "";
                        tmp[2] = "";
                        //添加当前在线者
                        tbm.addRow(tmp);
                    }
                }
                String[] tmp = {"group", "", ""};   //添加群发
                tbm.addRow(tmp);
                //提取在线列表的渲染模型
                DefaultTableCellRenderer tbr = new DefaultTableCellRenderer();
                //表格数据居中显示
                tbr.setHorizontalAlignment(JLabel.CENTER);
                cframe.jtbOnline.setDefaultRenderer(Object.class, tbr);
            }
            else if (type.equals("Chat")) {  //聊天
                //聊天消息由 发送者/消息体构成
                String sender = content.substring(0, content.indexOf("/"));   //提取发送者
                String word = content.substring(content.indexOf("/") + 1);    //提取消息体
                //在聊天窗打印聊天信息
                cframe.jtaChat.append(sender + "(" + cframe.sdf.format(new Date()) + "):\n" + word + "\n\n");
                //显示最新消息
                cframe.jtaChat.setCaretPosition(cframe.jtaChat.getDocument().getLength());
            }
        }
        //字节拼接，将 b[begin] 至 b[end] 的字节拼接至a的后面
        public byte[] mergebyte(byte[] a, byte[] b, int begin, int end) {
            byte[] add = new byte[a.length + end - begin];
            int i = 0;
            for (i = 0; i < a.length; i++) {
                add[i] = a[i];
            }
            for (int k = begin; k < end; k++, i++) {
                add[i] = b[k];
            }
            return add;
        }

        @Override
       /* 为了解决粘包和半包的问题，简单的定义一个服务器端至客户端间的通讯协议
        * 即在消息体的前面增加2字节的协议头，用于记录该条消息体的长度
        * run函数的主要任务就是根据以上协议解析服务器发送过来的数据包
        */
        public void run() {
            while (true) {
                try {
                    InputStream reader = socket.getInputStream(); //获得输入流
                    if (bytes.length < PACKET_HEAD_LENGTH) { //如果当前读入的字节不足以构成协议头
                        byte[] head = new byte[PACKET_HEAD_LENGTH - bytes.length];
                        int couter = reader.read(head); //则继续读取剩下的协议头
                        if (couter < 0) {  //如果未读取到数据则继续读取，不做后续处理
                            continue;
                        }
                        bytes = mergebyte(bytes, head, 0, couter);  //将刚刚读取的字节合并至已得到的协议头字节
                        if (couter < PACKET_HEAD_LENGTH) { //如果依然不够则继续读取，不做后续处理
                            continue;
                        }
                    }
                    //已得到协议头，根据协议头解析消息体
                    byte[] temp = new byte[0];  //临时缓冲区
                    temp = mergebyte(temp, bytes, 0, PACKET_HEAD_LENGTH); //将协议头放置于临时缓冲区头部
                    String templength = new String(temp); //将协议头的字节转换为字符串
                    int bodylength = Integer.parseInt(templength);//将协议头的字符串转换为整数，得到消息体长度
                    /*
                    * 此处稍作解释：
                    * 程序运行至此处，说明已获得协议头，即前面的判断为假，因此前的read不会执行
                    * 即此处的read会连续读取消息体
                     */
                    if (bytes.length - PACKET_HEAD_LENGTH < bodylength) {//如果已经得到的数据不够凑成消息体
                        byte[] body = new byte[bodylength + PACKET_HEAD_LENGTH - bytes.length];//剩下应该读的字节(凑一个包)
                        int couter = reader.read(body); //继续读取
                        if (couter < 0) { //如果未读取到数据则继续读取，不做后续处理
                            continue;
                        }
                        bytes = mergebyte(bytes, body, 0, couter);//将刚刚读取的消息体字节合并至已得到的消息体字节
                        if (couter < body.length) { //如果依然不够则继续读取，不做后续处理
                            continue;
                        }
                    }
                    //已得到消息体，解析消息体信息并作相应处理
                    byte[] body = new byte[0]; //临时消息体缓冲区
                    body = mergebyte(body, bytes, PACKET_HEAD_LENGTH, bytes.length); //将刚刚读取的消息体字节合并至已得到的消息体字节
                    String msg = new String(body);  //得到消息体字符串
                    dealMsg(msg); //处理服务器消息
                    bytes = new byte[0]; //清空缓冲区
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

//客户端窗口类
class ClientFrame extends JFrame {
    //窗口宽度
    final int WIDTH = 700;
    //窗口高度
    final int HEIGHT = 700;
    //时间显示格式
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    //创建发送按钮
    JButton btnSend = new JButton("发送");
    //创建清除按钮
    JButton btnClear = new JButton("清屏");
    //创建退出按钮
    JButton btnExit = new JButton("退出");
    //创建消息接收者标签
    JLabel lblReceiver = new JLabel("对谁说？");
    //创建文本输入框
    JTextArea jtaSay = new JTextArea();
    //创建聊天消息框
    JTextArea jtaChat = new JTextArea();
    //当前在线列表的列标题
    String[] colTitles = {"网名"};
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
    public ClientFrame() {
        //标题
        setTitle("聊天室");
        //大小
        setSize(WIDTH, HEIGHT);
        //不可缩放
        setResizable(false);
        //设置布局:不适用默认布局，完全自定义
        setLayout(null);

        //设置按钮大小和位置
        btnSend.setBounds(20, 600, 100, 60); //发送按钮
        btnClear.setBounds(140, 600, 100, 60); //清屏按钮
        btnExit.setBounds(260, 600, 100, 60); //推出按钮
        //设置按钮文本的字体
        btnSend.setFont(new Font("宋体", Font.BOLD, 18));//发送按钮
        btnClear.setFont(new Font("宋体", Font.BOLD, 18));//清屏按钮
        btnExit.setFont(new Font("宋体", Font.BOLD, 18));//退出按钮
        //添加按钮
        this.add(btnSend);//发送按钮
        this.add(btnClear);//清屏按钮
        this.add(btnExit);//退出按钮

        //设置消息接收者标签大小和位置
        lblReceiver.setBounds(20, 420, 300, 30);
        //添加消息接收者标签
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

        //设置在线列表只能选中一行
        jtbOnline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //设置在线列表水平滚动条属性:不出现
        jspOnline.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //设置在线列表垂直滚动条属性:需要时自动出现
        jspOnline.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //设置当前在线列表滚动窗大小和位置
        jspOnline.setBounds(420, 20, 250, 400);
        //添加当前在线列表
        this.add(jspOnline);

        //添加发送按钮的响应事件
        btnSend.addActionListener
                (
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent event) {
                                //显示最新消息
                                jtaChat.setCaretPosition(jtaChat.getDocument().getLength());
                                try {
                                    String msg = jtaSay.getText(); //获取输入框消息
                                    //在聊天窗打印聊天信息
                                    jtaChat.append(client.uid + "(" + sdf.format(new Date()) + "):\n" + msg + "\n\n");
                                    //显示最新消息
                                    jtaChat.setCaretPosition(jtaChat.getDocument().getLength());
                                    //向服务器发送聊天信息
                                    OutputStream out = client.s.getOutputStream(); //获得输出流
                                    out.write(("Chat/" + client.uidReceiver + "/" + msg).getBytes()); //消息由 Chat/接收者/消息体 构成
                                } catch (Exception e) {
                                    e.printStackTrace();
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
                                    OutputStream out = client.s.getOutputStream();
                                    out.write("Exit/".getBytes());
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
                                //提取鼠标选中的行作为消息目标
                                int selectedIndex = jtbOnline.getSelectedRow();
                                //设置接收者
                                client.uidReceiver = ((String) tbm.getValueAt(selectedIndex, 0));
                                lblReceiver.setText("发给：" + client.uidReceiver);
                            }

                            @Override
                            public void mousePressed(MouseEvent event) {
                            }

                            @Override
                            public void mouseReleased(MouseEvent event) {
                            }

                            @Override
                            public void mouseEntered(MouseEvent event) {
                            }

                            @Override
                            public void mouseExited(MouseEvent event) {
                            }

                        }
                );
    }
}
