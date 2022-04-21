package com.yph.shwork.common.utils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolTool<T> {

    private int singleCount;//单个线程处理的数据量

    private int taskNum;//创建的任务数量

    private List<T> list;//操作的数据集

    private CountDownLatch end,outer;//end 所有线程处理结束的标识，outer 告诉外部主线程继续处理

    private ExecutorService executorService;

    private CallBack callBack;//回调函数

    private AtomicInteger counter = new AtomicInteger();

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public ThreadPoolTool(int singleCount){
        if(singleCount < 1000)
            throw new RuntimeException("线程最大处理数不能低于1000");
        this.singleCount = singleCount;
        executorService = Executors.newCachedThreadPool();
    }

    public ThreadPoolTool(int singleCount, String threadName){
        if(singleCount < 1000)
            throw new RuntimeException("单个线程处理粒度不能小于1000");
        this.singleCount = singleCount;
        executorService = Executors.newCachedThreadPool((r) -> new Thread(r, threadName + "--" + counter.addAndGet(1)));
    }

    public void execute() throws Exception {
        end = new CountDownLatch(taskNum);
        List<T> newList = null;
        for (int i = 0; i < taskNum; i++) {
            if (i < (taskNum - 1)){
                newList = list.subList(i * singleCount, (i + 1) * singleCount);
            }else {
                newList = list.subList(i * singleCount, list.size());
            }
            Worker<T> task = new Worker(newList, end) {
                @Override
                public void method(List list) {
                    callBack.method(list);
                }
            };
            executorService.execute(task);
        }
        end.await();
        if(outer != null){
            outer.countDown();
        }
    }

    public void shutdown(){
        executorService.shutdown();
    }

    public abstract class Worker<T> implements Runnable{

        private List<T> list;
        private CountDownLatch end;

        public Worker(List<T> list, CountDownLatch end){
            this.list = list;
            this.end = end;
        }

        @Override
        public void run() {
            try {
                method(list);
            } finally {
                end.countDown();
            }
        }
        public abstract void method(List<T> list);
    }

    public interface CallBack<T>{
        public void method(List<T> list);
    }

    public void setOuter(CountDownLatch outer) {
        this.outer = outer;
    }

    public void setList(List<T> list) {
        this.list = list;
        if (list != null){
            if(list.size() % singleCount == 0){
                this.taskNum = (list.size()/singleCount);
            }else {
                this.taskNum = (list.size()/singleCount) + 1;
            }
        }
    }
}