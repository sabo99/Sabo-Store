package com.sabo.sabostore.API;

import com.sabo.sabostore.Model.CurrencyRates.CurrencyModel;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APICurrency {

    /**
     * base = EUR
     * Convert EUR to IDR
     * */

    @GET("latest?access_key=a7524b3e10cf646ffc464a75c10fa9cb&symbols=IDR")
    Call<CurrencyModel> getExchangeRatesAPI();
}
