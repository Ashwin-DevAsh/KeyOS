package tech.DevAsh.keyOS.Config.Adapters



import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import kotlinx.android.synthetic.dev.widget_plugin_listtile.view.*
import tech.DevAsh.KeyOS.Config.AnimateDeleteToggle
import tech.DevAsh.keyOS.Database.Plugins


class PluginAdapter(
        var items: ArrayList<Plugins>,

        val context: Activity,
                         ) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AnimateDeleteToggle {



    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SettingsPluginViewHolder(LayoutInflater.from(context)
                                     .inflate(R.layout.widget_plugin_listtile,
                                              parent,
                                              false))

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            holder as SettingsPluginViewHolder
            holder.name.text = items[position].pluginName

            holder.view.setOnClickListener{
                try {
                    val intent = Intent(items[position].packageName)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(intent)
                }catch(e: Exception){ }

            }


    }


    fun updateList(updatedList: ArrayList<Plugins>){
        this.items = ArrayList(updatedList)
        notifyDataSetChanged()
    }

}

class SettingsPluginViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var contact: Plugins?=null
    var name = view.name
}