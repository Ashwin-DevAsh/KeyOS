package tech.DevAsh.KeyOS.Helpers

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import com.danimahardhika.cafebar.CafeBar
import com.danimahardhika.cafebar.CafeBarTheme
import com.nispok.snackbar.Snackbar
import com.nispok.snackbar.SnackbarManager


object AlertHelper {
    fun showError(text: String, context: AppCompatActivity){
        SnackbarManager.show(
                Snackbar.with(context) // context
                        .text(text) // text to be displayed
                        .textTypeface(Typeface.DEFAULT_BOLD)
                        .duration(2000)
                        .textColor(Color.WHITE) // change the text color
                        .color(Color.parseColor("#b71c1c")) // change the background color
                , context
                            )
    }

    fun showToast(text: String, context: AppCompatActivity){
        SnackbarManager.show(
                Snackbar.with(context) // context
                        .text(text) // text to be displayed
                        .textTypeface(Typeface.DEFAULT_BOLD)
                        .duration(2000)
                        .textColor(Color.WHITE) // change the text color
                        .color(Color.parseColor("#4caf50")) // change the background color
                , context
                            )

    }

    fun showSnackbar(text: String,context: AppCompatActivity) {
        CafeBar.builder(context)
                .theme(CafeBarTheme.DARK)
                .floating(true)
                .duration(CafeBar.Duration.MEDIUM)
                .content(text)
                .show()
    }


}

