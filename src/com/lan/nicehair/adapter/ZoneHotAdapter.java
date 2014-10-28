package com.lan.nicehair.adapter;

import com.lan.nicehair.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ZoneHotAdapter extends BaseAdapter {

	private String[] titleStr=new String[] {"活动","达人秀","发型咨询","热门话题","学编发","美甲社","彩妆控","碎碎念"};
	private Context mContext;
	public ZoneHotAdapter(Context context) {
		this.mContext=context;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return titleStr.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return titleStr[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		convertView=LayoutInflater.from(mContext).inflate(R.layout.item_hot_tab, null);
		TextView tvTitle=(TextView)convertView.findViewById(R.id.tv_tab_item);
		tvTitle.setText(titleStr[position]);
		int color=mContext.getResources().getColor(R.color.hair_scan_tag_color1);
		switch(position) {
		case 0:
			color=mContext.getResources().getColor(R.color.hair_scan_tag_color1);
			break;
		case 1:
			color=mContext.getResources().getColor(R.color.hair_scan_tag_color2);
			break;
		case 2:
			color=mContext.getResources().getColor(R.color.hair_scan_tag_color3);
			break;
		case 3:
			color=mContext.getResources().getColor(R.color.hair_scan_tag_color4);
			break;
		case 4:
			color=mContext.getResources().getColor(R.color.hair_scan_tag_color5);
			break;
		case 5:
			color=mContext.getResources().getColor(R.color.hair_scan_tag_color6);
			break;
		case 6:
			color=mContext.getResources().getColor(R.color.hair_scan_tag_color7);
			break;
		case 7:
			color=mContext.getResources().getColor(R.color.hair_scan_tag_color8);
			break;
		}
		if((position+1)%2!=0) {
			tvTitle.setBackgroundResource(R.drawable.hot_tag_left_bg);
		}else {
			tvTitle.setBackgroundResource(R.drawable.hot_tag_right_bg);
		}
		tvTitle.setTextColor(color);
		return convertView;
	}

}
