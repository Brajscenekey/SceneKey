package com.scenekey.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.scenekey.R;
import com.scenekey.activity.HomeActivity;
import com.scenekey.adapter.DataAdapter;
import com.scenekey.cus_view.Grid_multiRow;
import com.scenekey.helper.Constant;
import com.scenekey.helper.Permission;
import com.scenekey.helper.WebServices;
import com.scenekey.lib_sources.Floting_menuAction.FloatingActionButton;
import com.scenekey.lib_sources.Floting_menuAction.FloatingActionMenu;
import com.scenekey.lib_sources.SwipeCard.Card;
import com.scenekey.lib_sources.SwipeCard.CardsAdapter;
import com.scenekey.lib_sources.SwipeCard.SwipeCardView;
import com.scenekey.listener.StatusBarHide;
import com.scenekey.model.EventAttendy;
import com.scenekey.model.EventDetails;
import com.scenekey.model.Events;
import com.scenekey.model.NotificationData;
import com.scenekey.model.Tags;
import com.scenekey.model.UserInfo;
import com.scenekey.util.ImageUtil;
import com.scenekey.util.SceneKey;
import com.scenekey.util.Utility;
import com.scenekey.volleymultipart.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

public class Event_Fragment extends Fragment implements View.OnClickListener,StatusBarHide {

    public final String TAG = Event_Fragment.class.toString();

    private Context context;
    private HomeActivity activity;
    private Utility utility;

    public Boolean canCallWebservice,isInfoVisible,isPopUpShowing,canGetNotification;

    public Double latitude,longitude;
    private String eventId,venueName;
    private String[] currentLatLng;
    private int currentNudge,noNotify,timer;

    private LinearLayout info_view,no_one;
    private RelativeLayout rtlv2_animate_f2,rtlv_top,demoView; //Demo Screen
    private ImageView img_infoget_f2, img_f10_back,image_map,img_notif;
    public TextView txt_event_name,txt_not_started,txt_discrp,txt_room,txt_f2_badge,txt_calender_i1,
            txt_hide_all_two,txt_hide_all_one,btn_got_it,txt_discipI_f2;

    private RecyclerView rclv_grid;
    private ScrollView scrl_all;

    public FloatingActionButton fabMenu1_like,fabMenu2_picture;
    private FloatingActionMenu menu_blue;

    private Handler handler;
    private View popupview;
    private Dialog dialog;

    private static Timer timerHttp;
    private Timer timerNudge;

    //model
    private NotificationData nudge;
    private EventDetails eventDetails;
    private Events event;

    //Tinder Swipe
    private SwipeCardView card_stack_view;

    //arrayLists
    public ArrayList<Card> cardsList;
    private ArrayList<NotificationData> nudgelist;

    //map data
    private MapView map_view;
    private GoogleMap googleMap;

    //ProfilePopUp_Notificaiton popup;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_demo_event, container, false);

        //TODO handling on grid adapter click if user is not key in

        txt_discipI_f2 = view.findViewById(R.id.txt_discipI_f2);
        txt_calender_i1 = view.findViewById(R.id.txt_calender_i1);
        info_view = view.findViewById(R.id.info_view);
        img_infoget_f2 = view.findViewById(R.id.img_infoget_f2);
        rclv_grid = view.findViewById(R.id.rclv_grid);
        img_f10_back = view.findViewById(R.id.img_f10_back);
        txt_f2_badge = view.findViewById(R.id.txt_f2_badge);
        txt_event_name = view.findViewById(R.id.txt_event_name);
        txt_discrp = view.findViewById(R.id.txt_discrp);
        txt_room = view.findViewById(R.id.txt_room);

        scrl_all = view.findViewById(R.id.scrl_all);
        image_map = view.findViewById(R.id.image_map);
        rtlv_top = view.findViewById(R.id.rtlv_top);
        menu_blue = view.findViewById(R.id.menu_blue);
        txt_not_started = view.findViewById(R.id.txt_not_started);

        map_view = view.findViewById(R.id.map_view);
        map_view.onCreate(savedInstanceState);
        map_view.onResume();

