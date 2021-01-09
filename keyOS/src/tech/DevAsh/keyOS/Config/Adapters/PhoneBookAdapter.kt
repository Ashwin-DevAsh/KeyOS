package tech.DevAsh.KeyOS.Config.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import kotlinx.android.synthetic.dev.header_app_gridtile.view.*
import kotlinx.android.synthetic.dev.widget_listtile_contact_selector.view.*
import tech.DevAsh.KeyOS.Config.ContactList
import tech.DevAsh.KeyOS.Config.PhoneBook
import tech.DevAsh.KeyOS.Helpers.ContactHelper
import tech.DevAsh.keyOS.Database.Contact


class PhoneBookAdapter(
        var items : ArrayList<Contact>,
        val context: Context,
        val subHeading:String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var colorMapper = HashMap<String,Int>()
    var selectedContact = ArrayList<Contact>()


    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType!=0){
            PhoneBookViewHolder(LayoutInflater.from(context).inflate(R.layout.widget_listtile_contact_selector, parent, false),
                this)
        }else{
            PhoneBookHeaderViewHolder(
                LayoutInflater.from(context).inflate(R.layout.header_app_gridtile, parent, false),
                context,
                this
            )

        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position!=0){
            holder as PhoneBookViewHolder
            holder.name.text = items[position].name
            holder.number.text = items[position].number
            holder.activityInfo = items[position]
            if(ContactList.isBlackList){
                holder.checkBox.checkedColor = Color.RED
            }else{
                holder.checkBox.checkedColor = context.resources.getColor(R.color.green)
            }

            try {
                holder.badge.text = items[position].name[0].toString()
                holder.badge.setBackgroundColor(colorMapper[items[position].number]!!)
            }catch (e:Throwable){
                colorMapper[items[position].number] =
                    Color.parseColor("#"+ ContactHelper.instance.mColors[position % ContactHelper.instance.mColors.size])
                holder.badge.setBackgroundColor( Color.parseColor("#"+ ContactHelper.instance.mColors[position% ContactHelper.instance.mColors.size]))
            }
            if (selectedContact.contains(items[position])){
                holder.checkBox.setChecked(true,false)
            }else{
                holder.checkBox.setChecked(false,false)
            }

        }
    }

    fun updateList(updatedList : ArrayList<Contact>){
        this.items = ArrayList(updatedList)
        notifyDataSetChanged()
    }
}

class PhoneBookViewHolder(view: View, val adapter: PhoneBookAdapter) : RecyclerView.ViewHolder(view) {
    lateinit var color: String
    var activityInfo: Contact?=null
    var badge = view.badge
    var name = view.name
    var number = view.number
    var checkBox = view.checkbox

    init {

        checkBox.setOnClickListener{
            if (checkBox.isChecked){
                checkBox.setChecked(false,true)
                adapter.selectedContact.remove(activityInfo)
            }else{
                checkBox.setChecked(true,true)
                adapter.selectedContact.add(activityInfo!!)
            }
        }

        view.setOnClickListener{
            if (checkBox.isChecked){
                checkBox.setChecked(false,true)
                adapter.selectedContact.remove(activityInfo)
            }else{
                checkBox.setChecked(true,true)
                adapter.selectedContact.add(activityInfo!!)
            }
        }
    }


}

class PhoneBookHeaderViewHolder (
    val view: View,
    val context: Context,
    private val adapter: PhoneBookAdapter
) : RecyclerView.ViewHolder(view) {

    init {
        loadView()
        onClick()
    }

    private fun loadView(){
        view.heading.text=adapter.subHeading
    }

    fun onClick(){
        view.selectAll.setOnCheckedChangeListener{ _, isChecked ->
            if(isChecked){
                selectAll()
            }else{
                clearAll()
            }
        }
    }



    private fun selectAll(){
        adapter.selectedContact = ArrayList(PhoneBook.phoneBookAdapter!!.items)
        adapter.notifyDataSetChanged()
    }

    private fun clearAll(){
        adapter.selectedContact = ArrayList()
        adapter.notifyDataSetChanged()
    }
}