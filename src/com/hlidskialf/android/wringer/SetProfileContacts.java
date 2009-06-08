package com.hlidskialf.android.wringer;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.Button;
import android.database.Cursor;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.view.View;
import android.provider.Contacts.People;

public class SetProfileContacts extends ListActivity
{
  private Cursor mPeopleCursor;
  private ListAdapter mListAdapter;

  @Override
  protected void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);
    setContentView(R.layout.contacts_list);

    Button b;
    b = (Button)findViewById(android.R.id.button1);
    b.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });

    b = (Button)findViewById(android.R.id.button2);
    b.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });

    mPeopleCursor = getContentResolver().query(People.CONTENT_URI, 
      new String[] {People._ID, People.NAME}, 
      null,null,null);
    startManagingCursor(mPeopleCursor);
    ListAdapter mListAdapter = new SimpleCursorAdapter(this, 
      android.R.layout.simple_list_item_1, mPeopleCursor, 
      new String[] {People.NAME},
      new int[] {android.R.id.text1});
    setListAdapter(mListAdapter);
      
  }
}
