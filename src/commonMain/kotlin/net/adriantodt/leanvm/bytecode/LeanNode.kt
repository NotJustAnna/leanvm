package net.adriantodt.leanvm.bytecode

import net.adriantodt.leanvm.utils.*
import okio.Buffer

public class LeanNode(
    public val insnArr: Array<LeanInsn>,
    public val jumpArr: Array<LeanJumpLabel>,
    public val sectArr: Array<LeanSectLabel>,
) : Serializable {

    public fun findSect(last: Int): LeanSectLabel? {
        var low = 0
        var high = sectArr.size - 1
        while (low <= high) {
            val mid = (low + high) / 2
            val midVal = sectArr.getOrNull(mid) ?: return null
            when {
                midVal.start + midVal.length < last -> low = mid + 1
                midVal.start > last -> high = mid - 1
                else -> return midVal
            }
        }
        return null
    }

    public fun findJump(code: Int): LeanJumpLabel? {
        var low = 0
        var high = jumpArr.size - 1
        while (low <= high) {
            val mid = (low + high) / 2
            val midVal = jumpArr.getOrNull(mid) ?: return null
            when {
                midVal.code < code -> low = mid + 1
                midVal.code > code -> high = mid - 1
                else -> return midVal
            }
        }
        return null
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
        return "LeanNode[${insnArr.size} insns, ${jumpArr.size} jumps, ${sectArr.size} sects]"
    }

    public companion object : Deserializer<LeanNode> {
        public fun create(
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
