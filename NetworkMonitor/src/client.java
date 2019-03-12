import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;

public class client {
    static public void main(String[] a) {
        clientFrame cfa = new clientFrame("A");
        cfa.setVisible(true);
        clientFrame cfb = new clientFrame("B");
        cfb.setVisible(true);
        clientFrame cfc = new clientFrame("C");
        cfc.setVisible(true);
        clientFrame cfd = new clientFrame("D");
        cfd.setVisible(true);
    }
}

class clientFrame extends JFrame {
    final private int WIDTH = 250;
    final private int HEIGHT = 180;
    private Boolean connet = false;
    private Panel p;
    private JButton jb = new JButton("上线");
    private Font f = new Font("宋体", Font.BOLD, 30);
    static Socket cs = null;

    public clientFrame(String title) {
        setTitle(title);
        setResizable(false);
        p = new Panel(title);
        //设置布局:不适用默认布局，完全自定义
        setLayout(null);
        setSize(WIDTH, HEIGHT);
        p.setBounds(30, 30, 60, 120);
        this.add(p);
        jb.setFont(f);
        jb.setBounds(110, 30, 90, 90);
        jb.setBorder(BorderFactory.createEtchedBorder());
        this.add(jb);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        jb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!connet) {
                    try {
                        cs = new Socket(InetAddress.getLocalHost(), 10000);
                        cs.setKeepAlive(true);
                        OutputStream os = cs.getOutputStream();//字节输出流
                        os.write(title.getBytes());
                        os.flush();
                        new Thread(new clientThread(cs)).start();
                    } catch (Exception ex) {
                        connet = false;
                        jb.setText("上线");
                        p.setColor(Color.red);
                        ex.printStackTrace();
                    }
                    connet = true;
                    jb.setText("下线");
                    p.setColor(Color.green);
                } else {
                    try {
                        if (cs != null)
                            cs.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    connet = false;
                    jb.setText("上线");
                    p.setColor(Color.red);
                }
            }
        });
    }

    public void setClientState(Boolean b) {
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
            color = c;
            updateUI();

        }

        public void drawCircle(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);
            g2d.fillOval(10, 0, r, r);
        }
    }
}
class clientThread implements Runnable {
    Socket cs;
    public  clientThread(Socket s){
        this.cs=s;
    }
    @Override
    public void run() {
        OutputStream out=null;
        try {
            while (true) {
                if(cs!=null){
                    out=cs.getOutputStream();
                    out.write("A".getBytes());
                    out.flush();
                    cs.getInputStream().read(new byte[128]);
                    Thread.sleep(2000);
                }else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}