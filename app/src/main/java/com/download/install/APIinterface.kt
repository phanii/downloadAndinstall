package com.download.install

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url


interface APIinterface {

    @Streaming
    @GET//("/phanii/BBP/blob/master/app/BBP.apk")
    fun getREspone(@Url url: String): Call<ResponseBody>
}
