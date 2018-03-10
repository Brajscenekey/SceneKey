package com.scenekey.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCredentialsProvider;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.scenekey.R;
import com.scenekey.activity.HomeActivity;
import com.scenekey.aws_service.AWSImage;
import com.scenekey.cus_view.ProfilePopUp;
import com.scenekey.fragment.Event_Fragment;
import com.scenekey.fragment.Profile_Fragment;
import com.scenekey.helper.WebServices;
import com.scenekey.model.EventAttendy;
import com.scenekey.util.CircleTransform;
import com.scenekey.util.SceneKey;
import com.scenekey.util.Utility;
import com.scenekey.volleymultipart.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import org.apache.commons.lang3.StringEscapeUtils;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private final String TAG = DataAdapter.class.toString();

    private HomeActivity activity;
    private Context context;
    private String data[];
    private ImageView img_p1_profile;

    private  Dialog dialog;
    private int currentImage;
    private Event_Fragment fragment;
    private DataAdapter dataAdapter = this;
    private ArrayList<EventAttendy> roomPersons;
    private int count;

    private AWSImage awsImage;
    private CognitoCredentialsProvider credentialsProvider;


    public DataAdapter(Activity activity, AWSImage awsImage, ArrayList<EventAttendy> list, String[] data, Event_Fragment fragment) {
        this.roomPersons = list;
        context=activity;
        this.activity = (HomeActivity) activity;
        this.data = data;
        this.fragment = fragment;
        this.awsImage = awsImage;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_demo_room, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, int i) {
        final EventAttendy attendy = roomPersons.get(i);
        final int position = i;
        viewHolder.txt_name_gvb1.setText(attendy.username);   //roomPersons.get(i).username.split("\\s+")[0]
        try {
            Picasso.with(activity).load(attendy.getUserimage()).placeholder(R.drawable.image_defult_profile).transform(new CircleTransform()).into(viewHolder.img_profile_gvb1);
        }catch (Exception e){
            e.printStackTrace();
        }
        switch (attendy.user_status) {

            case "1":
                viewHolder.img_profile_gvb1.setBackgroundResource(R.drawable.bg_green_ring);
                viewHolder.txt_name_gvb1.setTextColor(activity.getResources().getColor(R.color.green_ring));
                break;
            case "2":
                viewHolder.img_profile_gvb1.setBackgroundResource(R.drawable.bg_yellow_ring);
                viewHolder.txt_name_gvb1.setTextColor(activity.getResources().getColor(R.color.yellow_ring));
                break;
            case "3":
                viewHolder.img_profile_gvb1.setBackgroundResource(R.drawable.bg_red_ring_2);
                viewHolder.txt_name_gvb1.setTextColor(activity.getResources().getColor(R.color.red_ring));
                break;
            default:
                viewHolder.img_profile_gvb1.setBackgroundResource(R.drawable.bg_red_ring_2);
                viewHolder.txt_name_gvb1.setTextColor(activity.getResources().getColor(R.color.red_ring));
                break;
        }
        /*if(attendy.userid.equals(activity.userInfo().userID)){

            viewHolder.img_profile_gvb1.setBackgroundResource(R.drawable.bg_red_ring);
            viewHolder.txt_name_gvb1.setTextColor(activity.getResources().getColor(R.color.red_ring));
        }*/
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attendy.userid.equals(activity.userInfo().userID)) {
                    awsImage.downloadFileFromS3(SceneKey.sessionManager.getFacebookId(), (credentialsProvider == null ? credentialsProvider = awsImage.getCredentials() : credentialsProvider));
                    popUpMy(position );
                } else {
                    try {
                        if (fragment.check()) {
                            awsImage.downloadFileFromS3(awsImage.getFacebookId(attendy.userFacebookId, attendy.userid), (credentialsProvider == null ? credentialsProvider = awsImage.getCredentials() : credentialsProvider));
                            newPopUp(attendy, false);
                        }
                            //    popupRoom(position);

                        else fragment.cantInteract();
                    } catch (ParseException e) {
                        Utility.showToast(activity,activity.getString(R.string.somethingwentwrong),0);
                    }

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomPersons.size();
    }

    private void popUpMy(final int position) {
        final ImageView img_red, img_yellow, img_green;
        dialog = new Dialog(activity, android.R.style.Theme_Translucent);
        final TextView txt_stop, txt_caution, txt_go;
        final TextView txt_title ,txt_my_details;

        View popupView = LayoutInflater.from(activity).inflate(R.layout.custom_my_profile_popup, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(popupView);

        img_p1_profile = popupView.findViewById(R.id.img_p1_profile);
        LinearLayout llMyProfile = popupView.findViewById(R.id.llMyProfile);
        img_green = popupView.findViewById(R.id.img_green);
        img_yellow = popupView.findViewById(R.id.img_yellow);
        img_red = popupView.findViewById(R.id.img_red);
        ImageView img_left = popupView.findViewById(R.id.img_left);
        ImageView img_right = popupView.findViewById(R.id.img_right);
        txt_stop = popupView.findViewById(R.id.txt_stop);
        txt_caution = popupView.findViewById(R.id.txt_caution);
        txt_go = popupView.findViewById(R.id.txt_go);
        txt_title = popupView.findViewById(R.id.txt_title);
        txt_my_details = popupView.findViewById(R.id.txt_my_details);
        TextView txt_bio = popupView.findViewById(R.id.tv_my_bio);

        txt_bio.setText(activity.userInfo().bio);


        llMyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activity.hideStatusBar();
            }
        });

        img_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImage(false);
            }
        });


        img_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImage(true);
            }
        });


        img_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_red.setImageResource(R.drawable.bg_red_ring_2);
                img_yellow.setImageResource(R.drawable.bg_yellow_ring);
                setUserStatus(1, (ImageView) v);

            }
        });
        img_yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_green.setImageResource(R.drawable.bg_green_ring);
                img_red.setImageResource(R.drawable.bg_red_ring_2);
                setUserStatus(2, (ImageView) v);
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
        txt_my_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callProfile(roomPersons.get(position),true,0);
            }
        });
        img_p1_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callProfile(roomPersons.get(position),true,0);
            }
        });


        switch (roomPersons.get(position).user_status) {
            case "1":
                img_green.setImageResource(R.drawable.bg_green_ring_accept);
                break;
            case "2":
                img_yellow.setImageResource(R.drawable.bg_yellow_ring_accept);
                break;
            case "3":
                img_red.setImageResource(R.drawable.bg_red_ring_accept);
                break;
            default:
                img_red.setImageResource(R.drawable.bg_red_ring_accept);
                break;
        }


        Picasso.with(activity).load(roomPersons.get(position).getUserimage()).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(img_p1_profile);

       /* WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.gravity = Gravity.CENTER;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //lp.width = HomeActivity.ActivityWidth - ((int) activity.getResources().getDimension(R.dimen._30sdp)); old
        lp.width = HomeActivity.ActivityWidth ;
        dialog.getWindow().setAttributes(lp);*/

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activity.hideStatusBar();
            }
        });
        dialog.show();
        //popupView.setBackgroundColor(0);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

    }

    private void setUserStatus(int i, ImageView imageView) {

        switch (i) {
            case 1:
                imageView.setImageResource(R.drawable.bg_green_ring_accept);
                setUserStatus(i);
                break;
            case 2:
                imageView.setImageResource(R.drawable.bg_yellow_ring_accept);
                setUserStatus(i);
                break;
            case 3:
                imageView.setImageResource(R.drawable.bg_red_ring_accept);
                setUserStatus(i);
                break;

        }
    }

    private void setUserStatus(final int value){
        final Utility utility=new Utility(context);

        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.SET_STATUS, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Utility.e(TAG,response);
                    activity.dismissProgDialog();
                    fragment.getAllData();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    utility.volleyErrorListner(e);
                    activity.dismissProgDialog();
                    if(dialog!=null)dialog.dismiss();
                    Utility.showToast(context,context.getResources().getString(R.string.somethingwentwrong),0);
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("status",value+"");
                    params.put("user_id",activity.userInfo().userID);

                    Utility.e(TAG, " params " + params.toString());
                    return params;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        } else {
            Utility.showToast(context, context.getResources().getString(R.string.internetConnectivityError), 0);
            activity.dismissProgDialog();
        }
    }

    private void callProfile(EventAttendy attendy ,boolean ownProfile,@Nullable int Facebook) {
        dialog.dismiss();
        try {
            activity.addFragment(new Profile_Fragment().setData(attendy, ownProfile, fragment,Facebook), 1);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void newPopUp(final EventAttendy obj ,boolean myprofile){
        activity.showProgDialog(false, TAG);
        new ProfilePopUp(activity, awsImage, 4, obj) {
            @Override
            public void onClickView(TextView textView, ProfilePopUp profilePopUp ) {
                profilePopUp.setText(textView.getText().toString());

            }

            @Override
            public void onSendCLick(TextView textView, ProfilePopUp profilePopUp) {
                Log.e("Value " , profilePopUp.list.toString());
                String s = profilePopUp.list.toString();
                byte[] ptext = (s= s.substring(1,s.length()-1).replace("","")).getBytes();

                activity.showProgDialog(false,TAG);
                fragment.addNudge(obj.userid, obj.userFacebookId , StringEscapeUtils.escapeJava(s).replace(" +",""),profilePopUp);
            }
        }.show();
    }


    private void setImage(boolean isRight){
        if (awsImage.imageList.size() != 0) {
            currentImage = (isRight ? (currentImage == awsImage.imageList.size() - 1 ? 0 : currentImage + 1) : (currentImage == 0 ? awsImage.imageList.size() - 1 : currentImage - 1));
            Picasso.with(activity).load(awsImage.imageList.get(currentImage).path).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(img_p1_profile);
        }
    }

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