        img_notif = view.findViewById(R.id.img_notif);
        card_stack_view = view.findViewById(R.id.card_stack_view);//TinderSwipe
        rtlv2_animate_f2 = view.findViewById(R.id.rtlv2_animate_f2);
        txt_hide_all_one = view.findViewById(R.id.txt_hide_all_one);
        txt_hide_all_two = view.findViewById(R.id.txt_hide_all_two);
        no_one = view.findViewById(R.id.no_one);
        demoView = view.findViewById(R.id.demoView);
        btn_got_it = view.findViewById(R.id.btn_got_it);
        activity.showProgDialog(false, TAG);

        menu_blue.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened) {
                    if (eventDetails != null && eventDetails.getProfile_rating() != null) {
                        if (eventDetails.getProfile_rating().getKey_in().equals(Constant.KEY_NOTEXIST)) {
                            try {
                                if (userInfo().makeAdmin.equals(Constant.ADMIN_YES)) {
                                    addUserIntoEvent(-1, null);
                                } else if (activity.getDistance(new Double[]{latitude, longitude, Double.valueOf(currentLatLng[0]), Double.valueOf(currentLatLng[1])}) <= Constant.MAXIMUM_DISTANCE && activity.checkWithTime(eventDetails.getProfile_rating().getEvent_date(), Double.parseDouble(eventDetails.getProfile_rating().getInterval()))) {
                                    addUserIntoEvent(-1, null);
                                } else cantJoinDialog();
                            } catch (ParseException d) {
                                d.getMessage();
                            }
                        }
                    }
                }
            }
        });

        activity.setBBVisibility(View.GONE,TAG);
        activity.hideStatusBar();

        txt_discipI_f2.setText(event.getVenue().getAddress());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fabMenu1_like = view.findViewById(R.id.fabMenu1_like);
        ImageView img_edit_i1 = view.findViewById(R.id.img_edit_i1);
        fabMenu2_picture = view.findViewById(R.id.fabMenu2_picture);
        FloatingActionButton fabMenu3_comment = view.findViewById(R.id.fabMenu3_comment);
        fabMenu1_like.setTextView(new TextView[]{txt_hide_all_one, txt_hide_all_two});
        rtlv_top.getLayoutParams().height = ((HomeActivity.ActivityWidth) * 3 / 4);
        getAllData();

        if (userInfo().firstTimeDemo) {
            demoView.setVisibility(View.VISIBLE);
            userInfo().firstTimeDemo = (false);
            activity.updateSession(userInfo());

        }
        //check
        // if (timerHttp == null) setDataTimer();

        isInfoVisible = false;
        // rclv_grid.hasFixedSize();
        txt_event_name.setText("");

        setOnClick(img_edit_i1,
                btn_got_it,
                image_map,
                scrl_all,
                rtlv_top,
                img_infoget_f2,
                img_f10_back,
                fabMenu1_like,
                fabMenu2_picture,
                fabMenu3_comment,
                img_notif,
                txt_hide_all_one,
                txt_hide_all_two,
                txt_event_name); //mainlayout

        cardsList = new ArrayList<>();
        info_view.setVisibility(View.GONE);
        nudge = new NotificationData();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mapAsyncer(latitude,longitude);
            }
        });
    }

    private UserInfo userInfo() {
        return activity.userInfo();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        activity = (HomeActivity) getActivity();
        utility = new Utility(context);
    }

    public Event_Fragment setData(String eventId, String venueName, Events event, String[] currentLatLng, String[] venuLatLng) {
        this.eventId = eventId;
        this.venueName = venueName;
        this.event = event;
        this.currentLatLng = currentLatLng;
        latitude= Double.valueOf(venuLatLng[0]);
        longitude= Double.valueOf(venuLatLng[1]);
        return this;
    }

    @Override
    public void onStart() {
        super.onStart();

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_to_position);
        rtlv2_animate_f2.setAnimation(animation);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rtlv2_animate_f2.setBackgroundColor(getResources().getColor(R.color.bg_scenepage));
            }
        }, 2000);
        activity.dismissProgDialog();
        activity.setBBVisibility(View.GONE, TAG);
        canCallWebservice = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.dismissProgDialog();
        activity.setBBVisibility(View.GONE, TAG);
        activity.hideStatusBar();
        canCallWebservice = true;
    }

    private void setOnClick(View... views) {
        for (View v : views) {
            v.setOnClickListener(this);
        }
    }

    private void animateInfo(boolean currentVisible) {
        if (!currentVisible) {
            Animation trnslate_animate = AnimationUtils.loadAnimation(getActivity(), R.anim.translet_up_down);
            info_view.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) info_view.getLayoutParams();
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            info_view.setLayoutParams(layoutParams);
            scrl_all.smoothScrollTo(0, 0);
            info_view.setAnimation(trnslate_animate);
            rtlv2_animate_f2.setAnimation(trnslate_animate);
            rclv_grid.setAnimation(trnslate_animate);

            isInfoVisible = true;
        } else {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) info_view.getLayoutParams();
            layoutParams.height = 0;
            info_view.setLayoutParams(layoutParams);
            Animation trnslate_animate = AnimationUtils.loadAnimation(getActivity(), R.anim.translet_up_down);
            rtlv2_animate_f2.setAnimation(trnslate_animate);
            rclv_grid.setAnimation(trnslate_animate);
            isInfoVisible = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_infoget_f2:
                try {
                    animateInfo(isInfoVisible);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.img_f10_back:
                activity.onBackPressed();
                break;
            case R.id.fabMenu1_like:
                menu_blue.close(true);
                try {
                    if(userInfo().makeAdmin.equals(Constant.ADMIN_YES) && activity.checkWithTime(eventDetails.getProfile_rating().getEvent_date() , Double.parseDouble(eventDetails.getProfile_rating().getInterval()))){
                        addUserIntoEvent(0, null);
                    }
                    else if (activity.getDistance(new Double[]{latitude, longitude, Double.valueOf(currentLatLng[0]), Double.valueOf(currentLatLng[1])}) <= Constant.MAXIMUM_DISTANCE && activity.checkWithTime(eventDetails.getProfile_rating().getEvent_date() , Double.parseDouble(eventDetails.getProfile_rating().getInterval()) )) {
                        if (eventDetails.getProfile_rating().getKey_in().equals(Constant.KEY_NOTEXIST)) {
                            addUserIntoEvent(0, null);
                        } else likeEvent();
                    } else {
                        cantJoinDialog();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Utility.showToast(context, getResources().getString(R.string.somethingwentwrong),0);
                }

                break;
            case R.id.fabMenu2_picture:
                menu_blue.close(true);
                try {
                    if(userInfo().makeAdmin.equals(Constant.ADMIN_YES) && activity.checkWithTime(eventDetails.getProfile_rating().getEvent_date() , Double.parseDouble(eventDetails.getProfile_rating().getInterval()))){
                        captureImage();
                    }
                    else  if (activity.getDistance(new Double[]{latitude, longitude, Double.valueOf(currentLatLng[0]), Double.valueOf(currentLatLng[1])}) <= Constant.MAXIMUM_DISTANCE && activity.checkWithTime(eventDetails.getProfile_rating().getEvent_date() , Double.parseDouble(eventDetails.getProfile_rating().getInterval()) )) {
                        captureImage();
                    } else {
                        cantJoinDialog();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Utility.showToast(context, getResources().getString(R.string.somethingwentwrong),0);
                }

                break;
            case R.id.fabMenu3_comment:
                menu_blue.close(true);
                try {
                    if(userInfo().makeAdmin.equals(Constant.ADMIN_YES) && activity.checkWithTime(eventDetails.getProfile_rating().getEvent_date() , Double.parseDouble(eventDetails.getProfile_rating().getInterval()))){
                        canCallWebservice = false;
                        activity.addFragment(new Comment_Fragment().setData(currentLatLng,eventDetails.getProfile_rating().getKey_in(), eventId, eventDetails.getProfile_rating().getEvent_date(), eventDetails.getProfile_rating().getEvent_name(), this), 1);
                    }
                    else  if (activity.getDistance(new Double[]{latitude, longitude, Double.valueOf(currentLatLng[0]), Double.valueOf(currentLatLng[1])}) <= Constant.MAXIMUM_DISTANCE && activity.checkWithTime(eventDetails.getProfile_rating().getEvent_date() , Double.parseDouble(eventDetails.getProfile_rating().getInterval()) )) {
                        canCallWebservice = false;
                        activity.addFragment(new Comment_Fragment().setData(currentLatLng,eventDetails.getProfile_rating().getKey_in(), eventId, eventDetails.getProfile_rating().getEvent_date(), eventDetails.getProfile_rating().getEvent_name(), this), 1);

                    } else {
                        cantJoinDialog();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Utility.showToast(context, getResources().getString(R.string.somethingwentwrong),0);
                }
                break;
            case R.id.txt_hide_all_two:
                menu_blue.close(true);
                break;
            case R.id.txt_hide_all_one:
                menu_blue.close(true);
                break;
            case R.id.img_notif:
               /* canGetNotification = true;
                if (noNotify > 0) getNudges();
                else noNotification();*/
                break;
            case R.id.txt_event_name:

                break;
            case R.id.image_map:
                try {
                    if(eventDetails.getProfile_rating().getVenue_long() !=null)activity.addFragment(new SingleMap_Fragment().setData(eventDetails.getProfile_rating().getVenue_lat(),eventDetails.getProfile_rating().getVenue_long()),1);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                break;
            case R.id.rtlv_top:
                break;

            case R.id.btn_got_it:
                demoView.setVisibility(View.GONE);
                break;
            case R.id.img_edit_i1:
                //functionality comment
              /*  try{if(eventDetails.getProfile_rating().getEvent_date() != null){
                    Event_Profile_Rating rating = eventDetails.getProfile_rating();
                    if(rating !=null)activity.addFragment(new Add_Event_Fragment().setData(rating.getVenue_id(), rating.getEvent_date(), rating.getEvent_name(), rating.getInterval(), getEventId(),rating.getVenue_detail(),rating.getDescription(),this),1);}
                }
                catch (Exception e){
                    e.printStackTrace();
                }*/
                break;

        }
    }

    private void captureImage() {
        Permission permission = new Permission(activity);
        if (permission.checkCameraPermission()) callIntent(Constant.INTENT_CAMERA);
    }

    public void callIntent(int caseId) {

        switch (caseId) {
            case Constant.INTENT_CAMERA:
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    startActivityForResult(intent, Constant.INTENT_CAMERA);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /****
     * This method is used when The user is not exist in the event to first time key in the user
     *
     * @param type must be 0 or 1
     */
    private void addUserIntoEvent(final int type, @Nullable final Bitmap bitmap) {
        if (type != -1) activity.showProgDialog(false, TAG);

        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.ADD_EVENT, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    activity.dismissProgDialog();
                    // get response
                    try {
                        JSONObject jo = new JSONObject(response);
                        if (jo.getInt("success") == 0) {
                            activity.incrementKeyPoints(getString(R.string.kp_keyin));
                        }
                        if (type == 0) likeEvent();
                        else if (type == 1) sendPicture(bitmap);
                        getAllData();
                    } catch (Exception e) {
                        e.printStackTrace();
                        activity.dismissProgDialog();
                        Utility.showToast(context, getString(R.string.somethingwentwrong), 0);
                    }
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

                    params.put("userid", userInfo().userID);
                    params.put("eventname", eventDetails.getProfile_rating().getEvent_name());
                    params.put("eventid", eventId);
                    params.put("Eventdate", eventDetails.getProfile_rating().getDate_in_format());

                    Utility.e(TAG, " params " + params.toString());
                    return params;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        } else {
            utility.snackBar(rclv_grid, getString(R.string.internetConnectivityError), 0);
            activity.dismissProgDialog();
        }

    }

    /***
     * Event like volley
     */
    private void likeEvent() {
        activity.showProgDialog(false,TAG);

        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.EVENT_LIKE, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    activity.dismissProgDialog();
                    // get response
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.has("success")) if (object.getInt("success") == 1) {
                            if(object.getString("msg").contains(" liked the event.")){
                                fabMenu1_like.setImageDrawable(getResources().getDrawable(R.drawable.red_heart));
                                utility.showCustomPopup("you liked this event.", String.valueOf(R.font.raleway_regular));

                                activity.incrementKeyPoints(getString(R.string.kp_like));
                            }
                            else if(object.getString("msg").contains("unliked the event.")){
                                fabMenu1_like.setImageDrawable(getResources().getDrawable(R.drawable.heart));
                                activity.decrementKeyPoints(getString(R.string.kp_unlike));
                            }
                            getAllData();
                            //{"success":1,"msg":"your have liked the event."}
                            //{"success":1,"msg":"your have unliked the event."}
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utility.showToast(context, getResources().getString(R.string.somethingwentwrong),0);
                    }
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

                    params.put("user_id", userInfo().userID);
                    params.put("event_id", eventId);

                    Utility.e(TAG, " params " + params.toString());
                    return params;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        } else {
            utility.snackBar(rclv_grid, getString(R.string.internetConnectivityError), 0);
            activity.dismissProgDialog();
        }
    }

    /**
     * @param bitmap the bitmap return by the activity result
     */
    private void sendPicture(final Bitmap bitmap) {

        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.EVENT_POST_PIC, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    activity.dismissProgDialog();
                    // get response
                    getAllData();
                    try {
                        JSONObject respo = new JSONObject(response);
                        if(respo.getInt("success")==0){
                            Utility.showToast(context,respo.getString("msg"),0);
                            activity.incrementKeyPoints(getString(R.string.kp_img_post));
                        }
                        Utility.showToast(context,respo.getString("msg"),0);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    utility.volleyErrorListner(e);
                    activity.dismissProgDialog();
                    getAllData();
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("user_id", userInfo().userID);
                    params.put("event_id", eventId);
                    params.put("location", getLocation()); //TODO location
                    params.put("image", ImageUtil.encodeTobase64(bitmap));
                    params.put("ratingtime", activity.getCurrentTimeInFormat());

                    Utility.e(TAG, " params " + params.toString());
                    return params;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        } else {
            utility.snackBar(rclv_grid, getString(R.string.internetConnectivityError), 0);
            activity.dismissProgDialog();
        }


    }

    private String getLocation(){
        String result;
        if(userInfo().address.length()>1){
            result =userInfo().address;
        }
        else {
            result = activity.getAddress(Double.parseDouble(currentLatLng[0]), Double.parseDouble(currentLatLng[1]));
        }
        return result;
    }

    /**
     * The dialogue use to show if user is not in the range of the event and evneet is not started yet
     */
    private void cantJoinDialog() {

        if(activity.getDistance(new Double[]{latitude, longitude, Double.valueOf(currentLatLng[0]), Double.valueOf(currentLatLng[1])}) <= Constant.MAXIMUM_DISTANCE) {
            utility.showCustomPopup(getString(R.string.enotStarted), String.valueOf(R.font.raleway_regular));
        }else if(activity.userInfo().makeAdmin.equals(Constant.ADMIN_YES)){
            utility.showCustomPopup(getString(R.string.enotStarted), String.valueOf(R.font.raleway_regular));
        }else  {
            utility.showCustomPopup(getString(R.string.enotat), String.valueOf(R.font.raleway_regular));
        }

    }

    /**
     * GetALl the data for that event
     */
    public void getAllData() {

        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.LISTEVENTFEED, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    activity.dismissProgDialog();
                    // get response
                    try {
                        if (response != null) getResponse(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                        activity.dismissProgDialog();
                        Utility.showToast(context, getString(R.string.somethingwentwrong), 0);
                    }
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

                    params.put("event_id", eventId);
                    params.put("user_id", userInfo().userID);

                    Utility.e(TAG, " params " + params.toString());
                    return params;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        } else {
            utility.snackBar(rclv_grid, getString(R.string.internetConnectivityError), 0);
            activity.dismissProgDialog();
        }

    }

    /**
     * @param response the responce provided by getAlldata()
     * @throws JSONException
     */
    private void getResponse(String response) throws Exception  {
        JSONObject obj1 = new JSONObject(response);
        if (eventDetails == null) eventDetails = new EventDetails();
        try {
            if (obj1.has("eventattendy"))
                eventDetails.setAttendyJson(obj1.getJSONArray("eventattendy"),this);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (obj1.has("nudges_count"))
                eventDetails.setNudges_count(obj1.getString("nudges_count"));
            noNotify = Integer.parseInt(eventDetails.getNudges_count());
            setTextBadge();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (obj1.has("allTags")) {
                eventDetails.setTagList(obj1.getJSONArray("allTags"), this);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            if (obj1.has("allfeeds")) {
                eventDetails.setFeedsJson(obj1.getJSONArray("allfeeds"), this);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            if (obj1.has("event_profile_rating")){
                eventDetails.setProfile_ratingJSon(obj1.getJSONObject("event_profile_rating"),this);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if(obj1.has("userInfo")){

                Object intervention = obj1.get("userInfo");
                if (intervention instanceof JSONArray) {
                    SceneKey.sessionManager.logout(activity);
                }
                JSONObject user = obj1.getJSONObject("userInfo");
                if(user.has("makeAdmin"))   {
                    userInfo().makeAdmin=(user.getString("makeAdmin"));

                }
                if(user.has("lat"))         userInfo().latitude=(user.getString("lat"));
                if(user.has("longi"))       userInfo().longitude=(user.getString("longi"));
                if(user.has("adminLat"))    userInfo().latitude=(user.getString("adminLat"));
                if(user.has("adminLong"))   userInfo().longitude=(user.getString("adminLong"));
                if(user.has("address"))     userInfo().address=(user.getString("address"));
                activity.updateSession(userInfo());
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        int height = (int) getResources().getDimension(R.dimen._120sdp);//activity().ActivityWidth;
        int width =  activity.ActivityWidth;
        String url = "http://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=12&size=" + width + "x" + height + "&sensor=false";
        Utility.e(TAG, "URL" + url + "Lat lin" + latitude + " : " + longitude);

        try {
            Picasso.with(activity).load(url).into(image_map);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            setDateTime(eventDetails.getProfile_rating().getEvent_date());
        } catch (Exception e) {
            String[] dateSplit;
            dateSplit = eventDetails.getProfile_rating().getEvent_date().replace("TO", "T").split("T");
            txt_calender_i1.setText(dateSplit[0] + " " + dateSplit[1] + " " + dateSplit[2]);

            e.printStackTrace();
        }

        if(cardsList.size()<=0){
            Card card = new Card();
            card.imageUrl = null;
            card.text = "Welcome to the "+ venueName +"! Join the fun! Share your pics & comments right here!";
            cardsList.add(card);
        }
        setCardAdapter(cardsList);

        if(eventDetails.getAttendyList()!=null){if(eventDetails.getAttendyList().size()<=0){
            no_one.setVisibility(View.VISIBLE);
            try {
                if(checkWithTime_No_Attendy(eventDetails.getProfile_rating().getEvent_date() , Double.parseDouble(eventDetails.getProfile_rating().getInterval() )))
                    txt_not_started.setText(getString(R.string.dontBore));
                else txt_not_started.setText(getString(R.string.not_start));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        else {
            no_one.setVisibility(View.GONE);
        }}
        else {
            try {
                if(checkWithTime_No_Attendy(eventDetails.getProfile_rating().getEvent_date() , Double.parseDouble(eventDetails.getProfile_rating().getInterval() )))
                    txt_not_started.setText(getString(R.string.dontBore));
                else txt_not_started.setText(getString(R.string.not_start));
            } catch (ParseException e) {
                e.printStackTrace();
            }catch (NullPointerException e) {
                e.printStackTrace();
            }

        }
    }


    private void mapAsyncer(final double lat , final double lng) {
        map_view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                Marker m = null;
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setAllGesturesEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(false);
                googleMap.getUiSettings().setZoomGesturesEnabled(false);
                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            //callPermission(Constants.TYPE_PERMISSION_FINE_LOCATION);
                        } else if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            //callPermission(Constants.TYPE_PERMISSION_CORAS_LOCATION);
                        }
                    }
                    return;
                }
                googleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                LatLng sydney = new LatLng(lat,lng);


                final Marker mr = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .anchor(0.5f, 0.5f)
                        .title(event.getVenue().getVenue_name())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)));
                mr.showInfoWindow();

                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                           if(eventDetails.getProfile_rating().getVenue_long() !=null)activity.addFragment(new SingleMap_Fragment().setData(eventDetails.getProfile_rating().getVenue_lat(),eventDetails.getProfile_rating().getVenue_long()),1);
                    }
                });

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        if(eventDetails.getProfile_rating().getVenue_long() !=null)activity.addFragment(new SingleMap_Fragment().setData(eventDetails.getProfile_rating().getVenue_lat(),eventDetails.getProfile_rating().getVenue_long()),1);
                        marker.showInfoWindow();

                        return true;
                    }
                });
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if(eventDetails.getProfile_rating().getVenue_long() !=null)activity.addFragment(new SingleMap_Fragment().setData(eventDetails.getProfile_rating().getVenue_lat(),eventDetails.getProfile_rating().getVenue_long()),1);
                        mr.showInfoWindow();

                    }
                });//TODO check with iphone

                Handler handler = new Handler();
                final Marker finalM = m;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (finalM != null) finalM.showInfoWindow();
                    }
                }, 2000);


            }
        });


    }

    public void addChips(ArrayList<Tags> tag) {
        try {
            Grid_multiRow layout =  this.getView().findViewById(R.id.chip_linear);
            //check
            // layout.setAdapter(new GridChipsAdapter(context,tag));
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * @param date date of the event check format before use tie
     * @return
     * @throws ParseException
     */
    private boolean checkWithTime(final String date) throws ParseException {
        String[] dateSplit = (date.replace("TO", "T")).replace(" ", "T").split("T");
        Date startTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())).parse(dateSplit[0] + " " + dateSplit[1]);
        Date endTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault())).parse(dateSplit[0] + " " + dateSplit[2]);
        long currentTime = java.util.Calendar.getInstance().getTime().getTime();

        return currentTime < endTime.getTime() && currentTime > startTime.getTime();
    }

    public boolean checkWithTime_No_Attendy(final String date , Double interval) throws ParseException {

        return  true; //TODO change time check
       /* String[] dateSplit = (date.replace("TO", "T")).replace(" ", "T").split("T");
        Date startTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(dateSplit[0] + " " + dateSplit[1]);
        Date endTime = new Date(startTime.getTime()+(int)(interval* 60 * 60 * 1000));
        Util.printLog(TAG, " Date "+startTime+" : "+endTime);
        long currentTime = java.util.Calendar.getInstance().getTime().getTime();
        if (currentTime > startTime.getTime()) {
            return true;
        }
        return false;*/
    }

    /**
     * text badge count from 15 to 0 sec.
     */
    private void setTextBadge() {

        txt_f2_badge.setText(noNotify + "");
        if (noNotify > 0) {
            txt_f2_badge.setBackground(getResources().getDrawable(R.drawable.bg_circle_red_badge));
            img_notif.setImageResource(R.drawable.bell_red);
            img_notif.setBackgroundResource(R.drawable.bg_bell_red);
            if (noNotify > 99) txt_f2_badge.setText("99+");
        } else {
            txt_f2_badge.setText("0");
            txt_f2_badge.setBackground(getResources().getDrawable(R.drawable.bg_primary_circle));
            img_notif.setImageResource(R.drawable.bell);
            img_notif.setBackgroundResource(R.drawable.bg_bell);
        }
    }

    private void setDateTime(String eventDate) throws ParseException {
        String[] dateSplit;
        Utility.e(TAG, " " + eventDate);
        dateSplit = (eventDate.replace("TO", " ").replace("T", " ")).split(" ");

        Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).parse(dateSplit[0] + " " + dateSplit[1]);
        Date date2 = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).parse(dateSplit[0] + " " + dateSplit[2]));
        txt_calender_i1.setText(new SimpleDateFormat("MMM dd, yyyy hh:mm ",Locale.getDefault()).format(date1)+(date1.getHours()<12?"AM":"PM") + " - " + new SimpleDateFormat("hh:mm ",Locale.getDefault()).format(date2)+(date2.getHours()<12?"AM":"PM"));

    }

    /***
     * For setting the Grid Layout of room Persons showing at bottom of the Room
     *
     * @param list
     */
    public void setRecyclerView(ArrayList<EventAttendy> list) {
        if (rclv_grid.getLayoutManager() == null)
            rclv_grid.setLayoutManager(new GridLayoutManager(activity, 3));
        if (rclv_grid.getAdapter() == null) {
            DataAdapter dataAdapter = new DataAdapter(activity, list, new String[]{eventId, userInfo().userID}, this);
            rclv_grid.setAdapter(dataAdapter);
        } else {
            rclv_grid.getAdapter().notifyDataSetChanged();
        }
        Utility.e(TAG, "List SIZE " + list.size());

    }

    public void setCardAdapter(ArrayList<Card> cardsList) {
        Utility.e(TAG+" Size Card", cardsList.size() + "");
        if (card_stack_view.getAdapter() == null) {
            CardsAdapter arrayAdapter = new CardsAdapter(context, cardsList);
            card_stack_view.setAdapter(arrayAdapter);
        }
        else {
            ((CardsAdapter)card_stack_view.getAdapter()).notifyDataSetChanged();
            card_stack_view.restart();
        }

        ((CardsAdapter) card_stack_view.getAdapter()).notifyDataSetChanged();
        card_stack_view.setFlingListener(new SwipeCardView.OnCardFlingListener() {
            @Override
            public void onCardExitLeft(Object dataObject) {
                //makeToast(CardSwipeActivity.this, "Left!");
            }

            @Override
            public void onCardExitRight(Object dataObject) {
                //makeToast(CardSwipeActivity.this, "Right!");
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                if (itemsInAdapter == 0) {
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            //start your activity here
                            card_stack_view.restart();
                        }

                    }, 20L);


                }
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }

            @Override
            public void onCardExitTop(Object dataObject) {
                // makeToast(CardSwipeActivity.this, "Top!");
            }

            @Override
            public void onCardExitBottom(Object dataObject) {
                //makeToast(CardSwipeActivity.this, "Bottom!");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        activity.hideStatusBar();
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {


            if (requestCode == Constant.INTENT_CAMERA && data != null) {

                final Bitmap eventImg = (Bitmap) data.getExtras().get("data");
                // final Bitmap eventImg = ImageUtil.decodeFile(ImageUtil.getRealPathFromUri(getContext(), Uri.parse(data.toURI())));
                //((ImageView)this.getView().findViewById(R.id.iv_test)).setImageBitmap(eventImg);

                if (eventDetails.getProfile_rating().getKey_in().equals(Constant.KEY_NOTEXIST))
                    addUserIntoEvent(1, eventImg);
                else sendPicture(eventImg);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureImage();
                } else {
                    Utility.showToast(context, "permission denied by user ", Toast.LENGTH_LONG);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (timerHttp != null) timerHttp.cancel();
        if (timerNudge != null) timerNudge.cancel();
        timerHttp = null;
        timerNudge = null;
        for (Fragment fragment : activity.getSupportFragmentManager().getFragments()) {
            try {
                ((Trending_Fragment) fragment).getTrendingData();
                activity.setTitle(getString(R.string.trending));
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                ((Map_Fragment) fragment).checkEventAvailability();
                activity.setTitle(getString(R.string.scene));
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                ((Event_Search_Tag_Fragment) fragment).setVisibility();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        VolleySingleton.getInstance(context).cancelPendingRequests(TAG);
        activity.showStatusBar();
        handler.removeCallbacksAndMessages(null);
        activity.setBBVisibility(View.VISIBLE, 300, TAG);
        super.onDestroyView();
    }

    @Override
    public boolean onStatusBarHide() {
        return false;
    }
}

