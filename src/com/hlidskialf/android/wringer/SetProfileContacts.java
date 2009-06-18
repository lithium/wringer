package com.hlidskialf.android.wringer;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ContentValues;
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
  private static final int REQUEST_NOTIFYTONE=2;

  private Cursor mPeopleCursor;
  private int mPickingId;
  private View mPickingView;
  private ProfileContactsAdapter mListAdapter;
  private int mProfileId;
  private HashMap<Integer,Uri[]> mRingtones;

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

      // cache up the profile photos
      mPhotos = new HashMap<Integer,Bitmap>();
      if (cursor.moveToFirst()) {
        do {
          int id = cursor.getInt(mIdIdx);
          Bitmap b = Wringer.getContactPhoto(context, id);
          if (b != null) {
            mPhotos.put(id, Bitmap.createScaledBitmap(b, 48, 48, true));
          }
        } while (cursor.moveToNext());
      }
      cursor.moveToFirst();

    }
    @Override
    public void bindView(View v, Context context, Cursor cursor) {
      int id = cursor.getInt(mIdIdx);
      String name = cursor.getString(mNameIdx);

      TextView tv;
      tv = (TextView)v.findViewById(android.R.id.text1);
      tv.setText( name );

      ImageView iv = (ImageView)v.findViewById(android.R.id.icon);
      if (mPhotos.containsKey(id)) {
        iv.setVisibility(View.VISIBLE);
        iv.setImageBitmap(mPhotos.get(id));
      }
      else {
        iv.setVisibility(View.INVISIBLE);
      }

      if (mRingtones.containsKey(id)) {
        Uri[] uris = mRingtones.get(id);
        tv = (TextView)v.findViewById(R.id.ringtone_text);
        if (uris[0] != null)
          tv.setText( Wringer.getRingtoneTitle(context, uris[0]) ); 
        else
          tv.setText( R.string.ringtone );

        tv = (TextView)v.findViewById(R.id.notifytone_text);
        if (uris[1] != null)
          tv.setText( Wringer.getRingtoneTitle(context, uris[1]) ); 
        else
          tv.setText( R.string.notifytone );
      }
      else {
        tv = (TextView)v.findViewById(R.id.ringtone_text);
        tv.setText( R.string.ringtone );
        tv = (TextView)v.findViewById(R.id.notifytone_text);
        tv.setText( R.string.notifytone );
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

    Intent i = getIntent();
    mProfileId = i.getIntExtra(Wringer.EXTRA_PROFILE_ID, -1);

    //fetch the existing ringtones for this profile
    mRingtones = ProfileModel.getAllContactRingtones(getContentResolver(), mProfileId);

    Button b = (Button)findViewById(android.R.id.button1);
    b.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });

    mPeopleCursor = getContentResolver().query(People.CONTENT_URI, 
      new String[] {People._ID, People.NAME}, 
      People.PRIMARY_PHONE_ID+" IS NOT NULL",null,null);
    startManagingCursor(mPeopleCursor);
    mListAdapter = new ProfileContactsAdapter(this, mPeopleCursor);
    setListAdapter(mListAdapter);
      
  }

  @Override
  protected void onListItemClick(ListView lv, View v, int pos, long id)
  {
    mPickingId = (int)mListAdapter.getItemId(pos);
    mPickingView = v;

    
    final Dialog d = new Dialog(this);
    View layout = d.getLayoutInflater().inflate(R.layout.ringnotify_dialog, null);
    d.setContentView(layout);
    d.setTitle(R.string.choose_title);
    Button b;
    b = (Button)layout.findViewById(android.R.id.button1);
    b.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        Intent picker = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        startActivityForResult(picker, REQUEST_RINGTONE);
        d.dismiss();
      }
    });
    b = (Button)layout.findViewById(android.R.id.button2);
    b.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        Intent picker = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        startActivityForResult(picker, REQUEST_NOTIFYTONE);
        d.dismiss();
      }
    });
    d.show();

  }

  @Override
  protected void onActivityResult(int request, int result, Intent data)
  {
    if (result != RESULT_OK) return;
    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
    String tone = uri == null ? "" : uri.toString();
    ContentValues values = new ContentValues(3);

    values.put(ProfileModel.ProfileContactColumns.PROFILE_ID, mProfileId);
    values.put(ProfileModel.ProfileContactColumns.CONTACT_ID, mPickingId);

    Uri[] uris = mRingtones.get(mPickingId);
    if (uris == null)
      uris = new Uri[] {null,null};

    TextView tv = null;
    if (request == REQUEST_RINGTONE) {
      values.put(ProfileModel.ProfileContactColumns.RINGTONE, tone);
      tv = (TextView)mPickingView.findViewById(R.id.ringtone_text);
      uris[0] = uri;
    }
    else if (request == REQUEST_NOTIFYTONE) {
      values.put(ProfileModel.ProfileContactColumns.NOTIFYTONE, tone);
      tv = (TextView)mPickingView.findViewById(R.id.notifytone_text);
      uris[1] = uri;
    }

    int pcid = ProfileModel.getProfileContactId(getContentResolver(), mProfileId, mPickingId);
    if (pcid == -1) {
      getContentResolver().insert(ProfileModel.ProfileContactColumns.CONTENT_URI, values);
    } else {
      Uri update_uri = Uri.withAppendedPath(ProfileModel.ProfileContactColumns.CONTENT_URI, String.valueOf(pcid));
      getContentResolver().update(update_uri, values, null, null);
    }

    mRingtones.put(mPickingId, uris);

    if (tv != null)
      tv.setText( Wringer.getRingtoneTitle(this, uri) ); 
    mPickingId = -1;
    mPickingView = null;
  }

}
