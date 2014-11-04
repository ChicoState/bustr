package com.bustr.activities;

import junit.framework.TestCase;

import org.junit.Test;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.util.Log;
import android.view.View;

import com.bustr.R;

public class activitiesTest extends TestCase {
	
	@Test
	public void getCameraInstanceTest() {
		
		TestCase.assertFalse(false);
	}
	
	@Test
	public void onPictureKeepTest() {
		OnClickListener listener = new OnClickListener() {
			@Override
            public void onClick(View v) {
               TestCase.assertEquals(v.getId(), R.id.btn_keep);
		}
	}
	
	@Test
	public void onPictureDiscardTest() {
		OnClickListener listener = new OnClickListener() {
			@Override
	        public void onClick(View v) {
				TestCase.assertEquals(v.getId(), R.id.btn_discard);
			}
		}
	}
		
	@Test
	public void autoFocusTest() {
		autoFocusCallback = new AutoFocusCallback() {
	         @Override
	         public void onAutoFocus(boolean focused, Camera pCam) {
	            Log.d(LOGTAG, "Auto focus callback");
	            TestCase.assertEquals(true, takingPicture);
	            TestCase.assertEquals(true, focused);
	         }
		}
	}
		
	@Test
	public void getCaptionFromUserTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void onCreateTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void onCreateOptionsMenuTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void onLocationChangedTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void onOptionsItemSelectedTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void onPauseTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void onProviderDisabledTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void onProviderEnabledTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void onResumeTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void onStatusChangedTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void onStopTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void promptEnableGPSTest() {
		TestCase.assertFalse(false);
	}
	
	@Test
	public void switchCameraTest() {
		TestCase.assertFalse(false);
	}
}