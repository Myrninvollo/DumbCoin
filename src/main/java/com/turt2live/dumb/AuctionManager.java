package com.turt2live.dumb;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class AuctionManager extends BukkitRunnable {

    private DumbAuction plugin = DumbAuction.p;
    private int max = plugin.getConfig().getInt("max-queue-size", 3);
    private ArrayBlockingQueue<Auction> auctions = new ArrayBlockingQueue<Auction>(max);
    private long downtimeTicks = plugin.getConfig().getLong("seconds-between-auctions", 15);
    private long currentDowntime = 0;
    private boolean locked = false;
    private Auction activeAuction;

    public AuctionManager() {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 20L, 20L);
    }

    public boolean addAuction(Auction auction) {
        if (locked) return false;
        if (auction != null) {
            return auctions.offer(auction);
        }
        return false;
    }

    public Auction getActiveAuction() {
        return activeAuction;
    }

    public List<Auction> getAuctions() {
        List<Auction> auctionList = new ArrayList<Auction>();
        for (Auction auction : auctions.toArray(new Auction[0])) {
            auctionList.add(auction);
        }
        return auctionList;
    }

    public void cancel(Auction auction) {
        if (activeAuction != null && auction.getSeller().equalsIgnoreCase(activeAuction.getSeller())) {
            activeAuction = null;
            currentDowntime = downtimeTicks;
            activeAuction.reward();
        }
    }

    public void stop() {
        cancel();
        locked = true;
        Auction auction;
        if (activeAuction != null) activeAuction.reward();
        while ((auction = auctions.poll()) != null) {
            auction.reward(); // Will return items
        }
    }

    public void run() {
        if (currentDowntime > 0) {
            currentDowntime--;
        } else {
            if (activeAuction == null) {
                Auction auction = auctions.poll();
                if (auction != null) {
                    activeAuction = auction;
                    activeAuction.start();
                }
            }
            if (activeAuction != null) {
                activeAuction.tick();
                if (activeAuction.getSecondsLeft() <= 0) {
                    currentDowntime = downtimeTicks;
                    activeAuction.reward();
                }
            }
        }
    }

    public int size() {
        return auctions.size();
    }
}
