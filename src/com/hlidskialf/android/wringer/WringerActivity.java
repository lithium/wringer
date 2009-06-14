package com.hlidskialf.android.wringer;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.view.View;

public class WringerActivity extends Activity
{
  public static final int REQUEST_SET_PROFILE=1;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    Button b = (Button)findViewById(android.R.id.button1);
    b.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        int profile_id = ProfileModel.newProfile(getContentResolver());  
        edit_profile(profile_id);
      }
    });
  }

  private void edit_profile(int profile_id)
  {
    Intent intent = new Intent(this, SetProfile.class);
    intent.putExtra(Wringer.EXTRA_PROFILE_ID, profile_id);
    startActivityForResult(intent, REQUEST_SET_PROFILE);
  }
}
