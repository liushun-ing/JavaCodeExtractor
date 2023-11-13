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
 *     Frank Becker - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
/**
 * A hyperlink presenter that displays a tooltip when hovering over a {@link TaskHyperlink}.
 *
 * @author Steffen Pingel
 * @author Frank Becker
 * @since 3.1
 */
public final class TaskHyperlinkPresenter extends org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter {
    private org.eclipse.jface.text.IRegion activeRegion;

    /**
     * Stores which task a tooltip is being displayed for. It is used to avoid having the same tooltip being set
     * multiple times while you move the mouse over a task hyperlink (bug#209409)
     */
    private org.eclipse.mylyn.tasks.core.ITask currentTask;

    private org.eclipse.mylyn.tasks.ui.TaskHyperlink currentTaskHyperlink;

    private org.eclipse.jface.text.ITextViewer textViewer;

    private java.lang.String oldToolTip;

    private boolean restoreToolTip;

    // TODO e3.7 remove all references to delegate and replace with calls to super methods
    private final org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter delegate;

    private boolean errorLogged;

    /**
     *
     * @see DefaultHyperlinkPresenter#DefaultHyperlinkPresenter(IPreferenceStore)
     */
    public TaskHyperlinkPresenter(org.eclipse.jface.preference.IPreferenceStore store) {
        super(store);
        delegate = new org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter(store);
    }

    /**
     *
     * @see DefaultHyperlinkPresenter#DefaultHyperlinkPresenter(RGB)
     */
    public TaskHyperlinkPresenter(org.eclipse.swt.graphics.RGB color) {
        super(color);
        delegate = new org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter(color);
    }

    @java.lang.Override
    public void install(org.eclipse.jface.text.ITextViewer textViewer) {
        this.textViewer = textViewer;
        delegate.install(textViewer);
    }

    @java.lang.Override
    public void uninstall() {
        hideHyperlinks();
        this.textViewer = null;
        delegate.uninstall();
    }

    @java.lang.SuppressWarnings("unchecked")
    @java.lang.Override
    public void applyTextPresentation(org.eclipse.jface.text.TextPresentation textPresentation) {
        delegate.applyTextPresentation(textPresentation);
        // decorate hyperlink as strike-through if task is completed, this is now also handled by TaskHyperlinkTextPresentationManager
        if (((activeRegion != null) && (currentTask != null)) && currentTask.isCompleted()) {
            java.util.Iterator<org.eclipse.swt.custom.StyleRange> styleRangeIterator = textPresentation.getAllStyleRangeIterator();
            while (styleRangeIterator.hasNext()) {
                org.eclipse.swt.custom.StyleRange styleRange = styleRangeIterator.next();
                if ((activeRegion.getOffset() == styleRange.start) && (activeRegion.getLength() == styleRange.length)) {
                    styleRange.strikeout = true;
                    break;
                }
            } 
        }
    }

    // TODO e3.7 remove method
    @java.lang.Override
    public void showHyperlinks(org.eclipse.jface.text.hyperlink.IHyperlink[] hyperlinks) {
        showHyperlinks(hyperlinks, false);
    }

    // TODO e3.7 add @Override annotation
    public void showHyperlinks(org.eclipse.jface.text.hyperlink.IHyperlink[] hyperlinks, boolean takesFocusWhenVisible) {
        activeRegion = null;
        // show task name in tooltip
        if ((hyperlinks.length == 1) && (hyperlinks[0] instanceof org.eclipse.mylyn.tasks.ui.TaskHyperlink)) {
            org.eclipse.mylyn.tasks.ui.TaskHyperlink hyperlink = ((org.eclipse.mylyn.tasks.ui.TaskHyperlink) (hyperlinks[0]));
            org.eclipse.mylyn.internal.tasks.core.TaskList taskList = org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.getTaskList();
            java.lang.String repositoryUrl = hyperlink.getRepository().getRepositoryUrl();
            org.eclipse.mylyn.tasks.core.ITask task = taskList.getTask(repositoryUrl, hyperlink.getTaskId());
            if (task == null) {
                task = taskList.getTaskByKey(repositoryUrl, hyperlink.getTaskId());
            }
            if (!hyperlinks[0].equals(currentTaskHyperlink)) {
                currentTaskHyperlink = ((org.eclipse.mylyn.tasks.ui.TaskHyperlink) (hyperlinks[0]));
                currentTask = task;
                activeRegion = hyperlink.getHyperlinkRegion();
                if (((textViewer != null) && (textViewer.getTextWidget() != null)) && (!textViewer.getTextWidget().isDisposed())) {
                    oldToolTip = textViewer.getTextWidget().getToolTipText();
                    restoreToolTip = true;
                    if (task == null) {
                        java.lang.String taskLabel = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskPrefix(hyperlink.getRepository().getConnectorKind());
                        taskLabel += currentTaskHyperlink.getTaskId();
                        textViewer.getTextWidget().setToolTipText(org.eclipse.osgi.util.NLS.bind(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskHyperlinkPresenter_Not_In_Task_List, taskLabel));
                    } else if (task.getTaskKey() == null) {
                        textViewer.getTextWidget().setToolTipText(task.getSummary());
                    } else {
                        textViewer.getTextWidget().setToolTipText((task.getTaskKey() + ": ") + task.getSummary());// $NON-NLS-1$

                    }
                }
            }
        }
        // invoke super implementation
        try {
            // Eclipse 3.7
            java.lang.reflect.Method method = // $NON-NLS-1$
            org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter.class.getDeclaredMethod("showHyperlinks", org.eclipse.jface.text.hyperlink.IHyperlink[].class, boolean.class);
            method.invoke(delegate, hyperlinks, takesFocusWhenVisible);
        } catch (java.lang.NoSuchMethodException e) {
            // Eclipse 3.6 and earlier
            delegate.showHyperlinks(hyperlinks);
        } catch (java.lang.Exception e) {
            if (!errorLogged) {
                errorLogged = true;
                org.eclipse.mylyn.commons.core.StatusHandler.log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin.ID_PLUGIN, "Unexpected error while displaying hyperlink", e));// $NON-NLS-1$

            }
        }
    }

    @java.lang.Override
    public void hideHyperlinks() {
        if (currentTaskHyperlink != null) {
            if (((restoreToolTip && (textViewer != null)) && (textViewer.getTextWidget() != null)) && (!textViewer.getTextWidget().isDisposed())) {
                textViewer.getTextWidget().setToolTipText(oldToolTip);
                restoreToolTip = false;
            }
            currentTaskHyperlink = null;
            currentTask = null;
        }
        delegate.hideHyperlinks();
    }

    @java.lang.Override
    public boolean canHideHyperlinks() {
        return delegate.canHideHyperlinks();
    }

    @java.lang.Override
    public boolean canShowMultipleHyperlinks() {
        return delegate.canShowMultipleHyperlinks();
    }

    @java.lang.Override
    public void documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent event) {
        delegate.documentAboutToBeChanged(event);
    }

    @java.lang.Override
    public void inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument oldInput, org.eclipse.jface.text.IDocument newInput) {
        delegate.inputDocumentAboutToBeChanged(oldInput, newInput);
    }

    @java.lang.Override
    public void inputDocumentChanged(org.eclipse.jface.text.IDocument oldInput, org.eclipse.jface.text.IDocument newInput) {
        delegate.inputDocumentChanged(oldInput, newInput);
    }

    @java.lang.Override
    public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
        delegate.propertyChange(event);
    }

    @java.lang.Override
    public void setColor(org.eclipse.swt.graphics.Color color) {
        delegate.setColor(color);
    }
}