import scala.io.StdIn._

@main
def main(): Unit = {

  //task_01
  print("Enter your name: ")
  val name = readLine()
  println(s"Hello, $name!")

  //task_02
  val add = (x: Int, y: Int) => x + y

  //task_03
  val list = List(1, 2, 3, 4, 5)
  val addOne = (x: Int) => add(x, 1)
  val incrementedList = list.map(addOne)
  println(incrementedList)

  //task_04
  val isEven = (x: Int) => x % 2 == 0
  print("Enter number: ")
  val number = readInt()
  println(isEven(number))

  //task_05
  print("Enter any string: ")
  val anyString = readLine()
  val strLen = anyString.length
  println(s"Your string's length is $strLen")

  //task_06
  val joinStrings = (strings: List[String]) => strings.mkString(" ")
  val stringList = List("AAA", "bbb", "ccccccc")
  val joinedString = joinStrings(stringList)
  println(s"Joined string is: $joinedString")

}

