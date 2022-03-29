package net.adriantodt.leanvm.exceptions

import net.adriantodt.leanvm.StackTrace

public class MalformedBytecodeException(message: String, trace: List<StackTrace>) : LeanMachineException(message, trace)

