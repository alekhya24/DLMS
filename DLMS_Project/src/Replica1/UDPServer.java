package Replica1;

import Util.Constants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;

public class UDPServer extends Thread{
    private DatagramSocket serverSocket;
    private Constants.ServerLocation serverLocation;
    private DatagramPacket recievedPacket;
    private DLMSServerImplementation serverImplementation;

    public UDPServer(DatagramPacket packet, DLMSServerImplementation serverImplementation){
        recievedPacket = packet;
        this.serverImplementation = serverImplementation;
        try{
              this.serverSocket = new DatagramSocket();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        byte[] responseData;
        responseData = "".getBytes();
        try{
            String inputPacket = new String(recievedPacket.getData()).trim();
            String operation = inputPacket.substring(0,1);
            String userData = inputPacket.substring(1);
            switch (operation){
                case "B":
                     String[] arrString = userData.split(",");
                     responseData = manageBorrow(arrString[0],arrString[1]).getBytes();
                    break;
                case "R":
                    String[] arrString1 = userData.split(",");
                    responseData = manageItemReturn(arrString1[0],arrString1[1]).getBytes();
                    break;
                case "C":
                    responseData = findItem(userData).getBytes();
                    break;
            }
            serverSocket.send(new DatagramPacket(responseData,responseData.length,recievedPacket.getAddress(),recievedPacket.getPort()));

        } catch (Exception e){

        }
    }

    String manageBorrow(String itemID,String userID){
        String output= "";
        if (borrowItem(itemID,userID)){
            output = "true";
        } else {
            output = "false";
        }
        return output;
    }


    String manageItemReturn(String itemID,String userID){
        String result= "";
        String message="";
        String output = "";
        if (returnBook(itemID,userID)){
            result = "Sucess";
            message = " Book returned Successfully";
            output = "true";
        } else {
            result = "Failure";
            message = "Only User of borrowed the book can return";
            output = "false";
        }

        serverImplementation.logManager.logger.log(Level.INFO, Calendar.getInstance().getTime().toString()+"\t"+"Return Item"+"\t"+userID+"\t"+itemID+"\t"+"\t"+result+"\t"+message);

        return output;
    }

    String responseSearch(String searchItem,boolean isItemName){
        String response = "";
        if (isItemName){
            for (String key: serverImplementation.hashMapLibrary.keySet()){
                LibraryItem item = serverImplementation.hashMapLibrary.get(key);
                if (item.itemName.equalsIgnoreCase(searchItem)){
                    response = response.concat(key).concat("\t").concat(Integer.toString(item.itemQuantity)).concat("\n");
                }
            }
        } else {
            for (String key: serverImplementation.hashMapLibrary.keySet()){
                LibraryItem item = serverImplementation.hashMapLibrary.get(key);
                if (key.equalsIgnoreCase(searchItem)){
                    response = response.concat(key).concat("\t").concat(Integer.toString(item.itemQuantity)).concat("\n");
                }
            }
        }
        return response;
    }

    String findItem(String searchData){
        String[] arrSearchData = searchData.split(",");
        boolean isItemName = false;
        String searchItem = "";
        if (arrSearchData[0].equals("")){
            searchItem = arrSearchData[1];
            isItemName = false;
        }else{
            searchItem = arrSearchData[0];
            isItemName = true;
        }
        String response = "";
        String result = "";
        response = responseSearch(searchItem,isItemName);
        if (response.equals("")){
            response = "";
            result = "failure";
        }else{
            result = "Success";
        }
        serverImplementation.logManager.logger.log(Level.INFO, Calendar.getInstance().getTime().toString()+"\t"+"Find Item"+"\t"+searchItem+"\t"+result+"\t"+response);
        return response;
    }

    boolean returnBook(String itemID,String userID){
        ArrayList<String> arrUserID = serverImplementation.hashMapBorrowListItem.get(itemID);
        if (arrUserID.contains(userID)){
            serverImplementation.updateItemQuantity(itemID,1, DLMSServerImplementation.OPERATION.ADD);
            serverImplementation.updateHashMapItem(userID,itemID, DLMSServerImplementation.OPERATION.REMOVE);
            serverImplementation.manageWaitListOnAdd(itemID);
            return true;
        }else{
            return false;
        }
    }

    boolean borrowItem(String itemID,String userID){
       LibraryItem item = serverImplementation.hashMapLibrary.get(itemID);
       String result ="";
       String message = "";
       if (item.itemQuantity>0){
           serverImplementation.updateItemQuantity(itemID,1, DLMSServerImplementation.OPERATION.REMOVE);
           serverImplementation.updateHashMapItem(userID,itemID, DLMSServerImplementation.OPERATION.ADD);
           result = "Success";
           message = "User from"+userID.substring(0,3)+" borrowed book";
           serverImplementation.logManager.logger.log(Level.INFO, Calendar.getInstance().getTime().toString()+"\t"+"Borrow Item"+"\t"+userID+"\t"+itemID+"\t"+item.itemName+"\t"+result+"\t"+message);
           return true;
       }
       return false;
    }

}
