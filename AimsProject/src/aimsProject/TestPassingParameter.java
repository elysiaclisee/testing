package hust.soict.cybersecurity.aims.disc;

public class TestPassingParameter {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        DigitalVideoDisc jungleDVD = new DigitalVideoDisc("Jungle");
        DigitalVideoDisc cinderellaDVD = new DigitalVideoDisc("Cinderella");

        DigitalVideoDisc[] list = {jungleDVD, cinderellaDVD};
        swap(list, 0, 1);
        System.out.println("jungle dvd title: " + list[0].getTitle());
        System.out.println("cinderella dvd title: " + list[1].getTitle());


        changeTitle(jungleDVD, cinderellaDVD.getTitle());
        System.out.println("jungle dvd title: " + jungleDVD.getTitle());
    }

    public static void swap(DigitalVideoDisc[] discs, int i, int j) {
        DigitalVideoDisc temp = discs[i];
        discs[i] = discs[j];
        discs[j] = temp;
    }

    public static void changeTitle(DigitalVideoDisc dvd, String title) {
        String oldTitle = dvd.getTitle();
        dvd.setTitle(title);
        dvd = new DigitalVideoDisc(oldTitle);
    }
}