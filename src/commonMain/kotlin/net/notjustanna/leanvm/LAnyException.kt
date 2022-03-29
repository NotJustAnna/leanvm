package net.notjustanna.leanvm

import net.notjustanna.leanvm.types.LAny

public class LAnyException(public val value: LAny) : RuntimeException("Object thrown from Lin: $value")
