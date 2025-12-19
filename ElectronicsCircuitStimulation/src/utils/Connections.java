package utils;

public class Connections {

    // Song song: Z_eq = (Z1 * Z2) / (Z1 + Z2)
    public static Complex parallel(Complex d, Complex e) {
        Complex numerator = d.multiply(e);
        Complex denominator = d.add(e);
        
        // Nếu mẫu số = 0 (cộng hưởng nối tiếp LC lý tưởng trong nhánh song song), trả về 0
        if (denominator.getMagnitude() < 1e-9) return new Complex(0, 0);
        
        return numerator.divide(denominator);
    }
}