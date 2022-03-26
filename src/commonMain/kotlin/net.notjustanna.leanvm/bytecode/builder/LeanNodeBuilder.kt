package net.notjustanna.leanvm.bytecode.builder

import net.notjustanna.leanvm.bytecode.*
import net.notjustanna.leanvm.bytecode.LeanInsn.Opcode.*
import net.notjustanna.leanvm.bytecode.LeanInsn.ParameterlessCode.*
import net.notjustanna.leanvm.utils.BinaryOperationType
import net.notjustanna.leanvm.utils.UnaryOperationType

public class LeanNodeBuilder(private val parent: LeanCodeBuilder, public val nodeId: Int) {
    private val insnArr = mutableListOf<LeanInsn>()
    private val jumpArr = mutableListOf<LeanJumpLabel>()
    private val sectArr = mutableListOf<LeanSectLabel>()
    private val sectStack = mutableListOf<Int>()
    private var lastSectInsn = 0
    private var nextLabelCode = 0

    public fun nextLabel(): Int {
        return nextLabelCode++
    }

    /**
     * Creates an array.
     *
     * Stack Inputs: ()
     *
     * Stack Outputs: (array)
     */
    public fun newArrayInsn() {
        insnArr += LeanInsn.parameterless(NEW_ARRAY)
    }

    /**
     * Pushes a value into the array.
     *
     * Stack Inputs: (array, value)
     *
     * Stack Outputs: (array)
     */
    public fun arrayInsertInsn() {
        insnArr += LeanInsn.parameterless(ARRAY_INSERT)
    }

    /**
     * Assigns a value to a variable.
     *
     * Stack Inputs: (value)
     *
     * Stack Outputs: ()
     */
    public fun assignInsn(name: String) {
        insnArr += LeanInsn.simple(ASSIGN, parent.constantId(name))
    }

    /**
     * Pushes a boolean into the stack.
     *
     * Stack Inputs: ()
     *
     * Stack Outputs: (value)
     */
    public fun pushBooleanInsn(value: Boolean) {
        insnArr += LeanInsn.parameterless(if (value) PUSH_TRUE else PUSH_FALSE)
    }

    /**
     * Pushes a double into the stack.
     *
     * Stack Inputs: ()
     *
     * Stack Outputs: (value)
     */
    public fun pushDecimalInsn(value: Double) {
        if (value % 1 == 0.0 && value.toInt() in i24Range) {
            insnArr += LeanInsn.simple(PUSH_DECIMAL, value.toInt())
            return
        }
        insnArr += LeanInsn.simple(LOAD_DECIMAL, parent.constantId(value))
    }

    /**
     * Pushes a long into the stack.
     *
     * Stack Inputs: ()
     *
     * Stack Outputs: (value)
     */
    public fun pushIntegerInsn(value: Long) {
        if (value % 1 == 0L && value.toInt() in i24Range) {
            insnArr += LeanInsn.simple(PUSH_INTEGER, value.toInt())
            return
        }
        insnArr += LeanInsn.simple(LOAD_INTEGER, parent.constantId(value))
    }

    /**
     * Invokes a function on the stack with arguments.
     *
     * Stack Inputs: (function, args...)
     *
     * Stack Outputs: (result)
     */
    public fun invokeInsn(size: Int) {
        insnArr += LeanInsn.bigSmall(INVOKE, 0, size)
    }

    /**
     * Loads a function from the scope and invokes it with arguments.
     *
     * Stack Inputs: (args...)
     *
     * Stack Outputs: (result)
     */
    public fun invokeLocalInsn(name: String, size: Int) {
        insnArr += LeanInsn.bigSmall(INVOKE_LOCAL, parent.constantId(name), size)
    }

    /**
     * Invokes a member function from an object on the stack with arguments.
     *
     * Stack Inputs: (object, args...)
     *
     * Stack Outputs: (result)
     */
    public fun invokeMemberInsn(name: String, size: Int) {
        insnArr += LeanInsn.bigSmall(INVOKE_MEMBER, parent.constantId(name), size)
    }

    /**
     * Pushes a string into the stack.
     *
     * Stack Inputs: ()
     *
     * Stack Outputs: (value)
     */
    public fun pushStringInsn(value: String) {
        if (value.length <= 1) {
            insnArr += LeanInsn.smallBig(PUSH_CHAR, 0, value.firstOrNull()?.code ?: -1)
            return
        }
        insnArr += LeanInsn.simple(LOAD_STRING, parent.constantId(value))
    }

    /**
     * Returns the value on the top of the stack.
     *
     * Stack Inputs: (value)
     */
    public fun returnInsn() {
        insnArr += LeanInsn.parameterless(RETURN)
    }

