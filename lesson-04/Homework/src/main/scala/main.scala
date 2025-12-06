import scala.io.StdIn._

@main
def main(): Unit = {

  //task_01
  var age: Int = 25
  var weight: Double = 70.3
  var name: String = "George"
  var isStudent: Boolean = false
  //task_02
  println(s"age: $age weight: $weight name: $name isStudent: $isStudent")

  //task_03
  val add = (x: Int, y: Int) => x + y
  //task_03_1
  val add_call = add(1, 2)
  println(s"add(1, 2) call result is: $add_call")

  //task_04
  def getAgeCategory(age: Int): String = {
    if age < 30 then "Молодой" else "Взрослый"
  }
  //task_04_1
  val currentAge = 22
  val ageCategory = getAgeCategory(currentAge)
  println(s"Age category for the age of $currentAge is $ageCategory")

  //task_05
  for (i <- 1 to 10) {
    println(s"i is $i")
  }
  //task_05_1
  val students = List("Andrey", "George", "Max", "Ann", "Sarah", "Maria")
  for (s <- students) {
    println(s)
  }

  //List comprehensions
  val numList = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  val squareList = for {el <- numList} yield el*el
  val evenList = for {el <- numList if el % 2 == 0} yield el
  println(s"Square list is $squareList")
  println(s"Even list is $evenList")

}

