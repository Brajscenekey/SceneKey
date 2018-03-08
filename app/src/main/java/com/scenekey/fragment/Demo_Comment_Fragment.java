package com.scenekey.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scenekey.R;
import com.scenekey.activity.HomeActivity;
import com.scenekey.util.CircleTransform;
import com.squareup.picasso.Picasso;


public class Demo_Comment_Fragment extends Fragment implements View.OnClickListener {

    private Demo_Event_Fragment fragment;
    private HomeActivity activity;
    private Context context;
    private final String TAG = Demo_Comment_Fragment.class.toString();
    private int maxNumber = 120;
    private int count = 0;
    private TextView txt_char;
    private EditText edt_comment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_comment, container, false);

        //for status bar manage
        activity.setTopStatus();

        txt_char =  view.findViewById(R.id.txt_char);
        edt_comment =  view.findViewById(R.id.edt_comment);
        ImageView img_profile =  view.findViewById(R.id.img_profile);
        TextView txt_post_comment =  view.findViewById(R.id.txt_post_comment);
        ImageView img_f1_back =  view.findViewById(R.id.img_f1_back);
        txt_char.setText(maxNumber + " ");

        img_f1_back.setOnClickListener(this);
        txt_post_comment.setOnClickListener(this);

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
    }

    public Demo_Comment_Fragment setData( Demo_Event_Fragment fragment) {
        this.fragment = fragment;
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_post_comment:
                fragment.addUserComment(edt_comment.getText().toString());
                activity.onBackPressed();
                break;
            case R.id.img_f1_back:
                activity.onBackPressed();
                break;
        }
    }
}
