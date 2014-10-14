package com.bustr.utilities;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.bustr.fragments.ViewerFragment;

public class BustrViewerAdapter extends FragmentStatePagerAdapter {

   private ViewerFragment[] fragments;
   
   public BustrViewerAdapter(FragmentManager fm, int size) {
      super(fm);
      fragments = new ViewerFragment[size];
      for(int i = 0; i < size; i++) {
         fragments[i] = new ViewerFragment();
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
   
   public void setImage(int index, Bitmap pImage, String pCaption) {
      fragments[index].setImage(pImage, pCaption);      
   }

}