package ltd.evilcorp.atox.ui.chat

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlinx.android.synthetic.main.chat_fragment.view.*
import kotlinx.android.synthetic.main.profile_image_layout.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.atox.requireStringArg
import ltd.evilcorp.atox.tox.MAX_MESSAGE_LENGTH
import ltd.evilcorp.atox.tox.PublicKey
import ltd.evilcorp.atox.ui.MessagesAdapter
import ltd.evilcorp.atox.ui.colorByStatus
import ltd.evilcorp.atox.ui.setAvatarFromContact
import ltd.evilcorp.atox.vmFactory
import ltd.evilcorp.core.vo.ConnectionStatus
import ltd.evilcorp.core.vo.Message
import java.util.*

const val CONTACT_PUBLIC_KEY = "publicKey"

class ChatFragment : Fragment() {
    private val viewModel: ChatViewModel by viewModels { vmFactory }

    private lateinit var contactPubKey: String
    private var contactName = ""
    private var contactOnline = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.chat_fragment, container, false).apply {
        contactPubKey = requireStringArg(CONTACT_PUBLIC_KEY)
        viewModel.setActiveChat(PublicKey(contactPubKey))

        toolbar.setNavigationIcon(R.drawable.back)
        toolbar.setNavigationOnClickListener {
            viewModel.setActiveChat(PublicKey(""))
            activity?.onBackPressed()
        }

        toolbar.inflateMenu(R.menu.chat_options_menu)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.clear_history -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.clear_history)
                        .setMessage(getString(R.string.clear_history_confirm, contactName))
                        .setPositiveButton(R.string.clear_history) { _, _ ->
                            Toast.makeText(requireContext(), R.string.clear_history_cleared, Toast.LENGTH_LONG).show()
                            viewModel.clearHistory()
                        }
                        .setNegativeButton(R.string.cancel, null).show()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }

        viewModel.contact.observe(viewLifecycleOwner, Observer {
            contactName = it.name
            contactOnline = it.connectionStatus != ConnectionStatus.None

            title.text = contactName
            subtitle.text = if (it.typing) {
                getString(R.string.contact_typing)
            } else {
                // TODO(robinlinden): Replace with last seen.
                it.lastMessage
            }.toLowerCase(Locale.getDefault())
            statusIndicator.setColorFilter(colorByStatus(resources, it))
            setAvatarFromContact(profileImage, it)

            updateSendButton(this)
        })

        val adapter = MessagesAdapter(inflater, resources)
        messages.adapter = adapter
        registerForContextMenu(messages)
        viewModel.messages.observe(viewLifecycleOwner, Observer {
            adapter.messages = it
            adapter.notifyDataSetChanged()
        })

        call.setOnClickListener {
            viewModel.startCall()
        }

        send.setOnClickListener {
            val message = outgoingMessage.text.toString()
            outgoingMessage.text.clear()
            viewModel.sendMessage(message)
        }

        send.isEnabled = false

        outgoingMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                outgoingMessage.error = if (outgoingMessage.text.length > MAX_MESSAGE_LENGTH) {
                    getText(R.string.error_message_length_exceeded)
                } else {
                    null
                }
                updateSendButton(this@apply)
            }
        })
    }

    override fun onPause() {
        viewModel.setActiveChat(PublicKey(""))
        super.onPause()
    }

    override fun onResume() {
        viewModel.setActiveChat(PublicKey(contactPubKey))
        super.onResume()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        when (v.id) {
            R.id.messages -> {
                requireActivity().menuInflater.inflate(R.menu.chat_message_context_menu, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        return when (item.itemId) {
            R.id.copy -> {
                val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val message = messages.adapter.getItem(info.position) as Message
                clipboard.setPrimaryClip(ClipData.newPlainText(getText(R.string.message), message.message))

                Toast.makeText(requireContext(), getText(R.string.copied), Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun updateSendButton(layout: View) {
        layout.send.isEnabled = layout.outgoingMessage.text.isNotEmpty() && layout.outgoingMessage.error.isNullOrEmpty() && contactOnline
    }
}
