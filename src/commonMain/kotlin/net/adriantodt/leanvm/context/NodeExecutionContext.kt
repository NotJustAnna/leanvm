package net.adriantodt.leanvm.context

import net.adriantodt.leanvm.Scope
import net.adriantodt.leanvm.StackTrace
import net.adriantodt.leanvm.bytecode.LeanCode
import net.adriantodt.leanvm.bytecode.LeanInsn.Opcode
import net.adriantodt.leanvm.bytecode.LeanInsn.ParameterlessCode
import net.adriantodt.leanvm.bytecode.LeanNode
import net.adriantodt.leanvm.exceptions.LeanIndexOutOfBoundsException
import net.adriantodt.leanvm.exceptions.LeanNullPointerException
import net.adriantodt.leanvm.exceptions.MalformedBytecodeException
import net.adriantodt.leanvm.types.*
import net.adriantodt.leanvm.utils.Comparison

public class NodeExecutionContext(
    private val control: LeanMachineControl,
    private val code: LeanCode,
    private val functionName: String = "<main>",
    override val runtime: LeanRuntime = LeanRuntime(),
    private var scope: Scope = runtime.rootScope(),
    private val node: LeanNode = code.nodeArr.getOrElse(0) {
        throw MalformedBytecodeException(
            "Code does not contain an executable node.",
            control.stackTrace()
        )
    },
    private val thisValue: LAny? = null,
) : LeanContext {
    private var next: Int = 0
    private val stack: MutableList<LAny> = mutableListOf()
    private val exceptionHandlers: MutableList<ExceptionHandler> = mutableListOf()

    override fun step() {
        if (next >= node.insnArr.size) {
            control.onReturn(stack.removeLastOrNull() ?: LNull)
            return
        }
        val insn = node.insnArr.getOrElse(next++) {
            throw MalformedBytecodeException(
                "Tried to execute non-existent instruction at index $next.",
                control.stackTrace()
            )
        }

        when (Opcode.values()[insn.opcode]) {
            Opcode.PARAMETERLESS -> when (ParameterlessCode.values()[insn.immediate]) {
                ParameterlessCode.ARRAY_INSERT -> {
                    val value = popStack()
                    val array = peekStack()
                    if (array !is LArray) {
                        throw MalformedBytecodeException(
                            "Tried to arrayInsert value '$value' into '$array' which is of type '${array.type}'.",
                            control.stackTrace()
                        )
                    }
                    array.value.add(value)
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
                    val obj = peekStack()
                    if (obj !is LObject) {
                        throw MalformedBytecodeException(
                            "Tried to objectInsert key '$key' and value '$value' into '$obj' which is of type '${obj.type}'.",
                            control.stackTrace()
                        )
                    }
                    obj.value[key] = value
                }
                ParameterlessCode.POP -> {
                    popStack()
                }
                ParameterlessCode.POP_SCOPE -> {
                    scope = scope.parent ?: throw MalformedBytecodeException(
                        "Tried to pop scope but encountered a root scope.",
                        control.stackTrace()
                    )
                }
                ParameterlessCode.POP_EXCEPTION_HANDLING -> {
                    exceptionHandlers.removeLastOrNull() ?: throw MalformedBytecodeException(
                        "Tried execute POP_EXCEPTION_HANDLING instruction but found no exception handler.",
                        control.stackTrace()
                    )
                }
                ParameterlessCode.PUSH_NULL -> {
                    stack.add(LNull)
                }
                ParameterlessCode.PUSH_SCOPE -> {
                    scope = Scope(scope)
                }
                ParameterlessCode.PUSH_THIS -> {
                    stack.add(thisValue ?: runtime.customThisValue(control))
                }
                ParameterlessCode.RETURN -> {
                    control.onReturn(popStack())
                }
                ParameterlessCode.THROW -> {
                    onThrow(popStack())
                }
                ParameterlessCode.TYPEOF -> {
                    stack.add(LString(popStack().type))
                }
                ParameterlessCode.PUSH_TRUE -> {
                    stack.add(LBoolean.True)
                }
                ParameterlessCode.PUSH_FALSE -> {
                    stack.add(LBoolean.False)
                }
                ParameterlessCode.POSITIVE -> handlePositiveOperation()
                ParameterlessCode.NEGATIVE -> handleNegativeOperation()
                ParameterlessCode.TRUTH -> {
                    stack.add(LBoolean.of(popStack().truth()))
                }
                ParameterlessCode.NOT -> {
                    stack.add(LBoolean.of(!popStack().truth()))
                }
                ParameterlessCode.ADD -> handleAddOperation()
                ParameterlessCode.SUBTRACT -> handleSubtractOperation()
                ParameterlessCode.MULTIPLY -> handleMultiplyOperation()
                ParameterlessCode.DIVIDE -> handleDivideOperation()
                ParameterlessCode.REMAINING -> handleRemainingOperation()
                ParameterlessCode.EQUALS -> {
                    val right = popStack()
                    val left = popStack()
                    stack.add(LBoolean.of(right == left))
                }
                ParameterlessCode.NOT_EQUALS -> {
                    val right = popStack()
                    val left = popStack()
                    stack.add(LBoolean.of(right != left))
                }
                ParameterlessCode.LT -> handleComparison(Comparison.LT)
                ParameterlessCode.LTE -> handleComparison(Comparison.LTE)
                ParameterlessCode.GT -> handleComparison(Comparison.GT)
                ParameterlessCode.GTE -> handleComparison(Comparison.GTE)
                ParameterlessCode.IN -> handleInOperation()
                ParameterlessCode.RANGE -> handleRangeOperation()
            }
            Opcode.ASSIGN -> {
                val s = code.sConstArr.getOrElse(insn.immediate) {
                    throw MalformedBytecodeException(
                        "Tried to load string constant ${insn.immediate} which wasn't defined.",
                        control.stackTrace()
                    )
                }
                scope.set(s, popStack())
            }
            Opcode.BRANCH_IF_FALSE -> handleBranchIf(false, insn.immediate)
            Opcode.BRANCH_IF_TRUE -> handleBranchIf(true, insn.immediate)
            Opcode.DECLARE_VARIABLE_IMMUTABLE -> {
                val s = code.sConstArr.getOrElse(insn.immediate) {
                    throw MalformedBytecodeException(
                        "Tried to load string constant ${insn.immediate} which wasn't defined.",
                        control.stackTrace()
                    )
                }
                scope.define(s, false)
            }
            Opcode.DECLARE_VARIABLE_MUTABLE -> {
                val s = code.sConstArr.getOrElse(insn.immediate) {
                    throw MalformedBytecodeException(
                        "Tried to load string constant ${insn.immediate} which wasn't defined.",
                        control.stackTrace()
                    )
                }
                scope.define(s, true)
            }
            Opcode.GET_MEMBER_PROPERTY -> handleGetMemberProperty(insn.immediate)
            Opcode.GET_SUBSCRIPT -> handleGetSubscript(insn.immediate)
            Opcode.GET_VARIABLE -> {
                val s = code.sConstArr.getOrElse(insn.immediate) {
                    throw MalformedBytecodeException(
                        "Tried to load string constant ${insn.immediate} which wasn't defined.",
                        control.stackTrace()
                    )
                }
                stack.add(scope.get(s))
            }
            Opcode.INVOKE -> handleInvoke(insn.immediate)
            Opcode.INVOKE_LOCAL -> handleInvokeLocal(insn.immediate)
            Opcode.INVOKE_MEMBER -> handleInvokeMember(insn.immediate)
            Opcode.INVOKE_EXTENSION -> handleInvokeExtension(insn.immediate)
            Opcode.JUMP -> {
                next = node.findJump(insn.immediate)?.at ?: throw MalformedBytecodeException(
                    "Tried to jump to label ${insn.immediate} which wasn't defined.",
                    control.stackTrace()
                )
            }
            Opcode.LOAD_DECIMAL -> {
                val l = code.lConstArr.getOrElse(insn.immediate) {
                    throw MalformedBytecodeException(
                        "Tried to load number constant ${insn.immediate} which wasn't defined.",
                        control.stackTrace()
                    )
                }
                stack.add(LDecimal(Double.fromBits(l)))
            }
            Opcode.LOAD_INTEGER -> {
                val i = code.lConstArr.getOrElse(insn.immediate) {
                    throw MalformedBytecodeException(
                        "Tried to load number constant ${insn.immediate} which wasn't defined.",
                        control.stackTrace()
                    )
                }
                stack.add(LInteger(i))
            }
            Opcode.LOAD_STRING -> {
                val s = code.sConstArr.getOrElse(insn.immediate) {
                    throw MalformedBytecodeException(
                        "Tried to load string constant ${insn.immediate} which wasn't defined.",
                        control.stackTrace()
                    )
                }
                stack.add(LString(s))
            }
            Opcode.NEW_FUNCTION -> {
                val f = code.funcArr.getOrElse(insn.immediate) {
                    throw MalformedBytecodeException(
                        "Tried to load function ${insn.immediate} which wasn't defined.",
                        control.stackTrace()
                    )
                }
                stack.add(LCompiledFunction(code, f, runtime, scope))
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
            Opcode.PUSH_EXCEPTION_HANDLING -> {
                exceptionHandlers.add(
                    ExceptionHandler(
                        stack.size,
                        node.findJump(insn.immediate)?.at ?: throw MalformedBytecodeException(
                            "Tried to compute value of exception handling's catch label ${insn.immediate} which wasn't defined.",
                            control.stackTrace()
                        )
                    )
                )
            }
            Opcode.SET_MEMBER_PROPERTY -> handleSetMemberProperty(insn.immediate)
            Opcode.SET_SUBSCRIPT -> handleSetSubscript(insn.immediate)
            Opcode.SET_VARIABLE -> {
                val s = code.sConstArr.getOrElse(insn.immediate) {
                    throw MalformedBytecodeException(
                        "Tried to load string constant ${insn.immediate} which wasn't defined.",
                        control.stackTrace()
                    )
                }
                scope.set(s, popStack())
            }
        }
    }

    override fun onReturn(value: LAny) {
        stack.add(value)
    }

    override fun onThrow(value: LAny) {
        val handler = exceptionHandlers.removeLastOrNull()
        if (handler == null) {
            control.onThrow(value)
            return
        }
        if (handler.keepOnStack < stack.size) {
            println("WTF? Stack is missing ${handler.keepOnStack - stack.size} items!! This is probably a bug!")
        } else if (handler.keepOnStack > stack.size) {
            repeat(handler.keepOnStack - stack.size) { popStack() }
        }
        next = handler.onException
        stack.add(value)
    }

    override fun trace(): StackTrace? {
        val label = node.findSect(next - 1) ?: return null
        val section = code.sectArr.getOrElse(label.index) { return null }
        val s = code.sConstArr.getOrElse(section.nameConst) {
            throw MalformedBytecodeException(
                "Tried to load string constant ${section.nameConst} which wasn't defined.",
                control.stackTrace()
            )
        }
        return StackTrace(functionName, s, section.line, section.column)
    }

    public data class ExceptionHandler(val keepOnStack: Int, val onException: Int)

    private fun popStack(): LAny {
        return stack.removeLastOrNull() ?: throw MalformedBytecodeException(
            "Tried to remove an item from the stack, but the stack is empty.",
            control.stackTrace()
        )
    }

    private fun peekStack(): LAny {
        return stack.lastOrNull() ?: throw MalformedBytecodeException(
            "Tried to get the last item from the stack, but the stack is empty.",
            control.stackTrace()
        )
    }

    // handlers

    private fun handleBranchIf(value: Boolean, labelCode: Int) {
        if (popStack().truth() == value) {
            next = node.findJump(labelCode)?.at ?: throw MalformedBytecodeException(
                "Tried to branch to label $labelCode which wasn't defined.",
                control.stackTrace()
            )
        }
    }

    private fun handleGetMemberProperty(nameConst: Int) {
        val target = popStack()
        val name = code.sConstArr.getOrElse(nameConst) {
            throw MalformedBytecodeException(
                "Tried to load string constant $nameConst which wasn't defined.",
                control.stackTrace()
            )
        }
        if (target is LNull) {
            throw LeanNullPointerException(
                "Tried to access member '$name' of null target.", control.stackTrace()
            )
        }
        stack.add(runtime.getMemberProperty(control, target, name))
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
                    throw LeanIndexOutOfBoundsException(
                        "Tried to access index $index of array with size $listSize.",
                        control.stackTrace()
                    )
                }

                stack.add(list[if (index < 0) listSize + index.toInt() else index.toInt()])

                return
            } else if (arg is LRange) {
                val range = arg.value

                val start = range.first
                val end = range.last
                if (start < -listSize || end < -listSize || start > lastIndex || end > lastIndex) {
                    throw LeanIndexOutOfBoundsException(
                        "Tried to access range $start..$end of array with size $listSize.",
                        control.stackTrace()
                    )
                }

                if (range.isEmpty()) {
                    stack.add(LArray())
                    return
                }

                if (range.step == 1L) {
                    stack.add(LArray(list.subList(start.toInt(), end.toInt()).toMutableList()))
                    return
                }

                val select = mutableListOf<LAny>()
                for (i in range) {
                    select += list[i.toInt()]
                }
                stack.add(LArray(select))
                return
            }
        }
        if (parent is LObject && size == 1) {
            val arg = arguments.first()
            stack.add(parent.value[arg] ?: LNull)
            return
        }
        if (parent is LString && size == 1) {
            val arg = arguments.first()
            val string = parent.value
            val length = string.length
            val lastIndex = string.lastIndex
            if (arg is LInteger) {
                val index = arg.value
                if (index < -length || index > lastIndex) {
                    throw LeanIndexOutOfBoundsException(
                        "Tried to access index $index of string with size $length.",
                        control.stackTrace()
                    )
                }
                stack.add(LString(string[if (index < 0) length + index.toInt() else index.toInt()].toString()))
                return
            } else if (arg is LRange) {
                val range = arg.value

                val start = range.first
                val end = range.last
                if (start < -length || end < -length || start > lastIndex || end > lastIndex) {
                    throw LeanIndexOutOfBoundsException(
                        "Tried to access range $start..$end of string with size $length.",
                        control.stackTrace()
                    )
                }

                if (range.isEmpty()) {
                    stack.add(LString())
                    return
                }

                if (range.step == 1L) {
                    stack.add(LString(string.substring(start.toInt(), end.toInt())))
                    return
                }

                val select = buildString {
                    for (i in range) {
                        append(string[i.toInt()])
                    }
                }
                stack.add(LString(select))
                return
            }
        }

        stack.add(runtime.customGetSubscript(control, parent, arguments))
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
        val s = code.sConstArr.getOrElse(nameConst) {
            throw MalformedBytecodeException(
                "Tried to load string constant $nameConst which wasn't defined.",
                control.stackTrace()
            )
        }
        invocation(null, scope.get(s), arguments)
    }

    private fun handleInvokeMember(immediate: Int) {
        val nameConst: Int = immediate shr 16
        val size: Int = immediate and 0xff

        val arguments = List(size) { popStack() }.reversed()
        val parent = popStack()
        val s = code.sConstArr.getOrElse(nameConst) {
            throw MalformedBytecodeException(
                "Tried to load string constant $nameConst which wasn't defined.",
                control.stackTrace()
            )
        }
        invocation(parent, runtime.getMemberProperty(control, parent, s), arguments)
    }

    private fun handleInvokeExtension(size: Int) {
        val arguments = List(size) { popStack() }.reversed()
        val function = popStack()
        val target = popStack()
        invocation(target, function, arguments)
    }

    private fun handleSetMemberProperty(nameConst: Int) {
        val value = popStack()
        val name = code.sConstArr.getOrElse(nameConst) {
            throw MalformedBytecodeException(
                "Tried to load string constant $nameConst which wasn't defined.",
                control.stackTrace()
            )
        }
        val parent = popStack()
        if (parent is LObject) {
            parent.value[LString(name)] = value
        }

        runtime.setMemberProperty(control, parent, name, value)
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
        runtime.customSetSubscript(control, parent, arguments, value)
    }

    private fun handleAddOperation() {
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
        stack.add(runtime.customAddOperation(control, left, right))
    }

    private fun handleDivideOperation() {
        val right = popStack()
        val left = popStack()
        if (left is LNumber && right is LNumber) {
            stack.add(left / right)
            return
        }
        stack.add(runtime.customDivideOperation(control, left, right))
    }

    private fun handleMultiplyOperation() {
        val right = popStack()
        val left = popStack()
        if (left is LString && right is LInteger) {
            stack.add(LString(left.value.repeat(right.value.toInt())))
        }
        if (left is LNumber && right is LNumber) {
            stack.add(left * right)
            return
        }
        stack.add(runtime.customMultiplyOperation(control, left, right))
    }

    private fun handleRangeOperation() {
        val right = popStack()
        val left = popStack()
        if (left is LInteger && right is LInteger) {
            stack.add(left..right)
            return
        }
        stack.add(runtime.customRangeOperation(control, left, right))
    }

    private fun handleRemainingOperation() {
        val right = popStack()
        val left = popStack()
        if (left is LNumber && right is LNumber) {
            stack.add(left % right)
            return
        }
        stack.add(runtime.customRemainingOperation(control, left, right))
    }

    private fun handleSubtractOperation() {
        val right = popStack()
        val left = popStack()
        if (left is LNumber && right is LNumber) {
            stack.add(left - right)
            return
        }
        stack.add(runtime.customSubtractOperation(control, left, right))
    }

    private fun handleComparison(comparison: Comparison) {
        val right = popStack()
        val left = popStack()
        if (left is LString && right is LString) {
            stack.add(LBoolean.of(comparison.block(left.value.compareTo(right.value))))
            return
        }
        if (left is LNumber && right is LNumber) {
            stack.add(LBoolean.of(comparison.block(left.compareTo(right))))
            return
        }
        stack.add(runtime.customComparison(control, comparison, left, right))
    }

    private fun handleInOperation() {
        val right = popStack()
        val left = popStack()
        if (right is LArray) {
            stack.add(LBoolean.of(left in right.value))
            return
        }
        if (right is LObject) {
            stack.add(LBoolean.of(left in right.value))
            return
        }
        stack.add(LBoolean.of(runtime.customInOperation(control, left, right)))
    }

    private fun handleNegativeOperation() {
        val target = popStack()
        if (target is LNumber) {
            stack.add(-target)
            return
        }
        stack.add(runtime.customNegativeOperation(control, target))
    }

    private fun handlePositiveOperation() {
        val target = popStack()
        if (target is LNumber) {
            stack.add(+target)
            return
        }
        stack.add(runtime.customPositiveOperation(control, target))
    }

    private fun invocation(thisValue: LAny?, function: LAny, args: List<LAny>) {
        if (function is LFunction) {
            control.push(function.setupContext(control, thisValue, args, runtime))
            return
        }
        runtime.customInvocation(control, thisValue, function, args)
    }
}
