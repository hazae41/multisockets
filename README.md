# Multisockets

[Go to JitPack](https://jitpack.io/#hazae41/multisockets)

Compatible with ws protocol but not wss

Messages are encrypted with AES and keys are sent with RSA

Encryption prevent man-in-the-middle attacks but do not prevent a third party from connecting to you

**You must add password protection if you want to restrict who can connect to you**

### Usage

```kotlin
val socket = Socket("mysocket", 25590)

// Create WebSocket route
socket.onConversation("/test/hello"){

    // Get crypto functions
    val (encrypt, decrypt) = aes()

    // Read unencrypted message
    val message1 = readMessage()
    if(message1 != null) println(message1)

    // Send unencrypted message
    send("Hello world!") 
    
    // Read encrypted message
    val message2 = readMessage()?.decrypt()
    if(message2 != null) println(message2)
    
    // Send encrypted message
    send("Hello world!".encrypt()) 
}

socket.start()

val test = Connection("192.168.1.1", 25590)

test.conversation("/test/hello"){
    send("It works!")
}


```