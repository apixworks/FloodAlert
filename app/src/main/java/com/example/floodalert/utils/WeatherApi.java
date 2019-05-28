package com.example.floodalert.utils;
import com.example.floodalert.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.*;

public interface WeatherApi {

    @GET("weather?id=160260&appid=91d2b72a425510366e6f2778eb847973")
    Call<WeatherResponse> getCurrentWeatherData();

    @FormUrlEncoded
    @POST("user")
    Call<String>  registerUser(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password
    );
}
