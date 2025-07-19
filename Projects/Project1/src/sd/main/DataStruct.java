package sd.main;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;

public class DataStruct {
    public static boolean votersclosed = false;
    public static boolean pollstersclosed = false;
    public static boolean clerksclosed = false;
    public static int numClerks = 1;
    public static int numBooths = 3;
    public static int numVoters = 10;
    public static int numPollsters = 2;
    public static int voter_line_size = 5;
    public static int timeToClose = 2000;
    public static List<Thread> voterThreads = new ArrayList<>();

    public static Semaphore clerkRequest = new Semaphore(0);
    public static int clerkRequestFlag;
    public static Semaphore clerkResponse = new Semaphore(0);
    public static int clerkIDforRequest;
    public static Semaphore voterRequestsQueue = new Semaphore(0);
    public static int voterRequestsQueueFlag;

    public static Semaphore mutexRequest = new Semaphore(1);
    public static Semaphore pollsterRequest = new Semaphore(0);
    public static int pollsterRequestFlag;
    public static Semaphore pollsterResponse = new Semaphore(0);
    public static int pollsterIDforRequest;

    public static HashSet<Integer> voterIDs = new HashSet<>();
    public static HashMap<String, Integer> Candidates = new HashMap<String, Integer>();
    static {
        Candidates.put("DRP", 0);
        Candidates.put("NKDP", 0);
    }

    public static Semaphore mutex = new Semaphore(1);
    public static Semaphore[] clerksAvailable = new Semaphore[numClerks];
    public static Semaphore boothsAvailable = new Semaphore(numBooths);

    public static Semaphore[] voterIdReady = new Semaphore[numClerks];
    public static int[] voterIDsForClerk = new int[numClerks];
    public static Semaphore[] clerkResponseReady = new Semaphore[numClerks];
    public static boolean[] clerkResponses = new boolean[numClerks];

    public static Semaphore[] pollsterAvailable = new Semaphore[numPollsters];
    public static Semaphore[] semPollsterDecision = new Semaphore[numPollsters];
    public static boolean[] pollsterDecision = new boolean[numPollsters];

    public static Semaphore[] voterResponseReady = new Semaphore[numPollsters];
    public static String[] voterResponses = new String[numPollsters];
    public static HashMap<String, Integer> voterResponsesMap = new HashMap<String, Integer>();
    static {
        Candidates.put("DRP", 0);
        Candidates.put("NKDP", 0);
    }

    public static Queue<Voter> Queue = new LinkedList<>();
}