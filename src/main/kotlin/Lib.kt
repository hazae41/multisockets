package hazae41.sockets

import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.webSocket
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import io.ktor.client.features.websocket.WebSockets as ClientWebSockets
import io.ktor.server.cio.CIO as ServerCIO
import io.ktor.websocket.WebSockets as ServerWebSockets

typealias ServerWebSocketHandler = suspend DefaultWebSocketServerSession.() -> Unit
typealias ClientWebSocketHandler = suspend DefaultClientWebSocketSession.() -> Unit
typealias ConnectionNotifier = suspend Connection.(String) -> Unit

open class Socket(
    val name: String, val port: Int, val key: SecretKey? = null
) {

    lateinit var engine: ApplicationEngine

    val routings = mutableMapOf<String, ServerWebSocketHandler>()

    fun onConversation(path: String, block: ServerWebSocketHandler)  {
        routings[path] = block
    }

    fun stop() = engine.stop(0, 0, TimeUnit.SECONDS)
    fun start() {
        engine = embeddedServer(io.ktor.server.cio.CIO, port){
            install(io.ktor.websocket.WebSockets)
            routing{
                routings.forEach{ (path, block) -> webSocket(path = path, handler = block) }
            }
        }.start()
    }

    val connections = mutableMapOf<String, Connection>()
    val connectionsNotifiers = mutableListOf<ConnectionNotifier>()

    fun onConnection(filter: String? = null, block: ConnectionNotifier){
        connectionsNotifiers += { name ->
            if(filter == null || name == filter) block(name)
        }
    }

    fun connectTo(address: String) = GlobalScope.launch{
        val connection = Connection(address)
        connection.request("/name"){ name ->
            connections[name] = connection
            connectionsNotifiers.forEach{ it(connection, name) }
        }
    }

    fun connectTo(peers: List<String>) {
        peers.forEach{ connectTo(it) }
    }

    init {
        onConversation("/name") { send(name) }
        onConversation("/peers") {
            val peers = connections.values.map { it.address }
            send(peers.joinToString(","))
        }
    }
}

class Connection(val address: String)  {
    fun conversation(path: String, block: ClientWebSocketHandler) = GlobalScope.launch {
        val (host, _port) = address.split(":")
        val port = Integer.parseInt(_port)
        val client = HttpClient(CIO).config { install(ClientWebSockets) }
        client.ws(HttpMethod.Get, host = host, port = port, path = path, block = block)
    }
}

fun Connection.request(
    path: String,
    data: String? = null,
    callback: suspend (String) -> Unit = {}
) = conversation(path){
    if(data != null) send(data)
    callback(readMessage())
}

suspend fun WebSocketSession.readMessage(): String {
    val text = incoming.receive() as Frame.Text
    return text.readText()
}

suspend fun WebSocketSession.onMessage(handler: suspend (String) -> Unit) {
    incoming.consumeEach {
        handler((it as Frame.Text).readText())
    }
}

fun <T, U> Deferred<T>.then(callback: suspend (T) -> U) = let {
    deferred -> GlobalScope.async { callback(deferred.await()) }
}

fun String.aes(): SecretKey {
    if(isBlank()) return AES.generate()
    return AES.toKey(this)
}

fun aes(key: SecretKey?): Pair<String.() -> String, String.() -> String> {
    fun encrypt(message: String) = if(key == null) message else AES.encrypt(message, key)
    fun decrypt(message: String) = if(key == null) message else AES.decrypt(message, key)
    return Pair(::encrypt, ::decrypt)
}

fun Socket.aes() = aes(key)