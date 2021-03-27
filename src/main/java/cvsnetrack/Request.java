package cvsnetrack;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

//https://www.cvsnet.co.kr/
//reservation-inquiry/delivery
///index.do?
//dlvry_type= : 배송 종류
// //domestic : 국내
// //day :Postbox 퀵
// //slow : 반값택배
// //international : 국제
// //pickup : 픽업

//invoice_no= : 운송장번호 또는 주문번호 

//srch_type= : pickup의 라디오박스
// // 00 : Non-select
// // 01 : 주문번호
// // 02 : 운송장 번호

//invoice/tracking.do?
//invoice_no : 운송장번호

public class Request {
    protected String invoice_no;
    protected JsonObject info,receiver,sender,latestTrackingDetail;
    JsonElement serviceName, goodsName,carrierName;
    protected JsonArray trackingDetails;

    class InvalidInvoiceNoException extends Exception {

    }

    public Boolean isNumOnly() {
        return invoice_no.matches("^[0-9]+$") ? true : false;
    }

    public void getInfo() throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.cvsnet.co.kr/invoice/tracking.do?invoice_no=" + invoice_no)).build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String str = (str = (str = response.body().toString()).substring(str.indexOf("track"), str.indexOf(";")))
                .replace(str.substring(str.indexOf("track"), str.indexOf("=") + 1), "");
        info = new Gson().fromJson(str, JsonObject.class);

        receiver = info.getAsJsonObject("receiver");
        sender = info.getAsJsonObject("sender");
        trackingDetails = info.getAsJsonArray("trackingDetails");
        serviceName = info.get("serviceName");
        goodsName = info.get("goodsName");
        carrierName = info.get("carrierName");
        latestTrackingDetail = info.getAsJsonObject("latestTrackingDetail");
        return;
    }
}