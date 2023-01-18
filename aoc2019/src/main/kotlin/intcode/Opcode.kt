package intcode

sealed class Opcode(val numArgs: Int) {
    val size = numArgs + 1

    data class Add(val a: Address, val b: Address, val dst: Address) : Opcode(3) {
        constructor(
            args: IntArray
        ) : this(args[0].toAddress(), args[1].toAddress(), args[2].toAddress()) {
            check(args.size == numArgs) {
                error("Too many args passed to ${this::class.simpleName}")
            }
        }
    }

    data class Mul(val a: Address, val b: Address, val dst: Address) : Opcode(3) {
        constructor(
            args: IntArray
        ) : this(args[0].toAddress(), args[1].toAddress(), args[2].toAddress()) {
            check(args.size == numArgs) {
                error("Too many args passed to ${this::class.simpleName}")
            }
        }
    }

    object Halt : Opcode(0)
}
