package components;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CompositeComponent extends Components {
    private List<Components> children = new ArrayList<>();
    private Mode mode = Mode.SERIES;

    public enum Mode {
        SERIES, PARALLEL
    }

    public CompositeComponent(String id, int x, int y) {
        super(id, x, y);
    }
    
    // Added helper constructor to match your usage
    public CompositeComponent(String id, int x, int y, Mode mode, List<Components> parts) {
        super(id, x, y);
        this.mode = mode;
        this.children = parts;
        this.width = 100;
        this.height = 60;
    }

    public void add(Components component) {
        children.add(component);
    }
    
    public List<Components> getChildren() {
        return children;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }
    
    public Mode getMode() {
        return mode;
    }

    @Override
    public Complex getImpedance(double frequency) {
        if (children.isEmpty()) return new Complex(1e9, 0); //open circuit

        Complex total = children.get(0).getImpedance(frequency);
        for (int i = 1; i < children.size(); i++) {
            Complex nextZ = children.get(i).getImpedance(frequency);
            if (mode == Mode.SERIES) {
                total = total.add(nextZ); // Dùng hàm add của Complex
            } else {
                total = Connections.parallel(total, nextZ); // Dùng hàm parallel của Connections
            }
        }
        return total;
    }
    @Override
    public void setSimulationState(double voltage, double current, double frequency) {
        super.setSimulationState(voltage, current, frequency); // Store state

        if (children.isEmpty()) return;

        if (mode == Mode.SERIES) {
            handleSeriesDistribution(voltage, current, frequency);
        } else {
            handleParallelDistribution(voltage, frequency);
        }
    }
    
    private void handleSeriesDistribution(double totalVoltage, double totalCurrent, double frequency) {
        int openComponents = 0;
        
        // Kiểm tra các linh kiện hở mạch (trở kháng cực lớn)
        for(Components c : children) {
            if(c.getImpedance(frequency).getMagnitude() > 1e8) openComponents++;
        }

        for (Components c : children) {
            Complex zComplex = c.getImpedance(frequency);
            double zMag = zComplex.getMagnitude();
            double vDrop;

            if (openComponents > 0) {
                // Nếu có linh kiện hở mạch, áp sẽ rơi hết vào các điểm hở đó
                vDrop = (zMag > 1e8) ? totalVoltage / openComponents : 0.0;
            } else {
                // Công thức Ohm: V = I * |Z|
                vDrop = totalCurrent * zMag;
            }
            c.setSimulationState(vDrop, totalCurrent, frequency);
        }
    }

    private void handleParallelDistribution(double totalVoltage, double frequency) {
        for (Components c : children) {
            double zMag = c.getImpedance(frequency).getMagnitude();
            double iBranch;

            if (zMag < 1e-9) { 
                // Trường hợp đoản mạch (Short circuit)
                iBranch = (this.getImpedance(frequency).getMagnitude() < 1e-9) ? this.currentFlow : 0;
            } else if (zMag > 1e8) {
                // Trường hợp hở mạch (Open circuit)
                iBranch = 0.0;
            } else {
                // Công thức Ohm: I = V / |Z|
                iBranch = totalVoltage / zMag;
            }
            c.setSimulationState(totalVoltage, iBranch, frequency);
        }
    }
    
    @Override
    public double getResistance() {
    	return getImpedance(0).getReal();
    }

    @Override
    public void draw(Graphics2D g2) {
        int left = x - width/2;
        int top = y - height/2;
        // draw a box representing the composite
        g2.setColor(new Color(220, 220, 250));
        g2.fillRect(left, top, width, height);
        g2.setColor(Color.BLACK);
        g2.drawRect(left, top, width, height);

        g2.setFont(g2.getFont().deriveFont(12f));
        String label = mode == Mode.SERIES ? "Series" : "Parallel";
        FontMetrics fm = g2.getFontMetrics();
        int lx = x - fm.stringWidth(label)/2;
        int ly = y + fm.getAscent()/2;
        g2.drawString(label, lx, ly);

        if (selected) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            Rectangle bounds = getBounds();
            g2.draw(new Rectangle(bounds.x - 2, bounds.y - 2, bounds.width + 4, bounds.height + 4));
            g2.setStroke(new BasicStroke(1));
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }
}