import org.intellij.lang.annotations.Language
import utils.filterNotBlank
import utils.mapFirst
import utils.matchOrNull
import utils.part1
import utils.part2
import utils.readResourceAsString
import utils.toEnum
import utils.twoParts

private fun validateBetween(range: IntRange): (String) -> Boolean = { value ->
    value.toInt() in range
}

private fun validateRegex(@Language("regexp") regex: String): (String) -> Boolean = { value ->
    value.matches(regex.toRegex())
}

private fun validateOneOf(vararg options: String): (String) -> Boolean = { value ->
    value in options.toSet()
}

private fun validateHeight(): (String) -> Boolean =
    lambda@{ value ->
        val (height, num) = value.matchOrNull("""([0-9]+)(in|cm)""") ?: return@lambda false

        when (num) {
            "cm" -> height.toInt() in 150..193
            "in" -> height.toInt() in 59..76
            else -> error("Unreachable: $num")
        }
    }

private enum class PassportFieldType(private val validationFun: (String) -> Boolean) {
    BYR(validateBetween(1920..2002)),
    IYR(validateBetween(2010..2020)),
    EYR(validateBetween(2020..2030)),
    HGT(validateHeight()),
    HCL(validateRegex("""#[0-9a-f]{6}""")),
    ECL(validateOneOf("amb", "blu", "brn", "gry", "grn", "hzl", "oth")),
    PID(validateRegex("""[0-9]{9}""")),
    CID({ true });

    fun validate(input: String) = validationFun(input)
}

private data class PassportField(val field: PassportFieldType, val value: String) {
    val valid
        get() = field.validate(value)
}

private fun parsePassportField(input: String): PassportField {
    val (type, value) = input.twoParts(":").mapFirst { it.toEnum<PassportFieldType>() }

    return PassportField(type, value)
}

private val requiredFields = PassportFieldType.values().toSet() - PassportFieldType.CID

private data class Passport(val fields: List<PassportField>) {
    val presentFieldTypes = fields.map { it.field }
    val hasAllRequiredFields = presentFieldTypes.containsAll(requiredFields)
}

private fun parsePassport(input: String) =
    input.replace("\n", " ").split(" ").map { parsePassportField(it) }.let { Passport(it) }

fun main() {
    val input = readResourceAsString("/day04.txt")

    val parsed = input.split("\n\n").filterNotBlank().map { parsePassport(it) }

    part1 { parsed.count { it.hasAllRequiredFields } }

    part2 {
        parsed.count { passport ->
            passport.hasAllRequiredFields && passport.fields.all { it.valid }
        }
    }
}
