package sd.main;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VoterIDGen {

    private static final Lock lock = new ReentrantLock();
    private static final Condition customerAvailable = lock.newCondition();
    private static int _idCounter = 0;

    public static int generateID() {
        lock.lock();
        System.out.println("Generating ID");
        _idCounter++;
        lock.unlock();
        return _idCounter;
    }
}
