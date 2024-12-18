package bguspl.set.ex;

import java.util.Random;

import bguspl.set.Env;


/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    // Added
    private int tokenCounter;
    private Dealer dealer;
    private actionsQueue<Integer> inActions;
    private volatile Boolean toScore;
    protected boolean needToWait;
    private Object TCLock;
    private boolean freeze;

    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;

        // Added
        this.dealer = dealer;
        terminate = false;
        score = 0;
        tokenCounter = 0;
        inActions = new actionsQueue<Integer>();
        toScore = null;
        needToWait = true;
        TCLock = new Object();
        freeze = false;
    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        if (!human) createArtificialIntelligence();

        while (!terminate) {

            // if can't place token yet, wait until dealer notifies you
            // if waited, clear actions queue
            if (table.getCanPlaceToken())
                inActions.clearQueue();

            Integer slot = inActions.take();

            // check if input is relevant at the moment
            if (slot != null && table.getCard(slot) != null && (readTokenCounter() != env.config.featureSize || table.getToken(id, slot))){

                // place or remove token
                table.rw.playerLock();
                boolean wasRemoved = table.removeToken(id, slot);
                table.rw.playerUnlock();

                if (wasRemoved){
                    synchronized (TCLock) {tokenCounter--;}
                }
                else {
                    if (table.ourPlaceToken(id, slot))
                        synchronized (TCLock) {tokenCounter++;}
                }

                if (readTokenCounter() == env.config.featureSize){

                    freeze = true;

                    // extract the set and create triple for the dealer
                    int[][] set = table.returnSet(id);
                    if (set != null){
                        int[] setCards = set[0];
                        int[] setSlots = set[1];
                        Triple<Integer, int[], int[]> triple = new Triple<>(id, setCards, setSlots);
                        dealer.pushToTestSet(triple);

                        // wait until dealer responds
                        synchronized(dealer.locks[id]){
                            while (needToWait && !terminate){
                                try {
                                    dealer.locks[id].wait();
                                } catch (InterruptedException e) {}
                            }
                            needToWait = true;
                        }

                        // point or penalty and clear queue
                        // if set irrelevant do nothing
                        if (toScore != null){
                            if (toScore)
                                point();
                            else 
                                penalty();
                            inActions.clearQueue();
                        }
                    }

                    freeze = false;
                }
            }
        }
        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very, very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
            while (!terminate) {

                Random random = new Random();
                int slot = random.nextInt(12);
                inActions.put(slot);

            }
            env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        terminate = true;
        if (!human) aiThread.interrupt();
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {

        if (!freeze && table.inputManagerCPT())
            inActions.put(slot);

    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() { 
        env.ui.setScore(id, ++score);

        long endTime = System.currentTimeMillis() + env.config.pointFreezeMillis;

        boolean first = true;
        try {
            while(endTime > System.currentTimeMillis()){
                env.ui.setFreeze(id, endTime - System.currentTimeMillis() + 1000);
                if (first){
                    Thread.sleep(10);
                    first = false;
                }
                else
                    Thread.sleep(1000);
            }
        } catch (InterruptedException e) {}
        
        env.ui.setFreeze(id, 0);
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {

        long endTime = System.currentTimeMillis() + env.config.penaltyFreezeMillis;
        
        boolean first = true;
        try {
            while(endTime > System.currentTimeMillis()){
                env.ui.setFreeze(id, endTime - System.currentTimeMillis() + 1000);
                if(first){
                    Thread.sleep(10);
                    first = false;
                }
                else
                    Thread.sleep(1000);
            }
        } catch (InterruptedException e) {}
        
        env.ui.setFreeze(id, 0);
    }

    public int score() {
        return score;
    }
    
    //Added
    public void toScore(Boolean toscore) {
        toScore = toscore;
    }

    public void removeToken(int slot){
        if (table.removeToken(id, slot))
            synchronized (TCLock) {tokenCounter--;}
    }

    private int readTokenCounter(){
        int output;
        synchronized (TCLock) {output = tokenCounter;}
        return output;
    }
}
