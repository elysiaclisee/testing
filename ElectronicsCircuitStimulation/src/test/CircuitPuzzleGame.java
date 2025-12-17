package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

// ============= Component Classes =============

abstract class Component {
    protected int x, y;
    protected int width = 60;
    protected int height = 40;
    protected String id;
    protected double rotation = 0;
    protected List<Terminal> terminals;
    protected boolean selected = false;
    
    public Component(int x, int y, String id) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.terminals = new ArrayList<>();
        initializeTerminals();
    }
    
    protected abstract void initializeTerminals();
    public abstract void draw(Graphics2D g);
    public abstract String getType();
    public abstract String getInfo();
    
    public boolean contains(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
    
    public void move(int dx, int dy) {
        x += dx;
        y += dy;
        for (Terminal t : terminals) {
            t.updatePosition();
        }
    }
    
    public void rotate() {
        rotation = (rotation + 90) % 360;
        for (Terminal t : terminals) {
            t.updatePosition();
        }
    }
    
    public List<Terminal> getTerminals() {
        return terminals;
    }
    
    public String getId() {
        return id;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
}

class Terminal {
    private Component parent;
    private int offsetX, offsetY;
    private int x, y;
    private Connection connection;
    
    public Terminal(Component parent, int offsetX, int offsetY) {
        this.parent = parent;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        updatePosition();
    }
    
    public void updatePosition() {
        double rad = Math.toRadians(parent.rotation);
        int rx = (int)(offsetX * Math.cos(rad) - offsetY * Math.sin(rad));
        int ry = (int)(offsetX * Math.sin(rad) + offsetY * Math.cos(rad));
        x = parent.x + parent.width / 2 + rx;
        y = parent.y + parent.height / 2 + ry;
    }
    
    public boolean contains(int px, int py) {
        return Math.abs(px - x) <= 5 && Math.abs(py - y) <= 5;
    }
    
    public void setConnection(Connection conn) {
        this.connection = conn;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public boolean isConnected() {
        return connection != null;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public Component getParent() { return parent; }
}

class Resistor extends Component {
    private double resistance;
    
    public Resistor(int x, int y, String id, double resistance) {
        super(x, y, id);
        this.resistance = resistance;
    }
    
    @Override
    protected void initializeTerminals() {
        terminals.add(new Terminal(this, -30, 0));
        terminals.add(new Terminal(this, 30, 0));
    }
    
    @Override
    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(Math.toRadians(rotation), x + width/2, y + height/2);
        
        if (selected) {
            g2.setColor(new Color(100, 150, 255));
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
        }
        
        int cx = x + width/2;
        int cy = y + height/2;
        
        // Draw zigzag pattern
        int[] xPoints = {cx-25, cx-20, cx-15, cx-10, cx-5, cx, cx+5, cx+10, cx+15, cx+20, cx+25};
        int[] yPoints = {cy, cy-10, cy+10, cy-10, cy+10, cy-10, cy+10, cy-10, cy+10, cy-10, cy};
        g2.drawPolyline(xPoints, yPoints, 11);
        
        // Draw terminals
        g2.setColor(Color.RED);
        for (Terminal t : terminals) {
            g2.fillOval(t.getX() - 4, t.getY() - 4, 8, 8);
        }
        
        // Draw label
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString(resistance + "Ω", cx - 15, cy + 20);
        
        g2.dispose();
    }
    
    @Override
    public String getType() {
        return "Resistor";
    }
    
    @Override
    public String getInfo() {
        return "Resistance: " + resistance + " Ω";
    }
    
    public double getResistance() {
        return resistance;
    }
}

class Capacitor extends Component {
    private double capacitance;
    
    public Capacitor(int x, int y, String id, double capacitance) {
        super(x, y, id);
        this.capacitance = capacitance;
    }
    
    @Override
    protected void initializeTerminals() {
        terminals.add(new Terminal(this, -30, 0));
        terminals.add(new Terminal(this, 30, 0));
    }
    
    @Override
    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(Math.toRadians(rotation), x + width/2, y + height/2);
        
        if (selected) {
            g2.setColor(new Color(100, 150, 255));
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
        }
        
        int cx = x + width/2;
        int cy = y + height/2;
        
        // Draw plates
        g2.drawLine(cx-5, cy-15, cx-5, cy+15);
        g2.drawLine(cx+5, cy-15, cx+5, cy+15);
        
        // Draw wires
        g2.drawLine(cx-25, cy, cx-5, cy);
        g2.drawLine(cx+5, cy, cx+25, cy);
        
        // Draw terminals
        g2.setColor(Color.RED);
        for (Terminal t : terminals) {
            g2.fillOval(t.getX() - 4, t.getY() - 4, 8, 8);
        }
        
        // Draw label
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString(capacitance + "μF", cx - 15, cy + 25);
        
        g2.dispose();
    }
    
    @Override
    public String getType() {
        return "Capacitor";
    }
    
    @Override
    public String getInfo() {
        return "Capacitance: " + capacitance + " μF";
    }
    
    public double getCapacitance() {
        return capacitance;
    }
}

class Wire extends Component {
    public Wire(int x, int y, String id) {
        super(x, y, id);
        this.width = 40;
        this.height = 30;
    }
    
    @Override
    protected void initializeTerminals() {
        terminals.add(new Terminal(this, -20, 0));
        terminals.add(new Terminal(this, 20, 0));
    }
    
    @Override
    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(Math.toRadians(rotation), x + width/2, y + height/2);
        
        if (selected) {
            g2.setColor(new Color(100, 150, 255));
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
        }
        
        int cx = x + width/2;
        int cy = y + height/2;
        
        // Draw wire
        g2.drawLine(cx-20, cy, cx+20, cy);
        
        // Draw terminals
        g2.setColor(Color.RED);
        for (Terminal t : terminals) {
            g2.fillOval(t.getX() - 4, t.getY() - 4, 8, 8);
        }
        
        g2.dispose();
    }
    
    @Override
    public String getType() {
        return "Wire";
    }
    
    @Override
    public String getInfo() {
        return "Resistance: ~0 Ω";
    }
}

