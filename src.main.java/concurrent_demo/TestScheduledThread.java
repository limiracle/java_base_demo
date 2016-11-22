package concurrent_demo;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
/**
 * 一个 ExecutorService，可安排在给定的延迟后运行或定期执行的命令。
 * schedule 方法使用各种延迟创建任务，并返回一个可用于取消或检查执行的任务对象。
 * scheduleAtFixedRate 和 scheduleWithFixedDelay 方法创建并执行某些在取消前一直定期运行的任务。
 * 用 Executor.execute(java.lang.Runnable) 和 ExecutorService 的 submit 方法所提交的命令，通过所请求的 0 延迟进行安排。
 * schedule 方法中允许出现 0 和负数延迟（但不是周期），并将这些视为一种立即执行的请求。
 * 所有的 schedule 方法都接受相对 延迟和周期作为参数，而不是绝对的时间或日期。将以 Date 所表示的绝对时间转换成要求的形式很容易。
 * 例如，要安排在某个以后的日期运行，可以使用：schedule(task, date.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS)。
 * 但是要注意，由于网络时间同步协议、时钟漂移或其他因素的存在，因此相对延迟的期满日期不必与启用任务的当前 Date 相符。
 * Executors 类为此包中所提供的 ScheduledExecutorService 实现提供了便捷的工厂方法。
 */
public class TestScheduledThread {
    public static void main(String[] args) {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        final Runnable beeper = new Runnable() {
            int count = 0;
            public void run() {
                System.out.println(new Date() + " beep " + (++count));
            }
        };
        // 1秒钟后运行，并每隔2秒运行一次
        final ScheduledFuture beeperHandle = scheduler.scheduleAtFixedRate(beeper, 1, 2, SECONDS);
        // 2秒钟后运行，并每次在上次任务运行完后等待5秒后重新运行
        final ScheduledFuture beeperHandle2 = scheduler.scheduleWithFixedDelay(beeper, 2, 5, SECONDS);
        // 30秒后结束关闭任务，并且关闭Scheduler
        scheduler.schedule(new Runnable() {
            public void run() {
                beeperHandle.cancel(true);
                beeperHandle2.cancel(true);
                scheduler.shutdown();
            }
        }, 30, SECONDS);
    }
}
