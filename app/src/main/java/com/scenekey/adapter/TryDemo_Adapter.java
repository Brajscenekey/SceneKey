package com.scenekey.adapter;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scenekey.R;
import com.scenekey.activity.HomeActivity;
import com.scenekey.cus_view.ProfilePopUp_Demo;
import com.scenekey.fragment.Demo_Event_Fragment;
import com.scenekey.fragment.Home_No_Event_Fragment;
import com.scenekey.fragment.Profile_Fragment;
import com.scenekey.model.EventAttendy;
import com.scenekey.model.NotificationData;
import com.scenekey.model.RoomPerson;
import com.scenekey.util.CircleTransform;
import com.scenekey.util.Utility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by mindiii on 26/2/18.
 */

public class TryDemo_Adapter extends RecyclerView.Adapter<TryDemo_Adapter.ViewHolder> {

    private ArrayList<RoomPerson> roomPersonList;
    private HomeActivity activity;
    private View popupView;
    private Dialog dialog;

    private Bitmap imageArray[];
    private Demo_Event_Fragment demo_event_fragment;


    public TryDemo_Adapter(ArrayList<RoomPerson> roomPersonList, HomeActivity activity, Home_No_Event_Fragment home_no_event, Demo_Event_Fragment demo_event_fragment) {
        this.roomPersonList = roomPersonList;
        this.activity = activity;
        this.imageArray = home_no_event.imageArray;
        this.demo_event_fragment = demo_event_fragment;
    }

