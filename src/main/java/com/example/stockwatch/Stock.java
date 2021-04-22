package com.example.stockwatch;

import java.io.Serializable;

public class Stock implements Serializable,Comparable<Stock> {
    private String stockCode;
    private String company;
    private Double price;
    private Double change;
    private Double changePercent;

    public Stock(String stockCode, String company, Double price,Double change, Double changePercent) {
        this.stockCode = stockCode;
        this.company = company;
        this.price = price;
        this.changePercent = changePercent;
        this.change = change;
    }


    public Double getChange() {
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return stockCode.equals(stock.stockCode) &&
                company.equals(stock.company);
    }

    @Override
    public int compareTo(Stock stock) {
        return stockCode.compareTo(stock.getStockCode());
    }
}
