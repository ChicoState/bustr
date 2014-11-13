package com.bustr.utilities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.bustr.R;
import com.bustr.fragments.ViewerFragment;

public class BustrDialog extends Dialog {

   private Context context;   
   private TextView title_view;
   private TextView ok_button;
   private  android.view.View.OnClickListener listener;
   private String title;
   
   public BustrDialog(Context c) {
      super(c);
      this.context = c;
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.bustr_dialog_view);
      getWindow().setBackgroundDrawable(
            new ColorDrawable(android.graphics.Color.TRANSPARENT));
      
      // GUI
      title_view = (TextView) findViewById(R.id.dialog_title);
      ok_button = (TextView) findViewById(R.id.dialog_ok);
      title_view.setText(title);
      title_view.setTypeface(ResourceProvider.instance(context).getFont());
      ok_button.setTypeface(ResourceProvider.instance(context).getFont());
      ok_button.setOnClickListener(listener);
   }
   
   public void setCustomTitle(String pTitle) {
      title = pTitle;
   }

   public void setButtonListener(
         android.view.View.OnClickListener listener) {
      this.listener = listener;      
   }   
}
