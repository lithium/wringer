package com.hlidskialf.android.wringer;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.content.Context;
import android.net.Uri;

public class Wringer
{
  public static final String PACKAGE_NAME="com.hlidskialf.android.wringer";

  public static final String EXTRA_PROFILE_ID="profile_id";

  public static String getRingtoneTitle(Context context, Uri uri)
  {
    Ringtone r = RingtoneManager.getRingtone(context, uri);
    return r.getTitle(context);
  }
}
