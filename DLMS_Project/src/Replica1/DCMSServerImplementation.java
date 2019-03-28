package com.Server;

import com.Configuration.LogManager;
import com.Models.LibraryItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;


public class DCMSServerImplementation{

    enum OPERATION{
        ADD,REMOVE;
    }

    HashMap<String,LibraryItem> hashMapLibrary= new HashMap<String, LibraryItem>();
    HashMap<String, ArrayList<String>> hashMapWaitlist = new HashMap<String, ArrayList<String>>();
    HashMap<String,ArrayList<String>> hashMapBorrowListUser = new HashMap<String,ArrayList<String>>();
    HashMap<String,ArrayList<String>> hashMapBorrowListItem = new HashMap<String,ArrayList<String>>();
    String serverLocation;
    LogManager logManager;
    ServerUDPManager serverUDPManager;
    String IPAddress;
    String location;


    public  DCMSServerImplementation(com.Server.Constants.ServerLocation location){
        this.logManager = new LogManager(location.toString());
        this.hashMapBorrowListUser = new HashMap<>();
        this.hashMapLibrary = new HashMap<>();
        this.hashMapBorrowListItem = new HashMap<>();
        serverUDPManager = new ServerUDPManager(this,location,logManager.logger);
        serverUDPManager.start();
        this.serverLocation = location.toString();
    }

    public synchronized String addItem(String managerID, String itemID, String itemName, int quantity) {
        String message = "";
        String result = "";
        if (managerID.substring(3,4).equals("M")){
            LibraryItem item = hashMapLibrary.get(itemID);

            if (item != null){
                updateItemQuantity(itemID,quantity,OPERATION.ADD);
            }else{
                item = new LibraryItem(itemName,quantity,itemID);
                hashMapLibrary.put(itemID,item);
            }
            manageWaitListOnAdd(itemID);
            result = "Success";
            message = "Item Added successfull";
        }else{
            result = "failure";
            message = "Only manager can perform given operation";
        }

        logManager.logger.log(Level.INFO, Calendar.getInstance().getTime().toString()+"\t"+"Add Item"+"\t"+managerID+"\t"+itemID+"\t"+itemName+"\t"+result+"\t"+message);
        return result+message;
    }

    public void manageWaitListOnAdd(String itemID){
        ArrayList<String> arrWaitlist = new ArrayList<>();
        arrWaitlist = hashMapWaitlist.get(itemID);
        if ( arrWaitlist!=null && arrWaitlist.size()>0){
            borrowItem(arrWaitlist.get(0),itemID);
            arrWaitlist.remove(0);
        }
    }

    public synchronized String removeItem(String managerID, String itemID, int quantity) {
        String message = "";
        String result = "";
        String itemName = "";

        if (managerID.substring(3,4).equals("M")){
            String university = trimString(0,3,managerID);
            if (hashMapLibrary.get(itemID) != null){
                LibraryItem item = hashMapLibrary.get(itemID);
                itemName = item.itemName;
                if (quantity == -1){
                    ArrayList<String> arrUserBorrowed = hashMapBorrowListItem.get(itemID);
                    if (arrUserBorrowed!=null && arrUserBorrowed.size()>0){
                        for (String userID: arrUserBorrowed){
                            updateHashMapUser(userID,itemID,OPERATION.REMOVE);
                        }
                    }
                    hashMapBorrowListItem.remove(itemID);
                    hashMapLibrary.remove(itemID);
                    message = "Item deleted";
                    result = "Success";
                } else if (item.itemQuantity < quantity){
                    message = "Quantity to be reduced in more than existing,Please check";
                    result = "failure";
                } else {
                    updateItemQuantity(itemID,quantity,OPERATION.REMOVE);
                    message = "Item removed successfully";
                    result = "Success";
                }
            } else {
                message = "Item does not exist,No deletion performed";
                result = "failure";
            }
        }else{
            result = "failure";
            message = "Only manager can perform given operation";
        }

        logManager.logger.log(Level.INFO, Calendar.getInstance().getTime().toString()+"\t"+"Remove Item"+"\t"+managerID+"\t"+itemID+"\t"+itemName+"\t"+result+"\t"+message);

        return result+message;
    }

    public synchronized String listItemAvailability(String managerID) {
        ArrayList<String> arrRecords = new ArrayList<>();
        String message = "";
        String result = "";

        if (managerID.substring(3,4).equals("M")){
            for (String key : hashMapLibrary.keySet()){
                LibraryItem item = hashMapLibrary.get(key);
                arrRecords.add(key.concat("\t"+item.itemName).concat("\t"+Integer.toString(item.itemQuantity)));
            }
            message = "Items listed";
            result = "Success";
        }else{
            result = "failure";
            message = "Only manager can perform given operation";
        }

        System.out.println(arrRecords.toString());
        logManager.logger.log(Level.INFO, Calendar.getInstance().getTime().toString()+"\t"+"List Item"+"\t"+managerID+"\t"+result+"\t"+message+"\t"+arrRecords.toString());

        return result+"\t"+message+arrRecords.toString();
    }

