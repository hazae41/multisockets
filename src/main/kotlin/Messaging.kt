package hazae41.sockets

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.channels.consumeEach

typealias ServerWebSocketHandler = suspend DefaultWebSocketServerSession.() -> Unit
typealias ClientWebSocketHandler = suspend DefaultClientWebSocketSession.() -> Unit

suspend fun WebSocketSession.readMessage(): String {
    val text = incoming.receive() as Frame.Text
    return text.readText()
}

suspend fun WebSocketSession.onMessage(handler: suspend (String) -> Unit) {
    incoming.consumeEach {
        handler((it as Frame.Text).readText())
    }
}