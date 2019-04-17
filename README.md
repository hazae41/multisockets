# Multisockets

[Go to JitPack](https://jitpack.io/#hazae41/multisockets)

### Usage

```kotlin
// Generate an AES key (you can also load it from a string)
val key = AES.generateKey() // AES.toKey(keyStr)
val socket = Socket("mysocket", 25590, key)

// Create WebSocket route
socket.onConversation("/test/hello"){

    // Read unencrypted message
    val message1 = readMessage()
    if(message1 != null) println(message1)

    // Send unencrypted message
    send("Hello world!") 
    
    // Get crypto functions from the key
    val (encrypt, decrypt) = socket.aes()
    
    // Read encrypted message
    val message2 = readMessage()?.decrypt()
    if(message2 != null) println(message2)
    
    // Send encrypted message
    send("Hello world!".encrypt()) 
}

socket.start()

socket.onConnection{ name ->
    if(name == "mysocket"){
        conversation("/test/hello"){
            send("It works!")
        }
    }
}

socket.connectTo("192.168.1.1:25590")

```