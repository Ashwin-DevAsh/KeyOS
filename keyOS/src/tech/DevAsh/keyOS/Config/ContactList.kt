package tech.DevAsh.KeyOS.Config

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.android.launcher3.R
import io.realm.RealmList
import kotlinx.android.synthetic.dev.activity_contact_list.*
import tech.DevAsh.KeyOS.Config.Adapters.ContactListAdapter
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.keyOS.Database.Contact
import tech.DevAsh.keyOS.Database.User
import java.util.*
import kotlin.collections.ArrayList


class ContactList : AppCompatActivity(), ToggleCallback, AnimateDeleteToggle{

    companion object{
        var contactList : ArrayList<Contact> = ArrayList()
        var contactListAdapter:ContactListAdapter?=null
        var isBlackList:Boolean = false
        var isDeleteMode:Boolean = false
        var deleteView:DeleteView?=null
        var deleteList = ArrayList<Contact>()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RealmHelper.init(applicationContext)
        setContentView(R.layout.activity_contact_list)


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
        isDeleteMode=false
    }

    private fun loadView(){
        if(isBlackList){
            heading.text=getString(R.string.blacklist)
        }else{
            heading.text=getString(R.string.whitelist)
        }
    }

    private fun handelSearch(){
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
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
                    contactListAdapter?.items?.clear()
                    for (i in contactList) {
                        if (i.name.toLowerCase(Locale.ROOT)
                                .contains(query) || (i.name == "" && i.number == "")
                        ) {
                            contactListAdapter?.items?.add(i)
                        }
                    }
                    contactListAdapter?.notifyDataSetChanged()
                }
                handler.postDelayed(runnable!!, 100)
                return true
            }

        })
    }

    private fun loadAdapter(){
        contactListAdapter = if(isBlackList){
            getBlackList()
            ContactListAdapter(
                    ArrayList(contactList),
                    this,
                    getString(R.string.contacts_blacklist_subheading),
                    this,
            )
        }else{
            getWhiteList()
            ContactListAdapter(
                    ArrayList(contactList),
                    this,
                    getString(R.string.contacts_whitelist_subheading),
                    this,
            )
        }
        contactsContainer.layoutManager = LinearLayoutManager(this)
        contactsContainer.adapter = contactListAdapter
    }

    fun onClick(){
        phoneBook.setOnClickListener{
            addOption.close(true)
            Handler().postDelayed({
                startActivity(Intent(this, PhoneBook::class.java))
            }, 500)
        }

        manual.setOnClickListener {
            addOption.close(true)
            Handler().postDelayed({
                startActivity(Intent(this, ContactsInput::class.java))
            }, 500)
        }

        delete.setOnClickListener {
            showDeleteDialog(this)
        }

        addAll.setOnCheckedChangeListener{ _, isChecked->
            if(isChecked){
                deleteList.addAll(contactList)
                contactListAdapter?.notifyDataSetChanged()
            }else{
                deleteList.clear()
                contactListAdapter?.notifyDataSetChanged()
            }

        }

        back.setOnClickListener{
            super.onBackPressed()
        }
    }

    private fun getWhiteList(){
        contactList = ArrayList(User.user!!.calls.whiteListContacts)
        contactList.add(0, Contact("", ""))
    }

    private fun getBlackList(){
        contactList = ArrayList(User.user!!.calls.blacklistContacts)
        contactList.add(0, Contact("", ""))
    }

    private fun isGoBack():Boolean{
        if(addOption.isOpened ){
            addOption.close(true)
            return false
        }
        if(isDeleteMode){
            animateInvisible(deleteOptions, 100)
            isDeleteMode=false
            deleteList.clear()
            contactListAdapter?.notifyDataSetChanged()
            return false
        }
        return true
    }



    fun update(){
        val newContacts = RealmList<Contact>()
        for(i in  contactList){
            if(!newContacts.contains(i) && i.number!="")
                newContacts.add(i)
        }
        if(isBlackList){
            User.user!!.calls.blacklistContacts = newContacts
        }else{
            User.user!!.calls.whiteListContacts= newContacts
        }
        RealmHelper.updateUser(User.user!!)
    }

    override fun onBackPressed() {
        if(isGoBack()){
           super.onBackPressed()
        }
    }



    private fun showDeleteDialog(context: Context){
        val builder =   MaterialDialog.Builder(context)
        builder.title(getString(R.string.delete))
            .content(getString(R.string.delete_contact_subheading))
            .onPositive{_,_->
                delete()
            }
            .positiveText(android.R.string.ok)
            .negativeText(android.R.string.cancel)
            .show()
    }

    private fun delete(){
        contactList.removeAll(deleteList)
        if(contactList.isEmpty()){
            contactList.add(Contact("",""))
        }
        isDeleteMode=false
        contactListAdapter?.updateList(contactList)
        animateInvisible(deleteOptions, 100)
        update()
    }

    override fun turnOn() {

        if (isBlackList){
            User.user!!.calls.blackListCalls=true
            User.user!!.calls.whitelistCalls=false
            User.user!!.calls.automaticWhitelist=false
        }else{
            User.user!!.calls.blackListCalls=false
            User.user!!.calls.whitelistCalls=true
            User.user!!.calls.automaticWhitelist=false
        }
        contactListAdapter?.notifyDataSetChanged()

        update()
    }

    override fun turnOff() {
        if (isBlackList){
            User.user!!.calls.blackListCalls=false
        }else{
            User.user!!.calls.whitelistCalls=false
        }
        contactListAdapter?.notifyDataSetChanged()
        update()
    }

    override fun getToggleState(): Boolean {
       return  if (isBlackList){
           User.user!!.calls.blackListCalls
        }else{
           User.user!!.calls.whitelistCalls
        }
    }
}


interface AnimateDeleteToggle{
     fun animateVisible(
            viewVisible: View,
            visibleDuration: Long
                       ){
        viewVisible.animate()
                .alpha(1f)
                .setDuration(visibleDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        viewVisible.visibility = (View.VISIBLE)
                    }
                })
    }
    fun animateInvisible(viewInvisible: View, invisibleDuration: Long){
        viewInvisible.animate()
                .alpha(0f)
                .setDuration(invisibleDuration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        viewInvisible.visibility = (View.INVISIBLE)
                    }
                })
    }
}


interface ToggleCallback{
    fun turnOn()
    fun turnOff()
    fun getToggleState():Boolean
}

interface DeleteView{
    fun call()
}