package packages

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TodoApp extends App {
  val persistenceGateway: PersistenceGateway = InMemoryPersistenceGateway
  val applicationBoundary: ApplicationBoundary = new ApplicationCore(persistenceGateway)
  val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
  val userInterface: UserInterface = new UserInterface(applicationBoundary, pattern)

  userInterface.run()
}
