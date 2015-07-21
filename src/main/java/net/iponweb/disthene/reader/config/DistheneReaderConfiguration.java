package net.iponweb.disthene.reader.config;

/**
 * @author Andrei Ivanov
 */
public class DistheneReaderConfiguration {
    private ReaderConfiguration reader;
    private StoreConfiguration store;
    private IndexConfiguration index;

    public ReaderConfiguration getReader() {
        return reader;
    }

    public void setReader(ReaderConfiguration reader) {
        this.reader = reader;
    }

    public StoreConfiguration getStore() {
        return store;
    }

    public void setStore(StoreConfiguration store) {
        this.store = store;
    }

    public IndexConfiguration getIndex() {
        return index;
    }

    public void setIndex(IndexConfiguration index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "DistheneReaderConfiguration{" +
                "reader=" + reader +
                ", store=" + store +
                ", index=" + index +
                '}';
    }
}
