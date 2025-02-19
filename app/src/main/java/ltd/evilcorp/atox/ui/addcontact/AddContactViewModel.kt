package ltd.evilcorp.atox.ui.addcontact

import androidx.lifecycle.ViewModel
import ltd.evilcorp.atox.feature.ContactManager
import ltd.evilcorp.atox.tox.ToxID
import javax.inject.Inject

class AddContactViewModel @Inject constructor(private val contactManager: ContactManager) : ViewModel() {
    fun addContact(toxId: ToxID, message: String) = contactManager.add(toxId, message)
}
