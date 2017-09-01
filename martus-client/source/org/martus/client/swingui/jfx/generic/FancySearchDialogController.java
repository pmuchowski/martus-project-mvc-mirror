/*

Martus(TM) is a trademark of Beneficent Technology, Inc.
This software is (c) Copyright 2001-2017, Beneficent Technology, Inc.

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

import javax.swing.JComponent;

import org.json.JSONObject;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.dialogs.FancySearchDialogInterface;
import org.martus.client.swingui.dialogs.UiFancySearchDialogContents;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;

public class FancySearchDialogController extends SwingInFxShellController implements FancySearchDialogInterface
{
	public FancySearchDialogController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initialize()
	{
		Screen screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds();
		swingContainer.setPrefWidth(bounds.getWidth());
	}

	@Override
	public String getFxmlLocation()
	{
		return "generic/FancySearchDialog.fxml";
	}

	@Override
	public JComponent createSwingContent()
	{
		searchDlg = new UiFancySearchDialogContentsInFx(getMainWindow());
		searchDlg.setSearchFinalBulletinsOnly(searchFinalOnly);
		searchDlg.setSearchSameRowsOnly(sameRowsOnly);
		searchDlg.setSearchAsJson(search);

		return searchDlg;
	}

	class UiFancySearchDialogContentsInFx extends UiFancySearchDialogContents
	{
		UiFancySearchDialogContentsInFx(UiMainWindow owner)
		{
			super(owner);
		}

		public void dispose()
		{
			result = searchDlg.getResults();

			if (result)
			{
				searchTree = searchDlg.getSearchTree();

				searchFinalOnly = searchDlg.searchFinalBulletinsOnly();
				sameRowsOnly = searchDlg.searchSameRowsOnly();
				search = searchDlg.getSearchAsJson();
			}

			Platform.runLater(()-> getStage().close());
		}
	}

	@Override
	public boolean searchFinalBulletinsOnly()
	{
		return searchFinalOnly;
	}

	@Override
	public void setSearchFinalBulletinsOnly(boolean searchFinalOnlyToUse)
	{
		searchFinalOnly = searchFinalOnlyToUse;
	}

	@Override
	public boolean searchSameRowsOnly()
	{
		return sameRowsOnly;
	}

	@Override
	public void setSearchSameRowsOnly(boolean sameRowsOnlyToUse)
	{
		sameRowsOnly = sameRowsOnlyToUse;
	}

	@Override
	public JSONObject getSearchAsJson()
	{
		return search;
	}

	@Override
	public void setSearchAsJson(JSONObject searchGridToUse)
	{
		search = searchGridToUse;
	}

	public boolean getResults()
	{
		return result;
	}

	public SearchTreeNode getSearchTree()
	{
		return searchTree;
	}

	public Pane getSwingPane()
	{
		return swingContainer;
	}

	@FXML
	private Pane swingContainer;

	private UiFancySearchDialogContents searchDlg;

	private boolean searchFinalOnly;
	private boolean sameRowsOnly;
	private JSONObject search;

	private boolean result;
	private SearchTreeNode searchTree;
}
