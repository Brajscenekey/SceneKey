package com.scenekey.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.scenekey.R;
import com.scenekey.aws_service.AWSImage;
import com.scenekey.helper.Constant;
import com.scenekey.helper.CustomProgressBar;
import com.scenekey.helper.Permission;
import com.scenekey.helper.SessionManager;
import com.scenekey.helper.Validation;
import com.scenekey.helper.WebServices;
import com.scenekey.model.UserInfo;
import com.scenekey.util.SceneKey;
import com.scenekey.util.Utility;
import com.scenekey.volleymultipart.VolleyMultipartRequest;
import com.scenekey.volleymultipart.VolleySingleton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.scenekey.helper.Constant.REQUEST_ID_MULTIPLE_PERMISSIONS;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener,LocationListener {

    private final String TAG="LoginActivity";
    private Context context=this;
    private EditText etEmail,etPwd;
    private Button btnLogin,btnFB;

    private double latitude=0.0, longiude=0.0;
    private boolean checkGPS;

    private Permission permission;
    private  LocationManager locationManager;
    private CustomProgressBar customProgressBar;
    private Utility utility;
    private CallbackManager objFbCallbackManager;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initSdk();
        sessionManager = new SessionManager(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setStatusBarColor();
        initView();
        utility.checkGpsStatus();

        sessionManager.setSoftKey(hasSoftKeys(getWindowManager()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        permission.requestMultiplePermission();
        initLocation();
    }

    private void initSdk() {
        FacebookSdk.sdkInitialize(context);
        FacebookSdk.setIsDebugEnabled(true);
        AppEventsLogger.activateApp(this);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        FirebaseApp.initializeApp(context);
    }

    private  void initLocation() {
        try {
            // get GPS status
            checkGPS = locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // get network provider status
            boolean checkNetwork = locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           /* CustomPopup customPopup=new CustomPopup(LoginActivity.this);
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

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.bgImage));
        }
    }

    private void initView() {
        etEmail=findViewById(R.id.etEmail);
        etPwd=findViewById(R.id.etPwd);
        btnFB= findViewById(R.id.btnFB);

        findViewById(R.id.tvSignUp).setOnClickListener(this);
        findViewById(R.id.btnLogin).setOnClickListener(this);
        btnFB.setOnClickListener(this);

        initMember();
    }

    private void initMember() {
        permission=new Permission(context);
        customProgressBar=new CustomProgressBar(context);
        utility=new Utility(context);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.tvSignUp:
                Intent i=new Intent(context,RegistrationActivity.class);
                startActivity(i);
                break;

            case R.id.btnLogin:

                Validation validation=new Validation(context);
                if (utility.checkInternetConnection()&&permission.checkLocationPermission()) {
                    if (validation.isEmailValid(etEmail) && validation.isPasswordValid(etPwd)) {
                        //Utility.showToast(context, getString(R.string.underDevelopment), 0);
                        String email = etEmail.getText().toString().trim();
                        String password = etPwd.getText().toString().trim();
                        doLogin(email,password);

                    }
                }else{
                    Utility.showToast(context,getString(R.string.internetConnectivityError),0);
                }
                break;

            case R.id.btnFB:
                if (utility.checkInternetConnection()&&permission.checkLocationPermission()){
                    if (latitude!=0.0d&&longiude!=0.0d) {
                        facebookLoginApi();
                    }/*else if (checkGPS&&latitude==0.0&&longiude==0.0){
                        showErrorPopup();
                    }*/else if (!checkGPS){
                        utility.checkGpsStatus();
                    }else{
                        showErrorPopup();
                    }

                }else{
                    Utility.showToast(context,getString(R.string.internetConnectivityError),0);
                }
                break;
        }
    }

    public void doLogin(final String email, final String password) {

        if (utility.checkInternetConnection()) {

            customProgressBar=new CustomProgressBar(context);
            showProgDialog(false);

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, "http://dev.scenekey.com/event/webservices/login", new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);
                    Log.e("Response", data);

                    try {
                        JSONObject jsonObject = new JSONObject(data);

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equalsIgnoreCase("SUCCESS")) {
                            dismissProgDialog();

                            JSONObject userDetail = jsonObject.getJSONObject("userDetail");
                            UserInfo userInfo = new UserInfo();
                            userInfo.userID = userDetail.getString("userid");
                            userInfo.facebookId = userDetail.getString("userFacebookId");
                            String socialType  = userDetail.getString("socialType");
                            userInfo.userName = userDetail.getString("userName");
                            userInfo.email = userDetail.getString("userEmail");
                            userInfo.fullName = userDetail.getString("fullname");
                            String[] split=userInfo.fullName.split(" ");
                            if (split.length==2){
                                userInfo.firstName= split[0].substring(0, 1).toUpperCase() + split[0].substring(1);
                                userInfo.lastName=split[1].substring(0, 1).toUpperCase() + split[1].substring(1);
                            }else{
                                userInfo.firstName=userInfo.fullName.substring(0, 1).toUpperCase() + userInfo.fullName.substring(1);
                                userInfo.lastName="";
                            }
                            userInfo.password = userDetail.getString("password");
                            userInfo.userImage = userDetail.getString("userImage");
                            String age = userDetail.getString("age");
                            String dob = userDetail.getString("dob");
                            String gender = userDetail.getString("gender");
                            String userDeviceId = userDetail.getString("userDeviceId");
                            String deviceType = userDetail.getString("deviceType");
                            userInfo.userGender = userDetail.getString("userGender");
                            String userStatus = userDetail.getString("userStatus");
                            userInfo.loginTime = userDetail.getString("userLastLogin");
                            String registered_date = userDetail.getString("registered_date");
                            String usertype = userDetail.getString("usertype");
                            String artisttype = userDetail.getString("artisttype");
                            String stagename = userDetail.getString("stagename");
                            userInfo.venuName = userDetail.getString("venuename");
                            userInfo.address = userDetail.getString("address");
                            String fullAddress = userDetail.getString("fullAddress");
                            userInfo.latitude = userDetail.getString("lat");
                            userInfo.longitude = userDetail.getString("longi");
                            if (!(userDetail.getString("adminLat").isEmpty()&&userDetail.getString("adminLat").isEmpty())) {
                                userInfo.latitude = userDetail.getString("adminLat");
                                userInfo.longitude = userDetail.getString("adminLong");
                            }
                            String user_status = userDetail.getString("user_status");
                            userInfo.makeAdmin = userDetail.getString("makeAdmin");
                            userInfo.keyPoints = userDetail.getString("key_points");
                            userInfo.bio= userDetail.getString("bio");

                            sessionManager.createSession(userInfo);

                            Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            dismissProgDialog();
                        }

                    } catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        dismissProgDialog();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    Log.i("Error", networkResponse + "");
                    Toast.makeText(LoginActivity.this, networkResponse + "", Toast.LENGTH_SHORT).show();
                    dismissProgDialog();
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("userEmail", email);
                    params.put("password", password);
                    params.put("userDeviceId", FirebaseInstanceId.getInstance().getToken());
                    params.put("deviceType", "");

                    Utility.e("Login params", params.toString());
                    return params;
                }
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(LoginActivity.this).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(LoginActivity.this, getString(R.string.internetConnectivityError), Toast.LENGTH_SHORT).show();
        }
    }

    private void showErrorPopup() {
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
                final CustomProgressBar dialog = new CustomProgressBar(LoginActivity.this);
                dialog.show();


                new Handler().postDelayed(new Runnable() {
                    // Using handler with postDelayed called runnable run method
                    @Override
                    public void run() {
                        dialog.dismiss();
                        btnFB.callOnClick();
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

    /* facebook api start here */
    private void facebookLoginApi() {
        objFbCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
        LoginManager.getInstance().registerCallback(objFbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                try {
                                    UserInfo userInfo=new UserInfo();
                                    AccessToken token = AccessToken.getCurrentAccessToken();

                                    userInfo.facebookId = object.get("id").toString();
                                    userInfo.userAccessToken= String.valueOf(token);
                                    userInfo.fullName =  object.get("name").toString();
                                    userInfo.userImage =  "https://graph.facebook.com/" + userInfo.facebookId + "/picture?type=large";
                                    userInfo.userGender = "";//object.getString("gender");
                                    if (object.has("email")) {
                                        userInfo.email = object.getString("email");
                                    } else {
                                        userInfo.email =  userInfo.facebookId +".scenekey" + "@fb.com";
                                    }

                                    checkSocialDetail(userInfo);
                                    //registerSocialDetails(userInfo);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Utility.showToast(context, getString(R.string.cancel), 1);
            }

            @Override
            public void onError(FacebookException error) {
                Utility.showToast(context, error.getMessage(), 1);
            }
        });
    }
/* facebook api end here */

    private void checkSocialDetail(final UserInfo userInfo) {
        showProgDialog(false);

        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.CHK_LOGIN, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // get response
                    JSONObject jsonObject;
                    try {
                        customProgressBar.cancel();

                        jsonObject = new JSONObject(response);
                        Utility.e(" login response",response);
                        int statusCode = jsonObject.getInt("success");
                        String message = jsonObject.getString("msg");

                        //registered user
                        if (statusCode == 1) {
                            registerSocialDetails(userInfo);
                        } else if (statusCode == 0) {  //already registered user
                            if (manageSession(jsonObject, userInfo))
                                callIntent(btnFB.getId(),false);
                        } else {
                            Utility.showToast(context, getString(R.string.somethingwentwrong), 0);
                        }

                    } catch (Exception ex) {
                        customProgressBar.cancel();
                        ex.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    customProgressBar.cancel();
                    utility.volleyErrorListner(e);
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("facebook_id", userInfo.facebookId);
                    params.put("Fullname", userInfo.fullName);
                    params.put("device_token", FirebaseInstanceId.getInstance().getToken());
                    params.put("deviceType", "2");
                    params.put("ProfileImage", userInfo.userImage);
                    params.put("gender",userInfo.userGender);
                    params.put("userGender",userInfo.userGender);
                    params.put("latitude", String.valueOf(latitude));
                    params.put("longitude", String.valueOf(longiude));
                    Utility.e("Params",params.toString());

                  /*  params.put("userEmail",     user.getEmail());
                    params.put("facebookid",    user.getFacebookid());
                    params.put("fbusername",    user.getFbusername());
                    params.put("usertype",      user.getUserType());
                    params.put("fullname",      user.getFullName());
                    params.put("device_token",  user.getDevicetoken());
                    params.put("deviceType",    "2");
                    params.put("gender",        user.getGender());
                    params.put("userGender",    user.getGender());
                    params.put("ProfileImage",  user.getProfileImage());
                    params.put("latitude",      getlatlong()[0]+"");
                    params.put("longitude",     getlatlong()[1]+"");*/

                    return params;
                }
            };
            VolleySingleton.getInstance(this.getBaseContext()).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        }else{
            utility.snackBar(etEmail,getString(R.string.internetConnectivityError),0);
            customProgressBar.cancel();
        }

    }

    private void registerSocialDetails(final UserInfo userInfo) {
        showProgDialog(false);

        if (utility.checkInternetConnection()) {
            StringRequest request = new StringRequest(Request.Method.POST, WebServices.LOGIN, new Response.Listener<String>() {
                @Override
                public void onResponse(String Response) {
                    // get response
                    JSONObject jsonObject;
                    try {
                        customProgressBar.cancel();
                        // System.out.println(" login response" + response);
                        jsonObject = new JSONObject(Response);
                        int statusCode = jsonObject.getInt("success");
                        String message = jsonObject.getString("msg");

                        //registered user
                        if (statusCode==1) {
                            manageSession(jsonObject,userInfo);
                            callIntent(btnFB.getId(),true);
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                   try {
                                     showProgDialog(false);
                                       urlToBitmap();
                                   }catch (Exception e){
                                       e.printStackTrace();
                                   }
                                }
                            });

                        }else if (statusCode==0) {
                            Utility.showToast(context,message,0);
                            Utility.e(TAG,message);
                        }else{
                            Utility.showToast(context,getString(R.string.somethingwentwrong),0);
                        }

                    } catch (Exception ex) {
                        customProgressBar.cancel();
                        ex.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    utility.volleyErrorListner(e);
                    customProgressBar.cancel();
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("userEmail",     userInfo.email);
                    params.put("facebookid",    userInfo.facebookId);
                    params.put("fbusername",    userInfo.fullName);
                    params.put("usertype",      "Social User");
                    params.put("fullname",      userInfo.fullName);
                    params.put("device_token",  FirebaseInstanceId.getInstance().getToken());
                    params.put("deviceType",    "2");
                    params.put("gender",        userInfo.userGender);
                    params.put("ProfileImage",  userInfo.userImage);
                    params.put("latitude", String.valueOf(latitude));
                    params.put("longitude", String.valueOf(longiude));
                    return params;
                }
            };
            VolleySingleton.getInstance(this.getBaseContext()).addToRequestQueue(request);
            request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1));
        }else{
            utility.snackBar(etEmail,getString(R.string.internetConnectivityError),0);
            customProgressBar.cancel();
        }
    }

    private void urlToBitmap() {
        final AWSImage awsImage = new AWSImage(this);

        String image = sessionManager.getUserInfo().getUserImage();
        if (image != null && !image.equals("")) {
            Picasso.with(this).load(image).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    dismissProgDialog();
                    try {
                        if (bitmap!=null)
                            awsImage.initItem(bitmap);
                    } catch (Exception e) {
                        // some action
                        e.printStackTrace();
                    }
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    dismissProgDialog();
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    dismissProgDialog();
                }
            });
        }
        dismissProgDialog();
    }

    private void callIntent(int id , boolean isShow) {
        switch ( id){
            case R.id.btnFB:
                if (isShow){
                    Intent intent = new Intent(LoginActivity.this,IntroActivity.class);
                    startActivity(intent);
                }else {

                    Intent i=new Intent(context,HomeActivity.class);
                    i.putExtra(Constant.LATITUDE,latitude+"");
                    i.putExtra(Constant.LONGITUDE,longiude+"");
                    startActivity(i);
                    finish();
                }
                break;

        }
    }

    private boolean manageSession(JSONObject jsonObject, UserInfo uInfo) {
        try {
            JSONObject objUserDetails = jsonObject.getJSONObject("userinfo");
            UserInfo userInfo=new UserInfo();
            userInfo.userID = (objUserDetails.getString("userID"));
            userInfo.email = (objUserDetails.getString("email"));
            userInfo.fullName = (objUserDetails.getString("fullname"));
            userInfo.userName = (objUserDetails.getString("userName"));
            userInfo.userGender = (objUserDetails.getString("userGender"));
            if (objUserDetails.getString("userImage").isEmpty()){
                userInfo.userImage=uInfo.userImage;
            }else {
                userInfo.userImage = (objUserDetails.getString("userImage"));
            }
            userInfo.loginTime = (objUserDetails.getString("logintime"));
            userInfo.stageName = (objUserDetails.getString("stagename"));
            userInfo.venuName = (objUserDetails.getString("venuename"));
            userInfo.artistType = (objUserDetails.getString("artisttype"));
            userInfo.firstName = (objUserDetails.getString("firstname"));
            userInfo.lastName = (objUserDetails.getString("lastname"));
            userInfo.environment = (objUserDetails.getString("environment"));
            userInfo.facebookId = uInfo.facebookId;

            if (objUserDetails.has("lat")) userInfo.latitude = (objUserDetails.getString("lat"));
            if (objUserDetails.has("longi")) userInfo.longitude = (objUserDetails.getString("longi"));
            if (objUserDetails.has("address")) userInfo.address = (objUserDetails.getString("address"));
            if (objUserDetails.has("bio")) userInfo.bio = (objUserDetails.getString("bio"));
            if (objUserDetails.has("keyPoints")) userInfo.keyPoints = (objUserDetails.getString("keyPoints"));
            if(objUserDetails.has("makeAdmin"))userInfo.makeAdmin=(objUserDetails.getString("makeAdmin"));
            userInfo.userAccessToken=uInfo.userAccessToken;
            Utility.e("Auth Token", userInfo.userAccessToken);
            userInfo.firstTimeDemo=(true);

            Utility.e("session data",jsonObject.toString());
            //  Utility.showToast(context, message, 1);
            SceneKey.sessionManager.createSession(userInfo);

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void showProgDialog(boolean b) {
        customProgressBar.setCanceledOnTouchOutside(b);
        customProgressBar.setCancelable(b);
        customProgressBar.show();
    }

    private void dismissProgDialog() {
        if (customProgressBar != null) customProgressBar.dismiss();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            // Logic to handle location object
            latitude=location.getLatitude();
            longiude=location.getLongitude();
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

    private  boolean hasSoftKeys(WindowManager windowManager){
        boolean hasSoftwareKeys = true;


        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
            Display d = windowManager.getDefaultDisplay();

            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);

            int realHeight = realDisplayMetrics.heightPixels;
            int realWidth = realDisplayMetrics.widthPixels;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);

            int displayHeight = displayMetrics.heightPixels;
            int displayWidth = displayMetrics.widthPixels;

            hasSoftwareKeys =  (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
        }else{
            boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            hasSoftwareKeys = !hasMenuKey && !hasBackKey;
        }
        return hasSoftwareKeys;
    }

    /* on activity result start */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        objFbCallbackManager.onActivityResult(requestCode, resultCode, data);

    }
 /* on activity result end*/

    // Calling override method.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean locationPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean externalStoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    initLocation();
                    if (cameraPermission && locationPermission && externalStoragePermission) {

                        Toast.makeText(context, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
}
