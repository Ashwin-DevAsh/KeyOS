package com.android.launcher3.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk;
import tech.DevAsh.Launcher.KioskUtilsKt;
import com.android.launcher3.AllAppsList;
import com.android.launcher3.AppInfo;
import com.android.launcher3.IconCache;
import com.android.launcher3.InstallShortcutReceiver;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherAppWidgetInfo;
import com.android.launcher3.LauncherSettings.Favorites;
import com.android.launcher3.SessionCommitReceiver;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.graphics.BitmapInfo;
import com.android.launcher3.graphics.LauncherIcons;
import com.android.launcher3.logging.FileLog;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;
import com.android.launcher3.util.FlagOp;
import com.android.launcher3.util.ItemInfoMatcher;
import com.android.launcher3.util.LongArrayMap;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.util.PackageUserKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Handles updates due to changes in package manager (app installed/updated/removed)
 * or when a user availability changes.
 */
public class PackageUpdatedTask extends BaseModelUpdateTask {

    private static final boolean DEBUG = false;
    private static final String TAG = "PackageUpdatedTask";

    public static final int OP_ADD = 1;
    public static final int OP_UPDATE = 2;
    public static final int OP_REMOVE = 3; // uninstalled
    public static final int OP_UNAVAILABLE = 4; // external media unmounted
    public static final int OP_SUSPEND = 5; // package suspended
    public static final int OP_UNSUSPEND = 6; // package unsuspended
    public static final int OP_USER_AVAILABILITY_CHANGE = 7; // user available/unavailable
    public static final int OP_RELOAD = 8; // clears cache

    private final int mOp;
    private final UserHandle mUser;
    private final String[] mPackages;

    public PackageUpdatedTask(int op, UserHandle user, String... packages) {
        mOp = op;
        mUser = user;
        mPackages = packages;
    }

