package components;

public class CircuitManager {

    // Định nghĩa 2 chế độ mắc bóng đèn theo luật chơi
    public enum ConnectionMode {
        SERIES_WITH_BULB,  // Mạch người dùng nối tiếp bóng đèn
        PARALLEL_WITH_BULB // Mạch người dùng song song bóng đèn
    }

    /**
     * Hàm tính toán toàn bộ trạng thái mạch điện cho Game.
     * * @param source: Nguồn điện của hệ thống
     * @param userCircuit: Mạch điện do người dùng lắp (được gói trong CompositeComponent)
     * @param targetBulb: Bóng đèn mục tiêu cần làm sáng
     * @param mode: Chế độ kết nối (Nối tiếp hoặc Song song)
     */
    public static void simulate(PowerSource source, CompositeComponent userCircuit, Bulb targetBulb, ConnectionMode mode) {
        // 1. Lấy thông số đầu vào
        double voltageIn = source.getVoltage();   // U nguồn
        double freq = source.getFrequency();      // Tần số (f)
        
        // 2. Tính trở kháng tương đương của mạch người dùng (Z_user)
        // CompositeComponent đã tự xử lý đệ quy để ra con số này
        double zUser = userCircuit.getImpedance(freq); 
        
        // 3. Lấy điện trở của bóng đèn (R_bulb)
        double rBulb = targetBulb.getResistance();

        // Biến lưu kết quả tính toán để phân phối lại
        double iTotal = 0.0;        // Dòng tổng
        double vUser = 0.0;         // Áp trên mạch người dùng
        double iUser = 0.0;         // Dòng qua mạch người dùng
        double vBulb = 0.0;         // Áp trên bóng đèn
        double iBulb = 0.0;         // Dòng qua bóng đèn

        // --- TRƯỜNG HỢP 1: BÓNG ĐÈN MẮC NỐI TIẾP (SERIES) ---
        // Sơ đồ: Nguồn -> [Mạch User] -> [Bóng Đèn] -> Nguồn
        if (mode == ConnectionMode.SERIES_WITH_BULB) {
            // Tổng trở Z_total = Z_user + R_bulb
            // (Lưu ý: Cộng đại số là chấp nhận được cho mức độ mô phỏng game này)
            double zTotal = zUser + rBulb;

            // Định luật Ohm: I = U / Z
            if (Double.isInfinite(zTotal) || zTotal <= 0) {
                iTotal = 0.0; // Mạch hở hoặc lỗi
            } else {
                iTotal = voltageIn / zTotal;
            }

            // Mạch nối tiếp: I bằng nhau tại mọi điểm
            iUser = iTotal;
            iBulb = iTotal;

            // Phân áp (Voltage Divider)
            vUser = iUser * zUser;   // U_user = I * Z_user
            vBulb = iBulb * rBulb;   // U_bulb = I * R_bulb
        }

        // --- TRƯỜNG HỢP 2: BÓNG ĐÈN MẮC SONG SONG (PARALLEL) ---
        // Sơ đồ: Nguồn nối trực tiếp vào [Mạch User] VÀ [Bóng Đèn] riêng biệt
        else if (mode == ConnectionMode.PARALLEL_WITH_BULB) {
            // 1. Nhánh Bóng Đèn: Nhận trực tiếp điện áp nguồn
            vBulb = voltageIn;
            if (rBulb > 0) {
                iBulb = vBulb / rBulb;
            } else {
                iBulb = 0; // Tránh chia cho 0
            }

            // 2. Nhánh Mạch Người Dùng
            vUser = voltageIn;
            
            // Xử lý đoản mạch (Short Circuit)
            if (zUser < 1e-9) { 
                // Nếu mạch người dùng nối tắt (Z ~ 0) => Đoản mạch nguồn
                // Trong thực tế cầu chì sẽ nổ. Trong game ta coi như dòng cực đại.
                // Để an toàn mô phỏng, ta set dòng rất lớn hoặc báo lỗi.
                iUser = 9999.0; 
                System.out.println("Warning: Short Circuit in User Branch!");
            } else if (Double.isInfinite(zUser)) {
                iUser = 0.0; // Mạch hở
            } else {
                iUser = vUser / zUser;
            }
            
            // Dòng tổng = Dòng nhánh 1 + Dòng nhánh 2
            iTotal = iUser + iBulb;
        }

        // 4. Cập nhật trạng thái ngược lại vào các Component
        // Để CompositeComponent tự phân phối dòng/áp cho các linh kiện con bên trong nó
        userCircuit.setSimulationState(vUser, iUser, freq);
        
        // Cập nhật cho bóng đèn (để hàm draw biết đường vẽ sáng/tối)
        targetBulb.setSimulationState(vBulb, iBulb, freq);
        
        // 5. Kiểm tra điều kiện thắng/thua (Đèn sáng hay cháy?)
        checkBulbStatus(targetBulb);
    }

    /**
     * Kiểm tra công suất để set trạng thái sáng/tối/cháy cho đèn
     */
    private static void checkBulbStatus(Bulb bulb) {
        // P = I^2 * R
        double current = bulb.getCurrentFlow();
        double realPower = current * current * bulb.getResistance();
        double ratedPower = bulb.getPowerLimit();

        // Logic Game:
        // - Quá tải > 150% công suất => Cháy (Blown)
        // - Đạt > 50% công suất => Sáng (Lighted)
        // - Còn lại => Tối
        
        if (realPower > ratedPower * 1.5) {
            // Giả lập trạng thái cháy: Đèn tắt ngúm và có thể set cờ hỏng
            bulb.setLighted(false); 
            // Có thể thêm: bulb.setBlown(true); nếu class Bulb hỗ trợ
            System.out.println("Bóng đèn đã cháy! P_real=" + realPower + "W");
        } 
        else if (realPower > ratedPower * 0.5) {
            bulb.setLighted(true);
        } 
        else {
            bulb.setLighted(false);
        }
    }
}