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
 *     David Green - fix for bug 266693
 *     Abner Ballardo - fix for bug 288427
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylyn.commons.workbench.browser.UrlHyperlink;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractTaskHyperlinkDetector;
/**
 * Detects URLs based on a regular expression.
 *
 * @author David Green
 */
public class TaskUrlHyperlinkDetector extends org.eclipse.mylyn.tasks.ui.AbstractTaskHyperlinkDetector {
    // based on RFC 3986
    // even though it's valid, the platform hyperlink detector doesn't detect hyperlinks that end with '.', ',' or ')'
    // so we do the same here
    private static final java.util.regex.Pattern URL_PATTERN = java.util.regex.Pattern.compile("([a-zA-Z][a-zA-Z+.-]{0,10}://[a-zA-Z0-9%._~!$&?#'()*+,;:@/=-]*[a-zA-Z0-9%_~!$&?#'(*+;:@/=-])");// $NON-NLS-1$


    private static final java.lang.String CLOSED_PARENTHESIS_PATTERN = "[^)]";// $NON-NLS-1$


    private static final java.lang.String OPEN_PARENTHESIS_PATTERN = "[^(]";// $NON-NLS-1$


    private static final java.lang.String EMPTY_STRING = "";// $NON-NLS-1$


    public TaskUrlHyperlinkDetector() {
    }

    @java.lang.Override
    protected java.util.List<org.eclipse.jface.text.hyperlink.IHyperlink> detectHyperlinks(org.eclipse.jface.text.ITextViewer textViewer, java.lang.String content, int indexInContent, int contentOffset) {
        java.util.List<org.eclipse.jface.text.hyperlink.IHyperlink> links = null;
        java.util.regex.Matcher m = org.eclipse.mylyn.internal.tasks.ui.editors.TaskUrlHyperlinkDetector.URL_PATTERN.matcher(content);
        while (m.find()) {
            if (org.eclipse.mylyn.internal.tasks.ui.editors.TaskUrlHyperlinkDetector.isInRegion(indexInContent, m)) {
                java.lang.String urlString = getUrlString(content, m);
                org.eclipse.jface.text.hyperlink.IHyperlink link = null;
                if (getAdapter(org.eclipse.mylyn.tasks.core.TaskRepository.class) != null) {
                    try {
                        new java.net.URL(urlString);
                        link = org.eclipse.mylyn.internal.tasks.ui.editors.TaskUrlHyperlinkDetector.createTaskUrlHyperlink(contentOffset, m, urlString);
                    } catch (java.net.MalformedURLException e) {
                        // ignore
                    }
                } else if (org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.isTaskUrl(urlString)) {
                    link = org.eclipse.mylyn.internal.tasks.ui.editors.TaskUrlHyperlinkDetector.createTaskUrlHyperlink(contentOffset, m, urlString);
                }
                if (link != null) {
                    if (links == null) {
                        links = new java.util.ArrayList<org.eclipse.jface.text.hyperlink.IHyperlink>();
                    }
                    links.add(link);
                }
            }
        } 
        return links;
    }

    private java.lang.String getUrlString(java.lang.String content, java.util.regex.Matcher m) {
        java.lang.String urlString = m.group(1);
        // check if the urlString has more opening parenthesis than closing
        int parenthesisDiff = urlString.replaceAll(org.eclipse.mylyn.internal.tasks.ui.editors.TaskUrlHyperlinkDetector.OPEN_PARENTHESIS_PATTERN, org.eclipse.mylyn.internal.tasks.ui.editors.TaskUrlHyperlinkDetector.EMPTY_STRING).length() - urlString.replaceAll(org.eclipse.mylyn.internal.tasks.ui.editors.TaskUrlHyperlinkDetector.CLOSED_PARENTHESIS_PATTERN, org.eclipse.mylyn.internal.tasks.ui.editors.TaskUrlHyperlinkDetector.EMPTY_STRING).length();
        if (parenthesisDiff > 0) {
            // if any open paranthesis were not closed assume that trailing closing parenthesis are part of URL
            for (int i = m.end(); (i - m.end()) < parenthesisDiff; i++) {
                if ((i >= content.length()) || (content.charAt(i) != ')')) {
                    break;
                }
                urlString += ')';
            }
        }
        return urlString;
    }

    private static boolean isInRegion(int offsetInText, java.util.regex.Matcher m) {
        return (offsetInText == (-1)) || ((offsetInText >= m.start()) && (offsetInText <= m.end()));
    }

    private static org.eclipse.jface.text.hyperlink.IHyperlink createTaskUrlHyperlink(int textOffset, java.util.regex.Matcher m, java.lang.String urlString) {
        return new org.eclipse.mylyn.commons.workbench.browser.UrlHyperlink(new org.eclipse.jface.text.Region(textOffset + m.start(), urlString.length()), urlString);
    }
}