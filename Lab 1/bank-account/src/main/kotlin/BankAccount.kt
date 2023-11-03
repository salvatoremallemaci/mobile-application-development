import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class BankAccount {
    private var atomicBalance = AtomicLong(0)
    val balance: Long
        get() {
            if (closed.get()) throw IllegalStateException("The account is closed!")
            else return atomicBalance.get()
        }

    private var closed = AtomicBoolean(false)

    fun adjustBalance(amount: Long) {
        if (closed.get()) throw IllegalStateException("The account is closed!")

        atomicBalance.addAndGet(amount)
    }

    fun close() {
        closed.set(true)
    }
}
