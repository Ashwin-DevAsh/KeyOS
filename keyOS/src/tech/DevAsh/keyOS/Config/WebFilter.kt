package tech.DevAsh.keyOS.Config

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.launcher3.R
import kotlinx.android.synthetic.dev.activity_web_filter.*

class WebFilter : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_filter)
        loadView()
        onClick()
    }

    fun onClick(){
        enableWebFilter.setOnCheckedChangeListener { view, isChecked ->
            if(isChecked){
                switchStatus.text = "ON"
                options.alpha = 1f
            }else{
                switchStatus.text = "OFF"
                options.alpha = 0.25f
            }
        }
    }

    fun loadView(){

    }
}