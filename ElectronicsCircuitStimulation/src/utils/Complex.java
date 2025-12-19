package utils;

public class Complex {
    private final double real; // Phần thực (Điện trở - R)
    private final double imag; // Phần ảo (Kháng trở - X)

    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

    public double getReal() { return real; }
    public double getImag() { return imag; }

    // Cộng: (a + bi) + (c + di) = (a+c) + (b+d)i
    public Complex add(Complex other) {
        return new Complex(this.real + other.real, this.imag + other.imag);
    }

    // Nhân: (a + bi)(c + di) = (ac - bd) + (ad + bc)i
    public Complex multiply(Complex other) {
        double newReal = this.real * other.real - this.imag * other.imag;
        double newImag = this.real * other.imag + this.imag * other.real;
        return new Complex(newReal, newImag);
    }

    // Chia: (a + bi) / (c + di)
    public Complex divide(Complex other) {
        double denominator = other.real * other.real + other.imag * other.imag;
        if (denominator == 0) return new Complex(0, 0); // Tránh chia cho 0
        
        double newReal = (this.real * other.real + this.imag * other.imag) / denominator;
        double newImag = (this.imag * other.real - this.real * other.imag) / denominator;
        return new Complex(newReal, newImag);
    }

    // Tính độ lớn (Modul): |Z| = sqrt(R^2 + X^2) -> Dùng để tính I = U / |Z|
    public double getMagnitude() {
        return Math.sqrt(real * real + imag * imag);
    }
}
