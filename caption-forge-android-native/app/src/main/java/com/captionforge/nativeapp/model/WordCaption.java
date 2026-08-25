package com.captionforge.nativeapp.model;

import com.google.gson.annotations.SerializedName;

public class WordCaption {
    @SerializedName("word")
    private String word;

    @SerializedName("start")
    private double start;

    @SerializedName("end")
    private double end;

    public WordCaption(String word, double start, double end) {
        this.word = word;
        this.start = start;
        this.end = end;
    }

    public String getWord() {
        return word != null ? word : "";
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }
}
