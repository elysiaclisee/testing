package view;

import model.CircuitModel;
import components.*;
import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class CircuitPanel extends JPanel {
    // Đẩy Board xuống thấp hơn chút nữa để có chỗ cho 2 dòng thông số
    public final Rectangle boardRect = new Rectangle(10, 180, 965, 470);
    
    public final ToolboxView toolboxView = new ToolboxView();

    public final JButton seriesBtn = new JButton("Series Connect");
    public final JButton parallelBtn = new JButton("Parallel Connect");
    public final JButton undoBtn = new JButton("Undo");
    public final JButton helpBtn = new JButton("Help");

    // --- CÁC LABEL HIỂN THỊ THÔNG SỐ ---
    // Dòng 1: Nguồn, Dòng tổng, Áp tổng (Yêu cầu của bạn)
    public final JLabel mainStatsLabel = new JLabel("Source: 0V | Total I: 0A | V_Bulb: 0V");
    
    // Dòng 2: Trạng thái bóng đèn cụ thể (Sáng/Tối và con số W)
    public final JLabel bulbStatusLabel = new JLabel("Bulb Status: Waiting..."); 
    
    // Dòng 3: Chi tiết linh kiện đang chọn
    public final JLabel selectionLabel = new JLabel("Selection: None");

    public final JRadioButton rbSeriesBulb = new JRadioButton("Mode: Bulb Series (Nối tiếp)", true); 
    public final JRadioButton rbParallelBulb = new JRadioButton("Mode: Bulb Parallel (Song song)");
    public final ButtonGroup modeGroup = new ButtonGroup();

    private final CircuitModel model;

    public CircuitPanel(CircuitModel model) {
        this.model = model;
        setLayout(null);
        setBackground(Color.WHITE);

        int startX = 560; // Cột bên phải

        // 1. Hàng nút bấm (Giữ nguyên)
        seriesBtn.setBounds(startX, 15, 130, 28);
        parallelBtn.setBounds(startX + 140, 15, 130, 28);
        undoBtn.setBounds(startX + 280, 15, 70, 28);
        helpBtn.setBounds(startX + 360, 15, 70, 28);

        // 2. Chế độ chọn (Radio Button)
        rbSeriesBulb.setBounds(startX, 50, 200, 25);
        rbParallelBulb.setBounds(startX + 210, 50, 220, 25);
        rbSeriesBulb.setBackground(Color.WHITE);
        rbParallelBulb.setBackground(Color.WHITE);
        
        // 3. --- KHU VỰC THÔNG SỐ (MỚI) ---
        
        // Dòng 1: Main Stats (To, Đậm, Màu Xanh Dương)
        mainStatsLabel.setBounds(startX, 85, 400, 25);
        mainStatsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        mainStatsLabel.setForeground(new Color(0, 51, 153)); // Xanh đậm
        
        // Dòng 2: Bulb Status (Quan trọng để biết thắng thua - Màu Đỏ/Cam)
        bulbStatusLabel.setBounds(startX, 115, 400, 25);
        bulbStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        bulbStatusLabel.setForeground(new Color(204, 0, 0)); // Đỏ
        
        // Dòng 3: Selection
        selectionLabel.setBounds(startX, 145, 400, 20);
        selectionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        selectionLabel.setForeground(Color.DARK_GRAY);

        // Add components
        add(seriesBtn); add(parallelBtn); add(undoBtn); add(helpBtn);
        add(rbSeriesBulb); add(rbParallelBulb);
        add(mainStatsLabel);
        add(bulbStatusLabel);
        add(selectionLabel);
        
        modeGroup.add(rbSeriesBulb);
        modeGroup.add(rbParallelBulb);

        helpBtn.addActionListener(e -> showHelp());
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ các phần cũ
        toolboxView.draw(g2); 
        
        g2.setColor(new Color(245, 255, 245));
        g2.fill(boardRect);
        g2.setColor(Color.GRAY);
        g2.draw(boardRect);
        
        g2.setFont(g2.getFont().deriveFont(12f));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Circuit Board Area", boardRect.x + 8, boardRect.y + 16);

        // Vẽ dây & linh kiện
        Stroke oldStroke = g2.getStroke();
        for (Wire w : model.getWires()) w.draw(g2);
        g2.setStroke(oldStroke);

        for (Components c : model.getCircuit().getComponents()) {
            c.draw(g2);
            g2.setStroke(oldStroke);
        }
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(this, "Set Power Source > Connect Components > Check Stats!");
    }
}