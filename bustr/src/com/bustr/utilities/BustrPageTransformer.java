package com.bustr.utilities;

import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

public class BustrPageTransformer implements PageTransformer
{
	private static final float MIN_SCALE = 0.85f;
	private static final float MIN_ALPHA = 0.5f;	
	
	@Override
	public void transformPage(View view, float position) 
	{
		int pageWidth = view.getWidth();
		int pageHeight = view.getHeight();
		
//		if(position < -1) // Page is far off screen to the left...
//			view.setAlpha(0);
		if(position <= 1) // Page is somewhere in between...
		{
			// Modify the default slide transition to shrink the page as well
			float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
			float vertMargin = pageHeight * (1 - scaleFactor) / 2;
			float horzMargin = pageWidth * (1 - scaleFactor) / 2;
			if(position < 0)
				view.setTranslationX(horzMargin - vertMargin / 2);
			else
				view.setTranslationX(-horzMargin + vertMargin / 2);
			
			// Scale the page down (between MIN_SCALE and 1)
			view.setScaleX(scaleFactor);
			view.setScaleY(scaleFactor);

			
			// Fade the page relative to its size.
//			view.setAlpha(MIN_ALPHA  + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
		}
//		else // Page is far off screen to the right...
//			view.setAlpha(0);
	}

}