class Battery extends Component {
    private double voltage;
    
    public Battery(int x, int y, String id, double voltage) {
        super(x, y, id);
        this.voltage = voltage;
        this.height = 50;
    }
    
    @Override
    protected void initializeTerminals() {
        terminals.add(new Terminal(this, -30, 0));
        terminals.add(new Terminal(this, 30, 0));
    }
    
    @Override
    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(Math.toRadians(rotation), x + width/2, y + height/2);
        
        if (selected) {
            g2.setColor(new Color(100, 150, 255));
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
        }
        
        int cx = x + width/2;
        int cy = y + height/2;
        
        // Draw battery symbol
        g2.drawLine(cx-10, cy-15, cx-10, cy+15); // Negative (long)
        g2.drawLine(cx+10, cy-8, cx+10, cy+8);   // Positive (short)
        
        // Draw wires
        g2.drawLine(cx-25, cy, cx-10, cy);
        g2.drawLine(cx+10, cy, cx+25, cy);
        
        // Draw + and -
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("-", cx-18, cy+5);
        g2.drawString("+", cx+15, cy+5);
        
        // Draw terminals
        g2.setColor(Color.RED);
        for (Terminal t : terminals) {
            g2.fillOval(t.getX() - 4, t.getY() - 4, 8, 8);
        }
        
        // Draw label
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString(voltage + "V", cx - 12, cy + 30);
        
        g2.dispose();
    }
    
    @Override
    public String getType() {
        return "Battery";
    }
    
    @Override
    public String getInfo() {
        return "Voltage: " + voltage + " V";
    }
    
    public double getVoltage() {
        return voltage;
    }
}

class Bulb extends Component {
    private boolean lit = false;
    
    public Bulb(int x, int y, String id) {
        super(x, y, id);
        this.height = 50;
    }
    
    @Override
    protected void initializeTerminals() {
        terminals.add(new Terminal(this, -30, 0));
        terminals.add(new Terminal(this, 30, 0));
    }
    
