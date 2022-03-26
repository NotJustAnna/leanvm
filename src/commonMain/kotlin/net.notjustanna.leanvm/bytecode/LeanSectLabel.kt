package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.Deserializer
import net.notjustanna.leanvm.utils.Serializable
import okio.Buffer

public data class LeanSectLabel(val start: Int, val length: Int, val index: Int) : Serializable {
    override fun serializeTo(buffer: Buffer) {
        buffer.writeInt(start).writeInt(length).writeInt(index)
    }

    public companion object : Deserializer<LeanSectLabel> {
        public const val SIZE_BYTES: Int = Int.SIZE_BYTES * 3

        override fun deserializeFrom(buffer: Buffer): LeanSectLabel {
            return LeanSectLabel(buffer.readInt(), buffer.readInt(), buffer.readInt())
        }
    }
}
