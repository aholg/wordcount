package com.advancedtelematic.interview.aholg.solution

import java.io.EOFException

import com.advancedtelematic.interview.wordcount.CharacterReader
import com.advancedtelematic.interview.wordcount.aholg.solution.WordCount
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WordCountTest extends AnyFunSuite with Matchers with ScalaFutures {

  import scala.concurrent.ExecutionContext.Implicits.global

  test("should return empty map for empty string") {
    val words = ""
    val reader: CharacterReader = testReader(words)

    val counter = new WordCount(reader)

    whenReady(counter.process) { res =>
      res shouldBe Map.empty
    }
  }

  test("should return the count for a single character") {
    val reader: CharacterReader = testReader("A")

    val counter = new WordCount(reader)

    whenReady(counter.process) { res =>
      res shouldBe Map("A" -> 1)
    }
  }

  test("should return the count for a word occurring several times") {
    val words = "word1\nword1\nword1"
    val reader: CharacterReader = testReader(words)

    val counter = new WordCount(reader)

    whenReady(counter.process) { res =>
      res shouldBe Map("word1" -> 3)
    }
  }

  private def testReader(data: String) = {
    new CharacterReader {
      var pos = 0

      override def nextCharacter(): Char = {
        if (pos < data.length) {
          val c = data.charAt(pos)
          pos += 1
          c
        }
        else throw new EOFException()
      }

      override def close(): Unit = ()
    }
  }
}
