package com.scenekey.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.scenekey.R;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by mindiii on 1/2/18.
 */

public class Utility {

    private Context context;

    public Utility(Context context) {
        this.context = context;
    }

    public static void showToast(Context context, String message, int len) {
        Toast.makeText(context, message, len).show();
    }

    public static void e(String tag , String msg){
        Log.e(tag,msg);
    }

    public static void printBigLogcat(String TAG, String response) {
        int maxLogSize = 1000;
        for (int i = 0; i <= response.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > response.length() ? response.length() : end;
            Log.e(TAG, response.substring(start, end));
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        //activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public boolean checkNetworkProvider(){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // get network provider status

        return locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean checkGPSProvider(){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // get network provider status

        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void snackBar(View view, String message, int len){
        Snackbar snackbar= Snackbar.make(view,message,len);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(context.getResources().getColor(R.color.old_primary));
        snackbar.show();
    }

    public void checkGpsStatus() {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert manager != null;
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            final Dialog dialog = new Dialog(context);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.custom_popup_with_btn);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            //      deleteDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //style id

            TextView tvCancel, tvPopupOk,tvTitle,tvMessages;

            tvTitle = dialog.findViewById(R.id.tvTitle);
            tvMessages = dialog.findViewById(R.id.tvMessages);
            tvCancel = dialog.findViewById(R.id.tvPopupCancel);
            tvPopupOk = dialog.findViewById(R.id.tvPopupOk);
            tvPopupOk.setText(R.string.ok);
            tvTitle.setText(R.string.locationServicesNotActive);
            tvMessages.setText(R.string.high_accuracy);

            tvPopupOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Show location settings when the user acknowledges the alert dialog
                    dialog.cancel();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
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
    }

    public void showCustomPopup(String message,String fontType){
        final Dialog dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.custom_popup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //      deleteDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //style id

        TextView  tvPopupOk,tvMessages;

        tvMessages = dialog.findViewById(R.id.custom_popup_tvMessage);
        Typeface typeface=Typeface.create(fontType,Typeface.BOLD);
        tvMessages.setTypeface(typeface);
        tvPopupOk = dialog.findViewById(R.id.custom_popup_ok);
        tvPopupOk.setText(R.string.ok);
        tvMessages.setText(message);

        tvPopupOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show location settings when the user acknowledges the alert dialog
                dialog.cancel();

            }
        });

        dialog.show();
    }

    public boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /*volleyErrorListner start*/
    public void volleyErrorListner(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage;

        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout";
                Utility.showToast(context, errorMessage, 0);

            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server";
                Utility.showToast(context, errorMessage, 0);
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = new JSONObject(result);
                // String status = response.getString("status");
                String status = response.getString("responseCode");
                String message = response.getString("message");

                Log.e("Error Status", "" + status);
                Log.e("Error Message", message);
                errorMessage = message;

                if(error.networkResponse.statusCode==400 && new JSONObject(new String(error.networkResponse.data)).getString("message").equals("Invalid Auth Token")){
                    // Build the alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Session Expired");
                    builder.setMessage("Your session is expired please login again");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SceneKey.sessionManager.logout(((Activity) context));
                            // ((Activity) context).finishAffinity();
                        }
                    });
                    Dialog alertDialog = builder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }

                if (status.equals("300")) {
                    // Build the alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Session Expired");
                    builder.setMessage("Your session is expired please login again");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SceneKey.sessionManager.logout(((Activity) context));
                           // ((Activity) context).finishAffinity();
                        }
                    });
                    Dialog alertDialog = builder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                } else if (!(errorMessage.equals("Invalid Auth Token"))) {
                    Utility.showToast(context, errorMessage, 0);
                }
                /*  if (networkResponse.statusCode == 300) {
                    errorMessage = message + "Please login again";
                }
               else if (networkResponse.statusCode == 404) {
                    errorMessage = "Resource not found";
                } else if (networkResponse.statusCode == 401) {
                    errorMessage = message + " Please login again";
                } else if (networkResponse.statusCode == 400) {
                    errorMessage = message + " Check your inputs";
                } else if (networkResponse.statusCode == 500) {
                    errorMessage = message + "Ooops! Something went wrong,";
                }  */

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
      /*volleyErrorListner end*/
}
