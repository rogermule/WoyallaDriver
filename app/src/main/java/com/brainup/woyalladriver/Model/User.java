package com.brainup.woyalladriver.Model;

/**
 * Created by Roger on 8/4/2016.
 */
public class User {

    private String name, phoneNumber,licencePlateNumber,driverLicenceIdNo,carModelDescription;
    int serviceModel,status,owner;
    double gpsLatitude,gpsLongitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLicencePlateNumber() {
        return licencePlateNumber;
    }

    public void setLicencePlateNumber(String licencePlateNumber) {
        this.licencePlateNumber = licencePlateNumber;
    }

    public String getDriverLicenceIdNo() {
        return driverLicenceIdNo;
    }

    public void setDriverLicenceIdNo(String driverLicenceIdNo) {
        this.driverLicenceIdNo = driverLicenceIdNo;
    }

    public String getCarModelDescription() {
        return carModelDescription;
    }

    public void setCarModelDescription(String carModelDescription) {
        this.carModelDescription = carModelDescription;
    }

    public int getServiceModel() {
        return serviceModel;
    }

    public void setServiceModel(int serviceModel) {
        this.serviceModel = serviceModel;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getGpsLatitude() {
        return gpsLatitude;
    }

    public void setGpsLatitude(double gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public double getGpsLongitude() {
        return gpsLongitude;
    }

    public void setGpsLongitude(double gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }
}
