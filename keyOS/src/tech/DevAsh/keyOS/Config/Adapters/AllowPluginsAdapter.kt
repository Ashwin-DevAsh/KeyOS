package tech.DevAsh.keyOS.Config.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import kotlinx.android.synthetic.dev.header_contact_listtile.view.*
import kotlinx.android.synthetic.dev.widget_listtile_allow_plugin.view.*
import tech.DevAsh.KeyOS.Config.AnimateDeleteToggle
import tech.DevAsh.KeyOS.Config.ToggleCallback
import tech.DevAsh.keyOS.Database.Plugins


class AllowPluginsAdapter(
        var items: ArrayList<Plugins>,
        var allowedItems: ArrayList<Plugins>,
        val context: Activity,
        val subHeading: String,
        val toggleCallback: ToggleCallback,
                         ) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AnimateDeleteToggle {



    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType!=0){
            PluginViewHolder(LayoutInflater.from(context)
                                     .inflate(R.layout.widget_listtile_allow_plugin,
                                              parent,
                                              false))
        }else{
            PluginHeaderViewHolder(
                    LayoutInflater
                            .from(context).inflate(R.layout.header_plugin_listtile, parent, false),
                    context,
                    toggleCallback,
                    subHeading)

        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position!=0){
            holder as PluginViewHolder
            try{
                holder.name.text = context.getText(Plugins.allPluginsMap[items[position].pluginName]!!)
            }catch (e:Throwable){}
            holder.packageName.text = items[position].packageName
            if(toggleCallback.getToggleState()){
                holder.view.alpha = 1f
            }else{
                holder.view.alpha = 0.25f
            }


            val className: String = (try{
                val intent = Intent(items[position].packageName)
                intent.resolveActivity(context.packageManager).className
            }catch (e:Throwable){
                items[position].packageName
            }).toString()

            items[position].className = className

            holder.checkBox.isChecked = allowedItems.contains(items[position])



            holder.view.setOnClickListener{
                if(allowedItems.contains(items[position])){
                    val item = items[position]
                    allowedItems.removeAll(arrayOf(item))
                    holder.checkBox.setChecked(false, true)
                }else{
                    val item = items[position]
                    allowedItems.add(item)
                    holder.checkBox.setChecked(true, true)
                }
            }


        }else{
            holder as PluginHeaderViewHolder

        }
    }
}

class PluginViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var contact: Plugins?=null
    var name = view.name
    var packageName = view.packageName
    var checkBox = view.checkbox
}


class PluginHeaderViewHolder(
        val view: View,
        val context: Activity,
        private val toggleCallback: ToggleCallback,
        val subHeading: String
                            ) : RecyclerView.ViewHolder(view) {

    init {
        onClick()
        loadView()
    }

    private fun loadView(){
        view.isTurnOn.text = if (toggleCallback.getToggleState()) context.getString(
                R.string.on_caps) else context.getString(R.string.off_caps)
        view.turnOn.isChecked = toggleCallback.getToggleState()
        view.subHeading.text = subHeading
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
        view.isTurnOn.text = context.getString(R.string.on_caps)
        toggleCallback.turnOn()
    }

    private fun turnOff(){
        view.isTurnOn.text = context.getString(R.string.off_caps)
        toggleCallback.turnOff()
    }

}
