package com.tasomaniac.openwith.resolver;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.chooser.ChooserTarget;

import com.tasomaniac.openwith.App;
import com.tasomaniac.openwith.R;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.M)
public class ChooserTargetService extends android.service.chooser.ChooserTargetService {

    private static final String EXTRA_FROM_DIRECT_SHARE = "EXTRA_FROM_DIRECT_SHARE";

    @Override
    public List<ChooserTarget> onGetChooserTargets(ComponentName targetActivityName,
                                                   IntentFilter matchedFilter) {
        sendAnalyticsEvent();

        ComponentName componentName = new ComponentName(getPackageName(),
                ResolverActivity.class.getCanonicalName());

        ArrayList<ChooserTarget> targets = new ArrayList<>();
        targets.add(new ChooserTarget(
                getString(R.string.open_with),
                Icon.createWithResource(this, R.mipmap.ic_launcher),
                0.2f,
                componentName,
                getBundleExtra()
        ));
        return targets;
    }

    private Bundle getBundleExtra() {
        Bundle extras = new Bundle(1);
        extras.putBoolean(EXTRA_FROM_DIRECT_SHARE, true);
        return extras;
    }

    private void sendAnalyticsEvent() {
        App.getApp(this).getAnalytics().sendEvent(
                "Direct Share",
                "Shown",
                "true"
        );
    }

    public static boolean isFromDirectShare(Intent intent) {
        return intent.getBooleanExtra(EXTRA_FROM_DIRECT_SHARE, false);
    }
}
