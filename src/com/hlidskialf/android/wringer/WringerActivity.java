package com.hlidskialf.android.wringer;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;

public class WringerActivity extends Activity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    startActivity(new Intent(this, SetProfile.class));
  }
}
