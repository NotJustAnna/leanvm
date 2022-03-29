package net.adriantodt.leanvm.exceptions

import net.adriantodt.leanvm.StackTrace

public class LeanUnsupportedOperationException(
    message: String, trace: List<StackTrace>,
) : LeanMachineException(message, trace)