    @Override
    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.rotate(Math.toRadians(rotation), x + width/2, y + height/2);
        
        int cx = x + width/2;
        int cy = y + height/2;
        
        // Draw bulb circle
        if (lit) {
            g2.setColor(new Color(255, 255, 100));
            g2.fillOval(cx-15, cy-15, 30, 30);
            g2.setColor(new Color(255, 200, 0));
        } else {
            g2.setColor(Color.WHITE);
            g2.fillOval(cx-15, cy-15, 30, 30);
            g2.setColor(Color.BLACK);
        }
        
        if (selected) {
            g2.setStroke(new BasicStroke(3));
        } else {
            g2.setStroke(new BasicStroke(2));
        }
        g2.drawOval(cx-15, cy-15, 30, 30);
        
        // Draw filament
        g2.drawLine(cx-8, cy-8, cx+8, cy+8);
        g2.drawLine(cx-8, cy+8, cx+8, cy-8);
        
        // Draw wires
        g2.setColor(Color.BLACK);
        g2.drawLine(cx-25, cy, cx-15, cy);
        g2.drawLine(cx+15, cy, cx+25, cy);
        
        // Draw terminals
        g2.setColor(Color.RED);
        for (Terminal t : terminals) {
            g2.fillOval(t.getX() - 4, t.getY() - 4, 8, 8);
        }
        
        g2.dispose();
    }
    
    public void setLit(boolean lit) {
        this.lit = lit;
    }
    
    public boolean isLit() {
        return lit;
    }
    
    @Override
    public String getType() {
        return "Bulb";
    }
    
    @Override
    public String getInfo() {
        return "Status: " + (lit ? "LIT" : "OFF");
    }
}

class Connection {
    private Terminal terminal1;
    private Terminal terminal2;
    
    public Connection(Terminal t1, Terminal t2) {
        this.terminal1 = t1;
        this.terminal2 = t2;
        t1.setConnection(this);
        t2.setConnection(this);
    }
    
    public void draw(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.setStroke(new BasicStroke(2));
        g.drawLine(terminal1.getX(), terminal1.getY(), 
                   terminal2.getX(), terminal2.getY());
    }
    
    public Terminal getTerminal1() { return terminal1; }
    public Terminal getTerminal2() { return terminal2; }
    
    public void disconnect() {
        terminal1.setConnection(null);
        terminal2.setConnection(null);
    }
}

// ============= Circuit Board and Simulation =============

class CircuitBoard {
    private List<Component> components;
    private List<Connection> connections;
    private int componentCounter = 0;
    
    public CircuitBoard() {
        components = new ArrayList<>();
        connections = new ArrayList<>();
    }
    
    public void addComponent(Component comp) {
        components.add(comp);
    }
    
    public void removeComponent(Component comp) {
        // Remove all connections to this component
        List<Connection> toRemove = new ArrayList<>();
        for (Connection conn : connections) {
            if (conn.getTerminal1().getParent() == comp || 
                conn.getTerminal2().getParent() == comp) {
                toRemove.add(conn);
            }
        }
        for (Connection conn : toRemove) {
            removeConnection(conn);
        }
        components.remove(comp);
    }
    
    public void addConnection(Connection conn) {
        connections.add(conn);
    }
    
    public void removeConnection(Connection conn) {
        conn.disconnect();
        connections.remove(conn);
    }
    
    public List<Component> getComponents() {
        return components;
    }
    
    public List<Connection> getConnections() {
        return connections;
    }
    
    public String getNextId(String type) {
        return type + (++componentCounter);
    }
    
