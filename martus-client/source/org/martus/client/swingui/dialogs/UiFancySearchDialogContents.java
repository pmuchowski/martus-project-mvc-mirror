/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.json.JSONObject;
import org.martus.client.search.FancySearchGridEditor;
import org.martus.client.search.SearchSpec;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.grids.GridTableModel;
import org.martus.client.swingui.jfx.generic.ModalDialogWithSwingContents;
import org.martus.client.swingui.jfx.generic.SwingDialogContentPane;
import org.martus.clientside.FileDialogHelpers;
import org.martus.clientside.FormatFilter;
import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MiniLocalization;
import org.martus.common.crypto.MartusCrypto;
import org.martus.swing.FontHandler;
import org.martus.swing.UiButton;
import org.martus.swing.UiFxStyleButton;
import org.martus.swing.UiCheckBox;
import org.martus.swing.UiLinkButton;
import org.martus.swing.UiTextArea;
import org.martus.swing.UiWrappedTextPanel;
import org.martus.swing.Utilities;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class UiFancySearchDialogContents extends SwingDialogContentPane
{
	private static final String DIALOG_BACKGROUND_COLOR = "#F4F4F4";
	private static final String HEADER_FONT_COLOR = "#595959";
	private static final String SEARCH_BUTTON_BACKGROUND_COLOR = "#00B050";
	private static final String CANCEL_BUTTON_BACKGROUND_COLOR = "#A6A6A6";
	private static final String HELP_BUTTON_BACKGROUND_COLOR = "#FFFFFF";
	private static final String BUTTON_TEXT_WHITE = "#FFFFFF";
	private static final String BUTTON_TEXT_BLACK = "#000000";

	public UiFancySearchDialogContents(UiMainWindow owner)
	{
		super(owner);
		createBody();
	}
	
	void createBody()
	{
		setTitle(getLocalization().getWindowTitle("search"));

		String saveButtonText = getLocalization().getButtonLabel("SaveSearch");
		UiLinkButton save = new UiLinkButton(saveButtonText);
		save.addActionListener(new SaveButtonHandler(this));

		String loadButtonText = getLocalization().getButtonLabel("LoadSearch");
		UiLinkButton load = new UiLinkButton(loadButtonText);
		load.addActionListener(new LoadButtonHandler(this));

		UiButton help = createButton("Help", HELP_BUTTON_BACKGROUND_COLOR, BUTTON_TEXT_BLACK, true);
		help.addActionListener(new HelpListener(getMainWindow()));

		UiButton search = createButton("search", SEARCH_BUTTON_BACKGROUND_COLOR, BUTTON_TEXT_WHITE, false);
		search.addActionListener(new SearchButtonHandler());

		UiButton cancel = createButton(EnglishCommonStrings.CANCEL, CANCEL_BUTTON_BACKGROUND_COLOR, BUTTON_TEXT_WHITE, false);
		cancel.addActionListener(new CancelButtonHandler());

		UiDialogLauncher dlgLauncher = new UiDialogLauncher(getMainWindow(), getMainWindow().getCurrentActiveFrame().getSwingFrame());
		grid = FancySearchGridEditor.create(getMainWindow(), dlgLauncher);
		clearGridIfAnyProblems();

		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BorderLayout());
		headerPanel.setBackground(Color.decode(DIALOG_BACKGROUND_COLOR));
		headerPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
		UiTextArea searchHeader = new UiTextArea(getLocalization().getFieldLabel("SearchHeader"));
		searchHeader.setEditable(false);
		searchHeader.setForeground(Color.decode(HEADER_FONT_COLOR));
		searchHeader.setBackground(Color.decode(DIALOG_BACKGROUND_COLOR));
		searchHeader.setFont(FontHandler.getDefaultFont().deriveFont(Font.BOLD, 20.0f));
		searchHeader.setBorder(new EmptyBorder(5, 15, 5, 5));
		headerPanel.add(searchHeader, BorderLayout.CENTER);

		Box topButtonsBox = Box.createHorizontalBox();
		topButtonsBox.setBackground(Color.decode(DIALOG_BACKGROUND_COLOR));
		topButtonsBox.setBorder(new EmptyBorder(0,0,10,0));
		Component[] buttons = new Component[] { Box.createHorizontalGlue(), save, load };
		Utilities.addComponentsRespectingOrientation(topButtonsBox, buttons);

		Box bottomButtonsBox = Box.createHorizontalBox();
		bottomButtonsBox.setBackground(Color.decode(DIALOG_BACKGROUND_COLOR));
		bottomButtonsBox.setBorder(new EmptyBorder(10,0,0,0));
		buttons = new Component[] { search, Box.createHorizontalStrut(20), cancel, Box.createHorizontalGlue(), help };
		Utilities.addComponentsRespectingOrientation(bottomButtonsBox, buttons);

		dontSearchTrash = createCheckBox("DontSearchTrash");
		dontSearchTrash.setSelected(true);
		dontSearchTrash.setEnabled(false);
		searchFinalBulletins = createCheckBox("SearchFinalBulletinsOnly");
		searchFinalBulletins.setSelected(false);
		searchSameRowsOnly = createCheckBox("SearchSameRowsOnly");
		searchSameRowsOnly.setSelected(false);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.decode(DIALOG_BACKGROUND_COLOR));
		bottomPanel.setLayout(new GridBagLayout());

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1;
		bottomPanel.add(topButtonsBox, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		bottomPanel.add(dontSearchTrash, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		bottomPanel.add(searchFinalBulletins, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		bottomPanel.add(searchSameRowsOnly, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.weightx = 1;
		bottomPanel.add(bottomButtonsBox, gridBagConstraints);

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(15,20,15,20));
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBackground(Color.decode(DIALOG_BACKGROUND_COLOR));

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1;

		mainPanel.add(headerPanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.weighty = 1;

		mainPanel.add(grid.getComponent(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.weightx = 1;

		mainPanel.add(bottomPanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1;
		gridBagConstraints.weighty = 1;

		setLayout(new GridBagLayout());
		add(mainPanel, gridBagConstraints);
		setInsertButtonAsDefault();
	}

	private UiButton createButton(String messageCode, String backgroundColorCode, String textColorCode, boolean drawBorder) {
		UiFxStyleButton button = new UiFxStyleButton(getLocalization().getButtonLabel(messageCode));
		button.setForeground(Color.decode(textColorCode));
		button.setBackground(Color.decode(backgroundColorCode));
		button.setDrawBorder(drawBorder);

		return button;
	}

	private UiCheckBox createCheckBox(String messageCode) {
		UiCheckBox checkBox = new UiCheckBox(getLocalization().getButtonLabel(messageCode));
		checkBox.setFocusPainted(false);
		checkBox.setFont(FontHandler.getDefaultFont().deriveFont(12.0f));

		return checkBox;
	}

	private void setInsertButtonAsDefault()
	{
		setDefaultButton(grid.getInsertButton());
	}

	private void clearGridIfAnyProblems()
	{
		try
		{
			GridTableModel model = grid.getGridTableModel(); 
			for(int row = 0; row < model.getRowCount(); ++row)
			{
				for(int col = 0; col < model.getColumnCount(); ++col)
				{
					model.getFieldSpecForCell(row, col);
				}
			}
		}
		catch (RuntimeException e)
		{
			// unable to restore previous search for some reason.
			// most likely, the choices have changed, 
			// perhaps because we are now in a different UI language
			grid.setText("");
		}
		
	}
	
	private static class HelpListener implements ActionListener
	{
		HelpListener(UiMainWindow mainWindowToUse)
		{
			mainWindow = mainWindowToUse;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			String closeHelpButton = getLocalization().getButtonLabel("CloseHelp");
			String title = getLocalization().getWindowTitle("FancySearchHelp");

			StringBuffer rawHelpMessage = new StringBuffer(getLocalization().getFieldLabel("FancySearchHelpMsg1"));
			rawHelpMessage.append("\n");
			rawHelpMessage.append(getLocalization().getFieldLabel("FancySearchHelpMsg2"));
			rawHelpMessage.append("\n");
			if(notInEnglishSoExplainUsingEnglishAndOr())
			{
				rawHelpMessage.append(getLocalization().getFieldLabel("FancySearchHelpMsg3"));
				rawHelpMessage.append("\n");
			}
			rawHelpMessage.append(getLocalization().getFieldLabel("FancySearchHelpMsg4"));
			rawHelpMessage.append(getLocalization().getFieldLabel("FancySearchHelpMsg5"));
			rawHelpMessage.append("\n");
			
			try
			{
				HashMap tokenReplacement = new HashMap();
				tokenReplacement.put("#And#", getLocalization().getKeyword("and"));
				tokenReplacement.put("#Or#", getLocalization().getKeyword("or"));
				tokenReplacement.put("#AndEnglish#", "and");
				tokenReplacement.put("#OrEnglish#", "or");
				String helpMessage = TokenReplacement.replaceTokens(rawHelpMessage.toString(), tokenReplacement);
				showHelp(title, helpMessage, closeHelpButton);
			}
			catch(TokenInvalidException e1)
			{
				e1.printStackTrace();
			}
		}
		
		private void showHelp(String title, String message, String closeButton)
		{
			SwingDialogContentPane panel = new SwingDialogContentPane(mainWindow);
			panel.setTitle(title);
			panel.setBorder(new EmptyBorder(5,5,5,5));
			panel.setLayout(new BorderLayout());
			UiWrappedTextPanel messagePanel = new UiWrappedTextPanel(message);
			messagePanel.setBorder(new EmptyBorder(5,5,5,5));
			messagePanel.setPreferredSize(new Dimension(500,500));
			panel.add(messagePanel, BorderLayout.CENTER);

			UiButton button = new UiButton(closeButton);
			button.addActionListener((event) -> panel.dispose());
			Box hbox = Box.createHorizontalBox();
			hbox.add(Box.createHorizontalGlue());
			hbox.add(button);
			hbox.add(Box.createHorizontalGlue());
			JPanel buttonPanel = new JPanel();
			buttonPanel.setBorder(new EmptyBorder(5,5,0,5));
			buttonPanel.add(hbox);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			
			ModalDialogWithSwingContents.show(panel);
		}
		
		private boolean notInEnglishSoExplainUsingEnglishAndOr()
		{
			return !getLocalization().getCurrentLanguageCode().equals(MiniLocalization.ENGLISH);
		}
		
		private UiLocalization getLocalization()
		{
			return mainWindow.getLocalization();
		}
		
		private UiMainWindow mainWindow;
	}

	public SearchTreeNode getSearchTree()
	{
		return grid.getSearchTree();
	}

	public boolean dontSearchTrashFolder()
	{
		return dontSearchTrash.isSelected();
	}

	public void setDontSearchTrashFolder(boolean searchFinalOnly)
	{
		dontSearchTrash.setSelected(searchFinalOnly);
	}

	public boolean searchFinalBulletinsOnly()
	{
		return searchFinalBulletins.isSelected();
	}
	
	public void setSearchFinalBulletinsOnly(boolean searchFinalOnly)
	{
		searchFinalBulletins.setSelected(searchFinalOnly);
	}
	
	public boolean searchSameRowsOnly()
	{
		return searchSameRowsOnly.isSelected();
	}
	
	public void setSearchSameRowsOnly(boolean sameRowsOnly)
	{
		searchSameRowsOnly.setSelected(sameRowsOnly);
	}
	
	public JSONObject getSearchAsJson()
	{
		try
		{
			return grid.getSearchAsJson();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new JSONObject();
		}
	}
	
	public void setSearchAsJson(JSONObject searchGrid)
	{
		grid.setFromJson(searchGrid);
		grid.getTable().setRowSelectionInterval(0,0);
	}
	
	public MartusCrypto getSecurity()
	{
		return getMainWindow().getApp().getSecurity();
	}
	
	class SearchButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			result = true;
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
	
	static class SaveButtonHandler implements ActionListener
	{
		public SaveButtonHandler(UiFancySearchDialogContents dialogToSaveFrom)
		{
			dialog = dialogToSaveFrom;
		}
		
		public void actionPerformed(ActionEvent event)
		{
			UiLocalization localization = dialog.getLocalization();
			FormatFilter filter = new SearchSpecFilter(localization);
			// NOTE: If we pass the frame, the user will still be able to click on 
			// the fancy search dialog, possibly hiding the modal file save dialog.
			// If we pass the dialog, Java will STILL set the owner to the frame.
			// So we must hide the fancy search dialog while anything modal is above it
			dialog.setVisible(false);
			try
			{
				File saveTo = dialog.getMainWindow().showFileSaveDialog("SaveSearch", filter);
				if(saveTo == null)
					return;
				
				try
				{
					save(saveTo);
				} 
				catch (Exception e)
				{
					e.printStackTrace();
					dialog.getMainWindow().notifyDlg("ErrorWritingFile");
				}
			}
			finally
			{
				dialog.setVisible(true);
			}
		}
		
		void save(File destination) throws Exception
		{
			String text = dialog.getSearchSpec().toJson().toString();
			dialog.getSecurity().saveEncryptedStringToFile(destination, text);
		}
		
		UiFancySearchDialogContents dialog;
	}
	
	static class LoadButtonHandler implements ActionListener
	{
		public LoadButtonHandler(UiFancySearchDialogContents dialogToLoadInto)
		{
			dialog = dialogToLoadInto;
		}
		
		public void actionPerformed(ActionEvent event)
		{
			UiLocalization localization = dialog.getLocalization();
			String title = localization.getWindowTitle("LoadSavedSearch");
			String openButtonLabel = localization.getButtonLabel("LoadSearchOkButton");
			File directory = dialog.getMainWindow().getApp().getCurrentAccountDirectory();
			FormatFilter filter = new SearchSpecFilter(localization);
			// NOTE: If we pass the frame, the user will still be able to click on 
			// the fancy search dialog, possibly hiding the modal file save dialog.
			// If we pass the dialog, Java will STILL set the owner to the frame.
			// So we must hide the fancy search dialog while anything modal is above it
			dialog.setVisible(false);
			try
			{
				File loadFrom = FileDialogHelpers.doFileOpenDialog(dialog, title, openButtonLabel, directory, filter);
				if(loadFrom == null)
					return;
				
				try
				{
					SearchSpec spec = load(loadFrom);
					dialog.setSearchAsJson(spec.getSearchGrid());
					dialog.setSearchFinalBulletinsOnly(spec.getFinalOnly());
					dialog.setSearchSameRowsOnly(spec.getSameRowsOnly());
				} 
				catch (Exception e)
				{
					e.printStackTrace();
					dialog.getMainWindow().notifyDlg("ErrorReadingFile");
				}
			}
			finally
			{
				dialog.setVisible(true);
			}
		}
		
		SearchSpec load(File loadFrom) throws Exception
		{
			String text = dialog.getSecurity().loadEncryptedStringFromFile(loadFrom);
			return new SearchSpec(new JSONObject(text));
		}
		
		UiFancySearchDialogContents dialog;
	}
	
	static class SearchSpecFilter extends FormatFilter
	{
		public SearchSpecFilter(MiniLocalization localization)
		{
			description = localization.getFieldLabel("MartusSearchSpecFileFilter");
		}
		public String getExtension()
		{
			return ".mss";
		}

		public String getDescription()
		{
			return description;
		}
		
		String description;
	}
	
	SearchSpec getSearchSpec()
	{
		return new SearchSpec(getSearchAsJson(), searchFinalBulletinsOnly(), searchSameRowsOnly());
	}
	
	public boolean getResults()
	{
		return result;
	}

	boolean result;
	FancySearchGridEditor grid;
	UiCheckBox dontSearchTrash;
	UiCheckBox searchFinalBulletins;
	UiCheckBox searchSameRowsOnly;
}
