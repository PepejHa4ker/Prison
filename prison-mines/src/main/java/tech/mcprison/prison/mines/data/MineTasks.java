package tech.mcprison.prison.mines.data;

import java.util.List;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.internal.Player;
import tech.mcprison.prison.internal.World;
import tech.mcprison.prison.mines.PrisonMines;
import tech.mcprison.prison.mines.data.MineScheduler.MineJob;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.tasks.PrisonRunnable;
import tech.mcprison.prison.util.Location;
import tech.mcprison.prison.util.Text;

public abstract class MineTasks
        extends MineReset {

    public MineTasks() {
        super();


    }


    /**
     * <p>This initialize function gets called after the classes are
     * instantiated, and is initiated from Mine class and propagates
     * to the MineData class.  Good for kicking off the scheduler.
     * </p>
     *
     * <p>Once the mine has been loaded in to memory, the number of
     * air blocks must be counted to properly set the blockBreakCount.
     * </p>
     */
    @Override
    protected void initialize() {
        super.initialize();

    }


    /**
     * This should be used to submit async tasks.
     *
     * @param callbackAsync
     */
//	@Override
//    public int submitAsyncTask( PrisonRunnable callbackAsync ) {
//    	return submitAsyncTask( callbackAsync, 0L );
//    }
    @Override
    public int submitAsyncTask(PrisonRunnable callbackAsync, long delay) {
        return Prison.get().getPlatform().getScheduler().runTaskLaterAsync(callbackAsync,
                getResetPagePageSubmitDelayTicks() + delay);
    }

//	@Override
//    public int submitSyncTask( PrisonRunnable callbackSync ) {
//    	return submitSyncTask( callbackSync, 0L );
//    }

    @Override
    public int submitSyncTask(PrisonRunnable callbackSync, long delay) {
        return Prison.get().getPlatform().getScheduler().runTaskLater(callbackSync,
                getResetPagePageSubmitDelayTicks() + delay);
    }


    @Override
    public long teleportAllPlayersOut() {
        long start = System.currentTimeMillis();

        if (isVirtual()) {
            return 0;
        }

        World world = getBounds().getCenter().getWorld();

        try {
            if (isEnabled() && world != null) {
                List<Player> players = (world.getPlayers() != null ? world.getPlayers() :
                        Prison.get().getPlatform().getOnlinePlayers());
                for (Player player : players) {
                    if (getBounds().withinIncludeTopBottomOfMine(player.getLocation())) {

                        teleportPlayerOut(player);
                    }
                }
            }

        } catch (Exception e) {
            Output.get().logError("&cMineReset: Failed to TP players out of mine. mine= " +
                    getName(), e);
        }
        return System.currentTimeMillis() - start;
    }


    /**
     * <p>This function will teleport the player out of a given mine, or to the given
     * mine. It will not confirm if the player is within the mine before trying to
     * teleport.
     * </p>
     *
     * <p>This function will teleport the player to the defined spawn location, or it
     * will teleport the player to the center of the mine, but on top of the
     * mine's surface.</p>
     *
     * <p>If the player target location has an empty block under its feet, it will
     * then spawn in a single glass block so the player will not take fall damage.
     * If that block is within the mine, it will be reset at a later time when the
     * mine resets and resets that block.  If it is part of spawn for the mine, then
     * the glass block will become part of the landscape.
     * <p>
     *
     * <p>Do not show any TP notifications.  It will be obvious that the mine
     * just reset and that they were teleported out of the mine.  Since there is no
     * control over this message, like enabling or disabling, then I'm just
     * removing it since it just clutters chat and provides no real additional
     * value.
     * </p>
     *
     * @param player
     */
    @Override
    public Location teleportPlayerOut(Player player) {
        Location tpTargetLocation;

        if (isVirtual()) {
            return null;
        }


        if (!isEnabled()) {
            player.sendMessage(String.format("&7MineReset: Teleport failure: Mine is not enabled. Ensure world exists. mine= &3%s ", getName()));
            return null;
        }
//			Location altTp = alternativeTpLocation();
        Location playerLocation = player.getLocation();
        double topY = Math.max(getBounds().getMax().getY(), getBounds().getMin().getY());
        tpTargetLocation = new Location(playerLocation.getWorld(), playerLocation.getX(), topY + 1, playerLocation.getZ());
        // Player needs to stand on something.  If block below feet is air, change it to a
        // glass block:


        player.teleport(tpTargetLocation);

//    	PrisonMines.getInstance().getMinesMessages().getLocalizable("teleported")
//    			.withReplacements(this.getName()).sendTo(player);
        return tpTargetLocation;
    }

	/**
     * <p>The idea behind the alternative TP location is to provide a location for teleporting the player
     * to that is outside the mine and this is used when there isn't a defined spawn location for the
     * mine.
     * </p>
     *
     * <p>The target location is the very center of the mine, on the top.  Plus we are adding two blocks
     * to the Y location so their head is out of the mine's region.  Before it was just adding 1 block,
     * but the player was still in the mine, which could cause issues in extreme events (suffocation
     * events).
     * </p>
     */
    @Override
    public Location alternativeTpLocation() {
        Location altTp = new Location(getBounds().getCenter());
        int y = getBounds().getyBlockMax() + 2;
        altTp.setY(y);
        return altTp;
    }


    @Override
    protected void broadcastResetMessageToAllPlayersWithRadius() {
//    	long start = System.currentTimeMillis();

        if (isVirtual() || getResetTime() <= 0) {
            // ignore:
        } else if (getNotificationMode() != MineNotificationMode.disabled) {
            World world = getBounds().getCenter().getWorld();

            if (world != null) {
                List<Player> players = (world.getPlayers() != null ? world.getPlayers() :
                        Prison.get().getPlatform().getOnlinePlayers());
                for (Player player : players) {

                    // Check for either mode: Within the mine, or by radius from mines center:
                    if (getNotificationMode() == MineNotificationMode.within &&
                            getBounds().withinIncludeTopBottomOfMine(player.getLocation()) ||
                            getNotificationMode() == MineNotificationMode.radius &&
                                    getBounds().within(player.getLocation(), getNotificationRadius())) {

                        if (!isUseNotificationPermission() ||
                                isUseNotificationPermission() &&
                                        player.hasPermission(getMineNotificationPermissionName())) {


                            PrisonMines.getInstance().getMinesMessages()
                                    .getLocalizable("reset_message").withReplacements(getTag())
                                    .sendTo(player);

//    						player.sendMessage( "The mine " + getName() + " has just reset." );
                        }
                    }
                }

            }

        }

//        long stop = System.currentTimeMillis();

//        setStatsMessageBroadcastTimeMS( stop - start );
    }

    @Override
    protected void broadcastPendingResetMessageToAllPlayersWithRadius(MineJob mineJob) {

        if (isVirtual() || getResetTime() <= 0) {
            // ignore:
        } else if (getNotificationMode() != MineNotificationMode.disabled) {
            World world = getBounds().getCenter().getWorld();

            if (world != null) {
                List<Player> players = (world.getPlayers() != null ? world.getPlayers() :
                        Prison.get().getPlatform().getOnlinePlayers());
                for (Player player : players) {
                    // Check for either mode: Within the mine, or by radius from mines center:
                    if (getNotificationMode() == MineNotificationMode.within &&
                            getBounds().withinIncludeTopBottomOfMine(player.getLocation()) ||
                            getNotificationMode() == MineNotificationMode.radius &&
                                    getBounds().within(player.getLocation(), getNotificationRadius())) {

                        if (!isUseNotificationPermission() ||
                                isUseNotificationPermission() &&
                                        player.hasPermission(getMineNotificationPermissionName())) {

                            PrisonMines.getInstance().getMinesMessages()
                                    .getLocalizable("reset_warning")
                                    .withReplacements(getTag(),
                                            Text.getTimeUntilString(Math.round(mineJob.getResetInSec() * 1000.0d)))
                                    .sendTo(player);

//    						player.sendMessage( "The mine " + getName() + " will reset in " +
//    							Text.getTimeUntilString(mineJob.getResetInSec() * 1000) );
                        }


                    }
                }

            }
        }
    }

}
