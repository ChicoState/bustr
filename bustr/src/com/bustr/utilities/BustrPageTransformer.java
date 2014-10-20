package com.bustr.utilities;

import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

public class BustrPageTransformer implements PageTransformer
{
	private static final float MIN_SCALE = 0.85f;
	
	@Override
	public void transformPage(View view, float position) 
	{
		int pageWidth = view.getWidth();
		int pageHeight = view.getHeight();
		
		// Page is in between slots
		if(position <= 1)
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
		}
	}

}
