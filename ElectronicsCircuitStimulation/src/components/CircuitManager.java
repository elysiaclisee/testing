package components;

public class CircuitManager {

    // Enum xác định cách mắc bóng đèn vào mạch
    public enum ConnectionMode {
        SERIES_WITH_BULB,  // Bóng đèn nối tiếp với mạch người dùng
        PARALLEL_WITH_BULB // Bóng đèn song song với mạch người dùng
    }

    /**
     * Hàm tính toán trạng thái mạch điện.
     * * Nguyên lý:
     * 1. Coi toàn bộ linh kiện người dùng là một trở kháng Z_user.
     * 2. Kết hợp Z_user với R_bulb theo chế độ (Series/Parallel) để ra Z_total.
     * 3. Áp dụng định luật Ohm cho toàn mạch: I_source = V_source / Z_total.
     * 4. Phân phối dòng/áp ngược lại cho từng thành phần.
     */
    public static void simulate(PowerSource source, CompositeComponent userCircuit, Bulb targetBulb, ConnectionMode mode) {
        // 1. Lấy thông số đầu vào
        double uSource = source.getVoltage();   // Điện áp nguồn (V)
        double freq = source.getFrequency();    // Tần số (Hz)
        
        // 2. Tính trở kháng của mạch người dùng (Z_user) và bóng đèn (R_bulb)
        double zUser = userCircuit.getImpedance(freq); 
        double rBulb = targetBulb.getResistance();
        
        // Các biến kết quả cần tính
        double zTotal = 0.0;        // Tổng trở toàn mạch (nhìn từ nguồn)
        double iSource = 0.0;       // Dòng điện tổng từ nguồn cấp ra
        
        double vUser = 0.0;         // Áp trên mạch người dùng
        double iUser = 0.0;         // Dòng qua mạch người dùng
        
        double vBulb = 0.0;         // Áp trên bóng đèn
        double iBulb = 0.0;         // Dòng qua bóng đèn

        // =================================================================
        // TRƯỜNG HỢP 1: BÓNG ĐÈN MẮC NỐI TIẾP VỚI MẠCH USER
        // Sơ đồ: Nguồn --> [Bóng Đèn] --> [Mạch User] --> Nguồn
        // =================================================================
        if (mode == ConnectionMode.SERIES_WITH_BULB) {
            // Tổng trở = R đèn + Z mạch
            zTotal = rBulb + zUser;

            // Tính dòng tổng (I = U / Z)
            if (Double.isInfinite(zTotal) || zTotal <= 0) {
                iSource = 0.0;
            } else {
                iSource = uSource / zTotal;
            }

            // Đặc điểm mạch nối tiếp: I bằng nhau tại mọi điểm
            iBulb = iSource;
            iUser = iSource;

            // Phân áp (Voltage Divider)
            vBulb = iBulb * rBulb;
            vUser = iUser * zUser;
        }

        // =================================================================
        // TRƯỜNG HỢP 2: BÓNG ĐÈN MẮC SONG SONG VỚI MẠCH USER
        // Sơ đồ: Nguồn nối vào cụm [Bóng Đèn || Mạch User]
        // "Nguồn nối tiếp mạch" theo ý bạn là Nguồn cấp cho cụm song song này.
        // =================================================================
        else if (mode == ConnectionMode.PARALLEL_WITH_BULB) {
            
            // Xử lý trường hợp mạch hở hoặc đoản mạch trước khi tính công thức song song
            boolean isUserOpen = Double.isInfinite(zUser);
            boolean isUserShort = (zUser < 1e-9);

            if (isUserShort) {
                // Nếu mạch user đoản mạch => Cả cụm bị đoản mạch (Dòng dồn hết qua user)
                zTotal = 0.0;
                vBulb = 0.0; // Mất áp vì đoản mạch
                vUser = 0.0;
                // Để an toàn, gán dòng lớn tượng trưng hoặc 0 tùy logic game
                iSource = 9999.0; 
                iUser = 9999.0;
                iBulb = 0.0;
            } 
            else if (isUserOpen) {
                // Nếu mạch user hở => Chỉ còn mỗi bóng đèn nối vào nguồn
                zTotal = rBulb;
                vBulb = uSource;
                vUser = uSource; // Áp vẫn đặt lên đầu mạch hở
                
                iBulb = (rBulb > 0) ? (uSource / rBulb) : 9999.0;
                iUser = 0.0;
                iSource = iBulb;
            } 
            else {
                // Công thức trở kháng song song: Z_total = (R1 * R2) / (R1 + R2)
                if (rBulb + zUser == 0) zTotal = 0; // Tránh chia cho 0
                else zTotal = (rBulb * zUser) / (rBulb + zUser);

                // Tính dòng tổng từ nguồn cấp ra
                iSource = (zTotal > 0) ? (uSource / zTotal) : 9999.0;

                // Đặc điểm mạch song song: U thành phần = U nguồn
                vBulb = uSource;
                vUser = uSource;

                // Tính dòng thành phần (Chia dòng - Current Divider)
                iBulb = (rBulb > 0) ? (vBulb / rBulb) : 0;
                iUser = (zUser > 0) ? (vUser / zUser) : 0;
            }
        }

        // 3. Cập nhật trạng thái ngược lại vào các linh kiện
        // Để CompositeComponent tự phân phối dòng/áp sâu hơn cho các con bên trong
        userCircuit.setSimulationState(vUser, iUser, freq);
        
        // Cập nhật cho bóng đèn
        targetBulb.setSimulationState(vBulb, iBulb, freq);
        
        // 4. Kiểm tra bóng đèn sáng hay tắt
        checkBulbStatus(targetBulb);
        
        // (Optional) In debug để kiểm tra
        // System.out.println("Mode: " + mode + " | Z_Total: " + zTotal + " | I_Source: " + iSource);
    }

    /**
     * Kiểm tra công suất để set trạng thái sáng/tối/cháy cho đèn
     */
    private static void checkBulbStatus(Bulb bulb) {
        double current = bulb.getCurrentFlow();
        double realPower = current * current * bulb.getResistance(); // P = I^2 * R
        double ratedPower = bulb.getPowerLimit();

        if (realPower > ratedPower * 1.5) {
            bulb.setLighted(false); // Cháy
        } else if (realPower > ratedPower * 0.4) { // Hạ ngưỡng sáng xuống 40% cho dễ chơi
            bulb.setLighted(true);  // Sáng
        } else {
            bulb.setLighted(false); // Tối
        }
    }
}