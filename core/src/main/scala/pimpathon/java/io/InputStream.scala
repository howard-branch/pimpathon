package pimpathon.java.io

import java.io.{InputStream, OutputStream}
import scala.annotation.tailrec
import scala.util.Try

import pimpathon.any._
import pimpathon.java.io.outputStream._


object inputStream extends InputStreamUtils(closeIn = true, closeOut = true)

case class InputStreamUtils(closeIn: Boolean, closeOut: Boolean, bufSize: Int = 8192) {
  implicit class InputStreamOps[IS <: InputStream](val is: IS) {
    def read(os: OutputStream, closeIn: Boolean = closeIn, closeOut: Boolean = closeOut): IS =
      is.tap(copy(_, os, closeIn, closeOut))

    def attemptClose(): Try[Unit] = Try(is.close)
    def closeIf(condition: Boolean): IS     = is.tapIf(_ => condition)(_.close)
    def closeUnless(condition: Boolean): IS = is.tapUnless(_ => condition)(_.close)
  }

  def copy(
    is: InputStream, os: OutputStream,
    closeIn: Boolean = closeIn, closeOut: Boolean = closeOut, buf: Array[Byte] = new Array[Byte](bufSize)
  ): Unit = {
    @tailrec def recurse(): Unit = {
      val len = is.read(buf)

      if (len > 0) {
        os.write(buf, 0, len)
        recurse()
      }
    }

    Try(recurse())
    if (closeIn)  is.attemptClose
    if (closeOut) os.attemptClose
  }
}