    public void simulate() {
        // Find battery and bulb
        Battery battery = null;
        Bulb bulb = null;
        
        for (Component comp : components) {
            if (comp instanceof Battery) battery = (Battery) comp;
            if (comp instanceof Bulb) bulb = (Bulb) comp;
        }
        
        if (battery == null || bulb == null) {
            if (bulb != null) bulb.setLit(false);
            return;
        }
        
        // Check if there's a complete circuit using BFS
        boolean circuitComplete = hasCompletePath(battery, bulb);
        
        if (circuitComplete) {
            // Calculate total resistance
            double totalResistance = calculateTotalResistance(battery, bulb);
            
            // Simple check: if resistance is reasonable, light the bulb
            if (totalResistance > 0 && totalResistance < 10000) {
                bulb.setLit(true);
            } else {
                bulb.setLit(false);
            }
        } else {
            bulb.setLit(false);
        }
    }
    
    private boolean hasCompletePath(Component start, Component end) {
        Set<Component> visited = new HashSet<>();
        Queue<Component> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);
        
        while (!queue.isEmpty()) {
            Component current = queue.poll();
            
            if (current == end) {
                return true;
            }
            
            // Find connected components
            for (Terminal t : current.getTerminals()) {
                if (t.isConnected()) {
                    Connection conn = t.getConnection();
                    Terminal other = (conn.getTerminal1() == t) ? 
                                    conn.getTerminal2() : conn.getTerminal1();
                    Component nextComp = other.getParent();
                    
                    if (!visited.contains(nextComp)) {
                        visited.add(nextComp);
                        queue.add(nextComp);
                    }
                }
            }
        }
        
        return false;
    }
    
    private double calculateTotalResistance(Component start, Component end) {
        double totalR = 0;
        Set<Component> visited = new HashSet<>();
        
        // Simple series resistance calculation
        Queue<Component> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);
        
        while (!queue.isEmpty()) {
            Component current = queue.poll();
            
            if (current instanceof Resistor) {
                totalR += ((Resistor) current).getResistance();
            }
            
            for (Terminal t : current.getTerminals()) {
                if (t.isConnected()) {
                    Connection conn = t.getConnection();
                    Terminal other = (conn.getTerminal1() == t) ? 
                                    conn.getTerminal2() : conn.getTerminal1();
                    Component nextComp = other.getParent();
                    
                    if (!visited.contains(nextComp)) {
                        visited.add(nextComp);
                        queue.add(nextComp);
                    }
                }
            }
        }
        
        return totalR;
    }
    
    public String getSimulationResults() {
        Battery battery = null;
        Bulb bulb = null;
        
        for (Component comp : components) {
            if (comp instanceof Battery) battery = (Battery) comp;
            if (comp instanceof Bulb) bulb = (Bulb) comp;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Simulation Results ===\n");
        
        if (battery != null) {
            sb.append("Battery: ").append(battery.getVoltage()).append(" V\n");
        } else {
            sb.append("No battery in circuit\n");
        }
        
        if (bulb != null) {
            sb.append("Bulb status: ").append(bulb.isLit() ? "LIT ✓" : "OFF ✗").append("\n");
        } else {
            sb.append("No bulb in circuit\n");
        }
        
        if (battery != null && bulb != null) {
            boolean complete = hasCompletePath(battery, bulb);
            sb.append("Circuit: ").append(complete ? "Complete" : "Incomplete").append("\n");
            
            if (complete) {
                double resistance = calculateTotalResistance(battery, bulb);
                sb.append("Total Resistance: ").append(String.format("%.2f", resistance)).append(" Ω\n");
                
                if (resistance > 0) {
                    double current = battery.getVoltage() / resistance;
                    sb.append("Current: ").append(String.format("%.3f", current)).append(" A");
                }
            }
        }
        
        return sb.toString();
    }
}

// ============= GUI =============

class CircuitPanel extends JPanel {
    private CircuitBoard board;
    private Component selectedComponent = null;
    private Component draggingComponent = null;
    private Terminal connectingTerminal = null;
    private Point dragOffset = new Point();
    private Point mousePos = new Point();
    
