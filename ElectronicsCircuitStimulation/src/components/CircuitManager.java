package components;

public class CircuitManager {

    // Enum xác định cách mắc bóng đèn vào mạch
    public enum ConnectionMode {
        SERIES_WITH_BULB,  // Bóng đèn nối tiếp với mạch người dùng
        PARALLEL_WITH_BULB // Bóng đèn song song với mạch người dùng
    }

    /**
     * Hàm tính toán trạng thái mạch điện.
     * 1. Lấy trở kháng phức (Complex) của User và Bulb.
     * 2. Tính Z_total toàn mạch.
     * 3. Tính I_total và phân phối dòng qua đèn dựa trên chế độ (Series/Parallel).
     * 4. Tính công suất thực P = I^2 * R để xét trạng thái đèn.
     */
    public static void simulate(PowerSource source, CompositeComponent userCircuit, Bulb targetBulb, ConnectionMode mode) {
        double vSrc = source.getVoltage();
        double freq = source.getFrequency();

        // 1. Tính toán trở kháng dựa trên Số Phức (Complex)
        Complex zUser = userCircuit.getImpedance(freq);
        Complex zBulb = targetBulb.getImpedance(freq);
        
        // Z tổng tùy theo chế độ mắc nối tiếp hay song song
        Complex zTotal = (mode == ConnectionMode.SERIES_WITH_BULB) 
                         ? zUser.add(zBulb) 
                         : Connections.parallel(zUser, zBulb);

        double zMag = zTotal.getMagnitude();
        double iTotal = (zMag > 1e-9) ? vSrc / zMag : 0;
        
        // 2. Tính dòng điện qua bóng đèn
        double iBulb;
        if (mode == ConnectionMode.SERIES_WITH_BULB) {
            iBulb = iTotal; // Mạch nối tiếp: dòng điện bằng nhau
        } else {
            // Mạch song song: áp đặt lên đèn bằng áp nguồn (U_bulb = U_source)
            iBulb = vSrc / zBulb.getMagnitude();
        }

        // 3. Tính công suất thực tế: P = I^2 * R (R = zBulb.getReal())
        double pActual = iBulb * iBulb * zBulb.getReal();

        // 4. Cập nhật trạng thái để hiển thị lên GUI
        targetBulb.setSimulationState(iBulb * zBulb.getMagnitude(), iBulb, freq);
        userCircuit.setSimulationState(vSrc, iTotal, freq); 

        // 5. Logic trạng thái sáng (Định mức 50W)
        // Cháy (>1.5 định mức), Sáng/Yếu (>=0.2 định mức), Tắt (<0.2 định mức)
        updateBulbLogic(targetBulb, pActual);
        
        // In kết quả kiểm tra (Làm tròn 2 chữ số thập phân khi hiển thị)
        System.out.printf("V source: %.2fV | Z total: %.2fΩ | I total: %.2fA | P bulb: %.2fW\n", 
                vSrc, zMag, iTotal, pActual);
    }

    /**
     * Cập nhật trạng thái vật lý của bóng đèn dựa trên công suất tiêu thụ thực tế.
     * Ngưỡng cố định dựa trên công suất định mức 50W.
     */
    private static void updateBulbLogic(Bulb bulb, double pActual) {
        double pRated = 50.0; // Công suất định mức cố định

        if (pActual > 50.0 * 1.5) {
            bulb.setLighted(false);
            System.out.println("Status: BURNT");
        } else if (pActual >= 50.0 * 0.8) {
            bulb.setLighted(true);
            System.out.println("Status: NORMAL");
        } else if (pActual >= 50.0 * 0.2) {
            bulb.setLighted(true);
            System.out.println("Status: WEAK");
        } else {
            bulb.setLighted(false);
            System.out.println("Status: OFF");
        }
    }
}