package com.advancedtelematic.interview.wordcount.aholg.solution

import java.io.EOFException

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Framing, Sink, Source}
import akka.util.ByteString
import com.advancedtelematic.interview.wordcount.{CharacterReader, FastCharacterReaderImpl, SlowCharacterReaderImpl}

import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

object Main extends App {
  private implicit val system: ActorSystem = ActorSystem("WordCount")
  private implicit val ec: ExecutionContext = system.dispatcher

  private val fastWordCounter = new WordCount(Seq(new FastCharacterReaderImpl()))
  private val slowWordCounter = new WordCount(Seq.fill(10)(new SlowCharacterReaderImpl()))

  private val fastCounter: Future[Seq[Unit]] = fastWordCounter
    .process
    .map { res =>
      println("Fast final wordcount:")
      res.map(r => println(s"${r._1} - ${r._2}"))
    }

  private val slowCounter: Future[Seq[Unit]] = slowWordCounter
    .process
    .map { res =>
      println("Slow final wordcount:")
      res.map(r => println(s"${r._1} - ${r._2}"))
    }
  for {
    _ <- fastCounter
    _ <- slowCounter
    s <- system.terminate()
  } yield s
}

class WordCount(characterReaders: Seq[CharacterReader])
               (implicit ex: ExecutionContext, actorSystem: ActorSystem) {

  private val forbiddenCharactersRegex = "('\\s|\\s'|[;,:]|^'|'$|\\.)"

  def process: Future[Seq[(String, Int)]] = {
    source
      .statefulMapConcat(() => collectWords)
      .groupedWithin(Int.MaxValue, 10.seconds)
      .map{ ws =>
        val latestCount = ws.lastOption.getOrElse(Map.empty)
        sortByOccurrencesDescending(latestCount.toSeq)
      }
      .wireTap(printWordCountSummary(_))
      .runWith(Sink.lastOption)
      .map(_.getOrElse(Seq.empty))
  }

  private def collectWords: String => List[mutable.Map[String, Int]] = {
    val wordOccurrences = mutable.Map.empty[String, Int]

    word: String => {
      wordOccurrences.update(word, wordOccurrences.getOrElse(word, 0) + 1)
      wordOccurrences :: Nil
    }
  }

  private def source: Source[String, NotUsed] = {
    characterReaders
      .map(cr =>
        Source.fromIterator(() => charIterator(cr))
          .async
          .map(c => ByteString(c))
          .via(Framing.delimiter(ByteString(' '), 256, allowTruncation = true))
      )
      .reduce(_ merge _)
      .map(w => stripNonWordCharacters(w.utf8String))
  }

  private def charIterator(characterReader: CharacterReader): Iterator[Char] = {
    Iterator.continually {
      try {
        val c = characterReader.nextCharacter()
        if (c == '\n') ' '
        else c
      } catch {
        case _: EOFException =>
          characterReader.close()
          '\0'
      }
    }.takeWhile(_ != '\0')
  }

  private def printWordCountSummary(tuples: Seq[(String, Int)]): Unit = {
    println("Wordcount:")
    tuples.foreach(r => println(s"${r._1} - ${r._2}"))
  }

  private def stripNonWordCharacters(word: String): String = {
    word.toLowerCase.replaceAll(forbiddenCharactersRegex, "")
  }

  private def sortByOccurrencesDescending(wordOccurrences: Seq[(String, Int)]): Seq[(String, Int)] = {
    wordOccurrences.sortWith((a, b) => {
      a._2 > b._2 || (a._2 == b._2 && a._1.compareTo(b._1) < 0)
    })
  }
}
