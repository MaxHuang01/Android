package com.example.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private List<Stock> stockList;
    private MainActivity mainAct;

    public StockAdapter(List<Stock> stockList, MainActivity mainAct) {
        this.stockList = stockList;
        this.mainAct = mainAct;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list_row,parent,false);
        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);


        holder.stockCode.setText(stock.getStockCode());
        holder.company.setText(stock.getCompany());
        holder.wave.setText(String.format("%.2f",stock.getChange()));
        holder.price.setText(stock.getPrice().toString());
        holder.changePercent.setText("("+String.format("%.2f",stock.getChangePercent())+"%"+")");

        if(Double.parseDouble(holder.wave.getText().toString())<0){
            holder.stockCode.setTextColor(Color.RED);
            holder.company.setTextColor(Color.RED);
            holder.wave.setTextColor(Color.RED);
            holder.price.setTextColor(Color.RED);
            holder.arrow.setText("\u25bc");
            holder.arrow.setTextColor(Color.RED);
            holder.changePercent.setTextColor(Color.RED);
        }else
            {
                holder.stockCode.setTextColor(Color.GREEN);
                holder.company.setTextColor(Color.GREEN);
                holder.wave.setTextColor(Color.GREEN);
                holder.price.setTextColor(Color.GREEN);
                holder.arrow.setText("\u25b2");
                holder.arrow.setTextColor(Color.GREEN);
                holder.changePercent.setTextColor(Color.GREEN);

            }

    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
