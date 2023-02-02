package components.command.bigDecimal

import components.command.Command
import java.math.BigDecimal

abstract class BigDecimalCommand : Command {
    final override fun handleInput(input: String): Command.Result {
        val amount =
            try {
                BigDecimal(input)
            }catch (e: java.lang.Exception){
                null
            }

        return when{
            amount == null -> Command.Result("$input is not a valid number")
            amount.signum() <= 0 -> Command.Result("amount must be positive")
            else -> Command.Result(handleAmount(amount))
        }
    }

    /**
     * Handles the given (positive) `amount` of money.
     */
    protected abstract fun handleAmount(amount: BigDecimal): String
}