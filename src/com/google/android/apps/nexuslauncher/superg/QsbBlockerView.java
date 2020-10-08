package com.google.android.apps.nexuslauncher.superg;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import ch.deletescape.lawnchair.LawnchairAppKt;
import ch.deletescape.lawnchair.LawnchairUtilsKt;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.Workspace.OnStateChangeListener;
import com.android.launcher3.anim.AnimatorSetBuilder;
import org.jetbrains.annotations.Nullable;

/**
 * A simple view used to show the region blocked by QSB during drag and drop.
 */
public class QsbBlockerView extends FrameLayout implements OnStateChangeListener, View.OnLongClickListener, View.OnClickListener {
    public static final Property<QsbBlockerView, Integer> QSB_BLOCKER_VIEW_ALPHA = new QsbBlockerViewAlpha(Integer.TYPE, "bgAlpha");
    private int mState = 0;
    private View mView;

    private BubbleTextView mDummyBubbleTextView;

    private final Paint mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public QsbBlockerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBgPaint.setColor(Color.WHITE);
        mBgPaint.setAlpha(0);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mDummyBubbleTextView = findViewById(R.id.dummyBubbleTextView);
        mDummyBubbleTextView.setTag(new ItemInfo() {
            @Override
            public ComponentName getTargetComponent() {
                return new ComponentName(getContext(), "");
            }
        });
        mDummyBubbleTextView.setContentDescription("");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mView != null && mState == 2) {
            Launcher launcher = LawnchairUtilsKt.getLauncherOrNull(getContext());
            int size;
            if (launcher != null) {
                DeviceProfile deviceProfile = launcher.getDeviceProfile();
                if (launcher.useVerticalBarLayout()) {
                    size = ((MeasureSpec.getSize(widthMeasureSpec) / deviceProfile.inv.numColumns)
                            - deviceProfile.iconSizePx) / 2;
                } else {
                    size = 0;
                }
            } else {
                size = getResources().getDimensionPixelSize(R.dimen.smartspace_preview_widget_margin);
            }
            LayoutParams layoutParams = (LayoutParams) mView.getLayoutParams();
            layoutParams.leftMargin = layoutParams.rightMargin = size;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void prepareStateChange(AnimatorSetBuilder builder) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(mBgPaint);
    }




    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(0, 0, 0, 0);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        return true;
    }

    static class QsbBlockerViewAlpha extends Property<QsbBlockerView, Integer> {

        public QsbBlockerViewAlpha(Class<Integer> type, String name) {
            super(type, name);
        }

        @Override
        public void set(QsbBlockerView qsbBlockerView, Integer num) {
            qsbBlockerView.mBgPaint.setAlpha(num);
            qsbBlockerView.setWillNotDraw(num == 0);
            qsbBlockerView.invalidate();
        }

        @Override
        public Integer get(QsbBlockerView obj) {
            return obj.mBgPaint.getAlpha();
        }

    }
}