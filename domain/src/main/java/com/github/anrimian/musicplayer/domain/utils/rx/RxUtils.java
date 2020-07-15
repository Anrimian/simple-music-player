package com.github.anrimian.musicplayer.domain.utils.rx;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

public class RxUtils {

    public static <T> Flowable<T> repeatUntilComplete(Maybe<T> maybe) {
        class MaybeCompletedException extends RuntimeException {}

        return maybe.doOnComplete(() -> {
            throw new MaybeCompletedException();
        }).repeat()
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof MaybeCompletedException) {
                        return Flowable.empty();
                    }
                    return Flowable.error(throwable);
                });
    }
}
