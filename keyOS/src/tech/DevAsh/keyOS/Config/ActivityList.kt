package tech.DevAsh.KeyOS.Config

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import tech.DevAsh.KeyOS.Config.Adapters.ActivitiesListAdapter
import tech.DevAsh.KeyOS.Helpers.HelperVariables
import com.android.launcher3.R
import kotlinx.android.synthetic.dev.activity_activity_list.*
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.keyOS.Database.User

import java.util.*


class ActivityList : AppCompatActivity() {

    private var activities: Array<ActivityInfo?> = HelperVariables.selectedEditedApp!!.packageInfo.activities

    private var activityListAdapter = ActivitiesListAdapter(
        activities.toMutableList(),
        HelperVariables.selectedEditedApp!!.blockedActivities,
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

        back.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun saveData(){
        HelperVariables.selectedEditedApp?.blockedActivities?.clear()
        for ( i in activityListAdapter.blockedItems){
            HelperVariables.selectedEditedApp?.blockedActivities?.add(i)
        }

        User.user!!.editedApps.removeAll(arrayListOf(HelperVariables.selectedEditedApp))
        User.user!!.editedApps.add(HelperVariables.selectedEditedApp)
        RealmHelper.updateUser(User.user!!)
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