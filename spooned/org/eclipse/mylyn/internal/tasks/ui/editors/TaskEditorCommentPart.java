/**
 * *****************************************************************************
 * Copyright (c) 2004, 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Jingwen Ou - comment grouping
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.ui.FillWidthLayout;
import org.eclipse.mylyn.commons.ui.SelectionProviderAdapter;
import org.eclipse.mylyn.commons.ui.compatibility.CommonColors;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.commons.workbench.forms.ScalingHyperlink;
import org.eclipse.mylyn.internal.tasks.core.TaskComment;
import org.eclipse.mylyn.internal.tasks.ui.actions.CommentActionGroup;
import org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;
/**
 *
 * @author Robert Elves
 * @author Steffen Pingel
 * @author Jingwen Ou
 */
public class TaskEditorCommentPart extends org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart {
    private static final java.lang.String ID_POPUP_MENU = "org.eclipse.mylyn.tasks.ui.editor.menu.comments";// $NON-NLS-1$


    // private void createToolBar(final FormToolkit toolkit) {
    // if (section == null) {
    // return;
    // }
    // 
    // ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
    // 
    // Action collapseAllAction = new Action("") {
    // @Override
    // public void run() {
    // toggleSection(section, false);
    // }
    // };
    // collapseAllAction.setImageDescriptor(CommonImages.COLLAPSE_ALL_SMALL);
    // collapseAllAction.setToolTipText("Collapse All Current Comments");
    // toolBarManager.add(collapseAllAction);
    // 
    // Action expandAllAction = new Action("") {
    // @Override
    // public void run() {
    // toggleSection(section, true);
    // }
    // };
    // expandAllAction.setImageDescriptor(CommonImages.EXPAND_ALL_SMALL);
    // expandAllAction.setToolTipText("Expand All Current Comments");
    // toolBarManager.add(expandAllAction);
    // 
    // Composite toolbarComposite = toolkit.createComposite(section);
    // toolbarComposite.setBackground(null);
    // RowLayout rowLayout = new RowLayout();
    // rowLayout.marginTop = 0;
    // rowLayout.marginBottom = 0;
    // rowLayout.marginLeft = 0;
    // rowLayout.marginRight = 0;
    // toolbarComposite.setLayout(rowLayout);
    // 
    // toolBarManager.createControl(toolbarComposite);
    // section.setTextClient(toolbarComposite);
    // }
    public class CommentGroupViewer {
        private final org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup commentGroup;

        private java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer> commentViewers;

        private org.eclipse.ui.forms.widgets.Section groupSection;

        private boolean renderedInSubSection;

        public CommentGroupViewer(org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup commentGroup) {
            this.commentGroup = commentGroup;
        }

