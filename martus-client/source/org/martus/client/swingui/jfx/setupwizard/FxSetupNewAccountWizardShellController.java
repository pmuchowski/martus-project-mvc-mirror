/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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
package org.martus.client.swingui.jfx.setupwizard;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.FxWizardShellController;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class FxSetupNewAccountWizardShellController extends FxWizardShellController
{
	public FxSetupNewAccountWizardShellController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/SetupNewAccountWizardShell.fxml";
	}
	
	@Override
	protected String getCssName()
	{
		return "SetupNewAccountWizard.css";
	}
	
	public void loadAndIntegrateContentPane(FxController contentPaneController) throws Exception
	{
		AbstractFxSetupWizardContentController controller = (AbstractFxSetupWizardContentController) contentPaneController;
		setContentNavigationHandler(controller);
		Parent createContents = contentPaneController.createContents();
		
		contentPane.getChildren().addAll(createContents);
		
		int stepNumber = controller.getWizardStepNumber();
		Node step = getStep(stepNumber);
		step.getStyleClass().add("current-step");
	}
	
	private Node getStep(int stepNumber)
	{
		switch(stepNumber)
		{
			case 1: return step1;
			case 2: return step2;
			case 3: return step3;
		}
		
		throw new RuntimeException("Unknown step number: " + stepNumber);
	}

	@FXML
	protected HBox step1;
	
	@FXML
	protected HBox step2;
	
	@FXML
	protected HBox step3;
	
	@FXML
	private Pane contentPane;
}
