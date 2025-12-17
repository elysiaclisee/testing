package view;

import model.CircuitModel;
import components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class CircuitPanel extends JPanel {
    private final CircuitModel model;

    // ====== Toolbox ======
    public final ToolboxView toolboxView = new ToolboxView();
    private Rectangle toolboxDrawRect = new Rectangle(10, 10, 560, 120); // sẽ update trong doLayout()

    // ====== Board ======
    public final Rectangle boardRect = new Rectangle(10, 150, 965, 500);

    // ====== Controller dùng ======
    public final JButton seriesBtn = new JButton("Series");
    public final JButton parallelBtn = new JButton("Parallel");
    public final JButton undoBtn = new JButton("Undo");
    public final JButton helpBtn = new JButton("Help");

    // Bulb mode buttons (1 lần chọn, có phản ứng màu)
    public final JToggleButton bulbParallelBtn = new JToggleButton("parallel");
    public final JToggleButton bulbSeriesBtn   = new JToggleButton("series");

    public final JLabel instructionLabel = new JLabel("U total: - | I total: - | Z total: -");
    public final JLabel circuitStatsLabel = new JLabel("Set source.");
    public final JLabel componentValuesLabel = new JLabel("Selected: None");

    private final JLabel hintLabel = new JLabel("Click toolbox to add. Drag to move. Select two & connect.");

    // layout slots
    private final JPanel headerRow = new JPanel();
    private final JPanel slotToolbox = new JPanel(null);
    private final JPanel slotMenu = new JPanel(new BorderLayout());
    private final JPanel slotStats = new JPanel();

    // ===== Layout tuning =====
    private static final int GAP_X = 18;           // khoảng cách giữa 3 phần
    private static final int HEADER_H = 120;       // chiều cao vùng header (toolbox/menu/stats)
    private static final int TOOLBOX_MIN_W = 560;  // để đủ 5 item như trước (tăng/giảm nếu UI bạn khác)

    // colors
    private static final Color GRID_BORDER = new Color(60, 60, 60);
    private static final Color ACTIVE_BLUE = new Color(190, 215, 245);
    private static final Color INACTIVE_BG = Color.WHITE;

    public CircuitPanel(CircuitModel model) {
        this.model = model;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.setBorder(new EmptyBorder(8, 10, 0, 10));

        headerRow.setOpaque(false);
        headerRow.setLayout(new BorderLayout(GAP_X, 0)); // gap ngang

        // ===== SLOT TOOLBOX =====
        slotToolbox.setOpaque(false);
        slotToolbox.setPreferredSize(new Dimension(TOOLBOX_MIN_W, HEADER_H));
        slotToolbox.setMinimumSize(new Dimension(TOOLBOX_MIN_W, HEADER_H));
        headerRow.add(slotToolbox, BorderLayout.WEST);

        // ===== SLOT MENU =====
        slotMenu.setOpaque(false);
        slotMenu.add(buildMenuGrid(), BorderLayout.CENTER);
        headerRow.add(slotMenu, BorderLayout.CENTER);

        // ===== SLOT STATS =====
        slotStats.setOpaque(false);
        slotStats.setLayout(new BoxLayout(slotStats, BoxLayout.Y_AXIS));
        slotStats.setBorder(new EmptyBorder(6, 10, 0, 6));

        instructionLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        circuitStatsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        circuitStatsLabel.setForeground(new Color(70, 70, 70));
        componentValuesLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        componentValuesLabel.setForeground(new Color(0, 110, 0));

        slotStats.add(instructionLabel);
        slotStats.add(Box.createVerticalStrut(8));
        slotStats.add(circuitStatsLabel);
        slotStats.add(Box.createVerticalStrut(8));
        slotStats.add(componentValuesLabel);
        slotStats.add(Box.createVerticalGlue());

        headerRow.add(slotStats, BorderLayout.EAST);

        // ===== HINT =====
        hintLabel.setForeground(Color.DARK_GRAY);
        hintLabel.setBorder(new EmptyBorder(6, 0, 6, 0));

        north.add(headerRow, BorderLayout.NORTH);
        north.add(hintLabel, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);

        // Bulb toggle styling + exclusive
        styleBulbToggle(bulbParallelBtn);
        styleBulbToggle(bulbSeriesBtn);
        installExclusiveToggle(bulbParallelBtn, bulbSeriesBtn);
        installExclusiveToggle(bulbSeriesBtn, bulbParallelBtn);

        helpBtn.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                """
                HOW TO USE
                - Click Toolbox to add components
                - Drag to move
                - Click two components to select
                - Press Series / Parallel to connect
                - Undo reverts last action
                """,
                "Help",
                JOptionPane.INFORMATION_MESSAGE
        ));
    }

    private JComponent buildMenuGrid() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createLineBorder(GRID_BORDER));

        styleButton(undoBtn);
        styleButton(helpBtn);
        styleButton(parallelBtn);
        styleButton(seriesBtn);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        // Row 1
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1;
        grid.add(wrapCell(undoBtn), gbc);
        gbc.gridx = 2; gbc.gridwidth = 2;
        grid.add(wrapCell(helpBtn), gbc);

        // Row 2: header Wire / Bulb
        gbc.gridy = 1;
        gbc.gridx = 0; gbc.gridwidth = 2;
        grid.add(wrapCell(makeHeader("Wire")), gbc);
        gbc.gridx = 2; gbc.gridwidth = 2;
        grid.add(wrapCell(makeHeader("Bulb")), gbc);

        // Row 3
        gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.gridx = 0; grid.add(wrapCell(parallelBtn), gbc);
        gbc.gridx = 1; grid.add(wrapCell(seriesBtn), gbc);
        gbc.gridx = 2; grid.add(wrapCell(bulbParallelBtn), gbc);
        gbc.gridx = 3; grid.add(wrapCell(bulbSeriesBtn), gbc);

        return grid;
    }

    private JLabel makeHeader(String text) {
        JLabel lb = new JLabel(text, SwingConstants.CENTER);
        lb.setOpaque(true);
        lb.setBackground(new Color(245, 245, 245));
        lb.setFont(new Font("SansSerif", Font.BOLD, 12));
        return lb;
    }

    private void styleButton(AbstractButton b) {
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setMargin(new Insets(2, 6, 2, 6));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
    }

    private JComponent wrapCell(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, GRID_BORDER));
        p.setPreferredSize(new Dimension(160, 32));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    public boolean isInToolboxArea(Point p) {
        return toolboxDrawRect.contains(p);
    }

    public Point toToolboxLocal(Point global) {
        return new Point(global.x - toolboxDrawRect.x, global.y - toolboxDrawRect.y);
    }

    @Override
    public void doLayout() {
        super.doLayout();

        Rectangle slot = SwingUtilities.convertRectangle(
                slotToolbox.getParent(),
                slotToolbox.getBounds(),
                this
        );

        int pad = 0;
        toolboxDrawRect = new Rectangle(
                slot.x + pad,
                slot.y + pad,
                Math.max(10, slot.width - pad * 2),
                Math.max(10, slot.height - pad * 2)
        );

        int topH = getComponent(0).getHeight(); // north panel
        boardRect.x = 10;
        boardRect.y = topH + 10;
        boardRect.width = getWidth() - 20;
        boardRect.height = Math.max(200, getHeight() - boardRect.y - 20);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Stroke originalStroke = g2.getStroke();
        Shape originalClip = g2.getClip();
        AffineTransform originalTx = g2.getTransform();

        // TOOLBOX
        g2.setClip(toolboxDrawRect);
        g2.translate(toolboxDrawRect.x, toolboxDrawRect.y);
        toolboxView.draw(g2);
        g2.setTransform(originalTx);
        g2.setClip(originalClip);

        // toolbox bottom border (lấy lại viền dưới)
        g2.setColor(new Color(160, 160, 160));
        g2.drawLine(toolboxDrawRect.x, toolboxDrawRect.y + toolboxDrawRect.height,
                toolboxDrawRect.x + toolboxDrawRect.width, toolboxDrawRect.y + toolboxDrawRect.height);

        // BOARD
        g2.setColor(new Color(245, 255, 245));
        g2.fill(boardRect);
        g2.setColor(Color.GRAY);
        g2.draw(boardRect);
        g2.setFont(g2.getFont().deriveFont(12f));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Circuit Board", boardRect.x + 8, boardRect.y + 16);

        for (Wire w : model.getWires()) w.draw(g2);
        for (Components c : model.getCircuit().getComponents()) c.draw(g2);

        g2.setStroke(originalStroke);
    }

    private void styleBulbToggle(JToggleButton b) {
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setMargin(new Insets(2, 6, 2, 6));
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBackground(INACTIVE_BG);
    }

    private void installExclusiveToggle(JToggleButton self, JToggleButton other) {
        self.addActionListener(e -> {
            // nếu đã chọn rồi -> giữ nguyên (không cho tắt)
            if (self.isSelected()) {
                self.setBackground(ACTIVE_BLUE);
                other.setSelected(false);
                other.setBackground(INACTIVE_BG);
            } else {
                self.setSelected(true);
                self.setBackground(ACTIVE_BLUE);
                other.setSelected(false);
                other.setBackground(INACTIVE_BG);
            }
        });
    }
}
