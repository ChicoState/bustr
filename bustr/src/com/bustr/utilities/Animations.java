package com.bustr.utilities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class Animations {

   @SuppressWarnings("unused")
   private static String LOGTAG = "BACKTALK";

   public static void hide_and_show(View v, int pDuration) {
      AnimatorSet set = new AnimatorSet();
      set.play(ObjectAnimator.ofFloat(v, "translationY", v.getHeight())
            .setDuration(250));
      set.play(ObjectAnimator.ofFloat(v, "translationY", 0).setDuration(250))
            .after(pDuration);
      set.start();
   }

   public static void shake(View v) {
      AnimatorSet set = new AnimatorSet();
      Animator phase1 = (ObjectAnimator.ofFloat(v, "translationX", -15)
            .setDuration(50));
      Animator phase2 = (ObjectAnimator.ofFloat(v, "translationX", 15)
            .setDuration(50));
      Animator phase3 = (ObjectAnimator.ofFloat(v, "translationX", -10)
            .setDuration(50));
      Animator phase4 = (ObjectAnimator.ofFloat(v, "translationX", 5)
            .setDuration(50));
      Animator phase5 = (ObjectAnimator.ofFloat(v, "translationX", 0)
            .setDuration(50));
      set.playSequentially(phase1, phase2, phase3, phase4, phase5);
//      set.setInterpolator(new AccelerateDecelerateInterpolator());
      set.start();
   }

   public static TranslateAnimation slideDown(int pDuration) {

      TranslateAnimation slide_down = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.9f);
      slide_down.setDuration(pDuration);
      slide_down.setFillAfter(true);
      return slide_down;
   }

   public static TranslateAnimation slideUp(int pDuration) {

      TranslateAnimation slide_up = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
      slide_up.setDuration(pDuration);
      return slide_up;
   }

   public static TranslateAnimation slideRight(int pDuration) {

      TranslateAnimation slide_right = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_PARENT,
            1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f);
      slide_right.setFillAfter(true);
      slide_right.setDuration(pDuration);
      return slide_right;
   }

   public static RotateAnimation spin(int pDuration, float pX, float pY) {

      RotateAnimation spin = new RotateAnimation(0, -360.0f, pX, pY);
      spin.setInterpolator(new LinearInterpolator());
      spin.setRepeatCount(RotateAnimation.INFINITE);
      spin.setDuration(pDuration);
      return spin;
   }

   public static ScaleAnimation pulse(int pDuration, float pX, float pY) {

      ScaleAnimation pulse = new ScaleAnimation(1.0f, 1.25f, 1.0f, 1.25f, pX,
            pY);
      pulse.setRepeatCount(ScaleAnimation.INFINITE);
      pulse.setRepeatMode(ScaleAnimation.REVERSE);
      pulse.setDuration(pDuration);
      return pulse;
   }
}
