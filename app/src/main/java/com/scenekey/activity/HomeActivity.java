package com.scenekey.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.scenekey.R;
import com.scenekey.aws_service.Aws_Web_Service;
import com.scenekey.fragment.Add_Fragment;
import com.scenekey.fragment.Demo_Event_Fragment;
import com.scenekey.fragment.Event_Fragment;
import com.scenekey.fragment.Home_No_Event_Fragment;
import com.scenekey.fragment.Map_Fragment;
import com.scenekey.fragment.NearEvent_Fragment;
import com.scenekey.fragment.Profile_Fragment;
import com.scenekey.fragment.Search_Fragment;
import com.scenekey.fragment.Trending_Fragment;
import com.scenekey.helper.Constant;
import com.scenekey.helper.CustomProgressBar;
import com.scenekey.helper.Permission;
import com.scenekey.helper.WebServices;
import com.scenekey.lib_sources.arc_menu.ArcMenu;
import com.scenekey.listener.BackPressListner;
import com.scenekey.listener.StatusBarHide;
import com.scenekey.model.EventAttendy;
import com.scenekey.model.Events;
import com.scenekey.model.UserInfo;
import com.scenekey.util.SceneKey;
import com.scenekey.util.StatusBarUtil;
import com.scenekey.util.Utility;
import com.scenekey.volleymultipart.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener,LocationListener{

    public static int ActivityWidth;
    public static int ActivityHeight;
    public static int ActivitybottomMarginOne = 0;
    private static UserInfo userInfo;
    private final String TAG="HomeActivity";
    public Context context=this;
    public ImageView img_profile;
    public RelativeLayout rtlv_four;
    public FrameLayout frm_bottmbar;
    public boolean isApiM, isKitKat, statusKey;
    private FrameLayout frame_fragments;
    private TextView tvHomeTitle,tv_key_points;
    private RelativeLayout rl_title_view;
    private ArcMenu arcMenu;
    private RelativeLayout rtlv_one, rtlv_two,rtlv_three ,rtlv_five ,lastclicked;
    private ImageView img_three_logo ,img_three_one;
    private View view, bottom_margin_view ,top_status ;
    private boolean doubleBackPress;
    private ArrayList<Events> eventsArrayList,eventsNearbyList;
    private boolean isPermissionAvail;
    private Map_Fragment map_fragment;
    private double latitude=0.0, longitude =0.0;
    private double latitudeAdmin=0.0, longitudeAdmin=0.0;
    private boolean checkGPS;
    private CustomProgressBar customProgressBar;
    private  LocationManager locationManager;
    private Utility utility;
    private int position=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //StatusBarUtil.setTranslucent(this);
        setContentView(R.layout.activity_home);

        //automatically hide status bar
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            // TODO: The system bars are visible. Make any desired
                            Utility.e(TAG, "Status bar visible");
                            Fragment fragment = getCurrentFragment();
                            if (fragment instanceof Event_Fragment | fragment instanceof Demo_Event_Fragment) {
                                hideStatusBar();
                            }
                        } else {
                            // TODO: The system bars are NOT visible. Make any desired
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                            Utility.e(TAG, "Status bar Invisible");

                        }
                    }
                });

        top_status = findViewById(R.id.top_status);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!(SceneKey.sessionManager.isSoftKey())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                StatusBarUtil.setStatusBarTranslucent(this, true);
            } else {
                top_status.setVisibility(View.GONE);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                top_status.setBackgroundResource(R.color.white);
                isApiM = true;
            } else {
                StatusBarUtil.setStatusBarColor(this, R.color.new_white_bg);
                top_status.setVisibility(View.VISIBLE);
            }
        }else{
            StatusBarUtil.setStatusBarColor(this,R.color.white);
            top_status.setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                top_status.setBackgroundResource(R.color.white);
            } else {
                StatusBarUtil.setStatusBarColor(this, R.color.new_white_bg);
            }
        }


        //check
  /*      if (getIntent().getExtras() != null) {
          String notificationType = getIntent().getStringExtra("notificationType");
          if (!(notificationType.isEmpty())&&notificationType.equalsIgnoreCase("2")){
              String eventId = getIntent().getStringExtra("eventId");
              //getEventData(eventId);
          }
        }*/

    }


    /* when notification receive fragment start here */
