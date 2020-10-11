package tech.DevAsh.KeyOS.Config.Adapters

import androidx.appcompat.app.AppCompatActivity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.header_app_gridtile.view.*
import kotlinx.android.synthetic.keyOS.widget_listtile_contact_selector.view.*
import tech.DevAsh.KeyOS.Helpers.ContactHelper


class ActivitiesListAdapter(
    var _items: MutableList<ActivityInfo?>,
    _blockedItems: MutableList<String>,
    var heading:String,
    var subHeading:String,
    val context: AppCompatActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var blockedItems = ArrayList(_blockedItems)
    var items = ArrayList(_items)
    private var colorMapper = HashMap<String,Int>()

    init {
        items.add(0,ActivityInfo())
    }

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
            ActivityListViewHolder(LayoutInflater.from(context).inflate(
                    R.layout.widget_listtile_contact_selector, parent, false),
                                   this
            )
        }else{
            ActivityListHeaderViewHolder(LayoutInflater.from(context).inflate(
                R.layout.header_app_gridtile, parent, false),this
            )
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position!=0){
            val fullName = items[position]!!.name
            val name = fullName.split(".")[fullName.split(".").size-1]
            holder as ActivityListViewHolder
            holder.name.text = name
            holder.number.text = fullName
            holder.activityInfo = items[position]


            holder.checkBox.isChecked = blockedItems.contains(items[position]!!.name)


            try {
                holder.badge.text = name[0].toString()
                holder.badge.setBackgroundColor(colorMapper[items[position]?.name]!!)
            }catch (e:Throwable){
                colorMapper[items[position]!!.name] =
                    Color.parseColor("#"+ ContactHelper.instance.mColors[position % ContactHelper.instance.mColors.size])
                holder.badge.setBackgroundColor( Color.parseColor("#"+ ContactHelper.instance.mColors[position% ContactHelper.instance.mColors.size]))
            }
        }


    }
}


class ActivityListViewHolder(val view: View, val adapter: ActivitiesListAdapter) : RecyclerView.ViewHolder(view) {
    lateinit var color: String
    var activityInfo: ActivityInfo?=null
    var badge = view.badge
    var name = view.name
    var number = view.number
    var checkBox = view.checkbox

    init {
        checkBox.checkedColor = Color.RED
        onClick()
    }


    fun onClick(){
        checkBox.setOnClickListener{
            if (checkBox.isChecked){
                adapter.blockedItems.remove(activityInfo?.name)
                checkBox.setChecked(false,true)
            }else{
                adapter.blockedItems.add(activityInfo?.name)
                checkBox.setChecked(true,true)
            }
            println( adapter.blockedItems)

        }

        view.setOnClickListener{
            if (checkBox.isChecked){
                adapter.blockedItems.remove(activityInfo?.name)
                checkBox.setChecked(false,true)
            }else{
                adapter.blockedItems.add(activityInfo?.name)
                checkBox.setChecked(true,true)
            }
            println( adapter.blockedItems)
        }
    }


}


class ActivityListHeaderViewHolder (
    val view: View,
    private val adapter: ActivitiesListAdapter
) : RecyclerView.ViewHolder(view) {

    init {
        loadView()
        onClick()
    }

    private fun loadView(){
        view.subHeading.text=adapter.subHeading
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
        adapter.blockedItems = ArrayList()
        for (i in adapter.items){
            if(i!!.name!==null) adapter.blockedItems.add(i!!.name)
        }
        adapter.notifyDataSetChanged()
    }

    private fun clearAll(){
        adapter.blockedItems.clear()
        adapter.notifyDataSetChanged()
    }

}
