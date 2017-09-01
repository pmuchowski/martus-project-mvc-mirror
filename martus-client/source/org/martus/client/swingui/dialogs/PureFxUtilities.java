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

import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.martus.clientside.UiLocalization;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MiniLocalization;

import javafx.scene.control.ButtonType;

public class PureFxUtilities
{
	public static void notifyDlg(UiLocalization localization, JFrame parent, String baseTag)
	{
		HashMap emptyTokenReplacement = new HashMap();
		notifyDlg(localization, parent, baseTag, "notify" + baseTag, emptyTokenReplacement);
	}

	public static void notifyDlg(UiLocalization localization, JFrame parent, String baseTag, String titleTag, Map tokenReplacement)
	{
		String title = localization.getWindowTitle(titleTag);
		String cause = localization.getFieldLabel("notify" + baseTag + "cause");
		String ok = localization.getButtonLabel(EnglishCommonStrings.OK);
		String[] contents = {cause};
		String[] buttons = {ok};

		createNotifyDlg(parent, title, contents, buttons, tokenReplacement);
	}

	public static void messageDlg(UiLocalization localization, Frame parent, String baseTag, String message, Map tokenReplacement)
	{
		String title = localization.getWindowTitle(baseTag);
		String cause = localization.getFieldLabel("message" + baseTag + "cause");
		String ok = localization.getButtonLabel(EnglishCommonStrings.OK);
		String[] contents = {cause, "", message};
		String[] buttons = {ok};

		createNotifyDlg(parent, title, contents, buttons, tokenReplacement);
	}

	public static boolean confirmDlg(UiLocalization localization, Frame parent, String baseTag)
	{
		HashMap emptyTokenReplacement = new HashMap();
		return confirmDlg(localization, parent, baseTag, emptyTokenReplacement);
	}

	public static boolean confirmDlg(UiLocalization localization, Frame parent, String baseTag, Map tokenReplacement)
	{
		String title = localization.getWindowTitle("confirm" + baseTag);
		String cause = localization.getFieldLabel("confirm" + baseTag + "cause");
		String effect = localization.getFieldLabel("confirm" + baseTag + "effect");
		String question = getConfirmQuestionText(localization);
		String[] contents = {cause, "", effect, "", question};
		return confirmDlg(localization, parent, title, contents, tokenReplacement);
	}

	public static String getConfirmQuestionText(UiLocalization localization)
	{
		String question = localization.getFieldLabel("confirmquestion");
		return question;
	}

	public static boolean confirmDlg(MiniLocalization localization, Frame parent, String title, String[] contents)
	{
		HashMap emptyTokenReplacement = new HashMap();
		return confirmDlg(localization, parent, title, contents, emptyTokenReplacement);
	}

	public static boolean confirmDlg(MiniLocalization localization, Frame parent, String title, String[] contents, Map tokenReplacement)
	{
		String[] buttons = getConfirmDialogButtons(localization);

		return confirmDlg(parent, title, contents, buttons, tokenReplacement);
	}

	public static String[] getConfirmDialogButtons(MiniLocalization localization)
	{
		String yes = localization.getButtonLabel(EnglishCommonStrings.YES);
		String no = localization.getButtonLabel(EnglishCommonStrings.NO);
		String[] buttons = {yes, no};
		return buttons;
	}

	public static boolean confirmDlg(Frame parent, String title, String[] contents, String[] buttons)
	{
		HashMap emptyTokenReplacement = new HashMap();
		return confirmDlg(parent, title, contents, buttons, emptyTokenReplacement);
	}

	public static boolean confirmDlg(Frame parent, String title, String[] contents, String[] buttons, Map tokenReplacement)
	{
		PureFxNotifyDlg notify = createNotifyDlg(parent, title, contents, buttons, tokenReplacement);
		ButtonType result = notify.getResult();

		if(result == null)
			return false;

		return !result.getButtonData().isCancelButton();
	}

	private static PureFxNotifyDlg createNotifyDlg(Frame parent, String title, String[] contents, String[] buttons, Map tokenReplacement)
	{
		return new PureFxNotifyDlg(title, contents, buttons, tokenReplacement);
	}
}
