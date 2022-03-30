package net.adriantodt.leanvm.bytecode

import net.adriantodt.leanvm.utils.*
import okio.Buffer

public data class LeanInsn(val opcode: Int, val immediate: Int) : Serializable {
    override fun serializeTo(buffer: Buffer) {
        buffer.writeByte(opcode).writeU24(immediate)
    }

    public enum class Opcode {
        PARAMETERLESS, ASSIGN, BRANCH_IF_FALSE, BRANCH_IF_TRUE, DECLARE_VARIABLE_IMMUTABLE,
        DECLARE_VARIABLE_MUTABLE, GET_MEMBER_PROPERTY, GET_SUBSCRIPT, GET_VARIABLE, INVOKE, INVOKE_LOCAL, INVOKE_MEMBER,
        JUMP, LOAD_DECIMAL, LOAD_INTEGER, LOAD_STRING, NEW_FUNCTION, PUSH_CHAR, PUSH_DECIMAL, PUSH_INTEGER,
        PUSH_EXCEPTION_HANDLING, SET_MEMBER_PROPERTY, SET_SUBSCRIPT, SET_VARIABLE
    }

    public enum class ParameterlessCode {
        ARRAY_INSERT, DUP, NEW_ARRAY, NEW_OBJECT, OBJECT_INSERT, POP, POP_SCOPE, POP_EXCEPTION_HANDLING, PUSH_NULL,
        PUSH_SCOPE, PUSH_THIS, RETURN, THROW, TYPEOF, PUSH_TRUE, PUSH_FALSE, POSITIVE, NEGATIVE, TRUTH, NOT, ADD,
        SUBTRACT, MULTIPLY, DIVIDE, REMAINING, EQUALS, NOT_EQUALS, LT, LTE, GT, GTE, IN, RANGE
    }

    override fun toString(): String {
        if (opcode in 0..Opcode.values().size) {
            if (opcode == Opcode.PARAMETERLESS.ordinal && immediate in 0..ParameterlessCode.values().size) {
                return "LeanInsn[${ParameterlessCode.values()[immediate]} (PARAMETERLESS)]"
            }
            return "LeanInsn[${Opcode.values()[opcode]}, 0x${immediate.toUInt().toString(16)}]"
        }
        return "LeanInsn[0x${opcode.toUInt().toString(16)}, 0x${immediate.toUInt().toString(16)}]"
    }

    public companion object : Deserializer<LeanInsn> {
        public fun parameterless(code: ParameterlessCode): LeanInsn {
            return simple(Opcode.PARAMETERLESS, code.ordinal)
        }

        public fun smallBig(opcode: Opcode, small: Int, big: Int): LeanInsn {
            return simple(
                opcode,
                small.requireU16("LeanInsn#small") shl 16 or big.requireU16("LeanInsn#big")
            )
        }

        public fun bigSmall(opcode: Opcode, big: Int, small: Int) : LeanInsn {
            return simple(
                opcode,
                big.requireU16("LeanInsn#big") shl 8 or small.requireU16("LeanInsn#small")
            )
        }

        public fun simple(opcode: Opcode, immediate: Int) : LeanInsn {
            return LeanInsn(opcode.ordinal, immediate.requireU24("LeanInsn#immediate"))
        }

        override fun deserializeFrom(buffer: Buffer): LeanInsn {
            return LeanInsn(buffer.readU8(), buffer.readU24())
        }
    }
}
