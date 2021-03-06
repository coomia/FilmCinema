package com.example.hopjs.filmcinema.UI.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.hopjs.filmcinema.Adapter.CinemasAdapter;
import com.example.hopjs.filmcinema.Common.Transform;
import com.example.hopjs.filmcinema.Data.Cinema;
import com.example.hopjs.filmcinema.MyApplication;
import com.example.hopjs.filmcinema.Network.Connect;
import com.example.hopjs.filmcinema.R;
import com.example.hopjs.filmcinema.Test.Test;

import java.util.ArrayList;

/**
 * Created by Hopjs on 2016/10/12.
 */

public class CinemaListFragment extends Fragment {
    public static final int TYPE_ALLCITY = 1;
    public static final int TYPE_NEARBY = 2;
    public static final int TYPE_SEARCH = 3;
    private final int REFRESH_FINISHED = 1;
    private final int LOAD_MORE_FINISHED = 2;

    private SwipeRefreshLayout srlRefresh;
    private RecyclerView rvCinemas;
    private ArrayList<Cinema> cinemas;
    private CinemasAdapter cinemasAdapter;
    private Handler handler;
    private LinearLayoutManager linearLayoutManager;
    private int start,lastVisibleItem;
    private int type;
    private boolean noSpecial,isAll;
    private String filmId;
    private String cityId;
    private String cinemaNameLike;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cinemas,container,false);

        srlRefresh = (SwipeRefreshLayout)view.findViewById(R.id.srl_fragment_cinemas_refresh);
        rvCinemas = (RecyclerView)view.findViewById(R.id.rv_fragment_cinemas_list);
        linearLayoutManager = new LinearLayoutManager(getActivity());

        rvCinemas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(srlRefresh.isRefreshing()) {
                    return true;
                }else {
                    return false;
                }
            }
        });
        rvCinemas.setOnScrollListener(onScrollListener);
        srlRefresh.setRefreshing(true);
        srlRefresh.setOnRefreshListener(onRefreshListener);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.arg1 == REFRESH_FINISHED){
                    setCinemas();
                }else {
                    setMore();
                }
                srlRefresh.setRefreshing(false);
            }
        };

        type = TYPE_ALLCITY;
        Bundle bundle = getArguments();
        if(bundle != null && bundle.get("type")!= null){
            type = bundle.getInt("type");
            Log.i("000000",bundle.getInt("type")+"");
            if(type ==TYPE_SEARCH){
                srlRefresh.setRefreshing(false);
            }
        }

        noSpecial = true;
        isAll=false;
        if(bundle != null && bundle.get("filmId")!= null){
            noSpecial = false;
            filmId = bundle.get("filmId").toString();
        }
        start = 0;
        cityId = ((MyApplication)getActivity().getApplicationContext()).cityId+"";
        if(type != TYPE_SEARCH)
            loadCinemas();
        return view;
    }

    public void setCinemaNameLike(final String cinemaNameLike){
        start=0;
        this.cinemaNameLike = cinemaNameLike;
        new Thread(){
            @Override
            public void run() {
                //cinemas = Test.getCinemas(start,10);
                cinemas = Connect.getSearchCinema(cinemaNameLike,cityId,start+"" );
                Message message = new Message();
                message.arg1 = REFRESH_FINISHED;
                handler.sendMessage(message);
                if(cinemas.size()<10)isAll=true;
                start += cinemas.size();
            }
        }.start();

    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(newState == RecyclerView.SCROLL_STATE_SETTLING &&
                    lastVisibleItem+1 == linearLayoutManager.getItemCount()){
                loadMore();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
        }
    };

    private void setMore(){
        cinemasAdapter.setIsAll(isAll);
        for(Cinema cinema:cinemas){
            cinemasAdapter.add(cinema);
        }
    }
    private void loadMore(){
        new Thread(){
            @Override
            public void run() {
                //cinemas = Test.getCinemas(start,10);
                if(isAll)return;
                if(noSpecial) {
                    if (type == TYPE_ALLCITY) {
                        cinemas = Connect.getAllCity_CinemaList_NoSpecial(cityId,start+"");
                    } else if(type == TYPE_NEARBY){
                        cinemas = Connect.getNearby_CinemaList_NoSpecial(cityId,"jing", "wei",start+"" );
                    }else {
                        cinemas = Connect.getAllCity_CinemaList_NoSpecial(cityId,start+"");
                    }
                }else {
                    if (type == TYPE_ALLCITY) {
                        cinemas = Connect.getAllCity_CinemaList_Special(filmId,cityId,start+"");
                    } else {
                        cinemas = Connect.getNearby_CinemaList_Special(filmId,cityId, "jing", "wei",start+"");
                    }
                }
                Message message = new Message();
                message.arg1 = LOAD_MORE_FINISHED;
                handler.sendMessage(message);
                if(cinemas.size()<10)isAll=true;
                start += cinemas.size();
            }
        }.start();
    }

    private void loadCinemas(){
        new Thread(){
            @Override
            public void run() {
               // cinemas = Test.getCinemas(start,10);
                if(noSpecial) {
                   // cinemas = Test.getCinemas(1,1);
                    if (type == TYPE_ALLCITY) {
                        cinemas = Connect.getAllCity_CinemaList_NoSpecial(cityId,start+"");
                    } else if(type == TYPE_NEARBY){
                        cinemas = Connect.getNearby_CinemaList_NoSpecial(cityId,"jing", "wei",start+"" );
                    }else {

                    }
                }else {
                    if (type == TYPE_ALLCITY) {
                        cinemas = Connect.getAllCity_CinemaList_Special(filmId,cityId,start+"");
                    } else {
                        cinemas = Connect.getNearby_CinemaList_Special(filmId,cityId, "jing", "wei",start+"");
                    }
                }

                if(cinemas.size()<10)isAll=true;
                start += cinemas.size();
                Message message = new Message();
                message.arg1 = REFRESH_FINISHED;
                handler.sendMessage(message);
            }
        }.start();
    }

    private void setCinemas(){
        rvCinemas.setLayoutManager(linearLayoutManager);
        cinemasAdapter = new CinemasAdapter(getActivity(),cinemas,listener);
        cinemasAdapter.setIsAll(isAll);
        rvCinemas.setAdapter(cinemasAdapter);
    }

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (type != TYPE_SEARCH) {
                start = 0;
                isAll=false;
                loadCinemas();
            } else {
                srlRefresh.setRefreshing(false);
            }
        }
    };

    private CinemasAdapter.OnItemClickListener listener = new CinemasAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, String id) {
            Transform.toCinemaDetail(getActivity(),id);
        }
    };
}
