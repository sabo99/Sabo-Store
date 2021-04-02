package com.sabo.sabostore.API;

import com.sabo.sabostore.Model.CurrencyRates.CurrencyModel;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APICurrency {

    /**
     * base = EUR
     * Convert EUR to IDR
     * */

    @GET("&symbols=IDR")
    Call<CurrencyModel> getExchangeRatesAPI();
}