    public synchronized String addInWaitlist(String userID, String itemID) {
        String response = "";
        ArrayList<String> arrUser = hashMapWaitlist.get(itemID);
        if (arrUser!=null){
            arrUser.add(userID);
        }else{
            arrUser = new ArrayList<>();
            arrUser.add(userID);
        }
        hashMapWaitlist.put(itemID,arrUser);
        response = "Success User added in waitlist";
        return response;
    }

    public UDPClient[] createClient(String itemID){
        UDPClient[] request = new UDPClient[2];
        int counter = 0;
        ArrayList<String> locationList = new ArrayList<>();
        locationList.add("MCG");
        locationList.add("CON");
        locationList.add("MON");
        for (String loc: locationList){
            if (loc != this.serverLocation){
                try {
                    request[counter] = new UDPClient(Replica1.serverRepository.get(loc),itemID);
                } catch (IOException e){

                }
                request[counter].start();
                counter++;
            }
        }
        return request;
    }

    public String searchItem(String searchItem,Boolean isItemName){
        String response = "";
        //i could have used condition in loop but overhead would have been high, So used this although redundant
        if (isItemName){
            for (String key : hashMapLibrary.keySet()){
                LibraryItem item = hashMapLibrary.get(key);
                if (item.itemName.equals(searchItem)){
                    response = response.concat(key).concat("\t").concat(item.itemName).concat("\t").concat(Integer.toString(item.itemQuantity)).concat("\n");
                    break;
                }
            }
        }else{
            for (String key : hashMapLibrary.keySet()){
                LibraryItem item = hashMapLibrary.get(key);
                if (key.equals(searchItem)){
                    response = response.concat(key).concat("\t").concat(item.itemName).concat("\t").concat(Integer.toString(item.itemQuantity)).concat("\n");
                    break;
                }
            }
        }
        return response;
    }
    public synchronized String findItem(String userID, String itemName,String itemID) {

        String result = "";
        String message = "";
        String response = "";
        if (itemName.equals("")){
            response=searchItem(itemID,false);
        }else{
            response=searchItem(itemName,true);
        }
        String appendedItemID = "C".concat(itemName).concat(",").concat(itemID);
        UDPClient[] request = createClient(appendedItemID);
        for (UDPClient c: request){
            try {
                c.join();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            response = response.concat(c.itemSearchResult).concat("\n");
        }
        if (response.trim().equals("")){
            result = "failure";
            message = "No item found";
        }else{
            result ="Success ";
            message = "Item found";
        }
        System.out.println(response);
        logManager.logger.log(Level.INFO, Calendar.getInstance().getTime().toString()+"\t"+"Find Item"+"\t"+itemName+"\t"+result+"\t"+message+"\t"+response);
        return result+" "+message+" "+response;
    }

    public Boolean hasUserAlreadyBorrowedBookFromAnotherUniversity(String userID,String itemID){
        ArrayList<String> arrLibraryItem = hashMapBorrowListUser.get(userID);
        Boolean hasBorrowed = false;
        String universityOfCurrentItem = trimString(0,3,itemID);
        if (arrLibraryItem == null){
            hasBorrowed = false;
        }else{
            for (String item: arrLibraryItem){
                if (universityOfCurrentItem.equals(item.substring(0,3))){
//                    hasBorrowed = true;
                }
            }
        }
        return hasBorrowed;
    }

    public String borrowBookFromOtherUniversity(String userID, String itemID){
        String appendedItemID = "B".concat(itemID).concat(",").concat(userID);
        String serverLocation = itemID.substring(0,3);
        String result = "";
        String message = "";
        UDPClient client;
        try{
            client = new UDPClient(Replica1.serverRepository.get(serverLocation),appendedItemID);
            client.run();
            if (client.isBookBorrowd.equals("true")){
                result = "Success";
                message = " Book borrowed from "+ serverLocation;
            }else {
                result = "failure";
                message = " Book not available";
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return result + message;
    }

    public synchronized String borrowItem(String userID, String itemID) {
        String output = "";
        if (userID.substring(3,4).equals("U")){
            String universityOfItem = trimString(0,3,itemID);
            String universityOfUser = trimString(0,3,userID);
            if (universityOfItem.equals(universityOfUser)){
                LibraryItem item =  this.hashMapLibrary.get(itemID);
                if (item!=null){
                    ArrayList<String> arrUserBorrowed = hashMapBorrowListUser.get(userID);
                        if (arrUserBorrowed!= null&&arrUserBorrowed.contains(itemID)) {
                            output = "Fail Book already borrowed";
                        }else{
                            if (item.itemQuantity>0){
                                item.itemQuantity = item.itemQuantity-1;
                                hashMapLibrary.put(itemID,item);
                                updateHashMapItem(userID,itemID,OPERATION.ADD);
                                updateHashMapUser(userID,itemID,OPERATION.ADD);
                                output = "Success Book borrowed";
                            }else{
                                output = "failure Book not available";
                                //prompt and ask if they want to be added to waitlist.
                            }
                        }
                } else {
                    output = "failure Book not available";
                }
            } else {
                if (hasUserAlreadyBorrowedBookFromAnotherUniversity(userID,itemID) == false){
                    output =borrowBookFromOtherUniversity(userID,itemID);
                    updateHashMapUser(userID,itemID,OPERATION.ADD);
                }else{
                    output = "failure Only 1 book from other university can be borrowed";
                }
            }
        }else{
            output = "failure Only User can perform given operation";
        }

        logManager.logger.log(Level.INFO, Calendar.getInstance().getTime().toString()+"\t"+"Borrow item"+"\t"+output);


        return output;
    }

    public synchronized String returnItem(String userID, String itemID) {
        String response = "";
        String result = "";
        String message = "";

        if (userID.substring(0,3).equals(itemID.substring(0,3))){
            ArrayList<String> arrUserBorrowed = hashMapBorrowListItem.get(itemID);
            if (arrUserBorrowed==null){
                result = "Failure";
                message = "Item not borrowed";
            } else if (arrUserBorrowed.contains(userID)){
                updateHashMapItem(userID,itemID,OPERATION.REMOVE);
                LibraryItem item = hashMapLibrary.get(itemID);
                updateHashMapUser(userID,itemID,OPERATION.REMOVE);
                updateHashMapItem(userID,itemID,OPERATION.REMOVE);
                updateItemQuantity(itemID,1,OPERATION.ADD);
                result = "Success";
                message = "Book returned successfully";
            }else{
                result = "Failure";
                message = "Only User Borrowing the book can return";
            }
        } else {
            String appendedItemID = "R".concat(itemID).concat(",").concat(userID);
            String serverLocation = itemID.substring(0,3);
            try{
                UDPClient client = new UDPClient(Replica1.serverRepository.get(serverLocation),appendedItemID);
                client.run();
                if (client.isBookReturned.equals("true")){
                    result = "Success";
                    message = "Book returned successfully";
                }else{
                    result = "Failure";
                    message = "Only User of borrowed the book can return";
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            updateHashMapUser(userID,itemID,OPERATION.ADD);
        }

        logManager.logger.log(Level.INFO, Calendar.getInstance().getTime().toString()+"\t"+"Return Item"+"\t"+result+"\t"+message);

        return result+ " "+ response+ " "+ message;
    }

    public synchronized String exchangeItem(String studentID, String newItemID, String oldItemID) {
        String output = "";
        String message = "";
        ArrayList<String> arrBookBorrowed = hashMapBorrowListUser.get(studentID);
        if (arrBookBorrowed != null){
            if (arrBookBorrowed.contains(oldItemID)){
                String response = findItem(studentID,"",newItemID);
                if (response.contains("Success")){
                    String responseBorrow = borrowItem(studentID,newItemID);
                    if (responseBorrow.contains("Success")){
                        //i borrow first and then return since return would almost never fail but borrow can fail.
                        String responseReturn = returnItem(studentID,oldItemID);
                        output = "Success";
                        message = "Exchange item successfull";
                    }else{
                        output = "Failure";
                        message = "Borrow item failed";
                    }
                }else{
                    output = "Failure";
                    message = "Item not available to borrow";
                }
            }else{
                output = "Failure";
                message = "User has not borrowed the old book";
            }
        }else {
            output = "Failure";
            message = "User has not borrowed the old book";
        }
        logManager.logger.log(Level.INFO, Calendar.getInstance().getTime().toString()+"\t"+"Exchanged Item"+"\t"+output+"\t"+message);

        return output+" "+ message;
    }


    //Utility

    public String trimString(Integer fromIndex, Integer toIndex,String id){
        return id.substring(fromIndex,toIndex);
    }

    public void updateItemQuantity(String itemID,int quantity,OPERATION operation){
        LibraryItem item = hashMapLibrary.get(itemID);
        if (operation == OPERATION.ADD){
            item.itemQuantity = item.itemQuantity+quantity;
        }else if (operation == OPERATION.REMOVE){
            item.itemQuantity = item.itemQuantity - quantity;
        }
        hashMapLibrary.put(itemID,item);
    }

    public void updateHashMapUser(String userID,String itemID,OPERATION operation){

        ArrayList<String> arrItem = hashMapBorrowListUser.get(userID);
        if (operation == operation.ADD){
            if (arrItem!= null){
                arrItem.add(itemID);
                hashMapBorrowListUser.put(userID,arrItem);
            }else{
                arrItem = new ArrayList<>();
                arrItem.add(itemID);
                hashMapBorrowListUser.put(userID,arrItem);
            }
        } else if (operation == operation.REMOVE){
            arrItem.remove(itemID);
            hashMapBorrowListUser.put(userID,arrItem);
        }

    }

    public void updateHashMapItem(String userID,String itemID,OPERATION operation){
        ArrayList<String> arrUser = hashMapBorrowListItem.get(itemID);
        if (operation == OPERATION.ADD){
            if (arrUser!=null){
                arrUser.add(userID);
            }else{
                arrUser = new ArrayList<>();
                arrUser.add(userID);
            }
            hashMapBorrowListItem.put(itemID,arrUser);
        } else if (operation == operation.REMOVE){
            arrUser.remove(userID);
            hashMapBorrowListItem.put(itemID,arrUser);
        }

    }
}

