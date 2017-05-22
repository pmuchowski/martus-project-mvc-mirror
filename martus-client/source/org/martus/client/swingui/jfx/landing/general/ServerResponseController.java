/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;

public class ServerResponseController extends FxController
{
	public ServerResponseController(UiMainWindow mainWindowToUse, HashMap<String, String> recordNameToServerResponseMapToUse)
	{
		super(mainWindowToUse);
		
		recordNameToServerResponseMap = recordNameToServerResponseMapToUse;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		
		try
		{
			initializeAvailableTab();
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	private void initializeAvailableTab() throws Exception
	{
		recordNameColumn.setEditable(false);
        recordNameColumn.setCellValueFactory(new PropertyValueFactory<UploaderServerResponseTableRowData,String>(UploaderServerResponseTableRowData.RECORD_NAME));
        serverResponseColumn.setCellValueFactory(new PropertyValueFactory<UploaderServerResponseTableRowData,String>(UploaderServerResponseTableRowData.SERVER_RESPONSE));
        
        populateServerResponsesTable();
	}
	
	private void populateServerResponsesTable() throws Exception
	{
		ObservableList<UploaderServerResponseTableRowData> templateRows = FXCollections.observableArrayList();
		Set<String> jsonKeys = getRecordNameToServerResponseMap().keySet();
		for (String recordName : jsonKeys)
		{
			String serverResponseValue = getRecordNameToServerResponseMap().get(recordName);
			templateRows.add(new UploaderServerResponseTableRowData(recordName, serverResponseValue));
		}
		
		TableViewSelectionModel<UploaderServerResponseTableRowData> selectionModel = uploaderServerResponsesTable.selectionModelProperty().getValue();
		UploaderServerResponseTableRowData selected = selectionModel.getSelectedItem();		
		uploaderServerResponsesTable.setItems(templateRows);
		uploaderServerResponsesTable.sort();
		
		selectionModel.clearSelection();
		selectionModel.select(selected);
	}
	
	private HashMap<String, String> getRecordNameToServerResponseMap()
	{
		return recordNameToServerResponseMap;
	}

	@FXML
	private void onOkButton(ActionEvent event)
	{
		getStage().close();
	}

	@Override
	public String getFxmlLocation()
	{
		return "landing/general/ServerResponseTable.fxml";
	}
	
	private HashMap<String, String> recordNameToServerResponseMap;
	
	@FXML
	private TableView<UploaderServerResponseTableRowData> uploaderServerResponsesTable;
	
	@FXML
	protected TableColumn<UploaderServerResponseTableRowData, String> recordNameColumn;	

	@FXML
	protected TableColumn<UploaderServerResponseTableRowData, String> serverResponseColumn;
}
