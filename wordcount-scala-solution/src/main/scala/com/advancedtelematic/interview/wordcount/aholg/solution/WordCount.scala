package com.advancedtelematic.interview.wordcount.aholg.solution

import java.io.EOFException

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Framing, Sink, Source}
import akka.util.ByteString
import com.advancedtelematic.interview.wordcount.{CharacterReader, FastCharacterReaderImpl}

import scala.concurrent.{ExecutionContext, Future}

class WordCount(characterReader: CharacterReader)(implicit ex: ExecutionContext, actorSystem: ActorSystem) {

  private val charIterator: Iterator[Char] = {
    Iterator.continually {
      val c = characterReader.nextCharacter()
      if (c == '\n') ' '
      else c
    }
  }

  private val forbiddenCharactersRegex = "('\\s|\\s'|[;,]|^'|'$|\\.)"

  def process: Future[Seq[(String, Int)]] = {
    source
      .via(Framing.delimiter(ByteString(' '), 256, allowTruncation = true).map(_.utf8String.toLowerCase.replaceAll(forbiddenCharactersRegex, "")))
      .runWith(Sink.seq[String])
      .map(_.groupBy(identity).mapValues(_.size).toSeq)
      .map(sortByOccurrencesDescending)
  }

  private def source: Source[ByteString, NotUsed] = {
    Source
      .fromIterator(() => charIterator)
      .map(ByteString(_))
      .recover {
        case _: EOFException =>
          characterReader.close()
          ByteString("")
      }
  }

  private def sortByOccurrencesDescending(wordOccurences: Seq[(String, Int)]): Seq[(String, Int)] = {
    wordOccurences.sortWith((a, b) => {
      a._2 > b._2 || (a._2 == b._2 && a._1.compareTo(b._1) < 0)
    })
  }
}
