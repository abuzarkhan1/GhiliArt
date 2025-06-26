package com.abuzar.ghibli_art.Service;

import com.abuzar.ghibli_art.Client.StabilityAiClient;
import com.abuzar.ghibli_art.dto.TextToImageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GhibliArtService {

    private final StabilityAiClient stabilityAiClient;
    private final String apiKey;

    public GhibliArtService(StabilityAiClient stabilityAiClient,
                            @Value("${STABILITY_API_KEY}") String apiKey) {
        this.stabilityAiClient = stabilityAiClient;
        this.apiKey = apiKey;
    }

    public byte[] createGhibliArt(MultipartFile image, String prompt) {
        String finalPrompt = prompt+ "in the beautiful , detailed anime style of Studio Ghibli";
        String engineId = "stable-diffusion-v1-6";
        String stylePreset = "anime";

        return stabilityAiClient.generateImageFromImage(
                "Bearer " + apiKey,
                engineId,
                image,
                finalPrompt,
                stylePreset
        );

    }

    public  byte[] createGhibliArtFromText(String prompt, String style){
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

}
