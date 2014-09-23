package com.bustr.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.bustr.R;
import com.bustr.utilities.ResourceProvider;

public class MainActivity extends Activity implements OnClickListener {

   // Private fields -----------------------------------------------------------
   private static final String LOGTAG = "BUSTR";
   private SharedPreferences sharedPrefs;

   // GUI Components -----------------------------------------------------------
   private Button button1, button2;
   private TextView banner;
   Typeface fontopo;

   // OnCreate -----------------------------------------------------------------
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      // Log onCreate event for life-cycle debugging
      Log.d(LOGTAG, "OnCreate()");

      sharedPrefs = PreferenceManager
            .getDefaultSharedPreferences(getBaseContext());

      // GUI Component wiring
      banner = (TextView) findViewById(R.id.banner1);
      button1 = (Button) findViewById(R.id.button1);
      button2 = (Button) findViewById(R.id.button2);

      // Load type-face resources and apply
      fontopo = ResourceProvider.instance(getBaseContext()).getFont();
      banner.setTypeface(fontopo);
      button1.setTypeface(fontopo);
      button2.setTypeface(fontopo);

      // Register views that listen for clicks
      button1.setOnClickListener(this);
      button2.setOnClickListener(this);
   }

   // onClick event handler ----------------------------------------------------
   @SuppressLint("InlinedApi")
   @Override
   public void onClick(View view) {
      switch (view.getId()) {
      case R.id.button1:
         startActivity(new Intent(this, CameraActivity.class));
         break;
      case R.id.button2:
         int picsNum = 10;
         String contentText = String.format("There are %s picture here",
               picsNum);
         Notification.Builder notifiBuilder = new Notification.Builder(this)
               .setSmallIcon(R.drawable.bustr_logo)
               .setContentTitle("New Location!")
               .setContentText(contentText)
               .setAutoCancel(true);
       // Creates an explicit intent for an Activity in your app
       Intent resultIntent = new Intent(this, MainActivity.class);
       // This ensures that navigating backward from the Activity leads out of
       // your application to the Home screen.
       TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
       // Adds the back stack for the Intent (but not the Intent itself)
       stackBuilder.addParentStack(MainActivity.class);
       // Adds the Intent that starts the Activity to the top of the stack
       stackBuilder.addNextIntent(resultIntent);
       PendingIntent resultPendingIntent =
               stackBuilder.getPendingIntent(
                   0,
                   PendingIntent.FLAG_UPDATE_CURRENT
               );
       notifiBuilder.setContentIntent(resultPendingIntent);
       NotificationManager mNotificationManager =
           (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
       // 1033 allows you to update the notification later on.
       mNotificationManager.notify(1033, notifiBuilder.build());
         break;
      }

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {

      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();
      if (id == R.id.action_settings) {
         return true;
      }
      return super.onOptionsItemSelected(item);
   }

}
