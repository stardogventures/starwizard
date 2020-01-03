package io.stardog.starwizard.services.media.resizers;

import io.stardog.starwizard.services.media.MediaUtil;
import io.stardog.starwizard.services.media.data.ImageDimensions;
import io.stardog.starwizard.services.media.exceptions.MediaException;
import io.stardog.starwizard.services.media.interfaces.ImageResizer;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.process.ArrayListOutputConsumer;

import javax.inject.Singleton;
import java.io.File;

/**
 * An ImageResizer that makes use of ImageMagick. ImageMagick must be installed on the system for this to work.
 *
 * ImageMagick has high quality resizing/resampling algorithms, but has some dangerous security holes in some versions
 * related to untrusted input -- please review https://imagetragick.com/ for information and recommendations.
 *
 * Because many of the known vulnerabilities relate to SVG format, the recommendation is that you only enable the use
 * of this resizer for PNG, GIF, and JPEG files.
 *
 * The resizer does not use autodetection and explicitly specifies the file type based on the extension of the file,
 * so will fail if you attempt to pass a file that does not have an extension matching the correct format (.png, .gif, .jpg).
 */
@Singleton
public class ImagemagickResizer implements ImageResizer  {
    /**
     * Given a file, return image about its current width and height.
     *
     * @param localFile local file
     * @return  info about the image's width and height
     */
    public ImageDimensions getDimensions(File localFile) {
        // per https://stackoverflow.com/questions/23055041/imagemagick-get-image-size-with-respect-to-exif-orientation
        // the identify resizers does not allow -auto-orient, so use convert :info instead
        try {
            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            String ext = MediaUtil.toExtension(localFile.getAbsolutePath());
            ConvertCmd convert = new ConvertCmd();
            convert.setOutputConsumer(output);
            IMOperation op = new IMOperation();
            op.addImage(ext + ":" + localFile.getAbsolutePath());
            op.autoOrient();
            op.addRawArgs("-format", "%w %h ");
            op.addRawArgs("info:");
            convert.run(op);
            String widthXHeight = output.getOutput().get(0); // e.g. 3000 4512
            String[] parts = widthXHeight.split(" ");
            return ImageDimensions.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (Exception e) {
            throw new MediaException(e.getMessage());
        }
    }

    /**
     * Convert and resize an image using ImageMagick.
     *
     * @param sourceFile    source file
     * @param destFile  destination file
     * @param width desired width
     * @param height    desired height
     * @param isSquare    desired height
     */
    public void resizeImage(File sourceFile, File destFile, int width, int height, boolean isSquare)  {
        try {
            String ext = MediaUtil.toExtension(sourceFile.toString());
            ConvertCmd convert = new ConvertCmd();
            IMOperation op = new IMOperation();
            op.background("none");
            op.addImage(ext + ":" + sourceFile.getAbsolutePath());
            op.autoOrient();
            if (isSquare) {
                op.thumbnail(width, height, "^");
                op.gravity("center");
                op.extent(width, height);
            } else {
                op.resize(width, height);
            }
            op.addImage(ext + ":" + destFile.getAbsolutePath());
            convert.run(op);
        } catch (Exception e) {
            throw new MediaException("Unable to resize image: " + e.getMessage());
        }
    }
}
