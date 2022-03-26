package net.notjustanna.leanvm.exceptions

public class LeanUnsupportedOperationException : UnsupportedOperationException, LinNativeException {
    public constructor(operation: String, type: String) : super(
        "Cannot apply operation '$operation' for type '$type'."
    )

    public constructor(operation: String, leftType: String, rightType: String) : super(
        "Cannot apply operation '$operation' for types '$leftType' and '$rightType'."
    )

    override val exceptionType: String
        get() = "unsupportedOperation"

    override val exceptionDescription: String
        get() = message!!
}
