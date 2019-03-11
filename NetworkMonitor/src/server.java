import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.ServerSocket;
import java.net.Socket;

public class server {
    static public void main(String[] args){
        serverFrame s=new serverFrame();
        s.setVisible(true);
        try {
            ServerSocket socket=new ServerSocket(10000);
            while (true) {
                //接收客户端Socket
                Socket cs = socket.accept();
                //提取客户端IP和端口
                String ip = cs.getInetAddress().getHostAddress();
                int port = cs.getPort();

               // new Thread(new ServerThread(s, ss, ip, port, clientName,sf)).start();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}

class serverFrame extends JFrame {
    final int WIDTH = 300;
    final int HEIGHT = 180;
    private Panel p = new Panel();
    public  serverFrame(){
        setTitle("服务器");
        setResizable(false);
        //设置布局:不适用默认布局，完全自定义
        setLayout(null);
        setSize(WIDTH,HEIGHT);
        p.setBounds(30,30,240,120);
        this.add(p);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);

            }
        });
    }
    public void setClientState(char a,Boolean b){
        p.setColor(a,b?Color.green:Color.red);
    }
    class Panel extends JPanel {
        final int PanelWIDTH = 240;
        final int PanelHEIGHT = 120;
        final int r = 40;
        private JLabel pa;
        private JLabel pb;
        private JLabel pc;
        private JLabel pd;
        private Color[] color = new Color[4];
        private Font f = new Font("Times New Roman",Font.BOLD,16);
        public Panel() {
            setResizable(false);
            //设置布局:不适用默认布局，完全自定义
            setLayout(null);
            setSize(PanelWIDTH, PanelHEIGHT);
            pa = new JLabel("A",SwingConstants.CENTER);
            pb = new JLabel("B",SwingConstants.CENTER);
            pc = new JLabel("C",SwingConstants.CENTER);
            pd = new JLabel("D",SwingConstants.CENTER);

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
            color[0] = Color.red;
            color[1] = Color.red;
            color[2] = Color.red;
            color[3] = Color.red;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawCircle(g);
        }

        public void setColor(char a, Color c) {
            color[a-'A']=c;
            updateUI();

        }

        public void drawCircle(Graphics g) {
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