package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

/**
 * Created on 17.02.2018.
 */

public class SimpleDiffItemCallback<T> extends DiffUtil.ItemCallback<T> {

    private final ContentCheckFunction<T> contentCheckFunction;
    private final PayloadFunction<T> payloadFunction;

    public SimpleDiffItemCallback() {
        this(new EqualContentCheckFunction<>());
    }

    public SimpleDiffItemCallback(ContentCheckFunction<T> contentCheckFunction) {
        this(contentCheckFunction, new PayloadDefaultFunction<>());
    }

    /**
     * if you use this constructor, payload function must return null or empty only if no changes has detected
     */
    public SimpleDiffItemCallback(PayloadFunction<T> payloadFunction) {
        this(new PayloadContentCheckFunction<>(payloadFunction), payloadFunction);
    }

    public SimpleDiffItemCallback(ContentCheckFunction<T> contentCheckFunction,
                                  PayloadFunction<T> payloadFunction) {
        this.contentCheckFunction = contentCheckFunction;
        this.payloadFunction = payloadFunction;
    }

    @Override
    public boolean areItemsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areContentsTheSame(@NonNull T oldItem, @NonNull T newItem) {
        return contentCheckFunction.areContentsTheSame(oldItem, newItem);
    }

    @Nullable
    @Override
    public Object getChangePayload(@NonNull T oldItem, @NonNull T newItem) {
        return payloadFunction.getChangePayload(oldItem, newItem);
    }

    public static class EqualContentCheckFunction<T> implements ContentCheckFunction<T> {

        @Override
        public boolean areContentsTheSame(T oldItem, T newItem) {
            return oldItem.equals(newItem);
        }
    }

    public static class PayloadContentCheckFunction<T> implements ContentCheckFunction<T> {

        private final PayloadFunction<T> payloadFunction;

        public PayloadContentCheckFunction(PayloadFunction<T> payloadFunction) {
            this.payloadFunction = payloadFunction;
        }

        @Override
        public boolean areContentsTheSame(T oldItem, T newItem) {
            List<Object> payloads = payloadFunction.getChangePayload(oldItem, newItem);
            return payloads != null && !payloads.isEmpty();
        }
    }

    public static class PayloadDefaultFunction<T> implements PayloadFunction<T> {

        @Override
        public List<Object> getChangePayload(T oldItem, T newItem) {
            return null;
        }
    }
}
