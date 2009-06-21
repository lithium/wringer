package com.hlidskialf.android.wringer;

import android.app.ListActivity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.ContextMenu;

public class WringerActivity extends ListActivity
{
  public static final int REQUEST_SET_PROFILE=1;
  public static final int REQUEST_PREFERENCES=2;

  private Wringer.ProfileAdapter mListAdapter;
  private ContentResolver mResolver;
  private SharedPreferences mPrefs;
  private int mEditingId = -1;

    
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    mResolver = getContentResolver();
    mPrefs = getSharedPreferences(Wringer.PREFERENCES, 0);
    int cur_profile = mPrefs.getInt(Wringer.PREF_CUR_PROFILE, -1);

    Cursor c = Wringer.getProfileCursor(mResolver);
    startManagingCursor(c);
    mListAdapter = new Wringer.ProfileAdapter(this, c);
    mListAdapter.setCurProfile(cur_profile);
    mListAdapter.setOnChooseProfileListener(new Wringer.ProfileAdapter.OnChooseProfileListener() {
      public void onChooseProfile(int pos, long profile_id) {
        getListView().invalidateViews();

        Wringer.applyProfile(WringerActivity.this, (int)profile_id, getWindow(), null);
      }
    });
    setListAdapter(mListAdapter);

    Button b = (Button)findViewById(android.R.id.button1);
    b.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        int profile_id = ProfileModel.newProfile(WringerActivity.this);  
        edit_profile(profile_id);
      }
    });

    registerForContextMenu(getListView());
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
      int cur_profile = mPrefs.getInt(Wringer.PREF_CUR_PROFILE, -1);
      if (mEditingId != -1 && mEditingId == cur_profile) {
        Wringer.applyProfile(this, cur_profile, getWindow(), null);
      }
    }
    mEditingId =-1;
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.options, menu);

    return true;
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId()) {
      case R.id.options_menu_preferences:
        startActivityForResult( 
          new Intent(this, WringerPreferences.class), REQUEST_PREFERENCES);
        return true;
      case R.id.options_menu_about:
        View layout = getLayoutInflater().inflate(R.layout.about, null);
        AlertDialog dia = new AlertDialog.Builder(this)
                                .setTitle(R.string.about_title)
                                .setView(layout)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public  void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info)
  {
    super.onCreateContextMenu(menu,v,info);
    getMenuInflater().inflate(R.menu.context, menu);
  }
  @Override
  public boolean onContextItemSelected(MenuItem item)
  {
    int pos = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
    int profile_id = (int)mListAdapter.getItemId(pos);

    switch (item.getItemId()) {
      case R.id.context_menu_edit:
        edit_profile(profile_id);
        return true;
      case R.id.context_menu_delete:
        ProfileModel.deleteProfile(this, profile_id);
        return true;
      case R.id.context_menu_apply:
        mListAdapter.setCurProfile(profile_id);
        Wringer.applyProfile(this, profile_id, getWindow(), null);
        getListView().invalidateViews();
        return true;
    }
    return super.onContextItemSelected(item);
  }



  private void edit_profile(int profile_id)
  {
    mEditingId = profile_id;

    Intent intent = new Intent(this, SetProfile.class);
    intent.putExtra(Wringer.EXTRA_PROFILE_ID, profile_id);
    startActivityForResult(intent, REQUEST_SET_PROFILE);
  }

}
