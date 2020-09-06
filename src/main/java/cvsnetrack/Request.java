package cvsnetrack;

import java.io.IOException;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import lombok.NonNull;

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

public class Request {
    public interface CvsTrack {
        String getMatch(String keyFromMessage, String keyFromDlvry_Type);
    }

    public static final String mainURI = "https://www.cvsnet.co.kr/";
    public static final String subDirectory = "reservation-inquiry/delivery/index.do?";
    public static final String firefoxUserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0";
    public final HashMap<String, String> dlvry_type = new HashMap<String, String>() {
        {
            put("국내", "domestic");
            put("퀵", "day");
            put("반값", "slow");
            put("국제", "international");
            put("픽업", "pickup");
        }
    };

    public Document getCvsTrackDocument(@NonNull String invoice_no, @NonNull String dlvry_type, String srch_type)
            throws IOException {
        return Jsoup.connect(mainURI + subDirectory + "dlvry_type=" + getDlvryType(dlvry_type) + "&invoice_no="
                + invoice_no + (srch_type != null ? "&srch_type=" + srch_type : "")).userAgent(firefoxUserAgent).get();
    }

    public String getCvsTrackNowStatus(@NonNull Document cvsDocument) throws IOException {
        Elements cvsTrackImagElements = cvsDocument.getElementsByClass("onImage");
        for (int i = 0; i < cvsTrackImagElements.size(); i++) {
            if (cvsTrackImagElements.get(i).parentNode().attr("class").toString().contains("on"))
                return cvsTrackImagElements.get(i).parentNode().childNode(2).toString();
            else
                continue;
        }
        return null;
    }

    public String getInvoiceNo(@NonNull Document cvsDocument) {
        return cvsDocument.getElementsByClass(".num").get(0).childNode(0).toString();
    }

    public Elements getRow(@NonNull Document cvsDocument) {
        return cvsDocument.getElementsByAttributeValue("scope", "row");
    }

    public String getDlvryType(@NonNull String dlvry_type) {
        return this.dlvry_type.get(dlvry_type);
    }

    public HashMap<String, String> getRowMap(@NonNull Elements rowElements) {
        rowElements.trimToSize();
        HashMap<String, String> rowMap = new HashMap<String, String>();
        for (int i = 0; i < rowElements.size(); i++) {
            for (int j = 1; j < rowElements.get(i).parentNode().childNodeSize(); j += 4) {
                if (!(rowElements.get(i).parentNode().childNode(j).hasAttr("scope")))
                    continue;
                rowMap.put(rowElements.get(i).parentNode().childNode(j).childNode(0).toString(),
                        rowElements.get(i).parentNode().childNode(j + 2).childNodeSize() == 0 ? "해당 정보 없음"
                                : rowElements.get(i).parentNode().childNode(j + 2).childNode(0).toString()
                                        .replaceAll("(^\\p{Z}+|\\p{Z}+$)", ""));

            }
        }
        return rowMap;
    }

}