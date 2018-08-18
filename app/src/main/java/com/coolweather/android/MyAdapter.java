package com.coolweather.android;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements  View.OnClickListener{
    private List<String> data ;
    private OnItemClickListener myItemLitener;
    private static final String TAG = "MyAdapter";
    public MyAdapter(List<String> data,OnItemClickListener listener) {
        this.data = data;
        this.myItemLitener = listener;
    }
    class ViewHolder extends RecyclerView.ViewHolder{
         //int postion;
         TextView textview;
         ImageView imgView;

        public ViewHolder(@NonNull View itemView) {//view是列表子项的外层容器
            super(itemView);
            textview = itemView.findViewById(R.id.recycler_textview);
            imgView = itemView.findViewById(R.id.recycler_imageview);
        }
    }
    public interface OnItemClickListener{
        void onItemclick(View view , int position);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder,int i) {
        String str = data.get(i);
        viewHolder.imgView.setImageResource(R.mipmap.ic_launcher);
        viewHolder.textview.setText(str);
        viewHolder.textview.setTag(i);
        viewHolder.textview.setTextSize(20);
        Log.d(TAG, "onBindViewHolder: "+viewHolder.textview.getText());
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {

        return data.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item,viewGroup,false);
        v.setOnClickListener(this);

        Log.d(TAG, "onCreateViewHolder: ");
        return new ViewHolder(v);
    }

    @Override
    public void onClick(View view) {
        if(myItemLitener != null)
            myItemLitener.onItemclick(view, (int)view.findViewById(R.id.recycler_textview).getTag());
    }


}
