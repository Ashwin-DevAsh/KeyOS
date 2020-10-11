package tech.DevAsh.KeyOS.Config

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.launcher3.R
import io.realm.RealmList
import kotlinx.android.synthetic.keyOS.activity_allow_apps.*
import tech.DevAsh.KeyOS.Config.Adapters.AllowItemAdapter
import tech.DevAsh.KeyOS.Database.AppsContext
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.keyOS.Database.Apps

import java.util.*
import kotlin.collections.ArrayList


class AllowApps : AppCompatActivity() {

    private var isSelectAll = true

    var adapter: AllowItemAdapter? = null

    companion object{
        var type:String="Allow Apps"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allow_apps)
        RealmHelper.init(this)
        onClick()
        loadView()
        handelSearch()

        when(type){
            "Allow Apps"->{
                heading.text="Apps"
                loadAdapter(

                        AppsContext.allApps,
                        AppsContext.allowedApps,
                        "Whitelist Apps",
                        "Select which apps you want to\ngive access"
                )
            }
            "Allow Services"->{
                heading.text="Services"
                loadAdapter(
                    AppsContext.allService,
                    AppsContext.allowedService,
                    "Whitelist Services",
                    "Select which services you want to\ngive access"
                )
            }
        }

    }

    private fun onClick(){
        done.setOnClickListener {
            saveData()
        }
        cancel.setOnClickListener {
           super.onBackPressed()
        }
    }


    private fun handelSearch(){
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            var handler = Handler()
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.toString().toLowerCase(Locale.ROOT)
                handler.removeCallbacksAndMessages(true)
                handler.postDelayed({
                    adapter?.items?.clear()
                    for (i in adapter!!._items) {
                        if (i.appName
                                .toLowerCase(Locale.ROOT)
                                .contains(query)
                        ) {
                            adapter!!.items.add(i)
                        }
                    }
                    adapter!!.items.add(0, Apps())
                    adapter!!.notifyDataSetChanged()
                }, 100)
            return true
            }

        })
    }


    private fun loadView(){
        val layoutManager = LinearLayoutManager(this)
        appsContainer.isDrawingCacheEnabled = true
        appsContainer.setItemViewCacheSize(1000)
        appsContainer.layoutManager = layoutManager
    }

    private fun loadAdapter(items:MutableList<Apps>,
                            allowedItems:MutableList<Apps>,
                            heading:String,
                            subHeading:String
    ){
        Handler().postDelayed({
            items.removeAll(allowedItems)
            items.addAll(0, allowedItems)
            adapter = AllowItemAdapter(ArrayList(items), ArrayList(allowedItems), heading,subHeading,this)
            adapter!!.items.add(0,Apps())
            adapter!!.notifyDataSetChanged()
            appsContainer.adapter = adapter
            loadingScreen.visibility = View.GONE
        }, 1000)
    }



    private fun saveData(){
        when(type){
            "Allow Apps"->{
                AppsContext.allowedApps = ArrayList(adapter?.allowedItems!!)
                val allowedApps = getRealmList(AppsContext.allowedApps)
                UserContext.user?.allowedApps = allowedApps
            }
            "Allow Services"->  {
                AppsContext.allowedService = ArrayList(adapter?.allowedItems!!)
                val allowServices = getRealmList(AppsContext.allowedService)
                UserContext.user?.allowedServices = allowServices
            }
        }


        RealmHelper.updateUser(UserContext.user!!)
        finish()
    }

    private fun getRealmList(list: MutableList<Apps>): RealmList<Apps> {
        val allowedItems = RealmList<Apps>()
        for(i in list){
            if (i.appName!=null){
                allowedItems.add(Apps(i.packageName))
            }
        }
        return allowedItems
    }

}

