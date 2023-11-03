package it.polito.mad.playgroundsreservations.database

enum class Sport {
    TENNIS, BASKETBALL, FOOTBALL, VOLLEYBALL, GOLF
}

fun String.toSport(): Sport {
    return when (this) {
        "tennis" -> Sport.TENNIS
        "basketball" -> Sport.BASKETBALL
        "football" -> Sport.FOOTBALL
        "volleyball" -> Sport.VOLLEYBALL
        "golf" -> Sport.GOLF
        else -> throw Exception("Non-existing sport!")
    }
}