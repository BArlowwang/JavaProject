import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class server {
    static public void main(String[] args) {
        serverFrame s = new serverFrame(); //建立服务器窗口
        s.setVisible(true);
        try {
            ServerSocket socket = new ServerSocket(10000);  //监听10000端口
            byte[] buf = new byte[128];
            while (true) {
                //接收客户端Socket
                Socket cs = socket.accept();
                InputStream in = cs.getInputStream();
                int len = in.read(buf);
                String info = new String(buf, 0, len);  //接受客户端标识
                char[] chars = info.toCharArray();
                s.setClientState( chars[0], true);
                new Thread(new serverThread(cs, s, chars[0])).start();  //为客户端建立线程
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class serverThread implements Runnable {
    private serverFrame sf;
    private Socket cs;
    private int id ;
    byte[] buf = new byte[1024];
    public serverThread( Socket s,serverFrame sf,int id) {
        this.sf = sf;
        this.cs=s;
        this.id=id;
    }

    @Override
    public void run() {
        try {
            while (true) {
                    if (cs != null) {
                        InputStream in = cs.getInputStream();
                        try {
                            in.read(buf);  //读取客户端心跳，如果读取失败，说明客户端下线
                            cs.getOutputStream().write("ACK".getBytes()); //回复心跳
                        } catch (Exception ioe) {  //如果读取心跳失败
                            cs.close(); //关闭套接字
                         //   ioe.printStackTrace();
                            break;
                        }
                    } else {
                        break;
                    }
                }
            sf.setClientState((char) (id), false); //如果跳出循环，说明断开连接，将灯变为红色
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class serverFrame extends JFrame {
    final int WIDTH = 300;
    final int HEIGHT = 180;
    private Panel p = new Panel();

    public serverFrame() {
        setTitle("服务器");
        setResizable(false);
        //设置布局:不适用默认布局，完全自定义
        setLayout(null);
        setSize(WIDTH, HEIGHT);
        p.setBounds(30, 30, 240, 120);
        this.add(p);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);

            }
        });
    }

    public void setClientState(char a, Boolean b) {
        p.setColor(a, b ? Color.green : Color.red);
    }

    class Panel extends JPanel {  //指示灯面板
        final int PanelWIDTH = 240;
        final int PanelHEIGHT = 120;
        final int r = 40;
        private JLabel pa;  //四个标识符区域
        private JLabel pb;
        private JLabel pc;
        private JLabel pd;
        private Color[] color = new Color[4];  //四个指示灯颜色
        private Font f = new Font("Times New Roman", Font.BOLD, 16);

        public Panel() {
            setResizable(false);
            //设置布局:不适用默认布局，完全自定义
            setLayout(null);
            setSize(PanelWIDTH, PanelHEIGHT);
            pa = new JLabel("A", SwingConstants.CENTER);
            pb = new JLabel("B", SwingConstants.CENTER);
            pc = new JLabel("C", SwingConstants.CENTER);
            pd = new JLabel("D", SwingConstants.CENTER);

            pa.setFont(f);
            pb.setFont(f);
            pc.setFont(f);
            pd.setFont(f);

            pa.setBounds(0, 60, 60, 30);
            pb.setBounds(60, 60, 60, 30);
            pc.setBounds(120, 60, 60, 30);
            pd.setBounds(180, 60, 60, 30);
            this.add(pa);
            this.add(pb);
            this.add(pc);
            this.add(pd);
            color[0] = Color.red;  //初始为红色
            color[1] = Color.red;
            color[2] = Color.red;
            color[3] = Color.red;
        }

        @Override
        protected void paintComponent(Graphics g) {  //初始化和调用updateUI()时，系统会调用此方法重绘面板
            super.paintComponent(g);
            drawCircle(g);
        }

        public void setColor(char a, Color c) {  //设置灯颜色
            color[a - 'A'] = c;
            updateUI();
        }

        public void drawCircle(Graphics g) {  //画出四个指示灯
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color[0]);
            g2d.fillOval(10, 0, r, r);
            g2d.setColor(color[1]);
            g2d.fillOval(70, 0, r, r);
            g2d.setColor(color[2]);
            g2d.fillOval(130, 0, r, r);
            g2d.setColor(color[3]);
            g2d.fillOval(190, 0, r, r);
        }
    }
}