    @Override
    public void execute(LauncherAppState app, BgDataModel dataModel, AllAppsList appsList) {
        final Context context = app.getContext();
        final IconCache iconCache = app.getIconCache();
        final String[] packages = mPackages;
        final int N = packages.length;
        FlagOp flagOp = FlagOp.NO_OP;
        final HashSet<String> packageSet = new HashSet<>(Arrays.asList(packages));
        ItemInfoMatcher matcher = ItemInfoMatcher.ofPackages(packageSet, mUser);
        switch (mOp) {
            case OP_ADD: {
                for (String aPackage : packages) {
                    if (Kiosk.INSTANCE.canShowApp(aPackage)) {
                        if (DEBUG) Log.d(TAG, "mAllAppsList.addPackage " + aPackage);
                        iconCache.updateIconsForPkg(aPackage, mUser);
                        if (FeatureFlags.LAUNCHER3_PROMISE_APPS_IN_ALL_APPS) {
                            appsList.removePackage(aPackage, Process.myUserHandle());
                        }
                        appsList.addPackage(context, aPackage, mUser);
                        if (!KioskUtilsKt.workspaceContains(dataModel, aPackage, Process.myUserHandle())) {
                            SessionCommitReceiver.queueAppIconAddition(context, aPackage, mUser);
                        }
                    }
                }
                flagOp = FlagOp.removeFlag(ShortcutInfo.FLAG_DISABLED_NOT_AVAILABLE);
                break;
            }
            case OP_UPDATE:
                for (String aPackage : packages) {
                    if (Kiosk.INSTANCE.canShowApp(aPackage)){
                        if (DEBUG) Log.d(TAG, "mAllAppsList.updatePackage " + aPackage);
                        iconCache.updateIconsForPkg(aPackage, mUser);
                        appsList.updatePackage(context, aPackage, mUser);
                        app.getWidgetCache().removePackage(aPackage, mUser);
                    }
                }
                flagOp = FlagOp.removeFlag(ShortcutInfo.FLAG_DISABLED_NOT_AVAILABLE);
                break;
            case OP_REMOVE: {
                for (String aPackage : packages) {
                    iconCache.removeIconsForPkg(aPackage, mUser);
                }
                // Fall through
            }
            case OP_UNAVAILABLE:
                for (String aPackage : packages) {
                    if (DEBUG)
                        Log.d(TAG, "mAllAppsList.removePackage " + aPackage);
                    appsList.removePackage(aPackage, mUser);
                    app.getWidgetCache().removePackage(aPackage, mUser);
                }
                flagOp = FlagOp.addFlag(ShortcutInfo.FLAG_DISABLED_NOT_AVAILABLE);
                break;
            case OP_SUSPEND:
            case OP_UNSUSPEND:
                flagOp = mOp == OP_SUSPEND ?
                        FlagOp.addFlag(ShortcutInfo.FLAG_DISABLED_SUSPENDED) :
                        FlagOp.removeFlag(ShortcutInfo.FLAG_DISABLED_SUSPENDED);
                if (DEBUG) Log.d(TAG, "mAllAppsList.(un)suspend " + N);
                appsList.updateDisabledFlags(matcher, flagOp);
                break;
            case OP_USER_AVAILABILITY_CHANGE:
                flagOp = UserManagerCompat.getInstance(context).isQuietModeEnabled(mUser)
                        ? FlagOp.addFlag(ShortcutInfo.FLAG_DISABLED_QUIET_USER)
                        : FlagOp.removeFlag(ShortcutInfo.FLAG_DISABLED_QUIET_USER);
                // We want to update all packages for this user.
                matcher = ItemInfoMatcher.ofUser(mUser);
                appsList.updateDisabledFlags(matcher, flagOp);
                break;
            case OP_RELOAD:
                if (DEBUG) Log.d(TAG, "mAllAppsList.reloadPackages");
                appsList.reloadPackages(context, mUser);
                break;
        }

        final ArrayList<AppInfo> addedOrModified = new ArrayList<>(appsList.added);
        final ArrayList<AppInfo> added = new ArrayList<>(appsList.added);
        appsList.added.clear();
        addedOrModified.addAll(appsList.modified);
        appsList.modified.clear();

        final ArrayList<AppInfo> removedApps = new ArrayList<>(appsList.removed);
        appsList.removed.clear();

        final ArrayMap<ComponentName, AppInfo> addedOrUpdatedApps = new ArrayMap<>();
        if (!addedOrModified.isEmpty() || mOp == OP_UPDATE) {
            scheduleCallbackTask((callbacks) -> callbacks.bindAppsAddedOrUpdated(addedOrModified));
            for (AppInfo ai : addedOrModified) {
                addedOrUpdatedApps.put(ai.componentName, ai);
            }
        }

        final LongArrayMap<Boolean> removedShortcuts = new LongArrayMap<>();

        // Update shortcut infos
        if (mOp == OP_ADD || flagOp != FlagOp.NO_OP) {
            final ArrayList<ShortcutInfo> updatedShortcuts = new ArrayList<>();
            final ArrayList<LauncherAppWidgetInfo> widgets = new ArrayList<>();

            // For system apps, package manager send OP_UPDATE when an app is enabled.
            final boolean isNewApkAvailable = mOp == OP_ADD || mOp == OP_UPDATE;
            synchronized (dataModel) {
                for (ItemInfo info : dataModel.itemsIdMap) {

//                    if(!Kiosk.INSTANCE.isAllowedPackage(info.getIntent().getPackage())){
//                        continue;
//                    }

                    if (info instanceof ShortcutInfo && mUser.equals(info.user)) {
                        ShortcutInfo si = (ShortcutInfo) info;
                        boolean infoUpdated = false;
                        boolean shortcutUpdated = false;

                        // Update shortcuts which use iconResource.
                        if ((si.iconResource != null)
                                && packageSet.contains(si.iconResource.packageName)) {
                            LauncherIcons li = LauncherIcons.obtain(context);
                            BitmapInfo iconInfo = li.createIconBitmap(si.iconResource);
                            li.recycle();
                            if (iconInfo != null) {
                                iconInfo.applyTo(si);
                                infoUpdated = true;
                            }
                        }

                        ComponentName cn = si.getTargetComponent();
                        if (cn != null && matcher.matches(si, cn)) {
                            AppInfo appInfo = addedOrUpdatedApps.get(cn);

                            if (si.hasStatusFlag(ShortcutInfo.FLAG_SUPPORTS_WEB_UI)) {
                                removedShortcuts.put(si.id, false);
                                if (mOp == OP_REMOVE) {
                                    continue;
                                }
                            }

                            if (si.isPromise() && isNewApkAvailable) {
                                boolean isTargetValid = true;
                                if (si.itemType == Favorites.ITEM_TYPE_DEEP_SHORTCUT) {
                                    List<ShortcutInfoCompat> shortcut = DeepShortcutManager
                                            .getInstance(context).queryForPinnedShortcuts(
                                                    cn.getPackageName(),
                                                    Arrays.asList(si.getDeepShortcutId()), mUser);
                                    if (shortcut.isEmpty()) {
                                        isTargetValid = false;
                                    } else {
                                        si.updateFromDeepShortcutInfo(shortcut.get(0), context);
                                        infoUpdated = true;
                                    }
                                } else if (!cn.getClassName().equals(IconCache.EMPTY_CLASS_NAME)) {
                                    isTargetValid = LauncherAppsCompat.getInstance(context)
                                            .isActivityEnabledForProfile(cn, mUser);
                                }

                                if (si.hasStatusFlag(ShortcutInfo.FLAG_AUTOINSTALL_ICON)) {
                                    // Auto install icon
                                    if (!isTargetValid) {
                                        // Try to find the best match activity.
                                        Intent intent = new PackageManagerHelper(context)
                                                .getAppLaunchIntent(cn.getPackageName(), mUser);
                                        if (intent != null) {
                                            cn = intent.getComponent();
                                            appInfo = addedOrUpdatedApps.get(cn);
                                        }

                                        if (intent != null && appInfo != null) {
                                            si.intent = intent;
                                            si.status = ShortcutInfo.DEFAULT;
                                            infoUpdated = true;
                                        } else if (si.hasPromiseIconUi()) {
                                            removedShortcuts.put(si.id, true);
                                            continue;
                                        }
                                    }
                                } else if (!isTargetValid) {
                                    removedShortcuts.put(si.id, true);
                                    FileLog.e(TAG, "Restored shortcut no longer valid " + si.intent);
                                    continue;
                                } else {
                                    si.status = ShortcutInfo.DEFAULT;
                                    infoUpdated = true;
                                }
                            }

                            if (isNewApkAvailable &&
                                    si.itemType == Favorites.ITEM_TYPE_APPLICATION) {
                                iconCache.getTitleAndIcon(si, si.usingLowResIcon);
                                infoUpdated = true;
                            }

                            int oldRuntimeFlags = si.runtimeStatusFlags;
                            si.runtimeStatusFlags = flagOp.apply(si.runtimeStatusFlags);
                            if (si.runtimeStatusFlags != oldRuntimeFlags) {
                                shortcutUpdated = true;
                            }
                        }

                        if (infoUpdated || shortcutUpdated) {
                            updatedShortcuts.add(si);
                        }
                        if (infoUpdated) {
                            getModelWriter().updateItemInDatabase(si);
                        }
                    } else if (info instanceof LauncherAppWidgetInfo && isNewApkAvailable) {
                        LauncherAppWidgetInfo widgetInfo = (LauncherAppWidgetInfo) info;
                        if (mUser.equals(widgetInfo.user)
                                && widgetInfo.hasRestoreFlag(LauncherAppWidgetInfo.FLAG_PROVIDER_NOT_READY)
                                && packageSet.contains(widgetInfo.providerName.getPackageName())) {
                            widgetInfo.restoreStatus &=
                                    ~LauncherAppWidgetInfo.FLAG_PROVIDER_NOT_READY &
                                            ~LauncherAppWidgetInfo.FLAG_RESTORE_STARTED;

                            // adding this flag ensures that launcher shows 'click to setup'
                            // if the widget has a config activity. In case there is no config
                            // activity, it will be marked as 'restored' during bind.
                            widgetInfo.restoreStatus |= LauncherAppWidgetInfo.FLAG_UI_NOT_READY;

                            widgets.add(widgetInfo);
                            getModelWriter().updateItemInDatabase(widgetInfo);
                        }
                    }
                }
            }

            InstallShortcutReceiver.installNewAppShortcuts(context, added);

            bindUpdatedShortcuts(updatedShortcuts, mUser);
            if (!removedShortcuts.isEmpty()) {
                deleteAndBindComponentsRemoved(ItemInfoMatcher.ofItemIds(removedShortcuts, false));
            }

            if (!widgets.isEmpty()) {
                scheduleCallbackTask(callbacks -> callbacks.bindWidgetsRestored(widgets));
            }
        }

        final HashSet<String> removedPackages = new HashSet<>();
        final HashSet<ComponentName> removedComponents = new HashSet<>();
        if (mOp == OP_REMOVE) {
            // Mark all packages in the broadcast to be removed
            Collections.addAll(removedPackages, packages);

            // No need to update the removedComponents as
            // removedPackages is a super-set of removedComponents
        } else if (mOp == OP_UPDATE) {
            // Mark disabled packages in the broadcast to be removed
            final LauncherAppsCompat launcherApps = LauncherAppsCompat.getInstance(context);
            for (String aPackage : packages) {
                if (!launcherApps.isPackageEnabledForProfile(aPackage, mUser)) {
                    removedPackages.add(aPackage);
                }
            }

            // Update removedComponents as some components can get removed during package update
            for (AppInfo info : removedApps) {
                removedComponents.add(info.componentName);
            }
        }

        if (!removedPackages.isEmpty() || !removedComponents.isEmpty()) {
            ItemInfoMatcher removeMatch = ItemInfoMatcher.ofPackages(removedPackages, mUser)
                    .or(ItemInfoMatcher.ofComponents(removedComponents, mUser))
                    .and(ItemInfoMatcher.ofItemIds(removedShortcuts, true));
            deleteAndBindComponentsRemoved(removeMatch);

            // Remove any queued items from the install queue
            InstallShortcutReceiver.removeFromInstallQueue(context, removedPackages, mUser);
        }

        if (!removedApps.isEmpty()) {
            // Remove corresponding apps from All-Apps
            scheduleCallbackTask(callbacks -> callbacks.bindAppInfosRemoved(removedApps));
        }

        if (Utilities.ATLEAST_OREO && mOp == OP_ADD) {
            for (String aPackage : packages) {
                if(Kiosk.INSTANCE.canShowApp(aPackage)){
                    dataModel.widgetsModel.update(app, new PackageUserKey(aPackage, mUser));
                }
            }
            bindUpdatedWidgets(dataModel);
        }
    }
}
