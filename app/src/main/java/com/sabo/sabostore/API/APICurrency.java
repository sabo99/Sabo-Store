package com.sabo.sabostore.API;

import com.sabo.sabostore.Model.CurrencyRates.CurrencyModel;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APICurrency {

    /**
     * base = EUR
     * Convert EUR to IDR
     * */

    @GET("latest?access_key=651f5d0bb8c987122705903d8fb3d6c7&symbols=IDR")
    Call<CurrencyModel> getExchangeRatesAPI();
}
