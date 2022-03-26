package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.*
import okio.Buffer
import okio.ByteString

public actual class LeanNode(
    public actual val insnCount: Int,
    private val insnData: ByteString,
    public actual val jumpCount: Int,
    private val jumpData: ByteString,
    public actual val sectCount: Int,
    private val sectData: ByteString,
) : Serializable {

    private val insnArr: Array<LeanInsn?> = arrayOfNulls(insnCount)
    private val jumpArr: Array<LeanJumpLabel?> = arrayOfNulls(jumpCount)
    private val sectArr: Array<LeanSectLabel?> = arrayOfNulls(sectCount)

    public actual fun insnOrNull(index: Int): LeanInsn? {
        if (index !in 0 until insnCount) return null
        val fromArray = insnArr[index]
        if (fromArray != null) return fromArray
        val insn = LeanInsn.fromBytes(
            insnData.substring(index * LeanInsn.SIZE_BYTES, (index + 1) * LeanInsn.SIZE_BYTES)
        )
        insnArr[index] = insn
        return insn
    }

    public actual fun insn(index: Int): LeanInsn {
        return insnOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access insn $index on array with length ${insnArr.size}")
    }

    public actual fun sectOrNull(index: Int): LeanSectLabel? {
        if (index !in 0 until sectCount) return null
        val fromArray = sectArr[index]
        if (fromArray != null) return fromArray
        val sect = LeanSectLabel.fromBytes(
            sectData.substring(index * LeanSectLabel.SIZE_BYTES, (index + 1) * LeanSectLabel.SIZE_BYTES)
        )
        sectArr[index] = sect
        return sect
    }

    public actual fun sect(index: Int): LeanSectLabel {
        return sectOrNull(index)
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

    public actual fun jumpOrNull(index: Int): LeanJumpLabel? {
        if (index !in 0 until jumpCount) return null
        val fromArray = jumpArr[index]
        if (fromArray != null) return fromArray
        val jump = LeanJumpLabel.fromBytes(
            jumpData.substring(index * LeanJumpLabel.SIZE_BYTES, (index + 1) * LeanJumpLabel.SIZE_BYTES)
        )
        jumpArr[index] = jump
        return jump
    }

    public actual fun jump(index: Int): LeanJumpLabel {
        return jumpOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access jump $index on array with length ${jumpArr.size}")
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

    override fun serializeTo(buffer: Buffer) {
        check(jumpCount.isU24) { "LeanNode.jumpArr exceeds max size of U24 (0xFFFFFF)." }

        buffer.writeInt(insnCount)
            .write(insnData)
            .writeU24(jumpCount)
            .write(jumpData)
            .writeInt(sectCount)
            .write(sectData)
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
                insnArr.size,
                Buffer().apply { insnArr.forEach { it.serializeTo(this) } }.snapshot(),
                jumpArr.size,
                Buffer().apply { jumpArr.forEach { it.serializeTo(this) } }.snapshot(),
                sectArr.size,
                Buffer().apply { sectArr.forEach { it.serializeTo(this) } }.snapshot(),
            )
        }

        override fun deserializeFrom(buffer: Buffer): LeanNode {
            val insnCount = buffer.readInt()
            val insnData = buffer.readByteString(insnCount.toLong() * LeanInsn.SIZE_BYTES)

            val jumpCount = buffer.readU24()
            val jumpData = buffer.readByteString(jumpCount.toLong() * LeanJumpLabel.SIZE_BYTES)

            val sectCount = buffer.readInt()
            val sectData = buffer.readByteString(sectCount.toLong() * LeanSectLabel.SIZE_BYTES)

            return LeanNode(insnCount, insnData, jumpCount, jumpData, sectCount, sectData)
        }

        internal fun determineByteSize(source: Buffer): Long {
            val buffer = source.copy()

            val insnCount = buffer.readInt()
            buffer.skip(insnCount.toLong() * LeanInsn.SIZE_BYTES)

            val jumpCount = buffer.readU24()
            buffer.skip(jumpCount.toLong() * LeanJumpLabel.SIZE_BYTES)

            val sectCount = buffer.readInt()
            buffer.skip(sectCount.toLong() * LeanSectLabel.SIZE_BYTES)

            return Int.SIZE_BYTES * 2 + 3 +
                insnCount.toLong() * LeanInsn.SIZE_BYTES +
                jumpCount * LeanJumpLabel.SIZE_BYTES +
                sectCount * LeanSectLabel.SIZE_BYTES
        }
    }
}
