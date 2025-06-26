package com.abuzar.ghibli_art.dto;


import lombok.Data;

@Data
public class TextGenerationRequestDTO {

    private String prompt;
    private String style;
}
