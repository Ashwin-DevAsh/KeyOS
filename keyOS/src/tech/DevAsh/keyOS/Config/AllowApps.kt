package tech.DevAsh.KeyOS.Config

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.launcher3.R
import io.realm.RealmList
import kotlinx.android.synthetic.dev.activity_allow_apps.*
import tech.DevAsh.KeyOS.Config.Adapters.AllowItemAdapter
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.keyOS.Config.Adapters.SingleAppAdapter
import tech.DevAsh.keyOS.Database.Apps
import tech.DevAsh.keyOS.Database.User

import java.util.*
import kotlin.collections.ArrayList


class AllowApps : AppCompatActivity() {


    var adapter: AllowItemAdapter? = null

    companion object{
        enum class Types {
            ALLOWAPPS,ALLOWSERVICES,SINGLEAPP
        }
        var type:Types=Types.ALLOWAPPS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allow_apps)
        RealmHelper.init(this)
        onClick()
        loadView()
        handelSearch()

        when(type){
           Types.ALLOWAPPS->{
                heading.text="Apps"
                loadAdapter(

                        Apps.allApps,
                        User.user!!.allowedApps,
                        "Whitelist Apps",
                        "Select which apps you want to\ngive access"
                )
            }
            Types.ALLOWSERVICES->{
                heading.text="Services"
                loadAdapter(
                        Apps.allService,
                    User.user!!.allowedServices,
                    "Whitelist Services",
                    "Select which services you want to\ngive access"
                           )
            }
            Types.SINGLEAPP->{
                heading.text="Single App"
                loadAdapterSingleApp(
                        Apps.allApps,
                        "Select which app you want available\nin foreground"
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

        back.setOnClickListener {
            super.onBackPressed()
        }
    }


    private fun handelSearch(){
        searchView.setOnQueryTextListener(object :
                                                  androidx.appcompat.widget.SearchView.OnQueryTextListener {
            var handler = Handler()
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val adapter = adapter
                val query = newText.toString().toLowerCase(Locale.ROOT)
                handler.removeCallbacksAndMessages(true)
                handler.postDelayed({
                    try{
                        adapter?.items?.clear()
                        for (i in adapter!!._items) {
                            if (i.appName
                                            .toLowerCase(Locale.ROOT)
                                            .contains(query)
                            ) {
                                adapter.items.add(i)
                            }
                        }
                        adapter.items.add(0, Apps())
                        adapter.notifyDataSetChanged()

                    }catch (e:Throwable){} }, 100)
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
            val allowedItemsTemp = ArrayList<Apps>()
            for ( i in allowedItems){
                val index = items.indexOf(i)
                if(index!=-1){
                    val app = items[index]
                    if(!allowedItemsTemp.contains(app)) allowedItemsTemp.add(app)
                }
            }
            items.removeAll(allowedItemsTemp)
            items.addAll(0, allowedItemsTemp)
            adapter = AllowItemAdapter(ArrayList(items), ArrayList(allowedItemsTemp), heading,subHeading,this)
            adapter!!.items.add(0,Apps())
            adapter!!.notifyDataSetChanged()
            appsContainer.adapter = adapter
            loadingScreen.visibility = View.GONE
        }, 2000)
    }

    private fun loadAdapterSingleApp(items: MutableList<Apps>, subHeading: String = "Select which app you want always available\nin foreground"
                                    ){
        Handler().postDelayed({

                              val singleAppIndex = Apps.allApps.indexOf(User.user!!.singleApp)
                              if(singleAppIndex!=-1){
                                  User.user!!.singleApp = Apps.allApps[singleAppIndex]
                                  items.remove(User.user!!.singleApp)
                                  items.add(0, User.user!!.singleApp)
                              }

                              adapter = SingleAppAdapter(ArrayList(items), User.user!!.singleApp, this, subHeading,object :ToggleCallback{
                                  override fun turnOn() {

                                  }

                                  override fun turnOff() {
                                  }

                                  override fun getToggleState(): Boolean {
                                      return true
                                  }
                              },User.user!!.singleApp!=null)
                              adapter!!.items.add(0,Apps())
                              adapter!!.notifyDataSetChanged()
                              appsContainer.adapter = adapter
                              loadingScreen.visibility = View.GONE
                              }, 1000)
    }



    private fun saveData(){
        when(type){
            Types.ALLOWAPPS -> {
                println(adapter?.allowedItems)
                val allowedApps = getRealmList(ArrayList(adapter?.allowedItems!!))
                User.user?.allowedApps = allowedApps
            }
            Types.ALLOWSERVICES->  {
                val allowServices = getRealmList(ArrayList(adapter?.allowedItems!!))
                User.user?.allowedServices = allowServices
            }
            Types.SINGLEAPP->{
                val myAdapter = adapter as SingleAppAdapter
                if((myAdapter).toggleState){
                    User.user?.singleApp = myAdapter.singleApp
                }else{
                    User.user?.singleApp = null
                }

            }
        }

        RealmHelper.updateUser(User.user!!)
        finish()
    }

    private fun getRealmList(list: MutableList<Apps>): RealmList<Apps> {
        val allowedItems = RealmList<Apps>()
        for(i in list){
            if (i.appName!=null){
                allowedItems.add(Apps(i.packageName,i.appName))
            }
        }
        return allowedItems
    }

}

