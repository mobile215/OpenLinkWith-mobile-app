package com.tasomaniac.openwith.resolver.preferred

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.tasomaniac.openwith.data.PreferredApp
import com.tasomaniac.openwith.data.PreferredAppDao
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import io.reactivex.Maybe
import javax.inject.Inject

internal class PreferredResolver @Inject constructor(
    private val packageManager: PackageManager,
    private val appDao: PreferredAppDao
) {

    fun resolve(uri: Uri): Maybe<PreferredDisplayActivityInfo> {
        val host = uri.host
        if (host.isEmpty()) return Maybe.empty()

        return appDao.preferredAppByHost(host)
            .flatMap { app ->
                Maybe.fromCallable {
                    app.resolve()?.let {
                        PreferredDisplayActivityInfo(app, it)
                    }
                }
            }
    }

    private fun PreferredApp.resolve(): DisplayActivityInfo? {
        val intent = Intent().setComponent(componentName)
        val ri = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return ri?.let {
            DisplayActivityInfo(
                it.activityInfo,
                it.loadLabel(packageManager),
                null
            )
        }
    }
}