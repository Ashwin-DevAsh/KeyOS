package tech.DevAsh.KeyOS.Config

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import tech.DevAsh.KeyOS.Config.Adapters.ActivitiesListAdapter
import tech.DevAsh.KeyOS.Helpers.HelperVariables
import com.android.launcher3.R
import kotlinx.android.synthetic.keyOS.activity_activity_list.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Database.UserContext

import java.util.*
import kotlin.collections.ArrayList


class ActivityList : AppCompatActivity() {

    private var activities: Array<ActivityInfo?> = HelperVariables.selectedApp!!.packageInfo.activities

    private var activityListAdapter = ActivitiesListAdapter(
        activities.toMutableList(),
        HelperVariables.selectedApp!!.blockedActivities,
        "",
        "Select which activities \\ Pages \\ windows you want to block",
        this
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_list)
        onClick()
        loadAdapter()
        handelSearch()
        println(HelperVariables.selectedApp!!.blockedActivities)
    }

    private fun loadAdapter(){
        activityContainer.layoutManager = LinearLayoutManager(this)
        activityContainer.adapter = activityListAdapter
    }

    fun onClick(){
        cancel.setOnClickListener {
            super.onBackPressed()
        }

        done.setOnClickListener {
            saveData()
        }
    }

    private fun saveData(){
        HelperVariables.selectedApp?.blockedActivities?.clear()
        for ( i in activityListAdapter.blockedItems){
            HelperVariables.selectedApp?.blockedActivities?.add(i)
        }

        UserContext.user!!.editedApps.remove(HelperVariables.selectedApp)
        UserContext.user!!.editedApps.add(HelperVariables.selectedApp)

        RealmHelper.updateUser(UserContext.user!!)
        super.onBackPressed()
    }

    private fun handelSearch(){
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            var handler = Handler()
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.toString().toLowerCase(Locale.ROOT)
                handler.removeCallbacksAndMessages(true)
                handler.postDelayed({
                    activityListAdapter.items.clear()
                    for (i in activityListAdapter._items) {
                        val nameList = i!!.name.split(".")
                        if (nameList[nameList.size-1]
                                .toLowerCase(Locale.ROOT)
                                .contains(query)
                        ) {
                            activityListAdapter.items.add(i)
                        }
                    }
                    activityListAdapter.items.add(0, ActivityInfo())
                    activityListAdapter.notifyDataSetChanged()
                }, 250)
                return true
            }
        })
    }


}