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
   private String title = "!";
   private String message;
   private int layout;
   
   public BustrDialog(Context c, int layout) {
      super(c);
      this.context = c;
      this.layout = layout;      
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(layout);
      getWindow().setBackgroundDrawable(
            new ColorDrawable(android.graphics.Color.TRANSPARENT));
      
      // GUI
      title_view = (TextView) findViewById(R.id.dialog_title);
      ok_button = (TextView) findViewById(R.id.dialog_ok);
      title_view.setText(title);
      title_view.setTypeface(ResourceProvider.instance(context).getFont());
      ok_button.setTypeface(ResourceProvider.instance(context).getFont());
      ok_button.setOnClickListener(listener);
      if(layout == R.layout.bustr_alert_layout_view) {
         TextView messageView = (TextView) findViewById(R.id.alert_message);
         messageView.setText(message);
      }
   }
   
   public void setCustomTitle(String pTitle) {
      title = pTitle;
   }

   public void setButtonListener(
         android.view.View.OnClickListener listener) {
      this.listener = listener;      
   }   
   
   // Only if layout is bustr_alert_dialog_view!
   public void setAlertMessage(String pMessage) {
      this.message = pMessage;
   }
}
