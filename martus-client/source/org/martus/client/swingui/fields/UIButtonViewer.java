package org.martus.client.swingui.fields;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;

import org.martus.common.MiniLocalization;
import org.martus.swing.UiButton;
import org.martus.swing.UiTrashButton;

public class UIButtonViewer extends UiField implements ActionListener {

	public UIButtonViewer(MiniLocalization localizationToUse) {
		super(localizationToUse);

		button = new UiTrashButton();
		button.addActionListener(this);
	}

	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (actionListener != null) {
			actionListener.actionPerformed(event);
		}
	}

	@Override
	public JComponent getComponent() {
		return button;
	}

	@Override
	public JComponent[] getFocusableComponents() {
		return new JComponent[] {button};
	}

	@Override
	public String getText() {
		return "";
	}

	@Override
	public void setText(String newText) {
	}

	UiButton button;
	ActionListener actionListener;
}
