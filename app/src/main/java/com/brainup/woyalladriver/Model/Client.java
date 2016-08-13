package com.brainup.woyalladriver.Model;

/**
 * Created by Roger on 8/13/2016.
 */
public class Client {
    String clientName,clientPhoneNumber,clientGpsLatitude,clientGpsLongtude,orderId,status;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhoneNumber() {
        return clientPhoneNumber;
    }

    public void setClientPhoneNumber(String clientPhoneNumber) {
        this.clientPhoneNumber = clientPhoneNumber;
    }

    public String getClientGpsLatitude() {
        return clientGpsLatitude;
    }

    public void setClientGpsLatitude(String clientGpsLatitude) {
        this.clientGpsLatitude = clientGpsLatitude;
    }

    public String getClientGpsLongtude() {
        return clientGpsLongtude;
    }

    public void setClientGpsLongtude(String clientGpsLongtude) {
        this.clientGpsLongtude = clientGpsLongtude;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
