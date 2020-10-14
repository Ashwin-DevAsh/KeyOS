package tech.DevAsh.KeyOS.Config.Adapters

import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.header_app_gridtile.view.*
import kotlinx.android.synthetic.keyOS.widget_listtile_apps.view.*
import tech.DevAsh.KeyOS.Config.EditApp

import net.igenius.customcheckbox.CustomCheckBox
import tech.DevAsh.KeyOS.Helpers.HelperVariables
import tech.DevAsh.keyOS.Database.Apps
import kotlin.collections.ArrayList

class AllowItemAdapter(
        val _items: MutableList<Apps>,
        _allowedItems: MutableList<Apps>,
        var heading:String,
        var subHeading:String,
        val context: AppCompatActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var allowedItems = ArrayList(_allowedItems)
    var items = ArrayList(_items)
    override fun getItemCount(): Int {
        return items.size
    }


    override fun getItemViewType(position: Int): Int {
        if(position==0){
            return 0
        }
        return super.getItemViewType(position)+1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType!=0){
            ItemViewHolder(LayoutInflater.from(context).inflate(
                R.layout.widget_listtile_apps, parent, false),
                allowedItems,
                this
            )
        }else{
            ItemHeaderViewHolder(LayoutInflater.from(context).inflate(
                    R.layout.header_app_gridtile, parent, false), context, this
            )
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position != 0) {
            holder as ItemViewHolder
            holder.item = items[position]
            holder.packageName.text = items[position].packageName
            holder.name.text = items[position].appName
            holder.icon.setImageDrawable(items[position].icon)
            if(allowedItems.contains(items[position])){
                holder.checkBox.setChecked(true, false)
            }else{
                holder.checkBox.setChecked(false, false)
            }
        }

    }
}

class ItemViewHolder(
    view: View,
    allowedItem: ArrayList<Apps>,
    adapter: AllowItemAdapter
) : RecyclerView.ViewHolder(
    view) {
    val name = view.findViewById(R.id.name) as TextView
    val icon = view.findViewById(R.id.icon) as ImageView
    val checkBox =  view.checkbox as CustomCheckBox
    val packageName = view.packageName as TextView
    private val editApp = view.editApp as ImageView
    var item: Apps?=null

    init {
        checkBox.setOnClickListener{
            if(!checkBox.isChecked){
                checkBox.setChecked(true, true)
                allowedItem.add(item!!)
            }else{
                checkBox.setChecked(false, true)
                allowedItem.remove(item!!)
            }
        }

        if(adapter.heading.endsWith("Services")){
              editApp.visibility=View.GONE
        }

        editApp.setOnClickListener {
            HelperVariables.selectedEditedApp = item
            adapter.context.startActivity(Intent(adapter.context,EditApp::class.java))
        }

        view.setOnClickListener{
            if(!checkBox.isChecked){
                checkBox.setChecked(true, true)
                allowedItem.add(item!!)
            }else{
                checkBox.setChecked(false, true)
                allowedItem.remove(item!!)
            }
        }
    }
}

class ItemHeaderViewHolder (
    val view: View,
    val context: Context,
    private val adapter: AllowItemAdapter
) : RecyclerView.ViewHolder(view) {
    private var heading: TextView = view.heading
    private var subHeading:TextView = view.subHeading

    init {
        heading.text = adapter.heading
        subHeading.text = adapter.subHeading
        onClick()
    }

    private fun onClick(){
        view.selectAll.setOnCheckedChangeListener{ _, isChecked ->
            if(isChecked){
                selectAll()
            }else{
                clearAll()
            }
        }
    }

    private fun selectAll(){
        adapter.allowedItems = ArrayList(adapter.items.subList(1, adapter.items.size))
        adapter.notifyDataSetChanged()
    }

    private fun clearAll(){
        adapter.allowedItems =ArrayList()
        adapter.notifyDataSetChanged()
    }

}
