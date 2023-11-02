package org.qwActor.test.tutorial

import org.qwActor.{Actor, ActorContext, ActorRef, ActorSystem}
import org.qwActor.stream.iterator.{StreamSink, StreamSource, StreamBuffer}
import org.qwActor.stream.iterator.messages.{HasNext, Next, End}
import org.scalatest.funsuite.AnyFunSuite

import java.util.concurrent.{CompletableFuture, CountDownLatch}
import scala.util.Using

class BufferActorHelloWorld extends AnyFunSuite{

  class Worker(prev:ActorRef, cd:CountDownLatch, context: ActorContext) extends Actor(context){

    override def process(sender: ActorRef): PartialFunction[Any, Unit] = {
      case "start" =>
        prev.accept(this, HasNext)

      case Next(v) =>
        println(">>"+v)
        Thread.sleep(100)
        prev.accept(this, HasNext)

      case End =>
        cd.countDown()
    }

  }

  test("bufferActorHelloWorld"){
    Using(new ActorSystem) { system =>
      val range = (1 to 20).iterator
      val source = StreamSource[Int](range)

      val buffer = StreamBuffer(source, 10)

      val cd = new CountDownLatch(1)
      val w = system.create(c => new Worker(buffer, cd, c))
      w << "start"

      cd.await()

    }.get
  }

}
