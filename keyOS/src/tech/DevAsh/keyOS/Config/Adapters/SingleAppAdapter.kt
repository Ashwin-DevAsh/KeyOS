package tech.DevAsh.keyOS.Config.Adapters



import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.header_contact_listtile.view.*
import kotlinx.android.synthetic.keyOS.widget_listtile_apps.view.*
import net.igenius.customcheckbox.CustomCheckBox
import tech.DevAsh.KeyOS.Config.Adapters.AllowItemAdapter
import tech.DevAsh.KeyOS.Config.EditApp
import tech.DevAsh.KeyOS.Config.ToggleCallback
import tech.DevAsh.KeyOS.Helpers.HelperVariables
import tech.DevAsh.keyOS.Database.Apps


class SingleAppAdapter(
        override var items: ArrayList<Apps>,
        var singleApp:Apps?,
        override val context: AppCompatActivity,
        override var subHeading:String,
        val toggleCallback: ToggleCallback,
        var toggleState:Boolean
                        ) : AllowItemAdapter(items,ArrayList(),"","",context) {

    init {
        _items = ArrayList(items)
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType!=0){
            SingleAppViewHolder(LayoutInflater.from(context).inflate(
                    R.layout.widget_listtile_apps, parent, false),
                                this
                               )
        }else{
            SingleAppHeader(
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
        if (position != 0) {
            holder as SingleAppViewHolder
            holder.item = items[position]
            holder.packageName.text = items[position].packageName
            holder.name.text = items[position].appName
            holder.icon.setImageDrawable(items[position].icon)

            if(toggleState){
                holder.view.alpha=1f
            }else{
                holder.view.alpha=0.25f
            }

            if(singleApp == holder.item){
                holder.checkBox.setChecked(true, false)
            }else{
                holder.checkBox.setChecked(false, false)
            }
        }
    }




}

class SingleAppViewHolder(
        val view: View,
        adapter: SingleAppAdapter
                    ) : RecyclerView.ViewHolder(
        view) {
    val name = view.findViewById(R.id.name) as TextView
    val icon = view.findViewById(R.id.icon) as ImageView
    val checkBox =  view.checkbox as CustomCheckBox
    val packageName = view.packageName as TextView
    private val editApp = view.editApp as ImageView
    var item: Apps?=null

    init {



        view.editApp.visibility = View.INVISIBLE

        view.setOnClickListener{
            if(!checkBox.isChecked){
                checkBox.setChecked(true, true)
                adapter.singleApp = item
                adapter.notifyDataSetChanged()

            }else{
                checkBox.setChecked(false, true)
                adapter.singleApp=null
                adapter.notifyDataSetChanged()

            }
        }

        editApp.setOnClickListener {
            HelperVariables.selectedEditedApp = item
            adapter.context.startActivity(Intent(adapter.context, EditApp::class.java))
        }

    }
}


class SingleAppHeader(
        val view: View,
        val context: Context,
        val adapter: SingleAppAdapter) : RecyclerView.ViewHolder(view) {

    init {
        onClick()
        loadView()
    }

    private fun loadView(){
        view.isTurnOn.text = if (adapter.toggleState) "ON" else "OFF"
        view.turnOn.isChecked = adapter.toggleState
        view.subHeading.text = adapter.subHeading
        view.subHeading.textSize=17f
    }

    fun onClick(){
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
        adapter.toggleState=true
        adapter.notifyDataSetChanged()
        adapter.toggleCallback.turnOn()
    }

    private fun turnOff(){
        view.isTurnOn.text = "OFF"
        adapter.toggleState=false
        adapter.notifyDataSetChanged()
        adapter.toggleCallback.turnOff()
    }

}

