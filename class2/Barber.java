public class Barber extends Thread {
    private static ID_NUMBER = 0;
    private BarberShop inst;
    private int id;

    private get_IID() {
        return this.ID_NUMBER++;
    }

    public Barber(BarberShop inst) {
        this.id = get_IID();
        this.inst = inst;
    }

    @Override
    public void run() {
        // do stuff
    }



}
