package com.Models;

public class LibraryItem {
    public String itemName;
    public int itemQuantity;
    public String itemID;
    public LibraryItem(String itemName , int itemQuantity,String itemID){
        this.itemName = itemName;
        this.itemQuantity =  itemQuantity;
        this.itemID = itemID;
    }
}