# jLikes

A toy blockchain for exchanging likes and a distributed key value store.

## Implemented features

- Transaction signing and validation
- Block signing and validation
- Transaction rules for avoiding double spend and overdraft
- Basic mempool support

## Missing but planned next

- Basic wallet logic for scanning block headers and managing balance
- local RPC interface
- P2P network between nodes
- Proper consensus rules

## Transaction format

- There is no script or anything programmable.
    - Instead, inputs and outputs formats are fixed to make it easier to store and parse at the expense of flexibility.
- Other output formats will be added. Such as:
    - Writing a value to a key in the state trie.
    - Off-chain contract creation and proof of execution with finalized state.