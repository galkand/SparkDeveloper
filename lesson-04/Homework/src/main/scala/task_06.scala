import scala.io.StdIn._

@main
def task_06(): Unit = {

  def getAgeCategory(age: Int): String = {
    if age < 30 then "Молодой" else "Взрослый"
  }

  print("Enter your name: ")
  val name = readLine()
  print("Enter your age: ")
  val age = readInt()
  val ageCategory = getAgeCategory(age)
  print("Are you a student (y/n): ")
  val studentAnswer = readLine()
  val isStudent = studentAnswer == "y"
  print(s"Username $name of age $age ($ageCategory), student status: $isStudent")

}


