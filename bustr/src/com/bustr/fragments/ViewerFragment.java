package com.bustr.fragments;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bustr.R;
import com.bustr.packets.ImagePacket;
import com.bustr.packets.SignalPacket;
import com.bustr.packets.SignalPacket.BustrSignal;
import com.bustr.utilities.ResourceProvider;

public class ViewerFragment extends Fragment {

   // Fields -------------------------------------------------------------------
   private static final String LOGTAG = "BUSTR";
   private String imageName;
   private Bitmap image;

   // GUI elements -------------------------------------------------------------
   private ViewGroup rootView = null;
   private Button upvote, downvote;
   private TextView viewerCaption;
   private ImageView viewerImage;
   private ProgressBar progress;

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

      // GUI element wiring ----------------------------------------------------
      viewerCaption = (TextView) rootView.findViewById(R.id.viewerCaption);
      progress = (ProgressBar) rootView.findViewById(R.id.viewerProgress);
      viewerImage = (ImageView) rootView.findViewById(R.id.viewerImage);
      downvote = (Button) rootView.findViewById(R.id.downvote);
      upvote = (Button) rootView.findViewById(R.id.upvote);
      OnClickListener voteClick = new OnClickListener() {
         @Override
         public void onClick(View v) {
            if (v.getId() == R.id.upvote) {
               new Voter(BustrSignal.REP_UPVOTE);
            } else {
               new Voter(BustrSignal.REP_DOWNVOTE);
            }
         }
      };
      upvote.setOnClickListener(voteClick);
      downvote.setOnClickListener(voteClick);
      Typeface tf = ResourceProvider.instance(rootView.getContext()).getFont();
      upvote.setTypeface(tf);
      downvote.setTypeface(tf);
      viewerCaption.setTypeface(tf);
      new Downloader().execute();
      return rootView;
   }

   public void setImage(ImagePacket imagePacket) {
      viewerCaption.setText(imagePacket.getCaption());
      image = BitmapFactory.decodeByteArray(imagePacket.getData(), 0,
            imagePacket.getData().length);
      assert(image != null);
      viewerCaption.setVisibility(View.VISIBLE);
      try {
         viewerImage.setImageBitmap(image);
      } catch (Exception e) {
         Log.e(LOGTAG, e.toString());
      }
      progress.setVisibility(View.GONE);
      viewerImage.setVisibility(View.VISIBLE);
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
            SignalPacket vote = new SignalPacket(signalType);
            vote.setImageName(imageName);
            output.writeObject(vote);
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
            message = "successful";
         } else if (result.getSignal() == BustrSignal.FAILURE) {
            message = "failed";
         }
         Toast.makeText(rootView.getContext(), "Vote " + message,
               Toast.LENGTH_SHORT).show();
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
}
