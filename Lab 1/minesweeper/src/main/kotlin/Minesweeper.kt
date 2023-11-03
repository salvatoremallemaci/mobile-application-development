data class MinesweeperBoard(val minefield: List<String>) {
    private val rMax: Int = minefield.count()
    private val cMax: Int = if (minefield.none()) 0 else minefield[0].count()

    fun withNumbers(): List<String> {
        val mineOut: MutableList<String> = emptyList<String>().toMutableList()

        for ((r, str) in minefield.withIndex()) {
            var mineLineOut = ""

            for ((c, ch) in str.withIndex()) {
                when (ch) {
                    ' ' -> {
                        val adj = countAdjacent(r, c)
                        if (adj == 0) mineLineOut += ' '
                        else mineLineOut += adj
                    }

                    '*' -> mineLineOut += '*'
                    else -> {
                        println("There was an error in the input minefield!")
                        return emptyList()
                    }
                }
            }

            mineOut.add(mineLineOut)
        }

        return mineOut.toList()
    }

    private fun countAdjacent(r: Int, c: Int): Int {
        var adj = 0

        // check N
        if (r > 0 && minefield[r-1][c] == '*') { adj += 1 }

        // check S
        if (r < rMax-1 && minefield[r+1][c] == '*') { adj += 1 }

        // check E
        if (c < cMax-1 && minefield[r][c+1] == '*') { adj += 1 }

        // check O
        if (c > 0 && minefield[r][c-1] == '*') { adj += 1 }

        // check NO
        if (r > 0 && c > 0 && minefield[r-1][c-1] == '*') { adj += 1 }

        // check NE
        if (r > 0 && c < cMax-1 && minefield[r-1][c+1] == '*') { adj += 1 }

        // check SO
        if (r < rMax-1 && c > 0 && minefield[r+1][c-1] == '*') { adj += 1 }

        // check SE
        if (r < rMax-1 && c < cMax-1 && minefield[r+1][c+1] == '*') { adj += 1 }

        return adj
    }
}
