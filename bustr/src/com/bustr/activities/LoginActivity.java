package com.bustr.activities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bustr.R;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;
import com.bustr.utilities.ResourceProvider;

public class LoginActivity extends Activity {

   private EditText username, password;
   private TextView banner;
   private Button sign_in, sign_up;
   private static final String LOGTAG = "BUSTR";
   private SharedPreferences sharedPrefs;
   private SharedPreferences.Editor editor;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_login);

      sharedPrefs = PreferenceManager
            .getDefaultSharedPreferences(getBaseContext());
      editor = sharedPrefs.edit();
      if (sharedPrefs.getInt("logged_in", 0) == 1) {
         startActivity(new Intent(LoginActivity.this, MainActivity.class));
         finish();
      }

      // Wire GUI --------------------------------------------------------------
      username = (EditText) findViewById(R.id.username_input);
      password = (EditText) findViewById(R.id.password_input);
      banner = (TextView) findViewById(R.id.banner1);
      sign_in = (Button) findViewById(R.id.sign_in);
      sign_up = (Button) findViewById(R.id.sign_up);

      // Setup typeface --------------------------------------------------------
      Typeface tf = ResourceProvider.instance(LoginActivity.this).getFont();
      banner.setTypeface(tf);
      sign_in.setTypeface(tf);
      sign_up.setTypeface(tf);

      // Listener --------------------------------------------------------------
      OnClickListener listener = new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (v.getId() == R.id.sign_in) {
               new AttemptLogin().execute();
            } else if (v.getId() == R.id.sign_up) {
               startActivity(new Intent(LoginActivity.this,
                     NewAccountActivity.class));
               finish();
            }
         }
      };
      sign_in.setOnClickListener(listener);
      sign_up.setOnClickListener(listener);
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

   private class AttemptLogin extends AsyncTask<Void, Void, BustrSignal> {

      private Socket socket;
      private ObjectOutputStream output;
      private ObjectInputStream input;
      BustrSignal result = BustrSignal.REP_DOWNVOTE;

      @Override
      protected BustrSignal doInBackground(Void... arg0) {
         try {
            socket = new Socket(InetAddress.getByName("50.173.32.127"), 8000);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            SignalPacket user_login = new SignalPacket(BustrSignal.USER_AUTH);
            user_login.setUser(username.getText().toString());
            MessageDigest md = MessageDigest.getInstance("MD5");
            String hash_pass = new BigInteger(1, md.digest()).toString(16); 
                  //new String(md.digest(password.getText()
                  //.toString().getBytes("UTF-8")));
            user_login.setPass(hash_pass);
            Log.d(LOGTAG, "Login attempt: User: " + user_login.getUser()
                  + ", Pass: " + user_login.getPass());
            output.writeObject(user_login);
            SignalPacket response = (SignalPacket) input.readObject();
            input.close();
            output.close();
            socket.close();
            return response.getSignal();

         } catch (IOException ioe) {
            ioe.printStackTrace();
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
         } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
         }
         return null;
      }

      @Override
      protected void onPostExecute(BustrSignal result) {
         super.onPostExecute(result);
         Log.d(LOGTAG, "Login response: " + result.toString());
         if (result == BustrSignal.SUCCESS) {
            Toast.makeText(LoginActivity.this, "Login Successful!",
                  Toast.LENGTH_SHORT).show();
            editor.putInt("logged_in", 1).commit();
            editor.putString("username", username.getText().toString())
                  .commit();
            try {
               Thread.sleep(1000);
            } catch (InterruptedException e) {
               // Do nothing
            }
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
         } else {
            Toast.makeText(LoginActivity.this, "Login Failed",
                  Toast.LENGTH_SHORT).show();
         }
      }
   }

}
