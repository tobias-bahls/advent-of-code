package utils

import java.util.BitSet

fun BitSet.toIntSet() = stream().toArray().toSet()

fun BitSet.setAll(elems: List<Int>) = elems.forEach { set(it) }

fun BitSet.clearAll(elems: List<Int>) = elems.forEach { clear(it) }

fun BitSet.copy(): BitSet = clone() as BitSet
