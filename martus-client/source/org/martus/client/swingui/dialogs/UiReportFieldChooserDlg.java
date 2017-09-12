/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.martus.client.search.FieldChoicesByLabel;
import org.martus.client.search.FieldChooserSpecBuilder;
import org.martus.client.search.PageReportFieldChooserSpecBuilder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusLogger;
import org.martus.common.MiniLocalization;
import org.martus.common.PoolOfReusableChoicesLists;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.swing.FontHandler;
import org.martus.swing.UiButton;
import org.martus.swing.Utilities;

import javafx.collections.ObservableSet;

public class UiReportFieldChooserDlg extends UIReportFieldDlg
{
	public UiReportFieldChooserDlg(UiMainWindow mainWindowToUse, FieldSpec[] specsToUse, ResultsHandler resultsHandlerToUse)
	{
		this(mainWindowToUse, specsToUse);
		resultsHandler = resultsHandlerToUse;
	}

	public UiReportFieldChooserDlg(UiMainWindow mainWindowToUse, FieldSpec[] specsToUse)
	{
		super(mainWindowToUse.getSwingFrame());
		
		mainWindow = mainWindowToUse;
		setModal(true);
		String dialogTag = "ChooseReportFields";
		MartusLocalization localization = mainWindow.getLocalization();
		setTitle(localization.getWindowTitle(dialogTag));
		
		selectedSpecs = null;
		fieldSelector = new UiReportFieldSelectorPanel(mainWindow, specsToUse);
		selectedFieldsSelector = new UiReportFieldSelectorPanel(mainWindow, new FieldSpec[0]);
		
		JPanel fieldSelectorPanel = new JPanel(new BorderLayout());
		fieldSelectorPanel.add(new JLabel(localization.getFieldLabel("SelectFields")), BorderLayout.PAGE_START);
		fieldSelectorPanel.add(fieldSelector, BorderLayout.CENTER);
		
		JPanel selectedFieldsPanel = new  JPanel(new BorderLayout());
		selectedFieldsPanel.add(new JLabel(localization.getFieldLabel("SelectedFields")), BorderLayout.PAGE_START);
		selectedFieldsPanel.add(selectedFieldsSelector, BorderLayout.CENTER);

		JPanel dropDownPanel = new JPanel(new BorderLayout());
		createFormTemplateCombo();

		Box selectLabelBox = createStepLabel("ReportStep1", "SelectReportTemplate");
		Box fieldsLabelBox = createStepLabel("ReportStep2", dialogTag);

		dropDownPanel.add(selectLabelBox, BorderLayout.NORTH);
		dropDownPanel.add(formTemplateDropdown, BorderLayout.CENTER);
		dropDownPanel.add(fieldsLabelBox, BorderLayout.SOUTH);

		JPanel mainSelectionsPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gridBagConstraints = createGridBagConstraints(0, 0, GridBagConstraints.HORIZONTAL, 1, 0);
		gridBagConstraints.gridwidth = 3;
		mainSelectionsPanel.add(dropDownPanel, gridBagConstraints);

		gridBagConstraints = createGridBagConstraints(0, 1, GridBagConstraints.BOTH, 1, 1);
		mainSelectionsPanel.add(fieldSelectorPanel, gridBagConstraints);

		gridBagConstraints = createGridBagConstraints(1, 1, GridBagConstraints.NONE);
		gridBagConstraints.anchor = GridBagConstraints.PAGE_START;
		mainSelectionsPanel.add(createAddRemoveSelectedFieldsButtonsPanel(), gridBagConstraints);
		addTableRowDoubleClickListeners();

		gridBagConstraints = createGridBagConstraints(2, 1, GridBagConstraints.BOTH, 1, 1);
		mainSelectionsPanel.add(selectedFieldsPanel, gridBagConstraints);

		mainPanelWithButtonBar = new JPanel(new BorderLayout());
		mainPanelWithButtonBar.add(mainSelectionsPanel, BorderLayout.CENTER);
		mainPanelWithButtonBar.add(createButtonBar(localization), BorderLayout.AFTER_LAST_LINE);

		getContentPane().add(mainPanelWithButtonBar);
		pack();
		Utilities.packAndCenterWindow(this);
	}

	private void addTableRowDoubleClickListeners()
	{
		fieldSelector.getTable().addMouseListener(new AddFieldByDoubleClickSelectedField());
		selectedFieldsSelector.getTable().addMouseListener(new RemoveFieldByDoubleClickSelectedField());
	}
	
