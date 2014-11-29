package net.iponweb.disthene.reader.futures;

import com.datastax.driver.core.ResultSet;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Andrei Ivanov
 */
public class SinglePathFuture extends ForwardingListenableFuture<ResultSet> {

    /** Creates a new asynchronously-settable future. */
    public static SinglePathFuture create() {
        return new SinglePathFuture();
    }

    private final NestedFuture nested = new NestedFuture();
    private final ListenableFuture<ResultSet> dereferenced = Futures.dereference(nested);
    private String path;

    private SinglePathFuture() {}

    @Override
    protected ListenableFuture<ResultSet> delegate() {
        return dereferenced;
    }

    public boolean setFuture(ListenableFuture<ResultSet> future) {
        return nested.setFuture(checkNotNull(future));
    }

    public boolean setValue(ResultSet value) {
        return setFuture(Futures.immediateFuture(value));
    }

    public boolean setException(Throwable exception) {
        return setFuture(Futures.<ResultSet>immediateFailedFuture(exception));
    }

    public boolean isSet() {
        return nested.isDone();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private static final class NestedFuture extends AbstractFuture<ListenableFuture<ResultSet>> {
        boolean setFuture(ListenableFuture<ResultSet> value) {
            boolean result = set(value);
            if (isCancelled()) {
                value.cancel(wasInterrupted());
            }
            return result;
        }
    }

}
