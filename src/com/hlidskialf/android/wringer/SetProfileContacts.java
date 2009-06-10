package com.hlidskialf.android.wringer;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.HashMap;

public class SetProfileContacts extends ListActivity
{
  private static final int REQUEST_RINGTONE=1;

  private Cursor mPeopleCursor;
  private long mPickingId;
  private ProfileContactsAdapter mListAdapter;

  class ProfileContactsAdapter extends CursorAdapter 
  {
    private LayoutInflater mFactory;
    private int mNameIdx, mIdIdx;
    private HashMap<Integer,Bitmap> mPhotos;

    ProfileContactsAdapter(Context context, Cursor cursor) {
      super(context,cursor);
      mFactory = LayoutInflater.from(context);
      mIdIdx = cursor.getColumnIndexOrThrow(People._ID);
      mNameIdx = cursor.getColumnIndexOrThrow(People.NAME);

      mPhotos = new HashMap<Integer,Bitmap>();
      cursor.moveToFirst();
      do {
        int id = cursor.getInt(mIdIdx);
        Uri uri = ContentUris.withAppendedId(People.CONTENT_URI, id);
        Bitmap b = People.loadContactPhoto(context, uri, android.R.drawable.gallery_thumb, null);
        if (b != null) {
          mPhotos.put(id, Bitmap.createScaledBitmap(b, 48, 48, true));
        }
      } while (cursor.moveToNext());
      cursor.moveToFirst();
    }
    @Override
    public void bindView(View v, Context context, Cursor cursor) {
      int id = cursor.getInt(mIdIdx);
      String name = cursor.getString(mNameIdx);

      TextView tv;
      tv = (TextView)v.findViewById(android.R.id.text1);
      tv.setText( name );

      if (mPhotos.containsKey(id)) {
        ImageView iv = (ImageView)v.findViewById(android.R.id.icon);
        iv.setImageBitmap(mPhotos.get(id));
      }

    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      View v = mFactory.inflate(R.layout.contacts_list_item, parent, false);
      bindView(v,context,cursor);
      return v;
    }
  }

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
    mListAdapter = new ProfileContactsAdapter(this, mPeopleCursor);
    setListAdapter(mListAdapter);
      
  }

  @Override
  protected void onListItemClick(ListView lv, View v, int pos, long id)
  {
    mPickingId = mListAdapter.getItemId(pos);
    Intent picker = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
    startActivityForResult(picker, REQUEST_RINGTONE);
  }

  @Override
  protected void onActivityResult(int request, int result, Intent data)
  {
    if (result != RESULT_OK) return;
    if (request == REQUEST_RINGTONE) {
      Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
      android.util.Log.v("picked", String.valueOf(mPickingId)+": "+uri.toString());
      mPickingId = -1;
    }
  }

}
