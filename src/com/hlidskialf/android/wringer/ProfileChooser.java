package com.hlidskialf.android.wringer;

import android.app.ListActivity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.View;
import android.widget.ListView;

public class ProfileChooser extends ListActivity
                            implements Wringer.ProfileAdapter.OnChooseProfileListener
{
  private SharedPreferences mPrefs;
  private Wringer.ProfileAdapter mListAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.profile_chooser);

    mPrefs = getSharedPreferences(Wringer.PREFERENCES, 0);
    int cur_profile = mPrefs.getInt(Wringer.PREF_CUR_PROFILE, -1);
    Cursor c = Wringer.getProfileCursor(getContentResolver());
    mListAdapter = new Wringer.ProfileAdapter(this, c);
    mListAdapter.setCurProfile(cur_profile);
    mListAdapter.setOnChooseProfileListener(this);
    setListAdapter(mListAdapter);
  }

  public void onChooseProfile(int pos, long profile_id) {
    Wringer.applyProfile(this, (int)profile_id, getWindow());
    finish();
  }

  @Override
  protected void onListItemClick(ListView lv, View v, int pos, long id)
  {
    Wringer.applyProfile(this, (int)id, getWindow());
    finish();
  }
}
