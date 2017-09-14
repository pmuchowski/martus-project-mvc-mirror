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
package org.martus.client.swingui.dialogs;

import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.martus.client.swingui.UiMainWindow;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;

public class PureFxScrollableTextDlgWithBottomPanel extends PureFxScrollableTextDlg
{
	public PureFxScrollableTextDlgWithBottomPanel(UiMainWindow owner, String titleTag, String okButtonTag, String cancelButtonTag, String descriptionTag, String text, JComponent bottomPanel)
	{
		super(owner, titleTag, okButtonTag, cancelButtonTag, descriptionTag, text, new HashMap(), bottomPanel);
	}

	protected void addBottomPanel(Pane mainBox, JComponent bottomPanel)
	{
		SwingNode swingNode = new SwingNode();
		SwingUtilities.invokeLater(() -> addSwingContent(swingNode, bottomPanel));
		mainBox.getChildren().add(swingNode);
	}

	private void addSwingContent(SwingNode swingNode, JComponent bottomPanel)
	{
		double height = bottomPanel.getPreferredSize().getHeight();

		Platform.runLater(() -> setSizeAndCenterDialog(height));

		swingNode.setContent(bottomPanel);
	}

	private void setSizeAndCenterDialog(double swingNodeHeight)
	{
		double maxHeight = Screen.getPrimary().getVisualBounds().getHeight();
		double prefHeight = getHeight() + swingNodeHeight + HEIGHT_OFFSET;

		setHeight(Double.min(maxHeight, prefHeight));

		getDialogPane().getContent().getScene().getWindow().centerOnScreen();
	}

	private static final double HEIGHT_OFFSET = 30;
}
