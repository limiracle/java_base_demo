package concurrent_demo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 一个同步辅助类，在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待。
 用给定的计数 初始化 CountDownLatch。由于调用了 countDown() 方法，所以在当前计数到达零之前，await 方法会一直受阻塞。
 之后，会释放所有等待的线程，await 的所有后续调用都将立即返回。这种现象只出现一次——计数无法被重置。如果需要重置计数，请考虑使用 CyclicBarrier。
 CountDownLatch 是一个通用同步工具，它有很多用途。将计数 1 初始化的 CountDownLatch 用作一个简单的开/关锁存器，
 或入口：在通过调用 countDown() 的线程打开入口前，所有调用 await 的线程都一直在入口处等待。
 用 N 初始化的 CountDownLatch 可以使一个线程在 N 个线程完成某项操作之前一直等待，或者使其在某项操作完成 N 次之前一直等待。
 CountDownLatch 的一个有用特性是，它不要求调用 countDown 方法的线程等到计数到达零时才继续，
 而在所有线程都能通过之前，它只是阻止任何线程继续通过一个 await。
 */
public class TestCountDownLatch {
    public static void main(String[] args) throws InterruptedException {
        // 开始的倒数锁
        final CountDownLatch begin = new CountDownLatch(1);
        // 结束的倒数锁
        final CountDownLatch end = new CountDownLatch(10);
        // 十名选手
        final ExecutorService exec = Executors.newFixedThreadPool(10);

        for (int index = 0; index < 10; index++) {
            final int NO = index + 1;
            CountThread thread=new CountThread(begin,end,NO);
            exec.submit(thread);
        }
        System.out.println("Game Start");
        Thread.sleep((long) (10000));
        begin.countDown();
        end.await();
        System.out.println("Game Over");
        exec.shutdown();
    }
}
class CountThread implements Runnable{
    CountDownLatch begin;
    CountDownLatch end;
    int NO;
    CountThread(CountDownLatch begin,CountDownLatch end,int NO){
        this.begin=begin;
        this.end=end;
        this.NO=NO;
    }
    public void run() {
        try {
            begin.await();//一直阻塞
            Thread.sleep((long) (Math.random() * 10000));
            System.out.println("No." + NO + " arrived");
        } catch (InterruptedException e) {
        } finally {
            end.countDown();
        }
    }
}