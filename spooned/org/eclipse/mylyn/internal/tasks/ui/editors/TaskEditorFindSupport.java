/**
 * *****************************************************************************
 * Copyright (c) 2013, 2014 Tasktop Technologies and others.
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
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 * Adds support for finding text to the task editor.
 *
 * @author Jingwen Ou
 * @author Lily Guo
 * @author Sam Davis
 */
public class TaskEditorFindSupport {
    private org.eclipse.jface.action.Action toggleFindAction;

    private static final org.eclipse.swt.graphics.Color HIGHLIGHTER_YELLOW = new org.eclipse.swt.graphics.Color(org.eclipse.swt.widgets.Display.getDefault(), 255, 238, 99);

    private static final org.eclipse.swt.graphics.Color ERROR_NO_RESULT = new org.eclipse.swt.graphics.Color(org.eclipse.swt.widgets.Display.getDefault(), 255, 150, 150);

    private final java.util.List<org.eclipse.swt.custom.StyledText> styledTexts = new java.util.ArrayList<org.eclipse.swt.custom.StyledText>();

    private final java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer> commentGroupViewers = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer>();

    private final org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage;

    public TaskEditorFindSupport(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage page) {
        org.eclipse.core.runtime.Assert.isNotNull(page);
        this.taskEditorPage = page;
    }

    public void toggleFind() {
        if (toggleFindAction != null) {
            toggleFindAction.setChecked(!toggleFindAction.isChecked());
            toggleFindAction.run();
        }
    }

