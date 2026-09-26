package com.retrobrightness;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SliderActivity extends Activity {

    private RetroSliderView sliderView;
    private SoundEngine soundEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.BLACK);

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        soundEngine = new SoundEngine();

        sliderView = new RetroSliderView(this);

        setContentView(sliderView);
    }

    @Override
    protected void onDestroy() {

        if (soundEngine != null) {
            soundEngine.releaseResources();
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private class RetroSliderView extends View {

        private final Paint paint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final Paint shadowPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        private final RectF body =
                new RectF();

        private final RectF channel =
                new RectF();

        private final RectF amber =
                new RectF();

        private float brightness;

        private boolean dragging = false;

        private final float density;

        RetroSliderView(Context context) {
            super(context);

            density =
                    getResources()
                            .getDisplayMetrics()
                            .density;

            setBackgroundColor(
                    Color.rgb(
                            18,
                            16,
                            14
                    )
            );

            brightness =
                    readBrightness();

            setFocusable(true);
            setFocusableInTouchMode(true);
        }

        private float readBrightness() {

            try {

                int value =
                        Settings.System.getInt(
                                getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS,
                                128
                        );

                return Math.max(
                        0f,
                        Math.min(
                                1f,
                                (value - 1f) / 254f
                        )
                );

            } catch (Exception ignored) {

                return 0.5f;
            }
        }

        private void writeBrightness(
                float level) {

            if (!Settings.System.canWrite(
                    SliderActivity.this)) {

                requestWritePermission();

                return;
            }

            int value =
                    Math.round(
                            1f
                                    + level * 254f
                    );

            value =
                    Math.max(
                            1,
                            Math.min(
                                    255,
                                    value
                            )
                    );

            try {

                ContentResolver resolver =
                        getContentResolver();

                Settings.System.putInt(
                        resolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                );

                Settings.System.putInt(
                        resolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        value
                );

            } catch (SecurityException ignored) {
            }
        }

        private void requestWritePermission() {

            Intent intent =
                    new Intent(
                            Settings.ACTION_MANAGE_WRITE_SETTINGS,
                            Uri.parse(
                                    "package:"
                                            + getPackageName()
                    )
            );

            startActivity(intent);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            float width =
                    getWidth();

            float height =
                    getHeight();

            float controlWidth =
                    Math.min(
                            width * 0.88f,
                            760f * density
                    );

            float controlHeight =
                    120f * density;

            float left =
                    (width - controlWidth)
                            / 2f;

            float top =
                    (height - controlHeight)
                            / 2f;

            body.set(
                    left,
                    top,
                    left + controlWidth,
                    top + controlHeight
            );

            drawBody(canvas);
            drawChannel(canvas);
            drawAmber(canvas);
            drawKnob(canvas);
            drawSunSymbols(canvas);
        }

        private void drawBody(Canvas canvas) {

            float radius =
                    body.height() / 2f;

            shadowPaint.setColor(
                    Color.argb(
                            170,
                            0,
                            0,
                            0
                    )
            );

            RectF shadow =
                    new RectF(body);

            shadow.offset(
                    0,
                    9f * density
            );

            canvas.drawRoundRect(
                    shadow,
                    radius,
                    radius,
                    shadowPaint
            );

            LinearGradient metal =
                    new LinearGradient(
                            0,
                            body.top,
                            0,
                            body.bottom,
                            new int[]{
                                    Color.rgb(
                                            166,
                                            153,
                                            132
                                    ),
                                    Color.rgb(
                                            241,
                                            229,
                                            207
                                    ),
                                    Color.rgb(
                                            198,
                                            184,
                                            160
                                    ),
                                    Color.rgb(
                                            132,
                                            119,
                                            101
                                    )
                            },
                            null,
                            Shader.TileMode.CLAMP
                    );

            paint.setShader(metal);

            canvas.drawRoundRect(
                    body,
                    radius,
                    radius,
                    paint
            );

            paint.setShader(null);

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(
                    2f * density
            );

            paint.setColor(
                    Color.rgb(
                            255,
                            248,
                            232
                    )
            );

            RectF highlight =
                    new RectF(
                            body.left
                                    + 2f * density,
                            body.top
                                    + 2f * density,
                            body.right
                                    - 2f * density,
                            body.bottom
                                    - 2f * density
                    );

            canvas.drawRoundRect(
                    highlight,
                    radius,
                    radius,
                    paint
            );

            paint.setStyle(
                    Paint.Style.FILL
            );
        }

        private void drawChannel(Canvas canvas) {

            float channelLeft =
                    body.left
                            + 82f * density;

            float channelRight =
                    body.right
                            - 82f * density;

            float channelHeight =
                    25f * density;

            float centerY =
                    body.centerY();

            channel.set(
                    channelLeft,
                    centerY
                            - channelHeight / 2f,
                    channelRight,
                    centerY
                            + channelHeight / 2f
            );

            paint.setColor(
                    Color.rgb(
                            48,
                            39,
                            31
                    )
            );

            canvas.drawRoundRect(
                    channel,
                    channelHeight / 2f,
                    channelHeight / 2f,
                    paint
            );

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(
                    2f * density
            );

            paint.setColor(
                    Color.rgb(
                            91,
                            77,
                            62
                    )
            );

            canvas.drawRoundRect(
                    channel,
                    channelHeight / 2f,
                    channelHeight / 2f,
                    paint
            );

            paint.setStyle(
                    Paint.Style.FILL
            );
        }

        private void drawAmber(Canvas canvas) {

            float start =
                    channel.left
                            + 5f * density;

            float end =
                    channel.right
                            - 5f * density;

            float x =
                    start
                            + (end - start)
                            * brightness;

            amber.set(
                    start,
                    channel.top
                            + 7f * density,
                    Math.max(
                            start + 4f * density,
                            x
                    ),
                    channel.bottom
                            - 7f * density
            );

            LinearGradient amberGradient =
                    new LinearGradient(
                            0,
                            amber.top,
                            0,
                            amber.bottom,
                            new int[]{
                                    Color.rgb(
                                            122,
                                            62,
                                            5
                                    ),
                                    Color.rgb(
                                            226,
                                            146,
                                            35
                                    ),
                                    Color.rgb(
                                            157,
                                            78,
                                            7
                                    )
                            },
                            null,
                            Shader.TileMode.CLAMP
                    );

            paint.setShader(
                    amberGradient
            );

            canvas.drawRoundRect(
                    amber,
                    amber.height() / 2f,
                    amber.height() / 2f,
                    paint
            );

            paint.setShader(null);
        }

        private void drawKnob(Canvas canvas) {

            float start =
                    channel.left
                            + 5f * density;

            float end =
                    channel.right
                            - 5f * density;

            float x =
                    start
                            + (end - start)
                            * brightness;

            float radius =
                    30f * density;

            shadowPaint.setColor(
                    Color.argb(
                            180,
                            0,
                            0,
                            0
                    )
            );

            canvas.drawCircle(
                    x + 3f * density,
                    channel.centerY()
                            + 5f * density,
                    radius,
                    shadowPaint
            );

            LinearGradient metal =
                    new LinearGradient(
                            x - radius,
                            channel.centerY()
                                    - radius,
                            x + radius,
                            channel.centerY()
                                    + radius,
                            new int[]{
                                    Color.rgb(
                                            255,
                                            250,
                                            238
                                    ),
                                    Color.rgb(
                                            209,
                                            198,
                                            181
                                    ),
                                    Color.rgb(
                                            111,
                                            101,
                                            89
                                    )
                            },
                            null,
                            Shader.TileMode.CLAMP
                    );

            paint.setShader(metal);

            canvas.drawCircle(
                    x,
                    channel.centerY(),
                    radius,
                    paint
            );

            paint.setShader(null);

            paint.setStyle(
                    Paint.Style.STROKE
            );

            paint.setStrokeWidth(
                    2f * density
            );

            paint.setColor(
                    Color.rgb(
                            68,
                            63,
                            56
                    )
            );

            canvas.drawCircle(
                    x,
                    channel.centerY(),
                    radius,
                    paint
            );

            paint.setStyle(
                    Paint.Style.FILL
            );

            paint.setColor(
                    Color.rgb(
                            255,
                            252,
                            243
                    )
            );

            canvas.drawCircle(
                    x - 8f * density,
                    channel.centerY()
                            - 9f * density,
                    5f * density,
                    paint
            );
        }

        private void drawSunSymbols(
                Canvas canvas) {

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTextSize(
                    27f * density
            );

            float y =
                    body.centerY()
                            - (
                            paint.ascent()
                                    + paint.descent()
                    ) / 2f;

            paint.setColor(
                    Color.rgb(
                            87,
                            78,
                            67
                    )
            );

            canvas.drawText(
                    "☼",
                    body.left
                            + 45f * density,
                    y,
                    paint
            );

            paint.setColor(
                    Color.rgb(
                            103,
                            78,
                            47
                    )
            );

            canvas.drawText(
                    "☼",
                    body.right
                            - 45f * density,
                    y,
                    paint
            );
        }

        @Override
        public boolean onTouchEvent(
                MotionEvent event) {

            float x =
                    event.getX();

            switch (
                    event.getActionMasked()
            ) {

                case MotionEvent.ACTION_DOWN:

                    dragging = true;

                    updateFromTouch(x);

                    soundEngine.grab();

                    soundEngine.startSliding();

                    performClick();

                    return true;

                case MotionEvent.ACTION_MOVE:

                    if (dragging) {

                        updateFromTouch(x);

                        return true;
                    }

                    return true;

                case MotionEvent.ACTION_UP:

                    if (dragging) {

                        updateFromTouch(x);

                        dragging = false;

                        soundEngine.stopSliding();

                        soundEngine.release();

                        invalidate();

                        finish();

                        return true;
                    }

                    finish();

                    return true;

                case MotionEvent.ACTION_CANCEL:

                    dragging = false;

                    soundEngine.stopSliding();

                    soundEngine.release();

                    finish();

                    return true;
            }

            return true;
        }

        private void updateFromTouch(
                float x) {

            float start =
                    channel.left
                            + 5f * density;

            float end =
                    channel.right
                            - 5f * density;

            float level =
                    (x - start)
                            / (end - start);

            level =
                    Math.max(
                            0f,
                            Math.min(
                                    1f,
                                    level
                            )
                    );

            brightness = level;

            writeBrightness(level);

            invalidate();
        }

        @Override
        public boolean performClick() {

            super.performClick();

            return true;
        }
    }
}
