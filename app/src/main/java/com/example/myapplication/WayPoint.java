package com.example.myapplication;

public class WayPoint
{
    private String Name;
    private double latitude;
    private double longitude;
    private int type;

    public WayPoint(){
        super();
    }

    public WayPoint(String Name, double latitude, double longitude,int type)
    {
        //super();
        this.Name = Name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }


    public String getName()
    {
        return Name;
    }

    public void setName(String Name)
    {
        this.Name = Name;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public void set(int type) {this.type = type;}

    public int getType() {return type;}

}
