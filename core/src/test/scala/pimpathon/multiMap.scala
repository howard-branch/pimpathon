package pimpathon

import org.junit.Test
import scala.collection.{mutable => M}
import scala.collection.generic.CanBuildFrom

import org.junit.Assert._
import pimpathon.multiMap._


class MultiMapTest {
  @Test def multiMapCBF: Unit = {
    val cbf = MultiMap.build[List, Int, String]
    val builder = cbf.apply()

    builder += 1 -> "foo"
    builder += 1 -> "bar"
    assertEquals(Map(1 -> List("foo", "bar")), builder.result())

    builder.clear()
    assertEquals(Map(), builder.result())
  }

  @Test def ignoreFromCBF: Unit = {
    val ucbf = new UnitCanBuildFrom[List[Int], Int]

    assertEquals(UnitBuilder[Int]("apply()"), ucbf.apply())
    assertEquals(UnitBuilder[Int]("apply(List(1, 2, 3))"), ucbf.apply(List(1, 2, 3)))

    val ucbfi = new UnitCanBuildFrom[List[Int], Int] with IgnoreFromCBF[List[Int], Int, Unit]

    assertEquals(UnitBuilder[Int]("apply()"), ucbfi.apply())
    assertEquals(UnitBuilder[Int]("apply()"), ucbfi.apply(List(1, 2, 3)))
  }

  @Test def merge: Unit = {
    assertEquals(Map(1 -> List(1, 2)), Map(1 -> List(1, 2)).merge(MultiMap.empty[List, Int, Int]))
    assertEquals(Map(1 -> List(1, 2)), MultiMap.empty[List, Int, Int].merge(Map(1 -> List(1, 2))))
    assertEquals(Map(1 -> List(1, 2)), Map(1 -> List(1)).merge(Map(1 -> List(2))))
    assertEquals(Map(1 -> List(1), 2 -> List(2)), Map(1 -> List(1)).merge(Map(2 -> List(2))))
    assertEquals(Map(1 -> Set(1, 2)),  Map(1 -> Set(1)).merge(Map(1 -> Set(2))))
  }

  @Test def select: Unit = {
    assertEquals(Map(1 -> 2, 2 -> 3), Map(1 -> List(2), 2 -> List(3, 4)).select(_.head))
  }

  @Test def append: Unit = {
    assertEquals(Map(1 -> List(2, 3)), MultiMap.empty[List, Int, Int].append(1, List(2, 3)))
    assertEquals(Map(1 -> List(2, 3)), Map(1 -> List(2)).append(1, List(3)))
    assertEquals(Map(1 -> List(2, 3)), Map(1 -> List(2, 3)).append(1, Nil))
  }

  class UnitCanBuildFrom[From, Elem] extends CanBuildFrom[From, Elem, Unit] {
    def apply(): M.Builder[Elem, Unit]           = UnitBuilder[Elem]("apply()")
    def apply(from: From): M.Builder[Elem, Unit] = UnitBuilder[Elem](s"apply($from)")
  }

  case class UnitBuilder[E](from: String) extends M.Builder[E, Unit] {
    def +=(elem: E): this.type = this
    def clear(): Unit = {}
    def result(): Unit = ()
    override def toString = s"UnitBuilder($from)"
  }
}
