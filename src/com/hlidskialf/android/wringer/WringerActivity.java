package com.hlidskialf.android.wringer;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

  private Wringer.ProfileAdapter mListAdapter;
  private ContentResolver mResolver;
  private SharedPreferences mPrefs;
    
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    mResolver = getContentResolver();
    mPrefs = getSharedPreferences(Wringer.PREFERENCES, 0);
    int cur_profile = mPrefs.getInt(Wringer.PREF_CUR_PROFILE, -1);

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
    mListAdapter = new Wringer.ProfileAdapter(this, c);
    mListAdapter.setCurProfile(cur_profile);
    mListAdapter.setOnChooseProfileListener(new Wringer.ProfileAdapter.OnChooseProfileListener() {
      public void onChooseProfile(int pos, long profile_id) {
        getListView().invalidateViews();

        Wringer.applyProfile(WringerActivity.this, (int)profile_id);
        Wringer.setCurProfile(WringerActivity.this, (int)profile_id);
      }
    });
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

  @Override
  protected void onActivityResult(int request, int result, Intent data)
  {
    if (request == REQUEST_SET_PROFILE) {
      if (mListAdapter.getCount() == 1) {
        choose_profile(0);
      }
    }
  }

  private void edit_profile(int profile_id)
  {
    Intent intent = new Intent(this, SetProfile.class);
    intent.putExtra(Wringer.EXTRA_PROFILE_ID, profile_id);
    startActivityForResult(intent, REQUEST_SET_PROFILE);
  }

  private void choose_profile(int pos)
  {
  }
}
