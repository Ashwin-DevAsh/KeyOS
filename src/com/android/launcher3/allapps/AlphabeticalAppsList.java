/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.allapps;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;

import android.os.UserHandle;
import tech.DevAsh.Launcher.LawnchairPreferences;
import com.android.launcher3.AppInfo;
import com.android.launcher3.IconCache;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.ItemInfoMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * The alphabetically sorted list of applications.
 */
public class AlphabeticalAppsList implements AllAppsStore.OnUpdateListener {

    public static final String TAG = "AlphabeticalAppsList";

    /**
     * Info about a particular adapter item (can be either section or app)
     */
    public static class AdapterItem {
        /** Common properties */
        // The index of this adapter item in the list
        public int position;
        // The type of this item
        public int viewType;

        /** App-only properties */
        // The row that this item shows up on
        public int rowIndex;
        // The index of this app in the row
        public int rowAppIndex;
        // The associated AppInfo for the app
        public AppInfo appInfo = null;
        // The index of this app not including sections
        public int appIndex = -1;

        /**
         * Search suggestion-only properties
         */
        public String suggestion;

        public static AdapterItem asApp(int pos, AppInfo appInfo,
                                        int appIndex) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_ICON;
            item.position = pos;
            item.appInfo = appInfo;
            item.appIndex = appIndex;
            return item;
        }

        public static AdapterItem asEmptySearch(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_EMPTY_SEARCH;
            item.position = pos;
            return item;
        }

        public static AdapterItem asAllAppsDivider(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_ALL_APPS_DIVIDER;
            item.position = pos;
            return item;
        }

        public static AdapterItem asMarketSearch(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_SEARCH_MARKET;
            item.position = pos;
            return item;
        }

