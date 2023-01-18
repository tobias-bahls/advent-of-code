package intcode

import intcode.Param.AddressParam

sealed class Param {
    data class AddressParam(val address: Int) : Param()
    data class ValueParam(val value: Int) : Param()
}

sealed class Operation {
    data class Add(val a: Param, val b: Param, val dst: AddressParam) : Operation()
    data class Mul(val a: Param, val b: Param, val dst: AddressParam) : Operation()
    data class Input(val dst: AddressParam) : Operation()
    data class Output(val value: Param) : Operation()
    data class JumpIfTrue(val test: Param, val dst: Param) : Operation()
    data class JumpIfFalse(val test: Param, val dst: Param) : Operation()
    data class LessThan(val a: Param, val b: Param, val dst: AddressParam) : Operation()
    data class Equals(val a: Param, val b: Param, val dst: AddressParam) : Operation()

    object Halt : Operation()
}
