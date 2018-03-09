package com.scenekey.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.scenekey.R;
import com.scenekey.activity.HomeActivity;
import com.scenekey.activity.ImageUploadActivity;
import com.scenekey.adapter.Profile_Adapter;
import com.scenekey.helper.Constant;
import com.scenekey.helper.WebServices;
import com.scenekey.model.EventAttendy;
import com.scenekey.model.Feeds;
import com.scenekey.model.ImagesUpload;
import com.scenekey.model.UserInfo;
import com.scenekey.util.CircleTransform;
import com.scenekey.util.SceneKey;
import com.scenekey.util.StatusBarUtil;
import com.scenekey.util.Utility;
import com.scenekey.volleymultipart.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile_Fragment extends Fragment implements View.OnClickListener {

    private final String TAG = Profile_Fragment.class.toString();
    private Context context;
    private HomeActivity activity;
    private Utility utility;
    private CognitoCredentialsProvider credentialsProvider;
    private ImageView img_profile_pic,img_profile_pic2;
    private EventAttendy attendy;
    private boolean myProfile;
    private Event_Fragment event_fragment;
    private Key_In_Event_Fragment key_in_event_fragment;
    private Demo_Event_Fragment demo_event_fragment;
    private Fragment fragment;

    private ListView listViewFragProfile;
    private ImageView img_cross,img_left,img_right;
    private TextView txt_event_count,txt_dimmer;

    private ArrayList<Feeds> feedsList;
    private ArrayList<ImagesUpload> imageList;
    private int currentImage,pageToshow=1;
    private Profile_Adapter adapter;
    private boolean clicked;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_profile, container, false);
        // LinearLayout mainLayout = v.findViewById(R.id.mainlayout);
        listViewFragProfile = v.findViewById(R.id.listViewFragProfile);
        imageList = new ArrayList<>();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

      Handler handler=  new Handler();

      handler.post(new Runnable() {
          @Override
          public void run() {
              downloadFileFromS3((credentialsProvider==null?credentialsProvider = getCredentials():credentialsProvider));
          }
      });

      handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.showProgDialog(false,TAG);
                getProfileDataApi();
            }
        },200);

        feedsList = new ArrayList<>();
        adapter = new Profile_Adapter(context,feedsList,myProfile);
        listViewFragProfile.setAdapter(adapter);

        LayoutInflater inflater = activity.getLayoutInflater();

        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.adapter_custom_profile_header, listViewFragProfile, false);
        listViewFragProfile.addHeaderView(header, null, false);

        img_profile_pic2 = view.findViewById(R.id.img_profile_pic2);
         img_profile_pic = header.findViewById(R.id.img_profile_pic);
        img_left = view.findViewById(R.id.img_left);
        img_right = view.findViewById(R.id.img_right);
        img_cross = view.findViewById(R.id.img_cross);
