package com.github.ness.check.packet;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.github.ness.NessPlayer;
import com.github.ness.api.Violation;
import com.github.ness.check.CheckInfos;
import com.github.ness.check.ListeningCheck;
import com.github.ness.check.ListeningCheckFactory;
import com.github.ness.check.ListeningCheckInfo;
import com.github.ness.data.PlayerAction;
import com.github.ness.packets.ReceivedPacketEvent;
import com.github.ness.utility.Utility;

public class MorePackets extends ListeningCheck<ReceivedPacketEvent> {

    int maxPackets;
    
	public static final ListeningCheckInfo<ReceivedPacketEvent> checkInfo = CheckInfos.forEventWithAsyncPeriodic(ReceivedPacketEvent.class, Duration.ofSeconds(1));
	int normalPacketsCounter; // For MorePackets
	public MorePackets(ListeningCheckFactory<?, ReceivedPacketEvent> factory, NessPlayer player) {
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
        	if(player().setViolation(new Violation("MorePackets", Integer.toString(normalPacketsCounter)))) e.setCancelled(true);
        }
    }
}
