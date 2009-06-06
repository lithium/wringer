package com.hlidskialf.android.wringer;

import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.content.Intent;
import android.os.Bundle;

public class SetProfile extends PreferenceActivity implements ProfileModel.ProfileReporter
{
  private int mId;
  private String mName;
  private int mVolAlarm, mVolMusic, mVolNotify, mVolRinger, mVolSystem, mVolVoice;
  private int mRingerMode, mRingerVibrate, mNotifyVibrate;
  private boolean mPlaySoundFx;
  private String mRingtone, mNotifytone;
  private boolean mAirplaneOn, mWifiOn, mGpsOn, mLocationOn, mBluetoothOn, mAutoSyncOn;
  private int mBrightness, mTimeout;

  private Preference mPrefContacts; 


  @Override
  protected void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);


    addPreferencesFromResource(R.xml.profile_prefs);
    mPrefContacts = findPreference("contacts");

    mPrefContacts.setIntent(new Intent(this, SetProfile.class));

    Intent i = getIntent();
    mId = i.getIntExtra(Wringer.EXTRA_PROFILE_ID, -1);
    ProfileModel.getProfile(getContentResolver(), this, mId);
  }




  public void reportProfile(
      int id, String name, 
      int alarm_vol, int music_vol, int notify_vol, int ringer_vol, int system_vol, int voice_vol,
      int ringer_mode, int ringer_vibrate, int notify_vibrate, boolean play_soundfx,
      String ringtone, String notifytone, 
      boolean airplane_on, boolean wifi_on, boolean gps_on, boolean location_on, boolean bluetooth_on, boolean autosync_on, int brightness, int screen_timeout) 
  {
    mId = id;
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
}
