package com.hlidskialf.android.wringer;

import android.net.Uri;
import android.content.ContentProvider;
import android.content.UriMatcher;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.Cursor;
import android.database.SQLException;

public class ProfileProvider extends ContentProvider {
  private SQLiteOpenHelper mOpenHelper;

  public static final int URI_MATCH_PROFILE=1;
  public static final int URI_MATCH_PROFILE_ID=2;
  public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
 
  static {
    sUriMatcher.addURI(Wringer.PACKAGE_NAME, "profile",   URI_MATCH_PROFILE);
    sUriMatcher.addURI(Wringer.PACKAGE_NAME, "profile/#", URI_MATCH_PROFILE_ID);
  }

  private static class ProfileDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mimetypes.db";
    private static final int DATABASE_VERSION = 1;
 
    public ProfileDatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE "+ProfileModel.ProfileColumns.TABLE_NAME+" ("+
        "name TEXT, "+
        "alarm_volume INTEGER, "+
        "music_volume INTEGER, "+
        "notify_volume INTEGER, "+
        "ringer_volume INTEGER, "+
        "system_volume INTEGER, "+
        "voice_volume INTEGER, "+
        "ringer_mode INTEGER, "+
        "ringer_vibrate INTEGER, "+
        "notify_vibrate INTEGER, "+
        "play_soundfx INTEGER, "+
        "ringtone TEXT, "+
        "notifytone TEXT, "+

        "airplane_on INTEGER, "+
        "wifi_on INTEGER, "+
        "gps_on INTEGER, "+
        "location_on INTEGER, "+
        "bluetooth_on INTEGER, "+
        "autosync_on INTEGER, "+
        "brightness INTEGER, "+
        "screen_timeout INTEGER, "+

        "_id INTEGER PRIMARY KEY);");

      db.execSQL("CREATE TABLE "+ProfileModel.ProfileContactColumns.TABLE_NAME+" ("+
        "profile_id INTEGER KEY, "+
        "contact_id INTEGER, "+
        "ringtone TEXT, "+
        "notifytone TEXT, "+
        "_id INTEGER PRIMARY KEY);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int curVersion) {
    }
  };

  public ProfileProvider() { }

  @Override
  public boolean onCreate() {
    mOpenHelper = new ProfileDatabaseHelper(getContext());
    return true;
  }
  
  @Override
  public String getType(Uri uri) {
    switch(sUriMatcher.match(uri)) {
      case URI_MATCH_PROFILE:
        return "vnd.android.cursor.dir/"+ProfileModel.ProfileColumns.TABLE_NAME;
      case URI_MATCH_PROFILE_ID:
        return "vnd.android.cursor.item/"+ProfileModel.ProfileColumns.TABLE_NAME;
      default:
        throw new IllegalArgumentException("Unknown URI");
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    if (sUriMatcher.match(uri) != URI_MATCH_PROFILE) {
      throw new IllegalArgumentException("Cannot insert into URI: " + uri);
    }

    if (values == null) {
      throw new IllegalArgumentException("Missing required fields: " + uri);
    }

    // default values...

    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    long id = db.insert(ProfileModel.ProfileColumns.TABLE_NAME, ProfileModel.ProfileColumns.NAME, values);
    if (id < 0)
      throw new SQLException("Failed to insert row into "+uri);

    Uri new_uri = ContentUris.withAppendedId(ProfileModel.ProfileColumns.CONTENT_URI, id);
    getContext().getContentResolver().notifyChange(new_uri, null);
    return new_uri;
  }

  @Override
  public int update(Uri uri, ContentValues values, String where, String[] where_args) {
    if (sUriMatcher.match(uri) != URI_MATCH_PROFILE_ID)
      throw new UnsupportedOperationException("Cannot update URI: " + uri);

    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    String segment = uri.getPathSegments().get(1);
    long id = Long.parseLong(segment);
    int count = db.update(ProfileModel.ProfileColumns.TABLE_NAME, values, "_id="+id, null);
 
    getContext().getContentResolver().notifyChange(uri, null);
    return count;
  }

  @Override
  public int delete(Uri uri, String where, String[] where_args) {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int count;
    switch (sUriMatcher.match(uri)) {
      case URI_MATCH_PROFILE:
        count = db.delete(ProfileModel.ProfileColumns.TABLE_NAME, where, where_args);
        break;
      case URI_MATCH_PROFILE_ID:
        String segment = uri.getPathSegments().get(1);
        long id = Long.parseLong(segment);
        if (where == null || where.length() < 1)
          where = "_id=" + segment;
        else
          where = "_id=" + segment + " AND (" + where + ")";
        count = db.delete(ProfileModel.ProfileColumns.TABLE_NAME, where, where_args);
        break;
      default:
        throw new IllegalArgumentException("Cannot delete from URI: " + uri);
    }
 
    getContext().getContentResolver().notifyChange(uri, null);
    return count;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selection_args, String sort) 
  {
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 
    switch(sUriMatcher.match(uri)) {
      case URI_MATCH_PROFILE:
        qb.setTables(ProfileModel.ProfileColumns.TABLE_NAME);
        break;
      case URI_MATCH_PROFILE_ID:
        qb.setTables(ProfileModel.ProfileColumns.TABLE_NAME);
        qb.appendWhere("_id=");
        qb.appendWhere(uri.getPathSegments().get(1));
        break;
      default:
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    Cursor ret = qb.query(db, projection, selection, selection_args, null, null, sort);
 
    if (ret != null)
      ret.setNotificationUri(getContext().getContentResolver(), uri);
    
    return ret;
  }
}
