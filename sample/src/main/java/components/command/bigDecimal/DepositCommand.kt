package components.command.bigDecimal

import components.Database
import pers.apollokwok.tracer.common.generated.`_DatabaseAccount？`
import java.math.BigDecimal

context (pers.apollokwok.tracer.common.generated.CommandProcessorTracer)
class DepositCommand : BigDecimalCommand(){
    private val account: Database.Account get() = `_DatabaseAccount？`!!

    override fun handleAmount(amount: BigDecimal): String {
        account.balance += amount

        return "${account.username} now has: ${account.balance} dollars."
    }
}