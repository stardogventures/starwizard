package io.stardog.starwizard.services.media.data;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ImageDimensions {
    public abstract int getWidth();
    public abstract int getHeight();

    public static ImageDimensions of(int width, int height) {
        return new AutoValue_ImageDimensions(width, height);
    }
}