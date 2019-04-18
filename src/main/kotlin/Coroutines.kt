package hazae41.sockets

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

fun <T, U> Deferred<T>.then(callback: suspend (T) -> U) = let {
    deferred -> GlobalScope.async { callback(deferred.await()) }
}