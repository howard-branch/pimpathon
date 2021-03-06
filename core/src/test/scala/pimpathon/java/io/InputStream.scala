package pimpathon.java.io

import java.io.{BufferedInputStream, ByteArrayInputStream, ByteArrayOutputStream, InputStream, OutputStream}
import org.junit.Test
import scala.util.{Failure, Success}

import org.junit.Assert._
import pimpathon.java.io.inputStream._
import pimpathon.util._


class InputStreamTest {
  @Test def attemptClose: Unit = {
    assertEquals(Success(()), createInputStream().attemptClose())
    assertEquals(Failure(boom), createInputStream(onClose = () => throw boom).attemptClose())
  }

  @Test def closeAfter: Unit = {
    val is = createInputStream()

    assertInputStreamClosed(false, is.closed)
    assertEquals("result", is.closeAfter(_ => "result"))
    assertInputStreamClosed(true, is.closed)
  }

  @Test def closeIf: Unit = {
    val is = createInputStream()

    assertInputStreamClosed(false, is.closed)
    assertInputStreamClosed(false, is.closeIf(false).closed)
    assertInputStreamClosed(true,  is.closeIf(true).closed)
  }

  @Test def closeUnless: Unit = {
    val is = createInputStream()

    assertInputStreamClosed(false, is.closed)
    assertInputStreamClosed(false, is.closeUnless(true).closed)
    assertInputStreamClosed(true,  is.closeUnless(false).closed)
  }

  @Test def drain: Unit = {
    for {
      expectedCloseIn  <- List(false, true)
      expectedCloseOut <- List(false, true)
      input            <- List("Input", "Repeat" * 100)
    } {
      val (is, os) = (createInputStream(input.getBytes), createOutputStream())

      is.drain(os, expectedCloseIn, expectedCloseOut)

      assertEquals(input, os.toString)
      assertInputStreamClosed(expectedCloseIn, is.closed)
      assertOutputStreamClosed(expectedCloseOut, os.closed)
    }

    ignoreExceptions {
      val (is, os) = (createInputStream(), createOutputStream())

      is.drain(os, closeOut = false)
      is.drain(os, closeIn = false)
      is.drain(os, closeOut = false, closeIn = false)
      is.drain(os, closeIn = false, closeOut = false)
    }
  }

  @Test def >> : Unit = {
    val (is, os) = (createInputStream("content".getBytes), createOutputStream())

    is >> os

    assertEquals("content", os.toString)
    assertInputStreamClosed(false, is.closed)
    assertOutputStreamClosed(false, os.closed)
  }

  @Test def buffered: Unit = {
    val (is, os) = (createInputStream("content".getBytes), createOutputStream())
    (is.buffered: BufferedInputStream).drain(os)

    assertEquals("content", os.toString)
  }
}
