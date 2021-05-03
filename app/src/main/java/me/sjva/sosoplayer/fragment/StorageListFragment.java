package me.sjva.sosoplayer.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import me.sjva.sosoplayer.R;
import java.util.ArrayList;
import me.sjva.sosoplayer.activity.MainActivity;
import me.sjva.sosoplayer.adapter.StorageListAdapter;
import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.data.StorageType;
import me.sjva.sosoplayer.item.EntryItem;
import me.sjva.sosoplayer.item.Item;
import me.sjva.sosoplayer.item.SectionItem;
import me.sjva.sosoplayer.util.SharedPreferencesUtil;
import me.sjva.sosoplayer.activity.OnMainEventListener;
import me.sjva.sosoplayer.widget.StorageListLongClickDialog;


public class StorageListFragment extends Fragment implements AdapterView.OnItemLongClickListener{
    
    // MenuList
    private ListView mDrawerList;
    private ArrayList<Item> mItems;
    private StorageListAdapter mAdapter;

    private StorageInfo storageInfo;
    private EntryItem mSelectedItem = null;
    private OnMainEventListener onMainEventListener;
    public StorageListFragment(StorageInfo storageInfo,  OnMainEventListener onMainEventListener) {
        this.storageInfo = storageInfo;
        this.onMainEventListener = onMainEventListener;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.fragment_playlist, null);

        if(root != null) {
            mDrawerList = (ListView)root.findViewById(R.id.playlist_listView);
            mDrawerList.setDivider(getActivity().getResources().getDrawable(android.R.color.transparent));
            mDrawerList.setOnItemClickListener(mItemClickListener);
            mDrawerList.setOnItemLongClickListener(this);
            mDrawerList.setBackgroundColor(Color.TRANSPARENT);
        }
        
        return root;
    }

    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private boolean hasStorageType(StorageType storageType , ArrayList<StorageInfo> storageInfoArrayList ) {
        for (StorageInfo storageInfo : storageInfoArrayList) {
            if (storageInfo.getStorageType() == storageType) {
                return true;
            }
        }
        return false;
    }
    private void addStorageItems(ArrayList<Item> items, StorageType storageType , ArrayList<StorageInfo> storageInfoArrayList) {
        for (StorageInfo storageInfo : storageInfoArrayList) {
            if (storageInfo.getStorageType() == storageType) {
                items.add(new EntryItem(storageInfo, storageType));
            }
        }
    }

    @Override
    public void onResume() {
        if (mItems == null) {
            mItems = new ArrayList<Item>();
            SharedPreferencesUtil sharedPreferencesUtil = SharedPreferencesUtil.getInstance(getActivity());
            ArrayList<StorageInfo> storageInfoArrayList = sharedPreferencesUtil.getStorageInfoList();

            for (StorageType storageType : StorageType.values()) {
                if (storageType == StorageType.MediaStore) {
                    mItems.add(new SectionItem(StorageType.MediaStore));
                    mItems.add(new EntryItem( new StorageInfo(StorageType.MediaStore),  StorageType.MediaStore));
                } else {
                    if(hasStorageType(storageType, storageInfoArrayList)) {
                        mItems.add(new SectionItem(storageType));
                        addStorageItems(mItems, storageType, storageInfoArrayList);
                    }
                }
            }
            mAdapter = new StorageListAdapter(getActivity(), mItems);
            mDrawerList.setAdapter(mAdapter);
            if (mSelectedItem == null) {
                mSelectedItem = new EntryItem( storageInfo,  storageInfo.getStorageType());
            }
            updateMenu();
        }
       // menuListInitialize(false);
        super.onResume();
    }

    private void updateMenu() {
        if (getActivity() != null && mItems != null) {
            mDrawerList.setAdapter(mAdapter);

            mAdapter.setSelectedItem(mSelectedItem);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void selectItem(int position) {
        if (mItems != null && mItems.size() > position) {
            Item item = mItems.get(position);
            if (item instanceof EntryItem) {
                mSelectedItem = (EntryItem)item;
                mAdapter.setSelectedItem(mSelectedItem);
            }
            MainActivity activity = (MainActivity)getActivity();
            if (mSelectedItem != null) {
                activity.selectItem(mSelectedItem);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    };

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int postion, long l) {
        if (postion == 1)
            return true; // Media Store not modify
        Item item = mItems.get(postion);
        EntryItem entryItem = (EntryItem)item;
        new StorageListLongClickDialog(getActivity(), entryItem.info, onMainEventListener).show();
        return true;
    }


}
