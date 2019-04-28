package module;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 队列信息
 *
 * @author hanshuwan
 */
public class GameQueue {

    /**
     * 匹配队列
     */
    private ConcurrentLinkedQueue<PlayerQueueInfo> queue;

    /**
     * 玩法类型
     */
    private int gameType;

    /**
     * 场次信息
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
