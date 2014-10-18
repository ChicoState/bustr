package com.bustr.activities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bustr.R;
import com.bustr.utilities.ResourceProvider;

public class LoginActivity extends Activity {

   private EditText username, password;
   private TextView banner;
   private Button sign_in;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_login);

      // Wire GUI --------------------------------------------------------------
      username = (EditText) findViewById(R.id.username_input);
      password = (EditText) findViewById(R.id.password_input);
      banner = (TextView) findViewById(R.id.banner1);
      sign_in = (Button) findViewById(R.id.sign_in);
      Typeface tf = ResourceProvider.instance(LoginActivity.this).getFont();
      banner.setTypeface(tf);
      sign_in.setTypeface(tf);
      
      // Listener --------------------------------------------------------------
      sign_in.setOnClickListener(new OnClickListener() {

         @Override
         public void onClick(View arg0) {
            username.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
         }
         
      });
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.login, menu);
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

   private class AttemptLogin extends AsyncTask<Void, Void, Void> {

      private Socket socket;
      private ObjectOutputStream output;
      private ObjectInputStream input;

      @Override
      protected Void doInBackground(Void... arg0) {
         try {
            socket = new Socket();
            socket.connect(ResourceProvider.instance(LoginActivity.this)
                  .socketAddress(), 10);
         } catch (IOException ioe) {
            ioe.printStackTrace();
         }

         return null;
      }
   }

}
