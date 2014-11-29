package net.iponweb.disthene.reader.futures;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import net.iponweb.disthene.reader.utils.ListUtils;
import net.iponweb.disthene.reader.utils.WildcardUtil;
import org.apache.log4j.Logger;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Andrei Ivanov
 */
public class SinglePathFuture extends ForwardingListenableFuture<ResultSet> {
    final static Logger logger = Logger.getLogger(SinglePathFuture.class);

    /** Creates a new asynchronously-settable future. */
    public static SinglePathFuture create() {
        return new SinglePathFuture();
    }

    private final NestedFuture nested = new NestedFuture();
    private final ListenableFuture<ResultSet> dereferenced = Futures.dereference(nested);
    private String path;
    private String json;

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

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public void makeJson(int length, Map<Long, Integer> timestampIndices) {
        try {
            ResultSet resultSet = get();
            Double values[] = new Double[length];
            for (Row row : resultSet) {
                values[timestampIndices.get(row.getLong("time"))] =
                        WildcardUtil.isSumMetric(path) ? ListUtils.sum(row.getList("data", Double.class)) : ListUtils.average(row.getList("data", Double.class));
            }

            json = new Gson().toJson(values);
        } catch (Exception e) {
            logger.error("Failed to serialize to JSON: ", e);
        }
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
