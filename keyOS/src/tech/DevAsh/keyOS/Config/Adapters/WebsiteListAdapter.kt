package tech.DevAsh.KeyOS.Config.Adapters

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.dev.widget_listtile_website.view.*
import tech.DevAsh.KeyOS.Config.AnimateDeleteToggle
import tech.DevAsh.KeyOS.Config.ToggleCallback
import tech.DevAsh.KeyOS.Helpers.ContactHelper
import tech.DevAsh.keyOS.Config.WebsiteList
import tech.DevAsh.keyOS.Database.Contact


class WebsiteListAdapter(
        var items: ArrayList<String>,
        val context: Activity,
        val subHeading:String,
        val toggleCallback: ToggleCallback,
                 ) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),AnimateDeleteToggle {

    private var colorMapper = HashMap<String, Int>()


    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType!=0){
            WebsiteHolder(LayoutInflater.from(context).inflate(R.layout.widget_listtile_website,parent, false),context)
        }else{
            ContactListHeaderViewHolder(
                    LayoutInflater.from(context).inflate(R.layout.header_contact_listtile, parent, false),
                    context,
                    toggleCallback,
                    subHeading)

        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    companion object{
        var failedItems = arrayListOf<String>()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position!=0){
            holder as WebsiteHolder

            if(toggleCallback.getToggleState()){
                holder.view.alpha = 1f
            }else{
                holder.view.alpha = 0.25f
            }

            if(!failedItems.contains(items[position])) Handler().post{
                Glide
                        .with(context)
                        .load("https://www.${items[position]}/favicon.ico")
                        .apply( RequestOptions()
                                .fitCenter()
                                .format(DecodeFormat.PREFER_ARGB_8888)
                                .override(Target.SIZE_ORIGINAL))
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?,
                                                      target: Target<Drawable>?,
                                                      isFirstResource: Boolean): Boolean {
                                holder.profile.visibility = GONE
                                holder.itemView.badgeContainer.visibility = VISIBLE
                                e?.printStackTrace()
                                failedItems.add(items[position])
                                return isFirstResource
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?,
                                                         target: Target<Drawable>?,
                                                         dataSource: DataSource?,
                                                         isFirstResource: Boolean): Boolean {
                                holder.profile.setImageDrawable(resource)
                                holder.profile.visibility = VISIBLE
                                holder.itemView.badgeContainer.visibility = GONE
                                println("Success")
                                return isFirstResource
                            }

                        })
                        .into(holder.profile)
            }
            else{
                holder.profile.visibility = GONE
                holder.itemView.badgeContainer.visibility = VISIBLE
            }


            holder.url.text = items[position]
            try {
                holder.badge.text = items[position].replace("www.","")[0].toString()
                holder.badge.setBackgroundColor(colorMapper[items[position]]!!)
            }catch (e: Throwable){
                colorMapper[items[position]] =
                        Color.parseColor("#" + ContactHelper.instance.mColors[position % ContactHelper.instance.mColors.size])
                holder.badge
                        .setBackgroundColor(
                                Color.parseColor("#" + ContactHelper.instance.mColors[position % ContactHelper.instance.mColors.size])
                                           )
            }
                        if(WebsiteList.isDeleteMode){
                            if(WebsiteList.deleteList.contains(items[position])){
                                holder.checkBox.setChecked(true, false)
                            }else{
                                holder.checkBox.setChecked(false, false)
                            }
                            animateVisible(holder.checkBox, 100)
                        }else{
                            animateInvisible(holder.checkBox, 50)
                        }

            holder.itemView.setOnLongClickListener{
                                if(!WebsiteList.isDeleteMode){
                                    WebsiteList.isDeleteMode = true
                                    WebsiteList.deleteList.add(items[position])
                                    notifyDataSetChanged()
                                    WebsiteList.deleteView?.call()
                                }
                return@setOnLongClickListener true
            }

                        holder.view.itemRoot.setOnClickListener{
                            if(WebsiteList.isDeleteMode){
                                if (holder.checkBox.isChecked){
                                    WebsiteList.deleteList.remove(items[position])
                                    holder.checkBox.setChecked(false, true)
                                }else{
                                    WebsiteList.deleteList.add(items[position])
                                    holder.checkBox.setChecked(true, true)
                                }

                              notifyItemChanged(position)
                            }
                        }
        }else{
            holder as ContactListHeaderViewHolder

        }
    }


    fun updateList(updatedList: ArrayList<String>){
        this.items = ArrayList(updatedList)
        notifyDataSetChanged()
    }
}


class WebsiteHolder(val view: View, context: Context) : RecyclerView.ViewHolder(view) {
    lateinit var color: String
    var badge = view.badge
    var url = view.url
    var checkBox = view.checkBox
    var profile = view.profile
}