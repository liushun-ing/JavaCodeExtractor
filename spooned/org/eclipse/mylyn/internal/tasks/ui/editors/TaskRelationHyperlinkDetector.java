/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractTaskHyperlinkDetector;
import org.eclipse.mylyn.tasks.ui.TaskHyperlink;
/**
 *
 * @author Steffen Pingel
 */
public class TaskRelationHyperlinkDetector extends org.eclipse.mylyn.tasks.ui.AbstractTaskHyperlinkDetector {
    private static java.util.regex.Pattern HYPERLINK_PATTERN = java.util.regex.Pattern.compile("([^\\s,]+)");// $NON-NLS-1$


    @java.lang.Override
    public org.eclipse.jface.text.hyperlink.IHyperlink[] detectHyperlinks(org.eclipse.jface.text.ITextViewer textViewer, org.eclipse.jface.text.IRegion region, boolean canShowMultipleHyperlinks) {
        if (region.getLength() > 0) {
            return super.detectHyperlinks(textViewer, region, canShowMultipleHyperlinks);
        } else {
            if (textViewer.getDocument() == null) {
                return null;
            }
            org.eclipse.mylyn.tasks.core.TaskRepository taskRepository = getTaskRepository(textViewer);
            if (taskRepository != null) {
                java.lang.String prefix = extractPrefix(textViewer, region.getOffset());
                java.lang.String postfix = extractPostfix(textViewer, region.getOffset());
                java.lang.String taskKey = prefix + postfix;
                if (taskKey.length() > 0) {
                    org.eclipse.jface.text.Region hyperlinkRegion = new org.eclipse.jface.text.Region(region.getOffset() - prefix.length(), taskKey.length());
                    return new org.eclipse.jface.text.hyperlink.IHyperlink[]{ new org.eclipse.mylyn.tasks.ui.TaskHyperlink(hyperlinkRegion, taskRepository, taskKey) };
                }
            }
        }
        return null;
    }

    @java.lang.Override
    protected java.util.List<org.eclipse.jface.text.hyperlink.IHyperlink> detectHyperlinks(org.eclipse.jface.text.ITextViewer textViewer, java.lang.String content, int index, int contentOffset) {
        java.util.List<org.eclipse.jface.text.hyperlink.IHyperlink> links = null;
        for (org.eclipse.mylyn.tasks.core.TaskRepository repository : getTaskRepositories(textViewer)) {
            java.util.regex.Matcher m = org.eclipse.mylyn.internal.tasks.ui.editors.TaskRelationHyperlinkDetector.HYPERLINK_PATTERN.matcher(content);
            while (m.find()) {
                if (links == null) {
                    links = new java.util.ArrayList<org.eclipse.jface.text.hyperlink.IHyperlink>();
                }
                org.eclipse.jface.text.Region region = new org.eclipse.jface.text.Region(contentOffset + m.start(), m.end() - m.start());
                links.add(new org.eclipse.mylyn.tasks.ui.TaskHyperlink(region, repository, m.group()));
            } 
        }
        return links;
    }

    private java.lang.String extractPrefix(org.eclipse.jface.text.ITextViewer viewer, int offset) {
        int i = offset;
        org.eclipse.jface.text.IDocument document = viewer.getDocument();
        if (i > document.getLength()) {
            return "";// $NON-NLS-1$

        }
        try {
            if (isSeparator(document.getChar(i))) {
                return "";// $NON-NLS-1$

            }
            while (i > 0) {
                char ch = document.getChar(i - 1);
                if (isSeparator(ch)) {
                    break;
                }
                i--;
            } 
            return document.get(i, offset - i);
        } catch (org.eclipse.jface.text.BadLocationException e) {
            return "";// $NON-NLS-1$

        }
    }

    private java.lang.String extractPostfix(org.eclipse.jface.text.ITextViewer viewer, int offset) {
        int i = offset;
        org.eclipse.jface.text.IDocument document = viewer.getDocument();
        int length = document.getLength();
        if (i > length) {
            return "";// $NON-NLS-1$

        }
        try {
            if (isSeparator(document.getChar(i))) {
                return "";// $NON-NLS-1$

            }
            while (i < (length - 1)) {
                char ch = document.getChar(i + 1);
                if (isSeparator(ch)) {
                    break;
                }
                i++;
            } 
            return document.get(offset, (i - offset) + 1);
        } catch (org.eclipse.jface.text.BadLocationException e) {
        }
        return "";// $NON-NLS-1$

    }

    private boolean isSeparator(char ch) {
        return java.lang.Character.isWhitespace(ch) || (ch == ',');
    }
}