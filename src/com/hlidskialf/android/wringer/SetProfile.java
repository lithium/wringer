package com.hlidskialf.android.wringer;

import android.content.Context;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;
import com.hlidskialf.android.preference.SeekBarPreference;

public class SetProfile extends PreferenceActivity implements ProfileModel.ProfileReporter
{
  private ContentResolver mResolver;
  private Uri mContentUri;

  private int mId;
  private String mName;
  private int mVolAlarm, mVolMusic, mVolNotify, mVolRinger, mVolSystem, mVolVoice;
  private int mRingerMode;
  private boolean mRingerVibrate, mNotifyVibrate, mPlaySoundFx;
  private String mRingtone, mNotifytone;
  private boolean mAirplaneOn, mWifiOn, mGpsOn, mLocationOn, mBluetoothOn, mAutoSyncOn;
  private int mBrightness, mTimeout;

  private EditTextPreference mPrefName;
  private SeekBarPreference mPrefBrightness, mPrefTimeout, mPrefVolAlarm, mPrefVolMusic, mPrefVolNotify, mPrefVolRinger, mPrefVolSystem, mPrefVolVoice;
  private CheckBoxPreference mPrefAutosync, mPrefRingerVibrate, mPrefNotifyVibrate, mPrefPlaySoundFx, mPrefAirplane, mPrefWifi, mPrefGps, mPrefLocation, mPrefBluetooth;
  private RingtonePreference mPrefRingtone, mPrefNotifytone;
  private ListPreference mPrefRingerMode;
  private Preference mPrefContacts; 

