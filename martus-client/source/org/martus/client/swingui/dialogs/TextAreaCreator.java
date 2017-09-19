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

import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class TextAreaCreator
{
	public static TextArea createTextArea(String content)
	{
		Text text = new Text(content);
		text.setWrappingWidth(PREF_WIDTH);
		double prefHeight = text.getLayoutBounds().getHeight() + HEIGHT_OFFSET;

		TextArea textArea = new TextArea(content);
		textArea.setPrefWidth(PREF_WIDTH);
		textArea.setPrefHeight(Double.min(MAX_HEIGHT, prefHeight));
		textArea.setWrapText(true);
		textArea.setEditable(false);

		return textArea;
	}

	private static final int PREF_WIDTH = 700;
	private static final int HEIGHT_OFFSET = 20;
	private static final int MAX_HEIGHT = 300;
}
