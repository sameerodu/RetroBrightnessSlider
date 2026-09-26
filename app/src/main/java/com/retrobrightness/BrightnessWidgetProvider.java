package com.retrobrightness;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class BrightnessWidgetProvider extends AppWidgetProvider {

    private static final int REQUEST_OPEN_SLIDER = 100;

    @Override
    public void onUpdate(
            Context context,
            AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            updateWidget(
                    context,
                    appWidgetManager,
                    appWidgetId
            );
        }
    }

    private static void updateWidget(
            Context context,
            AppWidgetManager appWidgetManager,
            int appWidgetId) {

        RemoteViews views = new RemoteViews(
                context.getPackageName(),
                R.layout.retro_brightness_widget
        );

        Intent intent = new Intent(
                context,
                SliderActivity.class
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        REQUEST_OPEN_SLIDER,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        views.setOnClickPendingIntent(
                R.id.slider_touch_area,
                pendingIntent
        );

        appWidgetManager.updateAppWidget(
                appWidgetId,
                views
        );
    }
}
