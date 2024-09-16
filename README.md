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

## Model

### Transaction

- version, byte: tx model version. Current one is 0
- inputs, List<TransactionInput>: a reference to which transaction outputs are being spent and signatures proving
  ownership
- outputs, List<TransactionOutput>: amount and sha3-256 of the public key that can claim a designated amount
- There is no script or anything programmable.
    - Instead, inputs and outputs formats are fixed to make it easier to store and parse at the expense of flexibility.

#### Transaction Outputs

- byte type; Which output type. For now, there is only one assigned to 0.
- Bytes32 targetHash; The SHA3-256 hash of the public key that owns this output.
- long amount; How much is being transferred. Should be positive.
- Other output formats are expected. Such as:
    - Writing a value to a key in the state trie.
    - Off-chain contract creation and proof of execution with finalized state.

#### Transaction Inputs

- Bytes32 txHash; The hash of the transaction that holds the output being spent
- int txOutIdx; The index of the transaction output from the outputs list of the refereced transaction
- byte[] publicKeyBytes; A public key that matches the published output owner hash
- byte[] signature; secp256k signature of the whole transaction, showing agreement and proving ownership of the key

### Block

- byte VERSION; 0 for now
- Bytes32 transactionsRootHash; The merkle root hash for all transactions in the current block
- Bytes32 globalStateRootHash; The global key-value store patricia merkle trie root hash. (Should become a Verkle tree)
- Bytes32 previousHash; The previous block SHA3-256 hash.
- List<Transaction> txs; List of Transactions included in this block.
- byte[] signature; Signature from the block proposer.
- byte[] signerPubkey; Proposer public key.
