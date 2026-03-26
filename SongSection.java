package com.chordapp;

import java.util.ArrayList;
import java.util.List;

/**
 * 1曲のセクション（verse/chorus等）を表すモデル
 */
public class SongSection {
    private String id;
    private String sectionName;     // "A verse", "Chorus" など
    private String progressionId;   // 使用するコード進行のID（nullなら手動入力）
    private List<String> chords;    // コード一覧（登録済み進行から参照 or 直接入力）
    private int repeatCount;        // 繰り返し回数

    public SongSection(String sectionName) {
        this.id = java.util.UUID.randomUUID().toString();
        this.sectionName = sectionName;
        this.chords = new ArrayList<>();
        this.repeatCount = 1;
    }

    public String getId() { return id; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String name) { this.sectionName = name; }
    public String getProgressionId() { return progressionId; }
    public void setProgressionId(String pid) { this.progressionId = pid; }
    public List<String> getChords() { return chords; }
    public void setChords(List<String> chords) { this.chords = new ArrayList<>(chords); }
    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int r) { this.repeatCount = r; }

    public String getChordsAsString() {
        return String.join("  ", chords);
    }
}
