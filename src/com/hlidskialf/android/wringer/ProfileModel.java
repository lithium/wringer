package com.hlidskialf.android.wringer;

import android.provider.BaseColumns;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.content.ContentResolver;
import java.util.HashMap;
import java.util.List;
import android.database.Cursor;
import android.media.AudioManager;

public class ProfileModel
{
  public static class ProfileContactColumns implements BaseColumns {
    public static final Uri CONTENT_URI = Uri.parse("content://"+Wringer.PACKAGE_NAME+"/profile_contact");
    public static final String _ID = "_id";
    public static final String DEFAULT_SORT_ORDER = "contact_id ASC";
    public static final String TABLE_NAME = "profile_contacts";

    public static final String PROFILE_ID = "profile_id";
    public static final String CONTACT_ID = "contact_id";
    public static final String RINGTONE = "ringtone";
    public static final String NOTIFYTONE = "notifytone";

    static final String[] QUERY_COLUMNS = { _ID, PROFILE_ID, CONTACT_ID, RINGTONE, NOTIFYTONE };
    public static final int ID_INDEX = 0;
    public static final int PROFILE_ID_INDEX = 1;
    public static final int CONTACT_ID_INDEX = 2;
    public static final int RINGTONE_INDEX = 3;
    public static final int NOTIFYTONE_INDEX = 4;

  }

  public static class ProfileColumns implements BaseColumns {
    public static final Uri CONTENT_URI = Uri.parse("content://"+Wringer.PACKAGE_NAME+"/profile");
    public static final String _ID = "_id";
    public static final String DEFAULT_SORT_ORDER = "name ASC";
    public static final String TABLE_NAME = "profiles";

    public static final String NAME = "name";
    public static final String ALARM_VOLUME = "alarm_volume";
    public static final String MUSIC_VOLUME = "music_volume";
    public static final String NOTIFY_VOLUME = "notify_volume";
    public static final String RINGER_VOLUME = "ringer_volume";
    public static final String SYSTEM_VOLUME = "system_volume";
    public static final String VOICE_VOLUME = "voice_volume";
    public static final String RINGER_MODE = "ringer_mode";
    public static final String RINGER_VIBRATE = "ringer_vibrate";
    public static final String NOTIFY_VIBRATE = "notify_vibrate";
    public static final String PLAY_SOUNDFX = "play_soundfx";
    public static final String RINGTONE = "ringtone";
    public static final String NOTIFYTONE = "notifytone";
    public static final String AIRPLANE_ON = "airplane_on";
    public static final String WIFI_ON = "wifi_on";
    public static final String GPS_ON = "gps_on";
    public static final String LOCATION_ON = "location_on";
    public static final String BLUETOOTH_ON = "bluetooth_on";
    public static final String AUTOSYNC_ON = "autosync_on";
    public static final String BRIGHTNESS = "brightness";
    public static final String SCREEN_TIMEOUT = "screen_timeout";

