package ltd.evilcorp.atox.tox

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import im.tox.tox4j.av.enums.ToxavFriendCallState
import im.tox.tox4j.core.enums.ToxFileControl
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.impl.jni.ToxAvImpl
import im.tox.tox4j.impl.jni.ToxCoreImpl
import java.util.*

private const val TAG = "ToxWrapper"

class ToxWrapper(
    private val eventListener: ToxEventListener,
    private val saveManager: SaveManager,
    options: SaveOptions
) {
    private val tox: ToxCoreImpl = ToxCoreImpl(options.toToxOptions())
    private val toxAv: ToxAvImpl = ToxAvImpl(tox)
    init {
        updateContactMapping()
    }

    private fun updateContactMapping() {
        eventListener.contactMapping = getContacts()
    }

    fun bootstrap(address: String, port: Int, publicKey: ByteArray) {
        tox.bootstrap(address, port, publicKey)
        tox.addTcpRelay(address, port, publicKey)
    }

    fun stop() = tox.close()

    fun iterate(): Unit = tox.iterate(eventListener, Unit)
    fun iterationInterval(): Long = tox.iterationInterval().toLong()

    fun getName(): String = String(tox.name)
    fun setName(name: String) {
        tox.name = name.toByteArray()
    }

    fun getStatusMessage(): String = String(tox.statusMessage)
    fun setStatusMessage(statusMessage: String) {
        tox.statusMessage = statusMessage.toByteArray()
    }

    fun getToxId() = ToxID.fromBytes(tox.address)
    fun getPublicKey() = PublicKey.fromBytes(tox.publicKey)

    fun getSaveData() = tox.savedata

    fun addContact(toxId: ToxID, message: String) {
        tox.addFriend(toxId.bytes(), message.toByteArray())
        updateContactMapping()
    }

    fun deleteContact(publicKey: PublicKey) {
        Log.e(TAG, "Deleting $publicKey")
        tox.friendList.find { PublicKey.fromBytes(tox.getFriendPublicKey(it)) == publicKey }?.let { friend ->
            tox.deleteFriend(friend)
        } ?: Log.e(
            TAG, "Tried to delete nonexistent contact, this can happen if the database is out of sync with the Tox save"
        )

        updateContactMapping()
    }

    fun getContacts(): List<Pair<PublicKey, Int>> {
        val friendNumbers = tox.friendList
        Log.i(TAG, "Loading ${friendNumbers.size} friends")
        return List(friendNumbers.size) {
            Log.i(TAG, "${friendNumbers[it]}: ${tox.getFriendPublicKey(friendNumbers[it]).bytesToHex()}")
            Pair(PublicKey.fromBytes(tox.getFriendPublicKey(friendNumbers[it])), friendNumbers[it])
        }
    }

    fun sendMessage(publicKey: PublicKey, message: String): Int = tox.friendSendMessage(
        contactByKey(publicKey),
        ToxMessageType.NORMAL,
        0,
        message.toByteArray()
    )

    fun save() = saveManager.save(getPublicKey(), tox.savedata)

    fun acceptFriendRequest(publicKey: PublicKey) {
        tox.addFriendNorequest(publicKey.bytes())
        updateContactMapping()
    }

    fun startFileTransfer(publicKey: PublicKey, fileNumber: Int) =
        tox.fileControl(contactByKey(publicKey), fileNumber, ToxFileControl.RESUME)

    fun stopFileTransfer(publicKey: PublicKey, fileNumber: Int) =
        tox.fileControl(contactByKey(publicKey), fileNumber, ToxFileControl.CANCEL)

    fun startAudioCall(publicKey: PublicKey) {
        Thread(Runnable {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)

            toxAv.call(contactByKey(publicKey), 64, 0)

            val arraySize = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val outputArray = ShortArray(arraySize)
            //var inputArray = ShortArray(arraySize)

            val audioRecorder = AudioRecord(
                MediaRecorder.AudioSource.MIC, 44100,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, outputArray.size
            )

            audioRecorder.startRecording()

            while (audioRecorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                //toxAv.invokeAudioReceiveFrame(contactByKey(publicKey), inputArray, AudioFormat.CHANNEL_IN_MONO,44100)
                audioRecorder.read(outputArray, 0, outputArray.size)
                toxAv.audioSendFrame(
                    contactByKey(publicKey),
                    outputArray,
                    outputArray.size,
                    audioRecorder.channelCount,
                    audioRecorder.sampleRate
                )
            }
        }).run()
    }

    fun stopCall(publicKey: PublicKey) {
        toxAv.invokeCallState(contactByKey(publicKey), EnumSet.of(ToxavFriendCallState.FINISHED))
        //TODO: Stop recording, probably a new AV class is needed.
    }

    private fun contactByKey(publicKey: PublicKey): Int = tox.friendByPublicKey(publicKey.bytes())
}
