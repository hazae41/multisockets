package hazae41.sockets

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.readText
import io.ktor.http.cio.websocket.send
import kotlinx.coroutines.channels.consumeEach

suspend fun WebSocketSession.readMessage(): String {
    val text = incoming.receive() as Frame.Text
    return text.readText()
}

suspend fun WebSocketSession.onMessage(handler: suspend (String) -> Unit) {
    incoming.consumeEach {
        handler((it as Frame.Text).readText())
    }
}

suspend fun WebSocketSession.aes(): Pair<String.() -> String, String.() -> String> {
    val selfAES = AES.generate()
    val selfRSA = RSA.generate()

    send(RSA.save(selfRSA.public))
    val remoteRSA = RSA.PublicKey(readMessage())

    send(RSA.encrypt(AES.toString(selfAES), remoteRSA))
    val remoteAES = AES.toKey(RSA.decrypt(readMessage(), selfRSA.private))

    fun encrypt(message: String) = AES.encrypt(message, selfAES)
    fun decrypt(message: String) = AES.decrypt(message, remoteAES)
    return Pair(::encrypt, ::decrypt)
}