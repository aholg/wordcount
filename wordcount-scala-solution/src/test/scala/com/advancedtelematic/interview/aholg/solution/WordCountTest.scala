package com.advancedtelematic.interview.aholg.solution

import com.advancedtelematic.interview.wordcount.aholg.solution.WordCount
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class WordCountTest extends AnyFunSuite with Matchers {

  test("should return something") {
    WordCount.solution shouldBe Seq.empty
  }
}
