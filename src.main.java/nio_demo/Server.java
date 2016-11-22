package nio_demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by root on 2/18/16.
 */
public class Server {

    final static int PORT = 9999;
    //处理请求的线程池
    final static ExecutorService workThreadsPool = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5 / 8 + 3);
    public static void main(String[] args) throws IOException,
            InterruptedException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress("127.0.0.1", PORT));
        ssc.configureBlocking(false);
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            int selectorKeys = selector.select(1000L);
            if (selectorKeys == 0) {
                continue;
            }
            for (SelectionKey selectKey : selector.selectedKeys()) {
                // 客户端请求连接事件
                if (selectKey.isAcceptable()) {
                    ServerSocketChannel serverSocketChanel = (ServerSocketChannel) selectKey
                            .channel();
                    SocketChannel sc = serverSocketChanel.accept();
                    // 因为是采用的非阻塞模式，所以当没有连接时以上方法也为立即返回，只是返回的值为null
                    if (sc == null) {
                        continue;
                    }
                    System.out.println("accept a quest");
                    //给客户段发送消息
                    sc.write(ByteBuffer.wrap(new String("hello client").getBytes()));
                    sc.configureBlocking(false);
                    //设置为读事件
                    sc.register(selector, SelectionKey.OP_READ);
                } else if (selectKey.isReadable()) {
                    // 此处通过线程池实现了一请求一线程的机制，该机制的实现利用了NIO的非阻塞模式与通道选择器机制
                    // 相比以前BIO+THREADPOOL实现的一连接一线程的吞吐量更高，并发性更强
                    HandlerRequestWork handlerRequest = new HandlerRequestWork(
                            "Request handler");
                    workThreadsPool.execute(handlerRequest);
                    handlerRequest.handler(selectKey);
                }
            }
            selector.selectedKeys().clear();
        }
    }

    static class HandlerRequestWork extends Thread {
        private SelectionKey key;
        private final Lock lock = new ReentrantLock();
        private final Condition preparedSingle = lock.newCondition();

        public HandlerRequestWork(String threadName) {
            super(threadName);
        }

        public void handler(SelectionKey key) {
            lock.lock();
            try {
                preparedSingle.signalAll();
                this.key = key;
                // 在缓冲区数据未处理完成时，下一次轮询selector.select(1000L)时依然会触发readable事件，
                // 所以这里避免对同一次请求进行多次处理需要取消readable这一感兴趣事件 ，当处理完成后再注册该事件
                key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
            } finally {
                lock.unlock();
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {

            lock.lock();
            try {
                if (key == null) {
                    try {
                        preparedSingle.await();
                    } catch (InterruptedException e) {
//                            logger.admin(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
                if (key == null) {
                    return;
                }
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer bf = ByteBuffer.allocate(1024);
                try {
                    while (sc.read(bf) > 0) {
                        bf.flip();
                        System.out.println(Charset.forName("UTF-8").decode(bf)
                                .toString());
                        bf.clear();
                    }
                    if (bf != null) {
                        bf.clear();
                        bf = null;
                    }
                    sc.write(Charset.forName("UTF-8").encode(
                            "got messages from client=" + sc));
                    key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                } catch (IOException e) {
//                        logger.admin(e.getMessage(), e);
                    try {
                        sc.close();// 关闭发生发生异常的连接，并取消注册SelectionKey
                    } catch (IOException e1) {
//                            logger.admin(e1.getMessage(), e1);
                    }
                    key.cancel();
                }

            } finally {
                lock.unlock();

            }
        }

    }
}

