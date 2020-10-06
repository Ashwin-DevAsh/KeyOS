package ch.deletescape.lawnchair.flowerpot

import android.content.Context
import ch.deletescape.lawnchair.flowerpot.parser.ChinapotReader
import com.android.launcher3.AppInfo
import java.util.ArrayList

/**
 * A ruleset for an app category
 */
class Chinapot(private val context: Context) {

    companion object {
        const val ASSETS_PATH = "chinapot"
    }

    val categoryMap: HashMap<String, String> = HashMap()

    init {
        context.assets.list(ASSETS_PATH)?.forEach {
            loadpot(it)
        }
    }

    private fun loadpot(name : String) {
        ChinapotReader(context.assets.open("$ASSETS_PATH/$name")).readRules()?.forEach {
            categoryMap.put(it, name)
        }
    }

}