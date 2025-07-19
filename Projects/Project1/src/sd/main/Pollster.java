package sd.main;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

enum Pollster_States {
    WAITING,
    APPROACH,
    INTERVIEW,
}

public class Pollster implements Runnable {
    private static int _pollsterCount = 0;
    private int _pollsterID;
    private Pollster_States _state;
    private CountDownLatch _latch;

    public static double conversationProbability = 0.1;
    public static int delay = 5 + (int) (Math.random() * 6);

    public Pollster(CountDownLatch latch) {
        this._latch = latch;
        this._pollsterID = _pollsterCount++;
        DataStruct.pollsterAvailable[this._pollsterID] = new Semaphore(1);
        DataStruct.semPollsterDecision[this._pollsterID] = new Semaphore(0);
        DataStruct.voterResponseReady[this._pollsterID] = new Semaphore(0);
    }

    @Override
    public void run() {
        while (true) {
            if (DataStruct.pollstersclosed) {
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
            this._state = Pollster_States.WAITING;

            // Block until a request arrives
            DataStruct.pollsterRequest.acquire();

            DataStruct.mutex.acquire();
            if (DataStruct.pollsterRequestFlag >= 1) {
                System.out.println("Pollster " + this._pollsterID + " received request");
                DataStruct.pollsterRequestFlag--;
                DataStruct.pollsterIDforRequest = this._pollsterID;
                req = 1;
            }
            DataStruct.mutex.release();

            DataStruct.pollsterResponse.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
        return req;
    }

    private void check() {
        this._state = Pollster_States.APPROACH;
        String response = "No Response";
        try {
            boolean wantRes = Math.random() < conversationProbability;

            if (wantRes) {
                DataStruct.pollsterDecision[this._pollsterID] = true;
            } else {
                DataStruct.pollsterDecision[this._pollsterID] = false;
            }
            DataStruct.semPollsterDecision[this._pollsterID].release();

            if (!wantRes) {
                System.out.println("Pollster " + this._pollsterID + " does not want to interview");
                return;
            }
            this._state = Pollster_States.INTERVIEW;

            Thread.sleep(delay);

            DataStruct.voterResponseReady[this._pollsterID].acquire();
            response = DataStruct.voterResponses[this._pollsterID];
            DataStruct.mutex.acquire();
            DataStruct.voterResponsesMap.put(response, DataStruct.voterResponsesMap.getOrDefault(response, 0) + 1);
            DataStruct.mutex.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void gracefulExit() {
        System.out.println("Pollster " + this._pollsterID + " is exiting gracefully.");
        this._latch.countDown();
    }
}
