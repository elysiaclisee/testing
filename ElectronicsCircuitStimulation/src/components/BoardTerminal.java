package components;

import utils.Complex;
import java.awt.*;

public class BoardTerminal extends Components {

    public BoardTerminal(String id, int x, int y) {
        super(id, x, y);
        this.width = 14; 
        this.height = 14;
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillOval(x - width/2, y - height/2, width, height);
        g2.drawString(id, x - 5, y - 10);
        drawSelection(g2); 
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - width/2, y - height/2, width, height);
    }

    @Override
    public double getResistance() {
        return 0;
    }

    // Cập nhật trả về Complex(0, 0)
    @Override
    public Complex getImpedance(double frequency) {
        return new Complex(0, 0);
    }
}