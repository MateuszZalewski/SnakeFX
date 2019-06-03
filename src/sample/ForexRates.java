package sample;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForexRates {
    private double lastRates;
    private double lastRatesChange;
    private URL url;
    private HttpURLConnection conn;
    private long lastTime = 0;
    private Gson gson;

    public ForexRates() {
        lastRates = 0.0F;
        lastRatesChange = 0.0F;
        gson = new Gson();
        try {
            url = new URL("https://www.freeforexapi.com/api/live?pairs=EURUSD");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : "
                        + conn.getResponseCode());
            }

        } catch (Exception e) {
            System.out.println("Exception in NetClientGet:- " + e);
        }
    }

    public double getRatesChange() {
//        if(System.currentTimeMillis() - lastTime < 10000)
//            return lastRatesChange;
        try {
            url = new URL("https://www.freeforexapi.com/api/live?pairs=EURUSD");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP Error code : "
                        + conn.getResponseCode());
            }

        } catch (Exception e) {
            System.out.println("Exception in NetClientGet:- " + e);
        }
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(in);
        String jsonString = "";
        try {
            jsonString = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ForexJson forexJson = gson.fromJson(jsonString, ForexJson.class);
        if (lastRates != forexJson.getRate()) {
            lastRatesChange = forexJson.getRate() - lastRates;
            lastRates = forexJson.getRate();
        }
        return lastRatesChange;
    }
}
