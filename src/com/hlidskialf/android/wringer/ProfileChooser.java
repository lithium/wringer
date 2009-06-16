package com.hlidskialf.android.wringer;

import android.app.ListActivity;
import android.os.Bundle;

public class ProfileChooser extends ListActivity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.profile_chooser);
  }
}
