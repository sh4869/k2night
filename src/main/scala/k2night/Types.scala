package k2night

import fs2.Pipe
import fs2.Stream

type Input[F[_], T] = Stream[F, T]
type Process[F[_], I, O] = Pipe[F, I, O]
type Output[F[_], T] = Pipe[F, T, Unit]


