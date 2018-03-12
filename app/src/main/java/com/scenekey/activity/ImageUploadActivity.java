package com.scenekey.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
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
import com.amazonaws.services.s3.model.DeleteObjectRequest;
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
import com.scenekey.R;
import com.scenekey.adapter.ImageUpload_Adapter;
import com.scenekey.helper.Constant;
import com.scenekey.helper.CustomProgressBar;
import com.scenekey.helper.Pop_Up_Option;
import com.scenekey.helper.WebServices;
import com.scenekey.model.ImagesUpload;
import com.scenekey.model.UserInfo;
import com.scenekey.util.ImageUtil;
import com.scenekey.util.SceneKey;
import com.scenekey.util.StatusBarUtil;
import com.scenekey.util.Utility;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ImageUploadActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = ImageUploadActivity.class.toString();
    public boolean isChanged = false;
    private Context context=this;
    private ImageUpload_Adapter adapter;
    private ImageView img_profile,img_f1_back;
    private CognitoCredentialsProvider credentialsProvider;
    private int value = 0;
    private String key,from="";
    private CustomProgressBar prog;
    private Bitmap bitmap;
    private  Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // StatusBarUtil.setTranslucent(this);
        setContentView(R.layout.activity_image_upload);
        setStatusBar();
        utility=new Utility(context);
        if (getIntent().getStringExtra("from") != null) {
            //for intent data
            from = getIntent().getStringExtra("from");
        }

        RecyclerView recyclerView = findViewById(R.id.recyvlerview);
        img_profile =  findViewById(R.id.img_profile);
        img_f1_back =  findViewById(R.id.img_f1_back);

        TextView tv_done = findViewById(R.id.tv_done);
        tv_done.setOnClickListener(this);
        img_f1_back.setOnClickListener(this);

        if (Constant.DONE_BUTTON_CHECK==1){
            tv_done.setVisibility(View.GONE);
            img_f1_back.setVisibility(View.VISIBLE);
        }

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        adapter = new ImageUpload_Adapter(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        prog = new CustomProgressBar(this);
        showProgDialog(false);


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int ActivityWidth , ActivityHeight;
        ActivityWidth = width;
        ActivityHeight = height;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) img_profile.getLayoutParams();
        params.height = (ActivityWidth * 3) / 4;

        credentialsProvider=  this.getCredentials();

        downloadFileFromS3((credentialsProvider==null?credentialsProvider = this.getCredentials():credentialsProvider));
        img_profile.setLayoutParams(params);
        img_profile.setImageResource(R.drawable.image_defult_profile);
    }

    private void setStatusBar() {
        View top_status = findViewById(R.id.top_status);

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
                top_status.setVisibility(View.VISIBLE);
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
    }

    private void fbUploadImagesStart(){
        String image = SceneKey.sessionManager.getUserInfo().getUserImage();
        if (image != null && !image.equals("")) {
            Picasso.with(this).load(image).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    try {

                        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                        File myDir = new File(root + "/saved_images");
                        myDir.mkdirs();

                        String fname = "Image-"+UUID.randomUUID()+".jpg";
                        File file = new File (myDir, fname);
                        if (file.exists ()) file.delete ();
                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.flush();
                            out.close();
                            uploadFBImage(file,credentialsProvider);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        // some action
                        e.printStackTrace();
                    }
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            });
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

    private void uploadFBImage(File myPath,CognitoCredentialsProvider credentialsProvider){
        prog.show();
        AmazonS3Client s3Client;
        credentialsProvider = getCredentials();
        s3Client = new AmazonS3Client(credentialsProvider);
        String fbId = SceneKey.sessionManager.getFacebookId();
        Utility.e("FBID",fbId);

        // Set the region of your S3 bucket
        s3Client.setRegion(Region.getRegion(Regions.US_WEST_1));
        TransferUtility transferUtility = new TransferUtility(s3Client, this);
        // String  key1 = fbid+"-"+ System.currentTimeMillis()+".jpg";

        final String  key1 = fbId+".jpg";
        TransferObserver observer
                = transferUtility.upload(
                Constant.BUCKET+"/"+fbId,     /* The bucket to upload to */
                key1,    /* The key for the uploaded object */
                myPath   , CannedAccessControlList.PublicReadWrite  /* The file where the data to upload exists */
        );
        Utility.e("OBSERVER KEY",observer.getKey());
        key = fbId+"/"+observer.getKey();
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {

                if(state.equals(TransferState.COMPLETED)){
                    adapter.addImage(key,bitmap);
                    adapter.notifyDataSetChanged();
                    isChanged=true;
                    // Constant.DEF_PROFILE= WebServices.USER_IMAGE+key;
                    dismissProgDialog();
                }
                if(state.equals(TransferState.FAILED)){
                    /*Toast.makeText(Image_uploade_Activity.this, "State Change" + state,
                            Toast.LENGTH_SHORT).show();*/
                    dismissProgDialog();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
              /*  int percentage = (int) (bytesCurrent/(bytesTotal>0?bytesTotal:1) * 100);
                Toast.makeText(getApplicationContext(), "Progress in %" + percentage,
                        Toast.LENGTH_SHORT).show();*/
                Log.v("value","Progresschange");
            }

            @Override
            public void onError(int id, Exception ex) {
                dismissProgDialog();
                Utility.e("error","error"+ex);
            }
        });
    }

    private CognitoCredentialsProvider getCredentials(){
        CognitoCredentialsProvider credentialsProvider = new CognitoCredentialsProvider( "us-west-2:86b58a3e-0dbd-4aad-a4eb-e82b1a4ebd91", Regions.US_WEST_2);
        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

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

                        if (summaries.size() > 0) {
                            updateImages(summaries);
                            //setImage(summaries.get(1).getKey());
                        }


                        if(summaries.size() == 0){
                            fbUploadImagesStart();
                        }

                        value+=summaries.size()-1;
                        dismissProgDialog();
                        // Utility.e(TAG, "listing "+ summaries.get(0).getKey());

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Utility.e(TAG, "Exception found while listing "+ e);
                        dismissProgDialog();
                    }
                }
            });
            thread.start();
        }
        catch (Exception e){
            Utility.e("AMAZON",e.toString());
            prog.dismiss();
        }
    }

    private void updateImages(final List<S3ObjectSummary> summaries){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isFirst = true;
                for(S3ObjectSummary obj :summaries ){

                    String userImage = SceneKey.sessionManager.getUserInfo().getUserImage();
                    if (userImage != null && !userImage.equals("") && !userImage.equals("https://s3-us-west-1.amazonaws.com/scenekey-profile-images/")) {
                        Picasso.with(ImageUploadActivity.this).load(userImage).fit().into(img_profile);
                    }else {
                        if(isFirst){
                            setImage(obj.getKey());
                            isFirst= false;
                        }
                    }

                    adapter.addImage(obj.getKey());
                }
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.tv_done:
                if (from.equalsIgnoreCase("profile")){
                    //call detail activity
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("isResult",isChanged);
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }else
                    startActivity(new Intent(context,BioActivity.class));
                break;

            case R.id.img_f1_back:
                onBackPressed();
                break;
        }
    }

    private void upload(CognitoCredentialsProvider credentialsProvider, File file){
        showProgDialog(false);
        AmazonS3Client s3Client;
        s3Client = new AmazonS3Client(credentialsProvider);
        String fbid = SceneKey.sessionManager.getFacebookId();
        Utility.e("FBID",fbid);

        // Set the region of your S3 bucket
        s3Client.setRegion(Region.getRegion(Regions.US_WEST_1));
        TransferUtility transferUtility = new TransferUtility(s3Client, this);
        //  String  key1 = fbid+"-"+ System.currentTimeMillis()+".jpg";
        UUID uuid = UUID.randomUUID();
        String  key1 = uuid+".jpg";
        TransferObserver observer
                = transferUtility.upload(
                Constant.BUCKET+"/"+fbid,     /* The bucket to upload to */
                key1,    /* The key for the uploaded object */
                file   , CannedAccessControlList.PublicRead     /* The file where the data to upload exists */
        );
        Utility.e("OBSERVER KEY",observer.getKey());
        key = fbid+"/"+observer.getKey();
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {

                if(state.equals(TransferState.COMPLETED)){
                    adapter.addImage(key,bitmap);
                    adapter.notifyDataSetChanged();
                    dismissProgDialog();
                    isChanged=true;
                    //success uploaded
                    utility.showCustomPopup(context.getString(R.string.success_uploaded), String.valueOf(R.font.montserrat_medium));
                }
                if(state.equals(TransferState.FAILED)){
                    /*Toast.makeText(Image_uploade_Activity.this, "State Change" + state,
                            Toast.LENGTH_SHORT).show();*/
                    dismissProgDialog();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
              /*  int percentage = (int) (bytesCurrent/(bytesTotal>0?bytesTotal:1) * 100);
                Toast.makeText(getApplicationContext(), "Progress in %" + percentage,
                        Toast.LENGTH_SHORT).show();*/
                Log.v("value","Progresschange");
            }



            @Override
            public void onError(int id, Exception ex) {
                prog.dismiss();
                Utility.e("error","error"+ex);
            }
        });
    }

    /* adapter public method start here */

    public Pop_Up_Option initializePopup(){
        return new Pop_Up_Option(ImageUploadActivity.this) {
            @Override
            public void onGalleryClick(Pop_Up_Option dialog, Object object) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        callIntent(Constant.MY_PERMISSIONS_REQUEST_EXTERNAL);
                    } else {
                        callIntent(Constant.RESULT_LOAD);
                    }
                } else {
                    callIntent(Constant.RESULT_LOAD);
                }
                dialog.dismiss();
            }

            @Override
            public void onCameraClick(Pop_Up_Option dialog, Object object) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (getContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        callIntent(Constant.REQUEST_CAMERA);
                    } else {
                        callIntent(Constant.INTENT_CAMERA);
                    }
                } else {
                    callIntent(Constant.INTENT_CAMERA);
                }
                dialog.dismiss();
            }
        };
    }

    public void callIntent(int caseId){

        switch (caseId){
            case Constant.INTENT_CAMERA:
                try{Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, Constant.REQUEST_CAMERA);}
                catch ( Exception e){
e.printStackTrace();
                }
                break;
            case Constant.RESULT_LOAD:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoPickerIntent, Constant.RESULT_LOAD);
                break;
            case Constant.REQUEST_CAMERA:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                            Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                }
                break;
            case Constant.MY_PERMISSIONS_REQUEST_EXTERNAL:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions( new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            Constant.MY_PERMISSIONS_REQUEST_EXTERNAL);
                }
                break;
            /*case Constants.INTENTREQUESTWRITE:
                break;*/

        }
    }

    public void setImage(String s){
        Picasso.with(this).load(WebServices.USER_IMAGE+s).fit().into(img_profile);
        if (SceneKey.sessionManager.getUserInfo().getUserImage().equalsIgnoreCase(WebServices.USER_IMAGE+s)){
            adapter.showDefaultDialog(getString(R.string.default_profile_title),getString(R.string.default_profile_msg));
        }else{
            isChanged=true;
            showProgDialog(false);
            setDefaultImageOnServer(WebServices.USER_IMAGE+s,s);
        }

    }

    public void removeImage(final int position) {
        showProgDialog(false);
        final ImagesUpload obj = adapter.getList().get(position);
        if(obj.getPath()!=null || !obj.getPath().isEmpty()){

            new AsyncTask<Integer, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Integer... params) {
                    AmazonS3Client s3Client = new AmazonS3Client(getCredentials());
                    try {
                        s3Client.deleteObject(new DeleteObjectRequest(Constant.BUCKET, obj.getKey()));

                    } catch (AmazonServiceException ase) {
                        System.out.print("Caught an AmazonServiceException.");
                        System.out.print("Error Message:    " + ase.getMessage());
                        Utility.showToast(context,"Error Message:    " + ase.getMessage(),0);
                        System.out.print("HTTP Status Code: " + ase.getStatusCode());
                        System.out.print("AWS Error Code:   " + ase.getErrorCode());
                        System.out.print("Error Type:       " + ase.getErrorType());
                        System.out.print("Request ID:       " + ase.getRequestId());
                        prog.dismiss();
                        return false;


                    } catch (AmazonClientException ace) {
                        System.out.print("Caught an AmazonClientException.");
                        Utility.showToast(context,"Error Message: " + ace.getMessage(),0);
                        prog.dismiss();
                        return false;


                    }
                    return true;
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    if(aBoolean){
                        adapter.getList().remove(position);
                        adapter.notifyDataSetChanged();
                        dismissProgDialog();
                        isChanged=true;
                        utility.showCustomPopup(context.getString(R.string.success_deleted), String.valueOf(R.font.montserrat_medium));
                    }
                    else Utility.showToast(context,"Something went wrong",0);
                }
            }.execute(position);
        }
        else{
            adapter.getList().remove(position);
            adapter.notifyDataSetChanged();
            dismissProgDialog();
        }
    }
    /* adapter public method end here */

    @Override
    public void onBackPressed() {

        Constant.DONE_BUTTON_CHECK = 0;
        if (from.equalsIgnoreCase("profile")){
            //call detail activity
            Intent returnIntent = new Intent();
            returnIntent.putExtra("isResult",isChanged);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }else
            startActivity(new Intent(context,BioActivity.class));

        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.RESULT_LOAD ) {
            if(data==null) return;
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            // Get the cursor
            Cursor cursor = this.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String img_Decodable_Str = cursor.getString(columnIndex);
            bitmap = BitmapFactory
                    .decodeFile(img_Decodable_Str);
            //String path = ImageUtil.saveToInternalStorage(bitmap,getContext());

            try {
                bitmap = ImageUtil.modifyOrientation(bitmap,ImageUtil.getRealPathFromUri(this,selectedImage));
                upload((credentialsProvider==null?credentialsProvider = this.getCredentials():credentialsProvider),ImageUtil.saveToInternalfile(bitmap,this));
            }catch (IOException e){
                e.printStackTrace();
            }
            //adpter.addImage(bitmap);
            cursor.close();
        }
        if (requestCode == Constant.REQUEST_CAMERA ) {
            try{
                if(data==null) return;
                bitmap = (Bitmap) data.getExtras().get("data");
                File file = ImageUtil.saveToInternalfile(bitmap,this);
                upload((credentialsProvider==null?credentialsProvider = this.getCredentials():credentialsProvider),file);
            }catch (Exception e){
                e.printStackTrace();
            }
            /*try {
                 Util.printLog("path",file.getPath()+"\n"+file.getCanonicalPath());

                /data/user/0/com.scenekey/app_imageDir/profile.jpg
                        /data/data/com.scenekey/app_imageDir/profile.jpg
            } catch (IOException e) {
                e.printStackTrace();
            }*/

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_EXTERNAL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callIntent(Constant.RESULT_LOAD);
                } else {
                    Toast.makeText(this, "You denied permission , can't select image", Toast.LENGTH_LONG).show();
                }
            }
            break;

            case Constant.MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callIntent(Constant.INTENT_CAMERA);
                } else {
                    Toast.makeText(this, "permission denied by user ", Toast.LENGTH_LONG).show();
                }
                break;

            /*case Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callIntent(Constants.INTENTGALLERY);
                } else {
                    Toast.makeText(getContext(), "Permission not granted for Read", Toast.LENGTH_LONG).show();
                }
                break;*/

        }
    }

    private void setDefaultImageOnServer(final String key, String s){

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(context);

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
                    dismissProgDialog();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Utility.e("LOG_VOLLEY E", error.toString());
                    dismissProgDialog();
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
            dismissProgDialog();
        }
    }
}
