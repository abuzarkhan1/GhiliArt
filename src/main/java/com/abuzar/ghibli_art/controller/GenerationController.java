package com.abuzar.ghibli_art.controller;

import com.abuzar.ghibli_art.Service.GhibliArtService;
import com.abuzar.ghibli_art.dto.TextGenerationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {
        "*", "http://localhost:5173"
})
@RequiredArgsConstructor
public class GenerationController {

    private final GhibliArtService ghibliArtService;


    @PostMapping(value = "/generate", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateGhibliArt(@RequestParam("image")MultipartFile image,
                                                    @RequestParam("prompt") String prompt){
        try {
            byte[] imageBytes = ghibliArtService.createGhibliArt(image, prompt);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }

    }

    @PostMapping(value = "/generate-from-text", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateGhibliArtFromText(@RequestBody TextGenerationRequestDTO requestDTO){
        try {
          byte[] imageBytes = ghibliArtService.createGhibliArtFromText(requestDTO.getPrompt(), requestDTO.getStyle());
          return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

}
