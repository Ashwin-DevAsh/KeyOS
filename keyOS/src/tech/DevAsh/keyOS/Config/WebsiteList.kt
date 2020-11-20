package tech.DevAsh.keyOS.Config

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.android.launcher3.R
import io.realm.RealmList
import kotlinx.android.synthetic.dev.activity_website_list.*
import tech.DevAsh.KeyOS.Config.Adapters.WebsiteListAdapter
import tech.DevAsh.KeyOS.Config.AnimateDeleteToggle
import tech.DevAsh.KeyOS.Config.DeleteView
import tech.DevAsh.KeyOS.Config.ToggleCallback
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.keyOS.Config.Fragments.AddWebsite
import java.net.URI
import java.util.*
import kotlin.collections.ArrayList

class WebsiteList : AppCompatActivity(), ToggleCallback, AnimateDeleteToggle {

    companion object{
        enum class WebsiteListType {
            WHITELIST,BLACKLIST
        }
        var isDeleteMode:Boolean = false
        var websiteListType = WebsiteListType.WHITELIST
        var websiteList : ArrayList<String> = ArrayList()
        var websiteListAdapter: WebsiteListAdapter?=null
        var deleteList = ArrayList<String>()
        var deleteView:DeleteView?=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_website_list)

        deleteView = object: DeleteView{
            override fun call() {
                if(isDeleteMode){
                    animateVisible(deleteOptions, 100)
                }
            }
        }

        loadView()
        onClick()
        loadAdapter()
        handelSearch()
    }


    fun onClick(){


        delete.setOnClickListener {
            showDeleteDialog(this)
        }



        addAll.setOnCheckedChangeListener{ _, isChecked->
            if(isChecked){
                deleteList.addAll(websiteList)
                websiteListAdapter?.notifyDataSetChanged()
            }else{
                deleteList.clear()
                websiteListAdapter?.notifyDataSetChanged()
            }

        }

        addWebsite.setOnClickListener {
            addOption.close(true)
            Handler().postDelayed({
                                      AddWebsite(this).show(supportFragmentManager, "")
                                  }, 500)
        }

        back.setOnClickListener{
            super.onBackPressed()
        }
    }


    fun loadView(){
        when(websiteListType){
            WebsiteListType.WHITELIST -> {
                heading.text = "Whitelist"
            }
            WebsiteListType.BLACKLIST -> {
                heading.text = "Blacklist"
            }
        }
    }

    private fun loadAdapter(){
        websiteListAdapter = if(websiteListType == WebsiteListType.BLACKLIST){
            getBlackList()
            WebsiteListAdapter(
                    ArrayList(websiteList),
                    this,
                    "Websites save under blacklist won't be able to access by browser",
                    this,
                              )
        }else{
            getWhiteList()
            WebsiteListAdapter(
                    ArrayList(websiteList),
                    this,
                    "Websites save under whitelist only able to access by the browser",
                    this,
                              )
        }
        websitesContainer.layoutManager = LinearLayoutManager(this)
        websitesContainer.adapter = websiteListAdapter
    }

    private fun handelSearch(){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            var handler = Handler()
            var runnable: Runnable? = null
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                handler.removeCallbacksAndMessages(true)
                if (runnable != null) handler.removeCallbacks(runnable!!)
                val query = newText.toString().toLowerCase(Locale.ROOT)
                runnable = Runnable {
                    websiteListAdapter?.items?.clear()
                    for (i in websiteList) {
                        if (i.toLowerCase(Locale.ROOT).contains(query) || (i === "")) {
                            websiteListAdapter?.items?.add(i)
                        }
                    }
                    websiteListAdapter?.notifyDataSetChanged()
                }
                handler.postDelayed(runnable!!, 100)
                return true
            }

        })
    }

    override fun turnOn() {

        if (websiteListType == WebsiteListType.BLACKLIST){
            UserContext.user!!.webFilter.isBlacklistEnabled=true
            UserContext.user!!.webFilter.isWhitelistEnabled=false
        }else{
            UserContext.user!!.webFilter.isBlacklistEnabled=false
            UserContext.user!!.webFilter.isWhitelistEnabled=true
        }
        websiteListAdapter?.notifyDataSetChanged()

        update()
    }

    override fun turnOff() {
        if (websiteListType == WebsiteListType.BLACKLIST){
            UserContext.user!!.webFilter.isBlacklistEnabled=false
        }else{
            UserContext.user!!.webFilter.isWhitelistEnabled=false
        }
        websiteListAdapter?.notifyDataSetChanged()
        update()
    }

    override fun getToggleState(): Boolean {
        return  if (websiteListType == WebsiteListType.BLACKLIST){
            UserContext.user!!.webFilter.isBlacklistEnabled
        }else{
            UserContext.user!!.webFilter.isWhitelistEnabled
        }
    }

    override fun onBackPressed() {
        if(isGoBack()){
            super.onBackPressed()
        }
    }

    fun update(){
        val newWebsites = RealmList<String>()
        for(i in websiteList){
            if(!newWebsites.contains(i) && i!="")
                newWebsites.add(i)
        }
        if(websiteListType == WebsiteListType.BLACKLIST){
            UserContext.user!!.webFilter.blacklistWebsites = newWebsites
        }else{
            UserContext.user!!.webFilter.whitelistWebsites= newWebsites
        }
        RealmHelper.updateUser(UserContext.user!!)
    }


    private fun isGoBack():Boolean{
        if(addOption.isOpened ){
            addOption.close(true)
            return false
        }
        if(isDeleteMode){
            animateInvisible(deleteOptions, 100)
            isDeleteMode =false
            deleteList.clear()
            websiteListAdapter?.notifyDataSetChanged()
            return false
        }
        return true
    }

    fun addWebsite(string: String):Boolean{
        return try {
            if(string.endsWith(".")
               || string=="https://www."
               || string.endsWith("www")){
                throw Exception()
            }


            val url = URI(string)
            var host: String =  url.host
            val count: Int = host.length - host.replace(".", "").length
            if( count>1){
                host = host.substring(host.indexOf(".") + 1)
            }


            websiteList.add(host)
            websiteListAdapter?.items?.add(host)
            if(websiteListType == WebsiteListType.BLACKLIST){
                UserContext.user!!.webFilter.blacklistWebsites.add(host)
            }else{
                UserContext.user!!.webFilter.whitelistWebsites.add(host)
            }
            update()

            websiteListAdapter?.notifyDataSetChanged()
            true
        }catch (e: Throwable){
            e.printStackTrace()
            false
        }
    }

    private fun showDeleteDialog(context: Context){
        val builder =   MaterialDialog.Builder(context)
        builder.title("Delete")
                .content("Are you sure you want to delete this items")
                .onPositive{ _, _->
                    delete()
                }
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .show()
    }

    private fun getWhiteList(){
        websiteList = ArrayList(UserContext.user!!.webFilter.whitelistWebsites)
        websiteList.add(0, "")
    }

    private fun getBlackList(){
        websiteList = ArrayList(UserContext.user!!.webFilter.blacklistWebsites)
        websiteList.add(0, "")
    }

    private fun delete(){
        websiteList.removeAll(deleteList)
        if(websiteList.isEmpty()){
            websiteList.add("")
        }
        isDeleteMode =false
        websiteListAdapter?.updateList(websiteList)
        animateInvisible(deleteOptions, 100)
        update()
    }

}