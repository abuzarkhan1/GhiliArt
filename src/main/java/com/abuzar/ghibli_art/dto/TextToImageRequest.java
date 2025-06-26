package com.abuzar.ghibli_art.dto;

import java.util.ArrayList;
import java.util.List;

public class TextToImageRequest {

    private List<TextPrompt> textPrompts;
    private double cfg_scale = 7;
    private int height =512;
    private int width =768;
    private int samples = 1;
    private int steps = 30;
    private String style_preset;

    public static class TextPrompt {
        private String text;

        public TextPrompt(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }


    public TextToImageRequest(String text, String style) {
        this.textPrompts = List.of(new TextPrompt(text));
        this.style_preset = style;
    }


    public List<TextPrompt> getTextPrompts() {
        return textPrompts;
    }

    public void setTextPrompts(List<TextPrompt> textPrompts) {
        this.textPrompts = textPrompts;
    }

    public double getCfg_scale() {
        return cfg_scale;
    }

    public int getHeight(){
        return height;
    }

    public int getWidth(){
        return width;
    }

    public int getSamples(){
        return samples;
    }

    public String getStyle_preset(){
        return style_preset;
    }

    public int getSteps(){
        return steps;
    }


}
