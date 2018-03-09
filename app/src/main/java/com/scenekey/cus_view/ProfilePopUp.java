package com.scenekey.cus_view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scenekey.R;
import com.scenekey.activity.HomeActivity;
import com.scenekey.adapter.DataAdapter;
import com.scenekey.adapter.EmojiAdapter;
import com.scenekey.helper.Constant;
import com.scenekey.model.EventAttendy;
import com.scenekey.util.CircleTransform;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by mindiii on 11/2/17.
 */

public abstract class ProfilePopUp extends Dialog implements View.OnClickListener, DialogInterface.OnShowListener {

    private static final int maxsize = 28; //Maximum size of recent grid view
    public ArrayList<String> list;
    private HomeActivity activity;
    private Context context;
    private RecyclerView rclv_emoji;
    private ImageView lastSelected, iv_indicator, profileimg;
    private  TextView txt_send;
    private LinearLayout lr_send_nudge,lr_indicator;
    private int maxNudes,lastFillposition;
    private SharedPreferences preferences ;
    private String[] recent ;
    private int currentImage;
    private DataAdapter dataAdapter;
    private boolean isLastFilled,isClicked;

    protected ProfilePopUp(@NonNull Activity activity, int maxNudes, EventAttendy obj, final DataAdapter dataAdapter) {
        super(activity, android.R.style.Theme_Translucent);

        this.activity= (HomeActivity) activity;
        this.context=activity;
        this.dataAdapter = dataAdapter;

        View pop_up_view = LayoutInflater.from(context).inflate(R.layout.popup_nudge_n_notificaiton, null);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(pop_up_view);
        this.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        this.setCanceledOnTouchOutside(false);

        rclv_emoji =  pop_up_view.findViewById(R.id.rclv_emoji);
        lr_send_nudge =  pop_up_view.findViewById(R.id.lr_send_nudge);
        lr_indicator =  pop_up_view.findViewById(R.id.lr_indicator);
        ImageView iv_delete = pop_up_view.findViewById(R.id.iv_delete);
        LinearLayout llMain =  pop_up_view.findViewById(R.id.llMain);
        TextView tv_bio =  pop_up_view.findViewById(R.id.tv_my_bio);
        final ImageView img_left = pop_up_view.findViewById(R.id.img_left);
        final ImageView img_right = pop_up_view.findViewById(R.id.img_right);
        iv_indicator =  pop_up_view.findViewById(R.id.iv_indicator);
        ImageView img_cross = pop_up_view.findViewById(R.id.img_cross);
        profileimg = pop_up_view.findViewById(R.id.img_profile_pic2);
        ImageView zero = pop_up_view.findViewById(R.id.zero);
        ImageView one = pop_up_view.findViewById(R.id.one);
        ImageView two = pop_up_view.findViewById(R.id.two);
        ImageView three = pop_up_view.findViewById(R.id.three);
        ImageView four = pop_up_view.findViewById(R.id.four);
        ImageView five = pop_up_view.findViewById(R.id.five);
        txt_send = pop_up_view.findViewById(R.id.txt_send);

        tv_bio.setText(obj.bio);
        GridLayoutManager layoutManager = new GridLayoutManager(context,4, LinearLayoutManager.HORIZONTAL,false);

        rclv_emoji.setLayoutManager(layoutManager);

        rclv_emoji.setAdapter(new EmojiAdapter(context,this));

        this.maxNudes = maxNudes;
        list = new ArrayList<>();
        setClicks(llMain, iv_delete, img_cross, one, two, three, four, five, zero, txt_send, img_left, img_right);
        this.context = context;
        this.setOnShowListener(this);
        getRecentTask();
        ((TextView) pop_up_view.findViewById(R.id.tv_userName)).setText(obj.username);
        try {

            Picasso.with(context).load(obj.getUserimage()).transform(new CircleTransform()).into(profileimg);
        }catch (Exception e){
            e.printStackTrace();
        }
        updateImageView(one);
        txt_send.setText("Nudge "+obj.username);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dataAdapter.imageList.size() > 0) {
                    img_left.setVisibility(View.VISIBLE);
                    img_right.setVisibility(View.VISIBLE);
                }
            }
        }, 2000);
    }

    private void setClicks(View... views){
        for(View view :views) view.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        rclv_emoji.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstFull = ((GridLayoutManager)recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                updateIndicator(firstFull>28?firstFull/28:0);
            }
        });
        updateIndicator();
    }

    public abstract void onClickView(TextView textView , ProfilePopUp profilePopUp);
    public abstract void onSendCLick(TextView textView , ProfilePopUp profilePopUp);

    public void setText(String text){
        if(list.size()< maxNudes){
            list.add(text);
            int i=0;
            for(String str : list){
                ((EmojiTextView)lr_send_nudge.getChildAt(i)).setText(str.trim());
                i++;
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.llMain:
                activity.hideStatusBar();
                this.dismiss();
                break;

            case R.id.img_left:
                setUserImage(false);
                break;

            case R.id.img_right:
                setUserImage(true);
                break;

            case R.id.iv_delete:
                delete();
                break;
            case R.id.img_cross:
                this.dismiss();
                break;
            case R.id.zero:
                ((EmojiAdapter)rclv_emoji.getAdapter()).setList(-1);
                rclv_emoji.smoothScrollToPosition(0);
                updateImageView((ImageView) v);
                break;
            case R.id.one:
                ((EmojiAdapter)rclv_emoji.getAdapter()).setList(0);
                rclv_emoji.smoothScrollToPosition(0);
                updateImageView((ImageView) v);
                break;
            case R.id.two:
                ((EmojiAdapter)rclv_emoji.getAdapter()).setList(1);
                rclv_emoji.smoothScrollToPosition(0);
                updateImageView((ImageView) v);
                break;
            case R.id.three:
                ((EmojiAdapter)rclv_emoji.getAdapter()).setList(2);
                rclv_emoji.smoothScrollToPosition(0);
                updateImageView((ImageView) v);
                break;
            case R.id.four:
                ((EmojiAdapter)rclv_emoji.getAdapter()).setList(3);
                rclv_emoji.smoothScrollToPosition(0);
                updateImageView((ImageView) v);
                break;
            case R.id.five:
                ((EmojiAdapter)rclv_emoji.getAdapter()).setList(4);
                rclv_emoji.smoothScrollToPosition(0);
                updateImageView((ImageView) v);
                break;
            case R.id.txt_send:

                txt_send.setClickable(false);
                if (!isClicked) {
                    try {
                        activity.hideStatusBar();
                        onSendCLick((TextView) v, this);
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    isClicked = true;
                }
                try {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            txt_send.setClickable(true);
                            isClicked = false;
                        }
                    }, 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    private void setUserImage(boolean isRight) {
        if (dataAdapter.imageList.size() != 0) {
            currentImage = (isRight ? (currentImage == dataAdapter.imageList.size() - 1 ? 0 : currentImage + 1) : (currentImage == 0 ? dataAdapter.imageList.size() - 1 : currentImage - 1));
            Picasso.with(activity).load(dataAdapter.imageList.get(currentImage).path).transform(new CircleTransform()).placeholder(R.drawable.image_defult_profile).into(profileimg);
        }
    }

    private void delete(){
        if(list.size()>0){
            int position = list.size()-1;
            list.remove(position);
            ((EmojiTextView)lr_send_nudge.getChildAt(position)).setText("");

        }
    }

   public void updateIndicator(){
       updateIndicator(0);
    }

    private void updateIndicator(int position){
        int i =((EmojiAdapter)rclv_emoji.getAdapter()).getIndicatorCount((int) context.getResources().getDimension(R.dimen._240sdp), (int) context.getResources().getDimension(R.dimen._40sdp));
        lr_indicator.setWeightSum(i);
        int padding = (int) context.getResources().getDimension(R.dimen._3sdp);
        for (int j=0;j<i;j++){
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(iv_indicator.getLayoutParams());
            imageView.setImageResource(R.drawable.iv_indicator);
            imageView.setSelected(false);
            imageView.setPadding(padding,padding,padding,padding);
            if(position==j)imageView.setSelected(true);
            if(j==0)lr_indicator.removeAllViews();
            lr_indicator.addView(imageView);
        }
    }

    @Override
    public void onShow(DialogInterface dialog) {
        updateIndicator();
    }

    public String[] getRecent(){

        preferences = context.getSharedPreferences(Constant.PREF_EMOJI, Context.MODE_PRIVATE);
        String s = preferences.getString(Constant.PREF_string,"[]");
        String ar [] = new String[maxsize];
        int i=0;
        for(String txt :s.substring(1,s.length()-1).split(",")){
            ar[i] = txt.replace("null","").trim();
            if(txt.trim().isEmpty() && !isLastFilled) {
                lastFillposition = i;
                isLastFilled = true;
            }
            i++;
        }
        recent = ar;
        return ar;
    }

    public synchronized void setRecent(final String text){
        new AsyncTask<Void,Void ,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                preferences = context.getSharedPreferences(Constant.PREF_EMOJI, Context.MODE_PRIVATE);
                if(lastFillposition<maxsize){
                    if(canAddtoList(text))
                    {
                        recent[lastFillposition] = text.trim();
                        lastFillposition ++;
                    }
                }
                else {
                    if(canAddtoList(text))
                    {
                    recent[0]= text.trim();
                    lastFillposition = 1;
                    }
                }
                {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.apply();
                    editor.putString(Constant.PREF_string, Arrays.toString(recent));
                    editor.commit();

                }

                return null;
            }
        }.execute();


    }

    private void getRecentTask(){
        new AsyncTask<Void ,Void ,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                recent = new String[maxsize];
                for(String s: recent)s="";
                getRecent();
                return null;
            }
        }.execute();
    }

    private boolean canAddtoList(final String text){
        try{
            for(String s :recent){
                Log.e("Chacked" , s+":"+text);
                if(s!= null && s.equals(text)) return false;
            }

        return true;}catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private void updateImageView(ImageView selected){

        setImage(selected,true);
        if(lastSelected != null)setImage(lastSelected,false);
        lastSelected = selected;
        updateIndicator();
    }

   private void setImage(ImageView view , boolean isactive){
        view.setBackgroundResource(isactive?  R.color.old_primary     : R.drawable.bg_table     );
        switch (view.getId()){
            case R.id.zero: view.setImageResource(isactive?  R.drawable.ic_zero_active     : R.drawable.ic_zero     );    break;
            case R.id.one:  view.setImageResource(isactive?  R.drawable.ic_one_active      : R.drawable.ic_one      );    break;
            case R.id.two:  view.setImageResource(isactive?  R.drawable.ic_two_active      : R.drawable.ic_two      );    break;
            case R.id.three:view.setImageResource(isactive?  R.drawable.ic_three_active    : R.drawable.ic_three    );    break;
            case R.id.four: view.setImageResource(isactive?  R.drawable.ic_four_car_active : R.drawable.ic_four_car );    break;
            case R.id.five: view.setImageResource(isactive?  R.drawable.ic_five_active     : R.drawable.ic_five     );    break;
        }
    }

}