        private org.eclipse.swt.widgets.Composite createCommentViewers(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
            java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer> viewers = getCommentViewers();
            org.eclipse.swt.widgets.Composite composite = toolkit.createComposite(parent);
            org.eclipse.swt.layout.GridLayout contentLayout = new org.eclipse.swt.layout.GridLayout();
            contentLayout.marginHeight = 0;
            contentLayout.marginWidth = 0;
            composite.setLayout(contentLayout);
            for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer commentViewer : viewers) {
                org.eclipse.swt.widgets.Control control = commentViewer.createControl(composite, toolkit);
                org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).applyTo(control);
            }
            return composite;
        }

        private org.eclipse.swt.widgets.Control createControl(final org.eclipse.swt.widgets.Composite parent, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
            if (renderedInSubSection) {
                return createSection(parent, toolkit);
            } else {
                if (TaskEditorCommentPart.this.commentAttributes.size() >= CommentGroupStrategy.MAX_CURRENT) {
                    // show a separator before current comments
                    final org.eclipse.swt.widgets.Canvas separator = new org.eclipse.swt.widgets.Canvas(parent, org.eclipse.swt.SWT.NONE) {
                        @java.lang.Override
                        public org.eclipse.swt.graphics.Point computeSize(int wHint, int hHint, boolean changed) {
                            return new org.eclipse.swt.graphics.Point(wHint == org.eclipse.swt.SWT.DEFAULT ? 1 : wHint, 1);
                        }
                    };
                    separator.addPaintListener(new org.eclipse.swt.events.PaintListener() {
                        public void paintControl(org.eclipse.swt.events.PaintEvent e) {
                            e.gc.setForeground(separator.getForeground());
                            e.gc.drawLine(0, 0, separator.getSize().x, 0);
                        }
                    });
                    separator.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TB_BORDER));
                    org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).indent(2 * org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.INDENT, 0).applyTo(separator);
                }
                return createCommentViewers(parent, toolkit);
            }
        }

        private org.eclipse.ui.forms.widgets.Section createSection(final org.eclipse.swt.widgets.Composite parent, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
            int style = (org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE | org.eclipse.ui.forms.widgets.ExpandableComposite.SHORT_TITLE_BAR) | org.eclipse.ui.forms.widgets.ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT;
            // if (/*commentGroup.hasIncoming() || */expandAllInProgress) {
            // style |= ExpandableComposite.EXPANDED;
            // }
            groupSection = toolkit.createSection(parent, style);
            groupSection.clientVerticalSpacing = 0;
            if (commentGroup.hasIncoming()) {
                groupSection.setBackground(getTaskEditorPage().getAttributeEditorToolkit().getColorIncoming());
            }
            groupSection.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
            groupSection.setText(((commentGroup.getGroupName() + Messages.TaskEditorCommentPart_0) + commentGroup.getCommentAttributes().size()) + Messages.TaskEditorCommentPart_1);
            if (groupSection.isExpanded()) {
                org.eclipse.swt.widgets.Composite composite = createCommentViewers(groupSection, toolkit);
                groupSection.setClient(composite);
            } else {
                groupSection.addExpansionListener(new org.eclipse.ui.forms.events.ExpansionAdapter() {
                    @java.lang.Override
                    public void expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent e) {
                        if (commentGroup.hasIncoming()) {
                            if (e.getState()) {
                                groupSection.setBackground(null);
                            } else {
                                // only decorate background with incoming color when collapsed, otherwise
                                // there is too much decoration in the editor
                                groupSection.setBackground(getTaskEditorPage().getAttributeEditorToolkit().getColorIncoming());
                            }
                        }
                        if (groupSection.getClient() == null) {
                            try {
                                getTaskEditorPage().setReflow(false);
                                org.eclipse.swt.widgets.Composite composite = createCommentViewers(groupSection, toolkit);
                                groupSection.setClient(composite);
                            } finally {
                                getTaskEditorPage().setReflow(true);
                            }
                            reflow();
                        }
                    }
                });
            }
            return groupSection;
        }

        public java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer> getCommentViewers() {
            if (commentViewers != null) {
                return commentViewers;
            }
            commentViewers = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer>(commentGroup.getCommentAttributes().size());
            for (final org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute : commentGroup.getCommentAttributes()) {
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer commentViewer = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer(commentAttribute);
                commentViewers.add(commentViewer);
            }
            return commentViewers;
        }

        public boolean isExpanded() {
            if (groupSection != null) {
                return groupSection.isExpanded();
            }
            if (commentViewers != null) {
                for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer commentViewer : commentViewers) {
                    if (commentViewer.isExpanded()) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Returns true if this group and all comments in it are expanded.
         */
        public boolean isFullyExpanded() {
            if ((groupSection != null) && (!groupSection.isExpanded())) {
                return false;
            }
            if (commentViewers != null) {
                for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer commentViewer : commentViewers) {
                    if (!commentViewer.isExpanded()) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        public boolean isRenderedInSubSection() {
            return renderedInSubSection;
        }

        public void setExpanded(boolean expanded) {
            if ((groupSection != null) && (groupSection.isExpanded() != expanded)) {
                org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(groupSection, expanded);
            }
        }

        /**
         * Expands this group and all comments in it.
         */
        public void setFullyExpanded(boolean expanded) {
            if ((groupSection != null) && (groupSection.isExpanded() != expanded)) {
                org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(groupSection, expanded);
            }
            if (commentViewers != null) {
                for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer commentViewer : commentViewers) {
                    commentViewer.setExpanded(expanded);
                }
            }
        }

        public void setRenderedInSubSection(boolean renderedInSubSection) {
            this.renderedInSubSection = renderedInSubSection;
        }

        public void createSectionHyperlink(java.lang.String message, org.eclipse.ui.forms.events.HyperlinkAdapter listener) {
            if (groupSection != null) {
                org.eclipse.mylyn.commons.workbench.forms.ScalingHyperlink resultLink = new org.eclipse.mylyn.commons.workbench.forms.ScalingHyperlink(groupSection, org.eclipse.swt.SWT.READ_ONLY);
                resultLink.setForeground(org.eclipse.mylyn.commons.ui.compatibility.CommonColors.HYPERLINK_WIDGET);
                resultLink.setUnderlined(true);
                resultLink.setText(message);
                groupSection.setTextClient(resultLink);
                resultLink.getParent().layout(true, true);
                resultLink.addHyperlinkListener(listener);
            }
        }

        public void clearSectionHyperlink() {
            if (groupSection != null) {
                groupSection.setTextClient(null);
            }
        }
    }

    public class CommentViewer {
        private org.eclipse.swt.widgets.Composite buttonComposite;

        private final org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute;

        private org.eclipse.ui.forms.widgets.ExpandableComposite commentComposite;

        private final org.eclipse.mylyn.internal.tasks.core.TaskComment taskComment;

        private org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor editor;

        private boolean suppressSelectionChanged;

        public CommentViewer(org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute) {
            this.commentAttribute = commentAttribute;
            this.taskComment = new org.eclipse.mylyn.internal.tasks.core.TaskComment(getModel().getTaskRepository(), getModel().getTask(), commentAttribute);
        }

        public org.eclipse.swt.widgets.Control createControl(org.eclipse.swt.widgets.Composite composite, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
            boolean hasIncomingChanges = getModel().hasIncomingChanges(commentAttribute);
            getTaskData().getAttributeMapper().updateTaskComment(taskComment, commentAttribute);
            int style = (org.eclipse.ui.forms.widgets.ExpandableComposite.TREE_NODE | org.eclipse.ui.forms.widgets.ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT) | org.eclipse.ui.forms.widgets.ExpandableComposite.COMPACT;
            if (hasIncomingChanges || (expandAllInProgress && (!suppressExpandViewers))) {
                style |= org.eclipse.ui.forms.widgets.ExpandableComposite.EXPANDED;
            }
            commentComposite = toolkit.createExpandableComposite(composite, style);
            commentComposite.clientVerticalSpacing = 0;
            commentComposite.setLayout(new org.eclipse.swt.layout.GridLayout());
            commentComposite.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL));
            commentComposite.setTitleBarForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
            buttonComposite = createTitle(commentComposite, toolkit);
            final org.eclipse.swt.widgets.Composite commentTextComposite = toolkit.createComposite(commentComposite);
            commentComposite.setClient(commentTextComposite);
            commentTextComposite.setLayout(new org.eclipse.mylyn.commons.ui.FillWidthLayout(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getLayoutAdvisor(getTaskEditorPage()), 15, 0, 0, 3));
            commentComposite.addExpansionListener(new org.eclipse.ui.forms.events.ExpansionAdapter() {
                @java.lang.Override
                public void expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent event) {
                    expandComment(toolkit, commentTextComposite, event.getState());
                }
            });
            if (hasIncomingChanges) {
                commentComposite.setBackground(getTaskEditorPage().getAttributeEditorToolkit().getColorIncoming());
            }
            if (commentComposite.isExpanded()) {
                expandComment(toolkit, commentTextComposite, true);
            }
            // for outline
            org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.setMarker(commentComposite, commentAttribute.getId());
            return commentComposite;
        }

        private org.eclipse.swt.widgets.Composite createTitle(final org.eclipse.ui.forms.widgets.ExpandableComposite commentComposite, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
            // always visible
            org.eclipse.swt.widgets.Composite titleComposite = toolkit.createComposite(commentComposite);
            commentComposite.setTextClient(titleComposite);
            org.eclipse.swt.layout.RowLayout rowLayout = new org.eclipse.swt.layout.RowLayout();
            rowLayout.pack = true;
            rowLayout.marginLeft = 0;
            rowLayout.marginBottom = 0;
            rowLayout.marginTop = 0;
            org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.center(rowLayout);
            titleComposite.setLayout(rowLayout);
            titleComposite.setBackground(null);
            org.eclipse.ui.forms.widgets.ImageHyperlink expandCommentHyperlink = createTitleHyperLink(toolkit, titleComposite, taskComment);
            expandCommentHyperlink.addHyperlinkListener(new org.eclipse.ui.forms.events.HyperlinkAdapter() {
                @java.lang.Override
                public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
                    org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(commentComposite, !commentComposite.isExpanded());
                }
            });
            org.eclipse.jface.action.ToolBarManager toolBarManagerTitle = new org.eclipse.jface.action.ToolBarManager(org.eclipse.swt.SWT.FLAT);
            addActionsToToolbarTitle(toolBarManagerTitle, taskComment, this);
            toolBarManagerTitle.createControl(titleComposite);
            // only visible when section is expanded
            final org.eclipse.swt.widgets.Composite buttonComposite = toolkit.createComposite(titleComposite);
            org.eclipse.swt.layout.RowLayout buttonCompLayout = new org.eclipse.swt.layout.RowLayout();
            buttonCompLayout.marginBottom = 0;
            buttonCompLayout.marginTop = 0;
            buttonComposite.setLayout(buttonCompLayout);
            buttonComposite.setBackground(null);
            buttonComposite.setVisible(commentComposite.isExpanded());
            org.eclipse.jface.action.ToolBarManager toolBarManagerButton = new org.eclipse.jface.action.ToolBarManager(org.eclipse.swt.SWT.FLAT);
            addActionsToToolbarButton(toolBarManagerButton, taskComment, this);
            toolBarManagerButton.createControl(buttonComposite);
            return buttonComposite;
        }

        private org.eclipse.ui.forms.widgets.ImageHyperlink createTitleHyperLink(final org.eclipse.ui.forms.widgets.FormToolkit toolkit, final org.eclipse.swt.widgets.Composite toolbarComp, final org.eclipse.mylyn.tasks.core.ITaskComment taskComment) {
            org.eclipse.ui.forms.widgets.ImageHyperlink formHyperlink = toolkit.createImageHyperlink(toolbarComp, org.eclipse.swt.SWT.NONE);
            formHyperlink.setBackground(null);
            formHyperlink.setForeground(toolkit.getColors().getColor(org.eclipse.ui.forms.IFormColors.TITLE));
            org.eclipse.mylyn.tasks.core.IRepositoryPerson author = taskComment.getAuthor();
            if ((author != null) && author.matchesUsername(getTaskEditorPage().getTaskRepository().getUserName())) {
                formHyperlink.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PERSON_ME_NARROW));
            } else {
                formHyperlink.setImage(org.eclipse.mylyn.commons.ui.CommonImages.getImage(org.eclipse.mylyn.commons.ui.CommonImages.PERSON_NARROW));
            }
            java.lang.StringBuilder sb = new java.lang.StringBuilder();
            if (taskComment.getNumber() >= 0) {
                sb.append(taskComment.getNumber());
                sb.append(": ");// $NON-NLS-1$

            }
            java.lang.String toolTipText = "";// $NON-NLS-1$;

            if (author != null) {
                if (author.getName() != null) {
                    sb.append(author.getName());
                    toolTipText = author.getPersonId();
                } else {
                    sb.append(author.getPersonId());
                }
            }
            if (taskComment.getCreationDate() != null) {
                sb.append(", ");// $NON-NLS-1$

                sb.append(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.formatDateTime(taskComment.getCreationDate()));
            }
            formHyperlink.setFont(commentComposite.getFont());
            formHyperlink.setToolTipText(toolTipText);
            formHyperlink.setText(sb.toString());
            formHyperlink.setEnabled(true);
            formHyperlink.setUnderlined(false);
            return formHyperlink;
        }

        private void expandComment(org.eclipse.ui.forms.widgets.FormToolkit toolkit, org.eclipse.swt.widgets.Composite composite, boolean expanded) {
            buttonComposite.setVisible(expanded);
            if (expanded && (composite.getData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.KEY_EDITOR) == null)) {
                // create viewer
                org.eclipse.mylyn.tasks.core.data.TaskAttribute textAttribute = getTaskData().getAttributeMapper().getAssoctiatedAttribute(taskComment.getTaskAttribute());
                editor = createAttributeEditor(textAttribute);
                if (editor != null) {
                    editor.setDecorationEnabled(false);
                    editor.createControl(composite, toolkit);
                    editor.getControl().addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
                        @java.lang.Override
                        public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
                            getTaskEditorPage().selectionChanged(taskComment);
                        }
                    });
                    composite.setData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.KEY_EDITOR, editor);
                    getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
                    reflow();
                }
            } else if ((!expanded) && (composite.getData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.KEY_EDITOR) != null)) {
                // dispose viewer
                org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor editor = ((org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor) (composite.getData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.KEY_EDITOR)));
                editor.getControl().setMenu(null);
                editor.getControl().dispose();
                composite.setData(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.KEY_EDITOR, null);
                reflow();
            }
            if (!suppressSelectionChanged) {
                getTaskEditorPage().selectionChanged(taskComment);
            }
        }

        public boolean isExpanded() {
            return (commentComposite != null) && commentComposite.isExpanded();
        }

        public void setExpanded(boolean expanded) {
            if ((commentComposite != null) && (commentComposite.isExpanded() != expanded)) {
                org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(commentComposite, expanded);
            }
        }

        /**
         * Returns the comment viewer.
         *
         * @return null, if the viewer has not been constructed
         */
        public org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor getEditor() {
            return editor;
        }

        public org.eclipse.mylyn.tasks.core.data.TaskAttribute getTaskAttribute() {
            return commentAttribute;
        }

        public org.eclipse.mylyn.internal.tasks.core.TaskComment getTaskComment() {
            return taskComment;
        }

        public org.eclipse.swt.widgets.Control getControl() {
            return commentComposite;
        }

        public void suppressSelectionChanged(boolean value) {
            this.suppressSelectionChanged = value;
        }
    }

    private class ReplyToCommentAction extends org.eclipse.mylyn.internal.tasks.ui.editors.AbstractReplyToCommentAction implements org.eclipse.jface.action.IMenuCreator {
        private final org.eclipse.mylyn.tasks.core.ITaskComment taskComment;

        private final org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer commentViewer;

        public ReplyToCommentAction(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer commentViewer, org.eclipse.mylyn.tasks.core.ITaskComment taskComment) {
            super(TaskEditorCommentPart.this.getTaskEditorPage(), taskComment);
            this.commentViewer = commentViewer;
            this.taskComment = taskComment;
            setMenuCreator(this);
        }

        @java.lang.Override
        protected java.lang.String getReplyText() {
            return taskComment.getText();
        }

        public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Control parent) {
            currentViewer = commentViewer;
            selectionProvider.setSelection(new org.eclipse.jface.viewers.StructuredSelection(taskComment));
            return commentMenu;
        }

        public void dispose() {
        }

        public org.eclipse.swt.widgets.Menu getMenu(org.eclipse.swt.widgets.Menu parent) {
            selectionProvider.setSelection(new org.eclipse.jface.viewers.StructuredSelection(taskComment));
            return commentMenu;
        }
    }

    /**
     * Expandable composites are indented by 6 pixels by default.
     */
    private static final int INDENT = -6;

    private static final java.lang.String KEY_EDITOR = "viewer";// $NON-NLS-1$


    private java.util.List<org.eclipse.mylyn.tasks.core.data.TaskAttribute> commentAttributes;

    private org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy commentGroupStrategy;

    private java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer> commentGroupViewers;

    private boolean expandAllInProgress;

    private boolean hasIncoming;

    /**
     * We can't use the reflow flag in AbstractTaskEditorPage because it gets set at various points where we might not
     * want to reflow.
     */
    private boolean reflow = true;

    protected org.eclipse.ui.forms.widgets.Section section;

    private org.eclipse.mylyn.commons.ui.SelectionProviderAdapter selectionProvider;

    // XXX: stores a reference to the viewer for which the commentMenu was displayed last
    private org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer currentViewer;

    private org.eclipse.swt.widgets.Menu commentMenu;

    private org.eclipse.mylyn.internal.tasks.ui.actions.CommentActionGroup actionGroup;

    private boolean suppressExpandViewers;

    public TaskEditorCommentPart() {
        this.commentGroupStrategy = new org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy() {
            @java.lang.Override
            protected boolean hasIncomingChanges(org.eclipse.mylyn.tasks.core.ITaskComment taskComment) {
                return getModel().hasIncomingChanges(taskComment.getTaskAttribute());
            }
        };
        setPartName(Messages.TaskEditorCommentPart_Comments);
    }

    protected void addActionsToToolbarButton(org.eclipse.jface.action.ToolBarManager toolBarManager, final org.eclipse.mylyn.internal.tasks.core.TaskComment taskComment, org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer commentViewer) {
        org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.ReplyToCommentAction replyAction = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.ReplyToCommentAction(commentViewer, taskComment);
        replyAction.setImageDescriptor(org.eclipse.mylyn.tasks.ui.TasksUiImages.COMMENT_REPLY_SMALL);
        toolBarManager.add(replyAction);
    }

    protected void addActionsToToolbarTitle(org.eclipse.jface.action.ToolBarManager toolBarManager, final org.eclipse.mylyn.internal.tasks.core.TaskComment taskComment, org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer commentViewer) {
    }

    private void collapseAllComments() {
        try {
            getTaskEditorPage().setReflow(false);
            @java.lang.SuppressWarnings("unused")
            boolean collapsed = false;
            java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer> viewers = getCommentGroupViewers();
            for (int i = 0; i < viewers.size(); i++) {
                if (viewers.get(i).isExpanded()) {
                    viewers.get(i).setFullyExpanded(false);
                    collapsed = viewers.get(i).isRenderedInSubSection();
                    // bug 280152: collapse all groups
                    // break;
                }
            }
            // keep section expanded
            // if (!collapsed && section != null) {
            // CommonFormUtil.setExpanded(section, false);
            // }
        } finally {
            getTaskEditorPage().setReflow(true);
        }
        reflow();
    }

    private org.eclipse.mylyn.internal.tasks.core.TaskComment convertToTaskComment(org.eclipse.mylyn.tasks.core.data.TaskDataModel taskDataModel, org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute) {
        org.eclipse.mylyn.internal.tasks.core.TaskComment taskComment = new org.eclipse.mylyn.internal.tasks.core.TaskComment(taskDataModel.getTaskRepository(), taskDataModel.getTask(), commentAttribute);
        taskDataModel.getTaskData().getAttributeMapper().updateTaskComment(taskComment, commentAttribute);
        return taskComment;
    }

    @java.lang.Override
    public void createControl(org.eclipse.swt.widgets.Composite parent, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        initialize();
        selectionProvider = new org.eclipse.mylyn.commons.ui.SelectionProviderAdapter();
        actionGroup = new org.eclipse.mylyn.internal.tasks.ui.actions.CommentActionGroup();
        org.eclipse.jface.action.MenuManager menuManager = new org.eclipse.jface.action.MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new org.eclipse.jface.action.IMenuListener() {
            public void menuAboutToShow(org.eclipse.jface.action.IMenuManager manager) {
                // get comment and add reply action as first item in the menu
                org.eclipse.jface.viewers.ISelection selection = selectionProvider.getSelection();
                if ((selection instanceof org.eclipse.jface.viewers.IStructuredSelection) && (!selection.isEmpty())) {
                    java.lang.Object element = ((org.eclipse.jface.viewers.IStructuredSelection) (selection)).getFirstElement();
                    if (element instanceof org.eclipse.mylyn.tasks.core.ITaskComment) {
                        final org.eclipse.mylyn.tasks.core.ITaskComment comment = ((org.eclipse.mylyn.tasks.core.ITaskComment) (element));
                        org.eclipse.mylyn.internal.tasks.ui.editors.AbstractReplyToCommentAction replyAction = new org.eclipse.mylyn.internal.tasks.ui.editors.AbstractReplyToCommentAction(getTaskEditorPage(), comment) {
                            @java.lang.Override
                            protected java.lang.String getReplyText() {
                                return comment.getText();
                            }
                        };
                        manager.add(replyAction);
                    }
                }
                actionGroup.setContext(new org.eclipse.ui.actions.ActionContext(selectionProvider.getSelection()));
                actionGroup.fillContextMenu(manager);
                if ((currentViewer != null) && (currentViewer.getEditor() instanceof org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor)) {
                    org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor editor = ((org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor) (currentViewer.getEditor()));
                    if (editor.getViewSourceAction().isEnabled()) {
                        manager.add(new org.eclipse.jface.action.Separator("planning"));// $NON-NLS-1$

                        manager.add(editor.getViewSourceAction());
                    }
                }
            }
        });
        getTaskEditorPage().getEditorSite().registerContextMenu(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.ID_POPUP_MENU, menuManager, selectionProvider, false);
        commentMenu = menuManager.createContextMenu(parent);
        section = createSection(parent, toolkit, hasIncoming);
        section.setText(((section.getText() + " (") + commentAttributes.size()) + ")");// $NON-NLS-1$ //$NON-NLS-2$

        if (commentAttributes.isEmpty()) {
            section.setEnabled(false);
        } else if (hasIncoming) {
            expandSection(toolkit, section);
        } else {
            section.addExpansionListener(new org.eclipse.ui.forms.events.ExpansionAdapter() {
                @java.lang.Override
                public void expansionStateChanged(org.eclipse.ui.forms.events.ExpansionEvent event) {
                    if (section.getClient() == null) {
                        try {
                            expandAllInProgress = true;
                            getTaskEditorPage().setReflow(false);
                            expandSection(toolkit, section);
                        } finally {
                            expandAllInProgress = false;
                            getTaskEditorPage().setReflow(true);
                        }
                        reflow();
                    }
                }
            });
        }
        setSection(toolkit, section);
    }

    @java.lang.Override
    public void dispose() {
        super.dispose();
        if (actionGroup != null) {
            actionGroup.dispose();
        }
    }

    public void expandAllComments(boolean expandViewers) {
        try {
            expandAllInProgress = true;
            suppressExpandViewers = !expandViewers;
            getTaskEditorPage().setReflow(false);
            if (section != null) {
                // the expandAllInProgress flag will ensure that comments in top-level groups have been
                // expanded, no need to expand groups explicitly
                // boolean expandGroups = section.getClient() != null;
                org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(section, true);
                if (expandViewers) {
                    java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer> groupViewers = getCommentGroupViewers();
                    for (int i = groupViewers.size() - 1; i >= 0; i--) {
                        if (!groupViewers.get(i).isFullyExpanded()) {
                            groupViewers.get(i).setFullyExpanded(true);
                        }
                    }
                }
            }
        } finally {
            expandAllInProgress = false;
            suppressExpandViewers = false;
            getTaskEditorPage().setReflow(true);
        }
        reflow();
    }

    private void expandSection(final org.eclipse.ui.forms.widgets.FormToolkit toolkit, final org.eclipse.ui.forms.widgets.Section section) {
        org.eclipse.swt.widgets.Composite composite = toolkit.createComposite(section);
        section.setClient(composite);
        composite.setLayout(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createSectionClientLayout());
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer> viewers = getCommentGroupViewers();
        for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer viewer : viewers) {
            org.eclipse.swt.widgets.Control control = viewer.createControl(composite, toolkit);
            if (viewer.isRenderedInSubSection()) {
                // align twistie of sub-section with section
                org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).indent(2 * org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.INDENT, 0).applyTo(control);
            } else {
                org.eclipse.jface.layout.GridDataFactory.fillDefaults().grab(true, false).indent(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.INDENT, 0).applyTo(control);
            }
        }
    }

    @java.lang.Override
    protected void fillToolBar(org.eclipse.jface.action.ToolBarManager barManager) {
        org.eclipse.jface.action.Action collapseAllAction = new org.eclipse.jface.action.Action("") {
            // $NON-NLS-1$
            @java.lang.Override
            public void run() {
                collapseAllComments();
            }
        };
        collapseAllAction.setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.COLLAPSE_ALL_SMALL);
        collapseAllAction.setToolTipText(Messages.TaskEditorCommentPart_Collapse_Comments);
        barManager.add(collapseAllAction);
        org.eclipse.jface.action.Action expandAllAction = new org.eclipse.jface.action.Action("") {
            // $NON-NLS-1$
            @java.lang.Override
            public void run() {
                expandAllComments(true);
            }
        };
        expandAllAction.setImageDescriptor(org.eclipse.mylyn.commons.ui.CommonImages.EXPAND_ALL_SMALL);
        expandAllAction.setToolTipText(Messages.TaskEditorCommentPart_Expand_Comments);
        barManager.add(expandAllAction);
        if (commentAttributes.isEmpty()) {
            collapseAllAction.setEnabled(false);
            expandAllAction.setEnabled(false);
        }
    }

    public org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy getCommentGroupStrategy() {
        return commentGroupStrategy;
    }

    public void setCommentGroupStrategy(org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy commentGroupStrategy) {
        this.commentGroupStrategy = commentGroupStrategy;
    }

    public java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer> getCommentGroupViewers() {
        if (commentGroupViewers != null) {
            return commentGroupViewers;
        }
        // group comments
        java.util.List<org.eclipse.mylyn.tasks.core.ITaskComment> comments = new java.util.ArrayList<org.eclipse.mylyn.tasks.core.ITaskComment>();
        for (org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute : this.commentAttributes) {
            comments.add(convertToTaskComment(getModel(), commentAttribute));
        }
        java.lang.String currentPersonId = getModel().getTaskRepository().getUserName();
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.CommentGroupStrategy.CommentGroup> commentGroups = getCommentGroupStrategy().groupComments(comments, currentPersonId);
        commentGroupViewers = new java.util.ArrayList<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer>(commentGroups.size());
        if (commentGroups.size() > 0) {
            for (int i = 0; i < commentGroups.size(); i++) {
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer viewer = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer(commentGroups.get(i));
                boolean isLastGroup = i == (commentGroups.size() - 1);
                viewer.setRenderedInSubSection(!isLastGroup);
                commentGroupViewers.add(viewer);
            }
        }
        return commentGroupViewers;
    }

    private void initialize() {
        commentAttributes = getTaskData().getAttributeMapper().getAttributesByType(getTaskData(), org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_COMMENT);
        if (commentAttributes.size() > 0) {
            for (org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute : commentAttributes) {
                if (getModel().hasIncomingChanges(commentAttribute)) {
                    hasIncoming = true;
                    break;
                }
            }
        }
    }

    @java.lang.Override
    public boolean setFormInput(java.lang.Object input) {
        if (input instanceof java.lang.String) {
            java.lang.String text = ((java.lang.String) (input));
            if (commentAttributes != null) {
                for (org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute : commentAttributes) {
                    if (text.equals(commentAttribute.getId())) {
                        selectReveal(commentAttribute);
                    }
                }
            }
        }
        return super.setFormInput(input);
    }

    public org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer selectReveal(org.eclipse.mylyn.tasks.core.data.TaskAttribute commentAttribute) {
        if (commentAttribute == null) {
            return null;
        }
        expandAllComments(false);
        java.util.List<org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer> groupViewers = getCommentGroupViewers();
        for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentGroupViewer groupViewer : groupViewers) {
            for (org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorCommentPart.CommentViewer viewer : groupViewer.getCommentViewers()) {
                if (viewer.getTaskAttribute().equals(commentAttribute)) {
                    // expand section
                    groupViewer.setExpanded(true);
                    // EditorUtil is consistent with behavior of outline
                    org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.reveal(getTaskEditorPage().getManagedForm().getForm(), commentAttribute.getId());
                    return viewer;
                }
            }
        }
        return null;
    }

    public boolean isCommentSectionExpanded() {
        return (section != null) && section.isExpanded();
    }

    public void reflow() {
        if (reflow) {
            getTaskEditorPage().reflow();
        }
    }

    public void setReflow(boolean reflow) {
        this.reflow = reflow;
    }
}