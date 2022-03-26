package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.Deserializer
import net.notjustanna.leanvm.utils.Serializable

public expect class LeanFuncDecl: Serializable {

    public val nameConst: Int
    public val bodyId: Int
    public val varargsParam: Int

    public val paramCount: Int
    public fun paramOrNull(index: Int): LeanParamDecl?
    public fun param(index: Int): LeanParamDecl

    public companion object : Deserializer<LeanFuncDecl> {
        public fun create(
            nameConst: Int,
            bodyId: Int,
            varargsParam: Int,
            paramArr: List<LeanParamDecl>
        ): LeanFuncDecl
    }
}
