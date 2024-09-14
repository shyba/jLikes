package org.blockchain.storage;

import org.apache.tuweni.bytes.Bytes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class MemoryTreeKVStore<T> extends KVStore<T> {
    private final TreeMap<Bytes, Bytes> memory;

    public MemoryTreeKVStore(Function<T, Bytes> serializer, Function<Bytes, T> deSerializer) {
        super(serializer, deSerializer);
        this.memory = new TreeMap<>();
    }

    @Override
    public T get(Bytes key) {
        Bytes raw = this.memory.get(key);
        if (raw != null) return this.deSerializer.apply(this.memory.get(key));
        return null;
    }

    @Override
    public List<T> iter(Bytes prefix) {
        ArrayList<T> results = new ArrayList<>();
        for (Map.Entry<Bytes, Bytes> entry : this.memory.tailMap(prefix).sequencedEntrySet()) {
            if (entry.getKey().commonPrefixLength(prefix) >= prefix.size()) {
                results.add(this.deSerializer.apply(entry.getValue()));
            }
        }
        return results;
    }

    @Override
    public boolean put(Bytes key, T value) {
        Bytes valueBytes = this.serializer.apply(value);
        return this.memory.put(key, valueBytes) == null;
    }
}
