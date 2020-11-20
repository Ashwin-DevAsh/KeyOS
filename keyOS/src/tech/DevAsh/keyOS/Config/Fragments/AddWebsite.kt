package tech.DevAsh.keyOS.Config.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.launcher3.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.dev.fragment_add_website.*
import kotlinx.android.synthetic.dev.fragment_add_website.done
import tech.DevAsh.keyOS.Config.WebsiteList


class AddWebsite(val websiteList: WebsiteList) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_website, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadView()
        onClick()
    }

    fun loadView(){
        if(WebsiteList.websiteListType==WebsiteList.Companion.WebsiteListType.BLACKLIST){
            subHeading.text = "Please enter the website which you want to add in blacklist Eg: www.google.com"
        }else{
            subHeading.text = "Please enter the website which you want to add in whitelist Eg: www.google.com"
        }
    }

    fun onClick(){
        done.setOnClickListener {
           if (websiteList.addWebsite(url.text.toString())){
               dismiss()
           }else if(url.text.toString().isNotEmpty()){
               urlLayout.error = "Invalid Url"
           }
        }
    }

}