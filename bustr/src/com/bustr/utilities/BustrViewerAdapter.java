package com.bustr.utilities;

import java.util.Vector;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

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

   @Override
   public void destroyItem(ViewGroup container, int position, Object object) {      
      super.destroyItem(container, position, object);
      Log.d("BUSTR", "Destroying fragment " + position);
      fragments[position].recycleImage();
   }
}