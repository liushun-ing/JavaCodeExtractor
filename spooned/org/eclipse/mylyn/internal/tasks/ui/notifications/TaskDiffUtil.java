/**
 * *****************************************************************************
 * Copyright (c) 2004, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.notifications;
import org.eclipse.mylyn.internal.tasks.core.data.TaskAttributeDiff;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.data.ITaskAttributeDiff;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
/**
 *
 * @author Steffen Pingel
 */
public class TaskDiffUtil {
    private static final int MAX_CHANGED_ATTRIBUTES = 2;

    // could use the ellipsis glyph on some platforms "\u2026"
    private static final java.lang.String ELLIPSIS = "...";// $NON-NLS-1$


    private static int DRAW_FLAGS = ((org.eclipse.swt.SWT.DRAW_MNEMONIC | org.eclipse.swt.SWT.DRAW_TAB) | org.eclipse.swt.SWT.DRAW_TRANSPARENT) | org.eclipse.swt.SWT.DRAW_DELIMITER;

    public static java.lang.String toString(org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff diff) {
        return org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.toString(diff, true);
    }

    public static java.lang.String toString(org.eclipse.mylyn.internal.tasks.core.data.TaskDataDiff diff, boolean includeNewest) {
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        java.lang.String sep = "";// $NON-NLS-1$

        // append first comment
        int newCommentCount = diff.getNewComments().size();
        if (newCommentCount > 0) {
            java.util.Iterator<org.eclipse.mylyn.tasks.core.ITaskComment> iter = diff.getNewComments().iterator();
            org.eclipse.mylyn.tasks.core.ITaskComment comment = iter.next();
            if (includeNewest) {
                while (iter.hasNext()) {
                    comment = iter.next();
                } 
            }
            sb.append(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.trim(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.commentToString(comment), 60));
            if (newCommentCount > 1) {
                sb.append((" (" + (newCommentCount - 1)) + Messages.TaskDataDiff_more_);// $NON-NLS-1$

            }
            sep = "\n";// $NON-NLS-1$

        }
        // append changed attributes
        int n = 0;
        for (org.eclipse.mylyn.tasks.core.data.ITaskAttributeDiff changedAttribute : diff.getChangedAttributes()) {
            org.eclipse.mylyn.internal.tasks.core.data.TaskAttributeDiff attributeDiff = ((org.eclipse.mylyn.internal.tasks.core.data.TaskAttributeDiff) (changedAttribute));
            java.lang.String label = attributeDiff.getLabel();
            if (label != null) {
                sb.append(sep);
                sb.append(" ");// $NON-NLS-1$

                sb.append(label);
                sb.append(" ");// $NON-NLS-1$

                sb.append(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.trim(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.listToString(attributeDiff.getRemovedValues()), 28));
                sb.append(" -> ");// $NON-NLS-1$

                sb.append(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.trim(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.listToString(attributeDiff.getAddedValues()), 28));
                if ((++n) == org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.MAX_CHANGED_ATTRIBUTES) {
                    break;
                }
                sep = "\n";// $NON-NLS-1$

            }
        }
        return sb.toString();
    }

    public static java.lang.String commentToString(org.eclipse.mylyn.tasks.core.ITaskComment comment) {
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        sb.append(java.text.MessageFormat.format(Messages.TaskDiffUtil_Comment_by_X, org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.personToString(comment.getAuthor())));
        sb.append(": ");// $NON-NLS-1$

        sb.append(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.cleanCommentText(comment.getText()));
        return sb.toString();
    }

    private static java.lang.String personToString(org.eclipse.mylyn.tasks.core.IRepositoryPerson author) {
        if (author == null) {
            return Messages.TaskDiffUtil_Unknown;
        } else if (author.getName() != null) {
            return author.getName();
        }
        return author.getPersonId();
    }

