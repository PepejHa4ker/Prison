/*
 * Prison is a Minecraft plugin for the prison game mode.
 * Copyright (C) 2017 The Prison Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.mcprison.prison.mines.commands;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.chat.FancyMessage;
import tech.mcprison.prison.commands.Arg;
import tech.mcprison.prison.commands.Command;
import tech.mcprison.prison.internal.CommandSender;
import tech.mcprison.prison.internal.Player;
import tech.mcprison.prison.localization.Localizable;
import tech.mcprison.prison.mines.PrisonMines;
import tech.mcprison.prison.mines.data.Block;
import tech.mcprison.prison.mines.data.Mine;
import tech.mcprison.prison.mines.data.MineData;
import tech.mcprison.prison.mines.data.MineData.MineNotificationMode;
import tech.mcprison.prison.mines.managers.MineManager;
import tech.mcprison.prison.output.BulletedListComponent;
import tech.mcprison.prison.output.ChatDisplay;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.output.RowComponent;
import tech.mcprison.prison.selection.Selection;
import tech.mcprison.prison.util.BlockType;
import tech.mcprison.prison.util.MaterialType;
import tech.mcprison.prison.util.Text;

/**
 * @author Dylan M. Perks
 */
public class MinesCommands {
	
	private Long confirmTimestamp;
	
	private String lastMineReferenced;
	private Long lastMineReferencedTimestamp;
	

    private boolean performCheckMineExists(CommandSender sender, String mineName) {
    	mineName = Text.stripColor( mineName );
        if (PrisonMines.getInstance().getMine(mineName) == null) {
            PrisonMines.getInstance().getMinesMessages().getLocalizable("mine_does_not_exist")
                .sendTo(sender);
            return false;
        }
        return true;
    }

    @Command(identifier = "mines create", description = "Creates a new mine.", 
    		onlyPlayers = false, permissions = "mines.create")
    public void createCommand(CommandSender sender,
        @Arg(name = "mineName", description = "The name of the new mine.") String mineName) {

    	PrisonMines pMines = PrisonMines.getInstance();
        Selection selection = Prison.get().getSelectionManager().getSelection((Player) sender);
        if (!selection.isComplete()) {
        	pMines.getMinesMessages().getLocalizable("select_bounds")
                .sendTo(sender, Localizable.Level.ERROR);
            return;
        }

        if (!selection.getMin().getWorld().getName()
            .equalsIgnoreCase(selection.getMax().getWorld().getName())) {
        	pMines.getMinesMessages().getLocalizable("world_diff")
                .sendTo(sender, Localizable.Level.ERROR);
            return;
        }

        if (PrisonMines.getInstance().getMine(mineName) != null) {
        	pMines.getMinesMessages().getLocalizable("mine_exists")
                .sendTo(sender, Localizable.Level.ERROR);
            return;
        }

        setLastMineReferenced(mineName);
        
        Mine mine = new Mine(mineName, selection);
        pMines.getMineManager().add(mine);
        pMines.getMinesMessages().getLocalizable("mine_created").sendTo(sender);
        
        // Delete the selection:
        Prison.get().getSelectionManager().clearSelection((Player) sender);

    }

    @Command(identifier = "mines set spawn", description = "Set the mine's spawn to where you're standing.", 
    		onlyPlayers = false, permissions = "mines.set")
    public void spawnpointCommand(CommandSender sender,
        @Arg(name = "mineName", description = "The name of the mine to edit.") String mineName) {

        if (!performCheckMineExists(sender, mineName)) {
            return;
        }

        PrisonMines pMines = PrisonMines.getInstance();
        Mine mine = pMines.getMine(mineName);

        if (!mine.getWorld().isPresent()) {
            pMines.getMinesMessages().getLocalizable("missing_world")
                .sendTo(sender);
            return;
        }

        if (!((Player) sender).getLocation().getWorld().getName()
            .equalsIgnoreCase(
                mine.getWorldName())) {
            pMines.getMinesMessages().getLocalizable("spawnpoint_same_world")
                .sendTo(sender);
            return;
        }

        setLastMineReferenced(mineName);
        
        mine.setSpawn(((Player) sender).getLocation());
        pMines.getMineManager().saveMine(mine);
        pMines.getMinesMessages().getLocalizable("spawn_set").sendTo(sender);
    }

    @Command(identifier = "mines block add", permissions = "mines.block", onlyPlayers = false, 
    						description = "Adds a block to a mine.")
    public void addBlockCommand(CommandSender sender,
    			@Arg(name = "mineName", description = "The name of the mine to add the block to.")
            			String mineName, 
            	@Arg(name = "block", description = "The block's name or ID.") 
    					String block,
            	@Arg(name = "chance", description = "The percent chance (out of 100) that this block will occur.")
    					double chance) {
        if (!performCheckMineExists(sender, mineName)) {
            return;
        }

        PrisonMines pMines = PrisonMines.getInstance();
        
        setLastMineReferenced(mineName);
        
        Mine m = pMines.getMine(mineName);
        
        if ( !m.isEnabled() ) {
        	sender.sendMessage( "&cMine is disabled&7. Use &a/mines info &7for possible cause." );
        	return;
        }
        

        BlockType blockType = BlockType.getBlock(block);
        if (blockType == null || blockType.getMaterialType() != MaterialType.BLOCK ) {
        	pMines.getMinesMessages().getLocalizable("not_a_block")
                .withReplacements(block).sendTo(sender);
            return;
        }

        if (m.isInMine(blockType)) {
        	pMines.getMinesMessages().getLocalizable("block_already_added")
                .sendTo(sender);
            return;
        }

        if ( chance <= 0 ) {
        	sender.sendMessage( "The percent chance must have a value greater than zero." );
        	return;
        }
        
        final double[] totalComp = {chance};
        m.getBlocks().forEach(block1 -> totalComp[0] += block1.getChance());
        if (totalComp[0] > 100.0d) {
        	pMines.getMinesMessages().getLocalizable("mine_full")
                .sendTo(sender, Localizable.Level.ERROR);
            return;
        }

        m.getBlocks().add(new Block(blockType, chance));
        pMines.getMineManager().saveMine( m );
        
        pMines.getMinesMessages().getLocalizable("block_added")
            .withReplacements(block, mineName).sendTo(sender);
        getBlocksList(m, null).send(sender);

        //pMines.getMineManager().clearCache();
    }

