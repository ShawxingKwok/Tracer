package components.command

import components.CommandProcessor
import pers.apollokwok.tracer.common.generated.`__Database_˚Atm`

context (pers.apollokwok.tracer.common.generated.CommandProcessorTracer)
class LoginCommand : Command {
    // in this class, tracer property begins with '__' means outside CommandRouter,
    // and the other '_' means inside CommandRouter.
    private val database get()  = `__Database_˚Atm`
    private val router: CommandProcessor get()  = _CommandProcessor

    override fun handleInput(input: String): Command.Result =
        when {
            input.none() -> Command.Result("Empty username in input.")

            router.account != null -> Command.Result("Please logout before login.")

            else -> {
                router.account = database.getOrCreateAccount(input)
                Command.Result("$input is logged with balance ${router.account!!.balance}.")
            }
        }
}