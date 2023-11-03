package it.polito.mad.playgroundsreservations.database

enum class Gender {
    MALE, FEMALE, OTHER
}

fun String.toGender(): Gender {
    return when (this) {
        "male" -> Gender.MALE
        "female" -> Gender.FEMALE
        "other" -> Gender.OTHER
        else -> throw Exception("Non-existing gender!")
    }
}
