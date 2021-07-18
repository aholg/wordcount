package com.advancedtelematic.interview.wordcount.aholg.solution

import java.io.EOFException

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Framing, Sink, Source}
import akka.util.ByteString
import com.advancedtelematic.interview.wordcount.CharacterReader

import scala.concurrent.{ExecutionContext, Future}

class WordCount(characterReader: CharacterReader)(implicit ex: ExecutionContext) {

  implicit val system: ActorSystem = ActorSystem("WordCount")

  def process: Future[Seq[(String, Int)]] = {
    source
      .via(Framing.delimiter(ByteString('\n'), 256, allowTruncation = true).map(_.utf8String))
      .filter(_.nonEmpty)
      .runWith(Sink.seq[String])
      .map(_.groupBy(identity).mapValues(_.size).toSeq)
      .map(sortByOccurrencesDescending)
  }

  private def sortByOccurrencesDescending(wordOccurences: Seq[(String, Int)]): Seq[(String, Int)] = {
    wordOccurences.sortWith((a, b) => a._2 > b._2 || (a._2 == b._2 && a._1 < b._1))
  }

  private def source: Source[ByteString, NotUsed] = {
    Source
      .fromIterator(() => Iterator.continually(characterReader.nextCharacter()))
      .map(ByteString(_))
      .recover {
        case _: EOFException =>
          characterReader.close()
          ByteString("")
      }
  }
}
