package com.scenekey.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import com.scenekey.adapter.TryDemo_Adapter;
import com.scenekey.cus_view.ProfilePopUp_Demo;
import com.scenekey.helper.Constant;
import com.scenekey.helper.Permission;
import com.scenekey.lib_sources.Floting_menuAction.FloatingActionButton;
import com.scenekey.lib_sources.Floting_menuAction.FloatingActionMenu;
import com.scenekey.lib_sources.SwipeCard.Card;
import com.scenekey.lib_sources.SwipeCard.CardsAdapter;
import com.scenekey.lib_sources.SwipeCard.SwipeCardView;
import com.scenekey.listener.StatusBarHide;
import com.scenekey.model.NotificationData;
import com.scenekey.model.RoomPerson;
import com.scenekey.model.UserInfo;
import com.scenekey.util.Utility;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;

public class Demo_Event_Fragment extends Fragment implements View.OnClickListener,StatusBarHide  {

    private final String TAG = Demo_Event_Fragment.class.toString();

    private Context context;
    private HomeActivity activity;
    private Home_No_Event_Fragment home_no_event;

    private ImageView imageMap,img_edit_i1,img_notif,img_infoget_f2,img_f10_back;
    private TextView txt_discrp,txt_room,txt_f2_badge,txt_discipI_f2,btn_got_it;

    private FloatingActionButton fabMenu1,fabMenu2,fabMenu3;
    private FloatingActionMenu menu_blue;

    private SwipeCardView card_stack_view;

    private ArrayList<Card> cardList;
    private ArrayList<NotificationData> nList;

    private CardsAdapter arrayAdapter;
    private boolean liked;


    //Demo Screen
    private RelativeLayout demoView;

    private  Utility utility;

    private MapView map_view;
    private GoogleMap googleMap;

    private int currentPosition;
    private double latitude,longitude;