    public void addFindAction(org.eclipse.jface.action.IToolBarManager toolBarManager) {
        if ((toggleFindAction != null) && toggleFindAction.isChecked()) {
            org.eclipse.jface.action.ControlContribution findTextboxControl = new org.eclipse.jface.action.ControlContribution(Messages.TaskEditorFindSupport_Find) {
                @java.lang.Override
                protected org.eclipse.swt.widgets.Control createControl(org.eclipse.swt.widgets.Composite parent) {
                    org.eclipse.ui.forms.widgets.FormToolkit toolkit = taskEditorPage.getEditor().getHeaderForm().getToolkit();
                    final org.eclipse.swt.widgets.Composite findComposite = toolkit.createComposite(parent);
                    org.eclipse.swt.layout.GridLayout findLayout = new org.eclipse.swt.layout.GridLayout();
                    findLayout.marginHeight = 4;
                    findComposite.setLayout(findLayout);
                    findComposite.setBackground(null);
                    final org.eclipse.swt.widgets.Text findText = toolkit.createText(findComposite, "", org.eclipse.swt.SWT.FLAT);// $NON-NLS-1$

                    findText.setLayoutData(new org.eclipse.swt.layout.GridData(100, org.eclipse.swt.SWT.DEFAULT));
                    findText.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TEXT_BORDER);
                    findText.setFocus();
                    toolkit.adapt(findText, false, false);
                    findText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
                        @java.lang.Override
                        public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                            if (findText.getText().equals("")) {
                                // $NON-NLS-1$
                                clearSearchResults();
                                findText.setBackground(null);
                            }
                        }
                    });
                    findText.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                        @java.lang.Override
                        public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent event) {
                            searchTaskEditor(findText);
                        }
                    });
                    toolkit.paintBordersFor(findComposite);
                    return findComposite;
                }
            };
            toolBarManager.appendToGroup(org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS, findTextboxControl);
        }
        if (toggleFindAction == null) {
            toggleFindAction = new org.eclipse.jface.action.Action("", org.eclipse.swt.SWT.TOGGLE) {
                // $NON-NLS-1$
                @java.lang.Override
                public void run() {
                    if (!this.isChecked()) {
                        clearSearchResults();
                    }
                    taskEditorPage.getEditor().updateHeaderToolBar();
                }
            };
            toggleFindAction.setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.FIND);
            toggleFindAction.setToolTipText(Messages.TaskEditorFindSupport_Find);
        }
        toolBarManager.appendToGroup(org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS, toggleFindAction);
    }

    protected void searchTaskEditor(final org.eclipse.swt.widgets.Text findBox) {
        try {
            taskEditorPage.setReflow(false);
            findBox.setBackground(null);
            if (findBox.getText().equals("")) {
                // $NON-NLS-1$
                return;
            }
            clearSearchResults();
            java.lang.String searchString = findBox.getText().toLowerCase();
            for (org.eclipse.ui.forms.IFormPart part : taskEditorPage.getManagedForm().getParts()) {
                if (!(part instanceof org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart)) {
                    continue;
                }
                org.eclipse.swt.widgets.Control control = ((org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart) (part)).getControl();
                if (part instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorSummaryPart) {
                    if (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.contains(taskEditorPage.getModel().getTaskData(), org.eclipse.mylyn.tasks.core.data.TaskAttribute.SUMMARY, searchString)) {
                        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.gatherStyledTexts(control, styledTexts);
                    }
                } else if (part instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPlanningPart) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor noteEditor = ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPlanningPart) (part)).getPlanningPart().getNoteEditor();
                    if (((noteEditor != null) && (noteEditor.getText() != null)) && noteEditor.getText().toLowerCase().contains(searchString)) {
                        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.gatherStyledTexts(control, styledTexts);
                    }
                } else if (part instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorDescriptionPart) {
                    if (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.contains(taskEditorPage.getModel().getTaskData(), org.eclipse.mylyn.tasks.core.data.TaskAttribute.DESCRIPTION, searchString)) {
                        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.gatherStyledTexts(control, styledTexts);
                    }
                } else if (part instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart) {
                    commentGroupViewers.clear();
                    commentGroupViewers.addAll(((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart) (part)).getCommentGroupViewers());
                    searchCommentPart(searchString, ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart) (part)), commentGroupViewers, styledTexts);
                }
            }
            for (org.eclipse.swt.custom.StyledText styledText : styledTexts) {
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.highlightMatches(searchString, styledText);
            }
            if (styledTexts.isEmpty()) {
                findBox.setBackground(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.ERROR_NO_RESULT);
            }
        } finally {
            taskEditorPage.setReflow(true);
        }
        taskEditorPage.reflow();
        findBox.setFocus();
    }

    protected static boolean contains(org.eclipse.mylyn.tasks.core.data.TaskData taskData, java.lang.String attributeId, java.lang.String searchString) {
        org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute = taskData.getRoot().getMappedAttribute(attributeId);
        if (attribute != null) {
            return attribute.getValue().toLowerCase().contains(searchString);
        }
        return false;
    }

    private void searchCommentPart(final java.lang.String searchString, final org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart part, java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer> commentGroupViewers, final java.util.List<org.eclipse.swt.custom.StyledText> styledTexts) {
        org.eclipse.mylyn.tasks.core.data.TaskData taskData = taskEditorPage.getModel().getTaskData();
        java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> commentAttributes = taskData.getAttributeMapper().getAttributesByType(taskData, org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT);
        if (!org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.anyCommentContains(commentAttributes, searchString)) {
            return;
        }
        if (!part.isCommentSectionExpanded()) {
            try {
                part.setReflow(false);
                part.expandAllComments(false);
            } finally {
                part.setReflow(true);
            }
        }
        int end = commentAttributes.size();
        boolean expandMatchingGroup = true;
        for (int i = commentGroupViewers.size() - 1; i >= 0; i--) {
            final org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer group = commentGroupViewers.get(i);
            java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer> commentViewers = group.getCommentViewers();
            int start = end - commentViewers.size();
            java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> groupAttributes = commentAttributes.subList(start, end);
            if (expandMatchingGroup && org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.anyCommentContains(groupAttributes, searchString)) {
                if (!group.isExpanded()) {
                    try {
                        part.setReflow(false);
                        group.setExpanded(true);
                    } finally {
                        part.setReflow(true);
                    }
                }
                // once we've seen a matching group, don't expand any more groups
                expandMatchingGroup = false;
            }
            final java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer> matchingViewers = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.searchComments(groupAttributes, commentViewers, searchString);
            if ((!group.isRenderedInSubSection()) || group.isExpanded()) {
                try {
                    part.setReflow(false);
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.gatherStyledTexts(matchingViewers, styledTexts);
                } finally {
                    part.setReflow(true);
                }
                group.clearSectionHyperlink();
            } else if (!matchingViewers.isEmpty()) {
                addShowMoreLink(group, matchingViewers, part, searchString, styledTexts);
            } else {
                group.clearSectionHyperlink();
            }
            end = start;
        }
    }

    protected void addShowMoreLink(final org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer group, final java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer> matchingViewers, final org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart part, final java.lang.String searchString, final java.util.List<org.eclipse.swt.custom.StyledText> styledTexts) {
        org.eclipse.ui.forms.events.HyperlinkAdapter listener = new org.eclipse.ui.forms.events.HyperlinkAdapter() {
            @java.lang.Override
            public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                java.util.List<org.eclipse.swt.custom.StyledText> commentStyledTexts = new java.util.ArrayList<org.eclipse.swt.custom.StyledText>();
                try {
                    taskEditorPage.setReflow(false);
                    part.setReflow(false);
                    group.setExpanded(true);
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.gatherStyledTexts(matchingViewers, commentStyledTexts);
                } finally {
                    taskEditorPage.setReflow(true);
                    part.setReflow(true);
                }
                for (org.eclipse.swt.custom.StyledText styledText : commentStyledTexts) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.highlightMatches(searchString, styledText);
                    styledTexts.add(styledText);
                }
                group.clearSectionHyperlink();
                taskEditorPage.reflow();
            }
        };
        group.createSectionHyperlink(org.eclipse.osgi.util.NLS.bind(Messages.TaskEditorFindSupport_Show_X_more_results, matchingViewers.size()), listener);
    }

    private static boolean anyCommentContains(java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> commentAttributes, java.lang.String text) {
        for (org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute : commentAttributes) {
            if (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.commentContains(commentAttribute, text)) {
                return true;
            }
        }
        return false;
    }

    private static boolean commentContains(org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute, java.lang.String searchString) {
        org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute = commentAttribute.getMappedAttribute(org.eclipse.mylyn.tasks.core.data.TaskAttribute.COMMENT_TEXT);
        return attribute.getValue().toLowerCase().contains(searchString);
    }

    private static java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer> searchComments(java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> commentAttributes, java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer> commentViewers, java.lang.String searchString) {
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer> matchingViewers = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer>();
        for (int i = 0; i < commentViewers.size(); i++) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer viewer = commentViewers.get(i);
            if (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.commentContains(commentAttributes.get(i), searchString)) {
                matchingViewers.add(viewer);
            }
        }
        return matchingViewers;
    }

    protected static void gatherStyledTexts(java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer> commentViewers, java.util.List<org.eclipse.swt.custom.StyledText> styledTexts) {
        for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer viewer : commentViewers) {
            try {
                org.eclipse.ui.forms.widgets.ExpandableComposite composite = ((org.eclipse.ui.forms.widgets.ExpandableComposite) (viewer.getControl()));
                viewer.suppressSelectionChanged(true);
                if ((composite != null) && (!composite.isExpanded())) {
                    org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(composite, true);
                }
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.gatherStyledTextsInComposite(composite, styledTexts);
            } finally {
                viewer.suppressSelectionChanged(false);
            }
        }
    }

    private static void gatherStyledTexts(org.eclipse.swt.widgets.Control control, java.util.List<org.eclipse.swt.custom.StyledText> result) {
        if (control instanceof org.eclipse.ui.forms.widgets.ExpandableComposite) {
            org.eclipse.ui.forms.widgets.ExpandableComposite composite = ((org.eclipse.ui.forms.widgets.ExpandableComposite) (control));
            if (!composite.isExpanded()) {
                org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(composite, true);
            }
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.gatherStyledTextsInComposite(composite, result);
        } else if (control instanceof org.eclipse.swt.widgets.Composite) {
            org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.gatherStyledTextsInComposite(((org.eclipse.swt.widgets.Composite) (control)), result);
        }
    }

    private static void gatherStyledTextsInComposite(org.eclipse.swt.widgets.Composite composite, java.util.List<org.eclipse.swt.custom.StyledText> result) {
        if ((composite != null) && (!composite.isDisposed())) {
            for (org.eclipse.swt.widgets.Control child : composite.getChildren()) {
                if (child instanceof org.eclipse.swt.custom.StyledText) {
                    result.add(((org.eclipse.swt.custom.StyledText) (child)));
                } else if (child instanceof org.eclipse.swt.widgets.Composite) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.gatherStyledTextsInComposite(((org.eclipse.swt.widgets.Composite) (child)), result);
                }
            }
        }
    }

    private static void highlightMatches(java.lang.String searchString, org.eclipse.swt.custom.StyledText styledText) {
        java.lang.String text = styledText.getText().toLowerCase();
        for (int index = 0; index < text.length(); index += searchString.length()) {
            index = text.indexOf(searchString, index);
            if (index == (-1)) {
                break;
            }
            styledText.setStyleRange(new org.eclipse.swt.custom.StyleRange(index, searchString.length(), null, org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.HIGHLIGHTER_YELLOW));
        }
    }

    private void clearSearchResults() {
        for (org.eclipse.swt.custom.StyledText oldText : styledTexts) {
            java.util.List<org.eclipse.swt.custom.StyleRange> otherRanges = new java.util.ArrayList<org.eclipse.swt.custom.StyleRange>();
            if (!oldText.isDisposed()) {
                for (org.eclipse.swt.custom.StyleRange styleRange : oldText.getStyleRanges()) {
                    if ((styleRange.background == null) || (!styleRange.background.equals(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorFindSupport.HIGHLIGHTER_YELLOW))) {
                        otherRanges.add(styleRange);// preserve ranges that aren't from highlighting search results

                    }
                }
                oldText.setStyleRanges(otherRanges.toArray(new org.eclipse.swt.custom.StyleRange[otherRanges.size()]));
            }
        }
        styledTexts.clear();
        for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer group : commentGroupViewers) {
            group.clearSectionHyperlink();
        }
    }
}