package module;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ������Ϣ
 *
 * @author hanshuwan
 */
public class GameQueue {

    /**
     * ƥ�����
     */
    private ConcurrentLinkedQueue<PlayerQueueInfo> queue;

    /**
     * �淨����
     */
    private int gameType;

    /**
     * ������Ϣ
     */
    private int gameLevel;

    public GameQueue(int gameType, int gameLevel) {
        this.gameType = gameType;
        this.gameLevel = gameLevel;
        this.queue = new ConcurrentLinkedQueue<PlayerQueueInfo>();
    }

    public ConcurrentLinkedQueue<PlayerQueueInfo> getQueue() {
        return queue;
    }

    public void setQueue(ConcurrentLinkedQueue<PlayerQueueInfo> queue) {
        this.queue = queue;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public int getGameLevel() {
        return gameLevel;
    }

    public void setGameLevel(int gameLevel) {
        this.gameLevel = gameLevel;
    }

}
