package components

import Atm
import components.command.Command
import components.command.LoginCommand
import components.command.LogoutCommand
import components.command.bigDecimal.DepositCommand
import components.command.bigDecimal.WithdrawalCommand
import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.CommandProcessorTracer
import pers.apollokwok.tracer.common.generated.`__Outputter_˚Atm`

// This class is annotated with 'Tracer.Nodes' because it's multiton inside Atm.
@Tracer.Nodes(Atm::class)
class CommandProcessor(override val __Atm: Atm) : CommandProcessorTracer {
    var account: Database.Account? = null

    private val commands: Map<String, Command> =
        listOf(
            LoginCommand(),
            DepositCommand(),
            LogoutCommand(),
            WithdrawalCommand(),
        )
        .associateBy {
            it::class.simpleName!!
                .substringBeforeLast(Command::class.simpleName!!)
                .replaceFirstChar(Char::lowercase)
        }

    fun process(input: String): Boolean {
        val key = input.substringBefore(" ")
        val command = commands[key]

        val (msg, continuing) = when{
            command == null ->
                Command.Result(
                    "Couldn't understand $input, which is not in ${commands.keys}. Please try again.\n"
                )

            command !is LoginCommand
            && account == null ->
                Command.Result("Login before input other commands.")

            else -> {
                val content = input.substringAfter(" ", "").trim()
                command.handleInput(content)
            }
        }

        `__Outputter_˚Atm`.output(msg)

        return continuing
    }

    override val _CommandProcessor: CommandProcessor = this
}