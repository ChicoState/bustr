package com.bustr.activities.fragments;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bustr.R;
import com.bustr.utilities.ResourceProvider;

public class ViewerFragment extends Fragment {

   private ProgressBar progress;
   private TextView viewerCaption;
   private ImageView viewerImage;
   private Bitmap image;
   private static final String LOGTAG = "BUSTR";
   private ViewGroup rootView = null;

   public ViewerFragment() {
      super();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState) {
      rootView = (ViewGroup) inflater.inflate(R.layout.viewer_fragment,
            container, false);
      progress = (ProgressBar) rootView.findViewById(R.id.viewerProgress);
      viewerCaption = (TextView) rootView.findViewById(R.id.viewerCaption);
      viewerImage = (ImageView) rootView.findViewById(R.id.viewerImage);
      Typeface tf = ResourceProvider.instance(rootView.getContext()).getFont();
      viewerCaption.setTypeface(tf);
      return rootView;
   }

   public void setImage(Bitmap pImage, String pCaption) {
      progress.setVisibility(View.GONE);
      image = pImage;
      viewerCaption.setText(pCaption);      
      viewerCaption.setVisibility(View.VISIBLE);
      try {
         viewerImage.setImageBitmap(pImage);
      } catch (Exception e) {
         Log.e(LOGTAG, e.toString());
      }
      viewerImage.setVisibility(View.VISIBLE);
   }

}
