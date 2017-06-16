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
package org.martus.client.swingui.jfx.landing.general;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.bulletinstore.ClientBulletinStore.BulletinAlreadyExistsException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.landing.AbstractFxLandingContentController;
import org.martus.client.swingui.tablemodels.RetrieveHQDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveHQTableModel;
import org.martus.client.swingui.tablemodels.RetrieveMyDraftsTableModel;
import org.martus.client.swingui.tablemodels.RetrieveMyTableModel;
import org.martus.client.swingui.tablemodels.RetrieveTableModel;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;


public class ManageServerSyncRecordsController extends AbstractFxLandingContentController
{
	private static final double PROGRESS_VALUE_LOADING = -1.0;
	private static final double PROGRESS_VALUE_FINISHED = 1.0;
	private static final double PROGRESS_VALUE_FAILED = 0.0;

	public ManageServerSyncRecordsController(UiMainWindow mainWindowToUse) throws Exception
	{
		super(mainWindowToUse);
	}

	@Override
	public void initializeMainContentPane()
	{
		initalizeColumns();
		initalizeItemsTable();

		RecordSelectedListener recordSelectedListener = new RecordSelectedListener();
		allRecordsTable.getSelectionModel().selectedItemProperty().addListener(recordSelectedListener);
		updateButtons();
	}
	
	private void initalizeItemsTable()
	{
		allRecordsTable.setPlaceholder(createProgressPlaceholder());
		allRecordsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		syncRecordsTableProvider = new SyncRecordsTableProvider(getMainWindow());
		final SyncRecordsTableProvdiderTask task = new SyncRecordsTableProvdiderTask(syncRecordsTableProvider);
		createAndStartThread(task);
	}

	private BorderPane createProgressPlaceholder()
	{
		VBox progressBox = new VBox();
		progressBox.setAlignment(Pos.CENTER);
		BorderPane progressPane = new BorderPane();
		progressPane.setCenter(progressBox);		
		progressPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		recordsProgressBar = new ProgressBar();
		progressLabel = new Label();
		progressBox.getChildren().addAll(recordsProgressBar, progressLabel);
		
		return progressPane;
	}

	private void createAndSetNoRecordsPlaceholder()
	{
		Label noBulletins = new Label(getLocalization().getFieldLabel("NoBulletinsInTable"));
		allRecordsTable.setPlaceholder(noBulletins);
	}

	private void setNoRecordsPlaceholder()
	{
		Platform.runLater(() -> createAndSetNoRecordsPlaceholder());
	}

	protected class SyncRecordsTableProvdiderTask extends Task<SyncRecordsTableProvider>
	{
		private SyncRecordsTableProvider syncRecordsTableProvider;
		public SyncRecordsTableProvdiderTask(SyncRecordsTableProvider syncRecordsTableProvider)
		{
			this.syncRecordsTableProvider = syncRecordsTableProvider;
		}
		
		@Override
		protected void failed() 
		{ 
			updateStatusLabel(PROGRESS_VALUE_FAILED, "Failed to load data!");
		}

		@Override
		protected SyncRecordsTableProvider call() throws Exception
		{
			try
			{
				loadRecordsError = false;
				updateStatusLabel(PROGRESS_VALUE_LOADING, "Loading server records...");

				ServerMyDraftsTask myDraftsTask = new ServerMyDraftsTask();
				Thread myDraftsThread = createAndStartThread(myDraftsTask);

				ServerMySealedsTask mySealedsTask = new ServerMySealedsTask();
				Thread mySealedsThread = createAndStartThread(mySealedsTask);

				ServerHQDraftsTask hqDraftsTask = new ServerHQDraftsTask();
				Thread hqDraftsThread = createAndStartThread(hqDraftsTask);

				ServerHQSealedsTask hqSealedsTask = new ServerHQSealedsTask();
				Thread hqSealedsThread = createAndStartThread(hqSealedsTask);

				myDraftsThread.join();
				mySealedsThread.join();
				hqDraftsThread.join();
				hqSealedsThread.join();

				if (loadRecordsError)
					throw new Exception("Failed to load data");

				allRecordsTable.setItems(syncRecordsTableProvider);
				
				updateLocationLinks();
				updateSubFilterLinks();
				loadData();
				updateStatusLabel(PROGRESS_VALUE_FINISHED, "Finished");
				setNoRecordsPlaceholder();
			} 
			catch (Exception e)
			{
				updateStatusLabel(PROGRESS_VALUE_FAILED, "Failed to load data!");
				MartusLogger.logException(e);
			}
			
			return syncRecordsTableProvider;
		}
		
		@Override
		protected void succeeded()
		{
			super.succeeded();
			
			syncRecordsTableProvider = getValue();
			onShowAll(null);
		}
	}

