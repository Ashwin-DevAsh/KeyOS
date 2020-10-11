package tech.DevAsh.KeyOS.Helpers

import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView


object UiHelper {

    fun handelAppBar(scroller: View, appBar: CardView){
        scroller.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY >1) {
                appBar.cardElevation=10f

            }
            if(scrollY < 1){
                appBar.cardElevation=0f
            }
        }
    }

    fun handelAppBarWithRecyclerView(scroller: RecyclerView, appBar: CardView){
        scroller.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                android.os.Handler().post {
                    val scrollY = scroller.computeVerticalScrollOffset()
                    if (scrollY > 1) {
                        appBar.cardElevation = 10f

                    }
                    if (scrollY < 1) {
                        appBar.cardElevation = 0f
                    }
                }
            }
        })
    }

    fun hideKeyboard(activity: AppCompatActivity) {
        val imm: InputMethodManager =
            activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}