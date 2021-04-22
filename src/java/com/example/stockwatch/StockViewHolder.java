package com.example.stockwatch;


import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {

    public TextView stockCode;
    TextView company;
    TextView price;
    TextView wave;
    TextView arrow;
    TextView changePercent;

    public StockViewHolder(@NonNull View itemView) {
        super(itemView);
        stockCode = itemView.findViewById(R.id.stock_code_id);
        company = itemView.findViewById(R.id.company_id);
        wave = itemView.findViewById(R.id.wave_id);
        price = itemView.findViewById(R.id.price_id);
        arrow = itemView.findViewById(R.id.arrow_id);
        changePercent = itemView.findViewById(R.id.changePercent_id);
    }
}
