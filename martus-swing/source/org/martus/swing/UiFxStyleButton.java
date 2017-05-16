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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.Action;

public class UiFxStyleButton extends UiButton
{
	private static final Integer DEFAULT_ROUND_VALUE = 6;
	private static final Integer DEFAULT_PADDING = 30;
	private static final String DEFAULT_BACKGROUND = "#00B050";
	private static final String DEFAULT_TEXT_COLOR = "#FFFFFF";
	private static final String DEFAULT_BORDER_COLOR = "#000000";
	private static final Float DEFAULT_FONT_SIZE = 18.0f;

	private Boolean roundedCorners;
	private Boolean drawBorder;
	private Color borderColor;
	private Integer padding;

	public UiFxStyleButton()
	{
		initialize();
	}

	public UiFxStyleButton(String text)
	{
		super(text);
		initialize();
	}

	public UiFxStyleButton(Action action)
	{
		super(action);
		initialize();
	}

	private void initialize()
	{
		setContentAreaFilled(false);
		setFocusPainted(false);
		setBackground(Color.decode(DEFAULT_BACKGROUND));
		setForeground(Color.decode(DEFAULT_TEXT_COLOR));
		setFontSize(DEFAULT_FONT_SIZE);
		setTextPadding(DEFAULT_PADDING);
		setRoundedCorners(true);
		setDrawBorder(false);
		setBorderColor(DEFAULT_BORDER_COLOR);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		final Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(getBackground());

		if (getRoundedCorners())
		{
			g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, DEFAULT_ROUND_VALUE, DEFAULT_ROUND_VALUE);
		}
		else
		{
			g2.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		}

		drawBorders(g2);
		drawString(g2);
		g2.dispose();
	}

	private void drawBorders(Graphics2D g2)
	{
		if (getDrawBorder())
		{
			g2.setPaint(getBorderColor());
			if (getRoundedCorners())
			{
				g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, DEFAULT_ROUND_VALUE, DEFAULT_ROUND_VALUE);
			}
			else
			{
				g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			}
		}
	}

	private void drawString(Graphics2D g2)
	{
		FontMetrics metrics = g2.getFontMetrics(getFont());
		int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
		g2.setPaint(getForeground());
		g2.drawString(getText(), padding, y);
	}

	public void setFontSize(float fontSize) {
		setFont(FontHandler.getDefaultFont().deriveFont(fontSize));
	}

	public void setTextPadding(int padding)
	{
		this.padding = padding;
		setMargin(new Insets(0, padding, 0, padding));
	}

	public Boolean getRoundedCorners()
	{
		return roundedCorners;
	}

	public void setRoundedCorners(Boolean roundedCorners)
	{
		this.roundedCorners = roundedCorners;
	}

	public Boolean getDrawBorder()
	{
		return drawBorder;
	}

	public void setDrawBorder(Boolean drawBorder)
	{
		this.drawBorder = drawBorder;
	}

	public Color getBorderColor()
	{
		return borderColor;
	}

	public void setBorderColor(Color borderColor)
	{
		this.borderColor = borderColor;
	}

	public void setBorderColor(String borderColorCode)
	{
		this.borderColor = Color.decode(borderColorCode);
	}
}
