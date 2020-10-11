/*
 *     This file is part of Kiosk Launcher.
 *
 *     Kiosk Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Kiosk Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Kiosk Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.DevAsh.Launcher.flowerpot

import android.content.Context
import tech.DevAsh.Launcher.flowerpot.parser.FlowerpotReader
import tech.DevAsh.Launcher.flowerpot.rules.Rule
import tech.DevAsh.Launcher.toTitleCase
import java.io.InputStream

/**
 * A ruleset for an app category
 */
class Flowerpot(private val context: Context, val name: String, private val loader: Flowerpot.() -> Unit) {

    val displayName by lazy {
        val id = context.resources.getIdentifier("category_${name.toLowerCase()}", "string", context.packageName)
        if (id != 0)
            context.getString(id)
        else
            beautifyName(name)
    }
    private var loaded = false
    val rules: MutableSet<Rule> = mutableSetOf()
    val size get() = rules.size
    lateinit var apps: FlowerpotApps

    fun ensureLoaded() {
        if (!loaded) {
            load()
            loaded = true
        }
    }

    private fun load() {
        loader(this)
        apps = FlowerpotApps(context, this)
    }

    /**
     * Load all data from
     */
    private fun loadFromInputStream(inputStream: InputStream) {
        rules.addAll(FlowerpotReader(inputStream).readRules())
    }

    companion object {
        /**
         * Load a flowerpot from an assets file
         */
        fun fromAssets(context: Context, path: String, name: String): Flowerpot {
            return Flowerpot(context, name) {
                loadFromInputStream(context.assets.open(path))
            }
        }

        /**
         * The current Flowerpot format version
         */
        const val VERSION_CURRENT = Version.AZALEA
        /**
         * List of all currently supported versions
         */
        val SUPPORTED_VERSIONS = arrayOf(
                VERSION_CURRENT
        )
        /**
         * Path relative to assets/ to the directory containing the shipped flowerpot files
         */
        const val ASSETS_PATH = "flowerpot"

        private fun beautifyName(name: String): String {
            return name.replace('_', ' ').toLowerCase().toTitleCase()
        }

    }

    object Version {
        /**
         * Azalea - A beautiful pink flower, known in China as "thinking of home bush" (sixiang shu).
         *
         * Changes:
         *  - The very first version of the format
         */
        const val AZALEA = 1
    }

    /**
     * Class used to interact with Pots as a whole, load hem
     */
    class FlowerpotManager constructor(private val context: Context) {

        private val pots = mutableMapOf<String, Flowerpot>()

        init {
            loadAssets()
        }

        /**
         * Load flowerpot files located in assets/
         */
        private fun loadAssets() {
            context.assets.list(ASSETS_PATH)?.forEach {
                pots.getOrPut(it) {
                    fromAssets(context, "$ASSETS_PATH/$it", it)
                }
            }
        }

        fun getAllPots() = pots.values

    }
}