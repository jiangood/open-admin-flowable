package io.github.jiangood.openadmin.modules.flowable.common.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

public class FlowableImgTool {

    public static String toBase64DataUri(BufferedImage image) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
            byte[] data = os.toByteArray();
            return "data:image/jpg;base64," + Base64.getEncoder().encodeToString(data);
        } finally {
            os.close();
        }
    }
}
