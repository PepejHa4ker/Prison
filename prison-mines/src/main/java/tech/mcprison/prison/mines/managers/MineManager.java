/*
 *  Copyright (C) 2017 The Prison Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.mcprison.prison.mines.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.TimerTask;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.internal.Player;
import tech.mcprison.prison.localization.Localizable;
import tech.mcprison.prison.mines.MineException;
import tech.mcprison.prison.mines.PrisonMines;
import tech.mcprison.prison.mines.data.Block;
import tech.mcprison.prison.mines.data.Mine;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.store.Collection;
import tech.mcprison.prison.store.Document;
import tech.mcprison.prison.util.BlockType;
import tech.mcprison.prison.util.Text;

/**
 * Manages the creation, removal, and management of mines.
 *
 * @author Dylan M. Perks
 */
public class MineManager {

    // Base list
    List<Mine> mines;

    tech.mcprison.prison.store.Collection coll;

    // Declarations
    HashMap<String, List<BlockType>> randomizedBlocks;
    int resetCount = 0;

    /**
     * Initializes a new instance of {@link MineManager}
     */
    public MineManager(tech.mcprison.prison.store.Collection collection) {
        mines = new ArrayList<>();
        randomizedBlocks = new HashMap<>();
        coll = collection;
        mines = new ArrayList<>();

        loadMines();

        Output.get().logInfo("Loaded " + mines.size() + " mines");
        resetCount = PrisonMines.getInstance().getConfig().resetTime;
    }

    public void loadMine(String mineFile) throws IOException, MineException {
        Document document = coll.get(mineFile).orElseThrow(IOException::new);
        mines.add(new Mine(document));
    }

    /**
     * Adds a {@link Mine} to this {@link MineManager} instance.
     * 
     * Also saves the mine to the file system.
     *
     * @param mine the mine instance
     * @return if the add was successful
     */
    public boolean add(Mine mine) {
    	return add(mine, true);
    }
    
    /**
     * Adds a {@link Mine} to this {@link MineManager} instance.
     * 
     * Also saves the mine to the file system.
     *
     * @param mine the mine instance
     * @param save - bypass the option to save. Useful for when initially loading the mines since
     *               no data has changed.
     * @return if the add was successful
     */
    public boolean add(Mine mine, boolean save) {
    	boolean results = false;
        if (!mines.contains(mine)){
        	if ( save ) {
        		saveMine( mine );
        	}
        	
            results = mines.add(mine);
        }
        return results;
    }

    private void selectiveSend(Player x, Localizable localizable) {
        if (PrisonMines.getInstance().getWorlds()
            .contains(x.getLocation().getWorld().getName().toLowerCase())) {
            localizable.sendTo(x);
        }
    }

    /**
     * Gets the {@link TimerTask} for the reset timer of this {@link MineManager}
     */
    public TimerTask getTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                // Perform initial checks
                if (PrisonMines.getInstance().getConfig().resetTime == 0) {
                    return;
                }
                if (mines.size() == 0) {
                    return;
                }

                // It's time to reset
                if (resetCount == 0) {
                    resetMines();
                } else {
                    broadcastResetWarnings();
                }

