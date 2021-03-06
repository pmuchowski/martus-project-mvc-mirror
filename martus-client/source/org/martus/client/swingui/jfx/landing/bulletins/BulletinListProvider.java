/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.client.swingui.jfx.landing.bulletins;

import java.util.Set;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.bulletinstore.FolderContentsListener;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.data.ArrayObservableList;
import org.martus.client.swingui.jfx.landing.FolderSelectionListener;
import org.martus.client.swingui.jfx.landing.cases.FxCaseManagementController;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

public class BulletinListProvider extends ArrayObservableList<BulletinTableRowData> implements FolderSelectionListener
{
	public BulletinListProvider(UiMainWindow mainWindowToUse)
	{
		super(INITIAL_CAPACITY);
		mainWindow = mainWindowToUse;
		trashFolderBeingDisplayedProperty = new SimpleBooleanProperty();
		allFolderBeingDisplayedProperty = new SimpleBooleanProperty();
		searchFolderBeingDisplayedProperty = new SimpleBooleanProperty();

		bulletinFolderContentChangedHandler = new BulletinFolderContentChangedHandler();

		bulletinListMonitor = new Object();
	}

	@Override
	public void folderWasSelected(BulletinFolder newFolder)
	{
		if (wasFolderChanged(newFolder))
			setFolder(newFolder);
	}

	private boolean wasFolderChanged(BulletinFolder newFolder)
	{
		if (isAllFolder(newFolder) && getAllFolderBeingDisplayedBooleanProperty().get())
			return false;

		if (isAllFolder(newFolder) || isAllFolder(folder))
			return true;

		return !newFolder.getName().equals(folder.getName());
	}

	private boolean isAllFolder(BulletinFolder folder)
	{
		return folder == FxCaseManagementController.ALL_FOLDER;
	}

	public synchronized void setFolder(BulletinFolder newFolder)
	{
		if(folder != null)
			folder.removeFolderContentsListener(bulletinFolderContentChangedHandler);
		folder = newFolder;
		boolean isTrashBeingDisplayed = false;
		boolean isAllBeingDisplayed = false;
		boolean isSearchBeingDisplayed = false;
		if(isAllFolder(folder))
		{
			isAllBeingDisplayed = true;
		}
		else
		{
			folder.addFolderContentsListener(bulletinFolderContentChangedHandler);
			isTrashBeingDisplayed = folder.isDiscardedFolder();
		}
		
		allFolderBeingDisplayedProperty.set(isAllBeingDisplayed);
		trashFolderBeingDisplayedProperty.set(isTrashBeingDisplayed);
		searchFolderBeingDisplayedProperty.set(isSearchBeingDisplayed);
		updateContents();
	}
	
	public void showingSearchResults()
	{
		searchFolderBeingDisplayedProperty.set(true);
	}

	
	public BulletinFolder getFolder()
	{
		return folder;
	}
	
	public BooleanProperty getTrashFolderBeingDisplayedBooleanProperty()
	{
		return trashFolderBeingDisplayedProperty;
	}
	
	public BooleanProperty getAllFolderBeingDisplayedBooleanProperty()
	{
		return allFolderBeingDisplayedProperty;
	}
	
	public BooleanProperty getSearchResultsFolderBeingDisplayedBooleanProperty()
	{
		return searchFolderBeingDisplayedProperty;
	}

	public void updateContents()
	{
		Platform.runLater(() -> loadBulletinsInBackground(new LoadBulletinsTask(loadBulletinsThread)));
	}

	private synchronized Set getUniversalIds()
	{
		if(isAllFolder(folder))
			return getAllBulletinUidsIncludingDiscardedItems();

		return folder.getAllUniversalIdsUnsorted();
	}

	public Set getAllBulletinUidsIncludingDiscardedItems()
	{
		Set allBulletinUids = getBulletinStore().getAllBulletinLeafUids();
		return allBulletinUids;
	}

	protected class BulletinFolderContentChangedHandler implements FolderContentsListener
	{
		@Override
		public void folderWasRenamed(String newName)
		{
		}

		@Override
		public void bulletinWasAdded(UniversalId added)
		{
			startBackgroundTask(new AddBulletinTask(added, loadBulletinsTask, loadBulletinsThread));
		}

		@Override
		public void bulletinWasRemoved(UniversalId removed)
		{
			startBackgroundTask(new RemoveBulletinTask(removed, loadBulletinsTask, loadBulletinsThread));
		}

		@Override
		public void folderWasSorted()
		{
			updateContents();
		}
	}

	void updateAllItemsInCurrentFolder()
	{
		setFolder(folder);
	}

	protected void loadAllBulletinsSelectInitialCaseFolder()
	{
		setFolder(FxCaseManagementController.ALL_FOLDER);
	}

