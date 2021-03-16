package hardware;

import java.util.ArrayList;
import java.util.List;

public class Disk {
    public List<DiskBlock> diskBlocks;
    public int blocks;
    public long seekTime;
    public long transferTime;

    private long timer;

    public Disk(int blocks, long seekTime, long transferTime) {
        this.blocks = blocks;
        this.seekTime = seekTime;
        this.transferTime = transferTime;

        this.diskBlocks = new ArrayList<>();
    }

    public DiskBlock seek(int blockId) {
        startTimer();
        for (DiskBlock db : diskBlocks) {
            if (db.blockId == blockId) {
                long timeElapsed = recordTime();
                if (timeElapsed < seekTime) {
                    try {
                        wait(seekTime - timeElapsed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return db;
            }
        }
        return null;
    }

    private void startTimer() {
        timer = System.currentTimeMillis();
    }

    private long recordTime() {
        return System.currentTimeMillis() - timer;
    }
}
