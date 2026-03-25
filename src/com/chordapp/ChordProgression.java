package com.chordapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class ChordProgression {
    private String id;
    private String title;
    private String key;
    private String tempo;
    private List<String> chords;
    private LocalDateTime lastUsed;
    private String memo;

    public ChordProgression(String title, String key, String tempo, List<String> chords, String memo) {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.key = key;
        this.tempo = tempo;
        this.chords = new ArrayList<>(chords);
        this.memo = memo;
        this.lastUsed = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getTempo() { return tempo; }
    public void setTempo(String tempo) { this.tempo = tempo; }
    public List<String> getChords() { return chords; }
    public void setChords(List<String> chords) { this.chords = new ArrayList<>(chords); }
    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public String getChordsAsString() {
        return String.join(" → ", chords);
    }

    public String getFormattedLastUsed() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        return lastUsed.format(fmt);
    }
}
