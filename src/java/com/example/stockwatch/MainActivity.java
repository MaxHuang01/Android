package com.example.stockwatch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,View.OnLongClickListener,SwipeRefreshLayout.OnRefreshListener {
    private String TAG = "mainThread";
    private int MAKE_DATA_CODE = 1;
    private RecyclerView recyclerView;
    private final List<Stock> stockList = new ArrayList<>();//main content
    //public Stock testStock = new Stock("appl","apple",20.0,30.0,5.0);

    private StockAdapter sAdapter;
    private SwipeRefreshLayout swiper;
    private String choice;
    private String URL = "http://www.marketwatch.com/investing/stock/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        sAdapter = new StockAdapter(stockList,this);
        recyclerView.setAdapter(sAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //stockList.add(testStock);
        sAdapter.notifyDataSetChanged();

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(this);

        SymbolNameDownloader rd = new SymbolNameDownloader();
        new Thread(rd).start();

        readJSONData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuIn){
        getMenuInflater().inflate(R.menu.main_menu,menuIn);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_add:{
                makeStockDialog();
                //Toast.makeText(this,"add-clicked",Toast.LENGTH_LONG).show();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        clickStock(v);
    }

    @Override
    public boolean onLongClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.delete);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stockList.remove(pos);
                sAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.setMessage("Delete " + stockList.get(pos).getStockCode() + "?");
        builder.setTitle("Delete Selection");

        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    @Override
    public void onRefresh() {
        if (!checkNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Content Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            swiper.setRefreshing(false);
            return;
        }
        refresh(stockList);
        //Toast.makeText(this, "swipe refresh", Toast.LENGTH_SHORT).show();

    }

    public void addStock(Stock stock) {
        if (stock == null) {
            //badDataAlert(choice);
            return;
        }

        if (stockList.contains(stock)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(stock.getStockCode() + " is already displayed");
            builder.setTitle("Duplicate stock");
            builder.setIcon(R.drawable.error);

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        stockList.add(stock);
        Collections.sort(stockList);
        sAdapter.notifyDataSetChanged();

    }

    private void doSelection(String sym) {//add selection to main act
        //Toast.makeText(this,"doSelection-ran",Toast.LENGTH_LONG).show();
        Log.d(TAG, "doSelection: ");
        String[] data = sym.split("-");
        StockDownloader stockDownloader = new StockDownloader(this,data[0].trim(),false);
        new Thread(stockDownloader).start();
    }

    private void makeStockDialog() {

        if (!checkNetworkConnection()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Network Connection");
            builder.setMessage("Content Cannot Be Added Without A Network Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);


        builder.setView(et);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                choice = et.getText().toString().trim();

                final ArrayList<String> results = SymbolNameDownloader.findMatches(choice);

                if (results.size() == 0) {
                    doNoAnswer(choice);
                } else if (results.size() == 1) {
                    doSelection(results.get(0));
                } else {
                    String[] array = results.toArray(new String[0]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Make a selection");
                    builder.setItems(array, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String symbol = results.get(which);
                            doSelection(symbol);
                        }
                    });
                    builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog2 = builder.create();
                    dialog2.show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.setMessage("Please enter a Symbol or Name:");
        builder.setTitle("Stock Selection");

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private boolean checkNetworkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void doNoAnswer(String symbol) {//當找不到使用者輸入的股票時
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("No data for specified symbol/name");
        builder.setTitle("No Data Found: " + symbol);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void writeJSONData() {

        try {
            FileOutputStream fos = getApplicationContext().
                    openFileOutput(getString(R.string.data_file), Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            writer.setIndent("  ");
            writer.beginArray();
            for (Stock s : stockList) {
                writer.beginObject();


                writer.name("stockSymbol").value(s.getStockCode());
                writer.name("company").value(s.getCompany());
                writer.name("latestPrice").value(s.getPrice());
                writer.name("change").value(s.getChange());
                writer.name("changePercent").value(s.getChangePercent());
                writer.endObject();
            }
            writer.endArray();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "writeJSONData: " + e.getMessage());
        }
    }

    private void readJSONData() {

        try {
            FileInputStream fis = getApplicationContext().
                    openFileInput(getString(R.string.data_file));

            // Read string content from file
            byte[] data = new byte[fis.available()]; // this technique is good for small files
            int loaded = fis.read(data);
            Log.d(TAG, "readJSONData: Loaded " + loaded + " bytes");
            fis.close();
            String json = new String(data);

            // Create JSON Array from string file content
            JSONArray noteArr = new JSONArray(json);
            for (int i = 0; i < noteArr.length(); i++) {
                JSONObject cObj = noteArr.getJSONObject(i);


                String stockSymbol = cObj.getString("stockSymbol");
                String company = cObj.getString("company");
                double latestPrice = cObj.getDouble("latestPrice");
                double change = cObj.getDouble("change");
                double changePercent = cObj.getDouble("changePercent");

                Stock s = new Stock(stockSymbol,company,latestPrice,change,changePercent);
                stockList.add(s);
            }
            sAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        writeJSONData();
    }

    private void refresh(List<Stock> stockList){
        int size = stockList.size();
        List<Stock> tempList = new ArrayList<>();
        for(int i = 0;i<size;i++){

            Stock s = stockList.get(i);
            tempList.add(s);
            //stockList.remove(i);
        }

        for(int i = 0;i<size;i++){
            Log.d(TAG, "list size:"+stockList.size());
            stockList.remove(0);
        }

        for(int i = 0;i<size;i++){
            Stock s = tempList.get(i);

            StockDownloader stockDownloader = new StockDownloader(this,s.getStockCode(),true);
            new Thread(stockDownloader).start();
        }
        swiper.setRefreshing(false);


    }

    public void clickStock(View v){
        String URLToUse;
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock s = stockList.get(pos);
        String symbol = s.getStockCode();
        URLToUse = URL+symbol;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URLToUse));
        startActivity(intent);
    }
}