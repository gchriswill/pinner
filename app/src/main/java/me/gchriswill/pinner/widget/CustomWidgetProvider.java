package me.gchriswill.pinner.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;

public class CustomWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "SimpleWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i(TAG, "onUpdate: ID ARRAY LENGHT --> " + appWidgetIds.length);

        for (int appWidgetId : appWidgetIds) {
            WidgetHelper widgetHelper = new WidgetHelper(context);
            widgetHelper.updateWidget(appWidgetId);
        }
    }
}