	private Box createStepLabel(String stepCode, String labelCode) {
		Box labelBox = Box.createHorizontalBox();
		labelBox.setBorder(new EmptyBorder(15, 0, 5, 0));

		JLabel stepLabel = new JLabel(mainWindow.getLocalization().getFieldLabel(stepCode));
		stepLabel.setFont(FontHandler.getDefaultFont().deriveFont(Font.BOLD, 16.0f));

		JLabel label = new JLabel(mainWindow.getLocalization().getFieldLabel(labelCode));
		label.setFont(FontHandler.getDefaultFont().deriveFont(Font.PLAIN, 16.0f));

		Component[] labels = new Component[] { stepLabel, label, Box.createHorizontalGlue() };
		Utilities.addComponentsRespectingOrientation(labelBox, labels);

		return labelBox;
	}

	private GridBagConstraints createGridBagConstraints(int gridx, int gridy, int fill) {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = gridx;
		gridBagConstraints.gridy = gridy;
		gridBagConstraints.fill = fill;
		return gridBagConstraints;
	}

	private GridBagConstraints createGridBagConstraints(int gridx, int gridy, int fill, int weightx, int weighty) {
		GridBagConstraints gridBagConstraints = createGridBagConstraints(gridx, gridy, fill);
		gridBagConstraints.weightx = weightx;
		gridBagConstraints.weighty = weighty;
		return gridBagConstraints;
	}

	private Box createButtonBar(MartusLocalization localization)
	{
		UiButton okButton = new UiButton(localization.getButtonLabel(EnglishCommonStrings.OK));
		okButton.addActionListener(new OkButtonHandler());
		UiButton cancelButton = new UiButton(localization.getButtonLabel(EnglishCommonStrings.CANCEL));
		cancelButton.addActionListener(new CancelButtonHandler());
		Box buttonBar = Box.createHorizontalBox();
		Component[] buttons = {Box.createHorizontalGlue(), okButton, cancelButton};
		Utilities.addComponentsRespectingOrientation(buttonBar, buttons);
		
		return buttonBar;
	}

	private JPanel createAddRemoveSelectedFieldsButtonsPanel()
	{
		removeFieldButton = new JButton("<-");
		removeFieldButton.addActionListener(new RemoveFieldFromSelectedTableHandler());
		
		addFieldButton = new JButton("->");
		addFieldButton.addActionListener(new AddFieldToSelectedTableHandler());
		
		JPanel selectionButtonsPanel = new JPanel();
		selectionButtonsPanel.setLayout(new BoxLayout(selectionButtonsPanel, BoxLayout.Y_AXIS));
		selectionButtonsPanel.add(removeFieldButton, BorderLayout.NORTH);
		selectionButtonsPanel.add(addFieldButton, BorderLayout.SOUTH);
		
		return selectionButtonsPanel;
	}
	
	private void createFormTemplateCombo()
	{
		ObservableSet<String> availableTemplates = mainWindow.getStore().getAvailableTemplates();
		try 
		{
			formTemplateDropdown = new JComboBox<FormTemplate>();
			formTemplateDropdown.addItemListener(new FormTemplateComboboxItemListener());
			for (String templateName : availableTemplates)
			{
				FormTemplate formTemplate = getMainWindow().getStore().getFormTemplate(templateName);
				formTemplateDropdown.addItem(new ChoiceBoxItem(formTemplate));
			}

			formTemplateDropdown.addItem(new AllKnownFieldSpecsPseudoFormTemplate());
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	protected FieldSpecCollection getAllKnownFields()
	{
		FieldSpecCollection allKnownFields = new FieldSpecCollection();
		Set allKnownFieldsAsSet = getMainWindow().getStore().getAllKnownFieldSpecs();
		allKnownFields.addAllSpecs(allKnownFieldsAsSet);
		
		return allKnownFields;
	}
	
	protected void fillSelectorPanel(ChoiceBoxItem selectedFormTemplate)
	{
		FieldSpecCollection allFieldsForTemplate = new FieldSpecCollection();
		FieldSpecCollection topTemplateFields = selectedFormTemplate.getTopFields();
		FieldSpecCollection bottomTemplateFields = selectedFormTemplate.getBottomFields();
		allFieldsForTemplate.addAll(topTemplateFields);
		allFieldsForTemplate.addAll(bottomTemplateFields);

		PoolOfReusableChoicesLists reusableChoicesLists = new PoolOfReusableChoicesLists();
		reusableChoicesLists.addAll(topTemplateFields.getAllReusableChoiceLists());
		reusableChoicesLists.addAll(bottomTemplateFields.getAllReusableChoiceLists());

		FieldChooserSpecBuilder fieldChooserSpecBuilder = new PageReportFieldChooserSpecBuilder(getMainWindow().getLocalization());
		FieldChoicesByLabel allFieldsForFormTemplate = fieldChooserSpecBuilder.buildFieldChoicesByLabel(getMainWindow().getStore(), allFieldsForTemplate.asSet(), null, reusableChoicesLists);
		fieldSelector.rebuildTableWithNewFieldSpecs(allFieldsForFormTemplate.asArray(getMainWindow().getLocalization()));
	}

	@Override
	public void showDialog()
	{
		setVisible(true);
	}

	@Override
	public JComponent getMainContent()
	{
		return mainPanelWithButtonBar;
	}

	public interface ResultsHandler
	{
		void setResults(FieldSpec[] selectedSpecs);
	}

	class OkButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			selectedSpecs = selectedFieldsSelector.getAllItems();
			if (resultsHandler != null)
				resultsHandler.setResults(selectedSpecs);

			dispose();
		}
	}
	
