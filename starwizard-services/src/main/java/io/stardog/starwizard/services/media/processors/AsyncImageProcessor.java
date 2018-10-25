package io.stardog.starwizard.services.media.processors;

import io.stardog.starwizard.services.common.AsyncService;
import io.stardog.starwizard.services.media.MediaUtil;
import io.stardog.starwizard.services.media.data.ImageDimensions;
import io.stardog.starwizard.services.media.data.ImageVersion;
import io.stardog.starwizard.services.media.interfaces.ImageProcessor;
import io.stardog.starwizard.services.media.interfaces.ImageResizer;
import io.stardog.starwizard.services.media.interfaces.MediaStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Files;
import java.util.Collection;

/**
 * This is a simple image processor that does the processing in a local thread pool. This will work for most small
 * and medium scale usecases. For more robust processing, a different queue mechanism might be appropriate.
 */
@Singleton
public class AsyncImageProcessor implements ImageProcessor {
    private final AsyncService asyncService;
    private final ImageResizer imageResizer;
    private final MediaStorage mediaStorage;
    private final Logger LOG = LoggerFactory.getLogger(AsyncImageProcessor.class);

    @Inject
    public AsyncImageProcessor(AsyncService asyncService, ImageResizer imageResizer, MediaStorage mediaStorage) {
        this.asyncService = asyncService;
        this.imageResizer = imageResizer;
        this.mediaStorage = mediaStorage;
    }

    @Override
    public ImageDimensions getDimensions(File imageFile) {
        return imageResizer.getDimensions(imageFile);
    }

    @Override
    public void processImageAsync(File imageFile, String storagePath, Collection<ImageVersion> versions) {
        asyncService.submit(() -> {
            try {
                processImage(imageFile, storagePath, versions);
                Files.delete(imageFile.toPath());
                LOG.info("Resized " + versions.size() + " versions for image: " + storagePath);
            } catch (Exception e) {
                LOG.error("Failure while attempting to resize " + storagePath, e);
            }
        });
    }

    @Override
    public void processImage(File imageFile, String storagePath, Collection<ImageVersion> versions) {
        for (ImageVersion v : versions) {
            processImage(imageFile, storagePath, v);
        }
    }

    private void processImage(File imageFile, String storagePath, ImageVersion version) {
        File versionFile = MediaUtil.toVersionFile(imageFile, version.getName());
        String versionPath = MediaUtil.toVersionPath(storagePath, version.getName());

        imageResizer.resizeImage(imageFile, versionFile, version.getWidth(), version.getHeight(), version.isSquare());
        mediaStorage.storeVersion(versionFile, versionPath);
        versionFile.delete();
    }
}
