# Multisockets

[Go to JitPack](https://jitpack.io/#hazae41/multisockets)

Compatible with wss protocol only

Messages are encrypted with SSL

Encryption prevent man-in-the-middle attacks but do not prevent a third party from connecting to you

**You must add password protection if you want to restrict who can connect to you**

### Usage

```kotlin
val socket = Socket("mysocket", 25590)

// Create WebSocket route
socket.onConversation("/test/hello"){

    // Read message
    val message1 = readMessage()
    if(message1 != null) println(message1)

    send("Hello world!") 
}

socket.start()

val test = Connection("192.168.1.1", 25590)

test.conversation("/test/hello"){
    send("It works!")
}


```
