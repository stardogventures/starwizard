package io.stardog.starwizard.services.media;

import io.stardog.starwizard.services.media.data.ImageVersion;
import org.im4java.core.Info;

import javax.ws.rs.core.MediaType;
import java.io.File;

public class MediaUtil {
    /**
     * Given a MIME contentType string, return a MediaType object for that content type.
     *
     * @param contentType
     * @return
     */
    public static MediaType toMediaType(String contentType) {
        String[] explode = contentType.split("/");
        return new MediaType(explode[0], explode[1]);
    }

    /**
     * Given a media type, return an appropriate file extension for the media type.
     *
     * @param mediaType
     * @return
     */
    public static String toExtension(MediaType mediaType) {
        String result = mediaType.getSubtype().toLowerCase();
        if (result.indexOf('+') > -1) {
            result = result.substring(0, result.indexOf('+'));
        }
        if (result.equals("jpeg")) {
            result = "jpg";
        }
        return result;
    }

    /**
     * Given a file name, return the extension.
     * @param file  filename
     * @return  extension
     */
    public static String toExtension(String file) {
        int lastDot = file.lastIndexOf(".");
        return file.substring(lastDot+1).toLowerCase();
    }

    public static File toVersionFile(File sourceFile, String versionName) {
        return new File(sourceFile.getAbsolutePath().replace(".", "-" + versionName + "."));
    }

    public static String toVersionPath(String remotePath, String versionName) {
        return remotePath.replace(".", "-" + versionName + ".");
    }

    public static String getImageFormat(String file) throws InfoException {
        try {
            Info imageInfo = new Info(file, true);
            return imageInfo.getImageFormat();
        } catch (Exception e) {
            throw new InfoException("Unable to get file format: " + e.getMessage());
        }
    }
}