    @Override
    public TryDemo_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_demo_room, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TryDemo_Adapter.ViewHolder holder, int i) {
        final RoomPerson person = roomPersonList.get(i);
        final int position = i;
        if(person != null){
            holder.txt_name_gvb1.setText(person.android_version_name.split("\\s+")[0]);
            if (position == 8)
            {
                try {

                    Picasso.with(activity).load(person.android_image_url).transform(new CircleTransform()).into(holder.img_profile_gvb1);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
             else
                holder.img_profile_gvb1.setImageBitmap(imageArray[i]);

            switch (person.status) {
                case "busy":
                    holder.img_profile_gvb1.setBackgroundResource(R.drawable.bg_yellow_ring);
                    holder.txt_name_gvb1.setTextColor(activity.getResources().getColor(R.color.yellow_ring));
                    break;
                case "avilable":
                    holder.img_profile_gvb1.setBackgroundResource(R.drawable.bg_green_ring);
                    holder.txt_name_gvb1.setTextColor(activity.getResources().getColor(R.color.green_ring));
                    break;
                case "na":
                    holder.img_profile_gvb1.setBackgroundResource(R.drawable.bg_red_ring_2);
                    holder.txt_name_gvb1.setTextColor(activity.getResources().getColor(R.color.red_ring));
                    break;
            }

            holder.img_profile_gvb1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position == 8) popUpMyProfile();
                    else newPopUp(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return roomPersonList.size();
    }

    private void popUpMyProfile() {
        final ImageView img_red, img_yellow, img_green, img_p1_profile;
        dialog = new Dialog(activity);
        final TextView txt_stop, txt_caution, txt_go;
        final TextView tv_userName ,txt_my_details;

        popupView = LayoutInflater.from(activity).inflate(R.layout.custom_my_profile_popup, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(popupView);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        LinearLayout llMyProfile =  popupView.findViewById(R.id.llMyProfile);
        img_p1_profile =  popupView.findViewById(R.id.img_p1_profile);
        img_green =  popupView.findViewById(R.id.img_green);
        img_yellow =  popupView.findViewById(R.id.img_yellow);
        img_red =  popupView.findViewById(R.id.img_red);
        txt_stop =  popupView.findViewById(R.id.txt_stop);
        txt_caution =  popupView.findViewById(R.id.txt_caution);
        txt_go =  popupView.findViewById(R.id.txt_go);
        txt_my_details =  popupView.findViewById(R.id.txt_my_details);
        ImageView img_cross =  popupView.findViewById(R.id.img_cross);
        tv_userName =  popupView.findViewById(R.id.tv_userName);
        TextView txt_bio =  popupView.findViewById(R.id.tv_my_bio);

        tv_userName.setText(activity.userInfo().firstName);
        Utility.e("bio check",activity.userInfo().bio);
        txt_bio.setText(activity.userInfo().bio);

        llMyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activity.hideStatusBar();
            }
        });

        img_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_red.setImageResource(R.drawable.bg_red_ring_2);
                img_yellow.setImageResource(R.drawable.bg_yellow_ring);
                setUserStatus(2, (ImageView) v);

            }
        });
        img_yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_green.setImageResource(R.drawable.bg_green_ring);
                img_red.setImageResource(R.drawable.bg_red_ring_2);
                setUserStatus(1, (ImageView) v);
            }
        });
        img_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_yellow.setImageResource(R.drawable.bg_yellow_ring);
                img_green.setImageResource(R.drawable.bg_green_ring);
                setUserStatus(3, (ImageView) v);
            }
        });
        img_p1_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAttendy eventAttendy = new EventAttendy();
                eventAttendy.setUserimage(activity.userInfo().getUserImage());
                eventAttendy.username=(activity.userInfo().userName);
                eventAttendy.userid=(activity.userInfo().userID);
                eventAttendy.userFacebookId=(activity.userInfo().facebookId);
                callProfile(eventAttendy,true);
            }
        });
        txt_my_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventAttendy eventAttendy = new EventAttendy();
                eventAttendy.setUserimage(activity.userInfo().getUserImage());
                eventAttendy.username=(activity.userInfo().userName);
                eventAttendy.userid=(activity.userInfo().userID);
                eventAttendy.userFacebookId=(activity.userInfo().facebookId);
                callProfile(eventAttendy,true);
            }
        });

        img_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        switch (roomPersonList.get(8).status) {
            case "na":
                img_red.setImageResource(R.drawable.bg_red_ring_accept);
                break;
            case "busy":
                img_yellow.setImageResource(R.drawable.bg_yellow_ring_accept);
                break;
            case "avilable":
                img_green.setImageResource(R.drawable.bg_green_ring_accept);
                break;
        }


        Picasso.with(activity).load(roomPersonList.get(8).android_image_url).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(img_p1_profile);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.gravity = Gravity.CENTER;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = HomeActivity.ActivityWidth ;
        dialog.getWindow().setAttributes(lp);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activity.hideStatusBar();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

    }


   /* on Click popUpMyProfile start here  */

    private void setUserStatus(int i, ImageView imageView) {
        switch (i) {
            case 1:
                roomPersonList.get(8).status="busy";
                imageView.setImageResource(R.drawable.bg_yellow_ring_accept);
                break;
            case 2:
                roomPersonList.get(8).status="avilable";
                imageView.setImageResource(R.drawable.bg_green_ring_accept);
                break;
            case 3:
                roomPersonList.get(8).status="na";
                imageView.setImageResource(R.drawable.bg_red_ring_accept);
                break;

        }
        notifyDataSetChanged();
    }

    private void callProfile(EventAttendy attendy ,boolean ownProfile) {
        dialog.dismiss();
        try {
            activity.addFragment(new Profile_Fragment().setData(attendy, ownProfile,demo_event_fragment), 1);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void newPopUp(final int value){
        final NotificationData person = demo_event_fragment.getData(value);
        new ProfilePopUp_Demo(activity, 4, person ,0) {
            @Override
            public void onClickView(TextView textView, ProfilePopUp_Demo profilePopUp) {
                profilePopUp.setText(textView.getText().toString());
            }

            @Override
            public void onSendCLick(TextView textView, ProfilePopUp_Demo profilePopUp, NotificationData obj) {
                dismiss();
                Utility utility=new Utility(activity);
                utility.showCustomPopup("Good Nudge!", String.valueOf(R.font.montserrat_medium));
            }

            @Override
            public void onPrevClick(ImageView textView, ProfilePopUp_Demo profilePopUp) {

            }

            @Override
            public void onNextClick(ImageView textView, ProfilePopUp_Demo profilePopUp) {

            }

            @Override
            public void onDismiss(ProfilePopUp_Demo profilePopUp) {

            }
        }.show();
        /*new ProfilePopUp(context,4,obj) {
            @Override
            public void onClickView(TextView textView, ProfilePopUp profilePopUp) {
                profilePopUp.setText(textView.getText().toString());

            }

            @Override
            public void onSendCLick(TextView textView, ProfilePopUp profilePopUp) {
                Log.e("Value " , profilePopUp.list.toString());
            }
        }.show();*/
    }

    /* on Click popUpMyProfile end here */

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_name_gvb1;
        private ImageView img_profile_gvb1;

        ViewHolder(View view) {
            super(view);
            txt_name_gvb1 = view.findViewById(R.id.txt_name_gvb1);
            img_profile_gvb1 = view.findViewById(R.id.img_profile_gvb1);
        }
    }

}
