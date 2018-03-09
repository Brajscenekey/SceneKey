package com.scenekey.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scenekey.R;
import com.scenekey.activity.HomeActivity;
import com.scenekey.helper.Constant;
import com.scenekey.helper.WebServices;
import com.scenekey.model.Feeds;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by mindiii on 15/2/18.
 */

public class Profile_Adapter extends BaseAdapter {

    private Context context;
    private ArrayList<Feeds> feedList;
    private boolean myProfile;

    public Profile_Adapter(Context context, ArrayList<Feeds> feedsList, boolean myProfile) {
        this.context=context;
        this.feedList=feedsList;
        this.myProfile=myProfile;
    }

    @Override
    public int getCount() {
        return feedList.size();
    }

    @Override
    public Object getItem(int position) {
        return feedList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if(itemView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            itemView = inflater.inflate(R.layout.custom_profile_events, parent, false);
            Holder holder = new Holder();
            holder.txt_comment =  itemView.findViewById(R.id.txt_comment);
            holder.txt_EE =  itemView.findViewById(R.id.txt_EE);
            holder.txt_date = itemView.findViewById(R.id.txt_date);
            holder.txt_time =  itemView.findViewById(R.id.txt_time);
            holder.txt_at =  itemView.findViewById(R.id.txt_at);
            holder.gap = itemView.findViewById(R.id.gap);

            holder.txt_time.setVisibility(View.GONE);
            holder.txt_date.setVisibility(View.GONE);
            holder.txt_at.setVisibility(View.GONE);
            if(position==0)holder.gap.setVisibility(View.VISIBLE);
            else holder.gap.setVisibility(View.GONE);

            holder.img_event =  itemView.findViewById(R.id.img_event);
            holder.rtlv_EE =  itemView.findViewById(R.id.rtlv_EE);

             Feeds feeds = feedList.get(position);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.img_event.getLayoutParams();
            layoutParams.height = HomeActivity.ActivityWidth * 3 / 4;
            holder.img_event.setLayoutParams(layoutParams);
            holder.txt_comment.setLayoutParams(layoutParams);

            if (position == 0) {
                holder.txt_EE.setText(feeds.event_name);
                holder.rtlv_EE.setVisibility(View.VISIBLE);
                try {
                    holder.txt_date.setText(getDateInFormat(feeds.date));
                    holder.txt_time.setText(getTimeInFormat(feeds.date));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if (feeds.event_name.equals(feedList.get(position - 1).event_name)) {
                holder.rtlv_EE.setVisibility(View.GONE);
            } else {
                holder.rtlv_EE.setVisibility(View.VISIBLE);
                holder.txt_EE.setText(feeds.event_name);
                try {
                    holder.txt_date.setText(getDateInFormat(feeds.date));
                    holder.txt_time.setText(getTimeInFormat(feeds.date));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (feeds.type.equals(Constant.FEED_TYPE_COMMENT)) {
                holder.img_event.setVisibility(View.GONE);
                holder.txt_comment.setVisibility(View.VISIBLE);
                holder.txt_comment.setText(feeds.feed);
            } else if (feeds.type.equals(Constant.FEED_TYPE_PICTURE)) {
                holder.txt_comment.setVisibility(View.GONE);
                holder.img_event.setVisibility(View.VISIBLE);
                Picasso.with(context).load(WebServices.FEED_IMAGE + feeds.feed).placeholder(R.drawable.def_scene).into(holder.img_event);

            }

            itemView.setTag(holder);
        } else {
            Holder holder  = (Holder) itemView.getTag();
            final Feeds feeds = feedList.get(position);
            holder.txt_time.setVisibility(View.GONE);
            holder.txt_date.setVisibility(View.GONE);
            holder.txt_at.setVisibility(View.GONE);
            if(position==0)holder.gap.setVisibility(View.VISIBLE);
            else holder.gap.setVisibility(View.GONE);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.img_event.getLayoutParams();
            layoutParams.height = HomeActivity.ActivityWidth * 3 / 4;
            holder.img_event.setLayoutParams(layoutParams);
            holder.txt_comment.setLayoutParams(layoutParams);

            if (position == 0) {
                holder.txt_EE.setText(feeds.event_name);
                holder.rtlv_EE.setVisibility(View.VISIBLE);
                try {
                    holder.txt_date.setText(getDateInFormat(feeds.date));
                    holder.txt_time.setText(getTimeInFormat(feeds.date));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if (feeds.event_name.equals(feedList.get(position - 1).event_name)) {
                holder.rtlv_EE.setVisibility(View.GONE);
            } else {
                holder.rtlv_EE.setVisibility(View.VISIBLE);
                holder.txt_EE.setText(feeds.event_name);
                try {
                    holder.txt_date.setText(getDateInFormat(feeds.date));
                    holder.txt_time.setText(getTimeInFormat(feeds.date));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (feeds.type.equals(Constant.FEED_TYPE_COMMENT)) {
                holder.img_event.setVisibility(View.GONE);
                holder.txt_comment.setVisibility(View.VISIBLE);
                holder.txt_comment.setText(feeds.feed);
            } else if (feeds.type.equals(Constant.FEED_TYPE_PICTURE)) {
                holder.txt_comment.setVisibility(View.GONE);
                holder.img_event.setVisibility(View.VISIBLE);
                Picasso.with(context).load(WebServices.FEED_IMAGE + feeds.feed).placeholder(R.drawable.def_scene).into(holder.img_event);
            }

        }
        return itemView;
    }

    /**
     *
     * @param date date should be String like 2017-04-08 13:02:54
     * @return date
     */
    private String getDateInFormat(String date){
        String dateS = date.split(" ")[0];
        String dateArray[] = dateS.split("-");

        return dateArray[2]+"/"+dateArray[1]+"/"+dateArray[0];
    }

    private String getTimeInFormat(String date) {
        String dateS = date.split(" ")[1];
        String dateArray[] = dateS.split(":");
        if (Integer.parseInt(dateArray[0]) > 12) {
            int hour = Integer.parseInt(dateArray[0]) - 12;
            return hour + ":" + dateArray[1] + " pm";
        }
        return dateArray[0] + ":" + dateArray[1] + " am ";
    }

    private class Holder {
        private TextView txt_comment, txt_EE;
        private TextView txt_date, txt_time, txt_at;
        private ImageView img_event;
        private RelativeLayout rtlv_EE;
        private View gap;
    }


}
