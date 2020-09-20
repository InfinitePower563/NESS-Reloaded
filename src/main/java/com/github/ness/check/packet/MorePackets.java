package com.github.ness.check.packet;

import java.util.concurrent.TimeUnit;

import com.github.ness.NessPlayer;
import com.github.ness.api.Violation;
import com.github.ness.check.AbstractCheck;
import com.github.ness.check.CheckFactory;
import com.github.ness.check.CheckInfo;
import com.github.ness.data.PlayerAction;
import com.github.ness.packets.ReceivedPacketEvent;
import com.github.ness.utility.Utility;

public class MorePackets extends AbstractCheck<ReceivedPacketEvent> {

    int maxPackets;
    
	public static final CheckInfo<ReceivedPacketEvent> checkInfo = CheckInfo.eventWithAsyncPeriodic(ReceivedPacketEvent.class, 1, TimeUnit.SECONDS);
	int normalPacketsCounter; // For MorePackets
	public MorePackets(CheckFactory<?> factory, NessPlayer player) {
		super(factory, player);
        this.maxPackets = this.ness().getNessConfig().getCheck(this.getClass())
                .getInt("maxpackets", 65);
        normalPacketsCounter = -5;
	}

    @Override
    protected void checkAsyncPeriodic() {
        normalPacketsCounter = 0;
    }

    @Override
    protected void checkEvent(ReceivedPacketEvent e) {
        int ping = Utility.getPing(e.getNessPlayer().getPlayer());
        int maxPackets = this.maxPackets + ((ping / 100) * 6);
        NessPlayer np = e.getNessPlayer();
        if (np == null) {
            return;
        }
        // sender.sendMessage("Counter: " + np.getPacketscounter());
        if (normalPacketsCounter++ > maxPackets && np.nanoTimeDifference(PlayerAction.JOIN) > 2500) {
            /*
             * new BukkitRunnable() {
             *
             * @Override public void run() { // What you want to schedule goes here
             * sender.teleport(OldMovementChecks.safeLoc.getOrDefault(sender,
             * sender.getLocation())); } }.runTask(NESSAnticheat.main);
             */
            np.setViolation(new Violation("MorePackets", normalPacketsCounter + ""), e);
        }
    }
}
