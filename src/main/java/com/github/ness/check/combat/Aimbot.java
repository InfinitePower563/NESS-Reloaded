package com.github.ness.check.combat;

import java.util.ArrayList;
import java.util.List;

import com.github.ness.NessPlayer;
import com.github.ness.api.Violation;
import com.github.ness.check.CheckInfos;
import com.github.ness.check.ListeningCheck;
import com.github.ness.check.ListeningCheckFactory;
import com.github.ness.check.ListeningCheckInfo;
import com.github.ness.packets.ReceivedPacketEvent;
import com.github.ness.utility.GCDUtils;
import com.github.ness.utility.Utility;

public class Aimbot extends ListeningCheck<ReceivedPacketEvent> {

	public static final ListeningCheckInfo<ReceivedPacketEvent> checkInfo = CheckInfos.forEvent(ReceivedPacketEvent.class);

	private float lastYaw;
	private List<Float> pitchDiff;
	private double lastGCD = 0;
	public Aimbot(ListeningCheckFactory<?, ReceivedPacketEvent> factory, NessPlayer player) {
		super(factory, player);
		lastYaw = 0;
        this.pitchDiff = new ArrayList<Float>();
	}

	@Override
	protected void checkEvent(ReceivedPacketEvent e) {
		if (!player().equals(e.getNessPlayer())) {
			return;
		}
		if (!e.getPacket().getName().toLowerCase().contains("LOOK") || e.getNessPlayer().isTeleported()) {
			return;
		}
		makeSensitivity(e);
		Check2(e);
		Check3(e);
	}

	private void makeSensitivity(ReceivedPacketEvent event) {
		// float yaw = to.getYaw() - from.getYaw();
		NessPlayer player = event.getNessPlayer();
		float pitch = (float) Math.abs(player.getMovementValues().pitchDiff);
		if (Math.abs(pitch) >= 10 || Math.abs(pitch) < 0.05 || pitch == 0.0 || !player.isTeleported()
				|| Math.abs(player.getMovementValues().getTo().getPitch()) == 90) {
			return;
		}
		pitchDiff.add(pitch);
		if (pitchDiff.size() >= 20) {
			final float gcd = GCDUtils.gcdRational(pitchDiff);
			if (lastGCD == 0.0) {
				lastGCD = gcd;
			}
			double result = Math.abs(gcd - lastGCD);
			if (result < 0.007) {
				final double sensitivity = GCDUtils.getSensitivity(gcd);
				if (player.isDevMode()) {
					player.getPlayer().sendMessage("Setting Sensitivity to: " + sensitivity);
				}
				player.sensitivity = sensitivity;
			}
			pitchDiff.clear();
			lastGCD = gcd;
		}
	}

	@SuppressWarnings("unused")
	private void Check1(ReceivedPacketEvent e) {
		NessPlayer np = e.getNessPlayer();
		if (np.sensitivity == 0) {
			return;
		}
		if (np.getMovementValues().yawDiff < 1) {
			return;
		}
		double firstvar = np.sensitivity * 0.6F + 0.2F;
		float secondvar = (float) (Math.pow(firstvar, 3f));
		double yawResult = np.getMovementValues().yawDiff - lastYaw;
		float thirdvar = (float) yawResult / (secondvar * 1.2f);
		float x = (float) (thirdvar - Math.floor(thirdvar));
		// TODO Fixing Smooth Camera
		if (x > 0.1 && x < 0.95) {
			if(player().setViolation(new Violation("Aimbot", "ImpossibleRotation: " + x))) e.setCancelled(true);
		}
		lastYaw = (float) np.getMovementValues().yawDiff;
	}

	/**
	 * Check for some Aimbot Pattern
	 */
	private boolean Check2(ReceivedPacketEvent e) {
		NessPlayer player = e.getNessPlayer();
		float yawChange = (float) Math.abs(player.getMovementValues().yawDiff);
		float pitchChange = (float) Math.abs(player.getMovementValues().pitchDiff);
		if (yawChange >= 1.0f && yawChange % 0.1f == 0.0f) {
			if(player().setViolation(new Violation("Aimbot", "PerfectAura"))) e.setCancelled(true);
			return true;
		} else if (pitchChange >= 1.0f && pitchChange % 0.1f == 0.0f) {
			if(player().setViolation(new Violation("Aimbot", "PerfectAura1"))) e.setCancelled(true);
			return true;
		}
		return false;
	}

	/**
	 * Check for some Aimbot Pattern
	 */
	private void Check3(ReceivedPacketEvent e) {
		NessPlayer player = e.getNessPlayer();
		float yawChange = (float) Math.abs(player.getMovementValues().yawDiff);
		if (yawChange > 1.0f && Utility.round(yawChange, 100) * 0.1f == yawChange) {
			if(player().setViolation(new Violation("Aimbot", "[Experimental] PerfectAura3"))) e.setCancelled(true);
		}
	}
}