package com.bustr.utilities;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bustr.R;
import com.bustr.helpers.Comment;

public class CommentsAdapter extends ArrayAdapter<Comment> {

   private Context context;
   private ArrayList<Comment> comments;

   public CommentsAdapter(Context context, ArrayList<Comment> comments) {
      super(context, R.layout.comment_list_item, comments);
      this.context = context;
      this.comments = comments;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View comment_view = inflater.inflate(R.layout.comment_list_item, parent,
            false);
      TextView body = (TextView)comment_view.findViewById(R.id.comment_body);
      body.setText(comments.get(position).getBody());
      return comment_view;
   }

}
