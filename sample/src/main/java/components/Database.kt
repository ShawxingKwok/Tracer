package components

import pers.apollokwok.tracer.common.generated._Outputter
import java.math.BigDecimal

// The upper tracer Root/Nodes of Database is Atm, so here uses 'context (AtmTracer)' to get built
// tracer properties under 'AtmTracer'.
context (pers.apollokwok.tracer.common.generated.AtmTracer)
class Database{
    private val accounts = mutableMapOf<String, Account>()

    // Here declares needed outer properties.
    // Use 'private' and '=' rather than 'get() =' oriented to built tracer properties because
    // it may be mutable or not initialized yet.
    private val outputter: Outputter get() = _Outputter

    fun getOrCreateAccount(username: String) =
        accounts.getOrPut(username) {
            val account = Account(username)
            // If you are sure 'outputter' appears only once in this class you could omit its declaration
            // and use '_Outputter' directly.
            outputter.output(
                "Account of $username is created with credit limit ${-account.minimumBalance} dollars."
            )
            account
        }

    class Account(val username: String){
        var balance: BigDecimal = BigDecimal.ZERO
        val minimumBalance = BigDecimal(-1000)
    }
}