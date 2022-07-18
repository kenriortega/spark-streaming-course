package part1recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object ScalaRecap extends App {
  // values and variables
  val aBoolean: Boolean = false
  // expressions FP
  val anIfExpression = if (2 > 3) "bigger" else "smaller"
  // instructions (Imperative) vs expressions
  val theUnit = println("Hello, scala") // Unit = "no meaningful" = void in other languages

  // functions
  def myFunction(x: Int) = 42

  // OOP
  class Animal

  class Dog extends Animal

  trait Carnivore {
    def eat(animal: Animal)
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(animal: Animal): Unit = println("Crunch")
  }

  // singleton pattern
  object MySingleton

  // companions
  object Carnivore

  // generics
  trait MyList[A]

  // method notation
  val x = 1 + 2
  val y = 1.+(2)


  // FP
  val incrementer: Int => Int = x => x + 1

  val incremented = incrementer(42)

  // map, flatMap, filter HoF
  val processedList = List(1, 2, 3, 4, 5).map(incrementer)
  // patter Matching
  val unknown: Any = 45
  val ordinal = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "unknown"
  }
  // try-catch
  try {
    throw new NullPointerException
  } catch {
    case e: NullPointerException => "some returned value"
    case _ => "some else"
  }

  // Future

  import scala.concurrent.ExecutionContext.Implicits.global

  val aFuture = Future {
    // some expensive computation, runs on another thread
    42
  }

  aFuture.onComplete {
    case Success(value) => println(s"I`ve found $value")
    case Failure(exception) => println(s"Ex: $exception")
  }

  // Partial func
  val aPartialFunction: PartialFunction[Int, Int] = {
    case 1 => 34
    case 8 => 54
    case _ => 999
  }

  // implicits
  // auto-injection by the compiler
  def methodWithImplicitAgs(implicit x: Int) = x + 43

  implicit val implicitInt = 67
  val implicitCall = methodWithImplicitAgs

  // implicit conversions - implicit def
  case class Person(name: String) {
    def greet = println(s"hi, $name")
  }

  implicit def fromStringToPerson(name: String) = Person(name)

  "Bob".greet // fromStringToPerson("Bob").greet

  // implicit conversion - implicit classes
  implicit class Dog2(name: String) {
    def bark = println("Bark!")
  }

  "Lassie".bark

  /* -local scope
  *  - imported scope
  *  - companion objects of the types involved in the method call
  * */
}