	private Thread createAndStartThread(Task task)
	{
		Thread thread = new Thread(task);
		thread.setDaemon(false);
		thread.start();
		return thread;
	}

	protected abstract class LoadServerData extends Task<Vector>
	{
		@Override
		protected void failed()
		{
			loadRecordsError = true;

			if (getException() != null)
				MartusLogger.logException(new Exception("Could not load the bulletins", getException()));
		}
	}

	protected class ServerMyDraftsTask extends LoadServerData
	{

		@Override
		protected Vector call() throws Exception
		{
			return getServerMyDrafts();
		}

		@Override
		protected void succeeded()
		{
			super.succeeded();

			serverMyDrafts = getValue();
		}
	}

	protected class ServerMySealedsTask extends LoadServerData
	{

		@Override
		protected Vector call() throws Exception
		{
			return getServerMySealeds();
		}

		@Override
		protected void succeeded()
		{
			super.succeeded();

			serverMySealeds = getValue();
		}
	}

	protected class ServerHQDraftsTask extends LoadServerData
	{

		@Override
		protected Vector call() throws Exception
		{
			return getServerHQDrafts();
		}

		@Override
		protected void succeeded()
		{
			super.succeeded();

			serverHQDrafts = getValue();
		}
	}

	protected class ServerHQSealedsTask extends LoadServerData
	{

		@Override
		protected Vector call() throws Exception
		{
			return getServerHQSealeds();
		}

		@Override
		protected void succeeded()
		{
			super.succeeded();

			serverHQSealeds = getValue();
		}
	}

	protected void updateStatusLabel(double progressValue, String label)
	{
		Platform.runLater(new UpdateStatusProgressRunner(progressValue, label));
	}
	
	protected class UpdateStatusProgressRunner implements Runnable
	{
		private double progressValue;
		private String updateLabel;

		public UpdateStatusProgressRunner(double progressValueToUse, String updateLabelToUse)
		{
			progressValue = progressValueToUse;
			updateLabel = updateLabelToUse;
		}
		
		@Override
		public void run()
		{
			progressLabel.setText(updateLabel);
			recordsProgressBar.setProgress(progressValue);
		}
	}

