package hazae41.sockets

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

typealias ClientWebSocketHandler = suspend DefaultClientWebSocketSession.() -> Unit

data class Connection(val host: String, val port: Int)

fun Connection.conversation(path: String, block: ClientWebSocketHandler) = GlobalScope.launch {
    val client = HttpClient(Apache).config { install(WebSockets) }
    client.ws(HttpMethod.Get, host = host, port = port, path = path, block = block)
}

fun Connection.request(
    path: String,
    data: String? = null,
    callback: suspend (String) -> Unit = {}
) = conversation(path){
    if(data != null) send(data)
    callback(readMessage())
}