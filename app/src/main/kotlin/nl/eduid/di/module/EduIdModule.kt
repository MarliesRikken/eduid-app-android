package nl.eduid.di.module

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nl.eduid.BuildConfig
import nl.eduid.di.EduIdScope
import nl.eduid.di.api.EduIdApi
import nl.eduid.di.assist.AuthenticationAssistant
import nl.eduid.di.auth.TokenAuthenticator
import nl.eduid.di.auth.TokenInterceptor
import nl.eduid.di.auth.TokenProvider
import nl.eduid.di.repository.EduIdRepository
import nl.eduid.di.repository.StorageRepository
import nl.eduid.screens.personalinfo.PersonalInfoRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.tiqr.data.api.response.ApiResponseAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Module which serves the repositories.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object RepositoryModule {

    @Provides
    @Singleton
    internal fun provideEduApi(@EduIdScope retrofit: Retrofit): EduIdApi =
        retrofit.create(EduIdApi::class.java)

    @Provides
    @Singleton
    internal fun providesEduIdRepository(
        api: EduIdApi,
    ) = EduIdRepository(api)

    @Provides
    @Singleton
    internal fun providesPersonalInfoRepository(
        api: EduIdApi,
    ) = PersonalInfoRepository(api)

    @Provides
    @Singleton
    internal fun providesStorageRepository(
        @ApplicationContext context: Context,
    ) = StorageRepository(context)

    @Provides
    @Singleton
    internal fun providesTokenProvider(
        repository: StorageRepository,
        assistant: AuthenticationAssistant,
        @ApplicationContext context: Context,
    ) = TokenProvider(repository, assistant, context)

    @Provides
    @EduIdScope
    fun providesTokenAuthOkHttp(
        tokenAuthenticator: TokenAuthenticator,
        tokenInterceptor: TokenInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        okHttpClient: OkHttpClient,
    ): OkHttpClient {
        val builder = okHttpClient.newBuilder()
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.addInterceptor(tokenInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    internal fun providesAuthenticationAssist(
    ) = AuthenticationAssistant()

    @Provides
    @Singleton
    @EduIdScope
    internal fun provideEduIdRetrofit(
        @EduIdScope client: Lazy<OkHttpClient>, moshi: Moshi,
    ): Retrofit {
        return Retrofit.Builder().callFactory { client.get().newCall(it) }
            .addCallAdapterFactory(ApiResponseAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BuildConfig.ENV_HOST).build()
    }

    @Provides
    @Singleton
    internal fun provideOkHttpClientBuilder(): OkHttpClient {
        return OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).build()
    }
}