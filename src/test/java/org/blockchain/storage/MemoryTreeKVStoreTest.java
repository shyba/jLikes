package org.blockchain.storage;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemoryTreeKVStoreTest {

    @Test
    void iter() {
        MemoryTreeKVStore<Integer> map = new MemoryTreeKVStore<>(Bytes::of, Bytes::toInt);
        map.put(Bytes.fromHexString("cafecafaaa"), 11);
        map.put(Bytes.fromHexString("cafecafeaa"), 12);
        map.put(Bytes.fromHexString("cafecafebb"), 13);
        map.put(Bytes.fromHexString("cafecffeaa"), 14);
        map.put(Bytes.fromHexString("dddddddddd"), 15);
        List<Integer> vals = map.iter(Bytes.fromHexString("cafecafe"));
        List<Integer> expected = List.of(12, 13);
        assertArrayEquals(vals.toArray(), expected.toArray());
    }
}