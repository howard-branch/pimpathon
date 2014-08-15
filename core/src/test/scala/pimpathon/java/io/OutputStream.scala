package pimpathon.java.io

import java.io.{InputStream, OutputStream, ByteArrayInputStream, ByteArrayOutputStream}
import org.junit.Test

import org.junit.Assert._
import pimpathon.java.io.outputStream._
import pimpathon.util._


class OutputStreamTest {
  @Test def attemptClose {
    assertEquals(Right(()), createOutputStream().attemptClose())
    assertEquals(Left(boom), createOutputStream(onClose = () => throw boom).attemptClose())
  }

  @Test def closeIf {
    val os = createOutputStream()

    assertInputStreamClosed(false, os.closed)
    assertInputStreamClosed(false, os.closeIf(false).closed)
    assertInputStreamClosed(true,  os.closeIf(true).closed)
  }

  @Test def closeUnless {
    val os = createOutputStream()

    assertInputStreamClosed(false, os.closed)
    assertInputStreamClosed(false, os.closeUnless(true).closed)
    assertInputStreamClosed(true,  os.closeUnless(false).closed)
  }

  @Test def drain {
    for {
      expectedCloseIn  <- List(false, true)
      expectedCloseOut <- List(false, true)
      input            <- List("Input", "Repeat" * 100)
    } {
      val (is, os) = (createInputStream(input.getBytes), createOutputStream())

      os.drain(is, expectedCloseOut, expectedCloseIn)

      assertEquals(input, os.toString)
      assertOutputStreamClosed(expectedCloseOut, os.closed)
      assertInputStreamClosed(expectedCloseIn, is.closed)
    }

    ignoreExceptions {
      val (is, os) = (createInputStream(), createOutputStream())

      os.drain(is, closeOut = false)
      os.drain(is, closeIn = false)
      os.drain(is, closeOut = false, closeIn = false)
      os.drain(is, closeIn = false, closeOut = false)
    }
  }
}
