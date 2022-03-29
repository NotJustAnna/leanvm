package net.notjustanna.leanvm.exceptions

import net.notjustanna.leanvm.StackTrace

public class LeanIndexOutOfBoundsException(
    message: String, trace: List<StackTrace>,
) : LeanMachineException(message, trace)
