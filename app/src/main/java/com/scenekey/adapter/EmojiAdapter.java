package com.scenekey.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.scenekey.R;
import com.scenekey.cus_view.ProfilePopUp;
import com.vanniktech.emoji.EmojiTextView;

/**
 * Created by mindiii on 7/3/18.
 */


public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.Holder> {

    private Context context;
    private String list [];
    private ProfilePopUp profilePopUp;
   private boolean addToRecent;


   //for profilePopup
    public EmojiAdapter(Context context , ProfilePopUp profilePopUp ) {
        this.context = context;
        this.list = context.getResources().getStringArray(R.array.emojiArray);
        this.profilePopUp = profilePopUp;
        addToRecent =  true;

    }


    @Override
    public EmojiAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.custom_emoji,null));
    }


    @Override
    public void onBindViewHolder(EmojiAdapter.Holder holder, int position) {
        holder.tv_emoji.setText(list [position]);
        holder.tv_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profilePopUp.onClickView((TextView) v ,profilePopUp);
                if(addToRecent && profilePopUp.list.size()<=4){
                    profilePopUp.setRecent(profilePopUp.list.size()==4? profilePopUp.list.get(3) :((TextView) v).getText().toString());
                }


            }
        });
    }

    @Override
    public int getItemCount() {
        return list.length;
    }

    public int getIndicatorCount(double recyclerWidth,int tv_width){
        // return (int) ((tv_width*list.length)/(recyclerWidth*4));
        return  (list.length/28);

    }

    public void setList(int i){
        addToRecent =  true;
        switch (i){
            case 0: this.list = context.getResources().getStringArray(R.array.emojiArray);  break;//one
            case 1: this.list = context.getResources().getStringArray(R.array.nature);      break;//three
            case 2: this.list = context.getResources().getStringArray(R.array.things);      break;//two
            case 3: this.list = context.getResources().getStringArray(R.array.buildings);   break;//four
            case 4: this.list = context.getResources().getStringArray(R.array.symbool);     break;//five
            default:this.list = profilePopUp.getRecent(); addToRecent = false ;             break;
        }
        notifyDataSetChanged();
        profilePopUp.updateIndicator();

    }

    class Holder extends RecyclerView.ViewHolder {
        EmojiTextView tv_emoji;

        public Holder(View itemView) {
            super(itemView);
            tv_emoji = itemView.findViewById(R.id.tv_emoji);

        }
    }

}
