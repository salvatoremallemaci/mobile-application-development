class Forth {
    private val dictionary: MutableMap<String, List<String>> = mutableMapOf()

    fun evaluate(vararg line: String): List<Int> {
        val list = mutableListOf<Int>()

        for (str in line) {
            val stack = str.split(' ').toMutableList()

            while (stack.isNotEmpty()) {
                val result = operation(stack, stack.count() - 1)

                // only case when this happens is if the stack is "x drop"
                // or when defining new words
                if (result != null)
                    list.add(0, result)
            }
        }

        return list
    }

    private fun operation(stack: MutableList<String>, i: Int): Int? {
        val s = stack.removeAt(i).lowercase()

        return when {
            dictionary.containsKey(s) -> {
                for (item in dictionary[s]!!)
                    stack.add(item)

                return operation(stack, stack.count() - 1)
            }
            s == ";" -> {
                val definition: MutableList<String> = mutableListOf(s)
                var s2 = ""
                var j = i - 1

                while (s2 != ":") {     // read all token for the definition
                    s2 = stack.removeAt(j--).lowercase()
                    definition.add(0, s2)
                }

                val definitionDescription = definition
                    .filter { it != ":" && it != ";" }
                    .drop(1)    // drop the definition name
                    .toMutableList()

                var definitionDescriptionOk = false
                // can define word that uses a previous definition of the word
                // internal for has to do one complete loop without modifying definitionDescription
                // for the while to exit: this is to avoid modifications at the for iterator while iterating
                while (!definitionDescriptionOk) {
                    definitionDescriptionOk = true
                    for ((i, item) in definitionDescription.withIndex()) {
                        val previousDefinition = dictionary[item]
                        if (previousDefinition != null) {
                            definitionDescriptionOk = false
                            definitionDescription.removeAt(i)
                            for (previousDefinitionItem in previousDefinition.reversed())
                                definitionDescription.add(i, previousDefinitionItem)
                            break
                        }
                    }
                }

                if (definition[1].first().isDigit())    // cannot redefine numbers
                    throw Exception("illegal operation")

                dictionary[definition[1].lowercase()] = definitionDescription

                return null
            }
            s == "+" -> {
                val (op1, op2) = calculateLhsRhs(stack, i)
                return op1 + op2
            }

            s == "*" -> {
                val (op1, op2) = calculateLhsRhs(stack, i)
                return op1 * op2
            }

            s == "-" -> {
                val (op1, op2) = calculateLhsRhs(stack, i)
                return op1 - op2
            }

            s == "/" -> {
                val (op1, op2) = calculateLhsRhs(stack, i)
                if (op2 == 0)
                    throw Exception("divide by zero")

                return op1 / op2
            }

            s == "dup" -> {
                val op = try {
                    operation(stack, i - 1)
                } catch (e: Exception) {
                    throw Exception("empty stack")
                }

                // duplicates by adding to the stack and returning the same operand
                stack.add(op.toString())
                return op
            }

            s == "drop" -> {
                var j = i - 1
                var nDrop = 1
                // counts consecutive drop and operates them all at once
                // the current one is no more on the stack, it is already considered
                while (j >= 0 && stack[j].lowercase() == "drop") {
                    nDrop++
                    j--
                }

                try {
                    for (k in 0 until nDrop) {
                        operation(stack, i - nDrop - k)     // execute all the consecutive drops
                        if (k != nDrop - 1)     // the first drop token has already been removed, avoid removing one more
                            stack.removeAt(i - nDrop - k) // remove the drop token
                    }
                } catch (e: Exception) {
                    throw Exception("empty stack")
                }

                return null
            }

            s == "swap" -> {
                val (op1, op2) = calculateLhsRhs(stack, i)

                // swaps by adding to the stack the top value and returning the second one
                stack.add(op2.toString())
                return op1
            }

            s == "over" -> {
                val (op1, op2) = calculateLhsRhs(stack, i)

                // takes the top of stack and the previous one, puts them again on top
                // of the stack and returns the previous of the two
                stack.add(op1.toString())
                stack.add(op2.toString())
                return op1
            }

            s.first().isDigit() -> s.toInt()
            else -> throw Exception("undefined operation")
        }
    }

    private fun calculateLhsRhs(stack: MutableList<String>, i: Int): Pair<Int, Int> {
        var op1: Int? = null
        var op2: Int? = null
        var op1Failed = false
        var op2Failed = false

        try {
            op2 = operation(stack, i - 1)
        } catch (e: Exception) {
            if (e.message == "undefined operation")
                throw e
            op2Failed = true    // else e.message is OutOfBounds
        }

        try {
            op1 = operation(stack, i - 2)
        } catch (e: Exception) {
            if (e.message == "undefined operation")
                throw e
            op1Failed = true    // else e.message is OutOfBounds
        }

        if (op1Failed && op2Failed)
            throw Exception("empty stack")
        else if (op1Failed || op2Failed)
            throw Exception("only one value on the stack")

        return Pair(op1!!, op2!!)
    }
}