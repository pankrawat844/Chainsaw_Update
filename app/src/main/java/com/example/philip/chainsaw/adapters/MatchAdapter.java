package com.example.philip.chainsaw.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.philip.chainsaw.R;
import com.example.philip.chainsaw.model.Match;
import com.squareup.picasso.Picasso;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom ArrayAdapter for ListView in MessagesActivity
 */
public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.Viewholder> {
    private int resource;
    private ArrayList<Match> items;
    private Context context;

    public MatchAdapter(Context context, ArrayList<Match> items) {
        this.items = items;
        this.context = context;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.match_item,parent,false);

        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(Viewholder holder, int position) {
        Match item = items.get(position);
        String message = "No messages";
        if (item.getMessages().size() > 0) {
            message = item.getMessages().get((item.getMessages().size()-1)).getMessageText();
        }

        holder.nameView.setText(item.getName());
        holder.messageView.setText(message);
        String picUrl = item.getPhotoUrl();
        if (picUrl.equals("")) {
            Picasso.with(context).load(R.mipmap.recs_icon).into(holder.picView);
        } else {
            Picasso.with(context).load(item.getPhotoUrl()).into(holder.picView);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

     class Viewholder extends RecyclerView.ViewHolder
    {
        TextView nameView,messageView;
        ImageView picView;
        public Viewholder(View itemView) {
            super(itemView);
             nameView = (TextView) itemView.findViewById(R.id.message_item_nameTW);
             messageView = (TextView) itemView.findViewById(R.id.message_item_messageTW);
             picView = (ImageView) itemView.findViewById(R.id.message_picIW);

        }
    }



    public void refill(List<Match> events) {
        items.clear();
        items.addAll(events);
        notifyDataSetChanged();
    }
}
