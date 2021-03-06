package pimpathon

import org.junit.Test
import scala.collection.{mutable => M}
import scala.util.{Failure, Success}

import org.junit.Assert._
import pimpathon.any._
import pimpathon.util._


class AnyTest {
  @Test def calc: Unit = {
    assertEquals("123", "12".calc(_ + "3"))
    assertEquals("123", "12" |> (_ + "3"))
  }

  @Test def tap: Unit = {
    val ints = new M.ListBuffer[Int]

    1.tap(ints += _, ints += _)
    assertEquals(List(1, 1), ints.toList)
  }

  @Test def update: Unit = {
    val ints = new M.ListBuffer[Int]

    1.update(ints += _, ints += _)
    assertEquals(List(1, 1), ints.toList)
  }

  @Test def withSideEffect: Unit = {
    val ints = new M.ListBuffer[Int]

    1.withSideEffect(ints += _, ints += _)
    assertEquals(List(1, 1), ints.toList)
  }

  @Test def tapIf: Unit = {
    assertEquals(List(1, 3), new M.ListBuffer[Int].tap(ints => {
      List(1, 2, 3).foreach(i => i.tapIf(_ % 2 != 0)(ints += _))
    }).toList)
  }

  @Test def tapUnless: Unit = {
    assertEquals(List(2), new M.ListBuffer[Int].tap(ints => {
      List(1, 2, 3).foreach(i => i.tapUnless(_ % 2 != 0)(ints += _))
    }).toList)
  }

  @Test def cond: Unit = {
    assertEquals("true",   "true".cond(_ == "true", _ => "true", _ => "false"))
    assertEquals("false", "false".cond(_ == "true", _ => "true", _ => "false"))
  }

  @Test def partialMatch: Unit = {
    assertEquals(Some("Matched"), 1 partialMatch { case 1 => "Matched" })
    assertEquals(None,            0 partialMatch { case 1 => "Matched" })
  }

  @Test def lpair: Unit = {
    assertEquals((10, 1), 1.lpair(_ * 10))
  }

  @Test def rpair: Unit = {
    assertEquals((1, 10), 1.rpair(_ * 10))
  }

  @Test def filterSelf: Unit = {
    assertEquals(List(None, Some(2), None, Some(4)),
      List(1, 2, 3, 4).map(_.filterSelf(_ % 2 == 0)))
  }

  @Test def withFinally: Unit = {
    assertEquals(List("body: input", "finally: input", "done"), new M.ListBuffer[String].tap(strings => {
      strings += "input".withFinally(s => strings += "finally: " + s)(s => {strings += "body: " + s; "done"})
    }).toList)
  }

  @Test def attempt: Unit = {
    assertEquals(Success(2), 1.attempt(_ * 2))
    assertEquals(Failure(boom), 1.attempt(_ => throw boom))
  }

  @Test def addTo: Unit = {
    val ints = new M.ListBuffer[Int]

    1.addTo(ints)
    assertEquals(List(1), ints.toList)
  }
}
