package hazae41.sockets

import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object B64 {
    fun to(data: ByteArray) = Base64.getEncoder().encodeToString(data)
    fun from(data: String) = Base64.getDecoder().decode(data)
}

object AES {
    fun generate() = KeyGenerator.getInstance("AES").run {
        init(128)
        generateKey()
    }

    fun encrypt(data: String, key: SecretKey) =
        Cipher.getInstance("AES").run {
            init(Cipher.ENCRYPT_MODE, key)
            B64.to(doFinal(data.toByteArray()))
        }

    fun decrypt(data: String, key: SecretKey) =
        Cipher.getInstance("AES").run {
            init(Cipher.DECRYPT_MODE, key)
            String(doFinal(B64.from(data)))
        }

    fun toKey(key: String) = B64.from(key).run {
        SecretKeySpec(this, 0, size, "AES")
    }

    fun toString(key: SecretKey) = B64.to(key.encoded)
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