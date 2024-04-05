package tech.mcprison.prison.mines.tasks;

import java.util.ArrayList;
import java.util.List;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.internal.PrisonStatsElapsedTimeNanos;
import tech.mcprison.prison.internal.block.MineResetType;
import tech.mcprison.prison.internal.block.MineTargetPrisonBlock;
import tech.mcprison.prison.mines.PrisonMines;
import tech.mcprison.prison.mines.data.Mine;
import tech.mcprison.prison.mines.data.MineScheduler.MineResetActions;
import tech.mcprison.prison.mines.data.MineScheduler.MineResetScheduleType;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.tasks.PrisonRunnable;
import tech.mcprison.prison.tasks.PrisonTaskSubmitter;

public class MinePagedResetAsyncTask
		implements PrisonRunnable
{
	private Mine mine;
	private final MineResetType resetType;
	private final MineResetScheduleType resetScheduleType;
	
	private int position = 0;
	
	private int page = 0;
	
	private int totalPages = 0;
	private int pagesPerReport = 20;
	private int pagePosition = 0;
	

	private long timeStart = 0;
	private long timePage = 0;
	
	private PrisonStatsElapsedTimeNanos nanos;
	
	
	// Config Settings:
	private int configAsyncResetPageSize = -1;
	private int configSyncSubPageSlice = -1;
	
	
	private List<MineResetActions> resetActions;
	
	
	
	public MinePagedResetAsyncTask( Mine mine, MineResetType resetType, 
					List<MineResetActions> resetActions,
					MineResetScheduleType resetScheduleType ) {
		super();
		
		this.mine = mine;
		this.resetType = resetType;
		this.resetScheduleType = resetScheduleType;
		
		
		this.timeStart = System.currentTimeMillis();
		this.timePage = timeStart;
		
		this.nanos = new PrisonStatsElapsedTimeNanos();
		
		this.totalPages = (mine.getMineTargetPrisonBlocks().size() / 
												getConfigAsyncResetPageSize()) + 1;
		
		this.resetActions = resetActions;
	}
	
	
	public MinePagedResetAsyncTask( Mine mine, MineResetType resetType ) {
		this( mine, resetType, null, MineResetScheduleType.NORMAL );
	}
	
	
//	public void submitTaskSync() {
//		submitTaskAsync();
//	}
	public void submitTaskAsync() {
		
		// Prevent the task from being submitted if it is a virtual mine:
		if ( mine.isVirtual() ) {
			return;
		}
		
		// Prevent the task from being submitted if it was already reset less than 5 seconds ago.
		if ( mine.getLastResetTimeLong() > 0 && 
				(System.currentTimeMillis() - mine.getLastResetTimeLong()) <= 5000 && 
				(  resetScheduleType != MineResetScheduleType.ZERO_BLOCK_RESET ||
						resetScheduleType == MineResetScheduleType.ZERO_BLOCK_RESET && 
				mine.getBounds().getTotalBlockCount() > 25
						) ) {
			
			if ( !mine.getMineStateMutex().isMinable() ) {
				
				// Canceling reset, so cancel mutex if it was blocking mining.
				mine.getMineStateMutex().setMineStateResetFinishedForced();
			}
			
			
			// cannot reset quicker than every 5 seconds:
			return;
		}

		mine.setLastResetTimeLong( System.currentTimeMillis() );
		
		
		submitTaskAsyncInternalNextPage();
	}

	private void submitTaskAsyncInternalNextPage() {
		
		if ( position > 0 && page++ % pagesPerReport == 0 ) {
			
			if ( PrisonMines.getInstance().getMineManager().isMineStats() ) {
				
				logStats();
			}
		}
		
		long delay = 0;
		PrisonTaskSubmitter.runTaskLaterAsync( this, delay );
	}


	private void logStats()
	{
		long timeCurrent = System.currentTimeMillis();
		long timeElapsedTotal = timeCurrent - timeStart;
		long timeElapsedPage = timeCurrent - timePage;
		timePage = timeCurrent;
		
		
		int blocksPlaced = position - pagePosition;
		pagePosition = position;
		
		
		mine.setResetPosition( position );
		
		mine.setResetPage( page );
		
		mine.setStatsBlockUpdateTimeNanos( nanos.getElapsedTimeNanos() );

		//		long time = System.currentTimeMillis() - start;
		mine.setStatsBlockUpdateTimeMS( timeElapsedPage + mine.getStatsBlockUpdateTimeMS() );
		mine.setStatsResetTimeMS( timeElapsedPage + mine.getStatsResetTimeMS() );
		
		
		mine.setStatsResetPages( page );
		mine.setStatsResetPageBlocks( mine.getStatsResetPageBlocks() + blocksPlaced );
		mine.setStatsResetPageMs( mine.getStatsResetPageMs() + timeElapsedPage  );
		
		
		// Only print these details if stats are enabled:
		if ( PrisonMines.getInstance().getMineManager().isMineStats() ) {
			
			Output.get().logInfo( "MinePagedResetAsyncTask : " +
					mine.getName() + " " +
					resetType.name() + 
					" : page " + page + " of " + totalPages + " : " +
					"  blocks = " + blocksPlaced + "  elapsed = " + timeElapsedPage + 
					" ms  TotalElapsed = " + timeElapsedTotal + " ms   " +
							"block update elapsed = " + 
					( getNanos().getElapsedTimeNanos() / 1000000d ) + " ms(nanos)"
//							"  TPS " +
//					Prison.get().getPrisonTPS().getAverageTPSFormatted()
					);
		}
	}
	
	@Override
	public void run() {
		
		// The first time running this, need to setup the block list if a reset:
		if ( position == 0 ) {
			if ( runSetupCancelAutoResets() ) {
				// If the reset should be canceled then just return, and that will 
				// terminate the reset.  There is nothing else that needs to be done.
				return;
			}
		}
		
		List<MineTargetPrisonBlock> targetBlocks = mine.getMineTargetPrisonBlocks();

		int pageEndPosition = position + getConfigAsyncResetPageSize();
				
		
		while ( position < pageEndPosition ) {
			
			int endIndex = position + getConfigSyncSubPageSlice();
			if ( endIndex > targetBlocks.size() ) {
				endIndex = targetBlocks.size();
				pageEndPosition = endIndex;
			}

			// Isolate the sub-list from the main targetBlocks list:
            List<MineTargetPrisonBlock> tBlocks = new ArrayList<>(targetBlocks.subList(position, endIndex));
			
			int size = tBlocks.size();
			position += size;
			
			mine.getWorld().get().setBlocksSynchronously( tBlocks, resetType, getNanos() );
			
		}
		
		
		// Keep resubmitting this task until it is completed:
		if ( position < targetBlocks.size() ) {
			submitTaskAsyncInternalNextPage();
		}
		else {
			
			// Finished running the task and let it end:
			runShutdown();
		}
		
	}

	
	/**
	 * <p>The primary purpose of this function is to cancel the auto resets
	 * and to prepare for a manual reset.
	 * </p>
	 * 
	 * <p>This is ran before the initial actual processing is performed.  
	 * This calls the resetAsynchonouslyInitiate function which raises the
	 * MineResetEvent.  If that event is not canceled then it will run the
	 * pre-reset commands within the function asynchronouslyResetSetup.
	 * </p>
	 */
	private boolean runSetupCancelAutoResets() {
		boolean cancel = false;
		
		// If it makes it this far and the mutex is not locked, then lock it:
		if ( mine.getMineStateMutex().isMinable() ) {
			
			// Set the MineStateMutex to a state of starting a mine reset:
			mine.getMineStateMutex().setMineStateResetStart();
		}
 
    	mine.generateBlockListAsync();
		
//		if ( resetType == MineResetType.normal ) {
//			
//			
//		}
		// resetAsynchonouslyInitiate() will confirm if the reset should happened 
		// and will raise Prison's mine reset event. 
		// A return value of true means cancel the reset:
		cancel = mine.resetAsynchonouslyInitiate( resetType );
		
		if ( !cancel ) {
			
			mine.asynchronouslyResetSetup();
		}
		else if ( !mine.getMineStateMutex().isMinable() ) {
			
			// Canceling reset, so cancel mutex if it was blocking mining.
			mine.getMineStateMutex().setMineStateResetFinishedForced();
		}
		
		return cancel;
	}
	
	private void runShutdown() {

		logStats();
		
		
		// Set the MineStateMutex to a state of Finishing a mine reset:
		// It is now safe to allow mining in the mine.
		mine.getMineStateMutex().setMineStateResetFinishedForced();

		
		// Run items such as post-mine-reset commands:
		mine.asynchronouslyResetFinalize( getResetActions() );
 

	}

	
	public PrisonStatsElapsedTimeNanos getNanos() {
		return nanos;
	}
	public void setNanos( PrisonStatsElapsedTimeNanos nanos ) {
		this.nanos = nanos;
	}

	public List<MineResetActions> getResetActions() {
		return resetActions;
	}
	public void setResetActions( List<MineResetActions> resetActions ) {
		this.resetActions = resetActions;
	}


	public int getConfigAsyncResetPageSize() {
		if ( configAsyncResetPageSize == -1 ) {
			this.configAsyncResetPageSize = 
					Long.valueOf( Prison.get().getPlatform()
							.getConfigLong( "prison-mines.reset-async-paging.async-page-size", 
									4000 )).intValue();
		}
		return configAsyncResetPageSize;
	}
	
	public int getConfigSyncSubPageSlice() {
		if ( configSyncSubPageSlice == -1 ) {
			this.configSyncSubPageSlice = 
					Long.valueOf( Prison.get().getPlatform()
							.getConfigLong( "prison-mines.reset-async-paging.sync-sub-page-slice", 
									200 )).intValue();
		}
		return configSyncSubPageSlice;
	}
}
