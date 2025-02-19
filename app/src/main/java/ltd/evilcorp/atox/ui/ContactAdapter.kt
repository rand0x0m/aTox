package ltd.evilcorp.atox.ui

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.contact_list_view_item.view.*
import kotlinx.android.synthetic.main.profile_image_layout.view.*
import ltd.evilcorp.atox.R
import ltd.evilcorp.core.vo.Contact

class ContactAdapter(
    private val inflater: LayoutInflater,
    private val resources: Resources
) : BaseAdapter() {
    var contacts: List<Contact> = ArrayList()

    override fun getCount(): Int = contacts.size
    override fun getItem(position: Int): Any = contacts[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val vh: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.contact_list_view_item, parent, false)
            vh = ViewHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }

        val contact = contacts[position]
        vh.publicKey.text = contact.publicKey
        vh.name.text = contact.name
        vh.lastMessage.text = contact.lastMessage
        vh.status.setColorFilter(colorByStatus(resources, contact))
        setAvatarFromContact(vh.image, contact)

        return view
    }

    private class ViewHolder(row: View) {
        val name: TextView = row.name
        val publicKey: TextView = row.publicKey
        val lastMessage: TextView = row.lastMessage
        val status: ImageView = row.statusIndicator
        val image: ImageView = row.profileImage
    }
}