  @Override
  protected void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);

    AudioManager audio_mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

    addPreferencesFromResource(R.xml.profile_prefs);

    mPrefName = (EditTextPreference)findPreference("name");
    mPrefName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateName((String)newValue);
        return true;
      }
    });

    mPrefBrightness = (SeekBarPreference)findPreference("brightness");
    mPrefBrightness.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateBrightness((Integer)newValue);
        return true;
      }
    });
    mPrefTimeout = (SeekBarPreference)findPreference("screen_timeout");
    mPrefTimeout.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateTimeout((Integer)newValue);
        return true;
      }
    });

    mPrefVolAlarm = (SeekBarPreference)findPreference("alarm_vol");
    mPrefVolAlarm.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_ALARM));
    mPrefVolAlarm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateVolAlarm((Integer)newValue);
        return true;
      }
    });
    mPrefVolMusic = (SeekBarPreference)findPreference("music_vol");
    mPrefVolMusic.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
    mPrefVolMusic.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateVolMusic((Integer)newValue);
        return true;
      }
    });
    mPrefVolNotify = (SeekBarPreference)findPreference("notify_vol");
    mPrefVolNotify.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
    mPrefVolNotify.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateVolNotify((Integer)newValue);
        return true;
      }
    });
    mPrefVolRinger = (SeekBarPreference)findPreference("ringer_vol");
    mPrefVolRinger.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_RING));
    mPrefVolRinger.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateVolRinger((Integer)newValue);
        return true;
      }
    });
    mPrefVolSystem = (SeekBarPreference)findPreference("system_vol");
    mPrefVolSystem.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
    mPrefVolSystem.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateVolSystem((Integer)newValue);
        return true;
      }
    });
    mPrefVolVoice = (SeekBarPreference)findPreference("voice_vol");
    mPrefVolVoice.setMax(audio_mgr.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
    mPrefVolVoice.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateVolVoice((Integer)newValue);
        return true;
      }
    });
    mPrefRingerMode = (ListPreference)findPreference("ringer_mode");
    mPrefRingerMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateRingerMode((String)newValue);
        return true;
      }
    });

    mPrefAutosync = (CheckBoxPreference)findPreference("autosync_on");
    mPrefRingerVibrate = (CheckBoxPreference)findPreference("ringer_vibrate");
    mPrefNotifyVibrate = (CheckBoxPreference)findPreference("notify_vibrate");
    mPrefPlaySoundFx = (CheckBoxPreference)findPreference("play_soundfx");
    mPrefAirplane = (CheckBoxPreference)findPreference("airplane_on");
    mPrefWifi = (CheckBoxPreference)findPreference("wifi_on");
    mPrefGps = (CheckBoxPreference)findPreference("gps_on");
    mPrefLocation = (CheckBoxPreference)findPreference("location_on");
    mPrefBluetooth = (CheckBoxPreference)findPreference("bluetooth_on");

    mPrefRingtone = (RingtonePreference)findPreference("ringtone");
    mPrefRingtone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateRingtone((String)newValue);
        return true;
      }
    });
    mPrefNotifytone = (RingtonePreference)findPreference("notifytone");
    mPrefNotifytone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      public boolean onPreferenceChange(Preference pref, Object newValue) {
        updateNotifytone((String)newValue);
        return true;
      }
    });

    mResolver = getContentResolver();
    
    Intent i = getIntent();
    mId = i.getIntExtra(Wringer.EXTRA_PROFILE_ID, -1);
    ProfileModel.getProfile(mResolver, this, mId);

    mPrefContacts = findPreference("contacts");
    Intent contacts_intent = new Intent(this, SetProfileContacts.class); 
    contacts_intent.putExtra(Wringer.EXTRA_PROFILE_ID, mId);
    mPrefContacts.setIntent(contacts_intent);

  }




  public void reportProfile(
      int id, String name, 
      int alarm_vol, int music_vol, int notify_vol, int ringer_vol, int system_vol, int voice_vol,
      int ringer_mode, boolean ringer_vibrate, boolean notify_vibrate, boolean play_soundfx,
      String ringtone, String notifytone, 
      boolean airplane_on, boolean wifi_on, boolean gps_on, boolean location_on, boolean bluetooth_on, boolean autosync_on, int brightness, int screen_timeout) 
  {
    mId = id;
    mContentUri = Uri.withAppendedPath(ProfileModel.ProfileColumns.CONTENT_URI, String.valueOf(mId));
    mName = name;
    mVolAlarm = alarm_vol;
    mVolMusic = music_vol;
    mVolNotify = notify_vol;
    mVolRinger = ringer_vol;
    mVolSystem = system_vol;
    mVolVoice = voice_vol;
    mRingerMode = ringer_mode;
    mRingerVibrate = ringer_vibrate;
    mNotifyVibrate = notify_vibrate;
    mPlaySoundFx = play_soundfx;
    mRingtone = ringtone;
    mNotifytone = notifytone;
    mAirplaneOn = airplane_on;
    mWifiOn = wifi_on;
    mGpsOn = gps_on;
    mLocationOn = location_on;
    mBluetoothOn = bluetooth_on;
    mAutoSyncOn = autosync_on;
    mBrightness = brightness;
    mTimeout = screen_timeout;
  }

  private void update_column(String key, String value)
  {
    ContentValues values = new ContentValues(1);
    values.put(key, value);
    mResolver.update(mContentUri, values, null, null);
  }
  private void update_column(String key, int value)
  {
    ContentValues values = new ContentValues(1);
    values.put(key, value);
    mResolver.update(mContentUri, values, null, null);
  }

  public void updateName(String name) {
    mPrefName.setSummary(mName = name);
    update_column(ProfileModel.ProfileColumns.NAME, mName);
  }
  public void updateVolAlarm(int vol) {
    mVolAlarm = vol;
    mPrefVolAlarm.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolAlarm.getMax()));
    update_column(ProfileModel.ProfileColumns.ALARM_VOLUME, mVolAlarm);
  }
  public void updateVolMusic(int vol) {
    mVolMusic = vol;
    mPrefVolMusic.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolMusic.getMax()));
    update_column(ProfileModel.ProfileColumns.ALARM_VOLUME, mVolMusic);
  }
  public void updateVolNotify(int vol) {
    mVolNotify = vol;
    mPrefVolNotify.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolNotify.getMax()));
    update_column(ProfileModel.ProfileColumns.NOTIFY_VOLUME, mVolNotify);
  }
  public void updateVolRinger(int vol) {
    mVolRinger = vol;
    mPrefVolRinger.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolRinger.getMax()));
    update_column(ProfileModel.ProfileColumns.RINGER_VOLUME, mVolRinger);
  }
  public void updateVolSystem(int vol) {
    mVolSystem = vol;
    mPrefVolSystem.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolSystem.getMax()));
    update_column(ProfileModel.ProfileColumns.SYSTEM_VOLUME, mVolSystem);
  }
  public void updateVolVoice(int vol) {
    mVolVoice = vol;
    mPrefVolVoice.setSummary(String.valueOf(vol) +" / "+ String.valueOf(mPrefVolVoice.getMax()));
    update_column(ProfileModel.ProfileColumns.VOICE_VOLUME, mVolVoice);
  }
  public void updateRingerMode(String mode) {
    if (mode.equals("normal")) { 
      mRingerMode = AudioManager.RINGER_MODE_NORMAL; 
      mPrefRingerMode.setSummary(R.string.ringer_mode_normal);
    } 
    else if (mode.equals("vibrate")) { 
      mRingerMode = AudioManager.RINGER_MODE_VIBRATE; 
      mPrefRingerMode.setSummary(R.string.ringer_mode_vibrate);
    } 
    else if (mode.equals("silent")) { 
      mRingerMode = AudioManager.RINGER_MODE_SILENT; 
      mPrefRingerMode.setSummary(R.string.ringer_mode_silent);
    }
    update_column(ProfileModel.ProfileColumns.RINGER_MODE, mRingerMode);
  }
  public void updateBrightness(int brightness) {
    mPrefBrightness.setSummary(String.valueOf(mBrightness = brightness) +" / 255");
    update_column(ProfileModel.ProfileColumns.BRIGHTNESS, mBrightness);
  }
  public void updateTimeout(int timeout) {
    mPrefTimeout.setSummary(String.valueOf(mTimeout = timeout) +" seconds");
    update_column(ProfileModel.ProfileColumns.SCREEN_TIMEOUT, mTimeout);
  }
  public void updateRingtone(String uri) {
    mRingtone = uri;
    Ringtone tone = RingtoneManager.getRingtone(this, Uri.parse(uri));
    if (tone != null)
      mPrefRingtone.setSummary(tone.getTitle(this));
    update_column(ProfileModel.ProfileColumns.RINGTONE, mRingtone);
  }
  public void updateNotifytone(String uri) {
    mNotifytone = uri;
    Ringtone tone = RingtoneManager.getRingtone(this, Uri.parse(uri));
    if (tone != null)
      mPrefNotifytone.setSummary(tone.getTitle(this));
    update_column(ProfileModel.ProfileColumns.NOTIFYTONE, mNotifytone);
  }

}
