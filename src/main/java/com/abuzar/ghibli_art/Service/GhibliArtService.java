package com.abuzar.ghibli_art.Service;

import com.abuzar.ghibli_art.Client.StabilityAiClient;
import com.abuzar.ghibli_art.dto.TextToImageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class GhibliArtService {

    private final StabilityAiClient stabilityAiClient;
    private final String apiKey;

    // Constants for Stability AI v1-6 model constraints
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

        // Resize image if necessary
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
        // Read the original image
        BufferedImage bufferedImage = ImageIO.read(originalImage.getInputStream());

        if (bufferedImage == null) {
            throw new IOException("Unable to read image file");
        }

        int originalWidth = bufferedImage.getWidth();
        int originalHeight = bufferedImage.getHeight();

        // Check if resizing is needed
        if (originalWidth <= MAX_DIMENSION && originalHeight <= MAX_DIMENSION &&
                originalWidth >= MIN_DIMENSION && originalHeight >= MIN_DIMENSION) {
            return originalImage; // No resizing needed
        }

        // Calculate new dimensions while maintaining aspect ratio
        Dimension newDimensions = calculateNewDimensions(originalWidth, originalHeight);

        // Resize the image
        BufferedImage resizedImage = resizeImage(bufferedImage, newDimensions.width, newDimensions.height);

        // Convert back to MultipartFile
        return convertToMultipartFile(resizedImage, originalImage.getOriginalFilename(), originalImage.getContentType());
    }

    private Dimension calculateNewDimensions(int originalWidth, int originalHeight) {
        double aspectRatio = (double) originalWidth / originalHeight;

        int newWidth, newHeight;

        // If both dimensions exceed MAX_DIMENSION, scale down
        if (originalWidth > MAX_DIMENSION || originalHeight > MAX_DIMENSION) {
            if (originalWidth > originalHeight) {
                newWidth = MAX_DIMENSION;
                newHeight = (int) (MAX_DIMENSION / aspectRatio);
            } else {
                newHeight = MAX_DIMENSION;
                newWidth = (int) (MAX_DIMENSION * aspectRatio);
            }
        }
        // If any dimension is below MIN_DIMENSION, scale up
        else if (originalWidth < MIN_DIMENSION || originalHeight < MIN_DIMENSION) {
            if (originalWidth < originalHeight) {
                newWidth = MIN_DIMENSION;
                newHeight = (int) (MIN_DIMENSION / aspectRatio);
            } else {
                newHeight = MIN_DIMENSION;
                newWidth = (int) (MIN_DIMENSION * aspectRatio);
            }
        }
        // This shouldn't happen given our checks, but just in case
        else {
            newWidth = originalWidth;
            newHeight = originalHeight;
        }

        // Ensure dimensions are within bounds after calculation
        newWidth = Math.max(MIN_DIMENSION, Math.min(MAX_DIMENSION, newWidth));
        newHeight = Math.max(MIN_DIMENSION, Math.min(MAX_DIMENSION, newHeight));

        return new Dimension(newWidth, newHeight);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();

        // Enable high-quality rendering
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();

        return resizedImage;
    }

    private MultipartFile convertToMultipartFile(BufferedImage image, String originalFilename, String contentType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String formatName = getImageFormat(contentType);
        ImageIO.write(image, formatName, baos);
        byte[] imageBytes = baos.toByteArray();

        return new CustomMultipartFile(imageBytes, originalFilename, contentType);
    }

    private String getImageFormat(String contentType) {
        if (contentType != null) {
            if (contentType.contains("png")) return "png";
            if (contentType.contains("gif")) return "gif";
            if (contentType.contains("bmp")) return "bmp";
        }
        return "jpg"; // Default to jpg
    }

    // Custom MultipartFile implementation
    private static class CustomMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String filename;
        private final String contentType;

        public CustomMultipartFile(byte[] content, String filename, String contentType) {
            this.content = content;
            this.filename = filename;
            this.contentType = contentType;
        }

        @Override public String getName() { return "file"; }
        @Override public String getOriginalFilename() { return filename; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() { return content; }
        @Override public java.io.InputStream getInputStream() { return new ByteArrayInputStream(content); }
        @Override public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }
}