    @Command(identifier = "mines block set", permissions = "mines.block", onlyPlayers = false, 
    					description = "Changes the percentage of a block in a mine.")
    public void setBlockCommand(CommandSender sender,
    			@Arg(name = "mineName", description = "The name of the mine to edit.") 
    					String mineName,
    			@Arg(name = "block", description = "The block's name or ID.") 
    					String block,
    			@Arg(name = "chance", description = "The percent chance (out of 100) that this block will occur.") 
    					double chance) {
    	
        if (!performCheckMineExists(sender, mineName)) {
            return;
        }

        setLastMineReferenced(mineName);
        
        PrisonMines pMines = PrisonMines.getInstance();
        Mine m = pMines.getMine(mineName);

        if ( !m.isEnabled() ) {
        	sender.sendMessage( "&cMine is disabled&7. Use &a/mines info &7for possible cause." );
        	return;
        }
        
        BlockType blockType = BlockType.getBlock(block);
        if (blockType == null) {
        	pMines.getMinesMessages().getLocalizable("not_a_block")
                .withReplacements(block).sendTo(sender);
            return;
        }

        // Change behavior: If trying to change a block that is not in the mine, then instead add it:
        if (!m.isInMine(blockType)) {
        	addBlockCommand( sender, mineName, block, chance );
//        	pMines.getMinesMessages().getLocalizable("block_not_removed")
//                .sendTo(sender);
            return;
        }

        // If it's 0, just delete it!
        if (chance <= 0.0d) {
        	deleteBlock( sender, pMines, m, blockType );
//            delBlockCommand(sender, mine, block);
            return;
        }

        final double[] totalComp = {chance};
        m.getBlocks().forEach(block1 -> {
            if (block1.getType() == blockType) {
                totalComp[0] -= block1.getChance();
            } else {
                totalComp[0] += block1.getChance();
            }
        });
        if (totalComp[0] > 100.0d) {
        	pMines.getMinesMessages().getLocalizable("mine_full")
                .sendTo(sender, Localizable.Level.ERROR);
            return;
        }

        for (Block blockObject : m.getBlocks()) {
            if (blockObject.getType() == blockType) {
                blockObject.setChance(chance);
            }
        }

        pMines.getMineManager().saveMine( m );
        
        pMines.getMinesMessages().getLocalizable("block_set")
            .withReplacements(block, mineName).sendTo(sender);
        getBlocksList(m, null).send(sender);

        //pMines.getMineManager().clearCache();

    }

    @Command(identifier = "mines block remove", permissions = "mines.block", onlyPlayers = false, description = "Deletes a block from a mine.")
    public void delBlockCommand(CommandSender sender,
        @Arg(name = "mineName", description = "The name of the mine to edit.") String mineName,
        @Arg(name = "block", def = "AIR", description = "The block's name or ID.") String block) {

        if (!performCheckMineExists(sender, mineName)) {
            return;
        }

        setLastMineReferenced(mineName);
        
        PrisonMines pMines = PrisonMines.getInstance();
        Mine m = pMines.getMine(mineName);
        
        if ( !m.isEnabled() ) {
        	sender.sendMessage( "&cMine is disabled&7. Use &a/mines info &7for possible cause." );
        	return;
        }
        
        BlockType blockType = BlockType.getBlock(block);
        if (blockType == null) {
        	pMines.getMinesMessages().getLocalizable("not_a_block")
                .withReplacements(block).sendTo(sender);
            return;
        }

        if (!m.isInMine(blockType)) {
        	pMines.getMinesMessages().getLocalizable("block_not_removed")
                .sendTo(sender);
            return;
        }

        deleteBlock( sender, pMines, m, blockType );
    }

    /**
     * Delete only the first occurrence of a block with the given BlockType.
     * 
     * @param sender
     * @param pMines
     * @param m
     * @param blockType
     */
	private void deleteBlock( CommandSender sender, PrisonMines pMines, Mine m, BlockType blockType )
	{
		Block rBlock = null;
		for ( Block block : m.getBlocks() ) {
			if ( block.getType() ==  blockType ) {
				rBlock = block;
				break;
			}
		}
		if ( m.getBlocks().remove( rBlock )) {
			pMines.getMineManager().saveMine( m );
			
			pMines.getMinesMessages().getLocalizable("block_deleted")
			.withReplacements(blockType.name(), m.getName()).sendTo(sender);
			getBlocksList(m, null).send(sender);
		}
	}

    @Command(identifier = "mines block search", permissions = "mines.block", 
    				description = "Searches for a block to add to a mine.")
    public void searchBlockCommand(CommandSender sender,
        @Arg(name = "search", def = " ", description = "Any part of the block's name or ID.") String search,
        @Arg(name = "page", def = "1", description = "Page of search results (optional)") String page ) {

    	PrisonMines pMines = PrisonMines.getInstance();
    	if (search == null)
    	{
    		pMines.getMinesMessages().getLocalizable("block_search_blank").sendTo(sender);
    	}
    	
    	ChatDisplay display = blockSearchBuilder(search, page);
        
        display.send(sender);

        //pMines.getMineManager().clearCache();
    }

	private ChatDisplay blockSearchBuilder(String search, String page)
	{
		List<BlockType> blocks = new ArrayList<>();
    	for (BlockType block : BlockType.values())
		{
			if ( block.getMaterialType() == MaterialType.BLOCK && 
					(block.getId().contains(search.toLowerCase()) || 
					block.name().toLowerCase().contains(search.toLowerCase())) )
			{
				blocks.add(block);
			}
		}
    	
        
        CommandPagedData cmdPageData = new CommandPagedData(
        		"/mines block search " + search, blocks.size(),
        		0, page );
    	
    	// Same page logic as in mines info
//    	int curPage = 1;
//    	int pageSize = 10;
//    	int pages = (blocks.size() / pageSize) + 1;
//    	try
//		{
//			curPage = Integer.parseInt(page);
//		}
//		catch ( NumberFormatException e )
//		{
//			// Ignore: Not an integer, will use the default value.
//		}
//    	curPage = ( curPage < 1 ? 1 : (curPage > pages ? pages : curPage ));
//    	int pageStart = (curPage - 1) * pageSize;
//    	int pageEnd = ((pageStart + pageSize) > blocks.size() ? blocks.size() : pageStart + pageSize);

    	
        ChatDisplay display = new ChatDisplay("Block Search (" + blocks.size() + ")");
        display.text("&8Click a block to add it to a mine.");
        
        BulletedListComponent.BulletedListBuilder builder =
        						new BulletedListComponent.BulletedListBuilder();
        for ( int i = cmdPageData.getPageStart(); i < cmdPageData.getPageEnd(); i++ )
        {
        	BlockType block = blocks.get(i);
            FancyMessage msg =
                    new FancyMessage(
                    		String.format("&7%s %s  (%s)", 
                    				Integer.toString(i), block.name(), block.getId().replace("minecraft:", "")))
                    .suggest("/mines block add " + getLastMineReferenced() + " " + block.name() + " %")
                        .tooltip("&7Click to add block to a mine.");
                builder.add(msg);
        }
        display.addComponent(builder.build());
        
        // This command plus parameters used:
//        String pageCmd = "/mines block search " + search;
        
        cmdPageData.generatePagedCommandFooter( display );
        
		return display;
	}