	class CancelButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
	}
	
	protected class FormTemplateComboboxItemListener implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			ChoiceBoxItem selectedFormTemplate = (ChoiceBoxItem) formTemplateDropdown.getSelectedItem();
			fillSelectorPanel(selectedFormTemplate);
		}

	}
	
	protected class RemoveFieldFromSelectedTableHandler implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			FieldSpec[] selectedFieldsToRemove = selectedFieldsSelector.getSelectedItems();
			selectedFieldsSelector.model.removeFieldSpecs(selectedFieldsToRemove);
		}
	}
	
	protected class AddFieldToSelectedTableHandler implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			FieldSpec[] selectedFieldsToAdd = fieldSelector.getSelectedItems();
			selectedFieldsSelector.model.addUniqueSpecs(selectedFieldsToAdd);
			selectedFieldsSelector.repaint();
		}
	}

	public FieldSpec[] getSelectedSpecs()
	{
		return selectedSpecs;
	}
	
	protected UiMainWindow getMainWindow()
	{
		return mainWindow;
	}
	
	protected MiniLocalization getLocatization()
	{
		return getMainWindow().getLocalization();
	}
	
	private class ChoiceBoxItem
	{
		public ChoiceBoxItem(FormTemplate formTemplateToUse) throws Exception
		{
			formTemplate = formTemplateToUse;
		}
		
		public FieldSpecCollection getTopFields()
		{
			return formTemplate.getTopFields();
		}
		
		public FieldSpecCollection getBottomFields()
		{
			return formTemplate.getBottomFields();
		}
		
		public String getTitle()
		{
			return formTemplate.getTitle();
		}

		@Override
		public String toString()
		{
			return FormTemplate.getDisplayableTemplateName(getTitle(), getLocatization());
		}
		
		
		private FormTemplate formTemplate;
	}
	
	private class AllKnownFieldSpecsPseudoFormTemplate extends ChoiceBoxItem
	{
		public AllKnownFieldSpecsPseudoFormTemplate() throws Exception
		{
			super(null);
		}
		
		@Override
		public String toString()
		{
			return getLocatization().getFieldLabel("AllKnownFields");
		}
		
		@Override
		public FieldSpecCollection getBottomFields()
		{
			return getAllKnownFields();
		}
		
		@Override
		public FieldSpecCollection getTopFields()
		{
			return new FieldSpecCollection();
		}
	}
	
	protected abstract class AbstractDoubleClickHandler extends MouseAdapter
	{
		@Override
		 public void mousePressed(MouseEvent me) 
		 {
		        if (isDoubleClick(me)) 
		        {
		        	handleDoubleClick();
		        }
		    }

		private boolean isDoubleClick(MouseEvent me)
		{
			return me.getClickCount() == 2;
		}

		abstract protected void handleDoubleClick();
	}
	
	protected class RemoveFieldByDoubleClickSelectedField extends AbstractDoubleClickHandler
	{
		@Override
		 public void handleDoubleClick() 
		 {
		       removeFieldButton.doClick();
		 }
	}
	
	protected class AddFieldByDoubleClickSelectedField extends AbstractDoubleClickHandler
	{
		@Override
		public void handleDoubleClick() 
		{
			addFieldButton.doClick();
		}
	}
	
	protected JButton removeFieldButton;
	protected JButton addFieldButton;
	private UiMainWindow mainWindow;
	protected JComboBox formTemplateDropdown;
	protected UiReportFieldSelectorPanel fieldSelector;
	protected UiReportFieldSelectorPanel selectedFieldsSelector;
	protected FieldSpec[] selectedSpecs;

	private JPanel mainPanelWithButtonBar;
	private ResultsHandler resultsHandler;
}
