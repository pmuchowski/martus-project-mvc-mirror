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

package org.martus.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;

public class UiTrashButton extends UiButton
{

	public UiTrashButton()
	{
		initialize();
	}

	public UiTrashButton(Action action)
	{
		super(action);
		initialize();
	}

	private void initialize()
	{
		try
		{
			Image img = ImageIO.read(getClass().getResource("/org/martus/client/swingui/jfx/images/trash.png"));
			setIcon(new ImageIcon(img));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		setBorderPainted(false);
		setBackground(Color.WHITE);
		setFocusPainted(false);
		setContentAreaFilled(false);
		setPreferredSize(new Dimension(40, 40));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
}
