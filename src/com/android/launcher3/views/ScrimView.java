/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.launcher3.views;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static androidx.core.graphics.ColorUtils.compositeColors;
import static androidx.core.graphics.ColorUtils.setAlphaComponent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.LauncherStateManager;
import com.android.launcher3.LauncherStateManager.StateListener;
import com.android.launcher3.R;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.uioverrides.WallpaperColorInfo.OnChangeListener;
import com.android.launcher3.util.Themes;

/**
 * Simple scrim which draws a flat color
 */
public class ScrimView extends View implements Insettable, OnChangeListener,
        AccessibilityStateChangeListener, StateListener {

    protected final Launcher mLauncher;
    private final WallpaperColorInfo mWallpaperColorInfo;
    private final AccessibilityManager mAM;
    protected int mEndScrim;

    protected float mMaxScrimAlpha;

    protected float mProgress = 1;
    protected int mScrimColor;

    protected int mCurrentFlatColor;
    protected int mEndFlatColor;
    protected int mEndFlatColorAlpha;

    private final RectF mHitRect = new RectF();

    public ScrimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLauncher = Launcher.getLauncher(context);
        mWallpaperColorInfo = WallpaperColorInfo.getInstance(context);
        mEndScrim = Themes.getAttrColor(context, R.attr.allAppsScrimColor);

        mMaxScrimAlpha = 0.7f;

        mAM = (AccessibilityManager) context.getSystemService(ACCESSIBILITY_SERVICE);
        setFocusable(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWallpaperColorInfo.addOnChangeListener(this);
        onExtractedColorsChanged(mWallpaperColorInfo);

        mAM.addAccessibilityStateChangeListener(this);
        onAccessibilityStateChanged(mAM.isEnabled());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWallpaperColorInfo.removeOnChangeListener(this);
        mAM.removeAccessibilityStateChangeListener(this);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        mScrimColor = wallpaperColorInfo.getMainColor();
        mEndFlatColor = compositeColors(mEndScrim, setAlphaComponent(
                mScrimColor, Math.round(mMaxScrimAlpha * 255)));
        mEndFlatColorAlpha = Color.alpha(mEndFlatColor);
        updateColors();
        invalidate();
    }

    public void setProgress(float progress) {
        if (mProgress != progress) {
            mProgress = progress;
            updateColors();
            invalidate();
        }
    }

    public void reInitUi() { }

    protected void updateColors() {
        mCurrentFlatColor = mProgress >= 1 ? 0 : setAlphaComponent(
                mEndFlatColor, Math.round((1 - mProgress) * mEndFlatColorAlpha));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCurrentFlatColor != 0) {
            canvas.drawColor(mCurrentFlatColor);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean value = super.onTouchEvent(event);
        return value;
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
        LauncherStateManager stateManager = mLauncher.getStateManager();
        stateManager.removeStateListener(this);

        if (enabled) {
            stateManager.addStateListener(this);
            onStateSetImmediately(mLauncher.getStateManager().getState());
        } else {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }
    }

    @Override
    public void onFocusChanged(boolean gainFocus, int direction,
            Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public void setInsets(Rect insets) {

    }

    @Override
    public void onStateSetImmediately(LauncherState state) {

    }

    @Override
    public void onStateTransitionStart(LauncherState toState) {

    }

    @Override
    public void onStateTransitionComplete(LauncherState finalState) {

    }
}
