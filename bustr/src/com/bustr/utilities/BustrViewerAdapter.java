package com.bustr.utilities;

import java.util.Vector;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bustr.fragments.ViewerFragment;

public class BustrViewerAdapter extends FragmentStatePagerAdapter {

   private ViewerFragment[] fragments;
   private Vector<String> imageNames;
   
   public BustrViewerAdapter(FragmentManager fm, Vector<String> pImageNames) {
      super(fm);
      imageNames = pImageNames;
      int listSize = imageNames.size();
      fragments = new ViewerFragment[listSize];
      for(int i = 0; i < listSize; i++) {
         fragments[i] = new ViewerFragment(imageNames.get(i));
      }
   }

   @Override
   public Fragment getItem(int arg0) {
      return fragments[arg0];
   }

   @Override
   public int getCount() {
      return fragments.length;
   } 

}