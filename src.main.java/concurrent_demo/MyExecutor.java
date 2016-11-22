package concurrent_demo;
import java.util.concurrent.*;

/**
 * Created by root on 2/18/16.
 */
    public class MyExecutor extends Thread {
        private int index;
        public MyExecutor(int i){
            this.index=i;
        }
        public void run(){
            try{
                System.out.println("["+this.index+"] start....");
                Thread.sleep(100000);
                System.out.println("["+this.index+"] end.");
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        public static void main(String args[]){
            /**
             * 创建一个可重用固定线程集合的线程池，以共享的无界队列方式来运行这些线程（只有要请求的过来，就会在一个队列里等待执行）。
             * 如果在关闭前的执行期间由于失败而导致任何线程终止，那么一个新线程将代替它执行后续的任务（如果需要）。
             */
//            ExecutorService service=Executors.newFixedThreadPool(4);
            /**
             * 创建一个可根据需要创建新线程的线程池，但是在以前构造的线程可用时将重用它们。
             * 对于执行很多短期异步任务的程序而言，这些线程池通常可提高程序性能。
             * 调用 execute 将重用以前构造的线程（如果线程可用）。
             * 如果现有线程没有可用的，则创建一个新线程并添加到池中。
             * 终止并从缓存中移除那些已有 60 秒钟未被使用的线程。因此，长时间保持空闲的线程池不会使用任何资源。
             * 注意，可以使用 ThreadPoolExecutor 构造方法创建具有类似属性但细节不同（例如超时参数）的线程池。
             */
//            ExecutorService service=Executors.newCachedThreadPool();
//
            ExecutorService service=new ThreadPoolExecutor(8, 8, 0L,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100000));
            for(int i=0;i<10;i++){
                service.execute(new MyExecutor(i));
                //service.submit(new MyExecutor(i));
            }
            System.out.println("submit finish");
            service.shutdown();
        }
    }
