package ltd.evilcorp.atox.tox

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat.startForegroundService
import im.tox.tox4j.core.exceptions.ToxNewException
import ltd.evilcorp.atox.ToxService
import ltd.evilcorp.atox.feature.UserManager
import javax.inject.Inject

private const val TAG = "ToxStarter"

class ToxStarter @Inject constructor(
    private val saveManager: SaveManager,
    private val userManager: UserManager,
    private val tox: Tox,
    private val eventListener: ToxEventListener,
    private val context: Context
) {
    fun startTox(save: ByteArray? = null): Boolean = try {
        tox.start(SaveOptions(save), eventListener)
        startService()
        true
    } catch (e: ToxNewException) {
        Log.e(TAG, e.message)
        false
    }

    fun tryLoadTox(): Boolean {
        tryLoadSave()?.also { save ->
            startTox(save)
            userManager.verifyExists(tox.publicKey)
            return true
        }
        return false
    }

    private fun startService() = context.run {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            startService(Intent(this, ToxService::class.java))
        } else {
            startForegroundService(Intent(this, ToxService::class.java))
        }
    }

    private fun tryLoadSave(): ByteArray? =
        saveManager.run { list().firstOrNull()?.let { load(PublicKey(it)) } }
}
