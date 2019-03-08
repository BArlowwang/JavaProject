package test;//test.server.java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class server implements Runnable {// 服务端
    static List<Socket> socketList=new ArrayList<Socket>();
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
        Scanner input = new Scanner(System.in);
        System.out.println("************服务端*************");
        server t = new server();
        int count = 0;
        while (true) {
            try {
//              System.out.println("端口9999等待被连接......");
                socket = serverSocket.accept();
                count++;
                System.out.println("第" + count + "个客户已连接");
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
                System.out.println( jieshou);
                for (int i = 0; i < socketList.size(); i++) {
                    Socket socket=socketList.get(i);
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    if (socket!=this.socket) {
                        out.println(jieshou);
                    }else{
                        out.println("(你)"+jieshou);
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
    static List<Socket> socketList=new ArrayList<Socket>();
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
                    Socket socket=socketList.get(i);
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    // System.out.println("对客户端说：");
                    out.println("服务端说："+msg);
                    out.flush();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}