/*
    public void getEventData(String eventId){
        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.EVENT_DETAIL+eventId, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    dismissProgDialog();
                    // get response
                    try {
                        JSONObject jo = new JSONObject(response);

                        if (jo.has("success")) {
                            int status = jo.getInt("success");
                            if (status == 0){
                                dismissProgDialog();
                                try {
                                    Toast.makeText(context,jo.getString("message"),Toast.LENGTH_SHORT).show();
                                    if (eventsArrayList == null) eventsArrayList = new ArrayList<>();
                                    else eventsArrayList.clear();
                                    setRecyclerView();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                            //else if()
                        } else {

                            if (jo.has("eventDetail")) {
                                if (eventsArrayList == null) eventsArrayList = new ArrayList<>();
                                else eventsArrayList.clear();
                                JSONArray eventAr = jo.getJSONArray("events");
                                for (int i = 0; i < eventAr.length(); i++) {
                                    JSONObject object = eventAr.getJSONObject(i);
                                    Events events = new Events();
                                    if (object.has("venue"))
                                        events.setVenueJSON(object.getJSONObject("venue"));
                                    if (object.has("artists"))
                                        events.setArtistsArray(object.getJSONArray("artists"));
                                    if (object.has("events"))
                                        events.setEventJson(object.getJSONObject("events"));
                                    try{
                                        events.setOngoing(events.checkWithTime(events.getEvent().event_date , events.getEvent().interval));
                                    }catch (Exception e){
                                        Utility.e("Date exception",e.toString());
                                    }
                                    try {
                                        events.settimeFormat();
                                    }catch (Exception e){
                                        Utility.e("Exception time",e.toString());
                                    }
                                    try {
                                        events.setRemainingTime();
                                    }
                                    catch (Exception e){
                                        Utility.e("Exception Remaining",e.toString());
                                    }
                                    eventsArrayList.add(events);
                                    // Util.printLog("Result",events.toString());
                                }
                                if (eventsArrayList.size() <= 0) {
                                    Toast.makeText(context, "No Event found near your location", Toast.LENGTH_LONG).show();
                                }
                                setRecyclerView();
                            }
                            dismissProgDialog();
                        }
                    } catch (Exception e) {
                        dismissProgDialog();
                        Utility.showToast(context,getString(R.string.somethingwentwrong),0);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    utility.volleyErrorListner(e);
                    dismissProgDialog();
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("lat",lat);
                    params.put("long",lng);
                    params.put("user_id", SceneKey.sessionManager.getUserInfo().userID + "");

                    Utility.e(TAG," params "+params.toString());
                    return params;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        }else{
            utility.snackBar(rcViewTrending,getString(R.string.internetConnectivityError),0);
            dismissProgDialog();
        }
    }
*/

      /* when notification receive fragment end here */

    @Override
    protected void onStart() {
        super.onStart();
        initLocation();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Permission permission = new Permission(context);
        userInfo = SceneKey.sessionManager.getUserInfo();
        initView();
        dimmedEffect();
        isPermissionAvail= permission.checkLocationPermission();

        replaceFragment(new Home_No_Event_Fragment());
        try {
            if(userInfo.makeAdmin.equals(Constant.ADMIN_YES)){
                latitude = Double.parseDouble(userInfo.latitude);
                longitude = Double.parseDouble(userInfo.longitude);
                latitudeAdmin=latitude;
                longitudeAdmin=longitude;

            }else{
                latitude = Double.parseDouble(userInfo.latitude);
                longitude = Double.parseDouble(userInfo.longitude);
                latitudeAdmin=latitude;
                longitudeAdmin=longitude;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        rtlv_three.callOnClick();

    }


    private void initView() {
        frame_fragments =  findViewById(R.id.frame_fragments);
        frm_bottmbar =  findViewById(R.id.frm_bottmbar);
        rl_title_view =  findViewById(R.id.rl_title_view);
        tvHomeTitle =  findViewById(R.id.tvHomeTitle);
        tv_key_points =  findViewById(R.id.tv_key_points);
        img_profile =  findViewById(R.id.img_profile);

        rtlv_one =  findViewById(R.id.rtlv_one);
        rtlv_two =  findViewById(R.id.rtlv_two);
        rtlv_three =  findViewById(R.id.rtlv_three);
        rtlv_four =  findViewById(R.id.rtlv_four);
        rtlv_five =  findViewById(R.id.rtlv_five);
        img_three_logo =  findViewById(R.id.img_three_logo);
        img_three_one =  findViewById(R.id.img_three_one);
        bottom_margin_view =  findViewById(R.id.bottom_margin_view);

        setOnClick(rtlv_one, rtlv_two, rtlv_three, rtlv_four, rtlv_five, img_profile);

        tv_key_points.setText(userInfo.keyPoints);
        try {
            Utility.e("Profile pic Home",userInfo.getUserImage());

            Picasso.with(this).load(userInfo.getUserImage()).placeholder(R.drawable.image_defult_profile).into(img_profile);

        } catch (Exception e) {
            Utility.e("Picasso e",e.toString());
            Utility.e("Picasso e",userInfo.getUserImage());
        }
        //initArc();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        ActivityWidth = width;
        ActivityHeight = height;

        customProgressBar = new CustomProgressBar(context);
        utility = new Utility(context);
    }

    private void setOnClick(View... views) {
        for (View v : views) {
            v.setOnClickListener(this);
        }
    }

    private  void initLocation() {
        try {
            // get GPS status
            checkGPS = locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // get network provider status
            boolean checkNetwork = locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               /* CustomPopup customPopup = new CustomPopup(HomeActivity.this);
                customPopup.setMessage(getString(R.string.eLocationPermission_new));
                customPopup.show();*/
                return;
            }
            if (!checkGPS && !checkNetwork) {
                Utility.e(TAG, "GPS & Provider not avaialble");
                // utility.checkGpsStatus();
            } else {
                if (checkGPS) {
                    assert locationManager != null;
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this);
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (checkNetwork) {
                    assert locationManager != null;
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, this);
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void dimmedEffect() {
        Animation animat1 = AnimationUtils.loadAnimation(this, R.anim.one_fade);
        Animation animat2 = AnimationUtils.loadAnimation(this, R.anim.two_fade);
        Animation animat3 = AnimationUtils.loadAnimation(this, R.anim.three_fade);
        Animation animat4 = AnimationUtils.loadAnimation(this, R.anim.four_fade);

        getImgV(rtlv_one).setAnimation(animat1);
        getImgV(rtlv_two).setAnimation(animat2);
        getImgV(rtlv_four).setAnimation(animat3);
        getImgV(rtlv_five).setAnimation(animat4);

    }


   /* private void initArc() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        ActivityWidth = width;
        ActivityHeight = height;

        /************************** Arc Menu *****************************************/

      /*  arcMenu =  findViewById(R.id.arcMenuX);
        arcMenu.setToolTipTextSize(14);
        arcMenu.setMinRadius(((width - ((int) getResources().getDimension(R.dimen.fab_size_normal2))) / 4));
        arcMenu.setArc(177, 273);
        arcMenu.setToolTipSide(ArcMenu.TOOLTIP_LEFT);
        arcMenu.setToolTipTextColor(Color.BLACK);
        arcMenu.setToolTipBackColor(Color.parseColor("#88000000"));
        arcMenu.setToolTipCorner(2);
        arcMenu.setToolTipPadding(10);
        arcMenu.setColorNormal(getResources().getColor(R.color.colorPrimary));
        arcMenu.showTooltip(true);
        arcMenu.setDuration(ArcMenu.ArcMenuDuration.LENGTH_LONG);
        arcMenu.setAnim(500, 500, ArcMenu.ANIM_MIDDLE_TO_DOWN, ArcMenu.ANIM_MIDDLE_TO_RIGHT,
                ArcMenu.ANIM_INTERPOLATOR_ANTICIPATE, ArcMenu.ANIM_INTERPOLATOR_ANTICIPATE);
        final int[] ITEM_DRAWABLES = {R.drawable.image_defult_profile, R.drawable.image_defult_profile, R.drawable.image_defult_profile};
        String[] str = {"Existing Event", "Add Venue", "Add Event"};
        initArcMenu(arcMenu, str, ITEM_DRAWABLES, ITEM_DRAWABLES.length);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(60 * 1000);
        locationRequest.setFastestInterval(15 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        googleApiClient.connect();
        try {
              LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
              Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation(provider);
            latitude = latitude2 =  location.getLatitude();
            longitude = longiude2 =  location.getLongitude();
            Intent in = getIntent();
            latitude =  Double.parseDouble(in.getStringExtra(Constant.LATITUDE));
            longitude =  Double.parseDouble(in.getStringExtra(Constant.LONGITUDE));*//*


        }
        catch (Exception e){
            e.printStackTrace();
        }

    }*/


    /************************************
     * Arc Menu
     *********************************/

/*
    private void initArcMenu(final ArcMenu menu, final String[] str, int[] itemDrawables, int count) {
        for (int i = 0; i < count; i++) {
            FloatingActionButton item = new FloatingActionButton(this);
            item.setSize(FloatingActionButton.SIZE_MINI);
            item.setIcon(itemDrawables[i]);
            item.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            menu.setChildSize(item.getIntrinsicHeight());
            final int position = i;
            menu.addItem(item, str[i], new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(HomeActivity.this, str[position],
                            Toast.LENGTH_SHORT).show();
                    if (position == 1) {
                        menu.menuOut();
                    }
                }
            });
        }
    }
*/

    public UserInfo userInfo(){
        if(userInfo == null) {
            if(!SceneKey.sessionManager.isLoggedIn()){
                SceneKey.sessionManager.logout(HomeActivity.this);
            }
            userInfo = SceneKey.sessionManager.getUserInfo();
        }
        return userInfo;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rtlv_one:
                if (position!=1) {
                    if (isPermissionAvail) {
                        position = 1;
                        replaceFragment(new Trending_Fragment());
                        setBottomBar((RelativeLayout) v, lastclicked);
                    } else {
                        utility.snackBar(rtlv_three, "Location permission not available", 0);
                    }
                }
                break;
            case R.id.rtlv_two:
                if (position!=2) {
                    if (isPermissionAvail) {
                        position = 2;
                        setBottomBar((RelativeLayout) v, lastclicked);
                        if (map_fragment == null) map_fragment = new Map_Fragment();
                        replaceFragment(map_fragment);
                    } else {
                        utility.snackBar(rtlv_three, "Location permission not available", 0);
                    }
                }
                break;
            case R.id.rtlv_three:
                if (position!=3) {
                    position = 3;
                    replaceFragment(new Home_No_Event_Fragment());
                    setBottomBar((RelativeLayout) v, lastclicked);
                    if (eventsArrayList != null) eventsArrayList.clear();
                    if (eventsNearbyList != null) eventsNearbyList.clear();
                    if (latitude == 0.0d && longitude == 0.0d) {
                        if (userInfo.makeAdmin.equals(Constant.ADMIN_YES)) {
                            try {
                                checkEventAvailablity(true);
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }else{
                            showErrorPopup("rtlv_three");
                        }
                    } else {
                        try {
                            checkEventAvailablity(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case R.id.rtlv_four:
                if (position!=4) {
                    position = 4;
                    replaceFragment(new Search_Fragment());
                    setBottomBar((RelativeLayout) v, lastclicked);
                }
                break;
            case R.id.rtlv_five:
                if (position!=5) {
                    hideKeyBoard();
                    position = 5;
                    replaceFragment(new Add_Fragment());
                    setBottomBar((RelativeLayout) v, lastclicked);
                    //findViewById(R.id.activity_second).setVisibility(View.VISIBLE);
                    //arcMenu.setVisibility(View.VISIBLE);
                    //arcMenu.callOnClick();
                }
                break;
            case R.id.img_profile:
                try {
                    EventAttendy attendy = new EventAttendy();
                    attendy.userid=(userInfo.userID);
                    attendy.userFacebookId=(userInfo.facebookId);
                    attendy.setUserimage(userInfo.getUserImage());
                    attendy.username=(userInfo.userName);

                    addFragment(new Profile_Fragment().setData(attendy, true, null,0), 1);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;

            //arc menu click start

        /*    case R.id.lnr_add_event:
                position =5;
                setBottomBar((RelativeLayout) findViewById(R.id.rtlv_five), lastclicked);
                replaceFragment(new Add_Event_Fragmet());
                findViewById(R.id.activity_second).setVisibility(View.GONE);
                break;
            case R.id.lnr_add_venue:
                position =5;
                setBottomBar((RelativeLayout) findViewById(R.id.rtlv_five), lastclicked);
                replaceFragment(new Add_venue_Fragment());
                //Toast.makeText(this,"Unfortunately, you cannot access this feature right now. Check back for further updates",Toast.LENGTH_SHORT).show();
                findViewById(R.id.activity_second).setVisibility(View.GONE);
                break;
            case R.id.lnr_exist_venue:
                position =5;
                setBottomBar((RelativeLayout) findViewById(R.id.rtlv_five), lastclicked);
                replaceFragment(new Existing_Fragment());
                //Toast.makeText(this,"Unfortunately, you cannot access this feature right now. Check back for further updates",Toast.LENGTH_SHORT).show();
                findViewById(R.id.activity_second).setVisibility(View.GONE);
                break;
            case R.id.activity_second:
                //findViewById(R.id.activity_second).setVisibility(View.GONE);
                break;
            case R.id.img_cross:
                *//*findViewById(R.id.activity_second).setVisibility(View.GONE);
                if(position!=3)rtlv_three.callOnClick();*//*
                break;*/

            //arc menu click end

        }
    }

    private void setBottomBar(RelativeLayout v, final RelativeLayout lastClicked) {
        setRtlvText(v, true);
        if (lastClicked != null) {

            setRtlvText(lastClicked, false);

            switch (lastClicked.getId()) {
                case R.id.rtlv_one:
                    getImgV(lastClicked).setImageResource(R.drawable.flame);
                    break;
                case R.id.rtlv_two:
                    getImgV(lastClicked).setImageResource(R.drawable.ic_map_icon);
                    break;
                case R.id.rtlv_three:
                    img_three_logo.setImageResource(R.drawable.ic_logo);
                    img_three_logo.setBackgroundResource(R.drawable.bg_white_key);
                    img_three_logo.setColorFilter(ContextCompat.getColor(context, R.color.black30p), PorterDuff.Mode.SRC_ATOP);
                    getImgV(lastClicked).setImageResource(R.drawable.transparent);
                    break;
                case R.id.rtlv_four:
                    getImgV(lastClicked).setImageResource(R.drawable.ic_search_icon);
                    break;
                case R.id.rtlv_five:
                    getImgV(lastClicked).setImageResource(R.drawable.ic_add_icon);
                    break;

            }

        }
        switch (v.getId()) {
            case R.id.rtlv_one:
                getImgV(v).setImageResource(R.drawable.active_flame);
                break;
            case R.id.rtlv_two:
                getImgV(v).setImageResource(R.drawable.ic_selected_map_icon);
                break;
            case R.id.rtlv_three:
                //img_three_logo.setVisibility(View.VISIBLE);
                img_three_logo.setBackgroundResource(R.drawable.transparent);
                img_three_logo.setImageResource(R.drawable.ic_logo_selected);
                img_three_logo.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                img_three_one.setVisibility(View.INVISIBLE);
                ((TextView) v.getChildAt(1)).setTextColor(getResources().getColor(R.color.white));
                getImgV(v).setImageResource(R.drawable.selected_key_icon4);
                break;
            case R.id.rtlv_four:
                getImgV(v).setImageResource(R.drawable.ic_selected_search_icon);
                break;
            case R.id.rtlv_five:
                getImgV(v).setImageResource(R.drawable.ic_selected_add_icon);
                break;
        }

        this.lastclicked = v;
    }

    void setRtlvText(RelativeLayout rtlv, boolean isClicked) {
        img_three_logo.setVisibility(View.VISIBLE);
        img_three_one.setVisibility(View.VISIBLE);
        if (isClicked) {
            ((TextView) rtlv.getChildAt(1)).setTextColor(getResources().getColor(R.color.selected_bb_text));
        } else {
            ((TextView) rtlv.getChildAt(1)).setTextColor(getResources().getColor(R.color.black));
        }

    }

    ImageView getImgV(RelativeLayout rtlv) {
        return ((ImageView) rtlv.getChildAt(0));
    }

    private void replaceFragment(Fragment fragmentHolder) {
        try{
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            String fragmentName = fragmentHolder.getClass().getName();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            //fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            fragmentTransaction.replace(R.id.frame_fragments, fragmentHolder,fragmentName).addToBackStack(fragmentName);
            fragmentTransaction.commit();
            hideKeyBoard();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Fragment addFragment(Fragment fragmentHolder, int animationValue) {
        try{ FragmentManager fragmentManager = getSupportFragmentManager();
            String fragmentName = fragmentHolder.getClass().getName();
            if(!(fragmentHolder instanceof StatusBarHide)) showStatusBar();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (animationValue == 0) {

                fragmentTransaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up, R.anim.slide_out_down, R.anim.slide_in_down);
            }
            if (animationValue == 1)
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setEnterTransition(null);
            }
            fragmentTransaction.add(R.id.frame_fragments, fragmentHolder,fragmentName).addToBackStack(fragmentName);
            fragmentTransaction.commit();

            hideKeyBoard();
            return fragmentHolder;}
        catch (Exception e){
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        hideKeyBoard();
        Handler handler = new Handler();
        Runnable runnable;
        //  Util.printLog(TAG, "" + getSupportFragmentManager().getBackStackEntryCount());
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            if(getCurrentFragment() instanceof BackPressListner){
                if(((BackPressListner) getCurrentFragment()).onKeyBackPress()) return;
            }
            super.onBackPressed();

            onPopUpBackstage();
        } else {

            if(getCurrentFragment() instanceof BackPressListner){
                if(((BackPressListner) getCurrentFragment()).onKeyBackPress()) return;
            }

            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    doubleBackPress = false;
                }
            }, 1000);
            if (doubleBackPress) {
                handler.removeCallbacks(runnable);
                finish();
            } else {
                doubleBackPress = true;
            }
        }


    }


    public void hideKeyBoard() {
        try {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            assert inputManager != null;
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkEventAvailablity(final boolean showProgress) {
        if (latitudeAdmin!=0.0d&&longitudeAdmin!=0.0d)
        {
            showProgDialog(false,TAG);
            getEventsByLocationApi();
        }else if (!checkGPS){
            utility.checkGpsStatus();
        }else{
            showErrorPopup("checkEventAvailablity");
        }

    }

    private void getEventsByLocationApi(){

        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.EVENT_BY_LOCAL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    dismissProgDialog();
                    // get response
                    try {
                        //{"status":0,"message":"No data were found!","userInfo":{"fullname":"Thomas Lewis","address":"Los Angeles County,California","lat":"22.7051015","longi":"75.9090669","makeAdmin":"yes","key_points":"31"}}
                        JSONObject jo = new JSONObject(response);
                        if (jo.has("status")) {
                            int status = jo.getInt("status");
                            if (status == 0) {
                                // replaceFragment(new Home_No_Event_Fragment());

                                Utility.showToast(context,jo.getString("message"),0);

                            }
                            if (jo.has("userInfo")) {
                                if(userInfo== null) userInfo = SceneKey.sessionManager.getUserInfo();
                                Object intervention = jo.get("userInfo");
                                if (intervention instanceof JSONArray) {
                                    SceneKey.sessionManager.logout(HomeActivity.this);
                                }
                                JSONObject user = jo.getJSONObject("userInfo");
                                if(user.has("makeAdmin"))   userInfo.makeAdmin=(user.getString("makeAdmin"));
                                if(user.has("lat"))         userInfo.latitude=(user.getString("lat"));
                                if(user.has("longi"))       userInfo.longitude=(user.getString("longi"));
                                if(user.has("address"))       userInfo.address=(user.getString("address"));
                                if(user.has("fullname"))       userInfo.fullName=(user.getString("fullname"));
                                if(user.has("key_points"))userInfo.keyPoints=(user.getString("key_points"));
                                if(user.has("bio"))userInfo.bio=(user.getString("bio"));
                                updateSession(userInfo);
                            }
                            //else if()
                        } else {
                            if (jo.has("userInfo")) {
                                if(userInfo== null) userInfo = SceneKey.sessionManager.getUserInfo();
                                Object intervention = jo.get("userInfo");
                                if (intervention instanceof JSONArray) {
                                    SceneKey.sessionManager.logout(HomeActivity.this);
                                    return;
                                }
                                JSONObject user = jo.getJSONObject("userInfo");
                                if(user.has("makeAdmin"))   userInfo.makeAdmin=(user.getString("makeAdmin"));
                                if(user.has("lat"))         userInfo.latitude=(user.getString("lat"));
                                if(user.has("longi"))       userInfo.longitude=(user.getString("longi"));
                                if(user.has("adminLat"))    userInfo.latitude=(user.getString("adminLat"));
                                if(user.has("adminLong"))   userInfo.longitude=(user.getString("adminLong"));
                                if(user.has("address"))     userInfo.address=(user.getString("address"));
                                if(user.has("key_points"))userInfo.keyPoints=(user.getString("key_points"));
                                if(user.has("bio"))userInfo.bio=(user.getString("bio"));
                                updateSession(userInfo);
                            }
                            if (jo.has("events")) {
                                if (eventsArrayList == null) eventsArrayList = new ArrayList<>();
                                eventsArrayList.clear();
                                JSONArray eventAr = jo.getJSONArray("events");
                                for (int i = 0; i < eventAr.length(); i++) {
                                    JSONObject object = eventAr.getJSONObject(i);
                                    Events events = new Events();
                                    if (object.has("venue"))
                                        events.setVenueJSON(object.getJSONObject("venue"));
                                    if (object.has("artists"))
                                        events.setArtistsArray(object.getJSONArray("artists"));
                                    if (object.has("events"))
                                        events.setEventJson(object.getJSONObject("events"));
                                    try {
                                        events.settimeFormat();
                                        events.setRemainingTime();
                                    }catch (Exception e){
                                        Utility.e("Exception time",e.toString());
                                    }

                                    eventsArrayList.add(events);
                                    // Util.printLog("Size",eventsArrayList.size()+"");
                                }

                                try {
                                    for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                                        if (fragment instanceof Map_Fragment){
                                            if (map_fragment != null) map_fragment.notifyAdapter(eventsArrayList);
                                        }
                                    }
                                    checkedEventToJoin();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {
                                    Utility.e("Near Event Size", String.valueOf(eventsNearbyList.size()));
                                    if (!(eventsNearbyList.size() <= 0))
                                        onNearByEventFound();
                                    else {
                                        replaceFragment(new Home_No_Event_Fragment());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utility.showToast(context,getString(R.string.somethingwentwrong),0);

                    }
                    try {
                        Picasso.with(HomeActivity.this).load(userInfo.getUserImage()).placeholder(R.drawable.image_defult_profile).into(img_profile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    utility.volleyErrorListner(e);
                    dismissProgDialog();
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("lat", getLatLng()[0]);
                    params.put("long", getLatLng()[1]);
                    params.put("user_id",userInfo.userID);
                    params.put("updateLocation", Constant.ADMIN_NO);
                    params.put("fullAddress", userInfo.address);

                    Utility.e(TAG," params "+params.toString());
                    return params;
                }
            };
            VolleySingleton.getInstance(this.getBaseContext()).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        }else{
            utility.snackBar(frame_fragments,getString(R.string.internetConnectivityError),0);
            dismissProgDialog();
        }
    }

    public void updateSession(UserInfo user){

        SceneKey.sessionManager.createSession(user);
        userInfo = SceneKey.sessionManager.getUserInfo();
        try {
            Picasso.with(this).load(userInfo.getUserImage()).placeholder(R.drawable.image_defult_profile).into(img_profile);
            tv_key_points.setText(userInfo.keyPoints);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isLocation(){
        try {
            /* if(Double.parseDouble(userInfo.longitude)==0.0D){
                return false;
            }
            else return !(Double.parseDouble(userInfo.latitude) == 0.0D);*/
            return !(Double.parseDouble(userInfo.longitude) == 0.0D) && !(Double.parseDouble(userInfo.latitude) == 0.0D);
        }
        catch (Exception e){
            return false;
        }
    }

    private void showErrorPopup(final String tag) {
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.custom_popup_with_btn);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //      deleteDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //style id

        TextView tvCancel, tvTryAgain,tvTitle,tvMessages;

        tvTitle = dialog.findViewById(R.id.tvTitle);
        tvMessages = dialog.findViewById(R.id.tvMessages);
        tvCancel = dialog.findViewById(R.id.tvPopupCancel);
        tvTryAgain = dialog.findViewById(R.id.tvPopupOk);

        tvTitle.setText(R.string.gps_new);
        tvMessages.setText(R.string.couldntGetLocation);

        tvTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                showProgDialog(false,TAG);


                new Handler().postDelayed(new Runnable() {
                    // Using handler with postDelayed called runnable run method
                    @Override
                    public void run() {
                        dismissProgDialog();
                        switch (tag){
                            case "checkEventAvailablity":
                                checkEventAvailablity(true);
                                break;

                            case "rtlv_three":
                                position=0;
                                rtlv_three.callOnClick();
                                break;

                           /* case "getCurrentLatLng":
                                getCurrentLatLng();
                                break;*/

                        }
                    }
                }, 3 * 1000); // wait for 3 seconds
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    /***
     * This method is to check nearby events accouding to given conditions i.e, user should be within 200 meter radius from the event
     * && the event should be started i.e, user current time should be less then end time and should be greater then Start time.
     * if both the conditions are fullfiled then te event will show insted of the try demo screen.
     */
    public void checkedEventToJoin() {
        if (eventsNearbyList == null) eventsNearbyList = new ArrayList<>();
        else eventsNearbyList.clear();
        for (Events events : eventsArrayList) {

            Location startLocation = new Location("Location My");

            startLocation.setLatitude(Double.parseDouble(getLatLng()[0]));
            startLocation.setLongitude(Double.parseDouble(getLatLng()[1]));

            Location endLocation = new Location("Location end");

            endLocation.setLatitude(Double.parseDouble(events.getVenue().getLatitude()));
            endLocation.setLongitude(Double.parseDouble(events.getVenue().getLongitude()));


            double distance = endLocation.distanceTo(startLocation);
            try {
                boolean b=checkWithTime(events.getEvent().event_date , events.getEvent().interval);
                if (distance <= Constant.MAXIMUM_DISTANCE && b) {
                    events.setOngoing(true);
                    eventsNearbyList.add(events);
                }


            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(this, getResources().getString(R.string.somethingwentwrong), Toast.LENGTH_LONG).show();
            }
        }

    }

    /*********************************
     * Chekcing the Events
     ******************************************************/

    public boolean checkWithTime(final String date , double interval) throws ParseException {
        String[] dateSplit = (date.replace("TO", "T")).replace(" ", "T").split("T");
        Date startTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())).parse(dateSplit[0] + " " + dateSplit[1]);
        Date endTime = new Date(startTime.getTime()+(long) (interval* 60 * 60 * 1000));

        //Utility.e(TAG, " Date "+date+" : "+startTime+" : "+endTime);

        long currentTime = Calendar.getInstance().getTime().getTime();

        //return  currentTime > startTime.getTime();  //old ios logic

        return currentTime < endTime.getTime() && currentTime > startTime.getTime();

    }

    private boolean onNearByEventFound() {
        Utility.e(TAG, eventsNearbyList.size() + " : ");
        if (eventsNearbyList.size() >= 1) {
            NearEvent_Fragment nearEvent_fragment = new NearEvent_Fragment();
            nearEvent_fragment.setEventsList(eventsNearbyList);
            nearEvent_fragment.setNearLatLng(new String[]{getLatLng()[0],getLatLng()[1]});
            try {
                replaceFragment(nearEvent_fragment);
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        } else return true;
    }

    /*public common methods start */

    public void setTitle(String title) {
        try {
            tvHomeTitle.setText(title);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setTitleVisibility(int visibility) {
        try {
            rl_title_view.setVisibility(visibility);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void showProgDialog(boolean b , String TAG) {
        try {
            customProgressBar.setCanceledOnTouchOutside(b);
            customProgressBar.setCancelable(b);
            customProgressBar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissProgDialog() {
        try {
            if (customProgressBar != null) customProgressBar.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] getLatLng() {
        String latLng[] = new String[2];
        if(userInfo.makeAdmin.contains(Constant.ADMIN_YES) && userInfo().currentLocation)
        {
            latLng[0] = userInfo.latitude=(latitudeAdmin+"");
            latLng[1] = userInfo.longitude=(longitudeAdmin+"");
            return latLng;
        }else if(userInfo.makeAdmin.contains(Constant.ADMIN_YES) && isLocation()){
            //userInfo.setAddress("");
            latLng[0]=userInfo.latitude;
            latLng[1]=userInfo.longitude;
            return latLng;
        }  else{
            latLng[0]=latitude+"";
            latLng[1]=longitude+"";
            return latLng;
        }

    }

    public double getDistanceMile(Double[] LL){
        Utility.e("LAT LONG ", LL[0]+" "+LL[1]+" "+LL[2]+" "+LL[3]  );

        Location startPoint=new Location("locationA");
        startPoint.setLatitude(LL[0]);
        startPoint.setLongitude(LL[1]);

        Location endPoint=new Location("locationA");
        endPoint.setLatitude(LL[2]);
        endPoint.setLongitude(LL[3]);

        double distance=(startPoint.distanceTo(endPoint))*0.00062137;
        return new BigDecimal(distance ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public int getDistance(Double[] LL){
        Utility.e("LAT LONG ", LL[0] + " " + LL[1] + " " + LL[2] + " " + LL[3]);
        Location startPoint=new Location("locationA");
        startPoint.setLatitude(LL[0]);
        startPoint.setLongitude(LL[1]);

        Location endPoint=new Location("locationA");
        endPoint.setLatitude(LL[2]);
        endPoint.setLongitude(LL[3]);

        double distance=startPoint.distanceTo(endPoint);

        return (int)distance;
    }

    public double phpDistance(Double[] LL) {
        Utility.e(TAG, " Distance " + 6371000 * (Math.acos(Math.cos(Math.toRadians(LL[0])) * Math.cos(Math.toRadians(LL[2])) * Math.cos(Math.toRadians(LL[3]) - Math.toRadians(LL[1])) + Math.sin(Math.toRadians(LL[0])) * Math.sin(Math.toRadians(LL[2])))));
        return 6371000 * (Math.acos(Math.cos(Math.toRadians(LL[0])) * Math.cos(Math.toRadians(LL[2])) * Math.cos(Math.toRadians(LL[3]) - Math.toRadians(LL[1])) + Math.sin(Math.toRadians(LL[0])) * Math.sin(Math.toRadians(LL[2]))));
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Fragment getCurrentFragment(){
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            String fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
            return fragmentManager.findFragmentByTag(fragmentTag);}
        catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            return  null;
        }
    }

    /***
     * @param ViewDot One of {  #VISIBLE}, { #INVISIBLE}, or { #GONE}.
     */
    public void setBBVisibility(int ViewDot, String TAG) {
        Utility.e(TAG," B B visiballity "+ViewDot+" TAG "+TAG);
        try{
            frm_bottmbar.setVisibility(ViewDot);
            if (ViewDot == View.GONE) {

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) frame_fragments.getLayoutParams();
                if(SceneKey.sessionManager.isSoftKey()) layoutParams.bottomMargin = ActivitybottomMarginOne;
                else layoutParams.bottomMargin = 0;
                frame_fragments.setLayoutParams(layoutParams);
                bottom_margin_view.setVisibility(View.GONE);
                rl_title_view.setVisibility(View.GONE);
                setTopStatus();
                //  top_status.setVisibility(View.GONE);
            } else {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) frame_fragments.getLayoutParams();
                layoutParams.bottomMargin = (int) getResources().getDimension(R.dimen.bottomBar_margin);
                frame_fragments.setLayoutParams(layoutParams);
                bottom_margin_view.setVisibility(View.VISIBLE);
                rl_title_view.setVisibility(View.VISIBLE);
                setTopStatus();
                // top_status.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    public void setBBVisibility(final int ViewDot, final int delay, String TAG) {
        Utility.e(TAG," B B visiballity "+ViewDot+" TAG "+TAG);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (ViewDot == View.GONE) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) frame_fragments.getLayoutParams();
                    if(SceneKey.sessionManager.isSoftKey()) layoutParams.bottomMargin = ActivitybottomMarginOne;
                    else layoutParams.bottomMargin = 0;
                    frame_fragments.setLayoutParams(layoutParams);
                    rl_title_view.setVisibility(View.GONE);
                    frm_bottmbar.setVisibility(View.GONE);
                    bottom_margin_view.setVisibility(View.GONE);
                    //  top_status.setVisibility(View.GONE);
                    setTopStatus();

                } else {
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_up1);
                    Animation animation1 = AnimationUtils.loadAnimation(context, R.anim.slide_in_up);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) frame_fragments.getLayoutParams();
                    layoutParams.bottomMargin = (int) getResources().getDimension(R.dimen.bottomBar_margin);
                    frame_fragments.setLayoutParams(layoutParams);
                    rl_title_view.setVisibility(View.VISIBLE);
                    rl_title_view.startAnimation(animation1);
                    bottom_margin_view.setVisibility(View.VISIBLE);
                    //   top_status.setVisibility(View.VISIBLE);
                    setTopStatus();
                    frm_bottmbar.setVisibility(View.VISIBLE);
                    frm_bottmbar.startAnimation(animation);
                }
            }
        }, delay);

    }

    public void backPressToPosition(){
        try{
            switch (position){
                case 1:
                    rtlv_one.callOnClick();
                    break;
                case 2:
                    rtlv_two.callOnClick();
                    break;
                case 3:
                    rtlv_three.callOnClick();
                    break;
                case 4:
                    rtlv_four.callOnClick();
                    break;
                case 5:
                    rtlv_five.callOnClick();
                    break;
                default:
                    rtlv_three.callOnClick();
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        if (!(SceneKey.sessionManager.isSoftKey())){

            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // hide status bar and nav bar after a short delay, or if the user interacts with the middle of the screen
            );
            //decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // hide status bar and nav bar after a short delay, or if the user interacts with the middle of the screen
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
            //decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        top_status.setVisibility(View.GONE);
    }

    public void showStatusBar(){
        getWindow().clearFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            top_status.setBackgroundResource(R.color.white);
            isApiM = true;
        }
        else {
            StatusBarUtil.setStatusBarColor(this,R.color.new_white_bg);
            setStatusBarVisible();
        }
    }

    public void setTopStatus(){

        if (isKitKat) {
            setStatusBarVisible();
            top_status.setBackgroundResource(R.color.black);
        }
        if (isApiM) {
            setStatusBarVisible();
            top_status.setBackgroundResource(R.color.white);
        }
    }

    private void setStatusBarVisible(){
        if (!(SceneKey.sessionManager.isSoftKey())) {
            top_status.setVisibility(View.VISIBLE);
        }
    }

    public void onPopUpBackstage(){
        if(getCurrentFragment() instanceof StatusBarHide){
            hideStatusBar();
        }
        else showStatusBar();
    }

/*    public boolean isForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }*/

//TODO message on increment or decrement

    public void incrementKeyPoints(String msg){
        final int points = Integer.parseInt(userInfo.keyPoints);
        new Aws_Web_Service() {
            @Override
            public okhttp3.Response onResponseUpdate(okhttp3.Response response) {
                Utility.e(TAG, "Increment response " + response);
                if(response==null) return null;
                try {
                    String s = response.body().string();
                    if(new JSONObject(s).getInt("serverStatus")==2){
                        Utility.e("Response",s);
                        userInfo.keyPoints=((points+1)+"");
                        showKeyPoints("+1 Key Points!");
                        updateSession(userInfo);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
                return response;
            }
        }.updateKeyPoint(points + 1, userInfo.userID);
    }

    private void showKeyPoints(String s) {
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.custom_keypoint_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationLeftRight; //style id

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.gravity = Gravity.TOP;
        dialog.getWindow().setAttributes(lp);

        TextView tvKeyPoint;

        tvKeyPoint = dialog.findViewById(R.id.tvKeyPoint);
        tvKeyPoint.setText(s);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 2000);

        dialog.show();
    }

    public void decrementKeyPoints(final String msg){
        final int points = Integer.parseInt(userInfo.keyPoints);
        new Aws_Web_Service() {
            @Override
            public okhttp3.Response onResponseUpdate(okhttp3.Response response) {
                if(response==null) return null;
                try {
                    String s = response.body().string();
                    if(new JSONObject(s).getInt("serverStatus")==2){
                        //   utility.showCustomPopup(msg, String.valueOf(R.font.arial_regular));

                        showKeyPoints("-1 Key Points!");
                        userInfo.keyPoints=(points <=0?0+"":(points-1)+"");
                        updateSession(userInfo);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
                return response;
            }
        }.updateKeyPoint((points <= 0 ? 25 : points - 1), userInfo.userID);
    }

    public void keyPointsUpdate() {

        final int points = Integer.parseInt(userInfo.keyPoints);
        new Aws_Web_Service() {
            @Override
            public okhttp3.Response onResponseUpdate(okhttp3.Response response) {
                if (response == null) return null;
                try {
                    String s = response.body().string();
                    if (new JSONObject(s).getInt("serverStatus") == 2) {

                        userInfo.keyPoints = Integer.parseInt(userInfo.keyPoints) - 1 + "";
                        updateSession(userInfo);

                        if (userInfo.keyPoints.equals("0")) {
                            userInfo.keyPoints = 25 + "";
                            updateSession(userInfo);
                        }
                        dismissProgDialog();
                        //   utility.showCustomPopup(msg, String.valueOf(R.font.arial_regular));
                        // TSnackbar.make(rl_title_view, "-1 Key Points!", TSnackbar.LENGTH_LONG).show();
                        showKeyPoints("-1 Key Points!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }.updateKeyPoint((points <= 0 ? 25 : points - 1), userInfo.userID);

    }

    public void showCustomPopup(String message, final int call) {
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.custom_popup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tvPopupOk, tvMessages;

        tvMessages = dialog.findViewById(R.id.custom_popup_tvMessage);
        tvPopupOk = dialog.findViewById(R.id.custom_popup_ok);
        tvPopupOk.setText(R.string.ok);
        tvMessages.setText(message);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (call == 1) {
                    incrementKeyPoints(getString(R.string.kp_like));
                } else {
                    decrementKeyPoints(getString(R.string.kp_unlike));
                }
            }
        });

        tvPopupOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show location settings when the user acknowledges the alert dialog
                dialog.cancel();

            }
        });

        dialog.show();
    }

    public String getCurrentTimeInFormat() {
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault())).format(new Date(System.currentTimeMillis()));
    }

    public String getAddress(double latitude, double longitude) {
        String result = null;
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            //  String addressLine = addresses.get(0).getAddressLine(1);
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            // String postalCode = addresses.get(0).getPostalCode();
            // String knownName = addresses.get(0).getFeatureName();
            //result = knownName + " ," + addressLine + " , " + city + "," + state + "," + country + " counter" + counter;// Here 1 represent max location result to returned, by documents it recommended 1 to 5
            result =  address+","+city + "," + state + "," + country ;// Here 1 represent max location result to returned, by documents it recommended 1 to 5
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    /*public common methods end*/

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            // Logic to handle location object
            latitude=location.getLatitude();
            longitude =location.getLongitude();
            latitudeAdmin=location.getLatitude();
            longitudeAdmin =location.getLongitude();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Utility.e("Latitude","disable");
        if (provider.equals("network")){
            utility.checkGpsStatus();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Utility.e("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Utility.e("Latitude","status");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.IMAGE_UPLOAD_CALLBACK) {
            if(resultCode == Activity.RESULT_OK){
                if (getCurrentFragment() != null) {
                    try {
                        getCurrentFragment().onActivityResult(requestCode, resultCode, data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }



    }

}