    static final String[] QUERY_COLUMNS = { _ID, NAME, ALARM_VOLUME, MUSIC_VOLUME, NOTIFY_VOLUME, RINGER_VOLUME, SYSTEM_VOLUME, VOICE_VOLUME, RINGER_MODE, RINGER_VIBRATE, NOTIFY_VIBRATE, PLAY_SOUNDFX, RINGTONE, NOTIFYTONE, AIRPLANE_ON, WIFI_ON, GPS_ON, LOCATION_ON, BLUETOOTH_ON, AUTOSYNC_ON, BRIGHTNESS, SCREEN_TIMEOUT };
    public static final int ID_INDEX = 0;
    public static final int NAME_INDEX = 1;
    public static final int ALARM_VOLUME_INDEX = 2;
    public static final int MUSIC_VOLUME_INDEX = 3;
    public static final int NOTIFY_VOLUME_INDEX = 4;
    public static final int RINGER_VOLUME_INDEX = 5;
    public static final int SYSTEM_VOLUME_INDEX = 6;
    public static final int VOICE_VOLUME_INDEX = 7;
    public static final int RINGER_MODE_INDEX = 8;
    public static final int RINGER_VIBRATE_INDEX = 9;
    public static final int NOTIFY_VIBRATE_INDEX = 10;
    public static final int PLAY_SOUNDFX_INDEX = 11;
    public static final int RINGTONE_INDEX = 12;
    public static final int NOTIFYTONE_INDEX = 13;
    public static final int AIRPLANE_ON_INDEX = 14;
    public static final int WIFI_ON_INDEX = 15;
    public static final int GPS_ON_INDEX = 16;
    public static final int LOCATION_ON_INDEX = 17;
    public static final int BLUETOOTH_ON_INDEX = 18;
    public static final int AUTOSYNC_ON_INDEX = 19;
    public static final int BRIGHTNESS_INDEX = 20;
    public static final int SCREEN_TIMEOUT_INDEX = 21;
  };

  public static interface ProfileReporter {
    public void reportProfile(
      int id, String name, 
      int alarm_vol, int music_vol, int notify_vol, int ringer_vol, int system_vol, int voice_vol,
      String ringer_mode, boolean ringer_vibrate, boolean notify_vibrate, boolean play_soundfx,
      String ringtone, String notifytone, 
      boolean airplane_on, boolean wifi_on, boolean gps_on, boolean location_on, boolean bluetooth_on,
      boolean autosync_on, int brightness, int screen_timeout 
    );
  }


