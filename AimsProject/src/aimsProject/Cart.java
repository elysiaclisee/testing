package aimsProject;

public class Cart {
    public static final int MAX_NUMBERS_ORDERED = 20;
    private DigitalVideoDisc itemsOrdered[] = new DigitalVideoDisc[MAX_NUMBERS_ORDERED];
    private int qtyOrdered = 0;

    public void addDigitalVideoDisc(DigitalVideoDisc disc) {
        if (qtyOrdered >= MAX_NUMBERS_ORDERED) {
            System.out.println("The cart is full");
        } else {
            itemsOrdered[qtyOrdered] = disc;
            qtyOrdered++;
            System.out.println("The disc " + disc.getTitle() + " has been added.");

            if (qtyOrdered == MAX_NUMBERS_ORDERED - 1) {
                System.out.println("The cart is almost full!");
            }
        }
    }
    public void removeDigitalVideoDisc(DigitalVideoDisc disc) {
        boolean found = false;
        for (int i = 0; i < qtyOrdered; i++) {
            if (itemsOrdered[i] == disc) {
                found = true;
                for (int j = i; j < qtyOrdered - 1; j++) {
                    itemsOrdered[j] = itemsOrdered[j + 1];
                }
                itemsOrdered[qtyOrdered - 1] = null;
                qtyOrdered--;
                System.out.println("The disc " + disc.getTitle() + " has been removed.");
                break;
            }
        }

        if (!found) {
            System.out.println("The disc " + disc.getTitle() + " not found in the cart.");
        }
    }
    public float totalCost() {
        float total = 0;
        for (int i = 0; i < qtyOrdered; i++) {
            total += itemsOrdered[i].getCost();
        }
        return total;
    }
    public void displayCart() {
    	System.out.println("Cart content:");
        if (qtyOrdered == 0) {
            System.out.println("Your cart is empty.");
        } else {
            for (int i = 0; i < qtyOrdered; i++) {
                DigitalVideoDisc disc = itemsOrdered[i];
                System.out.printf("%d. %s - %s - %s - %d mins - $%.2f%n",
                        i + 1,
                        disc.getTitle(),
                        disc.getCategory(),
                        disc.getDirector(),
                        disc.getLength(),
                        disc.getCost());
            }
        }
    }
}

