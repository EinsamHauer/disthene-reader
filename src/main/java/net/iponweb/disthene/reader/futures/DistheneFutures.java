package net.iponweb.disthene.reader.futures;

import com.datastax.driver.core.ResultSet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Andrei Ivanov
 */
public class DistheneFutures {

    public static ImmutableList<SinglePathFuture> inCompletionOrder(Map<String, ? extends ListenableFuture<ResultSet>> futures
            , final int length
            , final Map<Long, Integer> timestampIndices) {
        final ConcurrentLinkedQueue<SinglePathFuture> delegates = Queues.newConcurrentLinkedQueue();
        ImmutableList.Builder<SinglePathFuture> listBuilder = ImmutableList.builder();
        DistheneExecutor executor = new DistheneExecutor(MoreExecutors.directExecutor());
        for(Map.Entry<String, ? extends ListenableFuture<ResultSet>> entry : futures.entrySet()) {
            SinglePathFuture delegate = SinglePathFuture.create();
            delegates.add(delegate);
            final ListenableFuture<ResultSet> future = entry.getValue();
            final String path = entry.getKey();
            future.addListener(new Runnable() {
                @Override public void run() {
                    SinglePathFuture first = delegates.remove();
                    first.setPath(path);
                    first.setFuture(future);
                    first.makeJson(length, timestampIndices);
                }
            }, executor);
            listBuilder.add(delegate);
        }
        return listBuilder.build();
    }
}
