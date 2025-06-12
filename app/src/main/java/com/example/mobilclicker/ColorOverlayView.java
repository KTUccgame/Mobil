package com.example.mobilclicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ColorOverlayView extends View {

    private Point crosshairPoint;
    private Rect boundingBox;

    private Paint boxPaint;
    private Paint crosshairPaint;

    public ColorOverlayView(Context context) {
        super(context);
        init();
    }

    public ColorOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(6f);

        crosshairPaint = new Paint();
        crosshairPaint.setColor(Color.RED);
        crosshairPaint.setStyle(Paint.Style.STROKE);
        crosshairPaint.setStrokeWidth(4f);
    }

    public void setCrosshairPoint(Point point) {
        this.crosshairPoint = point;
        invalidate();
    }

    public void setBoundingBox(Rect rect) {
        this.boundingBox = rect;
        invalidate();
    }

    public void clear() {
        this.crosshairPoint = null;
        this.boundingBox = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (boundingBox != null) {
            canvas.drawRect(boundingBox, boxPaint);
        }

        if (crosshairPoint != null) {
            float cx = crosshairPoint.x;
            float cy = crosshairPoint.y;
            float size = 30f;

            // Draw simple crosshair lines centered on the point
            canvas.drawLine(cx - size, cy, cx + size, cy, crosshairPaint);
            canvas.drawLine(cx, cy - size, cx, cy + size, crosshairPaint);
        }
    }
}
