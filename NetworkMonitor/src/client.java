import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class client {
    static public void main(String[] a) {
        //启动四个窗口，代表四个客户端
        clientFrame cfa = new clientFrame("A");
        cfa.setLocation(300,500);
        cfa.setVisible(true);
        clientFrame cfb = new clientFrame("B");
        cfb.setLocation(600,500);
        cfb.setVisible(true);
        clientFrame cfc = new clientFrame("C");
        cfc.setLocation(900,500);
        cfc.setVisible(true);
        clientFrame cfd = new clientFrame("D");
        cfd.setLocation(1200,500);
        cfd.setVisible(true);
    }
}

class clientFrame extends JFrame {
    final private int WIDTH = 250;
    final private int HEIGHT = 180;
    private Boolean connet = false;
    private Panel p; //指示灯
    private JButton jb = new JButton("上线"); //上线按钮
    private Font f = new Font("宋体", Font.BOLD, 30);
    private Socket cs = null; //与服务器通讯的socket

    public clientFrame(String title) {
        setTitle(title);
        setResizable(false);
        p = new Panel(title); //设置标识
        //设置布局:不适用默认布局，完全自定义
        setLayout(null);
        setSize(WIDTH, HEIGHT);
        p.setBounds(30, 30, 60, 120);
        this.add(p);
        jb.setFont(f);
        jb.setBounds(110, 30, 90, 90);
        jb.setBorder(BorderFactory.createEtchedBorder());
        this.add(jb);
        addWindowListener(new WindowAdapter() {   //关闭窗口时，关闭套接字
                              @Override
                              public void windowClosing(WindowEvent e) {
                                  try{
                                      if (cs != null)  //关闭连接
                                          cs.close();
                                  }catch (Exception cse){
                                      cse.printStackTrace();
                                  }
                                  dispose();
                              }
                          });
        //添加按钮监听事件
        jb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!connet) { //如果未连接
                    try {
                        cs = new Socket(InetAddress.getLocalHost(), 10000);  //请求与服务器连接
                        cs.setKeepAlive(true); //保持连接
                        OutputStream os = cs.getOutputStream();//字节输出流
                        os.write(title.getBytes());  //向服务器表明身份
                        os.flush();
                        new Thread(new clientThread(cs)).start(); //启动心跳线程
                    } catch (Exception ex) {  //连接失败
                        connet = false;
                        jb.setText("上线");
                        p.setColor(Color.red);
                      //  ex.printStackTrace();
                    }
                    connet = true;
                    jb.setText("下线");
                    p.setColor(Color.green);  //改变指示灯颜色
                } else {  //如果已连接
                    try {
                        if (cs != null)  //关闭连接
                            cs.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    connet = false;
                    jb.setText("上线");
                    p.setColor(Color.red);//改变指示灯颜色
                }
            }
        });
    }
    class clientThread implements Runnable {  //心跳线程，定期向服务器发送心跳，确认自己在线
        Socket cs;
        public  clientThread(Socket s){
            this.cs=s;
        }
        @Override
        public void run() {
            OutputStream out=null;
            try {
                while (true) {  //间隔1秒发送一次心跳
                    if(cs!=null){
                        out=cs.getOutputStream();
                        out.write("A".getBytes());
                        out.flush();
                        cs.getInputStream().read(new byte[128]);
                        Thread.sleep(1000);
                    }else {
                        break;
                    }
                }
            } catch (Exception e) {  //如果连接被关闭，结束线程
              //  e.printStackTrace();
            }
        }
    }
    public void setClientState(Boolean b) {  //该变指示灯颜色
        p.setColor(b ? Color.green : Color.red);
    }

    class Panel extends JPanel {
        final int PanelWIDTH = 60;
        final int PanelHEIGHT = 120;
        final int r = 40;
        private JLabel pa;
        private Color color;
        private Font f = new Font("Times New Roman", Font.BOLD, 16);

        public Panel(String text) {
            setResizable(false);
            //设置布局:不适用默认布局，完全自定义
            setLayout(null);
            setSize(PanelWIDTH, PanelHEIGHT);
            pa = new JLabel(text, SwingConstants.CENTER);
            pa.setFont(f);

            pa.setBounds(0, 60, 60, 30);
            this.add(pa);
            color = Color.red;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawCircle(g);
        }

        public void setColor(Color c) {
            color = c;  //设置颜色
            updateUI(); //刷新界面
        }

        public void drawCircle(Graphics g) {  //画圆
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);
            g2d.fillOval(10, 0, r, r);
        }
    }
}