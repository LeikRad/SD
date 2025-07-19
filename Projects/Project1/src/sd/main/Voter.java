package sd.main;

import java.util.concurrent.CountDownLatch;

enum Voter_States {
    BIRTH,
    WAITING,
    WAITING_IN_QUEUE,
    VOTING,
    EXIT,
    QUESTIONAIRE,
    REBIRTH,
}

public class Voter implements Runnable {

    private static int _voterCount = 0;
    private int _IID;
    private int _voterID;
    private Voter_States _state;
    private int _clerkID;
    private int _pollsterId;
    private CountDownLatch _latch;
    private String _votedFor;

    public static double answerProbability = 0.6;
    public static double lieProbability = 0.2;
    public static double changeVIProbability = 0.4;

    public static int delay = 0 + (int) Math.random() * 16;

    public Voter(CountDownLatch latch) {
        this._latch = latch;
        this._votedFor = getVotee();
        this._IID = _voterCount++;
        this._voterID = VoterIDGen.generateID();
    }

    public Voter(CountDownLatch latch, int VoterID, int IID) {
        this._votedFor = getVotee();
        this._latch = latch;
        this._voterID = VoterID;
        this._IID = IID;
    }

    @Override
    public void run() {
        if (!enter()){
            gracefulExit();
        };
        waitForClerk();
        if (validate()) {
            // rebirth();
            vote();
        }
        answerQuestionaire();
        rebirth();
    }

    private String getVotee() {
        if (Math.random() < 0.7) {
            return "DRP";
        } else {
            return "NKDP";
        }
    }

    private boolean enter() {
        _state = Voter_States.WAITING;
        try {
            synchronized (DataStruct.Queue) {
                while (DataStruct.Queue.size() >= DataStruct.voter_line_size) {
                    DataStruct.Queue.wait();
                }
                // waiting outside, if station is closed then kill thread
                if (DataStruct.votersclosed) {
                    return false;
                }
                DataStruct.Queue.add(this);
                DataStruct.Queue.notifyAll();
                System.out.println("Voter " +  this._voterID + " with IID " + this._IID  + " has entered the queue.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void waitForClerk() {
        try {
            synchronized (DataStruct.Queue) {
                while (DataStruct.Queue.peek() != this) {
                    DataStruct.Queue.wait();
                }
                DataStruct.Queue.poll();

                System.out.println("Voter " +  this._voterID + " with IID " + this._IID  + " is at Head and wait for clerk.");
                DataStruct.mutex.acquire();
                DataStruct.clerkRequestFlag = 1;
                DataStruct.mutex.release();
                DataStruct.clerkRequest.release();
                DataStruct.clerkResponse.acquire();
                this._clerkID = DataStruct.clerkIDforRequest;
    
                DataStruct.Queue.notifyAll();
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean validate() {
        boolean response = false;
        try {
            DataStruct.voterIDsForClerk[this._clerkID] = this._voterID;
            DataStruct.voterIdReady[this._clerkID].release();
            DataStruct.clerkResponseReady[this._clerkID].acquire();
            response = DataStruct.clerkResponses[this._clerkID];
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    private void vote() {
        _state = Voter_States.VOTING;
        try {
            
            DataStruct.boothsAvailable.acquire();
            DataStruct.mutex.acquire();

            Thread.sleep(delay);

            DataStruct.Candidates.put(this._votedFor, DataStruct.Candidates.get(this._votedFor) + 1);
            // 70% chance of voting for DRP, 30% chance of voting for NKDP
            System.out.println("Voter " +  this._voterID + " with IID " + this._IID  + " has voted for " + this._votedFor + ".");
            DataStruct.mutex.release();
            DataStruct.boothsAvailable.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void answerQuestionaire() {
        _state = Voter_States.QUESTIONAIRE;
        try {
            boolean willAnswer = Math.random() < answerProbability;
            if (!willAnswer) {
                System.out.println("Voter " +  this._voterID + " with IID " + this._IID  +  " is not answering the questionaire.");
                return;
            }
            DataStruct.mutexRequest.acquire();
            
            DataStruct.pollsterRequestFlag++;
            DataStruct.pollsterRequest.release();

            DataStruct.pollsterResponse.acquire();
            this._pollsterId = DataStruct.pollsterIDforRequest;
            DataStruct.mutexRequest.release();
            DataStruct.semPollsterDecision[this._pollsterId].acquire();
            boolean wantAnswer = DataStruct.pollsterDecision[this._pollsterId];

            if (!wantAnswer) {
                System.out.println("Voter " +  this._voterID + " with IID " + this._IID  + " is not answering the questionaire.");
                return;
            }

            String response = "No Response";
            System.out.println("Voter " +  this._voterID + " with IID " + this._IID  +   " is answering the questionaire.");

            boolean willLie = Math.random() < lieProbability;
            if (willLie) {
                response = this._votedFor.equals("DRP") ? "NKDP" : "DRP";
            } else {
                response = this._votedFor;
            }
            System.out.println("Voter " +  this._voterID + " with IID " + this._IID  +  " has answered the questionaire with " + response + ".");
            DataStruct.voterResponses[this._pollsterId] = response;
            System.out.println(DataStruct.voterResponses[this._pollsterId]);
            DataStruct.voterResponseReady[this._pollsterId].release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void rebirth() {
        if (Math.random() < changeVIProbability) {
            this._voterID = VoterIDGen.generateID();
        }
        if (DataStruct.votersclosed) {
            gracefulExit();
            return;
        }
        new Thread(new Voter(this._latch, this._voterID, this._IID)).start();
    }

    public int getVoterID() {
        return this._voterID;
    }

    public void gracefulExit() {
        System.out.println("Voter " + this._voterID + " with IID " + this._IID + " has exited.");
        this._latch.countDown();
    }
}
