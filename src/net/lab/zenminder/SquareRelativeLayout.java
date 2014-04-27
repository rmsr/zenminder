package net.lab.zenminder;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import android.view.View.MeasureSpec;

public class SquareRelativeLayout extends RelativeLayout
{
    private static final String TAG = "ZenMinder.SquareRelativeLayout";

    public SquareRelativeLayout(Context context) { super(context); }
    public SquareRelativeLayout(Context context, AttributeSet attrs) { super(context, attrs); }
    public SquareRelativeLayout(Context context, AttributeSet attrs, int style) { super(context, attrs, style); }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int size;
        if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) {
            size = widthSize;
        } else if (heightMode == MeasureSpec.EXACTLY && heightSize > 0) {
            size = heightSize;
        } else {
            size = widthSize < heightSize ? widthSize : heightSize;
        }

        int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(finalMeasureSpec, finalMeasureSpec);
    }
}
