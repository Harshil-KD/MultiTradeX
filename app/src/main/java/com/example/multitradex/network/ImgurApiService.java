package com.example.multitradex.network;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ImgurApiService {

    @Multipart
    @POST("image")
    Call<ResponseBody> uploadImage(
            @Header("Authorization") String auth,
            @Part MultipartBody.Part image
    );
}
