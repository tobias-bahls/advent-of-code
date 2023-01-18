package intcode

import intcode.OpcodeParameter.AddressParameter

sealed class OpcodeParameter {
    data class AddressParameter(val address: Int) : OpcodeParameter()
    data class ValueParameter(val value: Int) : OpcodeParameter()
}

sealed class Opcode {
    data class Add(val a: OpcodeParameter, val b: OpcodeParameter, val dst: AddressParameter) :
        Opcode()
    data class Mul(val a: OpcodeParameter, val b: OpcodeParameter, val dst: AddressParameter) :
        Opcode()

    object Halt : Opcode()
}
