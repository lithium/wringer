package com.hlidskialf.android.wringer;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout;

public class WringerActivity extends ListActivity
{
  public static final int REQUEST_SET_PROFILE=1;

  private ProfileAdapter mListAdapter;
  private ContentResolver mResolver;
  private int mCurProfile = -1;

  private class ProfileAdapter extends CursorAdapter 
  {
    private LayoutInflater mFactory;
    private int mNameIdx, mModeIdx, mAirplaneIdx, mWifiIdx, mGpsIdx, mLocationIdx, mBluetoothIdx, mAutosyncIdx;

    public ProfileAdapter(Context context, Cursor cursor) {
      super(context,cursor);
      mFactory = LayoutInflater.from(context);

      mNameIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.NAME);
      mModeIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.RINGER_MODE);
      mAirplaneIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.AIRPLANE_ON);
      mWifiIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.WIFI_ON);
      mGpsIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.GPS_ON);
      mLocationIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.LOCATION_ON);
      mBluetoothIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.BLUETOOTH_ON);
      mAutosyncIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.AUTOSYNC_ON);
    }

    @Override
    public void bindView(View v, Context context, Cursor cursor) {
      TextView tv = (TextView)v.findViewById(android.R.id.text1);
      String name = cursor.getString(mNameIdx);
      if (TextUtils.isEmpty(name))
        tv.setText(android.R.string.unknownName);
      else
        tv.setText(name);

      final int pos = cursor.getPosition();
      CheckBox cb = (CheckBox)v.findViewById(android.R.id.checkbox);
      cb.setChecked(mCurProfile == pos);
      cb.setOnClickListener(new Button.OnClickListener() {
        public void onClick(View v) {
          choose_profile(pos);
        }
      });

      ImageView iv; 
      ViewGroup bar = (ViewGroup)v.findViewById(R.id.icon_bar);
      bar.removeAllViews();

      String ringer_mode = cursor.getString(mModeIdx);
      if (ringer_mode == null || ringer_mode.equals("normal")) {
      }
      else if (ringer_mode.equals("vibrate")) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_ringer_vibrate);
        bar.addView(iv);
      }
      else if (ringer_mode.equals("silent")) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_ringer_silent);
        bar.addView(iv);
      }
      int airplane_on = cursor.getInt(mAirplaneIdx);
      if (airplane_on != 0) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_signal_flightmode);
        bar.addView(iv);
      }
      int wifi_on = cursor.getInt(mWifiIdx);
      if (wifi_on != 0) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_wifi_signal_4);
        bar.addView(iv);
      }
      int gps_on = cursor.getInt(mGpsIdx);
      if (gps_on != 0) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_gps_on);
        bar.addView(iv);
      }
      int bluetooth_on = cursor.getInt(mBluetoothIdx);
      if (bluetooth_on != 0) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_sys_data_bluetooth);
        bar.addView(iv);
      }
      int autosync_on = cursor.getInt(mAutosyncIdx);
      if (autosync_on != 0) {
        iv = new ImageView(context);
        iv.setImageResource(R.drawable.stat_notify_sync);
        bar.addView(iv);
      }


    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      View v = mFactory.inflate(R.layout.profile_list_item, parent, false);
      bindView(v,context,cursor);
      return v;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    mResolver = getContentResolver();

    Cursor c = mResolver.query(ProfileModel.ProfileColumns.CONTENT_URI,
      new String[] {ProfileModel.ProfileColumns._ID, 
        ProfileModel.ProfileColumns.NAME,
        ProfileModel.ProfileColumns.RINGER_MODE,
        ProfileModel.ProfileColumns.AIRPLANE_ON,
        ProfileModel.ProfileColumns.WIFI_ON,
        ProfileModel.ProfileColumns.GPS_ON,
        ProfileModel.ProfileColumns.LOCATION_ON,
        ProfileModel.ProfileColumns.BLUETOOTH_ON,
        ProfileModel.ProfileColumns.AUTOSYNC_ON}, null,null,null);
    startManagingCursor(c);
    mListAdapter = new ProfileAdapter(this, c);
    setListAdapter(mListAdapter);

    Button b = (Button)findViewById(android.R.id.button1);
    b.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        int profile_id = ProfileModel.newProfile(mResolver);  
        edit_profile(profile_id);
      }
    });
  }

  @Override
  protected void onListItemClick(ListView lv, View v, int pos, long id)
  {
    edit_profile((int)id);
  }

  private void edit_profile(int profile_id)
  {
    Intent intent = new Intent(this, SetProfile.class);
    intent.putExtra(Wringer.EXTRA_PROFILE_ID, profile_id);
    startActivityForResult(intent, REQUEST_SET_PROFILE);
  }

  private void choose_profile(int pos)
  {
    mCurProfile = pos;
    getListView().invalidateViews();

    Wringer.applyProfile(this,  (int)mListAdapter.getItemId(pos));
  }
}
