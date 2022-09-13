package net.adriantodt.leanvm.context

import net.adriantodt.leanvm.Scope
import net.adriantodt.leanvm.StackTrace
import net.adriantodt.leanvm.bytecode.LeanCode
import net.adriantodt.leanvm.bytecode.LeanNode
import net.adriantodt.leanvm.exceptions.LeanIndexOutOfBoundsException
import net.adriantodt.leanvm.exceptions.LeanNullPointerException
import net.adriantodt.leanvm.exceptions.LeanRuntimeException
import net.adriantodt.leanvm.exceptions.MalformedBytecodeException
import net.adriantodt.leanvm.types.*
import net.adriantodt.leanvm.utils.Comparison
import net.adriantodt.leanvm.utils.InstructionDecoder
import kotlin.math.sign

public class NodeExecutionContext(
    protected override val control: LeanMachineControl,
    private val code: LeanCode,
    private val functionName: String = "<main>",
    private val thisValue: LAny? = null,
    public override val runtime: LeanRuntime = LeanRuntime(),
    private var scope: Scope = runtime.rootScope(),
    private val node: LeanNode = code.nodeArr.getOrElse(0) {
        throw MalformedBytecodeException("Code does not contain an executable node.", control.stackTrace())
    },
) : InstructionDecoder(), LeanContext {
    private var next: Int = 0
    private val stack: MutableList<LAny> = mutableListOf()
    private val exceptionHandlers: MutableList<ExceptionHandler> = mutableListOf()
    private var customReturn: ((value: LAny) -> Unit)? = null

    public override fun step() {
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
        handle(insn)
    }

    public override fun onReturn(value: LAny) {
        val handler = this.customReturn
        if (handler != null) {
            customReturn = null
            handler(value)
        } else {
            stack.add(value)
        }
    }

    public override fun onThrow(value: LAny) {
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

    public override fun trace(): StackTrace? {
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

    private fun setupInvocation(thisValue: LAny?, function: LAny, args: List<LAny> = emptyList()) {
        if (function is LFunction) {
            control.push(function.setupContext(control, thisValue, args, runtime))
            return
        }
        if (function is LMetaObject) {
            val metaInvoke = function.access(LString("invoke"))
            if (metaInvoke is LFunction) {
                control.push(metaInvoke.setupContext(control, function, args, runtime))
                return
            }
        }
        runtime.customInvocation(control, thisValue, function, args)
    }

    protected override fun handleArrayInsert() {
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

    protected override fun handleDup() {
        stack.add(peekStack())
    }

    protected override fun handleNewArray() {
        stack.add(LArray())
    }

    protected override fun handleNewObject() {
        stack.add(LObject())
    }

    protected override fun handleObjectInsert() {
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

    protected override fun handlePop() {
        popStack()
    }

    protected override fun handlePopScope() {
        scope = scope.parent ?: throw MalformedBytecodeException(
            "Tried to pop scope but encountered a root scope.",
            control.stackTrace()
        )
    }

    protected override fun handlePopExceptionHandling() {
        exceptionHandlers.removeLastOrNull() ?: throw MalformedBytecodeException(
            "Tried execute POP_EXCEPTION_HANDLING instruction but found no exception handler.",
            control.stackTrace()
        )
    }

    protected override fun handlePushNull() {
        stack.add(LNull)
    }

    protected override fun handlePushScope() {
        scope = Scope(scope)
    }

    protected override fun handlePushThis() {
        stack.add(thisValue ?: runtime.customThisValue(control))
    }

    protected override fun handleReturn() {
        control.onReturn(popStack())
    }

    protected override fun handleThrow() {
        onThrow(popStack())
    }

    protected override fun handleTypeof() {
        stack.add(LString(popStack().type))
    }

    protected override fun handlePushBoolean(b: Boolean) {
        stack.add(LBoolean.of(b))
    }

    protected override fun handlePositive() {
        val target = popStack()
        if (target is LNumber) {
            stack.add(+target)
            return
        }
        if (target is LMetaObject) {
            val property = target.access(LString("unaryPlus"))
            if (property is LFunction) {
                control.push(property.setupContext(control, target, emptyList(), runtime))
                return
            }
            if (property != null) {
                stack.add(property)
                return
            }
        }
        runtime.customPositiveOperation(control, target)
    }

    protected override fun handleNegative() {
        val target = popStack()
        if (target is LNumber) {
            stack.add(-target)
            return
        }
        if (target is LMetaObject) {
            val property = target.access(LString("unaryMinus"))
            if (property is LFunction) {
                control.push(property.setupContext(control, target, emptyList(), runtime))
                return
            }
            if (property != null) {
                stack.add(property)
                return
            }
        }
        runtime.customNegativeOperation(control, target)
    }

    protected override fun handleTruth() {
        val target = popStack()
        if (target is LMetaObject) {
            val property = target.access(LString("truth"))
            if (property is LFunction) {
                customReturn = {
                    if (it is LMetaObject) {
                        throw LeanRuntimeException(
                            "Meta object '$target' returned a meta object as 'truth'.",
                            control.stackTrace()
                        )
                    }
                    stack.add(LBoolean.of(it.truth()))
                }
                control.push(property.setupContext(control, target, emptyList(), runtime))
                return
            } else if (property != null) {
                if (property is LMetaObject) {
                    throw LeanRuntimeException(
                        "Meta object '$target' has a meta object as 'truth'.",
                        control.stackTrace()
                    )
                }
                stack.add(LBoolean.of(property.truth()))
                return
            }
        }
    }

    protected override fun handleNot() {
        val target = popStack()
        if (target is LMetaObject) {
            val property = target.access(LString("truth"))
            if (property is LFunction) {
                customReturn = {
                    if (it is LMetaObject) {
                        throw LeanRuntimeException(
                            "Meta object '$target' returned a meta object as 'truth'.",
                            control.stackTrace()
                        )
                    }
                    stack.add(LBoolean.of(!it.truth()))
                }
                control.push(property.setupContext(control, target, emptyList(), runtime))
                return
            } else if (property != null) {
                if (property is LMetaObject) {
                    throw LeanRuntimeException(
                        "Meta object '$target' has a meta object as 'truth'.",
                        control.stackTrace()
                    )
                }
                stack.add(LBoolean.of(!property.truth()))
                return
            }
        }
    }

    protected override fun handleAdd() {
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
        if (left is LMetaObject) {
            val property = left.access(LString("add"))
            if (property is LFunction) {
                control.push(property.setupContext(control, left, listOf(right), runtime))
                return
            }
            if (property != null) {
                stack.add(property)
                return
            }
        }
        stack.add(runtime.customAddOperation(control, left, right))
    }

    protected override fun handleSubtract() {
        val right = popStack()
        val left = popStack()
        if (left is LNumber && right is LNumber) {
            stack.add(left - right)
            return
        }
        if (left is LMetaObject) {
            val property = left.access(LString("subtract"))
            if (property is LFunction) {
                control.push(property.setupContext(control, left, listOf(right), runtime))
                return
            }
            if (property != null) {
                stack.add(property)
                return
            }
        }
        stack.add(runtime.customSubtractOperation(control, left, right))
    }

    protected override fun handleMultiply() {
        val right = popStack()
        val left = popStack()
        if (left is LString && right is LInteger) {
            stack.add(LString(left.value.repeat(right.value.toInt())))
        }
        if (left is LNumber && right is LNumber) {
            stack.add(left * right)
            return
        }
        if (left is LMetaObject) {
            val property = left.access(LString("multiply"))
            if (property is LFunction) {
                control.push(property.setupContext(control, left, listOf(right), runtime))
                return
            }
            if (property != null) {
                stack.add(property)
                return
            }
        }
        stack.add(runtime.customMultiplyOperation(control, left, right))
    }

    protected override fun handleDivide() {
        val right = popStack()
        val left = popStack()
        if (left is LNumber && right is LNumber) {
            stack.add(left / right)
            return
        }
        if (left is LMetaObject) {
            val property = left.access(LString("divide"))
            if (property is LFunction) {
                control.push(property.setupContext(control, left, listOf(right), runtime))
                return
            }
            if (property != null) {
                stack.add(property)
                return
            }
        }
        stack.add(runtime.customDivideOperation(control, left, right))
    }

    protected override fun handleRemaining() {
        val right = popStack()
        val left = popStack()
        if (left is LNumber && right is LNumber) {
            stack.add(left % right)
            return
        }
        if (left is LMetaObject) {
            val property = left.access(LString("remaining"))
            if (property is LFunction) {
                control.push(property.setupContext(control, left, listOf(right), runtime))
                return
            }
            if (property != null) {
                stack.add(property)
                return
            }
        }
        stack.add(runtime.customRemainingOperation(control, left, right))
    }

    protected override fun handleEquals() {
        val right = popStack()
        val left = popStack()
        if (left is LMetaObject) {
            val metaEquals = left.value.getOrElse(LString("equals")) {
                stack.add(LBoolean.of(left.value == right))
                return
            }
            if (metaEquals is LFunction) {
                this.customReturn = {
                    if (it is LMetaObject) {
                        throw LeanRuntimeException(
                            "Meta object '$left' returned a meta object as 'equals'.",
                            control.stackTrace()
                        )
                    }
                    stack.add(LBoolean.of(it.truth()))
                }
                control.push(metaEquals.setupContext(control, left, listOf(right), runtime))
                return
            }
            if (metaEquals is LMetaObject) {
                throw LeanRuntimeException("Meta object '$left' has a 'equals' meta object.", control.stackTrace())
            }
            stack.add(LBoolean.of(metaEquals == right))
            return
        }
        if (right is LMetaObject) {
            val metaEquals = right.value.getOrElse(LString("equals")) {
                stack.add(LBoolean.of(left == right.value))
                return
            }
            if (metaEquals is LFunction) {
                this.customReturn = {
                    if (it is LMetaObject) {
                        throw LeanRuntimeException(
                            "Meta object '$right' returned a meta object as 'equals'.",
                            control.stackTrace()
                        )
                    }
                    stack.add(LBoolean.of(it.truth()))
                }
                control.push(metaEquals.setupContext(control, right, listOf(left), runtime))
                return
            }
            if (metaEquals is LMetaObject) {
                throw LeanRuntimeException("Meta object '$right' has a 'equals' meta object.", control.stackTrace())
            }
            stack.add(LBoolean.of(metaEquals == left))
            return
        }
        stack.add(LBoolean.of(right == left))
    }

    protected override fun handleNotEquals() {
        val right = popStack()
        val left = popStack()
        if (left is LMetaObject) {
            val metaEquals = left.value.getOrElse(LString("equals")) {
                stack.add(LBoolean.of(left.value != right))
                return
            }
            if (metaEquals is LFunction) {
                this.customReturn = {
                    if (it is LMetaObject) {
                        throw LeanRuntimeException(
                            "Meta object '$left' returned a meta object as 'equals'.",
                            control.stackTrace()
                        )
                    }
                    stack.add(LBoolean.of(!it.truth()))
                }
                control.push(metaEquals.setupContext(control, left, listOf(right), runtime))
                return
            }
            if (metaEquals is LMetaObject) {
                throw LeanRuntimeException("Meta object '$left' has a 'equals' meta object.", control.stackTrace())
            }
            stack.add(LBoolean.of(metaEquals != right))
            return
        }
        if (right is LMetaObject) {
            val metaEquals = right.value.getOrElse(LString("equals")) {
                stack.add(LBoolean.of(left != right.value))
                return
            }
            if (metaEquals is LFunction) {
                this.customReturn = {
                    if (it is LMetaObject) {
                        throw LeanRuntimeException(
                            "Meta object '$right' returned a meta object as 'equals'.",
                            control.stackTrace()
                        )
                    }
                    stack.add(LBoolean.of(!it.truth()))
                }
                control.push(metaEquals.setupContext(control, right, listOf(left), runtime))
                return
            }
            if (metaEquals is LMetaObject) {
                throw LeanRuntimeException("Meta object '$right' has a 'equals' meta object.", control.stackTrace())
            }
            stack.add(LBoolean.of(metaEquals != left))
            return
        }
        stack.add(LBoolean.of(right != left))
    }

    protected override fun handleComparison(comparison: Comparison) {
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
        if (left is LMetaObject) {
            val operatorProperty = left.access(LString(comparison.name.lowercase()))
            if (operatorProperty is LFunction) {
                control.push(operatorProperty.setupContext(control, left, listOf(right), runtime))
                return
            } else if (operatorProperty != null) {
                stack.add(operatorProperty)
                return
            }

            val comparingProperty = left.access(LString("compareTo"))
            if (comparingProperty is LFunction) {
                customReturn = {
                    if (it is LMetaObject) {
                        throw LeanRuntimeException(
                            "Meta object '$left' returned a meta object as 'compareTo'.",
                            control.stackTrace()
                        )
                    }
                    if (it !is LNumber) {
                        throw LeanRuntimeException(
                            "Meta object '$left' returned a non-number as 'compareTo'.",
                            control.stackTrace()
                        )
                    }
                    stack.add(LBoolean.of(comparison.block(it.decimalValue.sign.toInt())))
                }
                control.push(comparingProperty.setupContext(control, left, listOf(right), runtime))
                return
            }
        }
        stack.add(runtime.customComparison(control, comparison, left, right))
    }

    protected override fun handleIn() {
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
        if (right is LMetaObject) {
            val property = right.access(LString("contains"))
            if (property is LFunction) {
                control.push(property.setupContext(control, right, listOf(left), runtime))
                return
            }
        }
        stack.add(LBoolean.of(runtime.customInOperation(control, left, right)))
    }

    protected override fun handleRange() {
        val right = popStack()
        val left = popStack()
        if (left is LInteger && right is LInteger) {
            stack.add(left..right)
            return
        }
        if (left is LMetaObject) {
            val property = left.access(LString("rangeTo"))
            if (property is LFunction) {
                control.push(property.setupContext(control, left, listOf(right), runtime))
                return
            }
        }
        stack.add(runtime.customRangeOperation(control, left, right))
    }

    protected override fun handleAssign(immediate: Int) {
        val s = code.sConstArr.getOrElse(immediate) {
            throw MalformedBytecodeException(
                "Tried to load string constant $immediate which wasn't defined.",
                control.stackTrace()
            )
        }
        scope.set(s, popStack())
    }

    protected override fun handleBranchIf(b: Boolean, labelCode: Int) {
        val target = popStack()
        val i = node.findJump(labelCode)?.at ?: throw MalformedBytecodeException(
            "Tried to branch to label $labelCode which wasn't defined.",
            control.stackTrace()
        )
        if (target is LMetaObject) {
            val property = target.access(LString("truth"))
            if (property is LFunction) {
                customReturn = {
                    if (it is LMetaObject) {
                        throw LeanRuntimeException(
                            "Meta object '$target' returned a meta object as 'truth'.",
                            control.stackTrace()
                        )
                    }
                    if (it.truth() == b) {
                        next = i
                    }
                }
                control.push(property.setupContext(control, target, emptyList(), runtime))
                return
            } else if (property != null) {
                if (property is LMetaObject) {
                    throw LeanRuntimeException(
                        "Meta object '$target' has a meta object as 'truth'.",
                        control.stackTrace()
                    )
                }
                if (property.truth() == b) {
                    next = i
                }
                return
            }
        }
        if (target.truth() == b) {
            next = i
        }
    }

    protected override fun handleDeclareVariable(mutable: Boolean, immediate: Int) {
        val s = code.sConstArr.getOrElse(immediate) {
            throw MalformedBytecodeException(
                "Tried to load string constant $immediate which wasn't defined.",
                control.stackTrace()
            )
        }
        scope.define(s, mutable)
    }

    protected override fun handleGetMemberProperty(nameConst: Int) {
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
        if (target is LMetaObject) {
            val property = target.access(LString("getMember"))
            if (property is LFunction) {
                control.push(property.setupContext(control, target, listOf(LString(name)), runtime))
                return
            }
        }
        stack.add(runtime.getMemberProperty(control, target, name))
    }

    protected override fun handleGetSubscript(size: Int) {
        val arguments = List(size) { popStack() }.reversed()
        val parent = popStack()
        if (parent is LNull) {
            throw LeanNullPointerException(
                "Tried to access subscript of null target.", control.stackTrace()
            )
        }
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
        if (parent is LMetaObject) {
            val property = parent.access(LString("getSubscript"))
            if (property is LFunction) {
                control.push(property.setupContext(control, parent, arguments, runtime))
                return
            }
        }
        stack.add(runtime.customGetSubscript(control, parent, arguments))
    }

    protected override fun handleGetVariable(immediate: Int) {
        val s = code.sConstArr.getOrElse(immediate) {
            throw MalformedBytecodeException(
                "Tried to load string constant $immediate which wasn't defined.",
                control.stackTrace()
            )
        }
        stack.add(scope.get(s))
    }

    protected override fun handleInvoke(size: Int) {
        val arguments = List(size) { popStack() }.reversed()
        val function = popStack()
        setupInvocation(null, function, arguments)
    }

    protected override fun handleInvokeLocal(nameConst: Int, size: Int) {
        val arguments = List(size) { popStack() }.reversed()
        val s = code.sConstArr.getOrElse(nameConst) {
            throw MalformedBytecodeException(
                "Tried to load string constant $nameConst which wasn't defined.",
                control.stackTrace()
            )
        }
        setupInvocation(null, scope.get(s), arguments)
    }

    protected override fun handleInvokeMember(nameConst: Int, size: Int) {
        val arguments = List(size) { popStack() }.reversed()
        val parent = popStack()
        val s = code.sConstArr.getOrElse(nameConst) {
            throw MalformedBytecodeException(
                "Tried to load string constant $nameConst which wasn't defined.",
                control.stackTrace()
            )
        }
        setupInvocation(parent, runtime.getMemberProperty(control, parent, s), arguments)
    }

    protected override fun handleInvokeExtension(size: Int) {
        val arguments = List(size) { popStack() }.reversed()
        val function = popStack()
        val target = popStack()
        setupInvocation(target, function, arguments)
    }

    protected override fun handleJump(immediate: Int) {
        next = node.findJump(immediate)?.at ?: throw MalformedBytecodeException(
            "Tried to jump to label $immediate which wasn't defined.",
            control.stackTrace()
        )
    }

    protected override fun handleLoadDecimal(immediate: Int) {
        val l = code.lConstArr.getOrElse(immediate) {
            throw MalformedBytecodeException(
                "Tried to load number constant $immediate which wasn't defined.",
                control.stackTrace()
            )
        }
        stack.add(LDecimal(Double.fromBits(l)))
    }

    protected override fun handleLoadInteger(immediate: Int) {
        val i = code.lConstArr.getOrElse(immediate) {
            throw MalformedBytecodeException(
                "Tried to load number constant $immediate which wasn't defined.",
                control.stackTrace()
            )
        }
        stack.add(LInteger(i))
    }

    protected override fun handleLoadString(immediate: Int) {
        val s = code.sConstArr.getOrElse(immediate) {
            throw MalformedBytecodeException(
                "Tried to load string constant $immediate which wasn't defined.",
                control.stackTrace()
            )
        }
        stack.add(LString(s))
    }

    protected override fun handleNewFunction(immediate: Int) {
        val f = code.funcArr.getOrElse(immediate) {
            throw MalformedBytecodeException(
                "Tried to load function $immediate which wasn't defined.",
                control.stackTrace()
            )
        }
        stack.add(LCompiledFunction(code, f, runtime, scope))
    }

    protected override fun handlePushChar(immediate: Int) {
        val value = immediate.toChar()
        stack.add(LString(if (value != (-1).toChar()) value.toString() else ""))
    }

    protected override fun handlePushDecimal(immediate: Int) {
        stack.add(LDecimal(immediate.toDouble()))
    }

    protected override fun handlePushInteger(immediate: Int) {
        stack.add(LInteger(immediate.toLong()))
    }

    protected override fun handlePushExceptionHandling(immediate: Int) {
        exceptionHandlers.add(
            ExceptionHandler(
                stack.size,
                node.findJump(immediate)?.at ?: throw MalformedBytecodeException(
                    "Tried to compute value of exception handling's catch label $immediate which wasn't defined.",
                    control.stackTrace()
                )
            )
        )
    }

    protected override fun handleSetMemberProperty(nameConst: Int) {
        val value = popStack()
        val name = code.sConstArr.getOrElse(nameConst) {
            throw MalformedBytecodeException(
                "Tried to load string constant $nameConst which wasn't defined.",
                control.stackTrace()
            )
        }
        val parent = popStack()
        if (parent is LNull) {
            throw LeanNullPointerException(
                "Tried to access member '$name' of null target.", control.stackTrace()
            )
        }
        if (parent is LObject) {
            parent.value[LString(name)] = value
        }
        if (parent is LMetaObject) {
            val property = parent.access(LString("setMember"))
            if (property is LFunction) {
                control.push(property.setupContext(control, parent, listOf(LString(name), value), runtime))
                return
            }
        }
        runtime.setMemberProperty(control, parent, name, value)
    }

    protected override fun handleSetSubscript(size: Int) {
        val value = popStack()
        val arguments = List(size) { popStack() }.reversed()
        val parent = popStack()
        if (parent is LNull) {
            throw LeanNullPointerException(
                "Tried to access subscript of null target.", control.stackTrace()
            )
        }
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
        if (parent is LMetaObject) {
            val property = parent.access(LString("setSubscript"))
            if (property is LFunction) {
                control.push(property.setupContext(control, parent, arguments + value, runtime))
                return
            }
        }
        runtime.customSetSubscript(control, parent, arguments, value)
    }

    protected override fun handleSetVariable(immediate: Int) {
        val s = code.sConstArr.getOrElse(immediate) {
            throw MalformedBytecodeException(
                "Tried to load string constant $immediate which wasn't defined.",
                control.stackTrace()
            )
        }
        scope.set(s, popStack())
    }

    public data class ExceptionHandler(val keepOnStack: Int, val onException: Int)
}
