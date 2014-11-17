package com.bustr.fragments;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bustr.R;
import com.bustr.helpers.Comment;
import com.bustr.packets.ImagePacket;
import com.bustr.packets.ImagePacket.VoteState;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;
import com.bustr.utilities.BustrDialog;
import com.bustr.utilities.CommentsAdapter;
import com.bustr.utilities.ResourceProvider;

public class ViewerFragment extends Fragment {

   // Fields -------------------------------------------------------------------
   private static final String LOGTAG = "BUSTR";
   private String imageName;
   private Bitmap image;
   private Comment userComment;
   ArrayList<Comment> commentv;
   private VoteState voteState;
   private Downloader downloader = new Downloader();
   private CommentsAdapter comments_adapter;
   private SharedPreferences sharedPrefs;
   Vibrator vib;

   // GUI elements -------------------------------------------------------------
   private ViewGroup rootView = null;
   private ListView listView;
   private ImageView upvote, downvote, comment;
   private TextView viewerCaption;
   
   // private TextView repDisplay;
   private ImageView viewerImage;   
   private ImageView outer, inner;

   // Constructor --------------------------------------------------------------
   public ViewerFragment(String pImageName) {
      imageName = pImageName;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState) {
      Log.d(LOGTAG, "Creating fragment");
      rootView = (ViewGroup) inflater.inflate(R.layout.viewer_fragment,
            container, false);
      sharedPrefs = PreferenceManager.getDefaultSharedPreferences(rootView
            .getContext());
      vib = (Vibrator) rootView.getContext().getSystemService(
            Context.VIBRATOR_SERVICE);
      // GUI element wiring ----------------------------------------------------
      listView = (ListView) rootView.findViewById(R.id.comment_list);
      viewerCaption = (TextView) rootView.findViewById(R.id.viewerCaption);
      // repDisplay = (TextView) rootView.findViewById(R.id.repDisplay);
      viewerImage = (ImageView) rootView.findViewById(R.id.viewerImage);
      outer = (ImageView) rootView.findViewById(R.id.outer);
      inner = (ImageView) rootView.findViewById(R.id.inner);
      downvote = (ImageView) rootView.findViewById(R.id.downvote);
      upvote = (ImageView) rootView.findViewById(R.id.upvote);
      comment = (ImageView) rootView.findViewById(R.id.comment);
      OnClickListener voteClick = new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (v.getId() == R.id.upvote) {
               switch (voteState) {
               case NONE:
                  new Voter(BustrSignal.REP_UPVOTE);
                  voteState = VoteState.UP;
                  break;
               case UP:
                  new Voter(BustrSignal.REP_DOWNVOTE);
                  voteState = VoteState.NONE;
                  break;
               case DOWN:
                  new Voter(BustrSignal.REP_UPVOTE);
                  new Voter(BustrSignal.REP_UPVOTE);
                  voteState = VoteState.UP;
                  break;
               }
               vib.vibrate(60);
               setVoteButtonStates();
            }
            else if (v.getId() == R.id.downvote) {
               switch (voteState) {
               case NONE:
                  new Voter(BustrSignal.REP_DOWNVOTE);
                  voteState = VoteState.DOWN;
                  break;
               case UP:
                  new Voter(BustrSignal.REP_DOWNVOTE);
                  new Voter(BustrSignal.REP_DOWNVOTE);
                  voteState = VoteState.DOWN;
                  break;
               case DOWN:
                  new Voter(BustrSignal.REP_UPVOTE);
                  voteState = VoteState.NONE;
                  break;
               }
               vib.vibrate(60);
               setVoteButtonStates();
            }
            else if (v.getId() == R.id.comment) {
               getCommentFromUser();
            }
         }
      };
      upvote.setOnClickListener(voteClick);
      downvote.setOnClickListener(voteClick);
      comment.setOnClickListener(voteClick);
      Typeface tf = ResourceProvider.instance(rootView.getContext()).getFont();
      viewerCaption.setTypeface(tf);
      // repDisplay.setTypeface(tf);
      RotateAnimation rotate = new RotateAnimation(0, 360,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
      rotate.setRepeatCount(RotateAnimation.INFINITE);
      rotate.setInterpolator(new LinearInterpolator());
      rotate.setDuration(1000);
      outer.setAnimation(rotate);
      rotate.start();
      downloader.execute();
      return rootView;
   }

   private void getCommentFromUser() {

      final BustrDialog commentDialog = new BustrDialog(rootView.getContext(),
            R.layout.bustr_input_dialog_view);
      OnClickListener listener = new OnClickListener() {
         @Override
         public void onClick(View arg0) {
            userComment = new Comment(sharedPrefs.getString("username",
                  "no_user"), ResourceProvider.instance(rootView.getContext())
                  .getDate(),
                  ((EditText) commentDialog.findViewById(R.id.comment_input))
                        .getText().toString());
            new Voter(BustrSignal.NEW_COMMENT);
            commentDialog.dismiss();
         }
      };
      commentDialog.setCustomTitle("Comment");
      commentDialog.setButtonListener(listener);
      commentDialog.show();
      //
      // new AlertDialog.Builder(rootView.getContext()).setTitle("Comment")
      // .setView(captionInput).setNeutralButton("Ok", listener)
      // .setIcon(android.R.drawable.ic_input_get).show();
   }

   public void setImage(ImagePacket imagePacket) {
      commentv = imagePacket.getMessages();
      voteState = imagePacket.getVoteState();
      viewerCaption.setText(imagePacket.getCaption());
      viewerCaption.setVisibility(View.VISIBLE);
      image = BitmapFactory.decodeByteArray(imagePacket.getData(), 0,
            imagePacket.getData().length);
      comments_adapter = new CommentsAdapter(rootView.getContext(), commentv);
      listView.setDivider(getResources().getDrawable(
            android.R.drawable.divider_horizontal_dim_dark));
      listView.setDividerHeight(1);
      listView.setAdapter(comments_adapter);

      try {
         viewerImage.setScaleType(ScaleType.CENTER_CROP);
         viewerImage.setImageBitmap(image);

      } catch (Exception e) {
         Log.e(LOGTAG, e.toString());
      }
      outer.clearAnimation();
      inner.setVisibility(View.GONE);
      outer.setVisibility(View.GONE);
      viewerImage.setVisibility(View.VISIBLE);
      viewerImage.startAnimation(AnimationUtils.loadAnimation(
            rootView.getContext(), android.R.anim.fade_in));
   }

   private class Voter extends AsyncTask<Void, Void, SignalPacket> {
      private Socket socket;
      private ObjectOutputStream output;
      private ObjectInputStream input;
      private BustrSignal signalType;

      public Voter(BustrSignal signal) {
         signalType = signal;
         this.execute();
      }

      @Override
      protected SignalPacket doInBackground(Void... arg0) {
         SignalPacket response = new SignalPacket(BustrSignal.FAILURE);
         try {
            socket = new Socket(InetAddress.getByName("50.173.32.127"), 8000);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            SignalPacket packet = new SignalPacket(signalType);
            packet.setImageName(imageName);
            packet.setUser(sharedPrefs.getString("username", "no_user"));
            if (signalType == BustrSignal.NEW_COMMENT) {
               packet.setComment(userComment);
            }
            output.writeObject(packet);
            response = (SignalPacket) input.readObject();
            output.close();
            input.close();
            socket.close();
         } catch (UnknownHostException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
         }
         return response;
      }

      @Override
      protected void onPostExecute(SignalPacket result) {
         super.onPostExecute(result);
         String message = null;
         if (result.getSignal() == BustrSignal.SUCCESS) {
            message = "Success";
            refresh();
         }
         else if (result.getSignal() == BustrSignal.FAILURE) {
            message = "Fail";
         }
         Toast.makeText(rootView.getContext(), message, Toast.LENGTH_SHORT)
               .show();
      }
   }

   private class Downloader extends AsyncTask<Void, Void, ImagePacket> {

      private Socket socket;
      private ObjectOutputStream output;
      private ObjectInputStream input;

      @Override
      protected ImagePacket doInBackground(Void... arg0) {
         try {
            socket = new Socket(InetAddress.getByName("50.173.32.127"), 8000);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            SignalPacket imgReq = new SignalPacket(BustrSignal.IMAGE_REQUEST);
            imgReq.setImageName(imageName);
            output.writeObject(imgReq);
            ImagePacket imagePacket = (ImagePacket) input.readObject();
            output.close();
            input.close();
            socket.close();
            return imagePacket;
         } catch (UnknownHostException e) {
            e.printStackTrace();
         } catch (IOException e) {
            Log.e(LOGTAG, "You dropped the connection!!!");
            e.printStackTrace();
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
         }
         return null;
      }

      @Override
      protected void onPostExecute(ImagePacket result) {
         super.onPostExecute(result);
         setImage(result);
      }

   }

   private void setVoteButtonStates() {
      switch (voteState) {
      case NONE:
         downvote.setImageResource(R.drawable.upvote);
         upvote.setImageResource(R.drawable.upvote);
         break;
      case UP:
         downvote.setImageResource(R.drawable.upvote);
         upvote.setImageResource(R.drawable.upcircular2);
         break;
      case DOWN:
         downvote.setImageResource(R.drawable.upcircular2);
         upvote.setImageResource(R.drawable.upvote);
         break;
      }
   }

   public void recycleImage() {
      if (image != null) {
         image.recycle();
      }
   }

   private void refresh() {
      // TODO: refresh comments and rep score
   }

   public void cancelDownload() {
      downloader.cancel(true);
   }

   @Override
   public void onDetach() {
      // TODO Auto-generated method stub
      downloader.cancel(true);
      super.onDetach();
   }
}
