package net.adriantodt.leanvm.bytecode.debug

import net.adriantodt.leanvm.bytecode.LeanCode
import net.adriantodt.leanvm.bytecode.LeanFuncDecl
import net.adriantodt.leanvm.bytecode.LeanNode

public class BytecodePrinter {
    private var indent = 0

    public fun printCode(code: LeanCode) {
        indentedPrintln("$code:")
        indent++
        if (code.lCount > 0) {
            indentedPrintln("Long pool:")
            indent++
            repeat(code.lCount) {
                indentedPrintln("[$it] ${code.lConst(it)}")
            }
            indent--
        }
        if (code.sCount > 0) {
            indentedPrintln("String pool:")
            indent++
            repeat(code.sCount) {
                indentedPrintln("[$it] ${code.sConst(it)}")
            }
            indent--
        }
        if (code.nodeCount > 0) {
            indentedPrintln("Nodes:")
            indent++
            repeat(code.nodeCount) {
                printNode(code.node(it))
            }
            indent--
        }
        if (code.sectCount > 0) {
            indentedPrintln("Sections:")
            indent++
            repeat(code.sectCount) {
                indentedPrintln("[$it] ${code.sect(it)}")
            }
            indent--
        }
        if (code.funcCount > 0) {
            indentedPrintln("Functions:")
            indent++
            repeat(code.funcCount) {
                printFunc(code.func(it))
            }
            indent--
        }
    }

    private fun printNode(node: LeanNode) {
        indentedPrintln("$node:")
        indent++
        if (node.insnCount > 0) {
            indentedPrintln("Instructions:")
            indent++
            repeat(node.insnCount) {
                indentedPrintln("[$it] ${node.insn(it)}")
            }
            indent--
        }
        if (node.sectCount > 0) {
            indentedPrintln("Sections:")
            indent++
            repeat(node.sectCount) {
                indentedPrintln("[$it] ${node.sect(it)}")
            }
            indent--
        }
        if (node.jumpCount > 0) {
            indentedPrintln("Jumps:")
            indent++
            repeat(node.jumpCount) {
                indentedPrintln("[$it] ${node.jump(it)}")
            }
            indent--
        }
        indent--
    }

    private fun printFunc(func: LeanFuncDecl) {
        indentedPrintln("$func:")
        indent++
        if (func.paramCount > 0) {
            indentedPrintln("Parameters:")
            indent++
            repeat(func.paramCount) {
                indentedPrintln("[$it] ${func.param(it)}")
            }
            indent--
        }
        indent--
    }

    private fun indentedPrintln(s: String) {
        for (i in 0..indent) {
            print("  ")
        }
        println(s)
    }
}
