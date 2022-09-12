package net.notjustanna.leanvm.context

import net.notjustanna.leanvm.StackTrace
import net.notjustanna.leanvm.types.LAny

/**
 * A [net.notjustanna.leanvm.LeanMachine] context.
 * It is used to receive events and to provide information to the machine.
 */
public interface LeanContext {
    /**
     * The [LeanRuntime] currently being used by this context.
     *
     * This is used by [net.notjustanna.leanvm.LeanMachine] in order to handle platform exceptions.
     */
    public val runtime: LeanRuntime

    /**
     * Function called by [net.notjustanna.leanvm.LeanMachine] when the machine is ready to execute a new instruction.
     */
    public fun step()

    /**
     * Function called by [net.notjustanna.leanvm.LeanMachine] when the context pushed by this context finished executing
     * by returning from a function or by said function finishing executing.
     */
    public fun onReturn(value: LAny)

    /**
     * Function called by [net.notjustanna.leanvm.LeanMachine] when the context pushed by this context finished executing
     * by throwing an exception. (or not catching an exception coming from a context above)
     */
    public fun onThrow(value: LAny)

    /**
     * Function called by [net.notjustanna.leanvm.LeanMachine] when a stack trace is requested.
     * This should return a [StackTrace] containing the current stack trace, or null if there is no stack trace
     * related to this context.
     *
     * This function should not call [net.notjustanna.leanvm.LeanMachine.stackTrace] or [LeanMachineControl.stackTrace]
     * as it will cause a stack overflow.
     */
    public fun trace(): StackTrace?
}
