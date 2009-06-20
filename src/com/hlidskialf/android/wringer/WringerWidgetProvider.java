package com.hlidskialf.android.wringer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

public class WringerWidgetProvider extends AppWidgetProvider
{
  @Override
  public void onUpdate(Context context, AppWidgetManager awm, int[] widget_ids)
  { 
    SharedPreferences prefs = context.getSharedPreferences(Wringer.PREFERENCES, 0);
    int i;
    for (i=0; i < widget_ids.length; i++) {
      int widget_id = widget_ids[i];

      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);

      Intent intent = new Intent(context, ProfileChooser.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      views.setOnClickPendingIntent(android.R.id.button1, pendingIntent);

      if (prefs.getBoolean(Wringer.PREF_WIDGET_SHOW_ICON_APP, Wringer.DEF_WIDGET_SHOW_ICON_APP)) 
      {
        intent = new Intent(context, WringerActivity.class);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(android.R.id.icon, pendingIntent);
        views.setViewVisibility(android.R.id.icon, View.VISIBLE);
      } else {
        views.setViewVisibility(android.R.id.icon, View.GONE);
      }

      if (prefs.getBoolean(Wringer.PREF_WIDGET_SHOW_ICON_GPS, Wringer.DEF_WIDGET_SHOW_ICON_GPS)) 
      {
        intent = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
        intent.setComponent(new ComponentName(
          "com.android.settings", "com.android.settings.SecuritySettings"));
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.icon_gps, pendingIntent);
        views.setViewVisibility(R.id.icon_gps, View.VISIBLE);
      } else {
        views.setViewVisibility(R.id.icon_gps, View.GONE);
      }

      if (prefs.getBoolean(Wringer.PREF_WIDGET_SHOW_ICON_3G, Wringer.DEF_WIDGET_SHOW_ICON_3G)) 
      {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(
          "com.android.phone", "com.android.phone.Settings"));
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.icon_3g, pendingIntent);
        views.setViewVisibility(R.id.icon_3g, View.VISIBLE);
      } else {
        views.setViewVisibility(R.id.icon_3g, View.GONE);
      }


      String name = Wringer.getCurProfileName(context);
      if (name == null)
        name = context.getString(android.R.string.unknownName);
      views.setTextViewText(android.R.id.button1, name);

      awm.updateAppWidget(widget_id, views);
    }
  }
}
