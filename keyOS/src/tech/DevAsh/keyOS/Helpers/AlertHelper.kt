package tech.DevAsh.KeyOS.Helpers

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    fun showToast(text: String, context: Context) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()

//        CafeBar.builder(context)
//                .theme(CafeBarTheme.DARK)
//                .floating(true)
//                .duration(CafeBar.Duration.MEDIUM)
//                .content(text)
//                .show()

    }
}

