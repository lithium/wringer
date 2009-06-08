package com.hlidskialf.android.wringer;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;

public class WringerActivity extends Activity
{
  public static final int REQUEST_SET_PROFILE=1;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    startActivityForResult(new Intent(this, SetProfile.class), REQUEST_SET_PROFILE);
  }
}
