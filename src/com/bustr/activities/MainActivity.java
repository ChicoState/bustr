package com.bustr.activities;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.bustr.R;
import com.bustr.utilities.ResourceProvider;

public class MainActivity extends Activity implements OnClickListener {

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
		fontopo = ResourceProvider.instance(getBaseContext()).getFont();		
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
				startActivity(new Intent(this, CameraActivity.class));
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
