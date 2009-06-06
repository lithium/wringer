package com.hlidskialf.android.wringer;

import android.provider.BaseColumns;
import android.net.Uri;
import android.content.ContentResolver;

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
      int ringer_mode, int ringer_vibrate, int notify_vibrate, boolean play_soundfx,
      String ringtone, String notifytone, 
      boolean airplane_on, boolean wifi_on, boolean gps_on, boolean location_on, boolean bluetooth_on,
      boolean autosync_on, int brightness, int screen_timeout 
    );
  }

  public static void getProfile(ContentResolver resolver, ProfileReporter reporter, int profileId)
  {
  }
}
