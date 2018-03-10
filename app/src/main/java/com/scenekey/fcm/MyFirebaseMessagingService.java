package com.scenekey.fcm;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.scenekey.R;
import com.scenekey.activity.HomeActivity;
import com.scenekey.util.Utility;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    final String title = "Notification!";
    boolean toActivity ;
    private Notification notification;

    //11-01 15:58:49.211 17392-24749/com.scenekey E/MyFirebaseMsgService: From: 1034290158398{title={"notficationType":1}, message=You have been selected as an admin, notificationType=[]}
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Utility.e(TAG, "Message Notification Body: " + remoteMessage.getData().toString());
//{title=, message=Donie Darko has commented on mindiii nights, notificationType=[]}
        //11-02 12:34:59.837 1474-2269/com.scenekey E/MyFirebaseMsgService: From: 1034290158398{title=, message=Arvind Patidar has commented on Paige Clem, notificationType=[]}
        //11-02 12:35:11.793 1474-2288/com.scenekey E/MyFirebaseMsgService: From: 1034290158398{title=, message=Arvind Patidar has commented on Paige Clem, notificationType=[]}
        String notificationMsg = remoteMessage.getData().get("message");

        sendNotification(remoteMessage.getTtl(), title, notificationMsg, false);



      /*  if(remoteMessage.getData().containsKey("message")){
            String notificationMsg =  remoteMessage.getData().get("message").toString();
            String titleMsg =  remoteMessage.getData().get("title").toString();
            System.out.println(TAG+"  notification Msg --------------"+notificationMsg+" Title "+titleMsg+" id"+remoteMessage.getTtl());
            Intent intent = new Intent(this, HomeActivity.class);;
            try {
                JSONObject obj      = new JSONObject(titleMsg);
                String  Type        = obj.getString("notficationType");
                toActivity  = true;
                switch(Type){

                    case "1":

                        USER_SEND_INVITATION +=1;
                        intent.putExtra(Constants.NOTIFICATION , Str.PREF_TYPE_USER_SEND);
                        break;

                    case "2":

                        CREATE_SEND_REQUEST +=1;
                        intent.putExtra(Constants.NOTIFICATION ,Str.PREF_TYPE_MYREQUEST);
                        break;

                    case "3":

                        NOTIFICATION_ACCEPT +=1;
                        intent.putExtra(Constants.NOTIFICATION ,Str.PREF_TYPE_ACCEPTED_JOIN);

                        break;

                    case "4":

                        if(notificationMsg.contains("accepted")){
                            FRIEND_REQUEST_TYPE_ACCEPT  +=1;
                        }
                        else FRIEND_REQUEST_TYPE_SENT   +=1;
                        FRIEND_REQUEST_ACCEPT +=1;
                        intent.putExtra(Constants.NOTIFICATION ,Str.PREF_TYPE_FRIEND);
                        toActivity = true;
                        break;
                    case "5":

                        if(notificationMsg.contains("has been drafted to")){
                            ACCEPT_TEAM_REQ  +=1;
                            intent.putExtra(Constants.NOTIFICATION ,Str.PREF_TYPE_TEAM_LIST_ACT);
                            String teamId =  obj.getString("teamId");
                            intent.putExtra(Str.TEAM_ID ,teamId);
                        }
                        else {
                            NOTFICATION_TEAM_REQ   +=1;
                            intent.putExtra(Constants.NOTIFICATION ,Str.PREF_TYPE_TEAM_REQUEST);
                        }

                        toActivity = true;
                        break;

                }

                boolean foregroud = isAppOnForeground(getApplicationContext());
               try{
                       HomeActivity.activityTest.showNotification(getApplicationContext());

                }catch (Exception e){
                   *//**//*// Util.printLog("ErrorFIREBASE",e+"");
               }
                *//*intent.setAction("com.benchbuddy.onMessageReceived");
                sendBroadcast(intent);*//*



            } catch (JSONException e) {
                e.printStackTrace();
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //intent.putExtra("notification", notification);
           *//* try{
                HomeActivity.updateBottomBar();
            }catch (Exception e){
                 Util.printLog("FireBase_HOMEBADGE","Error:"+e+"");
                e.printStackTrace();
            }*//*
            *//*try {
                ProfileFragment.updateBadge();
            } catch (Exception e) {
                e.printStackTrace();
            }*//*
            //setCountToSharredPrefrence();
            sendNotification(remoteMessage.getTtl(), title, notificationMsg, intent ,toActivity);

        }*/
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts

    private void sendNotification(int id, String title, String messageBody, boolean send) {
        Intent intent = new Intent(this, HomeActivity.class);
/*
        intent.putExtra("reference_id", reference_id);
        intent.putExtra("title", title);
        intent.putExtra("body", body);*/

        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Random r = new Random();
        int random = r.nextInt(899 - 65) + 65;
        int m = ((int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE))+random;


        PendingIntent pendingIntent = PendingIntent.getActivity(this, m, intent, PendingIntent.FLAG_ONE_SHOT);
        //if( intent == null ) pendingIntent = null;
        NotificationCompat.Builder notificationBuilder;
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if( !send ){
            notificationBuilder = new NotificationCompat.Builder(this)
                    //.setSmallIcon(R.drawable.icon_app_192_white)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(title)
                    .setContentText(messageBody)
                   /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.scenekey_app_ico))*/
                    .setSmallIcon(R.drawable.scenekey_app_ico)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);;
        }
        else {
            notificationBuilder = new NotificationCompat.Builder(this)
                    // .setSmallIcon(R.drawable.icon_app_192_white)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(title)
                    .setContentText(messageBody)
                   /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.drawable.scenekey_app_ico))*/
                    .setSmallIcon(R.drawable.scenekey_app_ico)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        assert notificationManager != null;
        notificationManager.notify(m, notificationBuilder.build());
   //comment for data not available
          /*  try {
            if(HomeActivity.instance != null){
                if( HomeActivity.instance.getCurrentFragment() instanceof Event_Fragment) {
                    ((Event_Fragment) HomeActivity.instance.getCurrentFragment()).getAlldataAvailable();
                }
                if( HomeActivity.instance.getCurrentFragment() instanceof Key_In_Event_Fragment) {
                    ((Key_In_Event_Fragment) HomeActivity.instance.getCurrentFragment()).getAlldataAvailable();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/
    }




    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }




}
