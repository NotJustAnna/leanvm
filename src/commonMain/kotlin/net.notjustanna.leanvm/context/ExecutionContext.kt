package net.notjustanna.leanvm.context

import net.notjustanna.leanvm.*
import net.notjustanna.leanvm.bytecode.LeanCode
import net.notjustanna.leanvm.bytecode.LeanInsn.Opcode
import net.notjustanna.leanvm.bytecode.LeanInsn.ParameterlessCode
import net.notjustanna.leanvm.bytecode.LeanNode
import net.notjustanna.leanvm.exceptions.Exceptions
import net.notjustanna.leanvm.exceptions.LeanUnsupportedOperationException
import net.notjustanna.leanvm.exceptions.StackUnderflowException
import net.notjustanna.leanvm.types.*

public class ExecutionContext(
    private val access: LeanMachineAccess,
    private var scope: Scope,
    private val source: LeanCode,
    private val functionName: String,
    private val node: LeanNode = source.node(0),
    private val thisValue: LAny? = null,
) : LeanContext {
    private var next: Int = 0
    private val stack: MutableList<LAny> = mutableListOf()
    private val exceptionHandlers: MutableList<ExceptionHandler> = mutableListOf()
    private val loopHandlers: MutableList<LoopHandler> = mutableListOf()

    override fun step() {
        val insn = node.insnOrNull(next++)

        if (insn == null) {
            access.onReturn(stack.removeLastOrNull() ?: LNull)
            return
        }

        when (Opcode.values()[insn.opcode]) {
            Opcode.PARAMETERLESS -> when (ParameterlessCode.values()[insn.immediate]) {
                ParameterlessCode.ARRAY_INSERT -> {
                    val value = popStack()
                    val array = peekStack() as? LArray ?: error("Value is not an LArray.")
                    array.value.add(value)
                }
                ParameterlessCode.BREAK -> {
                    // TODO error on empty loop handlers
                    val last = loopHandlers.removeLast()
                    next = last.jumpOnBreak
                }
                ParameterlessCode.CONTINUE -> {
                    // TODO error on empty loop handlers
                    val last = loopHandlers.removeLast()
                    next = last.jumpOnContinue
                }
                ParameterlessCode.DUP -> {
                    stack.add(peekStack())
                }
                ParameterlessCode.NEW_ARRAY -> {
                    stack.add(LArray())
                }
                ParameterlessCode.NEW_OBJECT -> {
                    stack.add(LObject())
                }
                ParameterlessCode.OBJECT_INSERT -> {
                    val value = popStack()
                    val key = popStack()
                    val obj = peekStack() as? LObject ?: error("Value is not an LObject.")
                    obj.value[key] = value
                }
                ParameterlessCode.POP -> {
                    popStack()
                }
                ParameterlessCode.POP_SCOPE -> {
                    scope = scope.parent ?: error("Can't pop root scope.")
                }
                ParameterlessCode.POP_EXCEPTION_HANDLING -> {
                    exceptionHandlers.removeLast()
                }
                ParameterlessCode.POP_LOOP_HANDLING -> {
                    loopHandlers.removeLast()
                }
                ParameterlessCode.PUSH_NULL -> {
                    stack.add(LNull)
                }
                ParameterlessCode.PUSH_SCOPE -> {
                    scope = Scope(scope)
                }
                ParameterlessCode.PUSH_THIS -> {
                    stack.add(thisValue ?: error("There's no 'this' defined."))
                }
                ParameterlessCode.RETURN -> {
                    access.onReturn(popStack())
                }
                ParameterlessCode.THROW -> {
                    onThrow(popStack())
                }
                ParameterlessCode.TYPEOF -> {
                    stack.add(LString(popStack().type.toString()))
                }
                ParameterlessCode.PUSH_TRUE -> {
                    stack.add(LTrue)
                }
                ParameterlessCode.PUSH_FALSE -> {
                    stack.add(LFalse)
                }
                ParameterlessCode.UNARY_POSITIVE -> handleUnaryPositiveOperation()
                ParameterlessCode.UNARY_NEGATIVE -> handleUnaryNegativeOperation()
                ParameterlessCode.UNARY_TRUTH -> handleUnaryTruthOperation()
                ParameterlessCode.UNARY_NOT -> handleUnaryNotOperation()
                ParameterlessCode.BINARY_ADD -> handleBinaryAddOperation()
                ParameterlessCode.BINARY_SUBTRACT -> handleBinarySubtractOperation()
                ParameterlessCode.BINARY_MULTIPLY -> handleBinaryMultiplyOperation()
                ParameterlessCode.BINARY_DIVIDE -> handleBinaryDivideOperation()
                ParameterlessCode.BINARY_REMAINING -> handleBinaryRemainingOperation()
                ParameterlessCode.BINARY_EQUALS -> handleBinaryEqualsOperation()
                ParameterlessCode.BINARY_NOT_EQUALS -> handleBinaryNotEqualsOperation()
                ParameterlessCode.BINARY_LT -> handleBinaryComparison(LT)
                ParameterlessCode.BINARY_LTE -> handleBinaryComparison(LTE)
                ParameterlessCode.BINARY_GT -> handleBinaryComparison(GT)
                ParameterlessCode.BINARY_GTE -> handleBinaryComparison(GTE)
                ParameterlessCode.BINARY_IN -> handleBinaryInOperation()
                ParameterlessCode.BINARY_RANGE -> handleBinaryRangeOperation()
            }
            Opcode.ASSIGN -> {
                scope.set(source.sConst(insn.immediate), popStack())
            }
            Opcode.BRANCH_IF_FALSE -> handleBranchIf(false, insn.immediate)
            Opcode.BRANCH_IF_TRUE -> handleBranchIf(true, insn.immediate)
            Opcode.DECLARE_VARIABLE_IMMUTABLE -> {
                scope.define(source.sConst(insn.immediate), false)
            }
            Opcode.DECLARE_VARIABLE_MUTABLE -> {
                scope.define(source.sConst(insn.immediate), true)
            }
            Opcode.GET_MEMBER_PROPERTY -> handleGetMemberProperty(insn.immediate)
            Opcode.GET_SUBSCRIPT -> handleGetSubscript(insn.immediate)
            Opcode.GET_VARIABLE -> {
                stack.add(scope.get(source.sConst(insn.immediate)))
            }
            Opcode.INVOKE -> handleInvoke(insn.immediate)
            Opcode.INVOKE_LOCAL -> handleInvokeLocal(insn.immediate)
            Opcode.INVOKE_MEMBER -> handleInvokeMember(insn.immediate)
            Opcode.JUMP -> {
                next = node.findJump(insn.immediate)?.at ?: error("Label ${insn.immediate} was not found.")
            }
            Opcode.LOAD_DECIMAL -> {
                stack.add(LDecimal(Double.fromBits(source.lConst(insn.immediate))))
            }
            Opcode.LOAD_INTEGER -> {
                stack.add(LInteger(source.lConst(insn.immediate)))
            }
            Opcode.LOAD_STRING -> {
                stack.add(LString(source.sConst(insn.immediate)))
            }
            Opcode.NEW_FUNCTION -> {
                stack.add(LCompiledFunction(source, source.func(insn.immediate), scope))
            }
            Opcode.PUSH_CHAR -> {
                val value = insn.immediate.toChar()
                if (value != (-1).toChar()) {
                    stack.add(LString(value.toString()))
                } else {
                    stack.add(LString(""))
                }
            }
            Opcode.PUSH_DECIMAL -> {
                stack.add(LDecimal(insn.immediate.toDouble()))
            }
            Opcode.PUSH_INTEGER -> {
                stack.add(LInteger(insn.immediate.toLong()))
            }
            Opcode.PUSH_EXCEPTION_HANDLING -> handlePushExceptionHandling(insn.immediate)
            Opcode.PUSH_LOOP_HANDLING -> handlePushLoopHandling(insn.immediate)
            Opcode.SET_MEMBER_PROPERTY -> handleSetMemberProperty(insn.immediate)
            Opcode.SET_SUBSCRIPT -> handleSetSubscript(insn.immediate)
            Opcode.SET_VARIABLE -> {
                scope.set(source.sConst(insn.immediate), popStack())
            }
        }
    }

    override fun onReturn(value: LAny) {
        stack.add(value)
    }

    override fun onThrow(value: LAny) {
        val handler = exceptionHandlers.removeLastOrNull()
        if (handler == null) {
            access.onThrow(value)
            return
        }
        if (handler.keepOnStack < stack.size) {
            println("WTF? Stack is missing ${handler.keepOnStack - stack.size} items!! This is probably a bug!")
        } else if (handler.keepOnStack > stack.size) {
            repeat(handler.keepOnStack - stack.size) { popStack() }
        }
        next = handler.jumpOnException
        stack.add(value)
    }

    override fun trace(): StackTrace? {
        val label = node.findSect(next - 1) ?: return null
        val section = source.sectOrNull(label.index) ?: return null
        return StackTrace(functionName, source.sConst(section.nameConst), section.line, section.column)
    }

    public data class ExceptionHandler(val keepOnStack: Int, val jumpOnException: Int, val jumpOnEnd: Int)
    public data class LoopHandler(val keepOnStack: Int, val jumpOnBreak: Int, val jumpOnContinue: Int)

    private fun popStack(): LAny {
        return stack.removeLastOrNull()
            ?: throw StackUnderflowException("Tried to remove an item from the stack, but the stack is empty.")
    }

    private fun peekStack(): LAny {
        return stack.lastOrNull()
            ?: throw StackUnderflowException("Tried to get the last item from the stack, but the stack is empty.")
    }

    // handlers

    private fun handleBranchIf(value: Boolean, labelCode: Int) {
        if (popStack().truth() == value) {
            next = node.findJump(labelCode)?.at ?: error("Label $labelCode was not found.")
        }
    }

    private fun handleGetMemberProperty(nameConst: Int) {
        val target = popStack()
        val name = source.sConst(nameConst)
        val member = access.runtime.getMember(target, name)
        if (member != null) {
            stack.add(member)
        }
        access.runtime.noElementExists(access, target.type, name)
    }

    private fun handleGetSubscript(size: Int) {
        val arguments = List(size) { popStack() }.reversed()
        val parent = popStack()
        if (parent is LArray && size == 1) {
            val arg = arguments.first()
            val list = parent.value
            val listSize = list.size
            val lastIndex = list.lastIndex
            if (arg is LInteger) {
                val index = arg.value
                if (index < -listSize || index > lastIndex) {
                    TODO("Error: argument is out of bounds")
                }

                stack.add(list[if (index < 0) listSize + index.toInt() else index.toInt()])

                return
            } else if (arg is LRange) {
                val start = arg.value.first
                val end = arg.value.last
                if (start < -listSize || end < -listSize || start > lastIndex || end > lastIndex) {
                    TODO("Error: argument is out of bounds")
                }
                // TODO There's probably a faster way to do this.
                val select = arg.value.mapTo(mutableListOf()) {
                    list[if (it < 0) listSize + it.toInt() else it.toInt()]
                }
                stack.add(LArray(select))
                return
            }
        }
        if (parent is LObject && size == 1) {
            val arg = arguments.first()
            val element = parent.value[arg]
            if (element != null) {
                stack.add(element)
                return
            }
        }
        if (parent is LString && size == 1) {
            val arg = arguments.first()
            val string = parent.value
            val length = string.length
            val lastIndex = string.lastIndex
            if (arg is LInteger) {
                val index = arg.value
                if (index < -length || index > lastIndex) {
                    TODO("Error: argument is out of bounds")
                }
                stack.add(LString(string[if (index < 0) length + index.toInt() else index.toInt()].toString()))
                return
            } else if (arg is LRange) {
                val start = arg.value.first
                val end = arg.value.last
                if (start < -length || end < -length || start > lastIndex || end > lastIndex) {
                    TODO("Error: argument is out of bounds")
                }
                // TODO There's probably a faster way to do this.
                val select = arg.value.map {
                    string[if (it < 0) length + it.toInt() else it.toInt()]
                }
                stack.add(LString(select.toCharArray().concatToString()))
                return
            }
        }
        TODO("Not yet implemented: GetSubscript -> $parent$arguments")
    }

    private fun handleInvoke(size: Int) {
        val arguments = List(size) { popStack() }.reversed()
        val function = popStack()
        invocation(null, function, arguments)
    }

    private fun handleInvokeLocal(immediate: Int) {
        val nameConst: Int = immediate shr 16
        val size: Int = immediate and 0xff

        val arguments = List(size) { popStack() }.reversed()
        val function = scope.get(source.sConst(nameConst))
        invocation(null, function, arguments)
    }

    private fun handleInvokeMember(immediate: Int) {
        val nameConst: Int = immediate shr 16
        val size: Int = immediate and 0xff

        val arguments = List(size) { popStack() }.reversed()
        val parent = popStack()
        val function = access.runtime.getMember(parent, source.sConst(nameConst)) ?: LNull
        invocation(parent, function, arguments)
    }

    private fun handlePushExceptionHandling(immediate: Int) {
        val catchLabel: Int = immediate shr 12
        val endLabel: Int = immediate and 0xfff

        exceptionHandlers.add(ExceptionHandler(
            stack.size,
            node.findJump(catchLabel)?.at ?: error("Catch Label $catchLabel was not found."),
            node.findJump(endLabel)?.at ?: error("End Label $catchLabel was not found."),
        ))
    }

    private fun handlePushLoopHandling(immediate: Int) {
        val breakLabel: Int = immediate shr 12
        val continueLabel: Int = immediate and 0xfff

        loopHandlers.add(LoopHandler(
            stack.size,
            node.findJump(breakLabel)?.at ?: error("Break Label $breakLabel was not found."),
            node.findJump(continueLabel)?.at ?: error("Continue Label $continueLabel was not found."),
        ))
    }

    private fun handleSetMemberProperty(nameConst: Int) {
        val value = popStack()
        val s = source.sConst(nameConst)
        val parent = popStack()
        if (parent is LObject) {
            parent.value[LString(s)] = value
        }
        TODO("Not yet implemented: SetMember $parent.$s = $value")
    }

    private fun handleSetSubscript(size: Int) {
        val value = popStack()
        val arguments = List(size) { popStack() }.reversed()
        val parent = popStack()
        if (parent is LArray && size == 1) {
            val arg = arguments.first()
            if (arg is LInteger) {
                parent.value[arg.value.toInt()] = value
                return
            }
        }
        if (parent is LObject && size == 1) {
            val arg = arguments.first()
            parent.value[arg] = value
            return
        }
        TODO("Not yet implemented: SetSubscript -> $parent$arguments = $value")
    }

    private fun handleBinaryAddOperation() {
        val right = popStack()
        val left = popStack()
        if (left is LString || right is LString) {
            stack.add(LString(left.toString() + right.toString()))
            return
        }
        if (left is LArray && right is LArray) {
            stack.add(LArray((left.value + right.value).toMutableList()))
            return
        }
        if (left is LNumber && right is LNumber) {
            stack.add(left + right)
            return
        }
        throw LeanUnsupportedOperationException("add", left.type.toString(), right.type.toString())
    }

    private fun handleBinaryDivideOperation() {
        val right = popStack()
        val left = popStack()
        if (left is LNumber && right is LNumber) {
            stack.add(left / right)
            return
        }
        throw LeanUnsupportedOperationException("divide", left.type.toString(), right.type.toString())
    }

    private fun handleBinaryEqualsOperation() {
        val right = popStack()
        val left = popStack()
        stack.add(LAny.ofBoolean(right == left))
    }

    private fun handleBinaryMultiplyOperation() {
        val right = popStack()
        val left = popStack()
        if (left is LString && right is LInteger) {
            stack.add(LString(left.value.repeat(right.value.toInt())))
        }
        if (left is LNumber && right is LNumber) {
            stack.add(left * right)
            return
        }
        throw LeanUnsupportedOperationException("multiply", left.type.toString(), right.type.toString())
    }

    private fun handleBinaryNotEqualsOperation() {
        val right = popStack()
        val left = popStack()
        stack.add(LAny.ofBoolean(right != left))
    }

    private fun handleBinaryRangeOperation() {
        val right = popStack()
        val left = popStack()
        if (left is LInteger && right is LInteger) {
            stack.add(left..right)
            return
        }
        throw LeanUnsupportedOperationException("range", left.type.toString(), right.type.toString())
    }

    private fun handleBinaryRemainingOperation() {
        val right = popStack()
        val left = popStack()
        if (left is LNumber && right is LNumber) {
            stack.add(left % right)
            return
        }
        throw LeanUnsupportedOperationException("remaining", left.type.toString(), right.type.toString())
    }

    private fun handleBinarySubtractOperation() {
        val right = popStack()
        val left = popStack()
        if (left is LNumber && right is LNumber) {
            stack.add(left - right)
            return
        }
        throw LeanUnsupportedOperationException("subtract", left.type.toString(), right.type.toString())
    }

    private fun handleBinaryComparison(comparison: (Int) -> Boolean) {
        val right = popStack()
        val left = popStack()
        if (left is LString && right is LString) {
            stack.add(LAny.ofBoolean(comparison(left.value.compareTo(right.value))))
            return
        }
        if (left is LNumber && right is LNumber) {
            stack.add(LAny.ofBoolean(comparison(left.compareTo(right))))
            return
        }
        throw LeanUnsupportedOperationException("comparison", left.type.toString(), right.type.toString())
    }

    private fun handleBinaryInOperation() {
        val right = popStack()
        val left = popStack()
        if (right is LArray) {
            stack.add(LAny.ofBoolean(left in right.value))
            return
        }
        if (right is LObject) {
            stack.add(LAny.ofBoolean(left in right.value))
            return
        }
        throw LeanUnsupportedOperationException("in", left.type.toString(), right.type.toString())
    }

    private fun handleUnaryNegativeOperation() {
        val target = popStack()
        if (target is LNumber) {
            stack.add(-target)
            return
        }
        throw LeanUnsupportedOperationException("negative", target.type.toString())
    }

    private fun handleUnaryNotOperation() {
        stack.add(LAny.ofBoolean(!popStack().truth()))
    }

    private fun handleUnaryPositiveOperation() {
        val target = popStack()
        if (target is LNumber) {
            stack.add(+target)
            return
        }
        throw LeanUnsupportedOperationException("positive", target.type.toString())
    }

    private fun handleUnaryTruthOperation() {
        stack.add(LAny.ofBoolean(popStack().truth()))
    }

    private fun invocation(thisValue: LAny?, function: LAny, args: List<LAny>) {
        when (function) {
            is LNativeFunction -> {
                try {
                    stack.add(function.block(thisValue, args))
                } catch (e: Exception) {
                    val stackTrace = listOf(StackTrace(function.name ?: "<anonymous function>")) + access.stackTrace()
                    onThrow(
                        when (e) {
                            is LAnyException -> e.value
//                            is LeanNativeException -> Exceptions.toObject(e, stackTrace)
                            else -> Exceptions.fromNative(e, stackTrace)
                        }
                    )
                }
            }
            is LCompiledFunction -> {
                val layer = FunctionSetupContext(access, function, thisValue, args)
                access.push(layer)
                layer.step()
            }
            else -> {
                onThrow(Exceptions.notAFunction(function.type.toString(), access.stackTrace()))
            }
        }
    }

    private companion object {
        private val GT: (Int) -> Boolean = { it > 0 }
        private val GTE: (Int) -> Boolean = { it >= 0 }
        private val LT: (Int) -> Boolean = { it < 0 }
        private val LTE: (Int) -> Boolean = { it <= 0 }
    }
}
