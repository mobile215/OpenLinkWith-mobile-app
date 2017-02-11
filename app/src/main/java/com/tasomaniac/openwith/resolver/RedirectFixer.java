package com.tasomaniac.openwith.resolver;

import com.tasomaniac.openwith.rx.SchedulingStrategy;

import javax.inject.Inject;
import java.io.IOException;

import io.reactivex.Single;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class RedirectFixer {

    private final OkHttpClient client;
    private final SchedulingStrategy scheduling;

    private Call call;

    @Inject
    public RedirectFixer(OkHttpClient client, SchedulingStrategy scheduling) {
        this.client = client.newBuilder()
                .followRedirects(false)
                .build();
        this.scheduling = scheduling;
    }

    public Single<HttpUrl> followRedirects(final HttpUrl url) {
        return Single
                .fromCallable(() -> doFollowRedirects(url))
                .onErrorReturnItem(url)
                .doOnDispose(this::cancel)
                .compose(scheduling.applyToSingle());
    }

    private HttpUrl doFollowRedirects(HttpUrl url) throws IOException {
        call = client.newCall(request(url));
        String locationHeader = call.execute().header("Location");

        HttpUrl redirectedUrl = locationHeader == null ? null : HttpUrl.parse(locationHeader);
        if (redirectedUrl == null) {
            return url;
        } else {
            return doFollowRedirects(redirectedUrl);
        }
    }

    private Request request(HttpUrl httpUrl) {
        return new Request.Builder()
                .url(httpUrl)
                .head()
                .build();
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

}
