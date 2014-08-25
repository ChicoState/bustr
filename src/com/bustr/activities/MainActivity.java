package com.bustr.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bustr.R;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	// Private fields ----------------------------------------------------------
	private static final String LOGTAG = "BUSTR";	
	
	// GUI Components ----------------------------------------------------------
	private Button button1, button2;
	private TextView banner;
	Typeface fontopo;	
	
	// OnCreate ----------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Log onCreate event for life-cycle debugging
		Log.d(LOGTAG, "OnCreate()");
		
		// GUI Component wiring
		banner = (TextView)findViewById(R.id.banner1);		
		button1 = (Button)findViewById(R.id.button1);
		button2 = (Button)findViewById(R.id.button2);
		
		// Load type-face resources and apply
		fontopo = Typeface.createFromAsset (
			getBaseContext().getAssets(), "fontopo.ttf"
		);
		banner.setTypeface(fontopo);
		button1.setTypeface(fontopo);
		button2.setTypeface(fontopo);
		
		// Register views that listen for clicks
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		
	}

	// onClick event handler ---------------------------------------------------
	@Override
	public void onClick(View view) {
		switch(view.getId()) {		
			case R.id.button1:
				Toast.makeText(this, "button1 clicked...", Toast.LENGTH_SHORT)
				.show();
				break;
			case R.id.button2:
				Toast.makeText(this, "button2 clicked...", Toast.LENGTH_SHORT)
				.show();
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
