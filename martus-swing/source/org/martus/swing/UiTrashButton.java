package org.martus.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;

public class UiTrashButton extends UiButton {

	public UiTrashButton() {
		initialize();
	}

	public UiTrashButton(Action action) {
		super(action);
		initialize();
	}

	private void initialize() {
		try {
			Image img = ImageIO.read(getClass().getResource("/org/martus/client/swingui/jfx/images/trash.png"));
			setIcon(new ImageIcon(img));
		} catch (IOException e) {
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
