
package me.sjva.sosoplayer.adapter;

import android.graphics.Color;
import me.sjva.sosoplayer.R;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import com.google.android.exoplayer2.ext.plex.Directory;

public class MenuListAdapter extends BaseAdapter {
    private static final String TAG = MenuListAdapter.class.getSimpleName();

    Context mContext;

    LayoutInflater mLayoutInflater;

    ArrayList<Directory> mList;

    Directory mSelectedItem;

    public MenuListAdapter(Context context, ArrayList<Directory> list) {
        mContext = context;
        mLayoutInflater = (LayoutInflater)mContext.getApplicationContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mList = list;
        mSelectedItem = null;
    }

    @Override
    public int getCount() {
        if (mList != null)
            return mList.size();

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mList != null)
            return mList.get(position);

        return null;
    }

    @Override
    public long getItemId(int position) {
        if (mList != null)
            return position;

        return 0;
    }

    private boolean isItemSelected(Directory item) {
        if (mSelectedItem == null || item == null)
            return false;

        if (mSelectedItem.getUuid() == null || item.getUuid() == null)
            return false;

        return mSelectedItem.getUuid().equals(item.getUuid());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        final Directory item = mList.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            viewHolder.position = position;

            convertView = mLayoutInflater.inflate(R.layout.list_item_main_menu, null);

            viewHolder.categoryLayout = (LinearLayout)convertView
                    .findViewById(R.id.main_menu_category_lLayout);
            viewHolder.itemLayout = (RelativeLayout)convertView
                    .findViewById(R.id.main_menu_item_rLayout);
            viewHolder.title = (TextView)convertView.findViewById(R.id.main_menu_item_title_tView);
            viewHolder.icon = (ImageView)convertView.findViewById(R.id.main_menu_item_icon_iView);
            viewHolder.categoryName = (TextView)convertView
                    .findViewById(R.id.main_menu_category_name_tView);
//            viewHolder.categoryLine = convertView.findViewById(R.id.main_menu_category_line_view);
            viewHolder.categoryLine.setVisibility(View.GONE);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if (isItemSelected(item)) {
            convertView.setBackgroundResource(R.drawable.main_menu_selected_item_background);
            viewHolder.title.setTextColor(mContext.getResources().getColor(
                    R.color.main_menu_item_text_pressed));
            viewHolder.title.setTypeface(null, Typeface.BOLD);
        } else {
            convertView.setBackgroundResource(R.drawable.main_menu_item_background);

            viewHolder.title.setTextColor(mContext.getResources().getColor(
                    R.color.main_menu_item_text));
            viewHolder.title.setTypeface(null, Typeface.NORMAL);
        }
        convertView.setBackgroundColor(Color.TRANSPARENT);

        viewHolder.categoryLayout.setVisibility(View.GONE);
        viewHolder.itemLayout.setVisibility(View.VISIBLE);
        viewHolder.title.setText(item.getTitle());
        viewHolder.icon.setImageResource(R.drawable.ic_media_video_poster);


        return convertView;
    }

    public void setSelectedItem(Directory item) {
        mSelectedItem = item;
    }


    static class ViewHolder {
        RelativeLayout itemLayout;

        ImageView icon;

        TextView title;

        LinearLayout categoryLayout;

        TextView categoryName;

        View categoryLine;

        int position;
    }

}
