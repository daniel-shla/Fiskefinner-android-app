package no.uio.ifi.in2000.danishah.figmatesting.ml

object SpeciesMapper {
    private val speciesToId = mapOf(
        "torsk" to 0,
        "makrell" to 1,
        "sei" to 2,
        "ørret" to 3,
        "sjøørret" to 4,
        "laks" to 5,
        "gjedde" to 6,
        "røye" to 7,
        "hyse" to 8,
        "abbor" to 9,
        "havabbor" to 10,
        "steinbit" to 11,
        "kveite" to 12,
        "rødspette" to 13
    )

    fun getId(name: String): Float {
        return speciesToId[name.trim().lowercase()]?.toFloat() ?: -1f
    }

    fun getName(id: Int): String? {
        return speciesToId.entries.firstOrNull { it.value == id }?.key
    }
}
