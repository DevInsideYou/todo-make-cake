package traits

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TodoApp extends App
                  with ApplicationCoreModule
                  with InMemoryPersistenceGatewayModule
                  with TerminalUserInterfaceModule {
  override val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")

  userInterface.run()
}
