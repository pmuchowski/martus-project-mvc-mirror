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
package org.martus.client.swingui.jfx.generic;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import org.json.converter.JSONException;
import org.json.converter.JSONObject;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.filefilters.ServerResponseFileFilter;
import org.martus.client.swingui.jfx.landing.general.ServerResponseController;
import org.martus.clientside.FormatFilter;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;
import org.martus.util.UnicodeReader;

public class ImportServerResponseFileAction implements ActionDoer
{
	public ImportServerResponseFileAction(FxContentController fxCaseManagementControllerToUse)
	{
		fxCaseManagementController = fxCaseManagementControllerToUse;
		uiMainWindow = fxCaseManagementController.getMainWindow();
	}
	
	@Override
	public void doAction()
	{
		Vector<FormatFilter> filters = new Vector();
		filters.add(new ServerResponseFileFilter(getLocalization()));
		File[] selectedFiles = getMainWindow().showMultiFileOpenDialog("ImportServerResponseFile", filters);
		for (int index = 0; index < selectedFiles.length; ++index)
		{
			importServerResponseFile(selectedFiles[index]);
		}
	}
	
	public void importServerResponseFile(File serverResponseFileToImport)
	{
		try
		{
			safelyReadFile(serverResponseFileToImport);
		}
		catch (JSONException e)
		{
			MartusLogger.logException(e);
			uiMainWindow.notifyDlg("ImportServerResponseFileJsonError", new HashMap());
		}
		catch (Exception e)
		{
			uiMainWindow.unexpectedErrorDlg(e);
		}
	}

	private void safelyReadFile(File serverResponseFileToImport) throws Exception
	{
		UnicodeReader reader = new UnicodeReader(serverResponseFileToImport);
		try
		{		
			String allContentAsString = reader.readAll();
			JSONObject json = loadResponseJson(allContentAsString);
			Set jsonKeys = json.keySet();
			HashMap<String, String> recordNameToServerResponseMap = new HashMap<>();
			for (Object jsonKey : jsonKeys)
			{
				String jsonValue = json.getString(jsonKey.toString());
				String bulletinId = jsonKey.toString();
				UniversalId universalId = UniversalId.createFromString(bulletinId);
				Vector<Bulletin> expectedSingleItemBulletins = getMainWindow().getBulletins(new UniversalId[]{universalId});
				
				Bulletin foundBulletin = expectedSingleItemBulletins.get(0);
				if (foundBulletin == null)
				{
					recordNameToServerResponseMap.put(getLocalization().getFieldLabel("UnknownRecord"), getLocalization().getFieldLabel("RecordDoesNotExist"));
				}
				else
				{
					String bulletinTitle = foundBulletin.get(Bulletin.TAGTITLE);
					recordNameToServerResponseMap.put(bulletinTitle, jsonValue);
				}
			}
			
			FxController controller = new ServerResponseController(getMainWindow(), recordNameToServerResponseMap);
			ActionDoer shellController = new DialogWithNoButtonsShellController(getMainWindow(), controller);
			fxCaseManagementController.doAction(shellController);
		}
		finally 
		{
			reader.close();
		}
	}

	private JSONObject loadResponseJson(String allContent) throws Exception
	{
		try 
		{
			return new JSONObject(allContent);
		}
		catch (Exception e)
		{
			MartusLogger.logError("Could not load Json with string: " + allContent);
			throw e;
		}
	}

	private UiMainWindow getMainWindow()
	{
		return uiMainWindow;
	}
	
	private MartusLocalization getLocalization()
	{
		return getMainWindow().getLocalization();
	}

	private UiMainWindow uiMainWindow;
	private FxContentController fxCaseManagementController;
}
