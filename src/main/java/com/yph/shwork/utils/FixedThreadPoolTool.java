package com.yph.shwork.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedThreadPoolTool<T> {

    private Map<String, List<T>> map;//操作的数据集

    private CountDownLatch end, outer;//end 所有线程处理结束的标识，outer 告诉外部主线程继续处理

    private ExecutorService executorService;

    private CallBack callBack;//回调函数

    private AtomicInteger result = new AtomicInteger();

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public FixedThreadPoolTool(int threads, Map<String, List<T>> map) {
        this.executorService = Executors.newFixedThreadPool(threads);
        this.map = map;
    }

    public int getResult() {
        return result.intValue();
    }

    public void setMap(Map<String, List<T>> map) {
        this.map = map;
    }

    public void execute() throws Exception {
        end = new CountDownLatch(map.size());
        for (Map.Entry<String, List<T>> listEntry : map.entrySet()) {
            Worker task = new Worker<T>(listEntry.getKey(), listEntry.getValue(), end) {
                @Override
                public int method(String tableId, List<T> list) {
                    int cnt = callBack.method(tableId, list);
                    return cnt;
                }
            };
            executorService.execute(task);
        }
        end.await();
        if (outer != null) {
            outer.countDown();
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public abstract class Worker<T> implements Runnable {

        private String tableId;
        private List<T> list;
        private CountDownLatch end;

        public Worker(String tableId, List<T> list, CountDownLatch end) {
            this.tableId = tableId;
            this.list = list;
            this.end = end;
        }

        @Override
        public void run() {
            try {
                int count = method(tableId, list);
                result.addAndGet(count);
            } finally {
                end.countDown();
            }
        }

        public abstract int method(String tableId, List<T> list);
    }

    public void setOuter(CountDownLatch outer) {
        this.outer = outer;
    }
    public interface CallBack<T> {
        public int method(String tableId, List<T> list);
    }
}


