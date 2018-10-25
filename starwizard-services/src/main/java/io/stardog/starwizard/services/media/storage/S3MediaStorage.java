package io.stardog.starwizard.services.media.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.stardog.starwizard.services.media.interfaces.MediaStorage;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

/**
 * This is a storage system that uses potentially two S3 buckets; one (a "data" bucket kept private) is used to store
 * the originals, and another (a "media" bucket kept public) is used to store all of the resized versions.
 */
@Singleton
public class S3MediaStorage implements MediaStorage {
    private final AmazonS3 s3;
    private final String s3BucketData;
    private final String s3BucketMedia;

    @Inject
    public S3MediaStorage(AmazonS3 s3, @Named("s3BucketData") String s3BucketData, @Named("s3BucketMedia") String s3BucketMedia) {
        this.s3 = s3;
        this.s3BucketData = s3BucketData;
        this.s3BucketMedia = s3BucketMedia;
    }

    @Override
    public void storeOriginal(File file, String originalPath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(s3BucketData, originalPath, file);
        s3.putObject(putObjectRequest);
    }

    @Override
    public void storeVersion(File file, String versionPath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(s3BucketMedia, versionPath, file);
        s3.putObject(putObjectRequest);
    }
}