	public void loadBulletinData(Set bulletinUids)
	{
		loadBulletinsInBackground(new LoadBulletinsWithIdsTask(loadBulletinsThread, bulletinUids));
	}

	private synchronized void loadBulletinsInBackground(LoadBulletinsTask task)
	{
		if (loadBulletinsTask != null)
		{
			loadBulletinsTask.cancelTask();
		}

		loadBulletinsTask = task;
		loadBulletinsThread = new Thread(loadBulletinsTask);
		loadBulletinsThread.setDaemon(false);
		loadBulletinsThread.start();
	}

	private void startBackgroundTask(Runnable task)
	{
		Thread thread = new Thread(task);
		thread.start();
	}

	class LoadBulletinsTask extends Task<Void>
	{
		private Thread previousLoadThread;
		private boolean taskCancelled;

		public LoadBulletinsTask(Thread previousLoadThreadToUse)
		{
			previousLoadThread = previousLoadThreadToUse;
			taskCancelled = false;
		}

		@Override
		protected Void call() throws Exception
		{
			waitForPreviousSearch();

			loadBulletinData(getUniversalIds());
			return null;
		}

		protected void waitForPreviousSearch() throws Exception
		{
			if (previousLoadThread != null && previousLoadThread.isAlive())
				previousLoadThread.join();
		}

		public void cancelTask()
		{
			taskCancelled = true;
		}

		protected boolean isTaskCancelled()
		{
			return taskCancelled;
		}

		protected void loadBulletinData(Set bulletinUids)
		{
			if (isTaskCancelled())
				return;

			// FIXME: To avoid the bulletin list flickering,
			// we should just add or remove as needed, instead of
			// clearing and re-populating from scratch
			clear();
			try
			{
				for (Object bulletinUid : bulletinUids)
				{
					if (isTaskCancelled())
						return;

					UniversalId leafBulletinUid = (UniversalId) bulletinUid;
					addBulletin(leafBulletinUid);
				}
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}

			sortTableByMostRecentBulletins();
		}
	}

	class LoadBulletinsWithIdsTask extends LoadBulletinsTask
	{
		private Set bulletinUids;

		public LoadBulletinsWithIdsTask(Thread previousSearch, Set bulletinUids)
		{
			super(previousSearch);
			this.bulletinUids = bulletinUids;
		}

		@Override
		protected Void call() throws Exception
		{
			if (isTaskCancelled())
				return null;

			waitForPreviousSearch();

			loadBulletinData(bulletinUids);
			return null;
		}
	}

	abstract class ChangeBulletinTask implements Runnable
	{
		public ChangeBulletinTask(UniversalId bulletinIdToUse, LoadBulletinsTask loadTaskToUse, Thread loadThreadToUse)
		{
			bulletinId = bulletinIdToUse;
			loadTask = loadTaskToUse;
			loadThread = loadThreadToUse;
		}

		@Override
		public void run()
		{
			try
			{
				if (continueAfterLoadFinished())
				{
					changeBulletin();
				}
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
		}

		abstract void changeBulletin() throws Exception;

		public UniversalId getBulletinId()
		{
			return bulletinId;
		}

		private boolean continueAfterLoadFinished() throws Exception
		{
			if (loadThread != null && loadThread.isAlive())
				loadThread.join();

			return loadTask == null || !loadTask.isTaskCancelled();
		}

		private UniversalId bulletinId;
		private LoadBulletinsTask loadTask;
		private Thread loadThread;
	}

	class AddBulletinTask extends ChangeBulletinTask
	{
		public AddBulletinTask(UniversalId bulletinIdToUse, LoadBulletinsTask loadTaskToUse, Thread loadThreadToUse)
		{
			super(bulletinIdToUse, loadTaskToUse, loadThreadToUse);
		}

		@Override
		void changeBulletin() throws Exception
		{
			addBulletinIfNotExist(getBulletinId());
			sortTableByMostRecentBulletins();
		}
	}

	class RemoveBulletinTask extends ChangeBulletinTask
	{
		public RemoveBulletinTask(UniversalId bulletinIdToUse, LoadBulletinsTask loadTaskToUse, Thread loadThreadToUse)
		{
			super(bulletinIdToUse, loadTaskToUse, loadThreadToUse);
		}

		@Override
		void changeBulletin() throws Exception
		{
			removeBulletin(getBulletinId());
			refreshView();
		}
	}

	class UpdateBulletinTask extends ChangeBulletinTask
	{
		public UpdateBulletinTask(UniversalId bulletinIdToUse, LoadBulletinsTask loadTaskToUse, Thread loadThreadToUse)
		{
			super(bulletinIdToUse, loadTaskToUse, loadThreadToUse);
		}

		@Override
		void changeBulletin() throws Exception
		{
			updateBulletin(getBulletinId());
			sortTableByMostRecentBulletins();
		}
	}

