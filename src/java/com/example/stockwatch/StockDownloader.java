package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StockDownloader implements Runnable {

    private String API_KEY = "pk_7349065aff4d466b91e074e462e31080";
    private String STOCK_SYMBOL;
    private String TAG = "Thread";
    private String QUERY_FORMAT;
    private MainActivity mainActivity;
    private Boolean isRefresh;

    public StockDownloader(MainActivity mainActivity, String STOCK_SYMBOL,Boolean isRefresh) {
        this.mainActivity = mainActivity;
        this.STOCK_SYMBOL = STOCK_SYMBOL;
        this.isRefresh = isRefresh;
        this.QUERY_FORMAT = "https://cloud.iexapis.com/stable/stock/"+STOCK_SYMBOL+
                "/quote?token=pk_7349065aff4d466b91e074e462e31080";
    }

    @Override
    public void run() {


        Uri.Builder uriBuilder = Uri.parse(QUERY_FORMAT).buildUpon();
        //uriBuilder.appendQueryParameter("fullText", "true");
        String urlToUse = uriBuilder.toString();

        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "run: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
            return;
        }
        process(sb.toString());
    }

    private void process(String s) {
        try {

            /*mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mainActivity,"process running",Toast.LENGTH_LONG).show();
                }
            });*/
            JSONObject jStock = new JSONObject(s);



            String symbol = jStock.getString("symbol");
            String companyName = jStock.getString("companyName");
            /*Double latestPrice = jStock.getDouble("latestPrice");
            Double change = jStock.getDouble("change");
            Double changePercent = jStock.getDouble("changePercent");*/

            String latestP = jStock.getString("latestPrice");
            Double latestPrice = 0.0;
            if(!latestP.trim().isEmpty())
                latestPrice = Double.parseDouble(latestP);

            String cg = jStock.getString("change");
            Double change = 0.0;
            if(!latestP.trim().isEmpty())
                change = Double.parseDouble(cg);

            String cgP = jStock.getString("changePercent");
            Double changePercent = 0.0;
            if(!latestP.trim().isEmpty())
                changePercent = Double.parseDouble(cgP);

            /*StringBuilder codes = new StringBuilder();
            JSONArray jCodes = jStock.getJSONArray("callingCodes");
            for (int j = 0; j < jCodes.length(); j++) {
                codes.append(jCodes.get(j)).append(" ");
            }

            StringBuilder borders = new StringBuilder();
            JSONArray jBorders = jStock.getJSONArray("borders");
            for (int j = 0; j < jBorders.length(); j++) {
                borders.append(jBorders.get(j)).append(" ");
            }*/

            /*final Country country = new Country(name, alpha3Code,
                    capital, population, region, subRegion, area,
                    citizen, codes.toString(), borders.toString());*/

            final Stock stock = new Stock(symbol,companyName,latestPrice,change,changePercent);

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.addStock(stock);
                }
            });

        } catch (Exception e) {
            //Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
