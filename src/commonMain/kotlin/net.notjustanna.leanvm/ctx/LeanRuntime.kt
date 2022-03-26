package net.notjustanna.leanvm.ctx

import net.notjustanna.leanvm.exceptions.LeanUnsupportedOperationException
import net.notjustanna.leanvm.types.LAny
import net.notjustanna.leanvm.types.LNull
import net.notjustanna.leanvm.utils.Comparison

public open class LeanRuntime {

    public open fun getMember(target: LAny, name: String): LAny? {
        return null
    }

    /**
     * This method receives is called when the Lean Machine receives a Java-land exception and needs to convert the
     * exception to a Lean-land object.
     *
     * **WARNING**: This happens outside the main try-catch. Any exceptions thrown here will land **outside**
     * the Lean machine's scope, into the user's code.
     */
    public open fun handleJavaException(control: LeanMachineControl, e: Exception): LAny {
        TODO("Not yet implemented")
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
     * This function is called when an invocation is attempted on an object which is not a function.
     *
     * By default, this method throws an exception, but it can be overridden to add in custom behavior.
     */
    public fun customInvocation(control: LeanMachineControl, thisValue: LAny?, function: LAny, args: List<LAny>) {
        TODO("Not yet implemented")
    }

    /**
     * This function is called when an addiction is attempted on an unsupported pair.
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
     * This function is called when a division is attempted on an unsupported pair.
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
     * This function is called when a multiplication is attempted on an unsupported pair.
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
     * This function is called when a range is attempted on an unsupported pair.
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
     * This function is called when a modulo is attempted on an unsupported pair.
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
     * This function is called when a subtraction is attempted on an unsupported pair.
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
     * This function is called when a comparison than is attempted on an unsupported pair.
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
     * This function is called when a 'in' is attempted on an unsupported pair.
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
     * This function is called when a negation is attempted on an unsupported target.
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
     * This function is called when a positive is attempted on an unsupported target.
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
