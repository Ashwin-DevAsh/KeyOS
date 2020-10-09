package com.google.android.apps.nexuslauncher.qsb;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import tech.DevAsh.Launcher.LawnchairPreferences;
import tech.DevAsh.Launcher.colors.ColorEngine;
import tech.DevAsh.Launcher.colors.ColorEngine.ResolveInfo;
import tech.DevAsh.Launcher.colors.ColorEngine.Resolvers;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.google.android.apps.nexuslauncher.search.SearchThread;
import org.jetbrains.annotations.NotNull;

public class AllAppsQsbLayout extends AbstractQsbLayout implements o,
        ColorEngine.OnColorChangeListener {

    private final k Ds;
    private final int Dt;
    private int mShadowAlpha;
    private Bitmap Dv;
    private boolean mUseFallbackSearch;
    private FallbackAppsSearchView mFallback;
    public float Dy;
    private TextView mHint;
    boolean mDoNotRemoveFallback;
    private LawnchairPreferences prefs;
    private int mForegroundColor;

    private final boolean mLowPerformanceMode;

    public AllAppsQsbLayout(Context context) {
        this(context, null);
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsQsbLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mShadowAlpha = 0;
        setOnClickListener(this);
        this.Ds = k.getInstance(context);
        this.Dt = getResources().getDimensionPixelSize(R.dimen.qsb_margin_top_adjusting);
        this.Dy = getResources().getDimensionPixelSize(R.dimen.all_apps_search_vertical_offset);
        setClipToPadding(false);
        prefs = LawnchairPreferences.Companion.getInstanceNoCreate();

        mLowPerformanceMode = prefs.getLowPerformanceMode();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
//        mHint = findViewById(R.id.qsb_hint);
    }

    public void setInsets(Rect rect) {
        c(Utilities.getDevicePrefs(getContext()));
        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        mlp.topMargin = getTopMargin(rect);
        requestLayout();
//        if (mActivity.getDeviceProfile().isVerticalBarLayout()) {
////            mActivity.getAllAppsController().setScrollRangeDelta(0);
//        } else {
////            float delta = HotseatQsbWidget.getBottomMargin(mActivity, false) + Dy;
//            LawnchairPreferences prefs = LawnchairPreferences.Companion.getInstance(getContext());
//            if (!prefs.getDockHide()) {
//                delta += mlp.height + mlp.topMargin;
////                if (!prefs.getDockSearchBar()) {
//                    delta -= mlp.height;
//                    delta -= mlp.topMargin;
//                    delta -= mlp.bottomMargin;
//                    delta += Dy;
////                }
//            } else {
//                delta -= mActivity.getResources().getDimensionPixelSize(R.dimen.vertical_drag_handle_size);
//            }
//            mActivity.getAllAppsController().setScrollRangeDelta(Math.round(delta));
//        }
    }

    public int getTopMargin(Rect rect) {
        return Math.max(Math.round(-this.Dy), rect.top - this.Dt);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ColorEngine.getInstance(getContext())
                .addColorChangeListeners(this, Resolvers.ALLAPPS_QSB_BG);
        dN();
        Ds.a(this);
    }

    @Override
    public void onColorChange(@NotNull ResolveInfo resolveInfo) {
        if (resolveInfo.getKey().equals(Resolvers.ALLAPPS_QSB_BG)) {
            mForegroundColor = resolveInfo.getForegroundColor();
            ay(resolveInfo.getColor());
            az(this.Dc);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }



    @Override
    protected boolean logoCanOpenFeed() {
        return super.logoCanOpenFeed() && prefs.getAllAppsGlobalSearch();
    }


    protected void onDetachedFromWindow() {
        ColorEngine.getInstance(getContext())
                .removeColorChangeListeners(this, Resolvers.ALLAPPS_QSB_BG);
        super.onDetachedFromWindow();
        Ds.b(this);
    }

    @Override
    public void startSearch(String str, int i) {

    }

    protected final int aA(int i) {
//        if (this.mActivity.getDeviceProfile().isVerticalBarLayout()) {
//            return (i - this.mSearchView.getActiveRecyclerView().getPaddingLeft()) - this.mSearchView
//                    .getActiveRecyclerView().getPaddingRight();
//        }
        View view = this.mActivity.getHotseat().getLayout();
        return (i - view.getPaddingLeft()) - view.getPaddingRight();
    }



    public final void dM() {
        dN();
        invalidate();
    }

    private void dN() {
        az(this.Dc);
        h(this.Ds.micStrokeWidth());
        this.Dh = this.Ds.hintIsForAssistant();
        mUseTwoBubbles = useTwoBubbles();
        setHintText(this.Ds.hintTextValue(), this.mHint);
        dH();
    }


    public final void l(String str) {
        startSearch(str, 0);
    }







    public final void resetSearch() {
        setShadowAlpha(0);
        if (mUseFallbackSearch) {
            resetFallbackView();
        } else if (!mDoNotRemoveFallback) {
            removeFallbackView();
        }
    }

    private void ensureFallbackView() {
        if (mFallback == null) {
            setOnClickListener(null);
            mFallback = (FallbackAppsSearchView) this.mActivity.getLayoutInflater()
                    .inflate(R.layout.all_apps_google_search_fallback, this, false);
            mFallback.DJ = this;
            mFallback.DI.initialize(new SearchThread(mFallback.getContext()), mFallback,
                    Launcher.getLauncher(mFallback.getContext()), mFallback);
            addView(this.mFallback);
            mFallback.setTextColor(mForegroundColor);
        }
    }

    private void removeFallbackView() {
        if (mFallback != null) {
            mFallback.clearSearchResult();
            setOnClickListener(this);
            removeView(mFallback);
            mFallback = null;
        }
    }

    private void resetFallbackView() {
        if (mFallback != null) {
            mFallback.reset();
            mFallback.clearSearchResult();
        }
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        View view = (View) getParent();
        setTranslationX((float) ((view.getPaddingLeft() + (
                (((view.getWidth() - view.getPaddingLeft()) - view.getPaddingRight()) - (i3 - i))
                        / 2)) - i));
    }

    public void draw(Canvas canvas) {
        if (this.mShadowAlpha > 0) {
            if (this.Dv == null) {
                this.Dv = c(
                        getResources().getDimension(R.dimen.hotseat_qsb_scroll_shadow_blur_radius),
                        getResources().getDimension(R.dimen.hotseat_qsb_scroll_key_shadow_offset),
                        0, true);
            }
            this.mShadowHelper.paint.setAlpha(this.mShadowAlpha);
            a(this.Dv, canvas);
            this.mShadowHelper.paint.setAlpha(255);
        }
        super.draw(canvas);
    }

    final void setShadowAlpha(int i) {
        i = Utilities.boundToRange(i, 0, 255);
        if (this.mShadowAlpha != i) {
            this.mShadowAlpha = i;
            invalidate();
        }
    }

    protected final boolean dK() {
        if (this.mFallback != null) {
            return false;
        }
        return super.dK();
    }

    protected final void c(SharedPreferences sharedPreferences) {
        if (mUseFallbackSearch) {
            removeFallbackView();
            this.mUseFallbackSearch = false;
            if (this.mUseFallbackSearch) {
                ensureFallbackView();
            }
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }



    @Override
    protected void clearMainPillBg(Canvas canvas) {
        if (!mLowPerformanceMode && mClearBitmap != null) {
            drawPill(mClearShadowHelper, mClearBitmap, canvas);
        }
    }

    @Override
    protected void clearPillBg(Canvas canvas, int left, int top, int right) {
        if (!mLowPerformanceMode && mClearBitmap != null) {
            mClearShadowHelper.draw(mClearBitmap, canvas, left, top, right);
        }
    }

    @Override
    public void onClick(View v) {

    }
}
