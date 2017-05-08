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

import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Vector;

import org.martus.client.bulletinstore.ClientBulletinStore;
import org.martus.client.core.FxBulletin;
import org.martus.client.core.MartusApp;
import org.martus.client.search.SaneCollator;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.data.ObservableChoiceItemList;
import org.martus.common.ContactKeys;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.HeadquartersKey;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.ChoiceItem;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.util.TokenReplacement;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

public class BulletinEditorHeaderController extends FxController
{
	public BulletinEditorHeaderController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "landing/bulletins/BulletinEditorHeader.fxml";
	}
	
	@Override
	protected String getCssName()
	{
		return "css/MainDialog.css";
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);

		ClientBulletinStore store = getBulletinStore();
		ObservableSet<String> templateNames = store.getAvailableTemplates();
		ObservableChoiceItemList templateChoiceItems = new ObservableChoiceItemList();
		templateNames.forEach(name -> templateChoiceItems.add(createTemplateChoiceItem(name)));
		Comparator<ChoiceItem> sorter = new SaneCollator(getLocalization().getCurrentLanguageCode());
		templateChoiceItems.sort(sorter);
		availableTemplates.setItems(templateChoiceItems);
		updateSelectionFromReality();
		availableTemplates.valueProperty().addListener(new TemplateChangeHandler());
	}

	private void updateSelectionFromReality()
	{
		ClientBulletinStore store = getBulletinStore();
		ObservableChoiceItemList templateChoiceItems = (ObservableChoiceItemList) availableTemplates.getItems();
		try
		{
			blockSelection();
			ChoiceItem current = templateChoiceItems.findByCode(store.getCurrentFormTemplateName());
			availableTemplates.getSelectionModel().select(current);
			unblockSelection();
		}
		catch(Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private ClientBulletinStore getBulletinStore()
	{
		return getApp().getStore();
	}

	private ChoiceItem createTemplateChoiceItem(String name)
	{
		String displayableName = name;
		if(displayableName.equals(FormTemplate.MARTUS_DEFAULT_FORM_TEMPLATE_NAME))
			displayableName = getLocalization().getFieldLabel("DisplayableDefaultFormTemplateName");
		ChoiceItem choiceItem = new ChoiceItem(name, displayableName);
		return choiceItem;
	}

	public void showBulletin(FxBulletin bulletinToShow)
	{
		updateFrom(bulletinToShow);
		updateTo(bulletinToShow);
		updateAddRemoveContacts();
	}
	
	private void updateAddRemoveContacts()
	{
		try
		{
			ContactKeys ourContacts = getApp().getContactKeys();
			addRemoveContact.setDisable(ourContacts.isEmpty());
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void updateTo(FxBulletin bulletinToShow)
	{
			authorizedToContacts = bulletinToShow.getAuthorizedToReadList();
			updateAuthorizedToContactsList();
	}

	private void updateAuthorizedToContactsList()
	{
		try
		{
			Vector listOfAuthorizedAccounts = new Vector();
			ContactKeys ourContacts = getApp().getContactKeys();
			authorizedToContacts.forEach(key -> addKeyToField(key, ourContacts, listOfAuthorizedAccounts));
			toField.setText(String.join(getLocalization().getFieldLabel("ContactNamesSeparator"), listOfAuthorizedAccounts));
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	public StringProperty getToFieldProperty()
	{
		return toField.textProperty();
	}
	
	private void addKeyToField(HeadquartersKey key, ContactKeys ourContacts, Vector currentListOfAccounts)
	{
		try
		{
			currentListOfAccounts.add(getContactsNameOrPublicCode(getLocalization(), getApp(), key, ourContacts));
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	static public String getContactsNameOrPublicCode(MartusLocalization localization, MartusApp app, HeadquartersKey key, ContactKeys ourContacts) throws Exception  
	{
		String accountId = app.getSecurity().getPublicKeyString();
		if (key.getPublicKey().equals(accountId))
			return app.getUserName();

		String contactName = ourContacts.getLabelIfPresent(key.getPublicKey());
		if(!contactName.isEmpty())
			return contactName;
		
		String contactsPublicCode = key.getFormattedPublicCode40();
		if(ourContacts.containsKey(key.getPublicKey()))
			return contactsPublicCode;
		
		String notInContactsWarning = localization.getFieldLabel("AuthorizedToReadNotInContacts");
		HashMap tokenReplacement = new HashMap();
		tokenReplacement.put("#PublicCode#", contactsPublicCode);
		
		return TokenReplacement.replaceTokens(notInContactsWarning, tokenReplacement);
	}


	private void updateFrom(FxBulletin bulletinToShow)
	{
		try
		{
			String accountId = getMainWindow().getApp().getUserName();
			String accountKey = bulletinToShow.universalIdProperty().get().getAccountId();
			String publicCode = MartusCrypto.computeFormattedPublicCode40(accountKey);
			HashMap tokenReplacement = new HashMap();
			tokenReplacement.put("#AccountId#", accountId);
			tokenReplacement.put("#PublicCode#", publicCode);
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void blockSelection()
	{
		selectionBlocked = true;
	}

	private void unblockSelection()
	{
		selectionBlocked = false;
	}

	private boolean isSelectionBlocked()
	{
		return selectionBlocked;
	}

	protected class TemplateChangeHandler implements ChangeListener<ChoiceItem>
	{
		@Override
		public void changed(ObservableValue<? extends ChoiceItem> observable, ChoiceItem oldValue, ChoiceItem newValue)
		{
			try
			{
				if (isSelectionBlocked())
				{
					return;
				}
				else
				{
					String message = getLocalization().getFieldLabel("confirmOkToSwitchTemplate");
					if (!showConfirmationDialog("SelectTemplate", message))
					{
						blockSelection();
						availableTemplates.getSelectionModel().select(oldValue);
						unblockSelection();
						return;
					}
				}

				getBulletinStore().setFormTemplate(newValue.getCode());
			}
			catch(Exception e)
			{
				logAndNotifyUnexpectedError(e);
			}
		}
	}

	@FXML
	private void onAddRemoveContact(ActionEvent event) 
	{
		Vector currentAuthorizedKeys = new Vector();
		authorizedToContacts.forEach(key -> currentAuthorizedKeys.add(key));
		BulletinContactsController contactsController = new BulletinContactsController(getMainWindow(), currentAuthorizedKeys);
		if(showModalYesNoDialog("BulletinContacts", EnglishCommonStrings.OK, EnglishCommonStrings.CANCEL, contactsController))
		{
			Vector newAuthorizedContacts = contactsController.getCurrentAuthorizedKeys();
			authorizedToContacts.clear();
			authorizedToContacts.addAll(newAuthorizedContacts);
			updateAuthorizedToContactsList();
		}
	}

	@FXML
	private Label toField;

	@FXML
	private Button addRemoveContact;

	@FXML
	private ChoiceBox<ChoiceItem> availableTemplates;

	private boolean selectionBlocked;
	private ObservableList<HeadquartersKey> authorizedToContacts;
}
