package com.advancedtelematic.interview.aholg.solution

import java.io.EOFException

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.advancedtelematic.interview.wordcount.CharacterReader
import com.advancedtelematic.interview.wordcount.aholg.solution.WordCount
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.{AnyFunSuite, AnyFunSuiteLike}
import org.scalatest.matchers.should.Matchers

class WordCountTest
  extends TestKit(ActorSystem("WordCountTest"))
  with AnyFunSuiteLike
  with Matchers
  with ScalaFutures
  with MockFactory {

  import scala.concurrent.ExecutionContext.Implicits.global

  test("should return empty map for empty string") {
    val words = ""
    val reader: CharacterReader = testReader(words)

    val counter = new WordCount(reader)

    whenReady(counter.process) { res =>
      res shouldBe Seq.empty
    }
  }

  test("should call close on end of stream") {
    val reader: CharacterReader = stub[CharacterReader]

    val counter = new WordCount(reader)
    (reader.nextCharacter _).when().throws(new EOFException())

    whenReady(counter.process) { _ =>
      (reader.close _).verify().once()
    }
  }

  test("should return the count for a single character") {
    val reader: CharacterReader = testReader("A")

    val counter = new WordCount(reader)

    whenReady(counter.process) { res =>
      res shouldBe Seq(("a", 1))
    }
  }

  test("should return the count for a word occurring several times") {
    val words = "word1\nword1\nword1"
    val reader: CharacterReader = testReader(words)

    val counter = new WordCount(reader)

    whenReady(counter.process) { res =>
      res shouldBe Seq(("word1", 3))
    }
  }

  test("should return words with same count in alphabetical order") {
    val words = "wordD\nwordA\nwordB\nwordC"
    val reader: CharacterReader = testReader(words)

    val counter = new WordCount(reader)

    whenReady(counter.process) { res =>
      res should contain theSameElementsInOrderAs Map(
        ("worda", 1),
        ("wordb", 1),
        ("wordc", 1),
        ("wordd", 1)
      ).toSeq
    }
  }

  test("should return words with the highest count first and alphabetically ordered") {
    val words = "wordD\nwordA\nwordB\nwordC\nwordD"
    val reader: CharacterReader = testReader(words)

    val counter = new WordCount(reader)

    whenReady(counter.process) { res =>
      res should contain theSameElementsInOrderAs Seq(
        ("wordd", 2),
        ("worda", 1),
        ("wordb", 1),
        ("wordc", 1)
      )
    }
  }

  test("should separate by spaces and newlines") {
    val words = "wordD wordA wordB\nwordC\nwordD"
    val reader: CharacterReader = testReader(words)

    val counter = new WordCount(reader)

    whenReady(counter.process) { res =>
      res should contain theSameElementsInOrderAs Seq(
        ("wordd", 2),
        ("worda", 1),
        ("wordb", 1),
        ("wordc", 1)
      )
    }
  }

  test("should filter out commas and semicolons") {
    val words = "wordD, wordA; wordB\nwordC\nwordD"
    val reader: CharacterReader = testReader(words)

    val counter = new WordCount(reader)

    whenReady(counter.process) { res =>
      res should contain theSameElementsInOrderAs Seq(
        ("wordd", 2),
        ("worda", 1),
        ("wordb", 1),
        ("wordc", 1)
      )
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
