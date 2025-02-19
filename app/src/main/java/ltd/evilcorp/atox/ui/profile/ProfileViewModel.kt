package ltd.evilcorp.atox.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.feature.UserManager
import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.atox.tox.Tox
import ltd.evilcorp.atox.tox.ToxStarter
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val context: Context,
    private val userManager: UserManager,
    private val tox: Tox,
    private val toxStarter: ToxStarter
) : ViewModel() {
    val publicKey: PublicKey by lazy { tox.publicKey }

    fun startTox(save: ByteArray? = null): Boolean = toxStarter.startTox(save)

    fun tryImportToxSave(uri: Uri): ByteArray? =
        context.contentResolver.openInputStream(uri)?.readBytes()

    fun createUser(publicKey: PublicKey, name: String, password: String) =
        userManager.create(publicKey, name, password)

    fun verifyUserExists(publicKey: PublicKey) = userManager.verifyExists(publicKey)
}
