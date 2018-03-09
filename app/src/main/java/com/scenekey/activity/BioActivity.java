package com.scenekey.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.scenekey.R;
import com.scenekey.fragment.Bio_Fragment;
import com.scenekey.helper.CustomProgressBar;
import com.scenekey.helper.WebServices;
import com.scenekey.model.UserInfo;
import com.scenekey.util.SceneKey;
import com.scenekey.util.StatusBarUtil;
import com.scenekey.util.Utility;
import com.scenekey.volleymultipart.VolleySingleton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BioActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = Bio_Fragment.class.toString();
    private Context context = this;
    private UserInfo userInfo;
    private TextView tv_for_remainChar;
    private EditText et_for_enterTxt;
    private CustomProgressBar prog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  StatusBarUtil.setTranslucent(this);
        setContentView(R.layout.fragment_bio);

        setStatusBar();
        userInfo = SceneKey.sessionManager.getUserInfo();
        prog = new CustomProgressBar(this);

        tv_for_remainChar = findViewById(R.id.tv_for_remainChar);
        et_for_enterTxt = findViewById(R.id.et_for_enterTxt);

        findViewById(R.id.btn_for_done).setOnClickListener(this);
        findViewById(R.id.img_f1_back).setOnClickListener(this);

        TextWatcher textWatcher = new TextWatcher() {
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

    }

    private void setStatusBar() {
        View top_status = findViewById(R.id.top_status);
        if (!(SceneKey.sessionManager.isSoftKey())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                StatusBarUtil.setStatusBarTranslucent(this,true);
            }else{
                top_status.setVisibility(View.GONE);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = getWindow().getDecorView();
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                top_status.setBackgroundResource(R.color.white);
                top_status.setVisibility(View.VISIBLE);
            }
            else {
                StatusBarUtil.setStatusBarColor(this,R.color.new_white_bg);
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

    }


    private void showProgDialog(boolean cancelable){
        prog.setCancelable(cancelable);
        prog.setCanceledOnTouchOutside(cancelable);
        prog.show();
    }

    private void dismissProgDialog() {
        if (prog != null) prog.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_for_done:
                try {
                    String bio=et_for_enterTxt.getText().toString();
                    if (bio.isEmpty()){
                        callIntent();
                    }else
                        updateBio(bio);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                break;

            case R.id.img_f1_back:
                onBackPressed();
                break;
        }
    }

    public UserInfo userInfo(){
        if(userInfo == null) {
            if(!SceneKey.sessionManager.isLoggedIn()){
                SceneKey.sessionManager.logout(this);
            }
            userInfo = SceneKey.sessionManager.getUserInfo();
        }
        return userInfo;
    }

    private void updateBio(final String bio) {
        showProgDialog(false);
        final Utility utility=new Utility(context);

        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.BIO, new Response.Listener<String>() {
                @Override
                public void onResponse(String Response) {
                    // get response
                    JSONObject jsonObject;
                    try {
                        dismissProgDialog();
                        // System.out.println(" login response" + response);
                        jsonObject = new JSONObject(Response);
                        int statusCode = jsonObject.getInt("status");
                        String message = jsonObject.getString("message");

                        if (statusCode==1){
                            UserInfo userInfo=userInfo();
                            userInfo.bio=bio;
                            SceneKey.sessionManager.createSession(userInfo);

                            callIntent();
                        }else{
                            Utility.showToast(context,message,0);
                        }

                    } catch (Exception ex) {
                        dismissProgDialog();
                        ex.printStackTrace();
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
                    params.put("bio",bio);
                    params.put("user_id",userInfo().userID);

                    return params;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        }else{
            utility.snackBar(et_for_enterTxt,getString(R.string.internetConnectivityError),0);
            dismissProgDialog();
        }
    }

    private void callIntent() {
        Intent objSuccess=new Intent(context,HomeActivity.class);
        // Closing all the Activities
        objSuccess.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add new Flag to start new Activity
        objSuccess.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(objSuccess);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
