package ru.chimchima.help

import ru.chimchima.USAGE
import java.net.ServerSocket
import java.util.concurrent.Executors

class HelpServer(private val port: Int) {
    private val serverSocketService = Executors.newSingleThreadExecutor()

    fun start() {
        serverSocketService.submit {
            val serverSocket = ServerSocket(port)
            while (true) {
                val socket = serverSocket.accept()
                val output = socket.getOutputStream()
                // voprosy?
                output.write("HTTP/1.1 200 OK\r\nContent-Length: ${USAGE.toByteArray().size}\r\nContent-Type: text/plain;charset=UTF-8\r\n\r\n$USAGE".toByteArray())
                output.close()
                socket.close()
            }
        }
    }
}
