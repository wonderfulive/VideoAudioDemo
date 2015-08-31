package cn.net.xyd.videoaudiodemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by Administrator on 2015/8/12 0012.
 */
public class VolumeView extends View {
    private int width;
    private int mY;
    private Paint mPaint;
    private int count = 0;
    private final int mStep = 8;
    private Rect mRect = new Rect();

    public VolumeView(Context context, int y) {
        super(context);
        width = this.getResources().getDisplayMetrics().widthPixels;
        mY = y;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
        mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
        mPaint.setStrokeWidth(3);// 画笔宽度
        mPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.parseColor("#555555"));
        mRect.set(0, 0, this.getWidth(), getHeight());
        canvas.drawRect(mRect, mPaint);
        for (int i = 0; i < count; i++) {
            switch (i) {
                case 0:
                    mPaint.setColor(Color.parseColor("#FFF1DD"));
                    break;
                case 1:
                    mPaint.setColor(Color.parseColor("#FFF1DD"));
                    break;
                case 2:
                    mPaint.setColor(Color.parseColor("#FFF1DD"));
                    break;
                case 3:
                    mPaint.setColor(Color.parseColor("#FFDDAA"));
                    break;
                case 4:
                    mPaint.setColor(Color.parseColor("#FFCF88"));
                    break;
                case 5:
                    mPaint.setColor(Color.parseColor("#FFBB55"));
                    break;
                case 6:
                    mPaint.setColor(Color.parseColor("#FFA011"));
                    break;
                case 7:
                    mPaint.setColor(Color.parseColor("#EE8F00"));
                    break;
                case 8:
                    mPaint.setColor(Color.parseColor("#FF3333"));
                    break;
                case 9:
                    mPaint.setColor(Color.parseColor("#EE0000"));
                    break;
                case 10:
                    mPaint.setColor(Color.parseColor("#CC0000"));
                    break;
                case 11:
                    mPaint.setColor(Color.RED);
                    break;
                case 12:
                    mPaint.setColor(Color.RED);
                    break;
                default:
                    mPaint.setColor(Color.parseColor("#CC0000"));
            }
            canvas.drawLine((width / 2) - (mStep * i), mY - 8 * i, (width / 2)
                    + (mStep * i), mY - 8 * i, mPaint);
        }

    }

    public void changed(int db) {
        count = db / 10;
  /*count = count*2;*/
        invalidate();
    }
}
