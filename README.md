# jLikes

A toy blockchain for exchanging likes and a distributed key value store.

## Transaction format
There is no script or anything programmable.
Instead, inputs and outputs formats are fixed to make it easier to store and parse at the expense of flexibility.
However, in the future, other output formats will be added.
One example is writing a value to a key in the state trie.

## Block
Block carries a pointer to the previous block, transaction list and a global+block state trie.
Transactions are stored in the block trie and arbitrary data in the global state trie.

## Consensus
The current version is fixed to a single key/node, but it would be nice to use VDFs on something PoS based.