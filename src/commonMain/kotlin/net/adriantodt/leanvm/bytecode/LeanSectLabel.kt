package net.adriantodt.leanvm.bytecode

import net.adriantodt.leanvm.utils.Deserializer
import net.adriantodt.leanvm.utils.Serializable
import okio.Buffer

public data class LeanSectLabel(
    val start: Int,
    val length: Int,
    val index: Int,
) : Serializable, Comparable<LeanSectLabel> {
    override fun serializeTo(buffer: Buffer) {
        buffer.writeInt(start).writeInt(length).writeInt(index)
    }

    override fun compareTo(other: LeanSectLabel): Int {
        val i = start.compareTo(other.start)
        if (i != 0) return i
        return length.compareTo(other.length)
    }

    public companion object : Deserializer<LeanSectLabel> {
        override fun deserializeFrom(buffer: Buffer): LeanSectLabel {
            return LeanSectLabel(buffer.readInt(), buffer.readInt(), buffer.readInt())
        }
    }
}
