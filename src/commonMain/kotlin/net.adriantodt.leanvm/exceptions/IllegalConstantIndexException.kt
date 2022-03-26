package net.adriantodt.leanvm.exceptions

public class IllegalConstantIndexException(index: Int) : IllegalArgumentException(
    "Index #$index is not a valid constant"
)
