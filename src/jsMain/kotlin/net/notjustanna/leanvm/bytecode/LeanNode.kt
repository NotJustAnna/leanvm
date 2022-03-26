package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.*
import okio.Buffer

public actual class LeanNode(
    private val insnArr: Array<LeanInsn>,
    private val jumpArr: Array<LeanJumpLabel>,
    private val sectArr: Array<LeanSectLabel>,
) : Serializable {

    public actual val insnCount: Int get() = insnArr.size

    public actual fun insnOrNull(index: Int): LeanInsn? {
        return insnArr.getOrNull(index)
    }

    public actual fun insn(index: Int): LeanInsn {
        return insnArr.getOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access insn $index on array with length ${insnArr.size}")
    }

    public actual val sectCount: Int get() = sectArr.size

    public actual fun sectOrNull(index: Int): LeanSectLabel? {
        return sectArr.getOrNull(index)
    }

    public actual fun sect(index: Int): LeanSectLabel {
        return sectArr.getOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access sect $index on array with length ${sectArr.size}")
    }

    public actual fun findSect(last: Int): LeanSectLabel? {
        var low = 0
        var high = sectCount - 1
        while (low <= high) {
            val mid = (low + high) / 2
            val midVal = sectOrNull(mid) ?: return null
            when {
                midVal.start + midVal.length < last -> low = mid + 1
                midVal.start > last -> high = mid - 1
                else -> return midVal
            }
        }
        return null
    }

    public actual fun findJump(code: Int): LeanJumpLabel? {
        var low = 0
        var high = jumpArr.size - 1
        while (low <= high) {
            val mid = (low + high) / 2
            val midVal = jumpOrNull(mid) ?: return null
            when {
                midVal.code < code -> low = mid + 1
                midVal.code > code -> high = mid - 1
                else -> return midVal
            }
        }
        return null
    }

    public actual val jumpCount: Int get() = jumpArr.size

    public actual fun jumpOrNull(index: Int): LeanJumpLabel? {
        return jumpArr.getOrNull(index)
    }

    public actual fun jump(index: Int): LeanJumpLabel {
        return jumpArr.getOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access jump $index on array with length ${jumpArr.size}")
    }

    override fun serializeTo(buffer: Buffer) {
        check(jumpArr.size.isU24) { "LeanNode.jumpArr exceeds max size of U24 (0xFFFFFF)." }

        buffer.writeInt(insnArr.size)
        for (insn in insnArr) insn.serializeTo(buffer)

        buffer.writeU24(jumpArr.size)
        for (label in jumpArr) label.serializeTo(buffer)

        buffer.writeInt(sectArr.size)
        for (label in sectArr) label.serializeTo(buffer)
    }

    override fun toString(): String {
        return "LeanNode[$insnCount insns, $jumpCount jumps, $sectCount sects]"
    }

    public actual companion object : Deserializer<LeanNode> {
        public actual fun create(
            insnArr: List<LeanInsn>,
            jumpArr: List<LeanJumpLabel>,
            sectArr: List<LeanSectLabel>,
        ): LeanNode {
            return LeanNode(
                insnArr.toTypedArray(),
                jumpArr.toTypedArray(),
                sectArr.toTypedArray()
            )
        }

        override fun deserializeFrom(buffer: Buffer): LeanNode {
            return LeanNode(
                Array(buffer.readInt()) {
                    LeanInsn.deserializeFrom(buffer)
                },
                Array(buffer.readU24()) {
                    LeanJumpLabel.deserializeFrom(buffer)
                },
                Array(buffer.readInt()) {
                    LeanSectLabel.deserializeFrom(buffer)
                }
            )
        }
    }
}
