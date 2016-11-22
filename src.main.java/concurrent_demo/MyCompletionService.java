package concurrent_demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**CompletionService
 * ExecutorService的扩展，可以获得线程执行结果的
 * 将生产新的异步任务与使用已完成任务的结果分离开来的服务。生产者 submit 执行的任务。使用者 take 已完成的任务，
 * 并按照完成这些任务的顺序处理它们的结果。例如，CompletionService 可以用来管理异步 IO ，执行读操作的任务作为程序或系统的一部分提交，
 * 然后，当完成读操作时，会在程序的不同部分执行其他操作，执行操作的顺序可能与所请求的顺序不同。
 * 通常，CompletionService 依赖于一个单独的 Executor 来实际执行任务，在这种情况下，
 * CompletionService 只管理一个内部完成队列。ExecutorCompletionService 类提供了此方法的一个实现。
 */
public class MyCompletionService implements Callable<String>{
    private int id;

    public MyCompletionService(int i){
        this.id=i;
    }
    public static void main(String[] args) throws Exception{
        ExecutorService service= Executors.newCachedThreadPool();
        CompletionService<String> completion=new ExecutorCompletionService<String>(service);
        List<Future> list=new ArrayList<>();
        for(int i=0;i<10;i++){
            Future resultFuture=completion.submit(new MyCompletionService(i));
            list.add(resultFuture);
        }
        /**
         * 可以取得运行结果
        for(int i=0;i<10;i++){
            //可以取得运行结果
            System.out.println(completion.take().get());
        }
        service.shutdown();
         */
        for(Future f:list){
            if(!f.isDone()){
                System.out.println("未运行完成");
                f.cancel(false);
                System.out.println("停止运行");
                Thread.sleep(100);
            }else{
                System.out.println((String)f.get());
            }
        }

    }
    public String call() throws Exception {
        Integer time=(int)(Math.random()*1000);
        try{
            System.out.println(this.id+" start");
            Thread.sleep(time);
            System.out.println(this.id+" end");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return this.id+":"+time;
    }
}
