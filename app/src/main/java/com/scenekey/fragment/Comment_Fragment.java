package com.scenekey.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.scenekey.R;
import com.scenekey.activity.HomeActivity;
import com.scenekey.helper.Constant;
import com.scenekey.helper.WebServices;
import com.scenekey.model.UserInfo;
import com.scenekey.util.CircleTransform;
import com.scenekey.util.Utility;
import com.scenekey.volleymultipart.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Comment_Fragment extends Fragment implements View.OnClickListener {

    private final String TAG = Comment_Fragment.class.toString();
    private HomeActivity activity;
    private Context context;
    private Utility utility;
    private String[] currentLatLng;
    private int maxNumber = 120;
    private TextView txt_char;
    private EditText edt_comment;
    private String kyeInStatus,eventId, eventDate, eventName;
    private Event_Fragment event_fragment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this event_fragment
        View view= inflater.inflate(R.layout.fragment_comment, container, false);
        //for status bar manage
        activity.setTopStatus();

        txt_char =  view.findViewById(R.id.txt_char);
        edt_comment =  view.findViewById(R.id.edt_comment);
        ImageView img_profile =  view.findViewById(R.id.img_profile);
        ImageView imgPost = view.findViewById(R.id.imgPost);
        TextView txt_post_comment =  view.findViewById(R.id.txt_post_comment);
        ImageView img_f1_back =  view.findViewById(R.id.img_f1_back);
        view.findViewById(R.id.mainlayout).setOnClickListener(this);    //for background click on fragment
        txt_char.setText(maxNumber + " ");

        img_f1_back.setOnClickListener(this);
        txt_post_comment.setOnClickListener(this);
        imgPost.setOnClickListener(this);

        Picasso.with(activity).load(activity.userInfo().getUserImage()).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(img_profile);

        edt_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                txt_char.setText((maxNumber - s.length()) + " ");

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
        activity= (HomeActivity) getActivity();
        utility = new Utility(context);
    }

    private UserInfo userInfo() {
        return activity.userInfo();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgPost:
                if (kyeInStatus.equals(Constant.KEY_NOTEXIST)) {
                    activity.showProgDialog(false,TAG);
                    addUserIntoEvent();

                } else {
                    activity.showProgDialog(false,TAG);
                    commentEvent();
                }

                activity.onBackPressed();
                break;
            case R.id.img_f1_back:
                activity.onBackPressed();
                break;

        }
    }

    private void addUserIntoEvent() {
        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.ADD_EVENT, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    activity.dismissProgDialog();
                    // get response
                    try {
                        if(new JSONObject(response).getInt("success")==0){
                            activity.incrementKeyPoints(getString(R.string.kp_keyin));
                        }
                        commentEvent();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utility.showToast(context,getString(R.string.somethingwentwrong),0);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    utility.volleyErrorListner(e);
                    activity.dismissProgDialog();
                    commentEvent();
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("userid", userInfo().userID);
                    params.put("eventname", eventName);
                    params.put("eventid", eventId);
                    params.put("Eventdate", eventDate);  //userInfo().getUserID()

                    Utility.e(TAG, " params " + params.toString());
                    return params;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(20000, 0, 1));
        } else {
            utility.snackBar(txt_char, getString(R.string.internetConnectivityError), 0);
            activity.dismissProgDialog();
        }
    }

    private void commentEvent() {
        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.EVENT_COMMENT, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    activity.dismissProgDialog();
                    // get response

                    try {
                        if (new JSONObject(response).getString("msg").equals("Success")) {
                            activity.showCustomPopup("Comment has been posted successfully.", 1);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
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

                    params.put("user_id",  userInfo().userID);
                    params.put("event_id", eventId);
                    params.put("location", getLocation());
                    params.put("comment", edt_comment.getText().toString().trim());
                    params.put("ratingtime", activity.getCurrentTimeInFormat());

                    Utility.e(TAG, "" + params.toString());

                    return params;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(20000, 0, 1));
        } else {
            utility.snackBar(txt_char, getString(R.string.internetConnectivityError), 0);
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

    /* for event fragment */
    public Comment_Fragment setData(String[] currentLatLng, String kyeInStatus, String eventId, String eventDate, String eventName, Event_Fragment event_fragment) {
      this.currentLatLng=currentLatLng;
        this.event_fragment = event_fragment;
        this.kyeInStatus =kyeInStatus;
        this.eventId=eventId;
        this.eventDate =((eventDate.replace(" ", "T")).replace("TO", "T").split("TO"))[0];
        this.eventName =eventName;
        return this;
    }

    @Override
    public void onDestroy() {
        if(event_fragment !=null) {
            event_fragment.canCallWebservice = true;
            event_fragment.getAllData();
        }
        /*if(key_in_event_fragment !=null) {
            Key_In_Event_Fragment.can = true;
            key_in_event_fragment.getAlldata();
        }*/
        super.onDestroy();
    }

}
