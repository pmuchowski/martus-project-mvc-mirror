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
package org.martus.client.swingui;

import org.martus.clientside.UiLocalization;
import org.martus.util.language.LanguageOptions;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;

public class PureFxModalBusyDialog extends Dialog implements ModalBusyDialogInterface
{
	public PureFxModalBusyDialog(UiMainWindow mainWindow, String dialogTag)
	{
		initialize(mainWindow, dialogTag);
	}

	public void initialize(UiMainWindow mainWindow, String dialogTag)
	{
		if (LanguageOptions.isRightToLeftLanguage())
			getDialogPane().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		else
			getDialogPane().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

		UiLocalization localization = mainWindow.getLocalization();

		initStyle(StageStyle.UNDECORATED);
		setTitle(localization.getWindowTitle(dialogTag));
		setGraphic(null);
		setHeaderText(null);

		getDialogPane().setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		Label label = new Label(localization.getFieldLabel(dialogTag));

		HBox box = new HBox();
		box.setPadding(new Insets(30, 10, 10, 10));
		box.getChildren().addAll(label);

		getDialogPane().setContent(box);
	}

	@Override
	public void showDialog()
	{
		showAndWait();
	}

	@Override
	public void workerFinished()
	{
		Platform.runLater(() -> {
			getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
			close();
			getDialogPane().getButtonTypes().remove(ButtonType.CANCEL);
		});
	}
}
