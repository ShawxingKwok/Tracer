package components.command

import pers.apollokwok.tracer.common.generated.`__Database_˚Atm`
import pers.apollokwok.tracer.common.generated.CommandProcessorTracer
import pers.apollokwok.tracer.common.generated.`_DatabaseAccount？`

context (CommandProcessorTracer)
class LoginCommand : Command {
    // in this class, tracer property begins with '__' means outside CommandRouter,
    // and the other '_' means inside CommandRouter.
    private val database get()  = `__Database_˚Atm`

    private var account
        get() = `_DatabaseAccount？`
        set(value) { `_DatabaseAccount？` = value }

    // This syntax problem would be fixed in the future by kotlin.
//    private var account by ::`_DatabaseAccount？`

    override fun handleInput(input: String): Command.Result =
        when {
            input.none() -> Command.Result("Empty username in input.")

            account != null -> Command.Result("Please logout before login.")

            else -> {
                account = database.getOrCreateAccount(input)
                Command.Result("$input is logged with balance ${account!!.balance}.")
            }
        }
}