package com.flightontrack.Entities;

public class EntityAcft {
    public String AcftNum;
    public String AcftTagId;
    public String AcftName;

    public EntityAcft(String p1,String p2,String p3){
        AcftNum=p1;
        AcftName=p2;
        AcftTagId=p3;
    }
    public EntityAcft(String p1,String p2){
        AcftNum=p1;
        AcftName=p2;
    }
}
