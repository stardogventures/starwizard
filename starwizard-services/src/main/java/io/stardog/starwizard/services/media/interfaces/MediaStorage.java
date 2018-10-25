package io.stardog.starwizard.services.media.interfaces;

import java.io.File;

/**
 * A MediaStorage is capable of storing an image file in a particular path. A distinction can be made between "originals"
 * and versions, which may be stored in different places.
 */
public interface MediaStorage {
    void storeOriginal(File file, String originalPath);
    void storeVersion(File file, String versionPath);
}
