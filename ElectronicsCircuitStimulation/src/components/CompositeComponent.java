package components;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CompositeComponent extends Components {
    private List<Components> children = new ArrayList<>();
    private Mode mode = Mode.SERIES;
    private Point leftTerm;
    private Point rightTerm;
    
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
    public void setPosition(int newX, int newY) {
        int dx = newX - this.x;
        int dy = newY - this.y;
        super.setPosition(newX, newY);
        
        for (Components c : children) {
            Point p = c.getPosition();
            c.setPosition(p.x + dx, p.y + dy);
        }
    }

    @Override
    public Point getConnectorPoint(Components other) {
        if (mode == Mode.PARALLEL) {
            if (other != null && other.getPosition().x < this.x) return leftTerm;
            else return rightTerm;
        }
        
        // For SERIES, snap to the child closest to the external component
        if (children.isEmpty()) return new Point(x,y);
        
        Point target = (other != null) ? other.getPosition() : new Point(x,y);
        Components best = children.get(0);
        double minDst = Double.MAX_VALUE;
        
        for(Components c : children) {
            double d = c.getPosition().distance(target);
            if(d < minDst) { minDst = d; best = c; }
        }
        return best.getPosition();
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
        if (children.isEmpty()) return;

        if (mode == Mode.SERIES) {
            // Series Box
            g2.drawRect(x-20, y-20, 40, 40);
            g2.drawString("Series", x-15, y+5);
        } else {
            // --- PARALLEL RAIL DRAWING ---
            
            // 1. Calculate Bounds
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

            for (Components c : children) {
                Rectangle b = c.getBounds();
                if (b.x < minX) minX = b.x;
                if (b.x + b.width > maxX) maxX = b.x + b.width;
                if (b.y < minY) minY = b.y;
                if (b.y + b.height > maxY) maxY = b.y + b.height;
            }

            int padding = 30;
            int railLeft = minX - padding;
            int railRight = maxX + padding;
            
            // Find Top/Bottom Y levels
            int yTop = minY + (children.get(0).height / 2);
            int yBot = maxY - (children.get(0).height / 2);
            if (children.size() >= 2) {
                 yTop = Math.min(children.get(0).getPosition().y, children.get(1).getPosition().y);
                 yBot = Math.max(children.get(0).getPosition().y, children.get(1).getPosition().y);
            }

            // Update Group Center
            this.x = (railLeft + railRight) / 2;
            this.y = (yTop + yBot) / 2;
            this.width = railRight - railLeft;
            this.height = yBot - yTop;


            Stroke originalStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(1f)); 
            for (Components c : children) {
                // Ensure child doesn't think it's selected individually
                boolean wasSelected = c.selected;
                c.selected = false; // Temporarily disable child selection visuals
                c.draw(g2); 
                c.selected = wasSelected; // Restore state
            }
            
            // 3. Draw Rails (Switch to Bold for the wires)
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f)); 

            for (Components c : children) {
                int cy = c.getPosition().y;
                Rectangle b = c.getBounds();
                
                // Left Rail -> Component
                g2.drawLine(railLeft, cy, b.x, cy);
                // Component -> Right Rail
                g2.drawLine(b.x + b.width, cy, railRight, cy);
            }

            // Vertical Connectors
            g2.drawLine(railLeft, yTop, railLeft, yBot);
            g2.drawLine(railRight, yTop, railRight, yBot);

            // Connectable Dots
            int midY = (yTop + yBot) / 2;
            int dotSize = 10;
            leftTerm = new Point(railLeft, midY);
            rightTerm = new Point(railRight, midY);

            g2.fillOval(leftTerm.x - dotSize/2, leftTerm.y - dotSize/2, dotSize, dotSize);
            g2.fillOval(rightTerm.x - dotSize/2, rightTerm.y - dotSize/2, dotSize, dotSize);
            
            // Restore original stroke
            g2.setStroke(originalStroke);
        }
        
        if (selected) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f)); // Standard bold selection
            
            int selPadding = 8;
            g2.drawRect(x - width/2 - selPadding, y - height/2 - selPadding, 
                        width + (selPadding*2), height + (selPadding*2));
                        
            g2.setStroke(new BasicStroke(1f)); // Reset
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - width / 2, y - height / 2, width, height);
    }
}