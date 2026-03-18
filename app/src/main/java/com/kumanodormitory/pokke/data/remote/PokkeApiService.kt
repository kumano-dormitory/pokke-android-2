package com.kumanodormitory.pokke.data.remote

import com.kumanodormitory.pokke.data.remote.dto.CallNotifyRequest
import com.kumanodormitory.pokke.data.remote.dto.CallNotifyResponse
import com.kumanodormitory.pokke.data.remote.dto.SyncPullRequest
import com.kumanodormitory.pokke.data.remote.dto.SyncPullResponse
import com.kumanodormitory.pokke.data.remote.dto.SyncPushRequest
import com.kumanodormitory.pokke.data.remote.dto.SyncPushResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PokkeApiService {

    @POST("api/v1/sync/pull")
    suspend fun syncPull(
        @Body body: SyncPullRequest
    ): Response<SyncPullResponse>

    @POST("api/v1/sync/push")
    suspend fun syncPush(
        @Body body: SyncPushRequest
    ): Response<SyncPushResponse>

    @POST("api/v1/call/notify")
    suspend fun callNotify(
        @Body body: CallNotifyRequest
    ): Response<CallNotifyResponse>

    @GET("api/v1/health")
    suspend fun health(): Response<Unit>
}
