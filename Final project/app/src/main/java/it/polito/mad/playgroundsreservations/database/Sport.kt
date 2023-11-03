package it.polito.mad.playgroundsreservations.database

import it.polito.mad.playgroundsreservations.R

enum class Sport {
    TENNIS, BASKETBALL, FOOTBALL, VOLLEYBALL, GOLF
}

fun Sport.toLocalizedStringResourceId(): Int {
    return when (this) {
        Sport.TENNIS -> R.string.sport_tennis
        Sport.BASKETBALL -> R.string.sport_basketball
        Sport.FOOTBALL -> R.string.sport_football
        Sport.VOLLEYBALL -> R.string.sport_volleyball
        Sport.GOLF -> R.string.sport_golf
    }
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