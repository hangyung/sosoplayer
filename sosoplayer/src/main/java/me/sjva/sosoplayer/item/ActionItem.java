package me.sjva.sosoplayer.item;


import me.sjva.sosoplayer.data.StorageType;

public class ActionItem implements Item {

  public StorageType storageType;
  public  ActionItem(StorageType storageType) {
    this.storageType = storageType;
  }

  @Override
  public boolean isSection()
  {
    return false;
  }

  @Override
  public boolean isAciton() {
    return true;
  }
}