    public CircuitPanel(CircuitBoard board) {
        this.board = board;
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(240, 240, 240));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
                repaint();
            }
        });
    }
    
    private void handleMousePressed(MouseEvent e) {
        // Check if clicking on a terminal
        for (Component comp : board.getComponents()) {
            for (Terminal t : comp.getTerminals()) {
                if (t.contains(e.getX(), e.getY())) {
                    connectingTerminal = t;
                    return;
                }
            }
        }
        
        // Check if clicking on a component
        for (int i = board.getComponents().size() - 1; i >= 0; i--) {
            Component comp = board.getComponents().get(i);
            if (comp.contains(e.getX(), e.getY())) {
                if (selectedComponent != null) {
                    selectedComponent.setSelected(false);
                }
                selectedComponent = comp;
                comp.setSelected(true);
                draggingComponent = comp;
                dragOffset.x = e.getX() - comp.getX();
                dragOffset.y = e.getY() - comp.getY();
                repaint();
                return;
            }
        }
        
        // Deselect if clicking on empty space
        if (selectedComponent != null) {
            selectedComponent.setSelected(false);
            selectedComponent = null;
            repaint();
        }
    }
    
    private void handleMouseDragged(MouseEvent e) {
        if (draggingComponent != null) {
            int newX = e.getX() - dragOffset.x;
            int newY = e.getY() - dragOffset.y;
            draggingComponent.move(newX - draggingComponent.getX(), 
                                  newY - draggingComponent.getY());
            repaint();
        } else if (connectingTerminal != null) {
            mousePos = e.getPoint();
            repaint();
        }
    }
    
    private void handleMouseReleased(MouseEvent e) {
        if (connectingTerminal != null) {
            // Check if released on another terminal
            for (Component comp : board.getComponents()) {
                for (Terminal t : comp.getTerminals()) {
                    if (t != connectingTerminal && t.contains(e.getX(), e.getY())) {
                        if (!connectingTerminal.isConnected() && !t.isConnected()) {
                            board.addConnection(new Connection(connectingTerminal, t));
                        }
                        break;
                    }
                }
            }
            connectingTerminal = null;
            repaint();
        }
        draggingComponent = null;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw grid
        g2.setColor(new Color(220, 220, 220));
        for (int i = 0; i < getWidth(); i += 20) {
            g2.drawLine(i, 0, i, getHeight());
        }
        for (int i = 0; i < getHeight(); i += 20) {
            g2.drawLine(0, i, getWidth(), i);
        }
        
        // Draw connections
        for (Connection conn : board.getConnections()) {
            conn.draw(g2);
        }
        
        // Draw connecting line
        if (connectingTerminal != null) {
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, 
                        BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            g2.drawLine(connectingTerminal.getX(), connectingTerminal.getY(), 
                       mousePos.x, mousePos.y);
        }
        
        // Draw components
        for (Component comp : board.getComponents()) {
            comp.draw(g2);
        }
    }
    
    public void rotateSelected() {
        if (selectedComponent != null) {
            selectedComponent.rotate();
            repaint();
        }
    }
    
    public void deleteSelected() {
        if (selectedComponent != null) {
            board.removeComponent(selectedComponent);
            selectedComponent = null;
            repaint();
        }
    }
    
    public Component getSelectedComponent() {
        return selectedComponent;
    }
}

public class CircuitPuzzleGame extends JFrame {
    private CircuitBoard board;
    private CircuitPanel circuitPanel;
    private JTextArea outputArea;
    
