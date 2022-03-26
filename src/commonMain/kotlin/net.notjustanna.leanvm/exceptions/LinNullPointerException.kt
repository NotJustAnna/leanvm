package net.notjustanna.leanvm.exceptions

public class LinNullPointerException : NullPointerException(), LinNativeException {
    override val exceptionType: String
        get() = "nullPointer"

    override val exceptionDescription: String
        get() = "Argument passed is null."
}
