package org.martus.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;

public class UiLinkButton extends UiButton {

	public UiLinkButton() {
		initialize();
	}

	public UiLinkButton(String text) {
		super(text);
		initialize();
	}

	public UiLinkButton(Action action) {
		super(action);
		initialize();
	}

	public UiLinkButton(Icon icon) {
		super(icon);
		initialize();
	}

	private void initialize()
	{
		setBorderPainted(false);
		setBackground(Color.WHITE);
		setFocusPainted(false);
		setContentAreaFilled(false);
		Font font = FontHandler.getDefaultFont().deriveFont(Font.ITALIC, 16.0f);
		Map<TextAttribute, Integer> fontAttributes = new HashMap<>();
		fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		setFont(font.deriveFont(fontAttributes));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
}
