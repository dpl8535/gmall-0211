package com.atuigu.gmall.item;

import java.util.concurrent.CompletableFuture;

/**
 * @author dplStart
 * @create 下午 08:23
 * @Description
 */
public class CompletableFutureDemo {

    public static void main(String[] args) {

        CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            System.out.println("初始化了一个A任务");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("A任务执行完毕");
            return "A";
        });

        CompletableFuture bCompletableFuture = future.thenApplyAsync(t -> {
            System.out.println("B任务初始化了");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("B任务执行完毕");
            return "B";
        });

        CompletableFuture cCompletableFuture = future.thenApplyAsync(t -> {
            System.out.println("C任务初始化了");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("C任务执行完毕");
            return "C";
        });

        CompletableFuture.allOf(future,bCompletableFuture,cCompletableFuture).join();
        System.out.println("上述所有方法执行结束了");


        /*CompletableFuture future = CompletableFuture.supplyAsync(() -> {
            System.out.println("A任务执行了");
            int i = 10 / 1;
            return 1024;
        }).thenApply(t -> {
            System.out.println("依赖上一个方法执行，获取上一个任务的返回结果，并返回当前任务的返回值" + t);
            return "2048";
        }).thenAccept(t -> {
            System.out.println("消费处理结果，没有返回值" + t);
        }).thenRun( () -> {
            System.out.println("上面的任务执行结束，执行当前方法");
        });
*/

        /*CompletableFuture.supplyAsync(() ->{
            System.out.println("A线程执行了");
//            int i = 10 / 0;
            return "Hello CompletableFuture!";
        }).thenAccept(t -> {
            System.out.println("B线程执行了" + t);
        }).thenAcceptAsync(t -> {
            System.out.println("B+线程执行了" + t);
        }).whenComplete((t, u) -> {
            System.out.println("C线程执行了" + t + u);
        }).whenCompleteAsync((t, u) -> {
            System.out.println("D线程执行了");
        }).exceptionally(t -> {
            System.out.println("E线程执行了" + t);
            return null;
        });*/
    }

}