	protected BulletinTableRowData getCurrentBulletinData(UniversalId leafBulletinUid) throws Exception
	{
		ClientBulletinStore clientBulletinStore = getBulletinStore();
		Bulletin bulletin = getRevisedBulletin(leafBulletinUid);
		boolean onServer = clientBulletinStore.isProbablyOnServer(leafBulletinUid);
		Integer authorsValidation = getMainWindow().getApp().getKeyVerificationStatus(bulletin.getAccount());
		MiniLocalization localization = getMainWindow().getLocalization();
		BulletinTableRowData bulletinData = new BulletinTableRowData(bulletin, onServer, authorsValidation, localization);
		return bulletinData;
	}

	private Bulletin getRevisedBulletin(UniversalId leafBulletinUid)
	{
		return getBulletinStore().getBulletinRevision(leafBulletinUid);
	}

	public void removeBulletin(UniversalId removed)
	{
		synchronized (bulletinListMonitor)
		{
			int bulletinIndex = findBulletinIndexInTable(removed);
			if (bulletinIndex != BULLETIN_NOT_IN_TABLE)
				remove(bulletinIndex);
		}
	}

	public void addBulletin(UniversalId bulletinId) throws Exception
	{
		synchronized (bulletinListMonitor)
		{
			BulletinTableRowData bulletinData = getCurrentBulletinData(bulletinId);
			add(bulletinData);
		}
	}

	public void addBulletinIfNotExist(UniversalId bulletinId) throws Exception
	{
		synchronized (bulletinListMonitor)
		{
			int bulletinIndex = findBulletinIndexInTable(bulletinId);
			if (bulletinIndex == BULLETIN_NOT_IN_TABLE)
				addBulletin(bulletinId);
		}
	}

	@Override
	public void clear()
	{
		synchronized (bulletinListMonitor)
		{
			super.clear();
		}
	}

	protected int findBulletinIndexInTable(UniversalId uid)
	{
		for (int currentIndex = 0; currentIndex < size(); currentIndex++)
		{
			if(uid.equals(get(currentIndex).getUniversalId()))
				return currentIndex;
		}
		return BULLETIN_NOT_IN_TABLE;
	}

	public void updateBulletin(Bulletin bulletin) throws Exception
	{
		UniversalId bulletinId = bulletin.getUniversalId();

		if (folder == FxCaseManagementController.ALL_FOLDER || getUniversalIds().contains(bulletinId))
			startBackgroundTask(new UpdateBulletinTask(bulletinId, loadBulletinsTask, loadBulletinsThread));
	}

	public void updateBulletin(UniversalId bulletinId) throws Exception
	{
		synchronized (bulletinListMonitor)
		{
			int bulletinIndexInTable = findBulletinIndexInTable(bulletinId);

			if (bulletinIndexInTable == BULLETIN_NOT_IN_TABLE)
			{
				if (folder == FxCaseManagementController.ALL_FOLDER)
					addBulletin(bulletinId);
				return;
			}

			if (hasBulletinBeenDiscarded(bulletinId))
			{
				remove(bulletinIndexInTable);
				return;
			}

			BulletinTableRowData updatedBulletinData = getCurrentBulletinData(bulletinId);
			set(bulletinIndexInTable, updatedBulletinData);
		}
	}

	protected void refreshView()
	{
		if (refreshViewHandler != null)
			refreshViewHandler.refresh(bulletinListMonitor);
	}

	protected void sortTableByMostRecentBulletins()
	{
		if (refreshViewHandler != null)
			refreshViewHandler.sort(bulletinListMonitor);
	}

	private boolean hasBulletinBeenDiscarded(UniversalId bulletinId)
	{
		return getRevisedBulletin(bulletinId) == null;
	}
	
	private ClientBulletinStore getBulletinStore()
	{
		return getMainWindow().getStore();
	}

	private UiMainWindow getMainWindow()
	{
		return mainWindow;
	}

	public void setRefreshViewHandler(RefreshViewHandler refreshViewHandler)
	{
		this.refreshViewHandler = refreshViewHandler;
	}

	private static final int BULLETIN_NOT_IN_TABLE = -1;
	private static final int INITIAL_CAPACITY = 1000;

	private LoadBulletinsTask loadBulletinsTask;
	private Thread loadBulletinsThread;
	private RefreshViewHandler refreshViewHandler;

	private final BulletinFolderContentChangedHandler bulletinFolderContentChangedHandler;

	private UiMainWindow mainWindow;
	private BulletinFolder folder;
	private BooleanProperty trashFolderBeingDisplayedProperty;
	private BooleanProperty allFolderBeingDisplayedProperty;
	private BooleanProperty searchFolderBeingDisplayedProperty;

	private final Object bulletinListMonitor;
}
