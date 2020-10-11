package com.google.android.apps.nexuslauncher.superg;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import tech.DevAsh.Launcher.KioskUtilsKt;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;

import java.util.Locale;

public class DateWidgetView extends LinearLayout implements TextWatcher {


    public DateWidgetView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        init();
    }

    private void init() {
        Locale locale = Locale.getDefault();
        if (locale != null && Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Launcher launcher = KioskUtilsKt.getLauncherOrNull(getContext());
        int marginEnd;
        if (launcher != null) {
            DeviceProfile deviceProfile = Launcher.getLauncher(getContext()).getDeviceProfile();
            int size = MeasureSpec.getSize(widthMeasureSpec) / deviceProfile.inv.numColumns;
            marginEnd = (size - deviceProfile.iconSizePx) / 2;


        } else {
            marginEnd = getResources().getDimensionPixelSize(R.dimen.smartspace_preview_widget_margin);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void setMarginEnd(View view, int marginEnd) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.setMarginEnd(marginEnd);
        layoutParams.resolveLayoutDirection(layoutParams.getLayoutDirection());
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        update();
    }

    private void update() {
    }
}