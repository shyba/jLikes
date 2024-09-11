package org.blockchain.storage;

import org.apache.tuweni.bytes.Bytes;

import java.util.function.Function;

public abstract class KVStore<T> {
    protected final Function<T, Bytes> serializer;
    protected final Function<Bytes, T> deSerializer;
    public KVStore(Function<T, Bytes> serializer, Function<Bytes, T> deSerializer) {
        this.serializer = serializer;
        this.deSerializer = deSerializer;
    }

    public abstract T get(Bytes key);
    public abstract Iterable<T> iter(Bytes prefix);
    public abstract boolean put(Bytes key, T value);
}
