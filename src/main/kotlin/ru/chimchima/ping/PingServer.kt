package ru.chimchima.ping

import java.net.ServerSocket
import java.util.concurrent.Executors

class PingServer(private val port: Int) {
    private val serverSocketService = Executors.newSingleThreadExecutor()

    fun start() {
        serverSocketService.submit {
            val serverSocket = ServerSocket(port)
            while (true) {
                val socket = serverSocket.accept()
                val output = socket.getOutputStream()
                output.write("HTTP/1.1 200 OK\r\nContent-Length: 7\r\n\r\nping ok".toByteArray())
                output.close()
                socket.close()
            }
        }
    }
}
