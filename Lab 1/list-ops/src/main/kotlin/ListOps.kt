fun <T> List<T>.customAppend(list: List<T>): List<T> {
    return list.customFoldLeft(this) { acc, item -> acc + item }
}

fun List<Any>.customConcat(): List<Any> {
    return customFoldLeft(emptyList()) { acc, item ->
        if (item is List<*>)
            acc + (item as List<Any>).customConcat()
        else
            acc + item
    }
}

fun <T> List<T>.customFilter(predicate: (T) -> Boolean): List<T> {
    return customFoldLeft(emptyList()) { acc, item ->
        if (predicate(item))
            acc + item
        else acc
    }
}

val List<Any>.customSize: Int get() = customFoldLeft(0) { acc, _ -> acc + 1 }

fun <T, U> List<T>.customMap(transform: (T) -> U): List<U> {
    return customFoldLeft(emptyList()) { acc, item -> acc + transform(item) }
}

fun <T, U> List<T>.customFoldLeft(initial: U, f: (U, T) -> U): U {
    var accumulator = initial

    for (item in this)
        accumulator = f(accumulator, item)

    return accumulator
}

fun <T, U> List<T>.customFoldRight(initial: U, f: (T, U) -> U): U {
    var accumulator = initial

    for (item in this.customReverse())
        accumulator = f(item, accumulator)

    return accumulator
}

fun <T> List<T>.customReverse(): List<T> {
    return customFoldLeft(emptyList()) { acc, item -> listOf(item).customAppend(acc) }
}
