package com.sabo.sabostore.API;

import com.sabo.sabostore.Model.CurrencyRates.CurrencyModel;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APICurrency {

    /**
     * base = USD
     * Convert IDR to USD
     * */

    @GET("latest?base=USD")
    Call<CurrencyModel> getExchangeRatesAPI();
}
