package traits

import scala.io._
import scala.util._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

trait TerminalUserInterfaceModule {
  this: EntitiesModule with ApplicationBoundaryModule =>

  def pattern: DateTimeFormatter

  object userInterface {
    def run(): Unit = {
      var shouldKeepLooping = true

      while(shouldKeepLooping) {
        withPrompt {
          case "c"                          => create()
          case "d"                          => delete()
          case "da"                         => deleteAll()
          case "sa"                         => showAll()
          case "sd"                         => searchByPartialDescription()
          case "sid"                        => searchById()
          case "ud"                         => updateDescription()
          case "udl"                        => updateDeadline()
          case "e" | "q" | "exit" | "quit"  => exit()
          case _                            =>
        }
      }

      def withPrompt(onUserInput: String => Unit): Unit = {
        val userInput = StdIn.readLine(menu).toLowerCase.trim

        onUserInput(userInput)
      }

      def menu: String =
        s"""|
            |$hyphens
            |
            |c                   => create new todo
            |d                   => delete todo
            |da                  => delete all todos
            |sa                  => show all todos
            |sd                  => search by partial description
            |sid                 => search by id
            |ud                  => update description
            |udl                 => update deadline
            |e | q | exit | quit => exit the application
            |anything else       => show the main menu
            |
            |Please enter a command: """.stripMargin

      def hyphens: String =
        randomColor + ("-" * 100) + Console.RESET

      def randomColor: String = {
        val randomIndex = scala.util.Random.nextInt(colors.size)

        colors(randomIndex)
      }

      lazy val colors: Vector[String] =
        Vector(
          // Console.BLACK,
          Console.BLUE,
          Console.CYAN,
          Console.GREEN,
          Console.MAGENTA,
          Console.RED,
          // Console.WHITE,
          Console.YELLOW
        )

      def exit(): Unit = {
        println()
        println("Until next time!")
        println()

        shouldKeepLooping = false
      }
    }

    // C
    private def create(): Unit =
      withDescriptionPrompt { description =>
        withDeadlinePrompt { deadline =>
          val createdTodo = Todo.Data(description, deadline)

          applicationBoundary.createOne(createdTodo)

          println(Console.GREEN + "Successfully created the new todo" + Console.RESET)
        }
      }

    private def withDescriptionPrompt(onSuccess: String => Unit): Unit = {
      val userInput = StdIn.readLine("Please enter a description: ").trim

      onSuccess(userInput)
    }

    private def withDeadlinePrompt(onSuccess: LocalDateTime => Unit): Unit = {
      val pattern = "yyyy-M-d H:m"

      val format = Console.MAGENTA + pattern + Console.RESET
      val formatter = DateTimeFormatter.ofPattern(pattern)

      val userInput = StdIn.readLine(s"Please enter a deadline in the following format $format: ").trim

      Try(LocalDateTime.parse(userInput, formatter)).toOption match {
        case Some(deadline) => onSuccess(deadline)
        case None           => println(s"\n${Console.YELLOW + userInput + Console.RED} does not match the required format $format${Console.RESET}")
      }
    }

    // D
    private def delete(): Unit = {
      withIdPrompt { id =>
        withReadOne(id) { todo =>
          applicationBoundary.deleteOne(todo)

          println(Console.GREEN + "Successfully deleted the todo" + Console.RESET)
        }
      }
    }

    private def withIdPrompt(onValidId: Id => Unit): Unit = {
      val userInput = StdIn.readLine("Please enter the id: ").trim

      createIdFromString(userInput) match {
        case Some(id) => onValidId(id)
        case None     => println(s"\n${Console.YELLOW + userInput + Console.RED} is not a valid id${Console.RESET}")
      }
    }

    private def withReadOne(id: Id)(onFound: Todo.Existing => Unit): Unit = {
      applicationBoundary.readOneById(id) match {
        case Some(todo) => onFound(todo)
        case None       => displayNoTodosFoundMessage()
      }
    }

    private def deleteAll(): Unit = {
      applicationBoundary.deleteAll()

      println(Console.GREEN + "Successfully deleted all todos" + Console.RESET)
    }

    // R
    private def showAll(): Unit = {
      val zeroOrMany = applicationBoundary.readAll

      displayZeroOrMany(zeroOrMany)
    }

    private def displayZeroOrMany(todos: Set[Todo.Existing]): Unit = {
      if(todos.isEmpty)
        displayNoTodosFoundMessage()
      else {
        val uxMatters = if(todos.size == 1) "todo" else "todos"

        val renderedSize: String =
          Console.GREEN + todos.size + Console.RESET

        println(s"\nFound $renderedSize $uxMatters:\n")

        todos
          .to[Seq]
          .sortBy(_.deadline)(OldestFirst)
          .map(renderedWithPattern)
          .foreach(println)
      }
    }

    private def displayNoTodosFoundMessage(): Unit = {
      println(s"\n${Console.YELLOW}No todos found${Console.RESET}")
    }

    private lazy val OldestFirst: Ordering[LocalDateTime] =
      _ compareTo _

    private def renderedWithPattern(todo: Todo.Existing): String = {
      def renderedId: String =
        Console.GREEN + todo.id + Console.RESET

      def renderedDescription: String =
        Console.MAGENTA + todo.description + Console.RESET

      def renderedDeadline: String =
        Console.YELLOW + todo.deadline.format(pattern) + Console.RESET

      s"$renderedId $renderedDescription is due on $renderedDeadline"
    }

    private def searchByPartialDescription(): Unit = {
      withDescriptionPrompt { description =>
        val zeroOrMany = applicationBoundary.readManyByPartialDescription(description)

        displayZeroOrMany(zeroOrMany)
      }
    }

    private def searchById(): Unit = {
      withIdPrompt { id =>
        val zeroOrOne = applicationBoundary.readOneById(id)
        val zeroOrMany = zeroOrOne.to[Set]

        displayZeroOrMany(zeroOrMany)
      }
    }

    // U
    private def updateDescription(): Unit = {
      withIdPrompt { id =>
        withReadOne(id) { todo =>
          withDescriptionPrompt { description =>
            val updatedTodo = todo.withUpdatedDescription(description)

            applicationBoundary.updateOne(updatedTodo)

            println(Console.GREEN + "Successfully updated the description" + Console.RESET)
          }
        }
      }
    }

    private def updateDeadline(): Unit = {
      withIdPrompt { id =>
        withReadOne(id) { todo =>
          withDeadlinePrompt { deadline =>
            val updatedTodo = todo.withUpdatedDeadline(deadline)

            applicationBoundary.updateOne(updatedTodo)

            println(Console.GREEN + "Successfully updated the deadline" + Console.RESET)
          }
        }
      }
    }
  }
}
