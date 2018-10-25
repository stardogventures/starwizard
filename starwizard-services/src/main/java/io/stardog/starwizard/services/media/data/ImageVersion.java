package io.stardog.starwizard.services.media.data;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ImageVersion {
    public abstract String getName();
    public abstract int getWidth();
    public abstract int getHeight();
    public abstract boolean isSquare();

    public static ImageVersion of(String name, int width, int height, boolean isSquare) {
        return new AutoValue_ImageVersion(name, width, height, isSquare);
    }
}