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

  def process: Future[Map[String, Int]] = {
    source
      .via(Framing.delimiter(ByteString('\n'), 256, allowTruncation = true).map(_.utf8String))
      .filter(_.nonEmpty)
      .runWith(Sink.seq[String])
      .map(_.groupBy(identity).mapValues(_.size))
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
