# jLikes

A toy blockchain for exchanging likes and a distributed key value store.

## Transaction format
There is no script or anything like that.
Input carries a signature and public key for proving ownership of an output and output carries amount and pubkey target hash (addres);

Other transaction outputs are possible but not implemented yet. One example is writing a value to a key in the state trie.

## Block
Block carries a pointer to the previous block, transaction list and a global+block state trie.
Transactions are stored in the block trie and arbitrary data in the global state trie.