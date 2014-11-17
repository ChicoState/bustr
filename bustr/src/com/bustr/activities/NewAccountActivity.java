package com.bustr.activities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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

public class NewAccountActivity extends Activity {

   private static final String LOGTAG = "BUSTR";
   private SharedPreferences sharedPrefs;
   private SharedPreferences.Editor editor;

   // GUI Components -----------------------------------------------------------
   private TextView banner;
   private EditText username, password1, password2;
   private Button create;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_create_account);

      sharedPrefs = PreferenceManager
            .getDefaultSharedPreferences(getBaseContext());
      editor = sharedPrefs.edit();

      // Wire GUI components ---------------------------------------------------
      banner = (TextView) findViewById(R.id.banner2);
      username = (EditText) findViewById(R.id.new_username);
      password1 = (EditText) findViewById(R.id.new_password1);
      password2 = (EditText) findViewById(R.id.new_password2);
      create = (Button) findViewById(R.id.create_account);

      create.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (password1.getText().toString()
                  .equals(password2.getText().toString())) {
               new CreateAccount().execute();
            } else {
               Toast.makeText(NewAccountActivity.this, "Password mismatch",
                     Toast.LENGTH_SHORT).show();
            }
         }
      });

      // Setup typeface --------------------------------------------------------
      Typeface tf = ResourceProvider.instance(NewAccountActivity.this)
            .getFont();
      banner.setTypeface(tf);
      create.setTypeface(tf);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.create_account, menu);
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

   private class CreateAccount extends AsyncTask<Void, Void, BustrSignal> {

      private Socket socket;
      private ObjectOutputStream output;
      private ObjectInputStream input;
      BustrSignal result = BustrSignal.FAILURE;

      @Override
      protected BustrSignal doInBackground(Void... arg0) {
         try {
            socket = new Socket(InetAddress.getByName("50.173.32.127"), 8000);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            SignalPacket new_user = new SignalPacket(BustrSignal.NEW_USER);
            new_user.setUser(username.getText().toString());
            MessageDigest md = MessageDigest.getInstance("MD5");
            String pass_hash = new String(md.digest(
                  password1.getText().toString().getBytes("UTF-8")));
            new_user.setPass(pass_hash);
            Log.d(LOGTAG, "Login attempt: User: " + new_user.getUser()
                  + ", Pass: " + new_user.getPass());
            output.writeObject(new_user);
            SignalPacket response = (SignalPacket) input.readObject();
            input.close();
            output.close();
            socket.close();
            return response.getSignal();
         } catch (UnknownHostException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
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
         if (result == BustrSignal.SUCCESS) {
            Toast.makeText(NewAccountActivity.this, "Registration Successful!",
                  Toast.LENGTH_SHORT).show();
            editor.putInt("logged_in", 1).commit();
            editor.putString("username", username.getText().toString())
                  .commit();
            try {
               Thread.sleep(1000);
            } catch (InterruptedException e) {
               // Do nothing
            }
            startActivity(new Intent(NewAccountActivity.this,
                  MainActivity.class));
            finish();
         } else if (result == BustrSignal.FAILURE) {
            Toast.makeText(NewAccountActivity.this, "Login Failed",
                  Toast.LENGTH_SHORT).show();
         }

      }

   }

}
