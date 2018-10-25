package io.stardog.starwizard.services.media;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.stardog.starwizard.services.media.data.ImageDimensions;
import io.stardog.starwizard.services.media.data.ImageInfo;
import io.stardog.starwizard.services.media.data.ImageVersion;
import io.stardog.starwizard.services.media.exceptions.MediaException;
import io.stardog.starwizard.services.media.interfaces.ImageProcessor;
import io.stardog.starwizard.services.media.interfaces.MediaStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Singleton
public class MediaService {
    private final MediaStorage mediaStorage;
    private final ImageProcessor imageProcessor;
    private final Set<String> supportedExtensions;
    private final Map<String,ImageVersion> versionMap;
    private final Logger LOG = LoggerFactory.getLogger(MediaService.class);

    @Inject
    public MediaService(MediaStorage mediaStorage, ImageProcessor imageProcessor,
                        @Named("mediaImageExtensions") Set<String> supportedExtensions,
                        @Named("mediaImageVersions") Collection<ImageVersion> resizeVersions) {
        this.imageProcessor = imageProcessor;
        this.mediaStorage = mediaStorage;
        this.supportedExtensions = supportedExtensions;
        versionMap = new HashMap<>();
        for (ImageVersion v : resizeVersions) {
            versionMap.put(v.getName(), v);
        }
    }

    /**
     * Given an image file, return its dimensions.
     * @param file  file
     * @return  dimensions (height and width)
     */
    public ImageDimensions getDimensions(File file) {
        return imageProcessor.getDimensions(file);
    }

    /**
     * Given a local file, upload and resize it. One version (such as a small thumbnail) will be synchronously resized,
     * while all other desired versions will be resized asynchronously.
     * @param localFile local image file, ending in an extension that matches the media type
     * @param type  media type
     * @param storagePath   remote storage path
     * @param initialVersionName    name of the initial version to resize to
     * @return  metadata about the uploaded image
     */
    public ImageInfo uploadImage(File localFile, MediaType type, String storagePath, String initialVersionName) {
        String ext = MediaUtil.toExtension(type);
        if (!localFile.toString().endsWith("." + ext)) {
            throw new MediaException("Expected file to end with ." + ext);
        }
        if (!supportedExtensions.contains(ext)) {
            throw new MediaException("Unsupported image type: " + ext);
        }
        ImageVersion initialVersion = versionMap.get(initialVersionName);
        if (initialVersion == null) {
            throw new MediaException("Version not found: " + initialVersionName);
        }

        // determine the base image size
        ImageDimensions dimensions = imageProcessor.getDimensions(localFile);

        // save the original to media storage
        mediaStorage.storeOriginal(localFile, storagePath);

        // resize the original to the initial version, synchronously
        imageProcessor.processImage(localFile, storagePath, ImmutableList.of(initialVersion));

        // resize the other versions asynchronously
        Set<ImageVersion> otherVersions = Sets.difference(
                ImmutableSet.copyOf(versionMap.values()),
                ImmutableSet.of(initialVersion));
        imageProcessor.processImageAsync(localFile, storagePath, otherVersions);

        long bytes = localFile.length();

        return ImageInfo.builder()
                .path(storagePath)
                .width(dimensions.getWidth())
                .height(dimensions.getHeight())
                .type(type)
                .bytes(bytes)
                .versions(ImmutableSet.copyOf(versionMap.values()))
                .build();
    }
}
