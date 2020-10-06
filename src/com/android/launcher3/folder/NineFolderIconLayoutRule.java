package com.android.launcher3.folder;


public class NineFolderIconLayoutRule {

    public static final int MAX_NUM_ITEMS_IN_PREVIEW = 9;

    private static final float MIN_SCALE = 0.48f;
    private static final float MAX_SCALE = 0.58f;


    private float[] mTmpPoint = new float[2];

    private float mAvailableSpace;
    private float mIconSize;
    private boolean mIsRtl;
    private float mBaselineIconScale;

    public void init(int availableSpace, float intrinsicIconSize, boolean rtl) {
        mAvailableSpace = availableSpace;
        mIconSize = intrinsicIconSize;
        mIsRtl = rtl;
        mBaselineIconScale = availableSpace / (intrinsicIconSize * 1f);
    }

    public PreviewItemDrawingParams computePreviewItemDrawingParams(int index,
            int curNumItems, PreviewItemDrawingParams params) {

        float totalScale = scaleForItem(curNumItems);
        float transX;
        float transY;
        float overlayAlpha = 0;

        // Items beyond those displayed in the preview are animated to the center
        if (index >= MAX_NUM_ITEMS_IN_PREVIEW) {
            transX = transY = mAvailableSpace / 2 - (mIconSize * totalScale) / 2;
        } else {
            getPosition(index, curNumItems, mTmpPoint);
            transX = mTmpPoint[0];
            transY = mTmpPoint[1];
        }

        if (params == null) {
            params = new PreviewItemDrawingParams(transX, transY, totalScale, overlayAlpha);
        } else {
            params.update(transX, transY, totalScale);
            params.overlayAlpha = overlayAlpha;
        }
        return params;
    }

    private void getPosition(int index, int curNumItems, float[] result) {
        float iconSize = (mIconSize * scaleForItem(curNumItems));
        int row = index / 3;
        int col = index % 3;
        float x, y;
        float padding = getPadding();
        float step = (mAvailableSpace - 2 * padding) / 3;
        x = col * step + step / 2 - iconSize / 2;
        y = row * step + step / 2 - iconSize / 2;
        result[0] = x + padding;
        result[1] = y + padding;
    }

    public float getPadding() {
        return mIconSize / 12;
    }

    public float scaleForItem(int numItems) {
        // Scale is determined by the number of items in the preview.
        float scale;
        if(numItems == 1) {
            scale = MAX_SCALE;
        } else {
            scale = (MIN_SCALE / 2);
        }

        return scale * mBaselineIconScale;
    }

    public float getIconSize() {
        return mIconSize;
    }

    public int maxNumItems() {
        return MAX_NUM_ITEMS_IN_PREVIEW;
    }

    public boolean clipToBackground() {
        return true;
    }

    public boolean hasEnterExitIndices() {
        return false;
    }

    public int getExitIndex() {
        return 0;
    }

    public int getEnterIndex() {
        return 0;
    }
}
