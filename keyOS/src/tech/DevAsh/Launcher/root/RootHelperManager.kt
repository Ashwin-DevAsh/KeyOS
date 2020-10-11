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

package tech.DevAsh.Launcher.root

import android.content.Context
import tech.DevAsh.Launcher.ensureOnMainThread
import tech.DevAsh.Launcher.KioskPrefs
import tech.DevAsh.Launcher.useApplicationContext
import tech.DevAsh.Launcher.util.SingletonHolder
import com.android.launcher3.BuildConfig
import com.topjohnwu.superuser.Shell
import eu.chainfire.librootjava.RootIPCReceiver
import eu.chainfire.librootjava.RootJava
import java.util.*

class RootHelperManager(private val context: Context) {

    private val ipcReceiver = object : RootIPCReceiver<IRootHelper>(null, 0) {
        override fun onConnect(ipc: IRootHelper) {
            rootHelper = ipc
            executeQueuedCommands()
        }

        override fun onDisconnect(ipc: IRootHelper) {
            rootHelper = null
            commandQueue.clear()
        }
    }

    private var commandQueue = LinkedList<(IRootHelper) -> Unit>()
    private var rootHelper: IRootHelper? = null

    init {
        ipcReceiver.setContext(context)
    }

    fun run(command: (IRootHelper) -> Unit) {
        commandQueue.offer(command)
        launchRootHelper()
        executeQueuedCommands()
    }

    private fun launchRootHelper() {
        context.KioskPrefs.autoLaunchRoot = isAvailable
        if (!isAvailable) return
        if (rootHelper != null) return

        RootJava.getLaunchScript(
                context, RootHelper::class.java,
                null, null, null, BuildConfig.APPLICATION_ID + ":rootHelper"
                                ).forEach { Shell.su(it).submit() }
    }

    private fun executeQueuedCommands() {
        while (commandQueue.peek() != null && rootHelper != null) {
            commandQueue.poll()(rootHelper!!)
        }
    }

    companion object : SingletonHolder<RootHelperManager, Context>(
            ensureOnMainThread(useApplicationContext(::RootHelperManager))
                                                                  ) {

        val isAvailable by lazy { Shell.rootAccess() }
    }
}
