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

import java.util.HashMap;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.FxBulletin;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuViewFxBulletin;
import org.martus.client.swingui.dialogs.UiBulletinDetailsDialog;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.BulletinHistory;
import org.martus.common.packet.UniversalId;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class BulletinEditorFooterController extends FxController
{

	public BulletinEditorFooterController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	private abstract class ComboBoxChoiceItem
	{
		public ComboBoxChoiceItem(FxController controller, String label)
		{
			this.controller = controller;
			this.label = label;
		}

		public abstract void onClick();

		@Override
		public String toString()
		{
			return label;
		}

		public FxController getController()
		{
			return controller;
		}

		private FxController controller;
		private String label;
	}

	private class HistoryItem extends ComboBoxChoiceItem
	{
		public HistoryItem(FxController controller, String data, UniversalId revisionUidToUse)
		{
			super(controller, data);
			revisionUid = revisionUidToUse;
		}

		@Override
		public void onClick()
		{
			ClientBulletinStore store = getController().getApp().getStore();
			Bulletin bulletinHistoryItem = store.getBulletinRevision(this.getUid());

			if(bulletinHistoryItem == null)
				return;

			ActionMenuViewFxBulletin actionDoer = new ActionMenuViewFxBulletin(getMainWindow(), getController());
			actionDoer.setBulletin(bulletinHistoryItem);
			getController().doAction(actionDoer);
		}

		public UniversalId getUid()
		{
			return revisionUid;
		}

		private UniversalId revisionUid;
	}

	private class SaveDraftItem extends ComboBoxChoiceItem
	{
		public SaveDraftItem(FxController controller, String label)
		{
			super(controller, label);
		}

		@Override
		public void onClick()
		{
			FxBulletinEditorShellController shellController = (FxBulletinEditorShellController) getController().getShellController();
			shellController.saveDraftBulletin();
		}
	}

	public void showBulletin(FxBulletin bulletinToShow)
	{
		bulletin = bulletinToShow;
		try
		{
			BulletinHistory history = bulletinToShow.getHistory().getValue();
			UniversalId bulletinUid = bulletinToShow.universalIdProperty().getValue();
			String accountId = bulletinUid.getAccountId();
			UiMainWindow mainWindow = getMainWindow();
			historyItemLabels = FXCollections.observableArrayList();
			int versionNumber = 1;
			for(int i = 0; i < history.size(); ++i, ++versionNumber)
			{
				String localId = history.get(i);
				UniversalId revisionUid = UniversalId.createFromAccountAndLocalId(accountId, localId);
				String dateSaved = UiBulletinDetailsDialog.getSavedDateToDisplay(revisionUid,bulletinUid, mainWindow);
				String title = UiBulletinDetailsDialog.getTitleToDisplay(revisionUid, bulletinUid, mainWindow);
				String versionsData =  getHistoryItemData(versionNumber, dateSaved, title);
				historyItemLabels.add(new HistoryItem(this, versionsData, revisionUid));
			}
			String currentVersionTitle = bulletinToShow.fieldProperty(Bulletin.TAGTITLE).getValue();
			String currentVersionLastSaved = UiBulletinDetailsDialog.getSavedDateToDisplay(bulletinUid,bulletinUid, mainWindow);
			String versionsData =  getHistoryItemData(versionNumber, currentVersionLastSaved, currentVersionTitle);
			historyItemLabels.add(new HistoryItem(this, versionsData, bulletinUid));
			historyItemLabels.add(new SaveDraftItem(this, getLocalization().getButtonLabel("saveAsNewVersion")));
			historyItems.setItems(historyItemLabels);

			historyItems.setCellFactory(new HistoryItemCellFactory());
		}
		catch (TokenInvalidException e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	protected class HistoryItemCellFactory implements Callback<ListView<ComboBoxChoiceItem>, ListCell<ComboBoxChoiceItem>>
	{
		protected class ComboBoxChoiceItemUpdateHandler extends ListCell<ComboBoxChoiceItem>
		{
			@Override
			public void updateItem(ComboBoxChoiceItem item, boolean empty)
			{
				super.updateItem(item, empty);
				setText(getText(item));

				if (item != null && isLastItemInList())
				{
					this.getStyleClass().add("list-item-green");
				}
			}

			private String getText(ComboBoxChoiceItem item)
			{
				if (item == null)
					return null;

				return item.toString();
			}

			private boolean isLastItemInList()
			{
				return getIndex() == getListView().getItems().size() - 1;
			}
		}

		@Override
		public ListCell<ComboBoxChoiceItem> call(ListView<ComboBoxChoiceItem> param)
		{
			return new ComboBoxChoiceItemUpdateHandler();
		}
	}

	private String getHistoryItemData(int versionNumber, String dateSaved, String title) throws TokenInvalidException
	{
		HashMap tokenReplacement = new HashMap();
		tokenReplacement.put("#Title#", title);
		tokenReplacement.put("#DateSaved#", dateSaved);
		tokenReplacement.put("#VersionNumber#", Integer.toString(versionNumber));
		String historyItemTextWithTokens = getLocalization().getFieldLabel("HistoryVersion");
		return TokenReplacement.replaceTokens(historyItemTextWithTokens, tokenReplacement);
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/BulletinEditorFooter.fxml";
	}

	@FXML
	private void onShowVersion(ActionEvent event) 
	{
		try
		{
			final ComboBoxChoiceItem selectedItem = historyItems.getSelectionModel().getSelectedItem();
			selectedItem.onClick();
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@FXML
	private ComboBox<ComboBoxChoiceItem> historyItems;

	private ObservableList<ComboBoxChoiceItem> historyItemLabels;
	private FxBulletin bulletin;
}
