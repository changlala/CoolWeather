package com.coolweather.android;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 *
 * 现在出现了一个很奇葩的问题，county显示的不正确。原因是selectedC的id不正确，明明存的时 cityid正常，取的时候就不对了？？？
 * 需要一个数据库查看工具
 */
public class ChooseArea_Fragment extends Fragment {

    private List<String> dataList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView titleTextView;
    private Button b1;
    private MyAdapter.OnItemClickListener listener;
    private Province selectedP;
    private City selectedC;
    private ProgressDialog progressDialog;
    private MyAdapter adapter;
    private int currentLevel;
    private static final int PROVINCE = 0;
    private static final int CITY = 1;
    private static final int COUNTY = 2;
    private static final String TAG = "ChooseArea_Fragment";
    public ChooseArea_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_choose_area_,container);
        b1 = v.findViewById(R.id.back_button);
        titleTextView = v.findViewById(R.id.title_text);
        recyclerView = v.findViewById(R.id.recyclerview);
        dataList.add("11111");
        adapter = new MyAdapter(dataList, new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemclick(View view, int position) {
                Log.d(TAG, "onItemclick: position"+position);
                switch (currentLevel){
                    case PROVINCE:
                        selectedP = LitePal.where("name=?",dataList.get(position)).find(Province.class).get(0);
                        Log.d(TAG, "onItemclick: selectedP "+selectedP.getName()+"id"+selectedP.getProvinceId());
                        showCities();
                        break;
                    case CITY:
                        selectedC = LitePal.where("name=?",dataList.get(position)).find(City.class).get(0);
                        Log.d(TAG, "onItemclick: selectedC "+selectedC.getName()+"id"+selectedC.getCityId());
                        showCounties();
                        break;
                    case COUNTY:
                        County county = LitePal.where("name=?",dataList.get(position)).find(County.class).get(0);
                        if(getActivity() instanceof MainActivity){
                            Intent intent = new Intent(getActivity(),WeatherActivity.class);
                            intent.putExtra("weatherId",county.getWeatherId());
                            startActivity(intent);
                            getActivity().finish();
                        }else if(getActivity() instanceof WeatherActivity){
                            WeatherActivity activity = (WeatherActivity) getActivity();
                            activity.drawerLayout.closeDrawers();
                            activity.swipeRefreshLayout.setRefreshing(true);
                            activity.weatherId = county.getWeatherId();
                            activity.refreshPage();
                        }
                        break;

                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (currentLevel){
                    case CITY:
                        showProvinces();
                        break;
                    case COUNTY:
                        showCities();
                        break;
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showProvinces();

    }

    private void showCities(){
        titleTextView.setText(selectedP.getName());
        b1.setVisibility(View.VISIBLE);
        List<City> cities = LitePal.where("pId=?",String.valueOf(selectedP.getProvinceId())).find(City.class);
        if(cities.size() > 0){
            dataList.clear();
            for(City city : cities){
                dataList.add(city.getName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = ChooseArea_Fragment.CITY;
        }else{
            String url = "http://guolin.tech/api/china/"+selectedP.getProvinceId();
            queryFromServer(url,"city");
        }
    }

    private void showCounties(){
        String cityName = selectedC.getName();
        titleTextView.setText(cityName);
        b1.setVisibility(View.VISIBLE);
        List<County> counties = LitePal.where("cId=?",String.valueOf(selectedC.getCityId())).find(County.class);
        if(counties.size() > 0){
            dataList.clear();
            for(County county : counties){
                dataList.add(county.getName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = ChooseArea_Fragment.COUNTY;
        }else{
            String url = "http://guolin.tech/api/china/"+selectedP.getProvinceId()+"/"+selectedC.getCityId();
            queryFromServer(url,"county");
        }
        int sjzNums = LitePal.where("name=?",String.valueOf("石家庄")).find(City.class).size();
        Log.d(TAG, "showCounties: nums"+adapter.getItemCount()+"shijiazhuang 一共有几条记录呢"+sjzNums);
    }
    private void showProvinces(){
        titleTextView.setText("中国");
        b1.setVisibility(View.INVISIBLE);
        List<Province> provinces = LitePal.findAll(Province.class);


        if(provinces.size() > 0){
            dataList.clear();
            for(Province p : provinces){
                dataList.add(p.getName());
                Log.d(TAG, "showProvinces: datalist"+p.getName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = ChooseArea_Fragment.PROVINCE;

        }else{
            String url = "http://guolin.tech/api/china";
            queryFromServer(url,"province");
        }
        Log.d(TAG, "showProvinces: nums"+adapter.getItemCount());
    }
    public void queryFromServer(String address,final String TYPE){
        showProgressDialog();
        HttpUtil.sendOkhttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"发生错误。。",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result  = false;
                switch (TYPE){
                    case "province":
                        result = Utility.handleProvinceResponse(responseText);
                        break;
                    case "city":
                        result = Utility.handleCityRequest(responseText,selectedP.getProvinceId());
                        break;
                    case "county":
                        result = Utility.handleCountyRequest(responseText,selectedC.getCityId());
                        break;
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (TYPE){
                                case "province":
                                    showProvinces();//涉及到改UI所以需要写到runOnUiThread里
                                    break;
                                case "city":
                                    showCities();
                                    break;
                                case "county":
                                    showCounties();
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