    @Command(identifier = "mines delete", permissions = "mines.delete", onlyPlayers = false, description = "Deletes a mine.")
    public void deleteCommand(CommandSender sender,
        @Arg(name = "mineName", description = "The name of the mine to delete.") String mineName,
    	@Arg(name = "confirm", def = "", description = "Confirm that the mine should be deleted") String confirm) {
        if (!performCheckMineExists(sender, mineName)) {
            return;
        }
        
        setLastMineReferenced(mineName);
        
        // They have 1 minute to confirm.
        long now = System.currentTimeMillis();
        if ( getConfirmTimestamp() != null && ((now - getConfirmTimestamp()) < 1000 * 60 ) && 
        		confirm != null && "confirm".equalsIgnoreCase( confirm ))  {
        	setConfirmTimestamp( null );
        	
        	PrisonMines pMines = PrisonMines.getInstance();
        	
        	Mine mine = pMines.getMine(mineName);
        	
            if ( !mine.isEnabled() ) {
            	sender.sendMessage( "&cMine is disabled&7. Use &a/mines info &7for possible cause." );
            	return;
            }
        	
        	// Remove from the manager:
        	pMines.getMineManager().removeMine(mine);
        	
        	// Terminate the running task for mine resets. Will allow it to be garbage collected.
        	mine.terminateJob();
        	
        	setLastMineReferenced(null);
        	
        	pMines.getMinesMessages().getLocalizable("mine_deleted").sendTo(sender);
        	
        } else if ( getConfirmTimestamp() == null || ((now - getConfirmTimestamp()) >= 1000 * 60 ) ) {
        	setConfirmTimestamp( now );

        	ChatDisplay chatDisplay = new ChatDisplay("&cDelete " + mineName);
        	BulletedListComponent.BulletedListBuilder builder = new BulletedListComponent.BulletedListBuilder();
        	builder.add( new FancyMessage(
                    "&3Confirm the deletion of this mine" )
                    .suggest("/mines delete " + mineName + " cancel"));

        	builder.add( new FancyMessage(
        			"&3Click &eHERE&3 to display the command" )
        			.suggest("/mines delete " + mineName + " cancel"));
        	
        	builder.add( new FancyMessage(
        			"&3Enter: &7/mines delete " + mineName + " confirm" )
        			.suggest("/mines delete " + mineName + " cancel"));
        	
        	builder.add( new FancyMessage(
        			"&3Then change &ecancel&3 to &econfirm&3." )
        			.suggest("/mines delete " + mineName + " cancel"));
        	
        	builder.add( new FancyMessage("You have 1 minute to respond."));
        	
            chatDisplay.addComponent(builder.build());
            chatDisplay.send(sender);
            
        } else if (confirm != null && "cancel".equalsIgnoreCase( confirm )) {
        	setConfirmTimestamp( null );
        	
        	ChatDisplay display = new ChatDisplay("&cDelete " + mineName);
            display.text("&8Delete canceled.");

            display.send( sender );
            
        } else {
	    	ChatDisplay display = new ChatDisplay("&cDelete " + mineName);
	    	display.text("&8Delete confirmation failed. Try again.");
	    	
	    	display.send( sender );
	    }
        
    }

