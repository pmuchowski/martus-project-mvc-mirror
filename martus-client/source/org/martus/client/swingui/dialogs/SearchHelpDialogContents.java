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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.SwingDialogContentPane;
import org.martus.swing.UiButton;
import org.martus.swing.UiWrappedTextPanel;

public class SearchHelpDialogContents extends SwingDialogContentPane
{
	public SearchHelpDialogContents(UiMainWindow mainWindowToUse, String title, String message, String closeButton)
	{
		super(mainWindowToUse);

		setTitle(title);
		setBorder(new EmptyBorder(5,5,5,5));
		setLayout(new BorderLayout());
		UiWrappedTextPanel messagePanel = new UiWrappedTextPanel(message);
		messagePanel.setBorder(new EmptyBorder(5,5,5,5));
		messagePanel.setPreferredSize(new Dimension(500,500));
		add(messagePanel, BorderLayout.CENTER);

		UiButton button = new UiButton(closeButton);
		button.addActionListener((event) -> dispose());
		Box hbox = Box.createHorizontalBox();
		hbox.add(Box.createHorizontalGlue());
		hbox.add(button);
		hbox.add(Box.createHorizontalGlue());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new EmptyBorder(5,5,0,5));
		buttonPanel.add(hbox);
		add(buttonPanel, BorderLayout.SOUTH);
	}
}
