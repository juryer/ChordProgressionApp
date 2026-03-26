package com.chordapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * コードのトランスポーズ（移調）ユーティリティ
 * C基準のコード進行を任意のキーに移調する
 */
public class ChordTransposer {

    // 音名 → 半音インデックス (C=0)
    private static final Map<String, Integer> NOTE_INDEX = new LinkedHashMap<>();
    // インデックス → 音名（シャープ表記）
    private static final String[] INDEX_TO_NOTE = {
        "C","C#","D","D#","E","F","F#","G","G#","A","A#","B"
    };

    static {
        NOTE_INDEX.put("C",0);  NOTE_INDEX.put("C#",1); NOTE_INDEX.put("Db",1);
        NOTE_INDEX.put("D",2);  NOTE_INDEX.put("D#",3); NOTE_INDEX.put("Eb",3);
        NOTE_INDEX.put("E",4);  NOTE_INDEX.put("F",5);
        NOTE_INDEX.put("F#",6); NOTE_INDEX.put("Gb",6);
        NOTE_INDEX.put("G",7);  NOTE_INDEX.put("G#",8); NOTE_INDEX.put("Ab",8);
        NOTE_INDEX.put("A",9);  NOTE_INDEX.put("A#",10);NOTE_INDEX.put("Bb",10);
        NOTE_INDEX.put("B",11);
    }

    /**
     * コード1つを移調する
     * @param chord  元のコード名（例: "Am7"）
     * @param fromKey 元のキー（例: "C"）
     * @param toKey   移調先のキー（例: "D"）
     * @return 移調後のコード名（例: "Bm7"）
     */
    public static String transpose(String chord, String fromKey, String toKey) {
        if (chord == null || chord.equals("─") || chord.isEmpty()) return chord;

        // キーのルート音を取得
        String fromRoot = extractKeyRoot(fromKey);
        String toRoot   = extractKeyRoot(toKey);

        Integer fromIdx = NOTE_INDEX.get(fromRoot);
        Integer toIdx   = NOTE_INDEX.get(toRoot);
        if (fromIdx == null || toIdx == null) return chord;

        // 移調幅（半音数）
        int shift = (toIdx - fromIdx + 12) % 12;
        if (shift == 0) return chord;

        // コードのルート音とサフィックスを分離
        String root   = extractRoot(chord);
        String suffix = chord.substring(root.length());

        Integer rootIdx = NOTE_INDEX.get(root);
        if (rootIdx == null) return chord;

        // 移調後のルート音
        int newRootIdx = (rootIdx + shift) % 12;
        String newRoot = INDEX_TO_NOTE[newRootIdx];

        return newRoot + suffix;
    }

    /**
     * コードリストをまとめて移調する
     */
    public static List<String> transposeList(List<String> chords, String fromKey, String toKey) {
        List<String> result = new ArrayList<>();
        for (String c : chords) result.add(transpose(c, fromKey, toKey));
        return result;
    }

    /**
     * キー文字列からルート音を取得（"Am" → "A"）
     */
    public static String extractKeyRoot(String key) {
        if (key == null) return "C";
        if (key.length() > 1 && key.endsWith("m") &&
            !key.equals("Dm") && key.length() == 2) {
            // 1文字+m のマイナーキー
        }
        // マイナーキー判定（末尾がm かつ 長さ2以上）
        boolean isMinor = key.length() > 1 && key.endsWith("m");
        return isMinor ? key.substring(0, key.length()-1) : key;
    }

    /**
     * コードのルート音を抽出（"C#m7" → "C#"）
     */
    private static String extractRoot(String chord) {
        if (chord.length() >= 2 &&
            (chord.charAt(1) == '#' || chord.charAt(1) == 'b')) {
            return chord.substring(0, 2);
        }
        return chord.substring(0, 1);
    }

    /**
     * 利用可能なキー一覧（移調先として表示する）
     */
    public static final String[] ALL_KEYS = {
        "C","C#","D","D#","E","F","F#","G","G#","A","A#","B",
        "Cm","C#m","Dm","D#m","Em","Fm","F#m","Gm","G#m","Am","A#m","Bm"
    };
}
