package net.sh4869.bot

import fs2.Pipe
import fs2.Stream


object Core {
  type Input[F[_], T] = Stream[F, T]
  type Process[F[_], I, O] = Pipe[F, I, O]
  type Output[F[_], T] = Pipe[F, T, Unit]

  case class Bot[F[_], I, O](name: String, input: Input[F, I], process: Process[F, I, O], output: Output[F, O]) {
    def stream: Pipe[F, String, Unit] => Stream[F, Unit] = onError =>
      input.through(process).through(output).handleErrorWith { e => Stream(s"bot $name error: ${e.toString}\n").through(onError) }
  }

}

