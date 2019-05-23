package hazae41.sockets

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.wss
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

typealias ClientWebSocketHandler = suspend DefaultClientWebSocketSession.() -> Unit

data class Connection(val host: String, val port: Int)

fun UntrustManager() = object : X509TrustManager {
    override fun getAcceptedIssuers() = null
    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
}

fun Connection.conversation(path: String, block: ClientWebSocketHandler) = GlobalScope.launch {
    HttpClient(CIO){ engine { https { trustManager = UntrustManager() } } }
    .config { install(WebSockets) }
    .wss(HttpMethod.Get, host, port, path, block = block)
}

fun Connection.request(
    path: String,
    data: String? = null,
    callback: suspend (String) -> Unit = {}
) = conversation(path){
    if(data != null) send(data)
    callback(readMessage())
}