    public static java.lang.String cleanCommentText(java.lang.String value) {
        java.lang.String text = "";// $NON-NLS-1$

        java.lang.String[] lines = value.split("\n");// $NON-NLS-1$

        boolean attachment = false;
        boolean needSeparator = false;
        for (java.lang.String line : lines) {
            // skip comments and info lines
            if (attachment) {
                text += Messages.TaskDiffUtil_attachment + line;
                needSeparator = true;
                attachment = false;
            } else if (line.startsWith(">")// //$NON-NLS-1$
             || line.matches("^\\s*\\(In reply to comment.*")) {
                // $NON-NLS-1$
                needSeparator = true;
                continue;
            } else if (line.startsWith("Created an attachment (id=")) {
                // $NON-NLS-1$
                attachment = true;
            } else {
                if (needSeparator) {
                    if (!text.matches(".*\\p{Punct}\\s*")) {
                        // $NON-NLS-1$
                        text = text.trim();
                        if (text.length() > 0) {
                            text += ".";// $NON-NLS-1$

                        }
                    }
                }
                text += " " + line;// $NON-NLS-1$

                attachment = false;
                needSeparator = false;
            }
        }
        return org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.foldSpaces(text);
    }

    public static java.lang.String listToString(java.util.List<java.lang.String> values) {
        if (values == null) {
            return "";// $NON-NLS-1$

        }
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        boolean first = true;
        for (java.lang.String value : values) {
            if (!first) {
                sb.append(", ");// $NON-NLS-1$

            } else {
                first = false;
            }
            sb.append(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.foldSpaces(value));
        }
        return sb.toString();
    }

    public static java.lang.String foldSpaces(java.lang.String value) {
        return value.replaceAll("\\s+", " ").trim();// $NON-NLS-1$ //$NON-NLS-2$

    }

    public static java.lang.String trim(java.lang.String value, int length) {
        if (value.length() > length) {
            value = value.substring(0, length - 3) + "...";// $NON-NLS-1$

        }
        return value;
    }

    /**
     * Note: Copied from CLabel. Shorten the given text <code>t</code> so that its length doesn't exceed the given
     * width. The default implementation replaces characters in the center of the original string with an ellipsis
     * ("..."). Override if you need a different strategy.
     *
     * @param gc
     * 		the gc to use for text measurement
     * @param t
     * 		the text to shorten
     * @param width
     * 		the width to shorten the text to, in pixels
     * @return the shortened text
     */
    public static final java.lang.String shortenText2(org.eclipse.swt.widgets.Composite composite, java.lang.String t, int width) {
        org.eclipse.swt.graphics.GC gc = new org.eclipse.swt.graphics.GC(composite);
        if (t == null) {
            return null;
        }
        int w = gc.textExtent(org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.ELLIPSIS, org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.DRAW_FLAGS).x;
        if (width <= w) {
            return t;
        }
        int l = t.length();
        int max = l / 2;
        int min = 0;
        int mid = ((max + min) / 2) - 1;
        if (mid <= 0) {
            return t;
        }
        while ((min < mid) && (mid < max)) {
            java.lang.String s1 = t.substring(0, mid);
            java.lang.String s2 = t.substring(l - mid, l);
            int l1 = gc.textExtent(s1, org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.DRAW_FLAGS).x;
            int l2 = gc.textExtent(s2, org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.DRAW_FLAGS).x;
            if (((l1 + w) + l2) > width) {
                max = mid;
                mid = (max + min) / 2;
            } else if (((l1 + w) + l2) < width) {
                min = mid;
                mid = (max + min) / 2;
            } else {
                min = max;
            }
        } 
        gc.dispose();
        if (mid == 0) {
            return t;
        }
        return (t.substring(0, mid) + org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil.ELLIPSIS) + t.substring(l - mid, l);
    }

    // From PerspectiveBarContributionItem
    public static java.lang.String shortenText(org.eclipse.swt.graphics.Drawable composite, java.lang.String text, int maxWidth) {
        java.lang.String returnText = text;
        org.eclipse.swt.graphics.GC gc = new org.eclipse.swt.graphics.GC(composite);
        if (gc.textExtent(text).x > maxWidth) {
            for (int i = text.length(); i > 0; i--) {
                java.lang.String subString = text.substring(0, i);
                subString = subString + "...";// $NON-NLS-1$

                if (gc.textExtent(subString).x < maxWidth) {
                    returnText = subString;
                    break;
                }
            }
        }
        gc.dispose();
        return returnText;
    }
}