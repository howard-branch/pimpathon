package pimpathon

import scala.annotation.tailrec
import scala.collection.{breakOut, mutable => M, GenTraversableLike}
import scala.collection.breakOut
import scala.collection.generic.{CanBuildFrom, FilterMonadic}
import scala.collection.immutable._

import pimpathon.any._
import pimpathon.function._
import pimpathon.multiMap._
import pimpathon.option._
import pimpathon.tuple._


object list extends filterMonadic with genTraversableLike[List] {
  implicit class ListOps[A](val list: List[A]) extends AnyVal {
    def tap(empty: => Unit, nonEmpty: List[A] => Unit): List[A] = new AnyOps(list).tap(_.uncons(empty, nonEmpty))
    def tapEmpty(empty: => Unit): List[A] = tap(empty, _ => {})
    def tapNonEmpty(nonEmpty: List[A] => Unit): List[A] = tap({}, nonEmpty)

    def emptyTo(alternative: => List[A]): List[A] = uncons(alternative, _ => list)

    def uncons[B](empty: => B, nonEmpty: List[A] => B): B = if (list.isEmpty) empty else nonEmpty(list)

    def unconsC[B](empty: => B, nonEmpty: A => List[A] => B): B = list match {
      case Nil => empty
      case head :: tail => nonEmpty(head)(tail)
    }

    def mapNonEmpty[B](f: List[A] => B): Option[B] = if (list.isEmpty) None else Some(f(list))

    def zipToMap[B](values: List[B]): Map[A, B] = zip(values).toMap
    def zipWith[B, C](values: List[B])(f: ((A, B)) => C): List[C] = zip(values).map(f).toList


    def fraction(p: Predicate[A]): Double = countWithSize(p).fold(Double.NaN)(_.to[Double].calc(_ / _))

    def countWithSize(p: Predicate[A]): Option[(Int, Int)] = mapNonEmpty(_.foldLeft((0, 0)) {
      case ((passed, size), elem) => (if (p(elem)) passed + 1 else passed, size + 1)
    })


    def distinctBy[B](f: A => B): List[A] = list.map(equalBy(f)).distinct.map(_.a)

    def batchBy[B](f: A => B): List[List[A]] = list.unconsC(empty = Nil, nonEmpty = head => tail => {
      val (_, batch, batches) = tail.foldLeft((f(head), M.ListBuffer(head), M.ListBuffer[List[A]]())) {
        case ((currentKey, batch, batches), a) => f(a).cond(_ == currentKey,
          ifTrue  = key => (key, batch += a,      batches),
          ifFalse = key => (key, M.ListBuffer(a), batches += batch.toList)
        )
      }

      (batches += batch.toList).toList
    })

    def headTail: (A, List[A]) = headTailOption.getOrThrow("headTail of empty list")
    def initLast: (List[A], A) = initLastOption.getOrThrow("initLast of empty list")

    def headTailOption: Option[(A, List[A])] = unconsC(None, head => tail => Some((head, tail)))

    def initLastOption: Option[(List[A], A)] = uncons(None, _ => Some(list.init, list.last))

    def seqMap[B](f: A => Option[B]): Option[List[B]] =
      apoMap[B, Option[List[B]]](Some(_))(a => f(a).toRight(None))

    def tailOption: Option[List[A]] = uncons(None, nonEmpty => Some(nonEmpty.tail))

    def const[B](elem: B): List[B] = list.map(_ => elem)

    def prefixPadTo(len: Int, elem: A): List[A] = List.fill(len - list.length)(elem) ++ list

    def sharedPrefix(other: List[A])(implicit compare: A => A => Boolean = equalC[A]): (List[A], List[A], List[A]) = {
      @tailrec def recurse(lefts: List[A], rights: List[A], acc: List[A]): (List[A], List[A], List[A]) = {
        (lefts, rights) match {
          case (left :: lhs, right :: rhs) if compare(left)(right) => recurse(lhs, rhs, left :: acc)
          case _                                                   => (acc.reverse, lefts, rights)
        }
      }

      recurse(list, other, Nil)
    }

    private def apoMap[B, C](g: List[B] => C)(f: A => Either[C, B]): C = {
      @tailrec def recurse(acc: List[B], rest: List[A]): C = rest match {
        case Nil => g(acc.reverse)
        case head :: tail => f(head) match {
          case Left(c) => c
          case Right(b) => recurse(b :: acc, tail)
        }
      }

      recurse(Nil, list)
    }

    private def equalBy[B](f: A => B)(a: A): EqualBy[A, B] = new EqualBy(f(a))(a)
    private def zip[B](other: List[B]): Iterator[(A, B)] = list.iterator.zip(other.iterator)
  }
}

case class EqualBy[A, B](b: B)(val a: A)
