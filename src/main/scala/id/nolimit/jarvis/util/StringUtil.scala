package id.nolimit.jarvis.util

/**
  * Created by nabilfarras on 24/03/18.
  */
object StringUtil {

  private val charSet = ('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')

  def camel2Underscore(text: String) = text.drop(1).foldLeft(text.headOption.map(_.toLower + "") getOrElse "") {
    case (acc, c) if c.isUpper => acc + "_" + c.toLower
    case (acc, c) => acc + c
  }

  def tilde(name : String) = s"`$name`"

  def randomString(size : Int = 6) = {
    val rand = new java.util.Random()
    (0 to size).foldLeft(List.empty[String]) { (memo, _) =>
      val curChar = rand.nextInt(charSet.size)
      memo :+ charSet(curChar).toString
    }.mkString
  }
}