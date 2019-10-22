package com.download.install

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiBuilder {

    private var BASE_URL = "https://github.com/"


    private var retrofit: Retrofit? = null


    /**
     * configure the Builder for API calls
     */
    fun create(): APIinterface {
        BASE_URL.let {

            retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client1)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return retrofit?.create(APIinterface::class.java)!!
        }

    }

    /**
     * logging
     */
    private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }
    private val client1: OkHttpClient = OkHttpClient.Builder().apply {
        this.readTimeout(30, TimeUnit.SECONDS)
        this.connectTimeout(5, TimeUnit.SECONDS)
        this.writeTimeout(20, TimeUnit.SECONDS)
        this.addInterceptor(interceptor)


    }.build()

    //}


}
