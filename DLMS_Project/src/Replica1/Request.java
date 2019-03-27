package com.Server;

import java.util.Comparator;

public class Request {
    public String sequencerID;
    public String FEAddress;
    public String serverImplementation;
    public String request;

    public Request(String sequencerID,String FEAddress,String serverImplementation,String request){
        this.sequencerID = sequencerID;
        this.FEAddress = FEAddress;
        this.serverImplementation = serverImplementation;
        this.request = request;
    }




}
