package com.wemmies.app.model;

import java.io.Serializable;

public class Wemmie implements Serializable {

    private String id;
    private String shamefulThought;
    private String emotionType;
    private int empathyCount;
    private String userId;
    private long timestamp;
    private boolean transformed;

    // Required empty constructor for Firestore
    public Wemmie() {
    }

    public Wemmie(String shamefulThought, String emotionType) {
        this.shamefulThought = shamefulThought;
        this.emotionType = emotionType;
        this.empathyCount = 0;
        this.transformed = false;
    }

    // ----------------------------
    // ID
    // ----------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // ----------------------------
    // Thought
    // ----------------------------

    public String getShamefulThought() {
        return shamefulThought;
    }

    public void setShamefulThought(String shamefulThought) {
        this.shamefulThought = shamefulThought;
    }

    // ----------------------------
    // Emotion
    // ----------------------------

    public String getEmotionType() {
        return emotionType;
    }

    public void setEmotionType(String emotionType) {
        this.emotionType = emotionType;
    }

    // ----------------------------
    // Empathy
    // ----------------------------

    public int getEmpathyCount() {
        return empathyCount;
    }

    public void setEmpathyCount(int empathyCount) {
        this.empathyCount = empathyCount;
    }

    public void addEmpathy() {
        empathyCount++;
    }

    // ----------------------------
    // User
    // ----------------------------

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // ----------------------------
    // Timestamp
    // ----------------------------

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ----------------------------
    // Transformation
    // ----------------------------

    public boolean isTransformed() {
        return transformed;
    }

    public void setTransformed(boolean transformed) {
        this.transformed = transformed;
    }
}