    @Command(identifier = "mines info", permissions = "mines.info", onlyPlayers = false, 
    				description = "Lists information about a mine.")
    public void infoCommand(CommandSender sender,
        @Arg(name = "mineName", description = "The name of the mine to view.") String mineName,
        @Arg(name = "page", def = "1", 
        				description = "Page of search results (optional) [1-n, ALL]") String page 
    		) {
        if (!performCheckMineExists(sender, mineName)) {
            return;
        }

        
        setLastMineReferenced(mineName);
        
        PrisonMines pMines = PrisonMines.getInstance();
    	MineManager mMan = pMines.getMineManager();
        Mine m = pMines.getMine(mineName);
        
        
        CommandPagedData cmdPageData = new CommandPagedData(
        		"/mines info " + m.getName(), m.getBlocks().size(),
        		1, page );
        
//        // Same page logic as in mines block search:
//    	int curPage = 1;
//    	int pageSize = 10;
//    	int pages = (m.getBlocks().size() / pageSize) + 1;
//    	try
//		{
//			curPage = Integer.parseInt(page);
//		}
//		catch ( NumberFormatException e )
//		{
//			// Ignore: Not an integer, will use the default value.
//		}
//    	curPage = ( curPage < 1 ? 1 : (curPage > pages ? pages : curPage ));
//    	int pageStart = (curPage - 1) * pageSize;
//    	int pageEnd = ((pageStart + pageSize) > m.getBlocks().size() ? m.getBlocks().size() : pageStart + pageSize);

    	

        DecimalFormat dFmt = new DecimalFormat("#,##0");
        DecimalFormat fFmt = new DecimalFormat("#,##0.00");
        
        ChatDisplay chatDisplay = new ChatDisplay("&bMine: &3" + m.getName());

        // Display Mine Info only:
        if ( cmdPageData.getCurPage() == 1 ) {
        	
        	if ( !m.isEnabled() ) {
        		chatDisplay.text("&cWarning!! This mine is &lDISABLED&r&c!!" );
        	}
        	
        	
        	String worldName = m.getWorld().isPresent() ? m.getWorld().get().getName() : "&cmissing";
        	chatDisplay.text("&3World: &7%s", worldName);
        	
        	String minCoords = m.getBounds().getMin().toBlockCoordinates();
        	String maxCoords = m.getBounds().getMax().toBlockCoordinates();
        	chatDisplay.text("&3Bounds: &7%s &8to &7%s", minCoords, maxCoords);
        	Player player = getPlayer( sender );
        	
        	chatDisplay.text("&3Center: &7%s   &3%s &7%s", 
        			m.getBounds().getCenter().toBlockCoordinates(), 
        			(player == null ? "" : "Distance:"),
        			(player == null ? "" : fFmt.format( m.getBounds().getDistance3d( player.getLocation() ) ))
        			);
        	if ( mMan.isMineStats() ) {
        	}
        	
        	{
        		RowComponent row = new RowComponent();
        		double rtMinutes = m.getResetTime() / 60.0D;
        		row.addTextComponent( "&3Reset time: &7%s &3Secs (&7%.2f &3Mins)", 
        				Integer.toString(m.getResetTime()), rtMinutes );
        		chatDisplay.addComponent( row );
        	}
        	
        	{
        		RowComponent row = new RowComponent();
        		row.addTextComponent( "&3Mine Reset Count: &7%s ", 
        				dFmt.format(m.getResetCount()) );
        		chatDisplay.addComponent( row );
        	}
        	
        	{
        		RowComponent row = new RowComponent();
        		
        		long targetResetTime = m.getTargetResetTime();
        		double remaining = ( targetResetTime <= 0 ? 0d : 
        			(targetResetTime - System.currentTimeMillis()) / 1000d);
        		double rtMinutes = remaining / 60.0D;
        		
        		row.addTextComponent( "&3Time Remaining Until Reset: &7%s &3Secs (&7%.2f &3Mins)", 
        				dFmt.format( remaining ), rtMinutes );
        		chatDisplay.addComponent( row );
        	}
        	
        	{
        		RowComponent row = new RowComponent();
        		row.addTextComponent( "&3Notification Mode: &7%s &7%s", 
        				m.getNotificationMode().name(), 
        				( m.getNotificationMode() == MineNotificationMode.radius ? 
        						dFmt.format( m.getNotificationRadius() ) + " blocks" : "" ) );
        		chatDisplay.addComponent( row );
        	}
        	
        	
//        chatDisplay.text("&3Size: &7%d&8x&7%d&8x&7%d", Math.round(m.getBounds().getWidth()),
//            Math.round(m.getBounds().getHeight()), Math.round(m.getBounds().getLength()));
        	
        	{
        		RowComponent row = new RowComponent();
        		row.addTextComponent( "&3Size: &7%d&8x&7%d&8x&7%d", Math.round(m.getBounds().getWidth()),
        				Math.round(m.getBounds().getHeight()), Math.round(m.getBounds().getLength()) );
        		
        		row.addTextComponent( "    &3Volume: &7%s &3Blocks", 
        				dFmt.format( Math.round(m.getBounds().getTotalBlockCount())) );
        		chatDisplay.addComponent( row );
        	}
        	
        	
        	{
        		RowComponent row = new RowComponent();
        		row.addTextComponent( "&3Blocks Remaining: &7%s  %s%% ",
        				dFmt.format( m.getRemainingBlockCount() ), 
        				fFmt.format( m.getPercentRemainingBlockCount() ) );
        		
        		chatDisplay.addComponent( row );
        	}
        	
        	{
        		RowComponent row = new RowComponent();
        		if ( m.isZeroBlockResetDisabled() ) {
        			row.addTextComponent( "&3Zero Blocks Reset Delay: &cDISABLED");
        		} else {
        			row.addTextComponent( "&3Zero Blocks Reset Delay: &7%s &3Seconds",
        					fFmt.format( m.getZeroBlockResetDelaySec() ));
        		}
        		
        		chatDisplay.addComponent( row );
        	}
        	
        	
        	if ( m.isSkipResetEnabled() ) {
        		RowComponent row = new RowComponent();
        		row.addTextComponent( "&3Skip Reset &2Enabled&3: &3Threshold: &7%s  &3Skip Limit: &7%s",
        				fFmt.format( m.getSkipResetPercent() ), dFmt.format( m.getSkipResetBypassLimit() ));
        		chatDisplay.addComponent( row );
        		
        		if ( m.getSkipResetBypassCount() > 0 ) {
        			RowComponent row2 = new RowComponent();
        			row2.addTextComponent( "    &3Skipping Enabled: Skip Count: &7%s",
        					dFmt.format( m.getSkipResetBypassCount() ));
        			chatDisplay.addComponent( row2 );
        		}
        	} else {
        		RowComponent row = new RowComponent();
        		row.addTextComponent( "&3Skip Mine Reset if no Activity: &cnot set");
        		chatDisplay.addComponent( row );
        	}
        	
        	
        	String spawnPoint = m.getSpawn() != null ? m.getSpawn().toBlockCoordinates() : "&cnot set";
        	chatDisplay.text("&3Spawnpoint: &7%s", spawnPoint);
        	
        	if ( mMan.isMineStats() ) {
        		RowComponent rowStats = new RowComponent();
        		rowStats.addTextComponent( "  -- &7 Stats :: " );
        		rowStats.addTextComponent( m.statsMessage() );
        		
        		chatDisplay.addComponent(rowStats);
        	}
        	
        }
        
        if ( cmdPageData.isShowAll() || cmdPageData.getCurPage() > 1 ) {
        	chatDisplay.text("&3Blocks:");
        	chatDisplay.text("&8Click on a block's name to edit its chances of appearing.");
        	BulletedListComponent list = getBlocksList(m, cmdPageData);
        	
        	chatDisplay.addComponent(list);
        }
        
        cmdPageData.generatePagedCommandFooter( chatDisplay );

        chatDisplay.send(sender);
    }

    private BulletedListComponent getBlocksList(Mine m, CommandPagedData cmdPageData) {
        BulletedListComponent.BulletedListBuilder builder = new BulletedListComponent.BulletedListBuilder();

        DecimalFormat dFmt = new DecimalFormat("##0.00");
        double totalChance = 0.0d;
        int count = 0;
        for (Block block : m.getBlocks()) {
            double chance = Math.round(block.getChance() * 100.0d) / 100.0d;
            totalChance += chance;
 
            if ( cmdPageData == null ||
            		count++ >= cmdPageData.getPageStart() && count <= cmdPageData.getPageEnd() ) {
            	String blockName =
            			StringUtils.capitalize(block.getType().name().replaceAll("_", " ").toLowerCase());
            	String percent = dFmt.format(chance) + "%";
            	FancyMessage msg = new FancyMessage(String.format("&7%s - %s  (%s)", 
            			percent, block.getType().name(), blockName))
            			.suggest("/mines block set " + m.getName() + " " + block.getType().name() + " %")
            			.tooltip("&7Click to edit the block's chance.");
            	builder.add(msg);
            	
            }
        }

        if (totalChance < 100.0d) {
            builder.add("&e%s - Air", dFmt.format(100.0d - totalChance) + "%");
        }

        return builder.build();
    }

    @Command(identifier = "mines reset", permissions = "mines.reset", description = "Resets a mine.")
    public void resetCommand(CommandSender sender,
        @Arg(name = "mineName", description = "The name of the mine to reset.") String mineName) {

        if (!performCheckMineExists(sender, mineName)) {
            return;
        }

        setLastMineReferenced(mineName);
        
        PrisonMines pMines = PrisonMines.getInstance();
        
        Mine m = pMines.getMine(mineName);
        
        if ( !m.isEnabled() ) {
        	sender.sendMessage( "&cMine is disabled&7. Use &a/mines info &7for possible cause." );
        	return;
        }
        
        try {
        	m.manualReset();
        } catch (Exception e) {
        	pMines.getMinesMessages().getLocalizable("mine_reset_fail")
                .sendTo(sender);
            Output.get().logError("Couldn't reset mine " + mineName, e);
        }

        pMines.getMinesMessages().getLocalizable("mine_reset").sendTo(sender);
    }