  public static int newProfile(Context context)
  {
    AudioManager audio_mgr = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    ContentValues values = new ContentValues();

    values.put(ProfileColumns.ALARM_VOLUME, audio_mgr.getStreamVolume(AudioManager.STREAM_ALARM));
    values.put(ProfileColumns.MUSIC_VOLUME, audio_mgr.getStreamVolume(AudioManager.STREAM_MUSIC));
    values.put(ProfileColumns.NOTIFY_VOLUME, audio_mgr.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
    values.put(ProfileColumns.RINGER_VOLUME, audio_mgr.getStreamVolume(AudioManager.STREAM_RING));
    values.put(ProfileColumns.SYSTEM_VOLUME, audio_mgr.getStreamVolume(AudioManager.STREAM_SYSTEM));
    values.put(ProfileColumns.VOICE_VOLUME, audio_mgr.getStreamVolume(AudioManager.STREAM_VOICE_CALL));

    Uri new_uri = context.getContentResolver().insert(ProfileModel.ProfileColumns.CONTENT_URI, values);
    List<String> segments = new_uri.getPathSegments();
    return Integer.valueOf( segments.get(1) );
  }
  public static void deleteProfile(Context context, int profile_id)
  {
    context.getContentResolver().delete(ProfileColumns.CONTENT_URI, 
      ProfileColumns._ID+"="+profile_id, null);
  }

  public static void getProfile(ContentResolver resolver, ProfileReporter reporter, int profile_id)
  {
    if (reporter == null) 
      return;
    Cursor cursor = resolver.query(ProfileColumns.CONTENT_URI, ProfileColumns.QUERY_COLUMNS,
      ProfileColumns._ID+"=?", new String[] {String.valueOf(profile_id)}, null);
    if (cursor.moveToFirst()) {
      reporter.reportProfile(cursor.getInt(ProfileColumns.ID_INDEX), 
        cursor.getString(ProfileColumns.NAME_INDEX),
        cursor.getInt(ProfileColumns.ALARM_VOLUME_INDEX),
        cursor.getInt(ProfileColumns.MUSIC_VOLUME_INDEX),
        cursor.getInt(ProfileColumns.NOTIFY_VOLUME_INDEX),
        cursor.getInt(ProfileColumns.RINGER_VOLUME_INDEX),
        cursor.getInt(ProfileColumns.SYSTEM_VOLUME_INDEX),
        cursor.getInt(ProfileColumns.VOICE_VOLUME_INDEX),
        cursor.getString(ProfileColumns.RINGER_MODE_INDEX),
        cursor.getInt(ProfileColumns.RINGER_VIBRATE_INDEX) == 0 ? false : true,
        cursor.getInt(ProfileColumns.NOTIFY_VIBRATE_INDEX) == 0 ? false : true,
        cursor.getInt(ProfileColumns.PLAY_SOUNDFX_INDEX) == 0 ? false : true,
        cursor.getString(ProfileColumns.RINGTONE_INDEX),
        cursor.getString(ProfileColumns.NOTIFYTONE_INDEX),
        cursor.getInt(ProfileColumns.AIRPLANE_ON_INDEX) == 0 ? false : true,
        cursor.getInt(ProfileColumns.WIFI_ON_INDEX) == 0 ? false : true,
        cursor.getInt(ProfileColumns.GPS_ON_INDEX) == 0 ? false : true,
        cursor.getInt(ProfileColumns.LOCATION_ON_INDEX) == 0 ? false : true,
        cursor.getInt(ProfileColumns.BLUETOOTH_ON_INDEX) == 0 ? false : true,
        cursor.getInt(ProfileColumns.AUTOSYNC_ON_INDEX) == 0 ? false : true,
        cursor.getInt(ProfileColumns.BRIGHTNESS_INDEX),
        cursor.getInt(ProfileColumns.SCREEN_TIMEOUT_INDEX)
      );
    }
    cursor.close();
  }

  public static HashMap<Integer,Uri[]> getAllContactRingtones(ContentResolver resolver, int profile_id)
  {
    Cursor cursor = resolver.query(ProfileContactColumns.CONTENT_URI, 
      new String[] {
        ProfileContactColumns.CONTACT_ID, 
        ProfileContactColumns.RINGTONE,
        ProfileContactColumns.NOTIFYTONE
      },
      ProfileContactColumns.PROFILE_ID+"="+profile_id, null, null);
    HashMap<Integer,Uri[]> ret = new HashMap<Integer,Uri[]>(cursor.getCount());
    if (cursor.moveToFirst()) {
      do {
        int contact_id = cursor.getInt(0);
        String ringtone = cursor.getString(1);
        String notifytone = cursor.getString(2);
        Uri[] uris = new Uri[] {null,null};
        if (ringtone != null)
          uris[0] = Uri.parse(ringtone);
        if (notifytone != null)
          uris[1] = Uri.parse(notifytone);
        ret.put(contact_id, uris);
      } while (cursor.moveToNext());
    }
    cursor.close();
    return ret;
  }

  public static int getProfileContactId(ContentResolver resolver, int profile_id, int contact_id)
  {
    Cursor c = resolver.query(ProfileContactColumns.CONTENT_URI,
      new String[] { ProfileContactColumns._ID },
        ProfileModel.ProfileContactColumns.PROFILE_ID+"="+profile_id
        +" AND "+
        ProfileModel.ProfileContactColumns.CONTACT_ID+"="+contact_id, null,null);
    int ret = -1;
    if (c.moveToFirst()) {
      ret = c.getInt(0);
    }
    c.close();
    return ret;
  }
  public static String getContactNotifytone(ContentResolver resolver, int profile_id, int contact_id)
  {
    Cursor c = resolver.query(ProfileContactColumns.CONTENT_URI,
      new String[] { ProfileContactColumns._ID, ProfileContactColumns.NOTIFYTONE },
        ProfileModel.ProfileContactColumns.PROFILE_ID+"="+profile_id
        +" AND "+
        ProfileModel.ProfileContactColumns.CONTACT_ID+"="+contact_id, null,null);
    String ret = null;
    if (c.moveToFirst()) {
      ret = c.getString(1);
    }
    c.close();
    return ret;
  }
}
