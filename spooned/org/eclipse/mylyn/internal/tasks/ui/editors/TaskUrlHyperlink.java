/**
 * *****************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.mylyn.commons.workbench.browser.UrlHyperlink;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.osgi.util.NLS;
/**
 *
 * @author Rob Elves
 * @deprecated use {@link UrlHyperlink} instead
 */
@java.lang.Deprecated
public class TaskUrlHyperlink extends org.eclipse.jface.text.hyperlink.URLHyperlink {
    private final java.lang.String hyperlinkText;

    public TaskUrlHyperlink(org.eclipse.jface.text.IRegion region, java.lang.String urlString, java.lang.String hyperlinkText) {
        super(region, urlString);
        this.hyperlinkText = hyperlinkText;
    }

    public TaskUrlHyperlink(org.eclipse.jface.text.IRegion region, java.lang.String urlString) {
        this(region, urlString, null);
    }

    @java.lang.Override
    public void open() {
        org.eclipse.mylyn.tasks.ui.TasksUiUtil.openTask(getURLString());
    }

    @java.lang.Override
    public java.lang.String getHyperlinkText() {
        if (hyperlinkText != null) {
            return hyperlinkText;
        }
        return org.eclipse.osgi.util.NLS.bind(Messages.TaskUrlHyperlink_Open_URL_in_Task_Editor, getURLString());
    }
}