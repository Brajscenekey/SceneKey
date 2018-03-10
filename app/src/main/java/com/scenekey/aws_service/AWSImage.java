package com.scenekey.aws_service;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.scenekey.activity.HomeActivity;
import com.scenekey.helper.Constant;
import com.scenekey.helper.WebServices;
import com.scenekey.model.ImagesUpload;
import com.scenekey.model.UserInfo;
import com.scenekey.util.SceneKey;
import com.scenekey.util.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Created by mindiii on 28/2/18.
 */

public class AWSImage {

    public ArrayList<ImagesUpload> imageList;
    private String TAG = "AWSImage";
    private Activity activity;
    private CognitoCredentialsProvider credentialsProvider;
    private String imageKey;

    public AWSImage(Activity activity) {
        this.activity = activity;
        if (imageList == null) imageList = new ArrayList<>();
    }

    public void initItem(Bitmap bitmap){
        try {

            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            File myDir = new File(root + "/saved_images");
            myDir.mkdirs();

            String fname = "Image-"+ UUID.randomUUID()+".jpg";
            File file = new File (myDir, fname);
            if (file.exists ()) file.delete ();
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                credentialsProvider = getCredentials();
                uploadImage(file,credentialsProvider);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            // some action
            e.printStackTrace();
        }

    }

    private void uploadImage(File myPath, CognitoCredentialsProvider credentialsProvider){
        // prog.show();
        AmazonS3Client s3Client;
        s3Client = new AmazonS3Client(credentialsProvider);
        String fbid = SceneKey.sessionManager.getFacebookId();
        Utility.e("FBID",fbid);

        // Set the region of your S3 bucket
        s3Client.setRegion(Region.getRegion(Regions.US_WEST_1));
        TransferUtility transferUtility = new TransferUtility(s3Client, activity);
        // String  key1 = fbid+"-"+ System.currentTimeMillis()+".jpg";

        final String  key1 = fbid+".jpg";
        TransferObserver observer
                = transferUtility.upload(
                Constant.BUCKET+"/"+fbid,     /* The bucket to upload to */
                key1,    /* The key for the uploaded object */
                myPath   , CannedAccessControlList.PublicReadWrite  /* The file where the data to upload exists */
        );
        Utility.e("OBSERVER KEY",observer.getKey());
        imageKey = fbid + "/" + observer.getKey();
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {

                if(state.equals(TransferState.COMPLETED)){
                    // Constant.DEF_PROFILE= WebServices.USER_IMAGE+imageKey;
                    Utility.e("Image upload","State Change" + state);
                    setDefaultImageOnServer(WebServices.USER_IMAGE + imageKey, imageKey);
                }
                if(state.equals(TransferState.FAILED)){
                /*Toast.makeText(Image_uploade_Activity.this, "State Change" + state,
                        Toast.LENGTH_SHORT).show();*/
                Utility.e("Image upload","State Change" + state);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
          /*  int percentage = (int) (bytesCurrent/(bytesTotal>0?bytesTotal:1) * 100);
            Toast.makeText(getApplicationContext(), "Progress in %" + percentage,
                    Toast.LENGTH_SHORT).show();*/
                Log.e("value","Progresschange");
            }

            @Override
            public void onError(int id, Exception ex) {
                //prog.dismiss();
                Utility.e("error","error"+ex);
            }
        });
    }

    public CognitoCredentialsProvider getCredentials() {
        CognitoCredentialsProvider credentialsProvider = new CognitoCredentialsProvider( "us-west-2:86b58a3e-0dbd-4aad-a4eb-e82b1a4ebd91",Regions.US_WEST_2);
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3, activity);

        Map<String, String> logins = new HashMap<String, String>();

        String token = "";
        try {
            token = AccessToken.getCurrentAccessToken().getToken();
        }catch (Exception e){
            e.printStackTrace();
        }

        if (token != null && !token.equals("")) {
            logins.put("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
        }else {
            logins.put("graph.facebook.com", Constant.Token);
        }
        //Utility.printBigLogcat("Acess " , AccessToken.getCurrentAccessToken().getToken());
        credentialsProvider.setLogins(logins);
        return credentialsProvider;
    }

    private void setDefaultImageOnServer(final String key, final String s) {

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(activity);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("method","PUT");
            jsonBody.put("action","updateImage");
            jsonBody.put("userid", SceneKey.sessionManager.getUserInfo().userID);
            jsonBody.put("userImage",s);

            final String mRequestBody = jsonBody.toString();
            Utility.e("RequestBody"  , mRequestBody);

            StringRequest stringRequest = new StringRequest(Request.Method.PUT, WebServices.DEFAULT_IMAGE, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Utility.e("server image set", response);
                    UserInfo userInfo = SceneKey.sessionManager.getUserInfo();
                    userInfo.userImage=key;
                    // Constant.DEF_PROFILE=key;
                    SceneKey.sessionManager.createSession(userInfo);
                 //   dismissProgDialog();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Utility.e("LOG_VOLLEY E", error.toString());
                   // dismissProgDialog();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes();
                    } catch (Exception uee) {
                        //VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {

                        responseString = new String(response.data);
                        //Util.printLog("RESPONSE", responseString.toString());
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };
            stringRequest.setShouldCache(false);
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,1,0));
            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
           // dismissProgDialog();
        }
    }

    public void downloadFileFromS3(final String fbId, CognitoCredentialsProvider credentialsProvider) {//, CognitoCachingCredentialsProvider credentialsProvider){

        try {
            final AmazonS3Client s3Client;
            s3Client = new AmazonS3Client(credentialsProvider);

            // Set the region of your S3 bucket
            s3Client.setRegion(Region.getRegion(Regions.US_WEST_1));
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ObjectListing listing = s3Client.listObjects("scenekey-profile-images", fbId);
                        List<S3ObjectSummary> summaries = listing.getObjectSummaries();

                        imageList.clear();

                        while (listing.isTruncated()) {

                            listing = s3Client.listNextBatchOfObjects(listing);
                            summaries.addAll(listing.getObjectSummaries());

                        }

                        updateImages(summaries);

                        Utility.e(TAG, "listing " + summaries.get(0).getKey() + "no of image " + summaries.size());

                    } catch (Exception e) {
                        e.printStackTrace();
                        ((HomeActivity) activity).dismissProgDialog();
                        Utility.e(TAG, "Exception found while listing " + e);
                    }
                }
            });

            thread.start();

        } catch (Exception e) {
            Utility.e("AMAZON", e.toString());
            ((HomeActivity) activity).dismissProgDialog();
        }
    }

    private void updateImages(final List<S3ObjectSummary> summaries) {
        if (imageList == null) imageList = new ArrayList<>();
        for (S3ObjectSummary obj : summaries) {
            imageList.add(new ImagesUpload(obj.getKey()));
        }
    }

    public String getFacebookId(String userFacebookId, String userid) {
        String s = userFacebookId;
        if (s.equals("")) {
            s = userid;
        }
        return s;
    }
}
