package traits

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

trait EntitiesModule {
  type Id
  def createIdFromString(input: String): Option[Id]

  sealed trait Todo {
    protected type ThisType <: Todo

    def description: String

    def withUpdatedDescription(newDescription: String): ThisType

    def deadline: LocalDateTime

    def withUpdatedDeadline(newDeadline: LocalDateTime): ThisType
  }

  // https://issues.scala-lang.org/browse/SI-4440
  case object Todo {
    /*final*/ case class Existing(id: Id, data: Data) extends Todo {
      override protected type ThisType = Existing

      override def description: String =
        data.description

      override def withUpdatedDescription(newDescription: String): ThisType =
        copy(data = data.withUpdatedDescription(newDescription))

      override def deadline: LocalDateTime =
        data.deadline

      override def withUpdatedDeadline(newDeadline: LocalDateTime): ThisType =
        copy(data = data.withUpdatedDeadline(newDeadline))
    }

    /*final*/ case class Data(description: String, deadline: LocalDateTime) extends Todo {
      override protected type ThisType = Data

      override def withUpdatedDescription(newDescription: String): ThisType =
        copy(description = newDescription)

      override def withUpdatedDeadline(newDeadline: LocalDateTime): ThisType =
        copy(deadline = newDeadline)
    }
  }
}
