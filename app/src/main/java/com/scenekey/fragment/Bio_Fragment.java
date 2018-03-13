package com.scenekey.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.scenekey.R;
import com.scenekey.activity.HomeActivity;
import com.scenekey.helper.WebServices;
import com.scenekey.model.UserInfo;
import com.scenekey.util.Utility;
import com.scenekey.volleymultipart.VolleySingleton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Bio_Fragment extends Fragment implements View.OnClickListener {

    private final String TAG = Bio_Fragment.class.toString();

    private TextView tv_for_remainChar;
    private EditText et_for_enterTxt;
    private HomeActivity activity;
    private Context context;
    private String oldBio="";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_bio, container, false);

        tv_for_remainChar = view.findViewById(R.id.tv_for_remainChar);
        et_for_enterTxt = view.findViewById(R.id.et_for_enterTxt);

        view.findViewById(R.id.btn_for_done).setOnClickListener(this);
        view.findViewById(R.id.img_f1_back).setOnClickListener(this);
        view.findViewById(R.id.llBioFragment).setOnClickListener(this);   //for background click on fragment

        TextWatcher  textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int text = 60 - s.length();
                tv_for_remainChar.setText(text+"");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        et_for_enterTxt.addTextChangedListener(textWatcher);

        //for status bar manage
        activity.setTopStatus();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            if (!activity.userInfo().bio.isEmpty()){
                et_for_enterTxt.setText(activity.userInfo().bio);
                oldBio=activity.userInfo().bio;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
        activity= (HomeActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_for_done:
                try {
                    String bio=et_for_enterTxt.getText().toString();
                    if (oldBio.equals(bio)){
                        activity.onBackPressed();
                    }else {
                        updateBio(bio);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;

            case R.id.img_f1_back:
                activity.onBackPressed();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateBio(final String bio) {
        activity.showProgDialog(false,TAG);
        final Utility utility=new Utility(context);

        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.BIO, new Response.Listener<String>() {
                @Override
                public void onResponse(String Response) {
                    // get response
                    JSONObject jsonObject;
                    try {
                        activity.dismissProgDialog();
                        // System.out.println(" login response" + response);
                        jsonObject = new JSONObject(Response);
                        int statusCode = jsonObject.getInt("status");
                        String message = jsonObject.getString("message");

                        if (statusCode==1){
                            UserInfo userInfo=activity.userInfo();
                            userInfo.bio=bio;
                            activity.updateSession(userInfo);
                            activity.onBackPressed();

                        }else{
                            Utility.showToast(context,message,0);
                        }

                    } catch (Exception ex) {
                        activity.dismissProgDialog();
                        ex.printStackTrace();
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
                    params.put("bio",bio);
                    params.put("user_id",activity.userInfo().userID);

                    return params;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        }else{
            utility.snackBar(et_for_enterTxt,getString(R.string.internetConnectivityError),0);
            activity.dismissProgDialog();
        }
    }
}