    /**
     * Pushes `this` into the stack.
     *
     * Stack Inputs: ()
     *
     * Stack Outputs: (this)
     */
    public fun pushThisInsn() {
        insnArr += LeanInsn.parameterless(PUSH_THIS)
    }

    /**
     * Pushes `null` into the stack.
     *
     * Stack Inputs: ()
     *
     * Stack Outputs: (null)
     */
    public fun pushNullInsn() {
        insnArr += LeanInsn.parameterless(PUSH_NULL)
    }

    /**
     * Pushes a string representing the type of the value on the top of the stack.
     *
     * Stack Inputs: (value)
     *
     * Stack Outputs: (type of value)
     */
    public fun typeofInsn() {
        insnArr += LeanInsn.parameterless(TYPEOF)
    }

    /**
     * Creates an object.
     *
     * Stack Inputs: ()
     *
     * Stack Outputs: (array)
     */
    public fun newObjectInsn() {
        insnArr += LeanInsn.parameterless(NEW_OBJECT)
    }

    /**
     * Pushes an entry into the object.
     *
     * Stack Inputs: (object, key, value)
     *
     * Stack Outputs: (object)
     */
    public fun objectInsertInsn() {
        insnArr += LeanInsn.parameterless(OBJECT_INSERT)
    }

    /**
     * Breaks a loop.
     *
     * Stack Inputs: ()
     */
    public fun breakInsn() {
        insnArr += LeanInsn.parameterless(BREAK)
    }

    /**
     * Continues a loop.
     *
     * Stack Inputs: ()
     */
    public fun continueInsn() {
        insnArr += LeanInsn.parameterless(CONTINUE)
    }

    /**
     * Throws the object from the top of the stack.
     *
     * Stack Inputs: (value)
     */
    public fun throwInsn() {
        insnArr += LeanInsn.parameterless(THROW)
    }

    /**
     * Jumps to the label specified.
     */
    public fun jumpInsn(labelCode: Int) {
        insnArr += LeanInsn.simple(JUMP, labelCode)
    }

    /**
     * Branches to the label if the top value of the stack is false (according to the truth rules).
     *
     * Stack Inputs: (value)
     *
     * Stack Outputs: ()
     */
    public fun branchIfFalseInsn(labelCode: Int) {
        insnArr += LeanInsn.simple(BRANCH_IF_FALSE, labelCode)
    }

    /**
     * Branches to the label if the top value of the stack is true (according to the truth rules).
     *
     * Stack Inputs: (value)
     *
     * Stack Outputs: ()
     */
    public fun branchIfTrueInsn(labelCode: Int) {
        insnArr += LeanInsn.simple(BRANCH_IF_TRUE, labelCode)
    }

    public fun unaryOperationInsn(operator: UnaryOperationType) {
        insnArr += LeanInsn.parameterless(
            when (operator) {
                UnaryOperationType.POSITIVE -> UNARY_POSITIVE
                UnaryOperationType.NEGATIVE -> UNARY_NEGATIVE
                UnaryOperationType.NOT -> UNARY_NOT
                UnaryOperationType.TRUTH -> UNARY_TRUTH
            }
        )
    }

    public fun binaryOperationInsn(operator: BinaryOperationType) {
        insnArr += LeanInsn.parameterless(
            when (operator) {
                BinaryOperationType.ADD -> BINARY_ADD
                BinaryOperationType.SUBTRACT -> BINARY_SUBTRACT
                BinaryOperationType.MULTIPLY -> BINARY_MULTIPLY
                BinaryOperationType.DIVIDE -> BINARY_DIVIDE
                BinaryOperationType.REMAINING -> BINARY_REMAINING
                BinaryOperationType.EQUALS -> BINARY_EQUALS
                BinaryOperationType.NOT_EQUALS -> BINARY_NOT_EQUALS
                BinaryOperationType.LT -> BINARY_LT
                BinaryOperationType.LTE -> BINARY_LTE
                BinaryOperationType.GT -> BINARY_GT
                BinaryOperationType.GTE -> BINARY_GTE
                BinaryOperationType.IN -> BINARY_IN
                BinaryOperationType.RANGE -> BINARY_RANGE
                else -> throw RuntimeException("The operator $operator can't be converted to a instruction and must be de-sugared.")
            }
        )
    }

    public fun declareVariableInsn(name: String, mutable: Boolean) {
        insnArr += LeanInsn.simple(
            if (mutable) DECLARE_VARIABLE_MUTABLE else DECLARE_VARIABLE_IMMUTABLE,
            parent.constantId(name)
        )
    }

