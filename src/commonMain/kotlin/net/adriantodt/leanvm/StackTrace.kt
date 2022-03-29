package net.adriantodt.leanvm

public data class StackTrace(
    val functionName: String,
    val sourceName: String? = null,
    val line: Int = -1,
    val column: Int = -1
) {
    override fun toString(): String {
        if (line == -1 && column == -1) {
            if (sourceName == null) {
                return "$functionName[Platform]"
            }
            return "$functionName($sourceName)"
        }
        return "$functionName($sourceName:$line:$column)"
    }
}
