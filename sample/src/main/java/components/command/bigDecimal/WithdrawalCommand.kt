package components.command.bigDecimal

import components.Database
import pers.apollokwok.tracer.common.generated.`_DatabaseAccount？`
import java.math.BigDecimal

context (pers.apollokwok.tracer.common.generated.CommandProcessorTracer)
class WithdrawalCommand : BigDecimalCommand(){
    private val account: Database.Account get() = `_DatabaseAccount？`!!

    override fun handleAmount(amount: BigDecimal): String{
        val max = account.balance.subtract(account.minimumBalance)
        if (amount > max)
            return "You may withdrawal no more than $max."
        else{
            account.balance -= amount
            return "your new balance is: ${account.balance}."
        }
    }
}