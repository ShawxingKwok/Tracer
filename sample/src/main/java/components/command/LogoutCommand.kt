package components.command

context (pers.apollokwok.tracer.common.generated.CommandProcessorTracer)
class LogoutCommand : Command {
    override fun handleInput(input: String): Command.Result =
        if (input.any())
            Command.Result("Logout failed since it unexpectedly ends with some content.")
        else
            Command.Result("Logout successfully, have a nice day!", false)
}