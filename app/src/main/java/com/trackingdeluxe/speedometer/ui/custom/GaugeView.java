

package com.trackingdeluxe.speedometer.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.trackingdeluxe.speedometer.R;
import com.trackingdeluxe.speedometer.data.models.SpeedRange;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class GaugeView extends View {

    private Paint needlePaint;
    private Path needlePath;
    private Paint needleScrewPaint;
    private float canvasCenterX;
    private float canvasCenterY;
    private float canvasWidth;
    private float canvasHeight;
    private float needleTailLength;
    private int backgroundCircleColor = 0;
    private float needleWidth;
    private float needleLength;
    private Paint rimPaint;
    private Paint rimShadowPaint;
    private Paint circlePaint;
    private List<Paint> paints;
    private List<SpeedRange> speedRanges;
    private Paint scalePaint;
    private RectF scaleRect;

    private int totalNicks = 120;
    private float degreesPerNick = totalNicks / 360;
    private float valuePerNick = 10;
    private float minValue = 0;
    private float maxValue = 1000;
    private boolean intScale = true;
    private float requestedLabelTextSize = 0;
    private float initialValue = 0;
    private float value = 0;
    private float needleValue = 0;
    private float needleStep;
    private float centerValue;
    private float labelRadius;
    private int majorNickInterval = 10;

    private Paint labelPaint;
    private long lastMoveTime;
    private boolean needleShadow = true;
    private int faceColor;
    private int scaleColor;
    private int needleColor;
    private Paint upperTextPaint;
    private Paint lowerTextPaint;

    private float requestedTextSize = 0;
    private String metricTypeText = "";
    private String speedText = "";
    private String speedValueText = "";
    private float textScaleFactor;

    private static final int REF_MAX_PORTRAIT_CANVAS_SIZE = 1080;

    public GaugeView(Context context) {
        super(context);
        initValues();
        initPaint();
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttrs(context, attrs);
        initValues();
        initPaint();
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyAttrs(context, attrs);
        initValues();
        initPaint();
    }

    private void applyAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugeView, 0, 0);
        totalNicks = a.getInt(R.styleable.GaugeView_totalNicks, totalNicks);
        degreesPerNick = 360.0f / totalNicks;
        valuePerNick = a.getFloat(R.styleable.GaugeView_valuePerNick, valuePerNick);
        majorNickInterval = a.getInt(R.styleable.GaugeView_majorNickInterval, 10);
        minValue = a.getFloat(R.styleable.GaugeView_minValue, minValue);
        maxValue = a.getFloat(R.styleable.GaugeView_maxValue, maxValue);
        intScale = a.getBoolean(R.styleable.GaugeView_intScale, intScale);
        initialValue = a.getFloat(R.styleable.GaugeView_initialValue, initialValue);
        requestedLabelTextSize = a.getFloat(R.styleable.GaugeView_labelTextSize, requestedLabelTextSize);
        faceColor = a.getColor(R.styleable.GaugeView_faceColor, Color.argb(0xff, 0xff, 0xff, 0xff));
        scaleColor = a.getColor(R.styleable.GaugeView_scaleColor, 0x9f004d0f);
        needleColor = a.getColor(R.styleable.GaugeView_needleColor, Color.RED);
        needleShadow = a.getBoolean(R.styleable.GaugeView_needleShadow, needleShadow);
        requestedTextSize = a.getFloat(R.styleable.GaugeView_textSize, requestedTextSize);
        metricTypeText = a.getString(R.styleable.GaugeView_upperText) == null ? metricTypeText : fromHtml(a.getString(R.styleable.GaugeView_upperText)).toString();
        speedText = a.getString(R.styleable.GaugeView_lowerText) == null ? speedText : fromHtml(a.getString(R.styleable.GaugeView_lowerText)).toString();
        a.recycle();
    }

    private void initValues() {
        float needleStepFactor = 3f;
        needleStep = needleStepFactor * valuePerDegree();
        centerValue = (minValue + maxValue) / 2;
        needleValue = value = initialValue;

        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        textScaleFactor = (float) widthPixels / (float) REF_MAX_PORTRAIT_CANVAS_SIZE;

        if (getResources().getBoolean(R.bool.landscape)) {
            int heightPixels = getResources().getDisplayMetrics().heightPixels;
            float portraitAspectRatio = (float) heightPixels / (float) widthPixels;
            textScaleFactor = textScaleFactor * portraitAspectRatio;
        }
    }

    private void initPaint() {

        setSaveEnabled(true);

        // Rim and shadow are based on the Vintage Thermometer:
        // http://mindtherobot.com/blog/272/android-custom-ui-making-a-vintage-thermometer/

        rimPaint = new Paint();
        rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        Paint rimCirclePaint = new Paint();
        rimCirclePaint.setAntiAlias(true);
        rimCirclePaint.setStyle(Paint.Style.STROKE);
        rimCirclePaint.setColor(Color.argb(0x4f, 0x33, 0x36, 0x33));
        rimCirclePaint.setStrokeWidth(0.005f);

        Paint facePaint = new Paint();
        facePaint.setAntiAlias(true);
        facePaint.setStyle(Paint.Style.FILL);
        facePaint.setColor(faceColor);

        rimShadowPaint = new Paint();
        rimShadowPaint.setStyle(Paint.Style.FILL);

        scalePaint = new Paint();
        scalePaint.setStyle(Paint.Style.STROKE);
        scalePaint.setAntiAlias(true);
        scalePaint.setColor(scaleColor);

        labelPaint = new Paint();
        labelPaint.setColor(scaleColor);
        labelPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.agency));
        labelPaint.setTextAlign(Paint.Align.CENTER);

        upperTextPaint = new Paint();
        upperTextPaint.setColor(scaleColor);
        upperTextPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.agency));
        upperTextPaint.setTextAlign(Paint.Align.CENTER);

        lowerTextPaint = new Paint();
        lowerTextPaint.setColor(scaleColor);
        lowerTextPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.agency));
        lowerTextPaint.setTextAlign(Paint.Align.CENTER);

        needlePaint = new Paint();
        needlePaint.setColor(needleColor);
        needlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        needlePaint.setAntiAlias(true);

        needlePath = new Path();

        needleScrewPaint = new Paint();
        needleScrewPaint.setColor(Color.BLACK);
        needleScrewPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawScale(canvas);
        drawLabels(canvas);
        canvas.save();
        canvas.rotate(scaleToCanvasDegrees(valueToDegrees(needleValue)), canvasCenterX, canvasCenterY);
        canvas.drawPath(needlePath, needlePaint);
        canvas.drawCircle(canvasCenterX, canvasCenterY, canvasWidth / 61f, needleScrewPaint);
        if (needsToMove()) {
            moveNeedle();
        }
        canvas.restore();
        drawCenterCircle(canvas);
        drawTexts(canvas);
    }

    private void drawCenterCircle(Canvas canvas) {
        circlePaint = new Paint();
        circlePaint.setColor(getContext().getResources().getColor(R.color.mainGreenColor));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePaint.setShader(new RadialGradient(
                canvasCenterX,
                canvasCenterY,
                canvasHeight / 6,
                new int[]{backgroundCircleColor, getContext().getResources().getColor(R.color.mainGreenColorLight)},
                new float[]{0.85f, 1f},
                Shader.TileMode.CLAMP));
        Paint stokePaint = new Paint();
        stokePaint.setColor(getContext().getResources().getColor(R.color.mainGreenColor));
        stokePaint.setStrokeWidth(5);
        stokePaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(canvasCenterX, canvasCenterY, canvasHeight / 6, circlePaint);
        canvas.drawCircle(canvasCenterX, canvasCenterY, canvasHeight / 6, stokePaint);
    }

    public void setSpeedRanges(List<SpeedRange> speedRanges) {
        paints = new LinkedList<>();
        for (SpeedRange range : speedRanges) {
            scalePaint = new Paint();
            scalePaint.setStyle(Paint.Style.STROKE);
            scalePaint.setAntiAlias(true);
            scalePaint.setColor(range.getRangeColor());
            scalePaint.setStrokeWidth(0.005f * canvasWidth);
            paints.add(scalePaint);
        }
        this.speedRanges = speedRanges;
        invalidate();
    }

    private void moveNeedle() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastMoveTime;
        int deltaTimeInterval = 5;
        if (deltaTime >= deltaTimeInterval) {
            if (Math.abs(value - needleValue) <= needleStep) {
                needleValue = value;
            } else {
                if (value > needleValue) {
                    needleValue += 2 * valuePerDegree();
                } else {
                    needleValue -= 2 * valuePerDegree();
                }
            }
            lastMoveTime = System.currentTimeMillis();
            postInvalidateDelayed(deltaTimeInterval);
        }
    }

    private void drawScale(Canvas canvas) {
        canvas.save();
        for (int i = 0; i < totalNicks; ++i) {
            float y1 = scaleRect.top;
            float y2 = y1 + (0.020f * canvasHeight);
            float y3 = y1 + (0.060f * canvasHeight);
            float y4 = y1 + (0.030f * canvasHeight);
            float value = nickToValue(i);
            if (value >= minValue && value <= maxValue) {
                Paint paint = getPaintByValue(value);
                canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y2, paint);
                if (i % majorNickInterval == 0) {
                    canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y3, paint);
                }
                if (i % (majorNickInterval / 2) == 0) {
                    canvas.drawLine(0.5f * canvasWidth, y1, 0.5f * canvasWidth, y4, paint);
                }
            }
            canvas.rotate(degreesPerNick, 0.5f * canvasWidth, 0.5f * canvasHeight);
        }
        canvas.restore();
    }

    private Paint getPaintByValue(float value) {
        if (paints != null) {
            for (SpeedRange speedRange : speedRanges) {
                if (value >= speedRange.getFrom() && value < speedRange.getTo()) {
                    return paints.get(speedRanges.indexOf(speedRange));
                }
            }
        }
        return scalePaint;
    }

    private void drawLabels(Canvas canvas) {
        for (int i = 0; i < totalNicks; i += majorNickInterval) {
            float value = nickToValue(i);
            if (value >= minValue && value <= maxValue) {
                float scaleAngle = i * degreesPerNick;
                float scaleAngleRads = (float) Math.toRadians(scaleAngle);
                float deltaX = labelRadius * (float) Math.sin(scaleAngleRads);
                float deltaY = labelRadius * (float) Math.cos(scaleAngleRads);
                String valueLabel;
                if (intScale) {
                    valueLabel = String.valueOf((int) value);
                } else {
                    valueLabel = String.valueOf(value);
                }
                drawTextCentered(valueLabel, canvasCenterX + deltaX, canvasCenterY - deltaY, labelPaint, canvas);
            }
        }
    }

    public void setTextColor(int color) {
        lowerTextPaint.setColor(color);
        upperTextPaint.setColor(color);
        invalidate();
    }

    public void applyTheme(boolean isLightTheme) {
        this.backgroundCircleColor = getContext().getResources().getColor(
                isLightTheme ? R.color.white : R.color.backgroundColor);
        invalidate();
    }

    public void setMetricTypeText(String metricTypeText) {
        this.metricTypeText = metricTypeText;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        circlePaint = null;
        canvasWidth = (float) w;
        canvasHeight = (float) h + 20;
        canvasCenterX = w / 2f;
        canvasCenterY = h / 2f;
        needleTailLength = canvasWidth / 12f;
        needleWidth = canvasWidth / 98f;
        needleLength = (canvasWidth / 2f) * 0.8f;
        needlePaint.setStrokeWidth(canvasWidth / 197f);
        if (needleShadow)
            needlePaint.setShadowLayer(canvasWidth / 123f, canvasWidth / 10000f, canvasWidth / 10000f, Color.GRAY);
        setNeedle();
        RectF rimRect = new RectF(canvasWidth * .05f, canvasHeight * .05f, canvasWidth * 0.95f, canvasHeight * 0.95f);
        rimPaint.setShader(new LinearGradient(canvasWidth * 0.40f, canvasHeight * 0.0f, canvasWidth * 0.60f, canvasHeight * 1.0f,
                Color.rgb(0xf0, 0xf5, 0xf0),
                Color.rgb(0x30, 0x31, 0x30),
                Shader.TileMode.CLAMP));
        float rimSize = 0.02f * canvasWidth;
        RectF faceRect = new RectF();
        faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
                rimRect.right - rimSize, rimRect.bottom - rimSize);
        rimShadowPaint.setShader(new RadialGradient(0.5f * canvasWidth, 0.5f * canvasHeight, faceRect.width() / 2.0f,
                new int[]{0x00000000, 0x00000500, 0x50000500},
                new float[]{0.96f, 0.96f, 0.99f},
                Shader.TileMode.MIRROR));
        scalePaint.setStrokeWidth(0.005f * canvasWidth);
        if (paints != null) {
            for (Paint paint : paints) {
                paint.setStrokeWidth(0.005f * canvasWidth);
            }
        }
        scalePaint.setTextSize(0.045f * canvasWidth);
        scalePaint.setTextScaleX(0.8f * canvasWidth);
        float scalePosition = 0.015f * canvasWidth;
        scaleRect = new RectF();
        scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
                faceRect.right - scalePosition, faceRect.bottom - scalePosition);
        labelRadius = (canvasCenterX - scaleRect.left) * 0.70f;
        float textSize;
        if (requestedLabelTextSize > 0) {
            textSize = requestedLabelTextSize * textScaleFactor;
        } else {
            textSize = canvasWidth / 16f;
        }
        labelPaint.setTextSize(textSize);
        upperTextPaint.setTextSize(canvasWidth / 14);
        lowerTextPaint.setTextSize(canvasWidth / 8);
        super.onSizeChanged(w, h, oldw, oldh);
    }


    private void drawTexts(Canvas canvas) {
        drawTextCentered(speedValueText, canvasCenterX, canvasCenterY - (canvasHeight / 20f), lowerTextPaint, canvas);
        drawTextCentered(metricTypeText, canvasCenterX, canvasCenterY + (canvasHeight / 20f), upperTextPaint, canvas);
    }


    private void setNeedle() {
        needlePath.reset();
        needlePath.moveTo(canvasCenterX - needleTailLength, canvasCenterY);
        needlePath.lineTo(canvasCenterX, canvasCenterY - (needleWidth / 2));
        needlePath.lineTo(canvasCenterX + needleLength, canvasCenterY);
        needlePath.lineTo(canvasCenterX, canvasCenterY + (needleWidth / 2));
        needlePath.lineTo(canvasCenterX - needleTailLength, canvasCenterY);
        needlePath.addCircle(canvasCenterX, canvasCenterY, canvasWidth / 49f, Path.Direction.CW);
        needlePath.close();
        needleScrewPaint.setShader(
                new RadialGradient(canvasCenterX, canvasCenterY, needleWidth / 2, Color.DKGRAY, Color.BLACK, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();
        size = Math.min(widthWithoutPadding, heightWithoutPadding);
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putFloat("value", value);
        bundle.putFloat("needleValue", needleValue);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            value = bundle.getFloat("value");
            needleValue = bundle.getFloat("needleValue");
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private float nickToValue(int nick) {
        float rawValue = ((nick < totalNicks / 2) ? nick : (nick - totalNicks)) * valuePerNick;
        return rawValue + centerValue;
    }

    private float valueToDegrees(float value) {
        // these are scale degrees, 0 is on top
        return ((value - centerValue) / valuePerNick) * degreesPerNick;
    }

    private float valuePerDegree() {
        return valuePerNick / degreesPerNick;
    }

    private float scaleToCanvasDegrees(float degrees) {
        return degrees - 90;
    }

    private boolean needsToMove() {
        return Math.abs(needleValue - value) > 0;
    }

    private void drawTextCentered(String text, float x, float y, Paint paint, Canvas canvas) {
        //float xPos = x - (paint.measureText(text)/2f);
        float yPos = (y - ((paint.descent() + paint.ascent()) / 2f));
        canvas.drawText(text, x, yPos, paint);
    }

    /**
     * Set gauge to value.
     *
     * @param value Value
     */
    public void setValue(float value) {
        needleValue = this.value = value;
        postInvalidate();
    }

    /**
     * Animate gauge to value.
     *
     * @param value Value
     */
    public void moveToValue(float value) {
        this.value = value;
        postInvalidate();
    }

    /**
     * Request a text size.
     *
     * @param size Size (pixels)
     * @see Paint#setTextSize(float);
     */
    @Deprecated
    public void setRequestedTextSize(float size) {
        setTextSize(size);
    }

    /**
     * Set a text size for the upper and lower text.
     * <p>
     * Size is in pixels at a screen width (max. canvas width/height) of 1080 and is scaled
     * accordingly at different resolutions. E.g. a value of 48 is unchanged at 1080 x 1920
     * and scaled down to 27 at 600 x 1024.
     *
     * @param size Size (relative pixels)
     * @see Paint#setTextSize(float);
     */
    public void setTextSize(float size) {
        requestedTextSize = size;
    }


    /**
     * Set the maximum scale value.
     *
     * @param value maximum value
     */
    public void setMaxValue(float value) {
        maxValue = value;
        initValues();
        invalidate();
    }

    /**
     * Set the total amount of nicks on a full 360 degree scale. Should be a multiple of majorNickInterval.
     *
     * @param nicks number of nicks
     */
    public void setTotalNicks(int nicks) {
        totalNicks = nicks;
        degreesPerNick = 360.0f / totalNicks;
        initValues();
        invalidate();
    }

    private static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    public void setSpeedValueText(@NotNull String speedValueText) {
        this.speedValueText = speedValueText;
        invalidate();
    }
}