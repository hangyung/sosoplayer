package me.sjva.sosoplayer.item;

import me.sjva.sosoplayer.data.StorageType;

public class SectionItem implements Item
{
	private final String title;
	private final StorageType storageType;

	public SectionItem(StorageType storageType)
	{
		this.title = storageType.toString();
		this.storageType = storageType;
	}

	public String getTitle()
	{
		return title;
	}

	public StorageType getStorageType() {
		return storageType;
	}

	@Override
	public boolean isSection()
	{
		return true;
	}

	@Override
	public boolean isAciton() {
		return false;
	}
}
