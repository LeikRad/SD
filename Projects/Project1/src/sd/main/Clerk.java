package sd.main;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

enum Clerk_States {
    WAITING_FOR_REQUEST,
    CHECKING,
    CLOSING,
}

class Clerk implements Runnable {
    private static int clerkCount = 0;
    private int clerkID;
    private Clerk_States state;
    private CountDownLatch latch;

    public static int delay = 5 + (int) (Math.random() * 6);

    public Clerk(CountDownLatch latch) {
        this.latch = latch;
        this.clerkID = clerkCount++;
        DataStruct.clerksAvailable[this.clerkID] = new Semaphore(1);
        DataStruct.voterIdReady[this.clerkID] = new Semaphore(0);
        DataStruct.clerkResponseReady[this.clerkID] = new Semaphore(0);
    }

    @Override
    public void run() {
        while (true) {
            if (DataStruct.clerksclosed) {
                gracefulExit();
                return;
            }
            int req = getRequest();
            if (req == 1) {
                check();
            }
        }
    }

    private int getRequest() {
        int req = -1;
        try {
            // Wait until ready for a new request
            this.state = Clerk_States.WAITING_FOR_REQUEST;

            // Block until a request arrives
            DataStruct.clerkRequest.acquire();

            DataStruct.mutex.acquire();
            if (DataStruct.clerkRequestFlag == 1) {
                System.out.println("Clerk " + this.clerkID + " received request");
                DataStruct.clerkRequestFlag = 0;
                DataStruct.clerkIDforRequest = this.clerkID;
                req = 1;
            }
            DataStruct.mutex.release();

            DataStruct.clerkResponse.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
        return req;
    }

    private void check() {
        this.state = Clerk_States.CHECKING;
        boolean result = false;
        try {
            // Wait until ready for a new request

            DataStruct.voterIdReady[this.clerkID].acquire();
            int voterId = DataStruct.voterIDsForClerk[this.clerkID];

            Thread.sleep(delay);

            // check if already voted
            DataStruct.mutex.acquire();
            if (DataStruct.voterIDs.contains(voterId)) {
                result = false;
            } else {
                result = true;

                DataStruct.voterIDs.add(voterId);

            }
            DataStruct.mutex.release();

            DataStruct.clerkResponses[this.clerkID] = result;
            DataStruct.clerkResponseReady[this.clerkID].release();
            System.out.println("Clerk " + this.clerkID + " has responded to voter " + voterId + " and the result is " + result + ".");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void gracefulExit() {
        System.out.println("Clerk " + this.clerkID + " has exited");
        latch.countDown();
    }
}