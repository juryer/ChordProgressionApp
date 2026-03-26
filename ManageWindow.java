package com.chordapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

public class ManageWindow extends JDialog {

    private MainMenuWindow parent;
    private ChordProgressionRepository repo = ChordProgressionRepository.getInstance();
    private AppSettings settings = AppSettings.getInstance();
    private JPanel listPanel;
    private JTextField searchField;
    private JComboBox<String> sortCombo;
    private JRadioButton sortAscRadio;

    public ManageWindow(MainMenuWindow parent) {
        super(parent, "コード進行管理", true);
        this.parent = parent;
        setSize(960, 680);
        setLocationRelativeTo(parent);
        setResizable(true);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_DARK);
        setContentPane(root);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.BG_PANEL);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(220,140,60));
                g2.fillRect(0,getHeight()-2,getWidth(),2);
                g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(960, 64));
        h.setBorder(new EmptyBorder(12, 24, 12, 24));

        JLabel title = new JLabel("⚙  コード進行管理");
        title.setFont(AppTheme.titleFont(18));
        title.setForeground(AppTheme.TEXT_PRIMARY);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setOpaque(false);

        // ソートコンボ
        sortCombo = new JComboBox<>(new String[]{"登録順", "レーティング順", "最終使用順"});
        sortCombo.setFont(AppTheme.bodyFont(12));
        sortCombo.setBackground(AppTheme.BG_CARD);
        sortCombo.setForeground(AppTheme.TEXT_PRIMARY);
        sortCombo.setPreferredSize(new Dimension(130, 34));
        sortCombo.addActionListener(e -> refreshList());

        JRadioButton ascRadio = new JRadioButton("昇順");
        JRadioButton descRadio = new JRadioButton("降順");
        ascRadio.setFont(AppTheme.bodyFont(12));
        descRadio.setFont(AppTheme.bodyFont(12));
        ascRadio.setForeground(AppTheme.TEXT_PRIMARY);
        descRadio.setForeground(AppTheme.TEXT_PRIMARY);
        ascRadio.setOpaque(false);
        descRadio.setOpaque(false);
        ascRadio.setFocusPainted(false);
        descRadio.setFocusPainted(false);
        descRadio.setSelected(true);
        ButtonGroup orderGroup = new ButtonGroup();
        orderGroup.add(ascRadio);
        orderGroup.add(descRadio);
        ascRadio.addActionListener(e -> refreshList());
        descRadio.addActionListener(e -> refreshList());
        this.sortAscRadio = ascRadio;

        // 検索バー
        searchField = new JTextField();
        searchField.setFont(AppTheme.bodyFont(13));
        searchField.setForeground(AppTheme.TEXT_MUTED);
        searchField.setBackground(AppTheme.BG_CARD);
        searchField.setCaretColor(AppTheme.ACCENT_LIGHT);
        searchField.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)));
        searchField.setText("検索（タイトル・コード）");
        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().startsWith("検索")) { searchField.setText(""); searchField.setForeground(AppTheme.TEXT_PRIMARY); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) { searchField.setText("検索（タイトル・コード）"); searchField.setForeground(AppTheme.TEXT_MUTED); }
            }
        });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshList(); }
        });

        rightBar.add(sortCombo);
        rightBar.add(ascRadio);
        rightBar.add(descRadio);
        rightBar.add(searchField);

        h.add(title, BorderLayout.WEST);
        h.add(rightBar, BorderLayout.EAST);
        return h;
    }

    private JScrollPane buildContent() {
        listPanel = new JPanel();
        listPanel.setBackground(AppTheme.BG_DARK);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(16,24,16,24));
        refreshList();
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBackground(AppTheme.BG_DARK);
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    public void refreshList() {
        listPanel.removeAll();
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        boolean searching = !query.isEmpty() && !query.startsWith("検索");

        List<ChordProgression> all = repo.getAll();

        // ソート
        int sortIdx = sortCombo != null ? sortCombo.getSelectedIndex() : 0;
        boolean asc = sortAscRadio != null && sortAscRadio.isSelected();
        if (sortIdx == 0 && asc) {
            java.util.Collections.reverse(all);
        } else if (sortIdx == 1) {
            all.sort((a, b) -> asc
                ? Integer.compare(a.getRating(), b.getRating())
                : Integer.compare(b.getRating(), a.getRating()));
        } else if (sortIdx == 2) {
            all.sort((a, b) -> asc
                ? a.getLastUsed().compareTo(b.getLastUsed())
                : b.getLastUsed().compareTo(a.getLastUsed()));
        }

        int shown = 0;
        for (ChordProgression cp : all) {
            if (searching) {
                boolean match = cp.getTitle().toLowerCase().contains(query)
                    || cp.getChordsAsString().toLowerCase().contains(query)
                    || cp.getMemo().toLowerCase().contains(query);
                if (!match) continue;
            }
            listPanel.add(buildManageCard(cp));
            listPanel.add(Box.createVerticalStrut(10));
            shown++;
        }
        if (shown == 0) {
            JLabel empty = new JLabel(searching ? "検索結果がありません" : "登録されたコード進行がありません", SwingConstants.CENTER);
            empty.setFont(AppTheme.bodyFont(14));
            empty.setForeground(AppTheme.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(empty);
            listPanel.add(Box.createVerticalGlue());
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildManageCard(ChordProgression cp) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            boolean hovered = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? AppTheme.BG_CARD_HOVER : AppTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),AppTheme.RADIUS,AppTheme.RADIUS));
                g2.setColor(new Color(220,140,60));
                g2.fill(new RoundRectangle2D.Float(0,0,5,getHeight(),4,4));
                g2.dispose();
            }
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 18, 12, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(cp.getTitle());
        titleLabel.setFont(AppTheme.titleFont(15));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);

        String chordsDisplay = settings.getNoteMode() == AppSettings.NoteMode.DEGREE
            ? String.join(" → ", DegreeConverter.convertList(cp.getChords(), cp.getKey()))
            : cp.getChordsAsString();
        JLabel chordsLabel = new JLabel(chordsDisplay);
        chordsLabel.setFont(AppTheme.monoFont(12));
        chordsLabel.setForeground(AppTheme.ACCENT_LIGHT);

        JLabel metaLabel = new JLabel("BPM: " + cp.getTempo() + "  |  " + cp.getFormattedLastUsed());
        metaLabel.setFont(AppTheme.bodyFont(11));
        metaLabel.setForeground(AppTheme.TEXT_MUTED);

        StringBuilder starSb = new StringBuilder();
        for (int i = 1; i <= 5; i++) starSb.append(i <= cp.getRating() ? "★" : "☆");
        JLabel starLabel = new JLabel(starSb.toString());
        starLabel.setFont(AppTheme.bodyFont(12));
        starLabel.setForeground(new Color(255, 200, 50));

        info.add(titleLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(starLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(chordsLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(metaLabel);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);

        JButton upBtn = buildActionButton("▲", AppTheme.BG_CARD, AppTheme.TEXT_SECONDARY);
        upBtn.addActionListener(e -> { repo.moveUp(cp.getId()); parent.refreshRecentCards(); refreshList(); });

        JButton downBtn = buildActionButton("▼", AppTheme.BG_CARD, AppTheme.TEXT_SECONDARY);
        downBtn.addActionListener(e -> { repo.moveDown(cp.getId()); parent.refreshRecentCards(); refreshList(); });

        JButton editBtn = buildActionButton("編集", AppTheme.ACCENT, Color.WHITE);
        editBtn.addActionListener(e -> openEditDialog(cp));

        JButton deleteBtn = buildActionButton("削除", new Color(200, 60, 60), Color.WHITE);
        deleteBtn.addActionListener(e -> confirmDelete(cp));

        btns.add(upBtn);
        btns.add(downBtn);
        btns.add(editBtn);
        btns.add(deleteBtn);

        // ── ドラッグ＆ドロップ ──────────────────────────────────────────────
        card.setTransferHandler(new TransferHandler("text") {
            @Override public int getSourceActions(JComponent c) { return MOVE; }
            @Override protected Transferable createTransferable(JComponent c) {
                return new java.awt.datatransfer.StringSelection(cp.getId());
            }
        });
        card.addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                card.getTransferHandler().exportAsDrag(card, e, TransferHandler.MOVE);
            }
        });
        card.setDropTarget(new java.awt.dnd.DropTarget(card,
            new java.awt.dnd.DropTargetAdapter() {
                @Override public void drop(java.awt.dnd.DropTargetDropEvent e) {
                    try {
                        e.acceptDrop(java.awt.dnd.DnDConstants.ACTION_MOVE);
                        String draggedId = (String) e.getTransferable()
                            .getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
                        String targetId = cp.getId();
                        if (!draggedId.equals(targetId)) {
                            List<ChordProgression> all = repo.getAll();
                            int fromIdx = -1, toIdx = -1;
                            for (int i = 0; i < all.size(); i++) {
                                if (all.get(i).getId().equals(draggedId)) fromIdx = i;
                                if (all.get(i).getId().equals(targetId)) toIdx = i;
                            }
                            if (fromIdx >= 0 && toIdx >= 0) {
                                int steps = Math.abs(toIdx - fromIdx);
                                for (int i = 0; i < steps; i++) {
                                    if (fromIdx < toIdx) repo.moveDown(draggedId);
                                    else repo.moveUp(draggedId);
                                }
                                parent.refreshRecentCards();
                                refreshList();
                            }
                        }
                        e.dropComplete(true);
                    } catch (Exception ex) { e.dropComplete(false); }
                }
            }));

        card.add(info, BorderLayout.CENTER);
        card.add(btns, BorderLayout.EAST);
        return card;
    }

    // ── Edit Dialog ──────────────────────────────────────────────────────────
    private void openEditDialog(ChordProgression cp) {
        JDialog dlg = new JDialog(this, "編集: " + cp.getTitle(), true);
        dlg.setSize(860, 620);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_DARK);
        root.setBorder(new EmptyBorder(20, 28, 12, 28));
        dlg.setContentPane(root);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // タイトル
        form.add(fieldLabel("タイトル *"));
        form.add(Box.createVerticalStrut(5));
        JTextField titleF = styledField(cp.getTitle());
        form.add(titleF);
        form.add(Box.createVerticalStrut(12));

        // BPMのみ（キーはC固定）
        JPanel tempoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        tempoRow.setOpaque(false);
        tempoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        tempoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel tempoLbl = new JLabel("BPM:");
        tempoLbl.setFont(AppTheme.bodyFont(12));
        tempoLbl.setForeground(AppTheme.TEXT_SECONDARY);
        JTextField tempoF = styledField(cp.getTempo().equals("─") ? "" : cp.getTempo());
        tempoF.setPreferredSize(new Dimension(120, 34));
        tempoRow.add(tempoLbl); tempoRow.add(tempoF);
        form.add(tempoRow);
        form.add(Box.createVerticalStrut(12));

        // レーティング
        form.add(fieldLabel("レーティング"));
        form.add(Box.createVerticalStrut(6));
        JPanel ratingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        ratingRow.setOpaque(false);
        ratingRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        int[] currentRating = {cp.getRating()};
        JLabel[] stars = new JLabel[5];
        for (int i = 0; i < 5; i++) {
            final int star = i + 1;
            stars[i] = new JLabel(i < currentRating[0] ? "★" : "☆");
            stars[i].setFont(new Font("SansSerif", Font.PLAIN, 24));
            stars[i].setForeground(new Color(255, 200, 50));
            stars[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            stars[i].addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    currentRating[0] = star;
                    for (int j = 0; j < 5; j++) stars[j].setText(j < star ? "★" : "☆");
                }
                @Override public void mouseEntered(MouseEvent e) {
                    for (int j = 0; j < 5; j++) stars[j].setText(j < star ? "★" : "☆");
                }
                @Override public void mouseExited(MouseEvent e) {
                    for (int j = 0; j < 5; j++) stars[j].setText(j < currentRating[0] ? "★" : "☆");
                }
            });
            ratingRow.add(stars[i]);
        }
        form.add(ratingRow);
        form.add(Box.createVerticalStrut(12));
        tempoF.setPreferredSize(new Dimension(120, 34));
        tempoRow.add(tempoLbl); tempoRow.add(tempoF);
        form.add(tempoRow);
        form.add(Box.createVerticalStrut(12));

        // コード選択（ChordSelectorPanel使用・C基準）
        form.add(fieldLabel("コード進行  ( C / Cm 基準 )"));
        form.add(Box.createVerticalStrut(6));
        ChordSelectorPanel selector = new ChordSelectorPanel("C");
        selector.setChords(cp.getChords());
        selector.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(selector);
        form.add(Box.createVerticalStrut(12));

        // メモ
        form.add(fieldLabel("メモ"));
        form.add(Box.createVerticalStrut(5));
        JTextField memoF = styledField(cp.getMemo());
        form.add(memoF);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBackground(AppTheme.BG_DARK);
        scroll.getViewport().setBackground(AppTheme.BG_DARK);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton cancelB = buildActionButton("キャンセル", AppTheme.BG_CARD, AppTheme.TEXT_SECONDARY);
        cancelB.addActionListener(e -> dlg.dispose());

        JButton saveB = buildActionButton("  保存する  ", AppTheme.ACCENT, Color.WHITE);
        saveB.addActionListener(e -> {
            String newTitle = titleF.getText().trim();
            if (newTitle.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "タイトルを入力してください。", "エラー", JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<String> newChords = selector.getSelectedChords();
            if (newChords.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "コードを1つ以上選択してください。", "エラー", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cp.setTitle(newTitle);
            cp.setKey("C");
            String t = tempoF.getText().trim();
            cp.setTempo(t.isEmpty() ? "─" : t);
            cp.setChords(newChords);
            cp.setMemo(memoF.getText().trim());
            cp.setRating(currentRating[0]);
            cp.setLastUsed(java.time.LocalDateTime.now());
            repo.update(cp);
            parent.refreshRecentCards();
            refreshList();
            dlg.dispose();
        });

        footer.add(cancelB); footer.add(saveB);
        root.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void confirmDelete(ChordProgression cp) {
        int result = JOptionPane.showConfirmDialog(this,
            "「" + cp.getTitle() + "」を削除しますか？\nこの操作は元に戻せません。",
            "削除の確認", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            repo.delete(cp.getId());
            parent.refreshRecentCards();
            refreshList();
        }
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AppTheme.BG_PANEL);
        footer.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,AppTheme.BORDER),
            new EmptyBorder(12,24,12,24)));
        JLabel count = new JLabel("全 " + repo.getAll().size() + " 件");
        count.setFont(AppTheme.bodyFont(12));
        count.setForeground(AppTheme.TEXT_MUTED);
        footer.add(count, BorderLayout.WEST);
        JButton closeBtn = buildActionButton("閉じる", new Color(220,140,60), Color.WHITE);
        closeBtn.addActionListener(e -> dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        btnRow.setOpaque(false); btnRow.add(closeBtn);
        footer.add(btnRow, BorderLayout.EAST);
        return footer;
    }

    // ── UI Helpers ───────────────────────────────────────────────────────────
    private JLabel fieldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(AppTheme.bodyFont(12));
        l.setForeground(AppTheme.TEXT_SECONDARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String value) {
        JTextField tf = new JTextField(value);
        tf.setFont(AppTheme.bodyFont(13));
        tf.setForeground(AppTheme.TEXT_PRIMARY);
        tf.setBackground(AppTheme.BG_CARD);
        tf.setCaretColor(AppTheme.ACCENT_LIGHT);
        tf.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            new EmptyBorder(7, 10, 7, 10)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private JComboBox<String> styledCombo(String[] items, String selected) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(AppTheme.bodyFont(12));
        cb.setBackground(AppTheme.BG_CARD);
        cb.setForeground(AppTheme.TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        cb.setSelectedItem(selected);
        return cb;
    }

    private JButton buildActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),7,7));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.bodyFont(12));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6,14,6,14));
        return btn;
    }
}