    @Command(identifier = "mines list", permissions = "mines.list", onlyPlayers = false)
    public void listCommand(CommandSender sender, 
            @Arg(name = "page", def = "1", 
            	description = "Page of search results (optional) [1-n, ALL]") String page 
    		) {
        ChatDisplay display = new ChatDisplay("Mines");
        display.text("&8Click a mine's name to see more information.");
    	Player player = getPlayer( sender );
    	
        PrisonMines pMines = PrisonMines.getInstance();
    	MineManager mMan = pMines.getMineManager();
    	
    	
    	// Sort mines by: total blocks mined, name
    	List<Mine> mineList = pMines.getMines();
    	
    	// Sort first by name, then blocks mined so final sort order will be:
    	//   Most blocks mined, then alphabetical
    	mineList.sort( (a, b) -> a.getName().compareToIgnoreCase( b.getName()) );
    	mineList.sort( (a, b) -> Long.compare(b.getTotalBlocksMined(), a.getTotalBlocksMined()) );
    	
        
        CommandPagedData cmdPageData = new CommandPagedData(
        		"/mines list", pMines.getMines().size(),
        		0, page, 6 );
        
        BulletedListComponent list = 
        		getMinesLineItemList(pMines.getMines(), player, cmdPageData, mMan.isMineStats());
    	
    	display.addComponent(list);
    	
        
        cmdPageData.generatePagedCommandFooter( display );
        
        display.send(sender);
    }


    private BulletedListComponent getMinesLineItemList( List<Mine> mines, Player player,
    		CommandPagedData cmdPageData, boolean isMineStatsEnabled )
	{
    	BulletedListComponent.BulletedListBuilder builder =
    			new BulletedListComponent.BulletedListBuilder();
    	    	
    	DecimalFormat dFmt = new DecimalFormat("#,##0");
    	DecimalFormat fFmt = new DecimalFormat("#,##0.00");
    	
    	int count = 0;
    	 
        for (Mine m : mines) {
        	
            if ( cmdPageData == null ||
            		count++ >= cmdPageData.getPageStart() && count <= cmdPageData.getPageEnd() ) {
            
            	RowComponent row = new RowComponent();
            	
            	//row.addTextComponent( m.getWorldName() + " " );
            	
            	row.addFancy( 
            			new FancyMessage( String.format("&3Mine: &7%s ", m.getName()) )
            					.command("/mines info " + m.getName())
            					.tooltip("&7Click to view info."));
            	
            	if ( !m.isEnabled() ) {
            		row.addFancy(  
            				new FancyMessage( "&cDISABLED!! " )
            				.command("/mines info " + m.getName())
            				.tooltip("&7Click to view possible reason why the mine is " +
            						"disabled. World may not exist? "));
            	}
            	
            	row.addFancy( 
            			new FancyMessage("&eTP ").command("/mines tp " + m.getName())
            			.tooltip("&7Click to TP to the mine"));
            	
            	row.addTextComponent( "  &3Reset: &7" );
            	
            	row.addFancy( 
            			new FancyMessage(dFmt.format(m.getRemainingTimeSec()))
            			.tooltip( "Estimated time in seconds before the mine resets" ) );
            	row.addTextComponent( " sec &3(&b" );
            	row.addFancy( 
            			new FancyMessage(dFmt.format(m.getResetTime()))
            			.tooltip( "Reset time in seconds" ) );
            	row.addTextComponent( " sec&3)&b" );
            	
            	if ( player != null && m.getBounds().withinSameWorld( player.getLocation() ) ) {
            		
            		row.addTextComponent( "  &3Dist: &7");
            		row.addFancy( 
            				new FancyMessage( fFmt.format(m.getBounds().getDistance3d(player.getLocation()))).
            				tooltip("Distance to the Mine") );
            		
            	}
            	
            	builder.add(row.getFancy());
            	
            	
            	
            	RowComponent row2 = new RowComponent();
//            	row2.addTextComponent( "            &3Rem: " );
            	
            	// Right justify the total blocks mined, with 1000's separators:
            	String blocksMined = "           " + dFmt.format( m.getTotalBlocksMined() );
            	blocksMined = blocksMined.substring( blocksMined.length() - 10);
            	
            	row2.addFancy( 
            			new FancyMessage( String.format("  %s  &3Rem: ", blocksMined)).
            			tooltip( "Blocks mined" ) );
            	
            	row2.addFancy( 
            			new FancyMessage(fFmt.format(m.getPercentRemainingBlockCount())).
            			tooltip( "Percent Blocks Remaining" ) );
            	
            	row2.addTextComponent( "%%  &3RCnt: &7" );
            	
            	row2.addFancy( 
            			new FancyMessage(dFmt.format(m.getResetCount())).
            			tooltip( "Times the mine was reset." ) );
            	
            	
            	row2.addTextComponent( " &3 Vol: &7" );
            	row2.addFancy( 
            			new FancyMessage(dFmt.format(m.getBounds().getTotalBlockCount())).
            			tooltip( "Volume in Blocks" ) );
            	
            	
//       	 String noteMode = m.getNotificationMode().name() + 
//       			 ( m.getNotificationMode() == MineNotificationMode.radius ? 
//       					 " " + dFmt.format( m.getNotificationRadius() ) : "" );
//       	 row.addFancy( 
//       			 new FancyMessage(noteMode).tooltip( "Notification Mode" ) );
//       	 
//       	 row.addTextComponent( "&7 - &b" );
//
//       	 row.addFancy( 
//       			 new FancyMessage(m.getBounds().getDimensions()).tooltip( "Size of Mine" ) );
//       	 
//       	 row.addTextComponent( "&7 - &b");
            
            	builder.add(row2.getFancy());
            	
            	
            	if ( isMineStatsEnabled ) {
            		RowComponent rowStats = new RowComponent();
            		
            		rowStats.addTextComponent( "  -- &7 Stats :: " );
            		
            		rowStats.addTextComponent( m.statsMessage() );
            		
            		builder.add(rowStats.getFancy());
            	}
        	
            }
       }
       
//        display.addComponent(builder.build());

		return builder.build();
	}

