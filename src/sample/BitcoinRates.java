package sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BitcoinRates {
    String lastJson;
    public int intValue;
    public String stringValue;
    //    private GsonBuilder builder;
    private float lastRates = 0.0F;
    private long lastTime = 0;
    private URL url;
    private HttpURLConnection conn;

    //    private Gson gson;
//    private BitcoinMarketInfo bitcoinMarketInfo;
    public BitcoinRates() {
//        bitcoinMarketInfo = new BitcoinMarketInfo();
//        builder= new GsonBuilder();
//        gson = new GsonBuilder().setPrettyPrinting().create();
        lastJson = "";
        try {
            url = new URL("https://api.coindesk.com/v1/bpi/currentprice/USD.json");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : "
                        + conn.getResponseCode());
            }

        } catch (Exception e) {
            System.out.println("DUPA");
            System.out.println("Exception in NetClientGet:- " + e);
        }
    }

    public String getJson() throws IOException {
        if (conn == null)
            return "";
        if (System.currentTimeMillis() - lastTime < 60000)
            return lastJson;

        InputStreamReader in = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(in);
        lastJson = br.readLine();
        return lastJson;

//        builder.setPrettyPrinting();
//        builder.generateNonExecutableJson();
//        String stringJson = "{\"time\":{\"updated\":\"Sep 18, 2013 17:27:00 UTC\",\"updatedISO\":\"2013-09-18T17:27:00+00:00\"},\"disclaimer\":\"This data was produced from the CoinDesk Bitcoin Price Index. Non-USD currency data converted using hourly conversion rate from openexchangerates.org\",\"bpi\":{\"USD\":{\"code\":\"USD\",\"rate\":\"126.5235\",\"description\":\"United States Dollar\",\"rate_float\":126.5235}}}";
//        Gson gson = builder.create();
//        BitcoinMarketInfo bitcoinMarketInfo = new Gson().fromJson(stringJson, BitcoinMarketInfo.class);
//        BitcoinMarketInfo bitcoinMarketInfo = gson.fromJson(stringJson, BitcoinMarketInfo.class);
//        lastTime = System.nanoTime();
//        return bitcoinMarketInfo.getRate_float();
    }

    public void disconnect() {
        conn.disconnect();
    }
}