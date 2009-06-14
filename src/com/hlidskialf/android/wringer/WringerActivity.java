package com.hlidskialf.android.wringer;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WringerActivity extends ListActivity
{
  public static final int REQUEST_SET_PROFILE=1;

  private ProfileAdapter mListAdapter;
  private ContentResolver mResolver;

  private static class ProfileAdapter extends CursorAdapter 
  {
    private LayoutInflater mFactory;
    private int mNameIdx;

    public ProfileAdapter(Context context, Cursor cursor) {
      super(context,cursor);
      mFactory = LayoutInflater.from(context);

      mNameIdx = cursor.getColumnIndexOrThrow(ProfileModel.ProfileColumns.NAME);
    }

    @Override
    public void bindView(View v, Context context, Cursor cursor) {
      TextView tv = (TextView)v.findViewById(android.R.id.text1);
      tv.setText(cursor.getString(mNameIdx));
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
      new String[] {ProfileModel.ProfileColumns._ID, ProfileModel.ProfileColumns.NAME},
      null,null,null);
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
}
