package pimpathon.java.io

import java.io.{BufferedOutputStream, InputStream, OutputStream}
import scala.annotation.tailrec
import scala.util.Try

import pimpathon.any._
import pimpathon.java.io.inputStream._


object outputStream extends OutputStreamUtils(closeOut = true, closeIn = true, bufferSize = 8192)

case class OutputStreamUtils(closeOut: Boolean, closeIn: Boolean, bufferSize: Int) {
  implicit class OutputStreamOps[OS <: OutputStream](val os: OS) {
    def drain(is: InputStream, closeOut: Boolean = closeOut, closeIn: Boolean = closeIn): OS =
      os.tap(is.drain(_, closeIn, closeOut))

    def <<(is: InputStream): OS = drain(is, closeOut = false, closeIn = false)

    def attemptClose(): Try[Unit] = Try(os.close())
    def closeAfter[A](f: OS => A): A        = os.withFinally(_.attemptClose())(f)
    def closeIf(condition: Boolean): OS     = os.tapIf(_ => condition)(_.close())
    def closeUnless(condition: Boolean): OS = os.tapUnless(_ => condition)(_.close())

    def buffered: BufferedOutputStream = new BufferedOutputStream(os, bufferSize)
  }
}