/*
comment for:- fb and count not show for current scenario
 img_fb = header.findViewById(R.id.img_fb);
 txt_f2_badge = header.findViewById(R.id.txt_f2_badge);
*/

        ImageView img_capture = header.findViewById(R.id.img_capture);
        ImageView img_setting = header.findViewById(R.id.img_setting);
        ImageView img_back = header.findViewById(R.id.img_back);
        txt_event_count = header.findViewById(R.id.txt_event_count);
        txt_dimmer = view.findViewById(R.id.txt_dimmer);
        TextView txt_profile_name = header.findViewById(R.id.txt_profile_name);
        txt_profile_name.setText(attendy.username);

        setClick(view, img_back ,img_setting, img_profile_pic,txt_dimmer,img_cross, img_capture,img_right,img_left);

        img_profile_pic2.setVisibility(View.INVISIBLE);
        Utility.e(TAG,SceneKey.sessionManager.getUserInfo().getUserImage());

        Picasso.with(activity).load(SceneKey.sessionManager.getUserInfo().getUserImage()).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(img_profile_pic);
        Picasso.with(activity).load(SceneKey.sessionManager.getUserInfo().getUserImage()).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(img_profile_pic2);

 /*
 //fb and badge
 if (myProfile) {
 img_fb.setVisibility(View.GONE);
 txt_f2_badge.setVisibility(View.GONE);

 } else {
 img_capture.setVisibility(View.GONE);
 }


 if(mutulFriendCount<99)txt_f2_badge.setText(mutulFriendCount+"");
 else txt_f2_badge.setText("99+");*/

    }

    private void setClick(View... views) {
        for (View view : views) {
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
        activity= (HomeActivity) getActivity();
        utility=new Utility(context);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.img_profile_pic:
                profileImgClick();
                break;
            case R.id.img_cross:
                crossImgClicked();
                break;
            case R.id.img_back:
                activity.onBackPressed();
                break;
            case R.id.img_setting:
                if(!clicked){
                    //TODo Remove autocomplete fragment before call.
                    if(myProfile)activity.addFragment(new Setting_Fragment().setData(this),1);//TODO check button color in both case (own ,other)
                    clicked = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            clicked = false;
                        }
                    }, 4000);
                }
                break;
            case R.id.img_capture:
                if(myProfile){
                   Intent i= new Intent(context, ImageUploadActivity.class);
                   i.putExtra("from","profile");
                    activity.startActivityForResult(i, Constant.IMAGE_UPLOAD_CALLBACK);
                    Constant.DONE_BUTTON_CHECK=1;
                }
               // Utility.showToast(context,getString(R.string.underDevelopment),0);
                break;
            case R.id.img_right:
                setImage(true);
                break;
            case R.id.img_left:
                setImage(false);
                break;
            case R.id.txt_dimmer:
                crossImgClicked();
                break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        activity.setBBVisibility(View.GONE,TAG);
        //activity.setTopStatus();
        if(event_fragment != null)event_fragment.canCallWebservice = false;
        if(key_in_event_fragment != null)key_in_event_fragment.canCallWebservice = false;
    }

    @Override
    public void onResume() {
        activity.setBBVisibility(View.GONE,TAG);
      //  activity.setTopStatus();
        if(event_fragment != null)event_fragment.canCallWebservice = false;
        if(key_in_event_fragment != null)key_in_event_fragment.canCallWebservice = false;
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(event_fragment != null){
            event_fragment.canCallWebservice = true;
            activity.hideStatusBar();
        }
        if(key_in_event_fragment != null){
            key_in_event_fragment.canCallWebservice = true;
            activity.hideStatusBar();
        }
        if(fragment != null && fragment instanceof Add_Event_Fragment){
            activity.setBBVisibility(View.VISIBLE,TAG);
            activity.setTitleVisibility(View.GONE);
            return;
        }
        if(event_fragment == null && key_in_event_fragment == null) {
            if(demo_event_fragment ==null){
                activity.setBBVisibility(View.VISIBLE,TAG);
                activity.setTitleVisibility(View.VISIBLE);
                activity.backPressToPosition();
            }
        }

    }

    private void profileImgClick() {
        listViewFragProfile.smoothScrollToPosition(0);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.profile_pic_scale_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                img_profile_pic2.setAlpha(1.0f);
                img_profile_pic2.setVisibility(View.VISIBLE);

 /*Animation dimmer = AnimationUtils.loadAnimation(getContext(), R.anim.alpha_to_full);
 txt_dimmer.startAnimation(dimmer);*/
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                StatusBarUtil.setColorNoTranslucent(activity,getResources().getColor(R.color.black70p));
                txt_dimmer.setVisibility(View.VISIBLE);
                img_cross.setVisibility(View.VISIBLE);
                img_right.setVisibility(View.VISIBLE);
                img_left.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                Utility.e(TAG, "Animation Repeat");
            }
        });

        img_profile_pic2.startAnimation(animation);

    }

    private void crossImgClicked() {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.profile_pic_scale_down);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                img_profile_pic2.setAlpha(1.0f);
                //img_profile_pic2.setBorderColor(getResources().getColor(R.color.colorPrimary));
                Animation dimmer = AnimationUtils.loadAnimation(getContext(), R.anim.alpha_to_o);
                //txt_dimmer.startAnimation(dimmer);

                img_right.startAnimation(dimmer);
                img_left.startAnimation(dimmer);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                txt_dimmer.setVisibility(View.GONE);
                img_cross.setVisibility(View.GONE);
                img_profile_pic2.setVisibility(View.GONE);

                img_right.setVisibility(View.GONE);
                img_left.setVisibility(View.GONE);
                StatusBarUtil.setColorNoTranslucent(activity,getResources().getColor(R.color.white));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                Utility.e(TAG, "Animation Repeat");
            }
        });
        img_profile_pic2.startAnimation(animation);
    }

    private void setImage(boolean isRight){
        if(imageList.size()!=0){
            currentImage=(isRight?(currentImage==imageList.size()-1? 0 : currentImage+1):(currentImage==0? imageList.size()-1 :currentImage-1));
            Picasso.with(activity).load(imageList.get(currentImage).path).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(img_profile_pic2);
        }
    }

