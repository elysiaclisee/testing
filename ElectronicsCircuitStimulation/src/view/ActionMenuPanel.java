package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ActionMenuPanel extends JPanel {

    public final JButton seriesBtn = new JButton("Series");
    public final JButton parallelBtn = new JButton("Parallel");
    public final JButton undoBtn = new JButton("Undo");

    public final JLabel titleLabel = new JLabel("Actions");
    public final JLabel hintLabel = new JLabel("<html><div style='width:160px'>Select two components, then choose an action.</div></html>");

    public ActionMenuPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(14, 14, 14, 14));
        setBackground(Color.WHITE);

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        hintLabel.setForeground(new Color(90, 90, 90));

        add(titleLabel);
        add(Box.createVerticalStrut(6));
        add(hintLabel);
        add(Box.createVerticalStrut(14));

        stylePrimary(seriesBtn);
        stylePrimary(parallelBtn);
        styleSecondary(undoBtn);

        add(seriesBtn);
        add(Box.createVerticalStrut(10));
        add(parallelBtn);
        add(Box.createVerticalStrut(18));
        add(new JSeparator());
        add(Box.createVerticalStrut(12));
        add(undoBtn);

        add(Box.createVerticalGlue());
    }

    private void stylePrimary(JButton b) {
        b.setFocusPainted(false);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setPreferredSize(new Dimension(180, 40));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
    }

    private void styleSecondary(JButton b) {
        b.setFocusPainted(false);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        b.setPreferredSize(new Dimension(180, 36));
    }
}