	protected void loadData()
	{
		try
		{
			Set localRecords = getLocalRecords();
			syncRecordsTableProvider.addBulletinsAndSummaries(localRecords, serverMyDrafts, serverMySealeds, serverHQDrafts, serverHQSealeds);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	private Set getLocalRecords()
	{
		return getApp().getStore().getAllBulletinLeafUids();
	}

	protected Vector getServerMyDrafts() throws Exception
	{
		RetrieveTableModel model = new RetrieveMyDraftsTableModel(getApp(), getLocalization());
		model.initialize(null);
		return model.getAllSummaries();
	}

	protected Vector getServerMySealeds() throws Exception
	{
		RetrieveTableModel model = new RetrieveMyTableModel(getApp(), getLocalization());
		model.initialize(null);
		return model.getAllSummaries();
	}

	protected Vector getServerHQDrafts() throws Exception
	{
		RetrieveTableModel model = new RetrieveHQDraftsTableModel(getApp(), getLocalization());
		model.initialize(null);
		return model.getAllSummaries();
	}

	protected Vector getServerHQSealeds() throws Exception
	{
		RetrieveTableModel model = new RetrieveHQTableModel(getApp(), getLocalization());
		model.initialize(null);
		return model.getAllSummaries();
	}

	private void initalizeColumns()
	{
		recordLocationColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.LOCATION_PROPERTY_NAME));
		recordLocationColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		
		recordTitleColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.TITLE_PROPERTY_NAME));
		recordTitleColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		
		recordAuthorColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.AUTHOR_PROPERTY_NAME));
		recordAuthorColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		
		recordLastSavedColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, String>(ServerSyncTableRowData.DATE_SAVDED_PROPERTY_NAME));
		recordLastSavedColumn.setCellFactory(TextFieldTableCell.<ServerSyncTableRowData>forTableColumn());
		
		recordSizeColumn.setCellValueFactory(new PropertyValueFactory<ServerSyncTableRowData, Integer>(ServerSyncTableRowData.SIZE_PROPERTY_NAME));
		recordSizeColumn.setCellFactory(new RecordSizeColumnHandler());
	}

	private class RecordSelectedListener implements ChangeListener<ServerSyncTableRowData>
	{
		public RecordSelectedListener()
		{
		}

		@Override
		public void changed(ObservableValue<? extends ServerSyncTableRowData> observalue	,
				ServerSyncTableRowData previousRecord, ServerSyncTableRowData newRecord)
		{
			updateButtons();
		}
	}

	protected void updateButtons()
	{
		ObservableList<ServerSyncTableRowData> rowsSelected = allRecordsTable.getSelectionModel().getSelectedItems();
		boolean isAnythingDeleteable = false;
		boolean isAnythingUploadable = false;
		boolean isAnythingDownloadable = false;
		for (Iterator iterator = rowsSelected.iterator(); iterator.hasNext();)
		{
			ServerSyncTableRowData data = (ServerSyncTableRowData) iterator.next();
			if(data == null)
				return; 
			if(data.canDeleteFromServerProperty().getValue())
				isAnythingDeleteable = true;
			if(data.canUploadToServerProperty().getValue())
				isAnythingUploadable = true;
			if(data.isRemote().getValue())
				isAnythingDownloadable = true;
		}
		deleteButton.setDisable(!isAnythingDeleteable);
		uploadButton.setDisable(!isAnythingUploadable);
		downloadButton.setDisable(!isAnythingDownloadable);
		
		updateLocationLinks();
		updateSubFilterLinks();
	}

	protected void updateLocationLinks()
	{
		int location = syncRecordsTableProvider.getLocation();
		fxLocationAll.setVisited(location == ServerSyncTableRowData.LOCATION_ANY);
		fxLocationLocalOnly.setVisited(location == ServerSyncTableRowData.LOCATION_LOCAL);
		fxLocationServerOnly.setVisited(location == ServerSyncTableRowData.LOCATION_SERVER);
		fxLocationBoth.setVisited(location == ServerSyncTableRowData.LOCATION_BOTH);
	}

	protected void updateSubFilterLinks()
	{
		int subFilter = syncRecordsTableProvider.getSubFilter();
		fxSubFilterAll.setVisited(subFilter == SyncRecordsTableProvider.SUB_FILTER_ALL);
		fxSubFilterMyRecords.setVisited(subFilter == SyncRecordsTableProvider.SUB_FILTER_MY_RECORDS);
		fxSubFilterSharedWithMe.setVisited(subFilter == SyncRecordsTableProvider.SUB_FILTER_SHARED_WITH_ME);
	}

	private void closeDialog()
	{
		getStage().close();
	}

	private void DisplayWarningDialog( String warningTag, StringBuilder titlesInQuestion)
	{
		showNotifyDialog(warningTag, titlesInQuestion.toString());
	}
	
	private void updateTable(int TableToShow)
	{
		syncRecordsTableProvider.setSubFilter(SyncRecordsTableProvider.SUB_FILTER_ALL);
		syncRecordsTableProvider.show(TableToShow);
		updateButtons();
	}
	
	private void filterTable(int filter)
	{
		syncRecordsTableProvider.setSubFilter(filter);
		syncRecordsTableProvider.filterResults();
		updateButtons();
	}
		
	@Override
	public String getFxmlLocation()
	{
		return "landing/general/ManageServerSyncRecords.fxml";
	}
	
	public void addToInvalidRecords(StringBuilder serverOnlyRecords,
			ServerSyncTableRowData recordData)
	{
		serverOnlyRecords.append(TITLE_SEPARATOR);
		serverOnlyRecords.append(recordData.getTitle());
	}

	@FXML 	
	private void onUpload(ActionEvent event)
	{
		ObservableList<ServerSyncTableRowData> selectedRows = allRecordsTable.getSelectionModel().getSelectedItems();
		StringBuilder serverOnlyRecords = new StringBuilder();
		ClientBulletinStore store = getApp().getStore();
		BulletinFolder draftOutBox = store.getFolderDraftOutbox();
		BulletinFolder sealedOutBox = store.getFolderSealedOutbox();
		for (Iterator iterator = selectedRows.iterator(); iterator.hasNext();)
		{
			ServerSyncTableRowData recordData = (ServerSyncTableRowData) iterator.next();
			if(recordData.getRawLocation() == ServerSyncTableRowData.LOCATION_SERVER)
			{
				addToInvalidRecords(serverOnlyRecords, recordData);
			}
			else
			{
				String accountId = getApp().getAccountId();
				Bulletin bulletin =  store.getBulletinRevision(recordData.getUniversalId());
				if(!bulletin.getBulletinHeaderPacket().isAuthorizedToUpload(accountId))
				{
					addToInvalidRecords(serverOnlyRecords, recordData);
					continue;
				}

				try
				{
					if(bulletin.isMutable())
						draftOutBox.add(bulletin);
					if(bulletin.isImmutable())
						sealedOutBox.add(bulletin);
				} 
				catch (BulletinAlreadyExistsException ignored)
				{
				} 
				catch (IOException e)
				{
					addToInvalidRecords(serverOnlyRecords, recordData);
					logAndNotifyUnexpectedError(e);
				}
			}
		}
		if(serverOnlyRecords.length()>0)
			DisplayWarningDialog("SyncUnableToUploadServerFiles", serverOnlyRecords);

		closeDialog();
	}

	@FXML 	
	private void onDownload(ActionEvent event)
	{
		ObservableList<ServerSyncTableRowData> selectedRows = allRecordsTable.getSelectionModel().getSelectedItems();
		StringBuilder localOnlyRecords = new StringBuilder();
		Vector<UniversalId> uidsToDownload = new Vector(selectedRows.size());
		for (Iterator iterator = selectedRows.iterator(); iterator.hasNext();)
		{
			ServerSyncTableRowData recordData = (ServerSyncTableRowData) iterator.next();
			if(recordData.getRawLocation() == ServerSyncTableRowData.LOCATION_LOCAL)
				addToInvalidRecords(localOnlyRecords, recordData);
			else
				uidsToDownload.add(recordData.getUniversalId());
		}
		if(localOnlyRecords.length()>0)
			DisplayWarningDialog("SyncUnableToDownloadLocalFiles", localOnlyRecords);
		try
		{
			String retrievedFolder = getApp().getNameOfFolderForAllRetrieved();
			getMainWindow().retrieveRecordsFromServer(retrievedFolder, uidsToDownload);
			closeDialog();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@FXML 	
	private void onDelete(ActionEvent event)
	{
		ObservableList<ServerSyncTableRowData> selectedRows = allRecordsTable.getSelectionModel().getSelectedItems();
		StringBuilder localOrImmutableRecords = new StringBuilder();
		Vector<UniversalId> uidsToDelete = new Vector(selectedRows.size());
		for (Iterator iterator = selectedRows.iterator(); iterator.hasNext();)
		{
			ServerSyncTableRowData recordData = (ServerSyncTableRowData) iterator.next();
			if(recordData.canDeleteFromServerProperty().getValue())
				uidsToDelete.add(recordData.getUniversalId());
			else
				addToInvalidRecords(localOrImmutableRecords, recordData);
		}
		if(localOrImmutableRecords.length()>0)
			DisplayWarningDialog("SyncUnableToDeleteLocalOnlyOrImmutableFiles", localOrImmutableRecords);
		try
		{
			getMainWindow().deleteMutableRecordsFromServer(uidsToDelete);
			closeDialog();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@FXML 	
	protected void onShowAll(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_ANY);
	}

	@FXML 	
	private void onShowLocalOnly(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_LOCAL);
	}

	@FXML 	
	private void onShowServerOnly(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_SERVER);
	}

	@FXML 	
	private void onShowBoth(ActionEvent event)
	{
		updateTable(ServerSyncTableRowData.LOCATION_BOTH);
	}

	@FXML 	
	private void onSubfilterAll(ActionEvent event)
	{
		filterTable(SyncRecordsTableProvider.SUB_FILTER_ALL);
	}

	@FXML 	
	private void onSubfilterMyRecords(ActionEvent event)
	{
		filterTable(SyncRecordsTableProvider.SUB_FILTER_MY_RECORDS);
	}

	@FXML 	
	private void onSubfilterSharedWithMe(ActionEvent event)
	{
		filterTable(SyncRecordsTableProvider.SUB_FILTER_SHARED_WITH_ME);
	}

	private final String TITLE_SEPARATOR = "\n";
	
	@FXML
	private Hyperlink fxLocationAll;
	
	@FXML
	private Hyperlink fxLocationLocalOnly;
	
	@FXML
	private Hyperlink fxLocationServerOnly;
	
	@FXML
	private Hyperlink fxLocationBoth;
	
	@FXML
	private Hyperlink fxSubFilterAll;
	
	@FXML
	private Hyperlink fxSubFilterMyRecords;
	
	@FXML
	private Hyperlink fxSubFilterSharedWithMe;
	
	@FXML
	protected TableView<ServerSyncTableRowData> allRecordsTable;
	
	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordLocationColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordTitleColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordAuthorColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, String> recordLastSavedColumn;

	@FXML
	private TableColumn<ServerSyncTableRowData, Integer> recordSizeColumn;
	
	@FXML 
	private Button uploadButton;
	
	@FXML 
	private Button downloadButton;

	@FXML 
	private Button deleteButton;
	
	protected ProgressBar recordsProgressBar;
	protected Label progressLabel;
	protected Vector serverMyDrafts;
	protected Vector serverMySealeds;
	protected Vector serverHQDrafts;
	protected Vector serverHQSealeds;
	protected boolean loadRecordsError;
	private SyncRecordsTableProvider syncRecordsTableProvider;
}
