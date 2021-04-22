package me.sjva.sosoplayer.item;

import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.data.StorageType;

public class EntryItem implements Item
{
	public StorageInfo info;

	public boolean bSection = false;


	public StorageType type;

	public EntryItem(StorageInfo info,  StorageType type)
	{
		this.info = info;

		this.type = type;
	}



	@Override
	public boolean isSection()
	{
		return false;
	}

	@Override
	public boolean isAciton() {
		return false;
	}
}