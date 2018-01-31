package com.sesi.parkingmeter.data.model;

public class Suscriptions {

    private String sSku;
    private String sTitle;
    private String sDescription;
    private String sPrice;
    private String sPriceCurrencyCode;

    public Suscriptions(String sSku, String sTitle, String sDescription, String sPrice, String sPriceCurrencyCode){
        this.sSku = sSku;
        this.sTitle = sTitle;
        this.sDescription = sDescription;
        this.sPrice = sPrice;
        this.sPriceCurrencyCode = sPriceCurrencyCode;
    }
    public String getsSku() {
        return sSku;
    }

    public String getsTitle() {
        return sTitle;
    }

    public String getsDescription() {
        return sDescription;
    }

    public String getsPrice() {
        return sPrice;
    }

    public String getsPriceCurrencyCode() {
        return sPriceCurrencyCode;
    }
}
