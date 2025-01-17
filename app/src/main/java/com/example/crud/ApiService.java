package com.example.crud;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    Call<ResponseBody> loginUser(
            @Field("email") String email,
            @Field("password") String password
    );


    @FormUrlEncoded
    @POST("signup.php")
    Call<ResponseBody> signupUser(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password,
            @Field("photo") String photo
    );
}
