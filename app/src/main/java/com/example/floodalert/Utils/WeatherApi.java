package com.example.floodalert.Utils;
import com.example.floodalert.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WeatherApi {

    @GET("weather?id=160260&appid=91d2b72a425510366e6f2778eb847973")
    Call<WeatherResponse> getCurrentWeatherData();
}