	/**
     * <p>The following command will change the mine's time between resets. But it will
     * not be applied until after the next reset.
     * </p>
     * 
     * @param sender
     * @param mineName
     * @param time
     */
    @Command(identifier = "mines skipReset", permissions = "mines.skipreset", 
    		description = "Set a mine to skip the reset if not enough blocks have been mined.")
    public void skipResetCommand(CommandSender sender,
        @Arg(name = "mineName", description = "The name of the mine to edit.") String mineName,
        @Arg(name = "enabled", description = "Enable the skip reset processing: 'Enabled' or 'Disable'", 
        		def = "disabled") String enabled,
        @Arg(name = "percent", description = "Percent threshold before resetting.", def = "80" ) String percent,
        @Arg(name = "bypassLimit", description = "Limit number of skips before bypassing and performing a reset",
        		def = "50") String bypassLimit
    		) {
        
        if (performCheckMineExists(sender, mineName)) {
        	setLastMineReferenced(mineName);
        	
        	if ( enabled == null || !"enabled".equalsIgnoreCase( enabled ) && !"disabled".equalsIgnoreCase( enabled )) {
        		Output.get().sendWarn( sender,"&7Invalid &benabled&7 value. Must be either &benabled&7 or " +
        				"&bdisabled&7.  Was &b%s&7.", (enabled == null ? "&c-blank-" : enabled) );
        		return;
        	}

        	PrisonMines pMines = PrisonMines.getInstance();
        	Mine m = pMines.getMine(mineName);
            
            if ( !m.isEnabled() ) {
            	sender.sendMessage( "&cMine is disabled&7. Use &a/mines info &7for possible cause." );
            	return;
            }
            
        	boolean skipEnabled = "enabled".equalsIgnoreCase( enabled );
        	double skipPercent = 80.0d;
        	int skipBypassLimit = 50;
        	
        	try {
				skipPercent = Double.parseDouble( percent );
				if ( skipPercent < 0.0d ) {
					skipPercent = 0.0d;
				} else if ( skipPercent > 100.0d ) {
					skipPercent = 100.0d;
				}
			}
			catch ( NumberFormatException e1 ) {
				Output.get().sendWarn( sender,"&7Invalid percentage. Not a number. " +
						"Was &b%s&7.", (enabled == null ? "&c-blank-" : enabled) );
				return;
			}
        	
        	try {
				skipBypassLimit = Integer.parseInt( bypassLimit );
				if ( skipBypassLimit < 1 ) {
					skipBypassLimit = 1;
				} 
			}
			catch ( NumberFormatException e1 ) {
				Output.get().sendWarn( sender,"&7Invalid bypass limit. Not number. " +
						"Was &b%s&7.", (bypassLimit == null ? "-blank-" : bypassLimit) );
			}
        	
        	m.setSkipResetEnabled( skipEnabled );
        	m.setSkipResetPercent( skipPercent );
        	m.setSkipResetBypassLimit( skipBypassLimit );
        	
        	pMines.getMineManager().saveMine( m );
        	
        	// User's message:
        	String message = String.format( "&7mines skipreset for &b%s&7: &b%s&7  " +
					        			"threshold: &b%.2f&7 percent  bypassLimit: &b%d", 
					        			m.getName(), (skipEnabled ? "enabled" : "disabled"),
					        			skipPercent, skipBypassLimit );
        	Output.get().sendInfo( sender, message );
        	
        	// Server Log message:
        	Player player = getPlayer( sender );
        	Output.get().logInfo( "%s :: Changed by: %s", message,
        								(player == null ? "console" : player.getDisplayName()) );
        } 
    }



    /**
     * <p>The following command will change the mine's time between resets. But it will
     * not be applied until after the next reset.
     * </p>
     * 
     * @param sender
     * @param mineName
     * @param time
     */
    @Command(identifier = "mines resetTime", permissions = "mines.resettime", 
    		description = "Set a mine's time  to reset.")
    public void resetTimeCommand(CommandSender sender,
        @Arg(name = "mineName", description = "The name of the mine to edit.") String mineName,
        @Arg(name = "time", description = "Time in seconds for the mine to auto reset." ) String time
        
    		) {
        
        if (performCheckMineExists(sender, mineName)) {
        	setLastMineReferenced(mineName);

        	try {
        		int resetTime = MineData.MINE_RESET__TIME_SEC__DEFAULT;

        		if ( time != null && time.trim().length() > 0 ) {
        			resetTime = Integer.parseInt( time );
        		}

				if ( resetTime < MineData.MINE_RESET__TIME_SEC__MINIMUM ) {
					Output.get().sendWarn( sender, 
							"&7Invalid resetTime value for &b%s&7. Must be an integer value of &b%d&7 or greater. [&b%d&7]",
							mineName, MineData.MINE_RESET__TIME_SEC__MINIMUM, resetTime );
				} else {
					PrisonMines pMines = PrisonMines.getInstance();
					Mine m = pMines.getMine(mineName);
			        
			        if ( !m.isEnabled() ) {
			        	sender.sendMessage( "&cMine is disabled&7. Use &a/mines info &7for possible cause." );
			        	return;
			        }
					
					m.setResetTime( resetTime );
					
        			pMines.getMineManager().saveMine( m );
        								
					// User's message:
					Output.get().sendInfo( sender, "&7mines set resettime: &b%s &7resetTime set to &b%d", m.getName(), resetTime );
					
					// Server Log message:
					Player player = getPlayer( sender );
					Output.get().logInfo( "&bmines set resettime&7: &b%s &7set &b%s &7resetTime to &b%d", 
							(player == null ? "console" : player.getDisplayName()), m.getName(), resetTime  );
				}
			}
			catch ( NumberFormatException e ) {
				Output.get().sendWarn( sender, 
						"&7Invalid resetTime value for &b%s&7. Must be an integer value of &b%d &7or greater. [&b%s&7]",
						mineName, MineData.MINE_RESET__TIME_SEC__MINIMUM, time );
			}
        } 
    }

    
    
