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

package org.martus.client.swingui.fields;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;

import org.martus.common.MiniLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.UiTrashButton;

public class UIButtonViewer extends UiField implements ActionListener
{

	public UIButtonViewer(MiniLocalization localizationToUse)
	{
		super(localizationToUse);

		button = new UiTrashButton();
		button.addActionListener(this);
	}

	public void setActionListener(ActionListener actionListener)
	{
		this.actionListener = actionListener;
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if (actionListener != null)
		{
			actionListener.actionPerformed(event);
		}
	}

	@Override
	public JComponent getComponent()
	{
		return button;
	}

	@Override
	public JComponent[] getFocusableComponents()
	{
		return new JComponent[] {button};
	}

	@Override
	public String getText()
	{
		return "";
	}

	@Override
	public void setText(String newText)
	{
	}

	private UiButton button;
	private ActionListener actionListener;
}