    /**
     * Loads a variable from the scope into the stack.
     *
     * Stack Inputs: ()
     *
     * Stack Outputs: (value)
     */
    public fun getVariableInsn(name: String) {
        insnArr += LeanInsn.simple(GET_VARIABLE, parent.constantId(name))
    }

    public fun setVariableInsn(name: String) {
        insnArr += LeanInsn.simple(SET_VARIABLE, parent.constantId(name))
    }

    public fun getMemberPropertyInsn(name: String) {
        insnArr += LeanInsn.simple(GET_MEMBER_PROPERTY, parent.constantId(name))
    }

    public fun setMemberPropertyInsn(name: String) {
        insnArr += LeanInsn.simple(SET_MEMBER_PROPERTY, parent.constantId(name))
    }

    public fun getSubscriptInsn(size: Int) {
        insnArr += LeanInsn.bigSmall(GET_SUBSCRIPT, 0, size)
    }

    public fun setSubscriptInsn(size: Int) {
        insnArr += LeanInsn.bigSmall(SET_SUBSCRIPT, 0, size)
    }

    public fun newFunctionInsn(name: String?, bodyId: Int, varargsParam: Int, parameters: List<LeanParamDecl>) {
        insnArr += LeanInsn.simple(
            NEW_FUNCTION,
            parent.registerFunction(
                name,
                bodyId,
                varargsParam,
                parameters
            )
        )
    }

    public fun dupInsn() {
        insnArr += LeanInsn.parameterless(DUP)
    }

    public fun popInsn() {
        insnArr += LeanInsn.parameterless(POP)
    }

    public fun pushScopeInsn() {
        insnArr += LeanInsn.parameterless(PUSH_SCOPE)
    }

    public fun popScopeInsn() {
        insnArr += LeanInsn.parameterless(POP_SCOPE)
    }

    public fun pushExceptionHandlingInsn(catchLabel: Int, endLabel: Int) {
        insnArr += LeanInsn.double(PUSH_EXCEPTION_HANDLING, catchLabel, endLabel)
    }

    public fun popExceptionHandlingInsn() {
        insnArr += LeanInsn.parameterless(POP_EXCEPTION_HANDLING)
    }

    public fun pushLoopHandlingInsn(continueLabel: Int, breakLabel: Int) {
        insnArr += LeanInsn.double(PUSH_LOOP_HANDLING, continueLabel, breakLabel)
    }

    public fun popLoopHandlingInsn() {
        insnArr += LeanInsn.parameterless(POP_LOOP_HANDLING)
    }

    /**
     * Marks a label.
     *
     * This does not produce an instruction, but rather a label.
     */
    public fun markLabel(code: Int) {
        jumpArr += LeanJumpLabel(code = code, at = insnArr.size)
    }

    public fun markSectionStart(sectionId: Int) {
        val last = sectStack.lastOrNull()
        sectStack.add(sectionId)
        if (last != null) generateSectionLabel(last)
    }

    public fun markSectionEnd() {
        val last = sectStack.removeLast()
        generateSectionLabel(last)
    }

    public inline fun markSection(sectionId: Int, block: () -> Unit) {
        markSectionStart(sectionId)
        block()
        markSectionEnd()
    }

    /**
     * Automatically pushes/pops the required exception handlers.
     */
    public inline fun withExceptionHandling(catchLabel: Int, endLabel: Int, block: () -> Unit) {
        pushExceptionHandlingInsn(catchLabel, endLabel)
        block()
        popExceptionHandlingInsn()
    }

    /**
     * Automatically pushes/pops the required loop handlers.
     */
    public inline fun withLoopHandling(continueLabel: Int, breakLabel: Int, block: () -> Unit) {
        pushLoopHandlingInsn(continueLabel, breakLabel)
        block()
        popLoopHandlingInsn()
    }

    /**
     * Automatically pushes/pops the required scope instructions.
     */
    public inline fun withScope(block: () -> Unit) {
        pushScopeInsn()
        block()
        popScopeInsn()
    }

    private fun generateSectionLabel(lastSectionId: Int) {
        val currSectionInsn = insnArr.size
        if (lastSectInsn < currSectionInsn) {
            val length = currSectionInsn - lastSectInsn
            sectArr.add(LeanSectLabel(currSectionInsn, length, lastSectionId))
            lastSectInsn = currSectionInsn
        }
    }

    public fun build(): LeanNode {
        if (sectStack.isNotEmpty()) {
            println("This should not have happened.")
            generateSectionLabel(sectStack.last())
        }
        return LeanNode.create(insnArr.toList(), jumpArr.toList(), sectArr.toList())
    }

    public companion object {
        private const val I24_MAX = 0x7FFFFF
        private const val I24_MIN = -0x800000
        private val i24Range = I24_MIN..I24_MAX
    }
}
