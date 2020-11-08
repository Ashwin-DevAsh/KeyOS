package tech.DevAsh.KeyOS.Config.Adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.header_contact_listtile.view.*
import kotlinx.android.synthetic.keyOS.widget_listtile_contact.view.*
import tech.DevAsh.KeyOS.Config.ContactList
import tech.DevAsh.KeyOS.Config.ToggleCallback
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.KeyOS.Helpers.ContactHelper
import tech.DevAsh.keyOS.Database.Contact


class ContactListAdapter(
        var items: ArrayList<Contact>,
        val context: Context,
        val subHeading:String,
        val toggleCallback: ToggleCallback,
        var toggleState:Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var colorMapper = HashMap<String, Int>()


    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType!=0){
            ContactListViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.widget_listtile_contact,
                         parent,
                         false), context)
        }else{
            ContactListHeaderViewHolder(
                LayoutInflater.from(context).inflate(R.layout.header_contact_listtile, parent, false),
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
            holder as ContactListViewHolder

            if(toggleState){
                holder.view.alpha = 1f
            }else{
                holder.view.alpha = 0.25f
            }

            holder.name.text = items[position].name
            holder.number.text = items[position].number
            holder.contact = items[position]
            try {
                holder.badge.text = items[position].name[0].toString()
                holder.badge.setBackgroundColor(colorMapper[items[position].number]!!)
            }catch (e: Throwable){
                colorMapper[items[position].number] =
                    Color.parseColor("#" + ContactHelper.instance.mColors[position % ContactHelper.instance.mColors.size])
                holder.badge
                    .setBackgroundColor(
                        Color.parseColor("#" + ContactHelper.instance.mColors[position % ContactHelper.instance.mColors.size])
                    )
            }
            if(ContactList.isDeleteMode){
                if(ContactList.deleteList.contains(items[position])){
                    holder.checkBox.setChecked(true, false)
                }else{
                    holder.checkBox.setChecked(false, false)
                }
                animateVisible(holder.checkBox, 100)
            }else{
                animateInvisible(holder.checkBox, 50)
            }

            holder.itemView.setOnLongClickListener{
                if(!ContactList.isDeleteMode){
                    ContactList.isDeleteMode = true
                    ContactList.deleteList.add(items[position])
                    ContactList.contactListAdapter?.notifyDataSetChanged()
                    ContactList.deleteView?.call()
                }
                return@setOnLongClickListener true
            }

            holder.view.setOnClickListener{
                if(ContactList.isDeleteMode){
                    if (holder.checkBox.isChecked){
                        ContactList.deleteList.remove(items[position])
                        holder.checkBox.setChecked(false, true)
                    }else{
                        ContactList.deleteList.add(items[position])
                        holder.checkBox.setChecked(true, true)
                    }

                    ContactList.contactListAdapter?.notifyItemChanged(position)
                }
            }
        }else{
            holder as ContactListHeaderViewHolder

        }
    }

    private fun animateVisible(
        viewVisible: View,
        visibleDuration: Long
    ){
        viewVisible.animate()
            .alpha(1f)
            .setDuration(visibleDuration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    viewVisible.visibility = (View.VISIBLE)
                }
            })
    }

    private fun animateInvisible(viewInvisible: View, invisibleDuration: Long){
        viewInvisible.animate()
            .alpha(0f)
            .setDuration(invisibleDuration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    viewInvisible.visibility = (View.INVISIBLE)
                }
            })
    }

    fun updateList(updatedList: ArrayList<Contact>){
        this.items = ArrayList(updatedList)
        notifyDataSetChanged()
    }



}

class ContactListViewHolder(val view: View, context: Context) : RecyclerView.ViewHolder(view) {
    lateinit var color: String
    var contact: Contact?=null
    var badge = view.badge
    var name = view.name
    var number = view.number
    var isSelected = view.select
    var checkBox = view.checkBox
}

class ContactListHeaderViewHolder(
    val view: View,
    val context: Context,
    val adapter: ContactListAdapter
) : RecyclerView.ViewHolder(view) {

    init {
        onClick()
        loadView()
    }

    private fun loadView(){
        view.isTurnOn.text = if (adapter.toggleState) "ON" else "OFF"
        view.turnOn.isChecked = adapter.toggleState
        view.subHeading.text = adapter.subHeading

        if(BuildConfig.IS_PLAYSTORE_BUILD && Build.VERSION.SDK_INT > 25) {
            view.playstoreCover.visibility = View.VISIBLE
        }else{
            view.playstoreCover.visibility = View.GONE
        }
    }

    fun onClick(){
        view.playstoreCover.setOnClickListener {
            AlertHelper.showToast("Not supported in playstore version", context)
        }
        view.turnOn.setOnCheckedChangeListener{ _, isChecked ->
            if(isChecked){
                turnOn()
            }else{
                turnOff()
            }
        }
    }

    private fun turnOn(){
        view.isTurnOn.text = "ON"
        adapter.toggleState = true
        adapter.toggleCallback.turnOn()
        adapter.notifyDataSetChanged()
    }

    private fun turnOff(){
        view.isTurnOn.text = "OFF"
        adapter.toggleState = false
        adapter.toggleCallback.turnOff()
        adapter.notifyDataSetChanged()
    }

}

