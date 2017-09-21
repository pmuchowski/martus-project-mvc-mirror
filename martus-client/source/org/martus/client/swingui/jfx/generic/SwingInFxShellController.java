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

import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.martus.client.swingui.UiMainWindow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.Pane;

public abstract class SwingInFxShellController extends FxPopupController
{
	public SwingInFxShellController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);

		final SwingNode swingNode = new SwingNode();

		createAndSetSwingContent(swingNode);
		getSwingPane().getChildren().add(swingNode);

		getSwingPane().widthProperty().addListener(new ResizeHandler());
		getSwingPane().heightProperty().addListener(new ResizeHandler());
	}

	private void createAndSetSwingContent(final SwingNode swingNode)
	{
		SwingUtilities.invokeLater(() ->
		{
			swingContent = createSwingContent();
			swingNode.setContent(swingContent);
		});
	}

	class ResizeHandler implements ChangeListener<Number>
	{
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
		{
			SwingUtilities.invokeLater(() -> swingContent.repaint());
		}
	}

	public abstract JComponent createSwingContent();

	public abstract Pane getSwingPane();

	private JComponent swingContent;
}