                if (resetCount > 0) {
                    resetCount--;
                }
            }
        };
    }

    private void resetMines() {
        mines.forEach(Mine::reset);

        if (PrisonMines.getInstance().getConfig().resetMessages) {
            // Send it to everyone if it's not multi-world
            if (!PrisonMines.getInstance().getConfig().multiworld) {
                Prison.get().getPlatform().getOnlinePlayers().forEach(
                    x -> PrisonMines.getInstance().getMinesMessages()
                        .getLocalizable("reset_message")
                        .sendTo(x));
            } else { // Or those affected if it's multi-world
                Prison.get().getPlatform().getOnlinePlayers().forEach(x -> selectiveSend(x,
                    PrisonMines.getInstance().getMinesMessages().getLocalizable("reset_message")));
            }
        }

        // And reset the count
        resetCount = PrisonMines.getInstance().getConfig().resetTime;
    }

    private void broadcastResetWarnings() {
        if (!PrisonMines.getInstance().getConfig().resetMessages) {
            return;
        }

        for (int i : PrisonMines.getInstance().getConfig().resetWarningTimes) {
            if (resetCount == i) {
                if (!PrisonMines.getInstance().getConfig().multiworld) {

                    Prison.get().getPlatform().getOnlinePlayers().forEach(
                        x -> PrisonMines.getInstance().getMinesMessages()
                            .getLocalizable("reset_warning")
                            .withReplacements(Text.getTimeUntilString(resetCount * 1000))
                            .sendTo(x));
                } else {
                    Prison.get().getPlatform().getOnlinePlayers().forEach(x -> selectiveSend(x,
                        PrisonMines.getInstance().getMinesMessages().getLocalizable("reset_warning")
                            .withReplacements(Text.getTimeUntilString(resetCount * 1000))));
                }
            }
        }
    }

    public boolean removeMine(String id){
        if (getMine(id).isPresent()) {
            return removeMine(getMine(id).get());
        }
        else{
            return false;
        }
    }

    public boolean removeMine(Mine mine) {
    	boolean success = false;
    	if ( mine != null ) {
    		mines.remove(mine);
    		success = coll.delete( mine.getName() );
    	}
	    return success;
    }

    public static MineManager fromDb() {
    	PrisonMines pMines = PrisonMines.getInstance();
    	
        Optional<Collection> collOptional = pMines.getDb().getCollection("mines");

        if (!collOptional.isPresent()) {
        	Output.get().logError("Could not create 'mines' collection.");
        	pMines.getStatus().toFailed("Could not create mines collection in storage.");
        	return null;
        }

        return new MineManager(collOptional.get());
    }

    private void loadMines() {
        List<Document> mineDocuments = coll.getAll();

        for (Document document : mineDocuments) {
            try {
                Mine m = new Mine(document);
                add(m, false);
                if (PrisonMines.getInstance().getConfig().asyncReset) {
                    generateBlockList(m);
                }
            } catch (Exception e) {
                Output.get()
                    .logError("&cFailed to load mine " + document.getOrDefault("name", "null"), e);
            }
        }
    }

    /**
     * Saves the specified mine. This should only be used for the instance created by {@link
     * PrisonMines}
     */
    public void saveMine(Mine mine) {
        coll.save(mine.toDocument());
    }

    public void saveMines(){
        for (Mine m : mines){
            saveMine(m);
        }
    }

    /**
     * Generates blocks for the specified mine and caches the result.
     * 
     * The random chance is now calculated upon a double instead of integer.
     *
     * @param mine the mine to randomize
     */
    public void generateBlockList(Mine mine) {
        Random random = new Random();
        ArrayList<BlockType> blocks = new ArrayList<>();

        for (int i = 0; i < mine.getBounds().getTotalBlockCount(); i++) {
        	double chance = random.nextDouble() * 100.0d;
            
            BlockType value = BlockType.AIR;
            for (Block block : mine.getBlocks()) {
                if (chance <= block.getChance()) {
                    value = block.getType();
                    break;
                } else {
                    chance -= block.getChance();
                }
            }
            blocks.add(value);
        }
        randomizedBlocks.put(mine.getName(), blocks);
    }

    /**
     * Gets the randomized blocks cache
     *
     * @return a hashmap with all the randomized blocks
     */
    public HashMap<String, List<BlockType>> getRandomizedBlocks() {
        return randomizedBlocks;
    }

    /**
     * Returns the mine with the specified name.
     *
     * @param name The mine's name, case-sensitive.
     * @return An optional containing either the {@link Mine} if it could be found, or empty if it
     * does not exist by the specified name.
     */
    public Optional<Mine> getMine(String name) {
        return mines.stream().filter(mine -> mine.getName().equals(name)).findFirst();
    }

    /**
     * Clears all of the cached randomized blocks
     */
    public void clearCache() {
        randomizedBlocks.clear();
    }

    public List<Mine> getMines() {
        return mines;
    }

}
