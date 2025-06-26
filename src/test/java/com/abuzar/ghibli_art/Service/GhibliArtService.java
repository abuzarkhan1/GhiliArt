// GhibliArtService.java
package com.abuzar.ghibli_art.Service;

import com.abuzar.ghibli_art.Client.StabilityAiClient;
import com.abuzar.ghibli_art.dto.TextToImageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class GhibliArtService {

    private final StabilityAiClient stabilityAiClient;
    private final String apiKey;

    // Constants for Stability AI v1.6 constraints
    private static final int MIN_DIMENSION = 320;
    private static final int MAX_DIMENSION = 1536;

    public GhibliArtService(StabilityAiClient stabilityAiClient,
                            @Value("${stability.api.key:sk-hBQl3uQRBvAYVlt4zZkycokgi7vwbFeDTxwxWK8MckhRiUaO}") String apiKey) {
        this.stabilityAiClient = stabilityAiClient;
        this.apiKey = apiKey;
    }

    public byte[] createGhibliArt(MultipartFile image, String prompt) throws IOException {
        String finalPrompt = prompt + " in the beautiful, detailed anime style of Studio Ghibli";
        String engineId = "stable-diffusion-v1-6";
        String stylePreset = "anime";

        // Resize the image if necessary
        MultipartFile resizedImage = resizeImageIfNeeded(image);

        return stabilityAiClient.generateImageFromImage(
                "Bearer " + apiKey,
                engineId,
                resizedImage,
                finalPrompt,
                stylePreset
        );
    }

    public byte[] createGhibliArtFromText(String prompt, String style) {
        String finalPrompt = prompt + " in the beautiful, detailed anime style of Studio Ghibli";
        String engineId = "stable-diffusion-v1-6";
        String stylePreset = style.equals("anime") ? "anime" : style.replace("_", "-");

        TextToImageRequest requestPayLoad = new TextToImageRequest(finalPrompt, stylePreset);
        return stabilityAiClient.generateImageFromText(
                "Bearer " + apiKey,
                engineId,
                requestPayLoad
        );
    }

    private MultipartFile resizeImageIfNeeded(MultipartFile originalImage) throws IOException {
        try {
            BufferedImage bufferedImage = ImageIO.read(originalImage.getInputStream());

            if (bufferedImage == null) {
                throw new IOException("Unable to read the uploaded image");
            }

            int originalWidth = bufferedImage.getWidth();
            int originalHeight = bufferedImage.getHeight();

            // Check if resizing is needed
            if (originalWidth <= MAX_DIMENSION && originalHeight <= MAX_DIMENSION &&
                    originalWidth >= MIN_DIMENSION && originalHeight >= MIN_DIMENSION) {
                return originalImage; // No resizing needed
            }

            // Calculate new dimensions
            Dimension newDimensions = calculateNewDimensions(originalWidth, originalHeight);

            // Resize the image
            BufferedImage resizedImage = resizeImage(bufferedImage,
                    (int) newDimensions.getWidth(),
                    (int) newDimensions.getHeight());

            // Convert back to MultipartFile
            return convertToMultipartFile(resizedImage, originalImage.getOriginalFilename(),
                    originalImage.getContentType());

        } catch (IOException e) {
            throw new IOException("Error processing image: " + e.getMessage(), e);
        }
    }

    private Dimension calculateNewDimensions(int originalWidth, int originalHeight) {
        double aspectRatio = (double) originalWidth / originalHeight;

        int newWidth, newHeight;

        // If both dimensions are too large, scale down proportionally
        if (originalWidth > MAX_DIMENSION || originalHeight > MAX_DIMENSION) {
            if (originalWidth > originalHeight) {
                newWidth = MAX_DIMENSION;
                newHeight = (int) (MAX_DIMENSION / aspectRatio);
            } else {
                newHeight = MAX_DIMENSION;
                newWidth = (int) (MAX_DIMENSION * aspectRatio);
            }
        }
        // If both dimensions are too small, scale up proportionally
        else if (originalWidth < MIN_DIMENSION && originalHeight < MIN_DIMENSION) {
            if (originalWidth < originalHeight) {
                newWidth = MIN_DIMENSION;
                newHeight = (int) (MIN_DIMENSION / aspectRatio);
            } else {
                newHeight = MIN_DIMENSION;
                newWidth = (int) (MIN_DIMENSION * aspectRatio);
            }
        }
        // Mixed case: one dimension is fine, adjust the other
        else {
            if (originalWidth > MAX_DIMENSION) {
                newWidth = MAX_DIMENSION;
                newHeight = (int) (MAX_DIMENSION / aspectRatio);
            } else if (originalWidth < MIN_DIMENSION) {
                newWidth = MIN_DIMENSION;
                newHeight = (int) (MIN_DIMENSION / aspectRatio);
            } else if (originalHeight > MAX_DIMENSION) {
                newHeight = MAX_DIMENSION;
                newWidth = (int) (MAX_DIMENSION * aspectRatio);
            } else { // originalHeight < MIN_DIMENSION
                newHeight = MIN_DIMENSION;
                newWidth = (int) (MIN_DIMENSION * aspectRatio);
            }
        }

        // Ensure final dimensions are within bounds
        newWidth = Math.max(MIN_DIMENSION, Math.min(MAX_DIMENSION, newWidth));
        newHeight = Math.max(MIN_DIMENSION, Math.min(MAX_DIMENSION, newHeight));

        return new Dimension(newWidth, newHeight);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();

        // Set rendering hints for better quality
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();

        return resizedImage;
    }

    private MultipartFile convertToMultipartFile(BufferedImage bufferedImage, String originalFilename, String contentType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = getImageFormat(contentType);
        ImageIO.write(bufferedImage, format, baos);
        byte[] imageBytes = baos.toByteArray();

        return new MockMultipartFile(
                "image",
                originalFilename,
                contentType != null ? contentType : "image/png",
                imageBytes
        );
    }

    private String getImageFormat(String contentType) {
        if (contentType != null) {
            if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                return "jpg";
            } else if (contentType.contains("png")) {
                return "png";
            } else if (contentType.contains("gif")) {
                return "gif";
            } else if (contentType.contains("bmp")) {
                return "bmp";
            }
        }
        return "png"; // Default format
    }
}