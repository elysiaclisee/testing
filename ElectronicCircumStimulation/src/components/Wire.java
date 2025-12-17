package components;

import java.awt.*;

public class Wire implements Cloneable {
    public enum Type { SERIES, PARALLEL }
    private Components a, b;
    private Type type;

    public Wire(Components a, Components b, Type type) {
        this.a = a;
        this.b = b;
        this.type = type;
    }

    public Components getA() { return a; }
    public Components getB() { return b; }
    public Type getType() { return type; }

    @Override
    public Wire clone() {
        try {
            return (Wire) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Should not happen
        }
    }

    public void remap(java.util.Map<String, Components> componentMap) {
        a = componentMap.get(a.getId());
        b = componentMap.get(b.getId());
    }

    public void draw(Graphics2D g2) {
        Rectangle ra = a.getBounds();
        Rectangle rb = b.getBounds();

        Point aL = new Point(ra.x, ra.y + ra.height / 2);
        Point aR = new Point(ra.x + ra.width, ra.y + ra.height / 2);

        Point bL = new Point(rb.x, rb.y + rb.height / 2);
        Point bR = new Point(rb.x + rb.width, rb.y + rb.height / 2);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));

        if (type == Type.SERIES) {
            // series: nối từ aR sang bL (đơn giản, đẹp)
            g2.drawLine(aR.x, aR.y, bL.x, bL.y);
            return;
        }

        // ===== PARALLEL: 2 NODE + 2 NHÁNH =====

        // leftNode và rightNode là 2 "đầu mạch" chung
        int leftX  = Math.min(aL.x, bL.x) - 40;
        int rightX = Math.max(aR.x, bR.x) + 40;

        // node Y lấy trung bình để nhìn cân
        int topY = Math.min(aL.y, bL.y);
        int botY = Math.max(aL.y, bL.y);

        // Vẽ 2 node (chấm đen)
        int nodeR = 6;
        g2.fillOval(leftX - nodeR,  (topY + botY) / 2 - nodeR, nodeR * 2, nodeR * 2);
        g2.fillOval(rightX - nodeR, (topY + botY) / 2 - nodeR, nodeR * 2, nodeR * 2);

        // Dây nhánh 1: leftNode -> aL, và aR -> rightNode
        g2.drawLine(leftX, aL.y, aL.x, aL.y);
        g2.drawLine(aR.x, aR.y, rightX, aR.y);

        // Dây nhánh 2: leftNode -> bL, và bR -> rightNode
        g2.drawLine(leftX, bL.y, bL.x, bL.y);
        g2.drawLine(bR.x, bR.y, rightX, bR.y);

        // 2 "trục dọc" nối các nhánh vào node (đẹp như hình 2)
        g2.drawLine(leftX, topY, leftX, botY);
        g2.drawLine(rightX, topY, rightX, botY);
    }

}