package io.stardog.starwizard.services.media.data;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;

import javax.ws.rs.core.MediaType;
import java.util.Set;

@AutoValue
public abstract class ImageInfo {
    public abstract String getPath();
    public abstract MediaType getType();
    public abstract int getWidth();
    public abstract int getHeight();
    public abstract long getBytes();
    public abstract Set<ImageVersion> getVersions();

    public abstract Builder toBuilder();
    public static ImageInfo.Builder builder() {
        return new AutoValue_ImageInfo.Builder();
    }

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "")
    public abstract static class Builder {
        public abstract Builder path(String path);
        public abstract Builder type(MediaType type);
        public abstract Builder width(int width);
        public abstract Builder height(int height);
        public abstract Builder bytes(long bytes);
        public abstract Builder versions(Set<ImageVersion> versions);
        public abstract ImageInfo build();
    }
}