        public static AdapterItem asWorkTabFooter(int pos) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_WORK_TAB_FOOTER;
            item.position = pos;
            return item;
        }

        public static AdapterItem asSearchSuggestion(int pos, String suggestion) {
            AdapterItem item = new AdapterItem();
            item.viewType = AllAppsGridAdapter.VIEW_TYPE_SEARCH_SUGGESTION;
            item.position = pos;
            item.suggestion = suggestion;
            return item;
        }
    }

    private final Launcher mLauncher;

    // The set of apps from the system
    private final List<AppInfo> mApps = new ArrayList<>();
    private final AllAppsStore mAllAppsStore;

    // The set of filtered apps with the current filter
    private final List<AppInfo> mFilteredApps = new ArrayList<>();
    // The current set of adapter items
    private final ArrayList<AdapterItem> mAdapterItems = new ArrayList<>();

    // The of ordered component names as a result of a search query
    private ArrayList<ComponentKey> mSearchResults;
    private AllAppsGridAdapter mAdapter;
    private AppInfoComparator mAppNameComparator;
    private final int mNumAppsPerRow;
    private int mNumAppRowsInAdapter;
    private ItemInfoMatcher mItemFilter;
    private LawnchairPreferences prefs;

    private List<String> mSearchSuggestions;

    public AlphabeticalAppsList(Context context, AllAppsStore appsStore) {
        mAllAppsStore = appsStore;
        mLauncher = Launcher.getLauncher(context);
        mAppNameComparator = new AppInfoComparator(context);
        mNumAppsPerRow = mLauncher.getDeviceProfile().inv.numColsDrawer;
        mAllAppsStore.addUpdateListener(this);
        prefs = Utilities.getLawnchairPrefs(context);
    }

    public void updateItemFilter(ItemInfoMatcher itemFilter) {
        this.mItemFilter = itemFilter;
        onAppsUpdated();
    }

    /**
     * Sets the adapter to notify when this dataset changes.
     */
    public void setAdapter(AllAppsGridAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * Returns all the apps.
     */
    public List<AppInfo> getApps() {
        return mApps;
    }

    /**
     * Returns the current filtered list of applications broken down into their sections.
     */
    public List<AdapterItem> getAdapterItems() {
        return mAdapterItems;
    }

    /**
     * Returns the number of rows of applications
     */
    public int getNumAppRows() {
        return mNumAppRowsInAdapter;
    }

    /**
     * Returns the number of applications in this list.
     */
    public int getNumFilteredApps() {
        return mFilteredApps.size();
    }

    /**
     * Returns whether there are is a filter set.
     */
    public boolean hasFilter() {
        return (mSearchResults != null);
    }

    /**
     * Returns whether there are suggestions.
     */
    public boolean hasSuggestions() {
        return mSearchSuggestions != null && !mSearchSuggestions.isEmpty();
    }

    /**
     * Returns whether there are no filtered results.
     */
    public boolean hasNoFilteredResults() {
        return (mSearchResults != null) && mFilteredApps.isEmpty() && (mSearchSuggestions != null) && mSearchSuggestions.isEmpty();
    }

    public List<AppInfo> getFilteredApps() {
        return mFilteredApps;
    }

    /**
     * Sets the sorted list of filtered components.
     */
    public boolean setOrderedFilter(ArrayList<ComponentKey> f) {
        if (mSearchResults != f) {
            boolean same = mSearchResults != null && mSearchResults.equals(f);
            mSearchResults = f;
            onAppsUpdated();
            return !same;
        }
        return false;
    }

    public boolean setSearchSuggestions(List<String> suggestions) {
        if (mSearchSuggestions != suggestions) {
            boolean same = mSearchSuggestions != null && mSearchSuggestions.equals(suggestions);
            mSearchSuggestions = suggestions;
            onAppsUpdated();
            return !same;
        }
        return false;
    }

    /**
     * Updates internals when the set of apps are updated.
     */
    @Override
    public void onAppsUpdated() {
        // Sort the list of apps
        mApps.clear();

        ArrayList<ComponentKey> recentLaunchedApps = mLauncher.getRecentLaunchedApps();

        ArrayList<ComponentKey> removedApps = new ArrayList<>();
        for (ComponentKey recentLaunchedApp : recentLaunchedApps) {
            AppInfo app = mAllAppsStore.getApp(recentLaunchedApp);
            if (app != null) {
                mApps.add(app);
            } else {
                removedApps.add(recentLaunchedApp);
            }
        }
        if (removedApps.size() > 0) {
            recentLaunchedApps.removeAll(removedApps);
            mLauncher.setRecentLaunchedApps(recentLaunchedApps);
        }

        for (AppInfo app : mAllAppsStore.getApps()) {
            if (mApps.size() >= mNumAppsPerRow) {
                break;
            }
            if ((mItemFilter == null || mItemFilter.matches(app, null) || hasFilter()) &&
                    !recentLaunchedApps.contains(app.getComponentKey())) {
                mApps.add(app);
            }
        }

//        Collections.sort(mApps, mAppNameComparator);

        // Recompose the set of adapter items from the current set of apps
        updateAdapterItems();
    }

    /**
     * Updates the set of filtered apps with the current filter.  At this point, we expect
     * mCachedSectionNames to have been calculated for the set of all apps in mApps.
     */
    private void updateAdapterItems() {
        refillAdapterItems();
        refreshRecyclerView();
    }

    private void refreshRecyclerView() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void refillAdapterItems() {
        int position = 0;
        int appIndex = 0;

        // Prepare to update the list of sections, filtered apps, etc.
        mFilteredApps.clear();
        mAdapterItems.clear();

        // Recreate the filtered and sectioned apps (for convenience for the grid layout) from the
        // ordered set of sections
        for (AppInfo info : getFiltersAppInfos()) {
//            if (!hasFilter() && folderFilters.contains(info.toComponentKey())) {
//                continue;
//            }
//            String sectionName = getAndUpdateCachedSectionName(info);
//
//            // Create a new section if the section names do not match
//            if (!sectionName.equals(lastSectionName)) {
//                lastSectionName = sectionName;
//                int color = 0;
//                lastFastScrollerSectionInfo = new FastScrollSectionInfo(sectionName, color);
//                mFastScrollerSections.add(lastFastScrollerSectionInfo);
//            }

            // Create an app item
            AdapterItem appItem = AdapterItem.asApp(position++, info, appIndex++);
//            if (lastFastScrollerSectionInfo.fastScrollToItem == null) {
//                lastFastScrollerSectionInfo.fastScrollToItem = appItem;
//            }
            mAdapterItems.add(appItem);
            mFilteredApps.add(info);
        }

        if (hasFilter()) {
            // Append the search market item
            if (hasNoFilteredResults() && !hasSuggestions()) {
                mAdapterItems.add(AdapterItem.asEmptySearch(position++));
            } else {
                mAdapterItems.add(AdapterItem.asAllAppsDivider(position++));
            }
            mAdapterItems.add(AdapterItem.asMarketSearch(position++));
        }

        // Search suggestions should be all the way to the top
        if (hasFilter() && hasSuggestions()) {
            for (String suggestion : mSearchSuggestions) {
                mAdapterItems.add(AdapterItem.asSearchSuggestion(position++, suggestion));
            }
        }

        if (mNumAppsPerRow != 0) {
            // Update the number of rows in the adapter after we do all the merging (otherwise, we
            // would have to shift the values again)
            int numAppsInSection = 0;
            int numAppsInRow = 0;
            int rowIndex = -1;
            for (AdapterItem item : mAdapterItems) {
                item.rowIndex = 0;
                if (AllAppsGridAdapter.isDividerViewType(item.viewType)) {
                    numAppsInSection = 0;
                } else if (AllAppsGridAdapter.isIconViewType(item.viewType)) {
                    if (numAppsInSection % mNumAppsPerRow == 0) {
                        numAppsInRow = 0;
                        rowIndex++;
                    }
                    item.rowIndex = rowIndex;
                    item.rowAppIndex = numAppsInRow;
                    numAppsInSection++;
                    numAppsInRow++;
                }
            }
            mNumAppRowsInAdapter = rowIndex + 1;
        }

        // Add the work profile footer if required.
//        if (shouldShowWorkFooter()) {
//            mAdapterItems.add(AdapterItem.asWorkTabFooter(position++));
//        }
    }

//    private boolean shouldShowWorkFooter() {
//        return Utilities.ATLEAST_P &&
//                (DeepShortcutManager.getInstance(mLauncher).hasHostPermission()
//                        || Utilities
//                        .hasPermission(mLauncher, "android.permission.MODIFY_QUIET_MODE"));
//    }

    private List<AppInfo> getFiltersAppInfos() {
        ArrayList<AppInfo> result = new ArrayList<>();
        if (mSearchResults == null) {
            for (AppInfo app : mApps) {
                if (result.size() >= mNumAppsPerRow) {
                    break;
                }
                result.add(app);
            }
            return result;
        }

        final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(mLauncher);
        final UserHandle user = android.os.Process.myUserHandle();
        final IconCache iconCache = LauncherAppState.getInstance(mLauncher).getIconCache();
        boolean quietMode = UserManagerCompat.getInstance(mLauncher).isQuietModeEnabled(user);
        for (ComponentKey key : mSearchResults) {
            // 只显示一行
            if (result.size() >= mNumAppsPerRow) {
                break;
            }
            AppInfo match = mAllAppsStore.getApp(key);
            if (match != null) {
                result.add(match);
            } else {
                for (LauncherActivityInfo info : launcherApps
                        .getActivityList(key.componentName.getPackageName(), user)) {
                    if (info.getComponentName().equals(key.componentName)) {
                        final AppInfo appInfo = new AppInfo(info, user, quietMode);
                        iconCache.getTitleAndIcon(appInfo, false);
                        result.add(appInfo);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void reset() {
        updateAdapterItems();
    }

}