    private LinearLayout info_view;
    private RelativeLayout rtlv2_animate_f2;
    private RecyclerView rclv_grid;
    private ScrollView scrl_all;
    private Handler handler;
    private View popupview;
    private Dialog dialog;
    private int noNotify,timer;
    private Timer t;
    private Boolean initialized = false,isInfoVisible;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_demo_event, container, false);

        txt_discipI_f2 =  view.findViewById(R.id.txt_discipI_f2);

        info_view =  view.findViewById(R.id.info_view);
        img_infoget_f2 =  view.findViewById(R.id.img_infoget_f2);
        rclv_grid =  view.findViewById(R.id.rclv_grid);
        img_f10_back =  view.findViewById(R.id.img_f10_back);
        txt_f2_badge =  view.findViewById(R.id.txt_f2_badge);

        txt_discrp =  view.findViewById(R.id.txt_discrp);
        txt_room =  view.findViewById(R.id.txt_room);
        scrl_all =  view.findViewById(R.id.scrl_all);
        imageMap =  view.findViewById(R.id.image_map);
        RelativeLayout rtlv_top =  view.findViewById(R.id.rtlv_top);
        menu_blue =  view.findViewById(R.id.menu_blue);
        fabMenu1 =  view.findViewById(R.id.fabMenu1_like);
        fabMenu2 =  view.findViewById(R.id.fabMenu2_picture);
        fabMenu3 =  view.findViewById(R.id.fabMenu3_comment);
        demoView =  view.findViewById(R.id.demoView);
        btn_got_it =  view.findViewById(R.id.btn_got_it);
        img_edit_i1 =  view.findViewById(R.id.img_edit_i1);

        card_stack_view =  view.findViewById(R.id.card_stack_view);//TinderSwipe
        rtlv2_animate_f2 =  view.findViewById(R.id.rtlv2_animate_f2);
        LinearLayout no_one =  view.findViewById(R.id.no_one);
        no_one.setVisibility(View.GONE);

        latitude = 34.0527682;
        longitude = -118.2455121;
        info_view.setVisibility(View.GONE);
        rtlv_top.getLayoutParams().height = ((HomeActivity.ActivityWidth) * 3 / 4);

        activity.setBBVisibility(View.GONE,TAG);
        activity.hideStatusBar();

        map_view =  view.findViewById(R.id.map_view);
        map_view.onCreate(savedInstanceState);
        map_view.onResume();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        utility=new Utility(context);
        TextView txt_hide_all_one =  view.findViewById(R.id.txt_hide_all_one);
        TextView txt_hide_all_two =  view.findViewById(R.id.txt_hide_all_two);
        TextView txt_calender_i1 =  view.findViewById(R.id.txt_calender_i1);
        TextView txt_event_name =  view.findViewById(R.id.txt_event_name);
        img_notif =  view.findViewById(R.id.img_notif);
        fabMenu1.setTextView(new TextView[]{txt_hide_all_one, txt_hide_all_two});

        setOnClick(imageMap,
                img_infoget_f2,img_edit_i1,
                img_f10_back,
                fabMenu1,
                fabMenu2,
                fabMenu3,
                img_notif,
                txt_hide_all_one,
                txt_hide_all_two,
                btn_got_it,demoView);
        isInfoVisible = false;
        rclv_grid.hasFixedSize();

        UserInfo userInfo = activity.userInfo();
        if(userInfo.firstTimeDemo){
            demoView.setVisibility(View.VISIBLE);
            userInfo.firstTimeDemo=false;
            activity.updateSession(userInfo);

        }
        activity.dismissProgDialog();

        //TinderSwipe
        cardList = new ArrayList<>();
        getDummyData(cardList);
        arrayAdapter = new CardsAdapter(context, cardList);
        card_stack_view.setAdapter(arrayAdapter);
        noNotify = 5;

        setTextBadge();

        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        txt_calender_i1.setText(date + " 8:00 AM - 12:00 PM");
        txt_discipI_f2.setText("4975 W Pico Blvd,los angeles ");

        new Handler().post(new Runnable() {
           @Override
           public void run() {
               mapAsyncer(latitude,longitude);
           }
       });
    }

    private void setOnClick(View... views) {
        for (View v : views) {
            v.setOnClickListener(this);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
        activity= (HomeActivity) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        activity.setBBVisibility(View.GONE,TAG);

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_to_position);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!initialized) {
                    InitializeGrid();
                    int height = (int) activity.getResources().getDimension(R.dimen._120sdp);
                    int width =  activity.ActivityWidth;
                    String url = "http://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=12&size=" + width + "x" + height + "&sensor=false";
                    Picasso.with(activity).load(url).into(imageMap);
                    initialized = true;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        rtlv2_animate_f2.setAnimation(animation);
        handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rtlv2_animate_f2.setBackgroundColor(getResources().getColor(R.color.bg_scenepage));
            }
        }, 2000);

    }

    @Override
    public void onResume() {
        super.onResume();
        activity.setBBVisibility(View.GONE,TAG);
        activity.hideStatusBar();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_edit_i1:
            /*    try {
                    activity.addFragment(new Edit_Event_Demo_Fragmet().setData("1232", new SimpleDateFormat("yyyy-mm-dd").format(Calendar.getInstance().getTime())+"TO8:00:00TO12:0:00",getString(R.string.sample_event) ,4.0+"" , "12",getString(R.string.sample_Address),txt_discipI_f2.getText().toString(),this),1);
                }catch (NullPointerException e){}*/
                break;

            case R.id.image_map:
                try {
                   activity.addFragment(new SingleMap_Fragment().setData(String.valueOf(latitude),String.valueOf(longitude)),1);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            //   Utility.showToast(context,getString(R.string.underDevelopment),0);
                break;
            case R.id.img_infoget_f2:
                animateInfo(isInfoVisible);
                break;
            case R.id.img_f10_back:
                activity.onBackPressed();
                break;
            case R.id.fabMenu1_like:
                menu_blue.close(true);

                if(!liked){
                    liked=true;
                    fabMenu1.setImageDrawable(getResources().getDrawable(R.drawable.red_heart));

                    utility.showCustomPopup("You liked the event.", String.valueOf(R.font.montserrat_medium));
                }
                else {
                    liked=false;
                    fabMenu1.setImageDrawable(getResources().getDrawable(R.drawable.heart));
                    utility.showCustomPopup("You unliked the event.", String.valueOf(R.font.montserrat_medium));
                }
                break;
            case R.id.fabMenu2_picture:
                menu_blue.close(true);
                captureImage();
                break;
            case R.id.fabMenu3_comment:
                menu_blue.close(true);
                try {
                    activity.addFragment(new Demo_Comment_Fragment().setData(this),1);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                break;
            case R.id.txt_hide_all_two:
                menu_blue.close(true);
                break;
            case R.id.txt_hide_all_one:
                menu_blue.close(true);
                break;
            case R.id.img_notif:
                if (noNotify > 0 && (nList != null && nList.get(noNotify - 1)!= null) ) {
                    popup_notification_new(noNotify);
                }
                else noNotification();
                break;
            case R.id.btn_got_it:
                demoView.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        activity.setBBVisibility(View.VISIBLE, 500, TAG);
        activity.showStatusBar();
        initialized = false;
        super.onDestroyView();
    }

    public void setImageArray(Home_No_Event_Fragment home_no_event) {
        this.home_no_event = home_no_event;
    }

    private void animateInfo(boolean currentVisible) {
        if (!currentVisible) {
            Animation translate_animate = AnimationUtils.loadAnimation(getActivity(), R.anim.translet_up_down);
            info_view.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) info_view.getLayoutParams();
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            info_view.setLayoutParams(layoutParams);
            scrl_all.smoothScrollTo(0, 0);
            info_view.setAnimation(translate_animate);
            rtlv2_animate_f2.setAnimation(translate_animate);
            rclv_grid.setAnimation(translate_animate);

            isInfoVisible = true;
        } else {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) info_view.getLayoutParams();
            layoutParams.height = 0;
            info_view.setLayoutParams(layoutParams);
            Animation translate_animate = AnimationUtils.loadAnimation(getActivity(), R.anim.translet_up_down);
            rtlv2_animate_f2.setAnimation(translate_animate);
            rclv_grid.setAnimation(translate_animate);
            isInfoVisible = false;
        }
    }

    private void getDummyData(ArrayList<Card> al) {
        Card card = new Card();
        card.name = "Card1";
        card.imageId = R.drawable.demo_1;
        card.imageint = R.drawable.room_1;
        card.date = "2017-5-16 10:12:00";
        al.add(card);

        Card card2 = new Card();
        card2.name = "Card2";
        card2.text  = getResources().getString(R.string.omg__smooth);
        card2.imageint = R.drawable.room_2;
        card2.date = "2017-5-16 16:18:00";
        al.add(card2);

    }

    public void addUserComment(String comment){
        Card card = new Card();
        card.text = comment;
        card.userImage = activity.userInfo().getUserImage();
        cardList.add(0,card);
        arrayAdapter.notifyDataSetChanged();
        card_stack_view.setAdapter(arrayAdapter);
        card_stack_view.restart();
    }

    private void setTextBadge() {

        txt_f2_badge.setText(noNotify + "");
        if (noNotify > 0)
        {
            txt_f2_badge.setBackground(getResources().getDrawable(R.drawable.bg_circle_red_badge));
            img_notif.setImageResource(R.drawable.bell_red);
            img_notif.setBackgroundResource(R.drawable.bg_bell_red);
        }
        else
        {
            txt_f2_badge.setBackground(getResources().getDrawable(R.drawable.bg_primary_circle));
            img_notif.setImageResource(R.drawable.bell);
            img_notif.setBackgroundResource(R.drawable.bg_bell);
        }
    }

    private void InitializeGrid() {
        rclv_grid.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        rclv_grid.setLayoutManager(layoutManager);

        final String android_version_names[] = {
                "Alexander Alex",
                "Alizee French",
                "Amy Jackson",
                "Dahn-mein Siegel",
                "Darrin Espanto",
                "Erin Dietrich",
                "James Red",
                "Morgan Freeman",
                activity.userInfo().userName

        };

        final String android_image_urls[] = {
                "" + R.drawable.room_1,
                "" + R.drawable.room_2,
                "" + R.drawable.room_3,
                "" + R.drawable.room_4,
                "" + R.drawable.room_5,
                "" + R.drawable.room_6,
                "" + R.drawable.room_7,
                "" + R.drawable.room_8,
                activity.userInfo().getUserImage()
        };

        final String staus[] = {
                "avilable",
                "busy",
                "na",
                "busy",
                "na",
                "avilable",
                "avilable",
                "busy",
                "na",

        };


        final ArrayList<RoomPerson> roomPersonList = new ArrayList<>();
        final TryDemo_Adapter adapter = new TryDemo_Adapter(roomPersonList, activity, home_no_event,this);
        rclv_grid.setAdapter(adapter);

        for (int i = 0; i < 9; i++) {
            roomPersonList.add(new RoomPerson(android_version_names[i], android_image_urls[i], staus[i]));
            adapter.notifyItemInserted(i);
        }

        //Notification Data
        if (nList == null) nList = new ArrayList<>();

        nList.add(new NotificationData(R.drawable.room_1, "\uD83D\uDD31 , \u2665 , \u2705 , \uD83D\uDEB7 ","Alexander Alex"));
        nList.add(new NotificationData(R.drawable.room_2, "\uD83D\uDEA4 , \uD83D\uDE83 , \uD83D\uDE96 , \u2708 " , "Alizee French"));
        nList.add(new NotificationData(R.drawable.room_3, "\uD83C\uDF37 , \uD83C\uDF3B , \uD83C\uDF1E , \uD83C\uDF3C " ,"Amy Jackson"));
        nList.add(new NotificationData(R.drawable.room_4, "\uD83C\uDF81 , \uD83D\uDC9D , \uD83C\uDF85 , \uD83C\uDF89 ","Dahn-mein Siegel"));
        nList.add(new NotificationData(R.drawable.room_5, "\uD83D\uDE1C , \uD83D\uDE1D , \uD83D\uDE33 , \uD83D\uDE18 " ,"Darrin Espanto"));
        nList.add(new NotificationData(R.drawable.room_6, "\uD83D\uDEA4 , \uD83D\uDE83 , \uD83D\uDE96 , \u2708 " ,"Erin Dietrich"));
        nList.add(new NotificationData(R.drawable.room_7, "\uD83D\uDE1C , \uD83D\uDE1D , \uD83D\uDE33 , \uD83D\uDE18 " ,"James Red"));
        nList.add(new NotificationData(R.drawable.room_8, "\uD83C\uDF81 , \uD83D\uDC9D , \uD83C\uDF85 , \uD83C\uDF89 ","Morgan Freeman"));

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

        getDemoData2();


    }

    private void getDemoData2() {
        Card card3 = new Card();
        card3.name = "Card3";
        card3.imageId = R.drawable.demo_2;
        card3.imageint = R.drawable.room_3;
        card3.date = "2017-5-16 20:45:00";
        cardList.add(card3);

        Card card4 = new Card();
        card4.name = "Card4";
        card4.text = getResources().getString(R.string.i_have_best);
        card4.imageint = R.drawable.room_4;
        card4.date = "2017-5-16 22:26:00";
        cardList.add(card4);

        Card card5 = new Card();
        card5.name = "Card5";
        card5.imageId = R.drawable.demo_3;
        card5.imageint = R.drawable.room_5;
        card5.date = "2017-5-16 17:22:00";
        cardList.add(card5);

        Card card6 = new Card();
        card6.name = "Card6";
        card6.imageId = R.drawable.demo_4;
        card6.imageint = R.drawable.room_6;
        card6.date = "2017-5-16 18:46:00";
        cardList.add(card6);

        Card card7 = new Card();
        card7.name = "Card7";
        card7.text = getResources().getString(R.string.omg__smooth);
        card7.imageint = R.drawable.room_7;
        card7.date = "2017-5-16 9:12:00";
        cardList.add(card7);

        Card card8 = new Card();
        card8.name = "Card8";
        card8.imageId =R.drawable.demo_5;
        card8.imageint = R.drawable.room_8;
        card8.date = "2017-5-16 20:20:00";
        cardList.add(card8);

        Card card9 = new Card();
        card9.name = "card9";
        card9.text = getResources().getString(R.string.i_have_best);
        card9.imageint = R.drawable.room_1;
        card9.date = "2017-5-16 22:45:00";
        cardList.add(card9);

        Card card10 = new Card();
        card10.name = "card10";
        card10.imageId =R.drawable.demo_6;
        card10.imageint = R.drawable.room_2;
        card10.date = "2017-5-16 19:12:00";
        cardList.add(card10);

        Card card11 = new Card();
        card11.name = "Card8";
        card11.imageId =R.drawable.demo_7;
        card11.imageint = R.drawable.room_3;
        card11.date = "2017-5-16 18:46:00";
        cardList.add(card11);

        Card card12 = new Card();
        card12.name = "card9";
        card12.text = getResources().getString(R.string.ilovemike);
        card12.imageint = R.drawable.room_5;
        card12.date = "2017-5-16 23:12:00";
        cardList.add(card12);

        arrayAdapter.notifyDataSetChanged();
    }

    private void mapAsyncer(final double lat , final double lng) {
        map_view.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                Marker m = null;
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(false);
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

                Marker mr = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .anchor(0.5f, 0.5f)
                        .title("Staples Center")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)));
                mr.showInfoWindow();
                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {


                    }
                });
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        marker.showInfoWindow();

                        return true;
                    }
                });

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

    private void captureImage() {
        Permission permission = new Permission(activity);
        if (permission.checkCameraPermission()) callIntent(Constant.INTENT_CAMERA);
    }

    public void callIntent(int caseId) {

        switch (caseId) {
            case Constant.INTENT_CAMERA:
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                   // intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    startActivityForResult(intent, Constant.INTENT_CAMERA);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private String getCurrentTimeInFormat() {
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault())).format(new Date(System.currentTimeMillis()));
    }

    public void noNotification() {
        utility.showCustomPopup(getString(R.string.noNotification), String.valueOf(R.font.montserrat_medium));
    }

    private void popup_notification_new(final int position){
        noNotify-=1;
        setTextBadge();
        currentPosition = position-1;
        new ProfilePopUp_Demo(activity,4, getData(currentPosition),1) {
            @Override
            public void onClickView(TextView textView, ProfilePopUp_Demo profilePopUp) {
                profilePopUp.setText(textView.getText().toString());
                setTextBadge();
            }

            @Override
            public void onSendCLick(TextView textView, ProfilePopUp_Demo profilePopUp, NotificationData obj) {
            }

            @Override
            public void onPrevClick(ImageView textView, ProfilePopUp_Demo profilePopUp) {
                if(currentPosition<5){
                    currentPosition+=1;
                    updateData(getData(currentPosition));
                }
            }

            @Override
            public void onNextClick(ImageView textView, ProfilePopUp_Demo profilePopUp) {
                if(currentPosition>0){

                    currentPosition-=1;
                    if(currentPosition<noNotify){
                        noNotify-=1;
                        setTextBadge();
                    }
                    updateData(getData(currentPosition));
                }
                if(currentPosition==0){
                    noNotification();
                }
            }

            @Override
            public void onDismiss(ProfilePopUp_Demo profilePopUp) {

            }
        }.show();
    }

    public NotificationData getData(int position){
        return nList.get(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        activity.setBBVisibility(View.GONE,TAG);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {

            if (requestCode == Constant.INTENT_CAMERA && data != null) {
                final Bitmap eventImg = (Bitmap) data.getExtras().get("data");
                Card card = new Card();
                card.bitmap = eventImg;
                card.name = "Card";
                card.userImage =activity.userInfo().getUserImage();
                card.date = getCurrentTimeInFormat();
                cardList.add(0,card);
                //  activity.setBBvisiblity(View.GONE,TAG);
                arrayAdapter.notifyDataSetChanged();
                card_stack_view.restart();
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
    public boolean onStatusBarHide() {
        return false;
    }
}
