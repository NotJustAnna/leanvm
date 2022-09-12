@file:Suppress("UNUSED_PARAMETER")

package net.adriantodt.leanvm.context

import net.adriantodt.leanvm.exceptions.LeanRuntimeException
import net.adriantodt.leanvm.exceptions.LeanUnsupportedOperationException
import net.adriantodt.leanvm.types.*
import net.adriantodt.leanvm.utils.Comparison

/**
 * The runtime context for the LeanVM virtual machine.
 *
 * This class can be used to customize the behavior of the virtual machine.
 */
public open class LeanRuntime {

    public open fun getMemberProperty(control: LeanMachineControl, target: LAny, name: String): LAny {
        TODO("Not yet implemented")
    }

    public open fun setMemberProperty(control: LeanMachineControl, parent: LAny, name: String, value: LAny) {
        TODO("Not yet implemented")
    }

    /**
     * This method receives is called when the Lean Machine receives a platform exception and needs to convert the
     * exception to a Lean-land object.
     *
     * **WARNING**: This happens outside the main try-catch. Any exceptions thrown here will land **outside**
     * the Lean machine's scope, into the user's code.
     */
    public open fun handlePlatformException(control: LeanMachineControl, exception: Exception): LAny {
        val stackTrace = LString("stackTrace")
        val message = LString("message")
        val errorType = LString("errorType")
        val platformException = LString("platformException")

        if (exception is LeanRuntimeException) {
            return LObject(
                errorType to LString(exception.leanExceptionName),
                message to LString(exception.message),
                stackTrace to LArray(exception.leanStackTrace.mapTo(mutableListOf()) { LString(it.toString()) }),
                platformException to LObject(
                    errorType to LString(exception::class.simpleName ?: "<anonymous exception>"),
                    message to LString(exception.message),
                    stackTrace to LString(exception.stackTraceToString())
                )
            )
        }

        return LObject(
            errorType to platformException,
            message to LString("A platform exception occurred."),
            stackTrace to LArray(control.stackTrace().mapTo(mutableListOf()) { LString(it.toString()) }),
            platformException to LObject(
                errorType to LString(exception::class.simpleName ?: "<anonymous exception>"),
                message to (exception.message?.let(::LString) ?: LNull),
                stackTrace to LString(exception.stackTraceToString())
            )
        )
    }

    /**
     * This method is called when the 'thisValue' of a node is null.
     *
     * By default, this method returns LNull, but it can be overridden to return other value or throw an exception.
     */
    public open fun customThisValue(control: LeanMachineControl): LAny {
        return LNull
    }

    /**
     * This method is called when a subscript get is attempted on a value which is not an array, object, or string.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customGetSubscript(control: LeanMachineControl, parent: LAny, arguments: List<LAny>): LAny {
        throw LeanUnsupportedOperationException(
            "The 'subscript get' operation is not supported for '${parent}' (of type '${parent.type}').",
            control.stackTrace()
        )
    }

    /**
     * This method is called when a subscript set is attempted on a value which is not an array, object, or string.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customSetSubscript(control: LeanMachineControl, parent: LAny, arguments: List<LAny>, value: LAny) {
        throw LeanUnsupportedOperationException(
            "The 'subscript set' operation is not supported for '${parent}' (of type '${parent.type}').",
            control.stackTrace()
        )
    }

    /**
     * This method is called when an invocation is attempted on a value which is not a function.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public fun customInvocation(control: LeanMachineControl, thisValue: LAny?, function: LAny, args: List<LAny>) {
        throw LeanUnsupportedOperationException(
            "Invocation is not supported for '${function}' (of type '${function.type}').",
            control.stackTrace()
        )
    }

    /**
     * This method is called when an addiction is attempted on an unsupported pair of values.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customAddOperation(control: LeanMachineControl, left: LAny, right: LAny): LAny {
        throw LeanUnsupportedOperationException(
            "The operation '+' is not supported for '${left}' (of type ${left.type}) and '${right}' (of type ${right.type})",
            control.stackTrace()
        )
    }

    /**
     * This method is called when a division is attempted on an unsupported pair of values.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customDivideOperation(control: LeanMachineControl, left: LAny, right: LAny): LAny {
        throw LeanUnsupportedOperationException(
            "The operation '/' is not supported for '${left}' (of type ${left.type}) and '${right}' (of type ${right.type})",
            control.stackTrace()
        )
    }

    /**
     * This method is called when a multiplication is attempted on an unsupported pair of values.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customMultiplyOperation(control: LeanMachineControl, left: LAny, right: LAny): LAny {
        throw LeanUnsupportedOperationException(
            "The operation '*' is not supported for '${left}' (of type ${left.type}) and '${right}' (of type ${right.type})",
            control.stackTrace()
        )
    }

    /**
     * This method is called when a range is attempted on an unsupported pair of values.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customRangeOperation(control: LeanMachineControl, left: LAny, right: LAny): LAny {
        throw LeanUnsupportedOperationException(
            "The operation '..' is not supported for '${left}' (of type ${left.type}) and '${right}' (of type ${right.type})",
            control.stackTrace()
        )
    }

    /**
     * This method is called when a modulo is attempted on an unsupported pair of values.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customRemainingOperation(control: LeanMachineControl, left: LAny, right: LAny): LAny {
        throw LeanUnsupportedOperationException(
            "The operation '%' is not supported for '${left}' (of type ${left.type}) and '${right}' (of type ${right.type})",
            control.stackTrace()
        )
    }

    /**
     * This method is called when a subtraction is attempted on an unsupported pair of values.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customSubtractOperation(control: LeanMachineControl, left: LAny, right: LAny): LAny {
        throw LeanUnsupportedOperationException(
            "The operation '-' is not supported for '${left}' (of type ${left.type}) and '${right}' (of type ${right.type})",
            control.stackTrace()
        )
    }

    /**
     * This method is called when a comparison than is attempted on an unsupported pair of values.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customComparison(control: LeanMachineControl, cmp: Comparison, left: LAny, right: LAny): LAny {
        throw LeanUnsupportedOperationException(
            "The operation '${cmp}' is not supported for '${left}' (of type ${left.type}) and '${right}' (of type ${right.type})",
            control.stackTrace()
        )
    }

    /**
     * This method is called when a 'in' is attempted on an unsupported pair of values.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customInOperation(control: LeanMachineControl, left: LAny, right: LAny): Boolean {
        throw LeanUnsupportedOperationException(
            "The operation 'in' is not supported for '${left}' (of type ${left.type}) and '${right}' (of type ${right.type})",
            control.stackTrace()
        )
    }

    /**
     * This method is called when a negation is attempted on an unsupported target.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customNegativeOperation(control: LeanMachineControl, target: LAny): LAny {
        throw LeanUnsupportedOperationException(
            "The operation '-' is not supported for '${target}' (of type ${target.type})",
            control.stackTrace()
        )
    }

    /**
     * This method is called when a positive is attempted on an unsupported target.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public open fun customPositiveOperation(control: LeanMachineControl, target: LAny): LAny {
        throw LeanUnsupportedOperationException(
            "The operation '+' is not supported for '${target}' (of type ${target.type})",
            control.stackTrace()
        )
    }
}