    /**
     * <p>When a mine reaches zero blocks, a manual reset will be issued to run.  By default
     * it will have a 0 second delay before running, but this command controls how long of
     * a delay to use.
     * </p>
     * 
     * <p>Although the delay is in seconds, it should be known that the value will be multiplied
     * by 20 to convert it to ticks.  So any value less than 0.05 will be treated as zero and
     * effectively will be in 0.05 increments.  Give or take a tick should not matter, but 
     * beware if a player, or owner, complains that 0.17 is the same as 0.15.   
     * </p>
     * 
     * @param sender
     * @param mineName
     * @param time
     */
    @Command(identifier = "mines zeroBlockResetDelay", permissions = "mines.zeroblockresetdelay", 
    		description = "Set a mine's delay before reset when it reaches zero blocks.")
    public void zeroBlockResetDelayCommand(CommandSender sender,
    		@Arg(name = "mineName", description = "The name of the mine to edit.") String mineName,
    		@Arg(name = "time/DISABLE", description = "Delay in seconds before resetting when the mine reaches " +
    				"zero blocks, or DISABLE." ) String time
    		
    		) {
    	
    	if (performCheckMineExists(sender, mineName)) {
    		setLastMineReferenced(mineName);
    		
    		try {
    			double resetTime = 
    						time != null && "disable".equalsIgnoreCase( time ) ? -1.0d : 
    						0.0d;
    			
    			if ( resetTime != -1.0d && time != null && time.trim().length() > 0 ) {
    				resetTime = Double.parseDouble( time );
    				
    				// Only displaying two decimal positions, since 0.01 is 10 ms. 
    				// Anything less than 0.01 is set to ZERO so it does not mess with anything unseen.
    				// Also any value less than 0.05 is basically zero since this value has to be
    				// converted to ticks.
    				if ( resetTime < 0.01d ) {
    					resetTime = 0.0d;
    				}
    			}
    			
    			PrisonMines pMines = PrisonMines.getInstance();
    			Mine m = pMines.getMine(mineName);
    	        
    	        if ( !m.isEnabled() ) {
    	        	sender.sendMessage( "&cMine is disabled&7. Use &a/mines info &7for possible cause." );
    	        	return;
    	        }
    			
    			m.setZeroBlockResetDelaySec( resetTime );
    			
    			pMines.getMineManager().saveMine( m );
    			
    			DecimalFormat dFmt = new DecimalFormat("#,##0.00");
    			// User's message:
    			if ( m.isZeroBlockResetDisabled() ) {
    				Output.get().sendInfo( sender, "&7Mine &b%s Zero Block Reset Delay: &cDISABLED", 
    						m.getName(), dFmt.format( resetTime ) );
    				
    			} else {
    				Output.get().sendInfo( sender, "&7Mine &b%s Zero Block Reset Delay: &b%s &7sec", 
    						m.getName(), dFmt.format( resetTime ) );
    				
    			}
    			
    			// Server Log message:
    			Player player = getPlayer( sender );
    			Output.get().logInfo( "&7Mine &b%s Zero Block Reset Delay: &b%s &7set it to &b%s &7sec",
    					(player == null ? "console" : player.getDisplayName()), 
    					m.getName(), dFmt.format( resetTime )  );
    		}
    		catch ( NumberFormatException e ) {
    			Output.get().sendWarn( sender, 
    					"&7Invalid zeroBlockResetDelay value for &b%s&7. Must be an double value of &b0.00 &7or " +
    					"greater. [&b%s&7]",
    					mineName, time );
    		}
    	} 
    }
    

    @Command(identifier = "mines notification", permissions = "mines.notification", 
    		description = "Set a mine's notification mode.")
    public void setNotificationCommand(CommandSender sender,
        @Arg(name = "mineName", description = "The name of the mine to edit.") String mineName,
        @Arg(name = "mode", def="displayOptions", description = "The notification mode to use: disabled, within, radius") 
    					String mode,
        @Arg(name = "radius", def="0", description = "The distance from the center of the mine to notify players of a reset." ) 
    					String radius
        
    		) {
        
        if (performCheckMineExists(sender, mineName)) {
        	setLastMineReferenced(mineName);

        	PrisonMines pMines = PrisonMines.getInstance();
        	Mine m = pMines.getMine(mineName);
            
            if ( !m.isEnabled() ) {
            	sender.sendMessage( "&cMine is disabled&7. Use &a/mines info &7for possible cause." );
            	return;
            }
        	
        	MineNotificationMode noteMode = MineNotificationMode.fromString( mode, MineNotificationMode.displayOptions );
        	
        	if ( noteMode == MineNotificationMode.displayOptions ) {
        		sender.sendMessage( "&7Select a Mode: &bdisabled&7, &bwithin &7the mine, &bradius " +
        				"&7from center of mine." );
        	} else {
        		long noteRadius = 0L;
        		if ( noteMode == MineNotificationMode.radius ) {
        			if ( radius == null || radius.trim().length() == 0 ) {
        				noteRadius = MineData.MINE_RESET__BROADCAST_RADIUS_BLOCKS;
        			} else {
        				try {
        					noteRadius = Long.parseLong( radius );
        					
        					if ( noteRadius < 1 ) {
        						noteRadius = MineData.MINE_RESET__BROADCAST_RADIUS_BLOCKS;
        						DecimalFormat dFmt = new DecimalFormat("#,##0");
        						Output.get().sendWarn( sender, "&7Invalid radius value for &b%s&7. " +
            							"Must be an positive non-zero integer. Using the default value: &b%s &7[&b%s&7]",
            							mineName, dFmt.format(MineData.MINE_RESET__BROADCAST_RADIUS_BLOCKS), radius );
        					}
        				}
        				catch ( NumberFormatException e ) {
        					e.printStackTrace();
        					Output.get().sendWarn( sender, "&7Invalid notification radius for &b%s&7. " +
        							"Must be an positive non-zero integer. [&b%s&7]",
        							mineName, radius );
        				}
        			}
        		}
        		
        		if ( m.getNotificationMode() != noteMode || m.getNotificationRadius() != noteRadius ) {
        			m.setNotificationMode( noteMode );
        			m.setNotificationRadius( noteRadius );
        			
        			pMines.getMineManager().saveMine( m );
        			
        			DecimalFormat dFmt = new DecimalFormat("#,##0");
        			// message: notification mode changed
        			Output.get().sendInfo( sender, "&7Notification mode was changed for &b%s&7: &b%s %s",
        					mineName, m.getNotificationMode().name(), 
        					(m.getNotificationMode() == MineNotificationMode.radius ? 
        							dFmt.format( m.getNotificationRadius() ) : "" ));
        			
        		} else {
        			// message: notification mode did not change
        			Output.get().sendInfo( sender, "&7Notification mode was not changed for mine &b%s&7.", mineName );
        		}
        	}
        } 
    }


    @Command(identifier = "mines set area", permissions = "mines.set", 
    				description = "Set the area of a mine to your current selection.")
    public void redefineCommand(CommandSender sender,
        @Arg(name = "mineName", description = "The name of the mine to edit.") String mineName) {
    	
    	if (!performCheckMineExists(sender, mineName)) {
    		return;
    	}

        PrisonMines pMines = PrisonMines.getInstance();
        Mine m = pMines.getMine(mineName);
        
        if ( !m.isEnabled() ) {
        	sender.sendMessage( "&cMine is disabled&7. Use &a/mines info &7for possible cause." );
        	return;
        }
        
        Selection selection = Prison.get().getSelectionManager().getSelection((Player) sender);
        
        if (!selection.isComplete()) {
        	pMines.getMinesMessages().getLocalizable("select_bounds")
                .sendTo(sender);
            return;
        }

        if (!Objects.equals(selection.getMin().getWorld().getName(),
            selection.getMax().getWorld().getName())) {
            PrisonMines.getInstance().getMinesMessages().getLocalizable("world_diff")
                .sendTo(sender);
            return;
        }

        // TODO check to see if they are the same boundaries, if not, don't change...
        
        setLastMineReferenced(mineName);
        
        m.setBounds(selection.asBounds());
        pMines.getMineManager().saveMine( m );
        
        pMines.getMinesMessages().getLocalizable("mine_redefined")
            .sendTo(sender);
        
        // Delete the selection:
        Prison.get().getSelectionManager().clearSelection((Player) sender);
        //pMines.getMineManager().clearCache();
    }

    

