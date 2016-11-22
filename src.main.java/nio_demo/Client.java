package nio_demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by root on 2/18/16.
 */
public class Client {

    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);//需在sc.connect之前设置
        sc.connect(new InetSocketAddress("127.0.0.1", 9999));
        Selector selector = Selector.open();
        sc.register(selector, SelectionKey.OP_CONNECT);
        System.out.println("BEGIN...");
        while (true) {
            System.out.println("channel 连接："+sc.isConnected());
            if (sc.isConnected()) {
                BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
                int writeBytes = sc.write(Charset.forName("UTF-8").encode(read.readLine()));
                if (writeBytes == 0) {
                    sc.register(selector, SelectionKey.OP_WRITE);//注册写事件（当写缓冲区满时）
                }
            }
            int selectKeys = selector.select();
            if (selectKeys == 0) {
                continue;
            }
            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isConnectable()) {
                    SocketChannel socketChanel = (SocketChannel) key.channel();
                    if (socketChanel == null) {
                        continue;
                    }
                    socketChanel.configureBlocking(false);
                    socketChanel.register(selector, SelectionKey.OP_READ);
                    socketChanel.finishConnect();
                } else if (key.isReadable()) {
                    SocketChannel socketChanel = (SocketChannel) key.channel();
                    ByteBuffer bf = ByteBuffer.allocate(1024);
                    while (socketChanel.read(bf) > 0) {
                        bf.flip();
                        System.out.println(Charset.forName("UTF-8").decode(bf).toString());
                        bf.clear();
                    }
                } else if (key.isWritable()) {
                    //只要写缓冲区未满就一直会产生写事件，如果此时又不写数据时，会产生不必要的资源损耗，所以这里需要取消写事件以免cpu消耗100%
                    //写数据，如果写缓冲区满时继续注册写事件key.interestOps(key.interestOps()|SelectionKey.OP_WRITE);
                    key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));

                }
            }
            selector.selectedKeys().clear();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
