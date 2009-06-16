package com.hlidskialf.android.wringer;

import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WringerWidgetProvider extends AppWidgetProvider
{
  @Override
  public void onUpdate(Context context, AppWidgetManager awm, int[] widget_ids)
  {
    int i;
    for (i=0; i < widget_ids.length; i++) {
      int widget_id = widget_ids[i];

      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);

      Intent intent = new Intent(context, ProfileChooser.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      views.setOnClickPendingIntent(android.R.id.button1, pendingIntent);

      intent = new Intent(context, WringerActivity.class);
      pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      views.setOnClickPendingIntent(android.R.id.icon, pendingIntent);


      String name = Wringer.getCurProfileName(context);
      if (name == null)
        name = context.getString(android.R.string.unknownName);
      views.setTextViewText(android.R.id.button1, name);

      awm.updateAppWidget(widget_id, views);
    }
  }
}
