/*
 * Copyright (c) 2010-2021 SURFnet bv
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of SURFnet bv nor the names of its contributors
 *    may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package nl.eduid

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import org.tiqr.data.model.TiqrConfig
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
open class EduIdApplication : Application() {
    @Inject
    internal lateinit var imageOkHttpClient: OkHttpClient.Builder

    @Inject
    internal lateinit var appLifecycleObserver: ApplicationLifecycleObserver

    override fun onCreate() {
        super.onCreate()

        TiqrConfig.initialize(this)
        // Setup Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())

        }

        Coil.setImageLoader(ImageLoader.Builder(context = this)
            .crossfade(enable = true)
            .okHttpClient {
                imageOkHttpClient
                    .build()
            }
            .build())

        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }
}
