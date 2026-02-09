package com.titushm.udplug

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

@TaskerInputRoot
class UdpRequestInput(
    @field:TaskerInputField("ipAddress") var ipAddress: String? = null,
    @field:TaskerInputField("port") var port: Int? = null,
    @field:TaskerInputField("payload") var payload: String? = null,
    @field:TaskerInputField("isHexPayload") var isHexPayload: Boolean? = false,
    @field:TaskerInputField("useHexResponse") var useHexResponse: Boolean? = false,
    @field:TaskerInputField("waitForResponse") var waitForResponse: Boolean? = false,
    @field:TaskerInputField("timeout") var timeout: Int? = 1000,
    @field:TaskerInputField("maxBufferSize") var maxBufferSize: Int? = 1024,
)

@TaskerOutputObject
class UdpRequestOutput(
    @get:TaskerOutputVariable("response") var response: String? = null
)

class UdpRequestHelper(config: TaskerPluginConfig<UdpRequestInput>) :
    TaskerPluginConfigHelper<UdpRequestInput, UdpRequestOutput, UdpRequestActionRunner>(config) {
    override val runnerClass = UdpRequestActionRunner::class.java
    override val inputClass = UdpRequestInput::class.java
    override val outputClass = UdpRequestOutput::class.java
    override fun addToStringBlurb(input: TaskerInput<UdpRequestInput>, blurbBuilder: StringBuilder) {
        blurbBuilder.append("\nSending UDP packet to ${input.regular.ipAddress}:${input.regular.port}")
        if(input.regular.waitForResponse == true) {
            blurbBuilder.append(" and waiting for response")
        }
    }
}

class ActivityConfigUdpRequestAction : Activity(), TaskerPluginConfig<UdpRequestInput> {
    override val context: Context get() = applicationContext
    private val taskerHelper by lazy { UdpRequestHelper(this) }
    private var taskerInput: TaskerInput<UdpRequestInput>? = null

    override fun assignFromInput(input: TaskerInput<UdpRequestInput>) {
        taskerInput = input
    }

    override val inputForTasker: TaskerInput<UdpRequestInput>
        get() = taskerInput ?: TaskerInput(UdpRequestInput())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        taskerHelper.onCreate()

        val intent = Intent(this, MainActivity::class.java).apply {
            taskerInput?.regular?.let {
                putExtra(MainActivity.EXTRA_IP_ADDRESS, it.ipAddress)
                putExtra(MainActivity.EXTRA_PORT, it.port.toString())
                putExtra(MainActivity.EXTRA_PAYLOAD, it.payload)
                putExtra(MainActivity.EXTRA_IS_HEX_PAYLOAD, it.isHexPayload)
                putExtra(MainActivity.EXTRA_USE_HEX_RESPONSE, it.useHexResponse)
                putExtra(MainActivity.EXTRA_WAIT_FOR_RESPONSE, it.waitForResponse)
                putExtra(MainActivity.EXTRA_TIMEOUT, it.timeout.toString())
                putExtra(MainActivity.EXTRA_MAX_BUFFER_SIZE, it.maxBufferSize.toString())
            }
        }
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val input = UdpRequestInput(
                ipAddress = data.getStringExtra(MainActivity.EXTRA_IP_ADDRESS),
                port = data.getStringExtra(MainActivity.EXTRA_PORT)?.toIntOrNull(),
                payload = data.getStringExtra(MainActivity.EXTRA_PAYLOAD),
                isHexPayload = data.getBooleanExtra(MainActivity.EXTRA_IS_HEX_PAYLOAD, false),
                useHexResponse = data.getBooleanExtra(MainActivity.EXTRA_USE_HEX_RESPONSE, false),
                waitForResponse = data.getBooleanExtra(MainActivity.EXTRA_WAIT_FOR_RESPONSE, false),
                timeout = data.getStringExtra(MainActivity.EXTRA_TIMEOUT)?.toIntOrNull(),
                maxBufferSize = data.getStringExtra(MainActivity.EXTRA_MAX_BUFFER_SIZE)?.toIntOrNull(),
            )
            taskerInput = TaskerInput(input)
            taskerHelper.finishForTasker()
        } else {
            finish()
        }
    }
}

fun hexStringToByteArray(s: String): ByteArray {
    val len = s.length
    val data = ByteArray(len / 2)
    for (i in 0 until len step 2) {
        data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
    }
    return data
}
fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }


class UdpRequestActionRunner : TaskerPluginRunnerAction<UdpRequestInput, UdpRequestOutput>() {
    override fun run(context: Context, input: TaskerInput<UdpRequestInput>): TaskerPluginResult<UdpRequestOutput> {
        val ipAddress = input.regular.ipAddress ?: throw IllegalArgumentException("IP Address not set")
        val port = input.regular.port ?: throw IllegalArgumentException("Port not set")
        val payload = input.regular.payload ?: ""
        val isHexPayload = input.regular.isHexPayload == true
        val useHexResponse = input.regular.useHexResponse == true
        val waitForResponse = input.regular.waitForResponse == true
        val timeout = input.regular.timeout ?: 1000
        val maxBufferSize = input.regular.maxBufferSize ?: 1024

        try {
            DatagramSocket().use { socket ->
                val address = InetAddress.getByName(ipAddress)
                val data = if(isHexPayload) hexStringToByteArray(payload) else payload.toByteArray()

                val packet = DatagramPacket(data, data.size, address, port)
                socket.send(packet)

                if (waitForResponse) {
                    socket.soTimeout = timeout
                    val buffer = ByteArray(maxBufferSize)
                    val receivePacket = DatagramPacket(buffer, buffer.size)
                    socket.receive(receivePacket)

                    val responseBytes = receivePacket.data.copyOf(receivePacket.length)
                    val responseData = if(useHexResponse) responseBytes.toHexString() else String(responseBytes)
                    return TaskerPluginResultSucess(UdpRequestOutput(responseData))
                } else {
                    return TaskerPluginResultSucess(UdpRequestOutput())
                }
            }
        } catch (e: SocketTimeoutException) {
            throw Exception("Timed out waiting for response", e)
        }
        catch (e: IOException) {
            throw Exception("Error sending or receiving UDP packet", e)
        }
    }
}