    public CircuitPuzzleGame() {
        setTitle("Electronic Circuit Puzzle Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        board = new CircuitBoard();
        circuitPanel = new CircuitPanel(board);
        
        // Create toolbox
        JPanel toolbox = createToolbox();
        add(toolbox, BorderLayout.WEST);
        
        // Add circuit panel
        add(circuitPanel, BorderLayout.CENTER);
        
        // Create output panel
        JPanel outputPanel = createOutputPanel();
        add(outputPanel, BorderLayout.EAST);
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private JPanel createToolbox() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(150, 600));
        panel.setBackground(new Color(200, 200, 200));
        panel.setBorder(BorderFactory.createTitledBorder("Toolbox"));
        
        JButton resistorBtn = new JButton("Resistor (100Ω)");
        resistorBtn.addActionListener(e -> {
            Resistor r = new Resistor(100, 100, board.getNextId("R"), 100);
            board.addComponent(r);
            circuitPanel.repaint();
        });
        
        JButton resistor2Btn = new JButton("Resistor (220Ω)");
        resistor2Btn.addActionListener(e -> {
            Resistor r = new Resistor(100, 100, board.getNextId("R"), 220);
            board.addComponent(r);
            circuitPanel.repaint();
        });
        
        JButton capacitorBtn = new JButton("Capacitor (10μF)");
        capacitorBtn.addActionListener(e -> {
            Capacitor c = new Capacitor(150, 150, board.getNextId("C"), 10);
            board.addComponent(c);
            circuitPanel.repaint();
        });
        
        JButton wireBtn = new JButton("Wire");
        wireBtn.addActionListener(e -> {
            Wire w = new Wire(200, 200, board.getNextId("W"));
            board.addComponent(w);
            circuitPanel.repaint();
        });
        
        JButton batteryBtn = new JButton("Battery (9V)");
        batteryBtn.addActionListener(e -> {
            Battery b = new Battery(250, 250, board.getNextId("B"), 9);
            board.addComponent(b);
            circuitPanel.repaint();
        });
        
        JButton bulbBtn = new JButton("Bulb");
        bulbBtn.addActionListener(e -> {
            Bulb b = new Bulb(300, 300, board.getNextId("L"));
            board.addComponent(b);
            circuitPanel.repaint();
        });
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(resistorBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(resistor2Btn);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(capacitorBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(wireBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(batteryBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(bulbBtn);
        
        return panel;
    }
    
    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(250, 600));
        panel.setBorder(BorderFactory.createTitledBorder("Simulation Output"));
        
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(new Color(180, 180, 180));
        
        JButton simulateBtn = new JButton("Run Simulation");
        simulateBtn.addActionListener(e -> {
            board.simulate();
            outputArea.setText(board.getSimulationResults());
            circuitPanel.repaint();
        });
        
        JButton rotateBtn = new JButton("Rotate (R)");
        rotateBtn.addActionListener(e -> {
            circuitPanel.rotateSelected();
        });
        
        JButton deleteBtn = new JButton("Delete (Del)");
        deleteBtn.addActionListener(e -> {
            circuitPanel.deleteSelected();
            outputArea.setText("");
        });
        
        JButton clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Clear entire circuit?", "Confirm", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                board.getComponents().clear();
                board.getConnections().clear();
                outputArea.setText("");
                circuitPanel.repaint();
            }
        });
        
        JButton infoBtn = new JButton("Component Info");
        infoBtn.addActionListener(e -> {
            Component selected = circuitPanel.getSelectedComponent();
            if (selected != null) {
                String info = "Type: " + selected.getType() + "\n" +
                            "ID: " + selected.getId() + "\n" +
                            selected.getInfo();
                JOptionPane.showMessageDialog(this, info, 
                    "Component Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No component selected", "Info", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        panel.add(simulateBtn);
        panel.add(rotateBtn);
        panel.add(deleteBtn);
        panel.add(clearBtn);
        panel.add(infoBtn);
        
        // Add keyboard shortcuts
        circuitPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "rotate");
        circuitPanel.getActionMap().put("rotate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitPanel.rotateSelected();
            }
        });
        
        circuitPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        circuitPanel.getActionMap().put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitPanel.deleteSelected();
            }
        });
        
        return panel;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CircuitPuzzleGame game = new CircuitPuzzleGame();
            game.setVisible(true);
            
            // Show welcome message
            JOptionPane.showMessageDialog(game, 
                "Welcome to Circuit Puzzle Game!\n\n" +
                "Instructions:\n" +
                "1. Click components in toolbox to add them\n" +
                "2. Drag components to move them\n" +
                "3. Click and drag red terminals to connect\n" +
                "4. Select a component and press R to rotate\n" +
                "5. Press Delete to remove selected component\n" +
                "6. Click 'Run Simulation' to test your circuit\n\n" +
                "Goal: Connect battery and bulb to light it up!",
                "Welcome", JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