    @Command(identifier = "mines tp", permissions = "mines.tp", description = "TP to the mine.")
    public void mineTp(CommandSender sender,
        @Arg(name = "mineName", description = "The name of the mine to teleport to.") String mineName) {
    	
    	if (!performCheckMineExists(sender, mineName)) {
    		return;
    	}

    	setLastMineReferenced(mineName);

    	PrisonMines pMines = PrisonMines.getInstance();
    	Mine m = pMines.getMine(mineName);
        
        if ( !m.isEnabled() ) {
        	sender.sendMessage( "&cMine is disabled&7. Use &a/mines info &7for possible cause." );
        	return;
        }
        
    	if ( sender instanceof Player ) {
    		m.teleportPlayerOut( (Player) sender );
    	} else {
    		sender.sendMessage(
    	            "&3Telport failed. Are you sure you're a Player?");
    	}
    }

    
    
    @Command(identifier = "mines stats", permissions = "mines.stats", description = "Toggle stats on all mines.")
    public void mineStats(CommandSender sender) {
    	
    	PrisonMines pMines = PrisonMines.getInstance();
    	MineManager mMan = pMines.getMineManager();
    	
    	// toggle the stats:
    	mMan.setMineStats( !mMan.isMineStats() );
    	
    	if ( mMan.isMineStats() ) {
    		sender.sendMessage(
    				"&3Mine Stats are now enabled. Use &7/mines list&3 to view stats on last mine reset. ");
    	} else {
    		sender.sendMessage( "&3Mine stats are now disabled." );
    	}
    }
   
    
    
    @Command(identifier = "mines whereami", permissions = "mines.whereami", 
    				description = "Identifies what mines you are in, or are the closest to." )
    public void mineWhereAmI(CommandSender sender) {
    	
    	Player player = getPlayer( sender );
    	
    	if (player == null || !player.isOnline()) {
    		sender.sendMessage( "&3You must be a player in the game to run this command." );
    		return;
    	}
    	
    	player.sendMessage( "&3Your coordinates are: &7" + player.getLocation().toBlockCoordinates() );

    	PrisonMines pMines = PrisonMines.getInstance();

    	List<Mine> inMine = new ArrayList<>();
    	TreeMap<Integer, Mine> nearMine = new TreeMap<>();
    	for ( Mine mine : pMines.getMineManager().getMines() ) {
    		if ( mine.getBounds().within( player.getLocation() ) ) {
    			inMine.add( mine );
    		}
    		
    		// This is checking for within a certain distance from any mine, so we just need to use
    		// some arbitrary distance as a max radius.  We do not want to use the individual values
    		// that have been set for each mine.
    		else if ( mine.getBounds().within( player.getLocation(), MineData.MINE_RESET__BROADCAST_RADIUS_BLOCKS) ) {
    			Double distance = mine.getBounds().getDistance3d( player.getLocation() );
//    			Double distance = new Bounds( mine.getBounds().getCenter(), player.getLocation()).getDistance();
    			nearMine.put( distance.intValue(), mine );
    		}
    	}
    	
    	if ( inMine.size() > 0 ) {
    		// You are in the mines:
    		for ( Mine m : inMine ) {
    			sender.sendMessage( "&3You are in mine &7" + m.getName() );
    		}
    	}
    	if ( nearMine.size() > 0 ) {
    		// You are near the mines:
    		int cnt = 0;
    		Set<Integer> distances = nearMine.keySet();
    		for ( Integer dist : distances ) {
				Mine m = nearMine.get( dist );
				sender.sendMessage( "&3You are &7" + dist + " &7blocks away from the center of mine &3" + m.getName() );
				if ( ++cnt >= 5 ) {
					break;
				}
			}
    		
    	} else {
    		// you are not near any mines:
    		sender.sendMessage( "&3Sorry, you are not within " + MineData.MINE_RESET__BROADCAST_RADIUS_BLOCKS + 
    				" blocks from any mine." );
    	}

    }

	private Player getPlayer( CommandSender sender ) {
		Optional<Player> player = Prison.get().getPlatform().getPlayer( sender.getName() );
		return player.isPresent() ? player.get() : null;
	}
    

    
    @Command(identifier = "mines wand", permissions = "mines.wand", description = "Receive a wand to select a mine area.")
    public void wandCommand(Player sender) {
        Prison.get().getSelectionManager().bestowSelectionTool(sender);
        sender.sendMessage(
            "&3Here you go! &7Left click to select the first corner, and right click to select the other.");
    }


    
	public Long getConfirmTimestamp()
	{
		return confirmTimestamp;
	}
	public void setConfirmTimestamp( Long confirmTimestamp )
	{
		this.confirmTimestamp = confirmTimestamp;
	}

	/**
	 * <p>This function will return the last mine reference to be used to fill in the
	 * <code>&lt;mine&gt;</code> reference within these commands.  After 30 minutes of 
	 * the last reference, this value will be reset to null and this function will then
	 * return the default mine place holder of <code>&lt;mine&gt;</code>.
	 * </p>
	 * 
	 * @return last mine referenced, or <code>&lt;mine&gt;</code>
	 */
	public String getLastMineReferenced()
	{
		if ( getLastMineReferencedTimestamp() != null &&
				System.currentTimeMillis() - getLastMineReferencedTimestamp() > (30 * 60 * 1000))
		{
			setLastMineReferenced( null );
		}
		return (lastMineReferenced == null ? "<mine>" : lastMineReferenced);
	}
	public void setLastMineReferenced( String lastMineReferenced )
	{
		lastMineReferenced( System.currentTimeMillis() );
		this.lastMineReferenced = lastMineReferenced;
	}

	public Long getLastMineReferencedTimestamp()
	{
		return lastMineReferencedTimestamp;
	}
	public void lastMineReferenced( Long lastMineReferencedTimestamp )
	{
		this.lastMineReferencedTimestamp = lastMineReferencedTimestamp;
	}

	
}
