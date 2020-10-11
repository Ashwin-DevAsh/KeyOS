/*
 *     Copyright (C) 2019 Kiosk Team.
 *
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

package tech.DevAsh.Launcher.allapps

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import tech.DevAsh.Launcher.KioskPrefs
import com.android.launcher3.AppFilter
import com.android.launcher3.AppInfo
import com.android.launcher3.LauncherAppState
import com.android.launcher3.allapps.search.AllAppsSearchBarController
import com.android.launcher3.allapps.search.SearchAlgorithm
import com.android.launcher3.compat.LauncherAppsCompat
import com.android.launcher3.compat.UserManagerCompat
import me.xdrop.fuzzywuzzy.FuzzySearch
import me.xdrop.fuzzywuzzy.ToStringFunction

class FuzzyAppSearchAlgorithm(private val context: Context, private val apps: List<AppInfo>) :
        SearchAlgorithm {

    private var resultHandler: Handler = Handler()
    private var suggestionsHandler: Handler = Handler()
    private var filter: AppFilter = AppFilter.newInstance(context)

    override fun doSearch(query: String, callback: AllAppsSearchBarController.Callbacks) {
        val res = query(context, query, apps, filter).map { it.toComponentKey() }
        resultHandler.post {
            callback.onSearchResult(query, ArrayList(res))
        }

    }

    override fun cancel(interruptActiveRequests: Boolean) {
        if (interruptActiveRequests) {
            resultHandler.removeCallbacksAndMessages(null)
            suggestionsHandler.removeCallbacksAndMessages(null)
        }
    }



    companion object {
        const val MIN_SCORE = 65

        @JvmStatic
        fun getApps(context: Context, defaultApps: List<AppInfo>,
                    filter: AppFilter): List<AppInfo> {
            if (!context.KioskPrefs.searchHiddenApps) {
                return defaultApps
            }
            val iconCache = LauncherAppState.getInstance(context).iconCache
            val lac = LauncherAppsCompat.getInstance(context)
            return UserManagerCompat.getInstance(context).userProfiles.flatMap { user ->
                val duplicatePreventionCache = mutableListOf<ComponentName>()
                lac.getActivityList(null, user).filter { info ->
                    filter.shouldShowApp(info.componentName, user) &&
                    !duplicatePreventionCache.contains(info.componentName)
                }.map { info ->
                    duplicatePreventionCache.add(info.componentName)
                    AppInfo(context, info, user).apply {
                        iconCache.getTitleAndIcon(this, false)
                    }
                }
            }
        }

        @JvmStatic
        fun query(context: Context, query: String, defaultApps: List<AppInfo>,
                  filter: AppFilter): List<AppInfo> {
            return FuzzySearch.extractAll(query, getApps(context, defaultApps, filter),
                                          ToStringFunction<AppInfo> { item ->
                                              item?.title.toString()
                                          }, WinklerWeightedRatio(), MIN_SCORE)
                    .sortedBy { it.referent.title.toString() }
                    .sortedByDescending { it.score }
                    .map { it.referent }
        }
    }
}