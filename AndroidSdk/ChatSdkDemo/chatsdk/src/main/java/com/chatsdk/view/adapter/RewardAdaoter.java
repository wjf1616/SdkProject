package com.chatsdk.view.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chatsdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dongjunjie on 2018/3/16.
 */

public class RewardAdaoter  extends BaseAdapter {
    private Context c;
    private List<RewardInfo> items;
    private ArrayList<RewardInfo> itemsBackup;
    private LayoutInflater inflater;
    private boolean 					_endGetPing;
    private int time;
    public RewardAdaoter(Context f, int textViewResourceId, ArrayList<RewardInfo> objects)
    {
        this.c = f;
        this.itemsBackup = objects;
        this.items = (ArrayList<RewardInfo>)itemsBackup.clone();
        this.inflater = ((LayoutInflater) this.c.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.cs__reward_item, viewGroup,false);
            holder.itemNameText = (TextView) convertView.findViewById(R.id.reward_name_text);
            holder.itemNumText = (TextView) convertView.findViewById(R.id.reward_num_text);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }
        RewardInfo wsServerInfo = itemsBackup.get(i);
        holder.itemNameText.setText(wsServerInfo.getItemName());//80000082 = 线路
        holder.itemNumText.setText(String.valueOf("x"+wsServerInfo.getNumber()));
        holder.itemNumText.setTextColor(c.getResources().getColor(R.color.cs__white));
        return convertView;
    }


    @Override
    public void notifyDataSetChanged() {
        this.items = (ArrayList<RewardInfo>)this.itemsBackup.clone();
        super.notifyDataSetChanged();
    }

    public class ViewHolder{
        public TextView itemNameText;
        public TextView itemNumText;
        public ViewHolder(){

        }

    }

    public static class RewardInfo{
        private String itemPic;
        private String itemName;
        private String number;

        public RewardInfo(String name, String number){
            this.itemName = name;
            this.number = number;
        }

        public RewardInfo(String pic,String name, String number){
            this.itemPic = pic;
            this.itemName = name;
            this.number = number;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getItemPic() {
            return itemPic;
        }

        public void setItemPic(String itemPic) {
            this.itemPic = itemPic;
        }
    }
}
