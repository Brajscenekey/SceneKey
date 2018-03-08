package com.scenekey.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scenekey.R;
import com.scenekey.activity.HomeActivity;
import com.scenekey.adapter.SearchEvent_Adapter;
import com.scenekey.model.Events;

import java.util.ArrayList;


public class NearEvent_Fragment extends Fragment {

    private Context context;
    private HomeActivity activity;
    private ArrayList<Events> eventsList;
    private RecyclerView rcViewNearEvent;
    private String[] currentLatLng;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_near_event, container, false);
        rcViewNearEvent=v.findViewById(R.id.rcViewNearEvent);
        activity.setTitleVisibility(View.VISIBLE);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setTitle(activity.getResources().getString(R.string.enter));
        setRecyclerView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
        activity= (HomeActivity) getActivity();
    }

    public void setEventsList(ArrayList<Events> eventsList) {
        this.eventsList = eventsList;
    }

    public void setNearLatLng(String[] strings) {
        currentLatLng=strings;
    }

    private void setRecyclerView() {
        if (rcViewNearEvent.getAdapter() == null) {
            SearchEvent_Adapter nearEventAdapter = new SearchEvent_Adapter(activity, eventsList,currentLatLng);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
            rcViewNearEvent.setLayoutManager(layoutManager);
            rcViewNearEvent.setAdapter(nearEventAdapter);
            nearEventAdapter.notifyDataSetChanged();
            rcViewNearEvent.setHasFixedSize(true);
        } else {
            rcViewNearEvent.getAdapter().notifyDataSetChanged();
        }
    }

    public void eventApiRefresh(){
        activity.checkEventAvailablity(true);
    }
}
