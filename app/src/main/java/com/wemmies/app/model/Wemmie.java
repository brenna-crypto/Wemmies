package com.wemmies.app.model;

import java.io.Serializable;

// This is the main data model for a Wemmie
// It needs to be Serializable so we can pass it between activities using Intent
public class Wemmie implements Serializable {

    private String shamefulThought; // the text the user typed
    private String emotionType;     // which emotion they selected (sad, anxious, etc.)
    private int empathyCount;       // how many empathies this Wemmie has received

    public Wemmie(String shamefulThought, String emotionType) {
        this.shamefulThought = shamefulThought;
        this.emotionType = emotionType;
        this.empathyCount = 0; // starts at 0, increases when people send empathy
    }

    // Getters so other classes can read the data
    public String getShamefulThought() { return shamefulThought; }
    public String getEmotionType() { return emotionType; }
    public int getEmpathyCount() { return empathyCount; }

    // Called every time someone taps an empathy button
    public void addEmpathy() { this.empathyCount++; }

    // A Wemmie transforms once it gets 5 or more empathies
    public boolean isTransformed() { return empathyCount >= 5; }
}