/* Profile setData start here */

    /**
     * @param attendy if do not Eventattendy object just create one , set userId URL and pass it.
     * @param myProfile if user comming to show his own profile then true otherwise false.
     * @return setData content
     */
    public Profile_Fragment setData(EventAttendy attendy, boolean myProfile, Event_Fragment fragment,int mutulFriendCount) {
        this.attendy = attendy;
        this.myProfile = myProfile;
        this.event_fragment = fragment;
        //this.mutulFriendCount = mutulFriendCount;
        return this;
    }

    /**
     * @param attendy if do not Eventattendy object just create one , set userId URL and pass it.
     * @param myProfile if user comming to show his own profile then true otherwise false.
     * @return setData content
     */
    public Profile_Fragment setData(EventAttendy attendy, boolean myProfile, Key_In_Event_Fragment fragment) {
        this.attendy = attendy;
        this.myProfile = myProfile;
        this.key_in_event_fragment = fragment;
        return this;
    }

    /**
     * @param attendy if do not Eventattendy object just create one , set userId URL and pass it.
     * @param myProfile if user comming to show his own profile then true otherwise false.
     * @return setData content
     */
    public Profile_Fragment setData(EventAttendy attendy, boolean myProfile, @Nullable Demo_Event_Fragment fragment) {
        this.attendy = attendy;
        this.myProfile = myProfile;
        this.demo_event_fragment = fragment;
        return this;
    }

    /**
     * @param attendy if do not Eventattendy object just create one , set userId URL and pass it.
     * @param myProfile if user comming to show his own profile then true otherwise false.
     * @return setData content
     */
    public Profile_Fragment setData(EventAttendy attendy, boolean myProfile, Fragment fragment) {
        this.attendy = attendy;
        this.myProfile = myProfile;
        this.fragment = fragment;
        return this;
    }

 /* Profile setData end here */

    private void getProfileDataApi() {

        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.LISTATTENDEDEVENT, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Utility.printBigLogcat(TAG, response);
                    try {
                        getResponse(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (feedsList == null) {
                        feedsList = new ArrayList<>();
                    }
                    activity.dismissProgDialog();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    utility.volleyErrorListner(e);
                    activity.dismissProgDialog();
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("user_id",attendy.userid);
                    params.put("type","app");
                    params.put("myId",activity.userInfo().userID);

                    Utility.e(TAG," params "+params.toString());
                    return params;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        }else{
            utility.snackBar(listViewFragProfile,getString(R.string.internetConnectivityError),0);
            activity.dismissProgDialog();
        }
    }

    private synchronized void getResponse(String response) throws JSONException {
        if (feedsList == null) {
            feedsList = new ArrayList<>();
            feedsList.clear();
        }
        JSONObject object = new JSONObject(response);

        try {
            if (object.has("myInfo")) {
                UserInfo userInfo = activity.userInfo();
                JSONObject user = object.getJSONObject("myInfo");
                if(user.has("makeAdmin")) userInfo.makeAdmin=(user.getString("makeAdmin"));
                if(user.has("lat")) userInfo.latitude=(user.getString("lat"));
                if(user.has("longi")) userInfo.longitude=(user.getString("longi"));
                if(user.has("address")) userInfo.address=(user.getString("address"));
                if(user.has("fullname")) userInfo.fullName=(user.getString("fullname"));
                if(user.has("key_points"))userInfo.keyPoints=(user.getString("key_points"));

                Utility.e("Profile session update.",userInfo.getUserImage());
                activity.updateSession(userInfo);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        if (object.has("allfeeds")) {
            JSONArray array = object.getJSONArray("allfeeds");
            for (int i = 0; i < array.length(); i++) {
                Feeds feeds = new Feeds();
                JSONObject feedJson = array.getJSONObject(i);

                if (feedJson.has("username")) feeds.username=(feedJson.getString("username"));
                if (feedJson.has("userid")) feeds.userid=(feedJson.getString("userid"));
                if (feedJson.has("userFacebookId"))
                    feeds.userFacebookId=(feedJson.getString("userFacebookId"));
                if (feedJson.has("event_id")) feeds.event_id=(feedJson.getString("event_id"));
                if (feedJson.has("ratetype")) feeds.ratetype=(feedJson.getString("ratetype"));
                if (feedJson.has("event_name"))
                    feeds.event_name=(feedJson.getString("event_name"));
                if (feedJson.has("userimage")) feeds.userimage=(feedJson.getString("userimage"));
                if (feedJson.has("type")) feeds.type=(feedJson.getString("type"));
                if (feedJson.has("location")) feeds.location=(feedJson.getString("location"));
                if (feedJson.has("date")) feeds.date=(feedJson.getString("date"));
                if (feedJson.has("feed")) feeds.feed=(feedJson.getString("feed"));

                feedsList.add(feeds);
                if(i==1)activity.dismissProgDialog();

            }
            adapter.notifyDataSetChanged();

        }

        if(feedsList.size()==0){
            Utility.showToast(context," No event found !",0);
            adapter.notifyDataSetChanged();
        }
        if (object.has("keyin_count")) {
            txt_event_count.setText(object.getInt("keyin_count") + " events");
        }
        //setRecyclerView();
        //rclv_f3_trending.setHasFixedSize(true);
    }


 /* get image from server start here*/

    private void downloadFileFromS3(CognitoCredentialsProvider credentialsProvider){//, CognitoCachingCredentialsProvider credentialsProvider){
        try {
            final AmazonS3Client s3Client;
            s3Client = new AmazonS3Client(credentialsProvider);

            // Set the region of your S3 bucket
            s3Client.setRegion(Region.getRegion(Regions.US_WEST_1));
            Thread thread = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        ObjectListing listing = s3Client.listObjects( "scenekey-profile-images", SceneKey.sessionManager.getFacebookId());
                        List<S3ObjectSummary> summaries = listing.getObjectSummaries();


                        while (listing.isTruncated()) {

                            listing = s3Client.listNextBatchOfObjects (listing);
                            summaries.addAll (listing.getObjectSummaries());

                        }
                        updateImages(summaries);

                        Utility.e(TAG, "listing "+ summaries.get(0).getKey()+"no of image "+summaries.size());

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Utility.e(TAG, "Exception found while listing "+ e);
                    }
                }
            });

            thread.start();
            activity.dismissProgDialog();
        }
        catch (Exception e){
            Utility.e("AMAZON",e.toString());
            activity.dismissProgDialog();
        }
    }


    private void updateImages(final List<S3ObjectSummary> summaries){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(S3ObjectSummary obj :summaries ){
                    imageList.add(new ImagesUpload(obj.getKey()));
                }
                try{
                    Picasso.with(activity).load(imageList.get(currentImage).path).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(img_profile_pic2);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

 /* get image from server end here*/

    public CognitoCredentialsProvider getCredentials(){
        CognitoCredentialsProvider credentialsProvider = new CognitoCredentialsProvider( "us-west-2:86b58a3e-0dbd-4aad-a4eb-e82b1a4ebd91",Regions.US_WEST_2);
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3, context);

        Map<String, String> logins = new HashMap<String, String>();

        try{
            logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
        }catch (Exception e){
            e.printStackTrace();
        }


        credentialsProvider.setLogins(logins);
        return credentialsProvider;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constant.IMAGE_UPLOAD_CALLBACK) {
            if(resultCode == Activity.RESULT_OK){
                boolean isValue=data.getBooleanExtra("isResult",false);
                if (isValue){
                    //reload image
                    imageList.clear();
                   UserInfo userInfo= SceneKey.sessionManager.getUserInfo();
                   userInfo.userImage=SceneKey.sessionManager.getUserInfo().getUserImage();
                   activity.updateSession(userInfo);
                    Picasso.with(activity).load(SceneKey.sessionManager.getUserInfo().getUserImage()).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(img_profile_pic);
                    Picasso.with(activity).load(SceneKey.sessionManager.getUserInfo().getUserImage()).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(img_profile_pic2);
                    Picasso.with(activity).load(SceneKey.sessionManager.getUserInfo().getUserImage()).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(activity.img_profile);
                    activity.showProgDialog(false,TAG);
                    downloadFileFromS3((credentialsProvider==null?credentialsProvider = this.getCredentials():credentialsProvider));
                }
            }

        }
    }//onActivityResult

}
