package com.wemmies.app.model;

import java.io.Serializable;

public class Wemmie implements Serializable {

    private String id;              // Firestore document ID — new!
    private String shamefulThought;
    private String emotionType;
    private int empathyCount;

    // Empty constructor required by Firestore
    public Wemmie() {}

    public Wemmie(String shamefulThought, String emotionType) {
        this.shamefulThought = shamefulThought;
        this.emotionType = emotionType;
        this.empathyCount = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getShamefulThought() { return shamefulThought; }
    public String getEmotionType() { return emotionType; }
    public int getEmpathyCount() { return empathyCount; }
    public void setEmpathyCount(int empathyCount) { this.empathyCount = empathyCount; }
    public void addEmpathy() { this.empathyCount++; }
    public boolean isTransformed() { return empathyCount >= 5; }
}