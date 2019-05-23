package hazae41.sockets

import io.ktor.application.install
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.webSocket
import java.util.concurrent.TimeUnit
import io.ktor.client.features.websocket.WebSockets as ClientWebSockets
import io.ktor.websocket.WebSockets as ServerWebSockets

typealias ServerWebSocketHandler = suspend DefaultWebSocketServerSession.() -> Unit

open class Socket(
    val port: Int
) {
    lateinit var engine: ApplicationEngine
    val routes = mutableMapOf<String, ServerWebSocketHandler>()
    val connections = mutableMapOf<String, Connection>()
}

fun Socket.start() {
    engine = embeddedServer(Netty, port){
        install(ServerWebSockets)
        routing{
            routes.forEach{
                (path, block) -> webSocket(path = path, handler = block)
            }
        }
    }.start()
}

fun Socket.stop() = engine.stop(0, 0, TimeUnit.SECONDS)

fun Socket.onConversation(path: String, block: ServerWebSocketHandler)  {
    routes[path] = block
}

fun Socket.connectTo(name: String, connection: Connection){
    connections[name] = connection
}

fun Socket.connectTo(peers: Map<String, Connection>) =
    peers.forEach{ (name, connection) -> connectTo(name, connection) }

fun Socket.connectTo(name: String, host: String, port: Int) =
    Connection(host, port).also{ connectTo(name, it) }
