
package me.sjva.sosoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import me.sjva.sosoplayer.R;
import java.util.ArrayList;
import me.sjva.sosoplayer.item.ActionItem;
import me.sjva.sosoplayer.item.EntryItem;
import me.sjva.sosoplayer.item.Item;
import me.sjva.sosoplayer.item.SectionItem;

public class StorageListAdapter extends BaseAdapter {
    private static final String TAG = StorageListAdapter.class.getSimpleName();

    Context mContext;

    LayoutInflater mLayoutInflater;

    ArrayList<Item> items;

    EntryItem mSelectedItem;

    public StorageListAdapter(Context context, ArrayList<Item> items) {
        mContext = context;
        mLayoutInflater = (LayoutInflater)mContext.getApplicationContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
        mSelectedItem = null;
    }

    @Override
    public int getCount() {
        if (items != null)
            return items.size();

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (items != null)
            return items.get(position);

        return null;
    }

    @Override
    public long getItemId(int position) {
        if (items != null)
            return position;

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        final Item item = items.get(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.list_item_main_menu, null);
            viewHolder.categoryLayout = (LinearLayout)convertView.findViewById(R.id.main_menu_category_lLayout);
            viewHolder.itemLayout = (RelativeLayout)convertView.findViewById(R.id.main_menu_item_rLayout);
            viewHolder.title = (TextView)convertView.findViewById(R.id.main_menu_item_title_tView);
            viewHolder.icon = (ImageView)convertView.findViewById(R.id.main_menu_item_icon_iView);
            viewHolder.categoryName = (TextView)convertView.findViewById(R.id.main_menu_category_name_tView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }


        if (checkSelectedEntryItem(item)) {
            convertView.setBackgroundResource(R.drawable.main_menu_selected_item_background);
        } else {
            convertView.setBackgroundResource(R.drawable.main_menu_item_background);
        }

        if (checkSelectedEntryItem(item)) {
            convertView.findViewById(R.id.main_menu_item_selected_view).setVisibility(View.VISIBLE);
        } else {
            convertView.findViewById(R.id.main_menu_item_selected_view).setVisibility(View.GONE);
        }
        if (item.isSection()){
            viewHolder.itemLayout.setVisibility(View.GONE);
            viewHolder.categoryLayout.setVisibility(View.VISIBLE);
            SectionItem sectionItem = (SectionItem)item;
            viewHolder.categoryName.setText(sectionItem.getTitle());

        } else {
            viewHolder.itemLayout.setVisibility(View.VISIBLE);
            viewHolder.categoryLayout.setVisibility(View.GONE);

            if (item.isAciton()) {
                viewHolder.title.setText("Add");
                viewHolder.icon.setImageResource(getImageRes(item));
            } else if(item instanceof EntryItem) {
                EntryItem entryItem = (EntryItem)item;
                viewHolder.title.setText(entryItem.info.getName());
            }
            viewHolder.icon.setImageResource(getImageRes(item));
        }
        convertView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (item.isSection()) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        return convertView;
    }

    public void setSelectedItem(EntryItem item) {
        mSelectedItem = item;
    }

    private int getImageRes(Item item) {
        if (item instanceof EntryItem) {
            EntryItem entryItem = (EntryItem)item;
            switch (entryItem.type){
                case MediaStore:
                    return R.drawable.ic_tab_selected_all;
                case Mount:
                    return R.drawable.ic_folder_holo_dark;
                case Samba:
                case Ftp:
                case WebDav:
                case Plex:
                    return R.drawable.btn_star_on_normal_holo_dark;
            }

        } else if (item instanceof ActionItem) {
            return R.drawable.ic_menu_add;
        } else if (item instanceof SectionItem) {

        }
        return 0;
    }

    private boolean checkSelectedEntryItem(Item item) {
        if (item.isSection())
            return false;

        if (mSelectedItem == null || item == null)
            return false;

        EntryItem entryItem = (EntryItem)item;
        if (mSelectedItem.info.getName() != null
            && mSelectedItem.info.getName().equals(entryItem.info.getName())) {
            return true;
        }

        return false;
    }

    static class ViewHolder {
        RelativeLayout itemLayout;
        LinearLayout categoryLayout;
        ImageView icon;
        TextView title;
        TextView categoryName;
    }

}
