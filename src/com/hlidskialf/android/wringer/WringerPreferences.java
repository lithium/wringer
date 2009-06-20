package com.hlidskialf.android.wringer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class WringerPreferences extends PreferenceActivity 
{
  @Override
  protected void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);
    addPreferencesFromResource(R.xml.preferences);
  }

  @Override
  protected void onStop()
  {
    super.onStop();
    Wringer.updateWidgets(this);
  }
}
