package controller;

import model.CircuitMemento;
import model.CircuitModel;
import view.CircuitPanel;
import components.*;
import java.awt.*;
import java.awt.event.*;

public class CircuitController {
    private final CircuitModel model;
    private final CircuitPanel view;
    // Thêm biến này để lưu chế độ game hiện tại
    private CircuitManager.ConnectionMode gameMode = CircuitManager.ConnectionMode.SERIES_WITH_BULB;

    public CircuitController(CircuitModel model, CircuitPanel view) {
        this.model = model;
        this.view = view;
        
     // Setup Terminals (Cọc nối nguồn trên board)
        int midY = view.boardRect.y + view.boardRect.height / 2;
        model.termLeft.setPosition(view.boardRect.x, midY);
        model.termRight.setPosition(view.boardRect.x + view.boardRect.width, midY);
        view.repaint();

        // Listeners cho nút bấm
        view.seriesBtn.addActionListener(e -> connectSelected(CompositeComponent.Mode.SERIES));
        view.parallelBtn.addActionListener(e -> connectSelected(CompositeComponent.Mode.PARALLEL));
        view.undoBtn.addActionListener(e -> undo());

        // Listener cho chế độ bóng đèn (Sửa lỗi logic cũ)
        ActionListener modeListener = e -> {
            if (view.rbSeriesBulb.isSelected()) {
                this.gameMode = CircuitManager.ConnectionMode.SERIES_WITH_BULB;
            } else {
                this.gameMode = CircuitManager.ConnectionMode.PARALLEL_WITH_BULB;
            }
            updateCircuit(); 
        };
        view.rbSeriesBulb.addActionListener(modeListener);
        view.rbParallelBulb.addActionListener(modeListener);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePress(e);
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (model.dragging != null) {
                    if (!view.boardRect.contains(e.getPoint())) {
                        model.circuit.removeComponent(model.dragging);
                    }
                    model.dragging = null;
                    updateCircuit();
                    view.repaint();
                }
            }
        };
        view.addMouseListener(ma);
        view.addMouseMotionListener(ma);
        
        // Gọi update lần đầu để hiện số 0
        updateCircuit();
    }
 // --- PHẦN QUAN TRỌNG NHẤT: LOGIC TÍNH TOÁN VÀ HIỂN THỊ ---
    private void updateCircuit() {
        // 1. Lấy thông số nguồn từ Model (SỬA LỖI: Không tìm component nữa)
        double vSource = model.circuit.getSourceVoltage();
        double fSource = model.circuit.getSourceFrequency();

        // 2. Tìm bóng đèn trên mạch
        Bulb targetBulb = null;
        for (Components c : model.circuit.getComponents()) {
            if (c instanceof Bulb) {
                targetBulb = (Bulb) c;
                break;
            }
        }
        
        // 3. Cập nhật Label Dòng 1 (Main Stats) ngay lập tức
        // Nếu chưa set nguồn thì báo lỗi ở dòng status
        if (vSource <= 0) {
            view.mainStatsLabel.setText("Source: NOT SET (0V) | Total I: 0.00A");
            view.bulbStatusLabel.setText("Please click Power Source tool to set Voltage.");
            view.bulbStatusLabel.setForeground(Color.RED);
            return;
        }

        if (targetBulb == null) {
            view.mainStatsLabel.setText(String.format("Source: %.0fV %.0fHz | Total I: 0.00A", vSource, fSource));
            view.bulbStatusLabel.setText("Status: Missing Bulb on circuit!");
            view.bulbStatusLabel.setForeground(Color.RED);
            return;
        }

        // 4. Chuẩn bị giả lập
        CompositeComponent root = model.circuit.getRoot();
        if (root == null) {
             // Mạch rỗng, chưa nối gì
             view.mainStatsLabel.setText(String.format("Source: %.0fV | Total I: 0.00A", vSource));
             view.bulbStatusLabel.setText("Status: Connect components to start.");
             return;
        }

        // Tạo một nguồn ảo để ném vào bộ giả lập
        PowerSource virtualSource = new PowerSource("Main", 0, 0, vSource, fSource);

        // 5. GỌI CIRCUIT MANAGER (Class tính toán đã cung cấp trước đó)
        CircuitManager.simulate(virtualSource, root, targetBulb, gameMode);

        // 6. TÍNH TOÁN HIỂN THỊ CHI TIẾT
        // Lấy kết quả từ bóng đèn sau khi mô phỏng
        double iBulb = targetBulb.getCurrentFlow();
        double vBulb = targetBulb.getVoltageDrop();
        double pReal = iBulb * vBulb; // Công suất thực tế
        double pRated = targetBulb.getPowerLimit();

        // Tính tổng dòng mạch (Tùy theo mode)
        double totalI;
        if (gameMode == CircuitManager.ConnectionMode.SERIES_WITH_BULB) {
            totalI = iBulb; // Nối tiếp thì dòng như nhau
        } else {
            // Song song: I_total = I_bulb + I_user_circuit
            totalI = iBulb + root.getCurrentFlow();
        }

        // 7. CẬP NHẬT GIAO DIỆN THEO YÊU CẦU
        
        // Dòng 1: Source + Total I + V_Bulb
        view.mainStatsLabel.setText(String.format("Source: %.0fV | Total I: %.2fA | V_Bulb: %.2fV", 
                vSource, totalI, vBulb));

        // Dòng 2: Trạng thái bóng đèn + Số liệu cụ thể
        String stateText;
        Color stateColor;
        
        if (pReal >= pRated * 1.5) {
            stateText = "BLOWN (Cháy)";
            stateColor = Color.RED;
        } else if (pReal >= pRated * 0.8) { // Sáng tốt
            stateText = "BRIGHT (Sáng mạnh)";
            stateColor = new Color(0, 153, 0); // Xanh lá
        } else if (pReal >= pRated * 0.4) { // Sáng yếu
            stateText = "DIM (Sáng yếu)";
            stateColor = new Color(204, 204, 0); // Vàng đất
        } else {
            stateText = "OFF (Chưa đủ áp)";
            stateColor = Color.DARK_GRAY;
        }
        
        // Chuỗi hiển thị: "Status: OFF (Real: 10W / Rated: 100W)"
        view.bulbStatusLabel.setText(String.format("Bulb: %s (Real: %.1fW / Rated: %.0fW)", 
                stateText, pReal, pRated));
        view.bulbStatusLabel.setForeground(stateColor);

        // Dòng 3: Selection (Giữ nguyên)
        if (model.firstSelected != null) {
            String txt = model.firstSelected.getId();
            if (model.firstSelected instanceof Resistor) txt += String.format(" (%.0fΩ)", model.firstSelected.getResistance());
            view.selectionLabel.setText("Selected: " + txt);
        } else {
            view.selectionLabel.setText("Selected: None");
        }
        
        view.repaint();
    }
    
    // --- CÁC HÀM HỖ TRỢ MOUSE (Rút gọn cho ngắn code hiển thị ở đây) ---
    private void handleMousePress(MouseEvent e) {
        Point p = e.getPoint();
        Toolbox.Tool t = view.toolboxView.hitTool(p);
        if (t != null) {
            if (t == Toolbox.Tool.POWER_SOURCE) {
                double[] vals = view.toolboxView.promptForPowerSource();
                if (vals != null) {
                    view.toolboxView.updatePowerSourceDisplay(vals[0], vals[1]);
                    model.circuit.setPowerSupply(vals[0], vals[1]);
                    updateCircuit(); // Cập nhật ngay khi nhập xong
                }
                return;
            }
            // Logic thêm linh kiện khác...
            Components c = createComponent(t);
            if(c != null) {
                model.circuit.addComponent(c);
                updateCircuit();
                view.repaint();
            }
            return;
        }
        
        Components hit = componentAt(p);
        if (hit != null) {
            handleSelection(hit, e.isControlDown());
            model.dragging = hit;
            model.dragOffset = new Point(p.x - hit.getPosition().x, p.y - hit.getPosition().y);
        } else if (view.boardRect.contains(p)) {
            clearSelection();
        }
        updateCircuit();
        view.repaint();
    }
    
    private Components createComponent(Toolbox.Tool t) {
        // 1. Xử lý Resistor (Điện trở)
        if (t == Toolbox.Tool.RESISTOR) {
            Double v = view.toolboxView.promptForValue("Resistor", "Ohm");
            return (v != null) ? Toolbox.create(t, 200, 200, v) : null;
        }
        
        // 2. Xử lý Capacitor (Tụ điện) - ĐÃ BỔ SUNG
        if (t == Toolbox.Tool.CAPACITOR) {
            Double v = view.toolboxView.promptForValue("Capacitor", "F"); // Đơn vị Farad
            return (v != null) ? Toolbox.create(t, 200, 200, v) : null;
        }

        // 3. Xử lý Inductor (Cuộn cảm) - ĐÃ BỔ SUNG
        if (t == Toolbox.Tool.INDUCTOR) {
            Double v = view.toolboxView.promptForValue("Inductor", "H"); // Đơn vị Henry
            return (v != null) ? Toolbox.create(t, 200, 200, v) : null;
        }

        // 4. Xử lý Bulb (Bóng đèn)
        if (t == Toolbox.Tool.BULB) {
            return Toolbox.create(t, 200, 200);
        }

        return null;
    }    private void handleMouseDrag(MouseEvent e) {
        if (model.dragging != null) {
            model.dragging.setPosition(e.getX() - model.dragOffset.x, e.getY() - model.dragOffset.y);
            updateCircuit();
            view.repaint();
        }
    }
    private void handleSelection(Components hit, boolean ctrl) {
        if(!ctrl) clearSelection();
        hit.setSelected(!hit.isSelected());
        if(hit.isSelected()) {
            if(model.firstSelected == null) model.firstSelected = hit;
            else model.secondSelected = hit;
        }
    }

    private void undo() {
        if (model.undoStack.isEmpty()) return;
        CircuitMemento memento = model.undoStack.remove(model.undoStack.size() - 1);
        model.redoStack.add(new CircuitMemento(model.circuit, model.wires));
        restoreState(memento);
        view.repaint();
    }
    private void restoreState(CircuitMemento memento) {
        model.circuit.setComponents(memento.getComponents());
        model.wires.clear();
        model.wires.addAll(memento.getWires());
        updateCircuit();
    }

    // --- CONNECT LOGIC ---
    private void connectSelected(CompositeComponent.Mode mode) {
        // Logic connect cũ
        if(model.firstSelected != null && model.secondSelected != null) {
            model.saveState();
            model.addWire(model.firstSelected, model.secondSelected, 
                (mode == CompositeComponent.Mode.SERIES) ? Wire.Type.SERIES : Wire.Type.PARALLEL);
            updateCircuit();
            view.repaint();
        }
    }
        
    private void clearSelection() {
        for (Components c : model.circuit.getComponents()) c.setSelected(false);
        model.firstSelected = null;
        model.secondSelected = null;
    }

    private Components componentAt(Point p) {
        for(Components c : model.circuit.getComponents()) if(c.contains(p)) return c;
        return null;
    }
}