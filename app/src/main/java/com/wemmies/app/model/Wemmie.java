package com.wemmies.app.model;

import java.io.Serializable;

public class Wemmie implements Serializable {
    private String shamefulThought;
    private String emotionType;
    private int empathyCount;

    public Wemmie(String shamefulThought, String emotionType) {
        this.shamefulThought = shamefulThought;
        this.emotionType = emotionType;
        this.empathyCount = 0;
    }

    public String getShamefulThought() { return shamefulThought; }
    public String getEmotionType() { return emotionType; }
    public int getEmpathyCount() { return empathyCount; }
    public void addEmpathy() { this.empathyCount++; }
    public boolean isTransformed() { return empathyCount >= 5; }
}
