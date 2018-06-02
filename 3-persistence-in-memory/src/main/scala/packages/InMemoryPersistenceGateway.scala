package packages

object InMemoryPersistenceGateway extends PersistenceGateway {
  private var nextId: Int = 0
  private var state: Set[Todo.Existing] = Set.empty

  // C
  override def writeMany(todos: Set[Todo]): Set[Todo.Existing] = {
    todos.map(writeOne)
  }

  private def writeOne(todo: Todo): Todo.Existing = todo match {
    case item: Todo.Data     => createOne(item)
    case item: Todo.Existing => updateOne(item)
  }

  private def createOne(todo: Todo.Data): Todo.Existing = {
    val created =
      Todo.Existing(
        id   = nextId.toString,
        data = todo
      )

    state  = state  + created

    nextId = nextId + 1

    created
  }

  // R
  override def readManyById(ids: Set[String]): Set[Todo.Existing] =
    state.filter(todo => ids.contains(todo.id))

  override def readManyByPartialDescription(partialDescription: String): Set[Todo.Existing] =
    state.filter(_.description.toLowerCase.contains(partialDescription.toLowerCase))

  override def readAll: Set[Todo.Existing] =
    state

  // U
  private def updateOne(todo: Todo.Existing): Todo.Existing = {
    state = state.filterNot(_.id == todo.id) + todo

    todo
  }

  // D
  override def deleteMany(todos: Set[Todo.Existing]): Unit = {
    state = state.filterNot(element => todos.map(_.id).contains(element.id))
  }

  override def deleteAll(): Unit = {
    state = Set.empty
  }
}
