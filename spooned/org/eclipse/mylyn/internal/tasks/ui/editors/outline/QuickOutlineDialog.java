/**
 * *****************************************************************************
 *  Copyright (c) 2006, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tasktop Technologies - adapted for Mylyn
 *     Frank Becker - adapted for Mylyn Task Editor
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors.outline;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.workbench.DecoratingPatternStyledCellLabelProvider;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineContentProvider;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineModel;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.editor.IFormPage;
/**
 *
 * @author Mik Kersten
 * @author Frank Becker
 * @author Steffen Pingel
 */
public class QuickOutlineDialog extends org.eclipse.jface.dialogs.PopupDialog implements org.eclipse.jface.text.IInformationControl , org.eclipse.jface.text.IInformationControlExtension , org.eclipse.jface.text.IInformationControlExtension2 , org.eclipse.swt.events.DisposeListener {
    public final class Filter extends org.eclipse.ui.dialogs.PatternFilter {
        @java.lang.Override
        protected boolean wordMatches(java.lang.String text) {
            return super.wordMatches(text);
        }
    }

    public final class TaskEditorOutlineLabelDecorator implements org.eclipse.jface.viewers.ILabelDecorator {
        public java.lang.String decorateText(java.lang.String text, java.lang.Object element) {
            if (element instanceof org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) {
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode node = ((org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode) (element));
                if (node.getTaskRelation() != null) {
                    return org.eclipse.osgi.util.NLS.bind(Messages.QuickOutlineDialog_Node_Label_Decoration, text, node.getTaskRelation().toString());
                }
            }
            return null;
        }

        public void addListener(org.eclipse.jface.viewers.ILabelProviderListener listener) {
            // ignore
        }

        public void dispose() {
            // ignore
        }

        public boolean isLabelProperty(java.lang.Object element, java.lang.String property) {
            return false;
        }

        public void removeListener(org.eclipse.jface.viewers.ILabelProviderListener listener) {
            // ignore
        }

        public org.eclipse.swt.graphics.Image decorateImage(org.eclipse.swt.graphics.Image image, java.lang.Object element) {
            return null;
        }
    }

    private class OpenListener implements org.eclipse.jface.viewers.IOpenListener , org.eclipse.jface.viewers.IDoubleClickListener , org.eclipse.swt.events.MouseListener {
        private final org.eclipse.jface.viewers.Viewer viewer;

        public OpenListener(org.eclipse.jface.viewers.Viewer viewer) {
            this.viewer = viewer;
        }

        public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
            setSelection(e);
        }

        public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
            setSelection(e);
        }

        public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
            // ignore
        }

        public void doubleClick(org.eclipse.jface.viewers.DoubleClickEvent event) {
            open(null);
        }

        public void open(org.eclipse.jface.viewers.OpenEvent event) {
            org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage = getTaskEditorPage();
            if (taskEditorPage == null) {
                return;
            }
            org.eclipse.jface.viewers.StructuredSelection selection = ((org.eclipse.jface.viewers.StructuredSelection) (viewer.getSelection()));
            java.lang.Object select = selection.getFirstElement();
            taskEditorPage.selectReveal(select);
        }

        private void setSelection(org.eclipse.swt.events.MouseEvent event) {
            try {
                java.lang.Object selection = ((org.eclipse.swt.widgets.Tree) (event.getSource())).getSelection()[0].getData();
                viewer.setSelection(new org.eclipse.jface.viewers.StructuredSelection(selection));
                open(null);
            } catch (java.lang.Exception e) {
                // ignore
            }
        }
    }

    public static final java.lang.String ID_VIEWER = "org.eclipse.mylyn.internal.tasks.ui.taskdata.quick";// $NON-NLS-1$


    private org.eclipse.jface.viewers.TreeViewer viewer;

    private org.eclipse.swt.widgets.Text filterText;

    private org.eclipse.mylyn.internal.tasks.ui.editors.outline.QuickOutlineDialog.Filter namePatternFilter;

    private org.eclipse.mylyn.internal.tasks.ui.editors.outline.QuickOutlineDialog.OpenListener openListener;

    private final org.eclipse.ui.IWorkbenchWindow window;

    public QuickOutlineDialog(org.eclipse.ui.IWorkbenchWindow window) {
        super(window.getShell(), org.eclipse.swt.SWT.RESIZE, true, true, true, true, true, null, null);
        this.window = window;
        setInfoText(Messages.QuickOutlineDialog_Press_Esc_Info_Text);
        create();
    }

    @java.lang.Override
    public boolean close() {
        // nothing additional to dispose
        return super.close();
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createDialogArea(org.eclipse.swt.widgets.Composite parent) {
        createViewer(parent);
        createUIListenersTreeViewer();
        addDisposeListener(this);
        return viewer.getControl();
    }

    private void createViewer(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Control composite = super.createDialogArea(parent);
        viewer = createCommonViewer(((org.eclipse.swt.widgets.Composite) (composite)));
        openListener = new org.eclipse.mylyn.internal.tasks.ui.editors.outline.QuickOutlineDialog.OpenListener(viewer);
        viewer.addOpenListener(openListener);
        viewer.getTree().addMouseListener(openListener);
        namePatternFilter = new org.eclipse.mylyn.internal.tasks.ui.editors.outline.QuickOutlineDialog.Filter();
        namePatternFilter.setIncludeLeadingWildcard(true);
        viewer.addFilter(namePatternFilter);
        org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage = getTaskEditorPage();
        if (taskEditorPage != null) {
            try {
                viewer.getControl().setRedraw(false);
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode root = org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.parse(taskEditorPage.getModel().getTaskData(), true);
                viewer.setInput(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineModel(root));
                viewer.expandAll();
                org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode attributesNode = root.getChild(org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode.LABEL_ATTRIBUTES);
                if (attributesNode != null) {
                    viewer.collapseToLevel(attributesNode, org.eclipse.jface.viewers.AbstractTreeViewer.ALL_LEVELS);
                }
            } finally {
                viewer.getControl().setRedraw(true);
            }
        }
    }

    protected org.eclipse.jface.viewers.TreeViewer createCommonViewer(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.jface.viewers.TreeViewer viewer = new org.eclipse.jface.viewers.TreeViewer(parent, org.eclipse.swt.SWT.H_SCROLL | org.eclipse.swt.SWT.V_SCROLL);
        viewer.setUseHashlookup(true);
        viewer.getControl().setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, true));
        viewer.setContentProvider(new org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineContentProvider());
        viewer.setLabelProvider(new org.eclipse.mylyn.commons.workbench.DecoratingPatternStyledCellLabelProvider(new org.eclipse.mylyn.internal.tasks.ui.editors.outline.QuickOutlineLabelProvider(), new org.eclipse.mylyn.internal.tasks.ui.editors.outline.QuickOutlineDialog.TaskEditorOutlineLabelDecorator(), null));
        return viewer;
    }

    @java.lang.Override
    protected void fillDialogMenu(org.eclipse.jface.action.IMenuManager dialogMenu) {
        dialogMenu.add(new org.eclipse.jface.action.Separator());
        super.fillDialogMenu(dialogMenu);
    }

    private void createUIListenersTreeViewer() {
        final org.eclipse.swt.widgets.Tree tree = viewer.getTree();
        tree.addKeyListener(new org.eclipse.swt.events.KeyListener() {
            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
                if (e.character == 0x1b) {
                    // Dispose on ESC key press
                    dispose();
                }
            }

            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
                // ignore
            }
        });
        tree.addMouseListener(new org.eclipse.swt.events.MouseAdapter() {
            @java.lang.Override
            public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
                handleTreeViewerMouseUp(tree, e);
            }
        });
        tree.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                // ignore
            }

            public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
                gotoSelectedElement();
            }
        });
    }

    private void handleTreeViewerMouseUp(final org.eclipse.swt.widgets.Tree tree, org.eclipse.swt.events.MouseEvent e) {
        if (((tree.getSelectionCount() < 1) || (e.button != 1)) || (tree.equals(e.getSource()) == false)) {
            return;
        }
        // Selection is made in the selection changed listener
        java.lang.Object object = tree.getItem(new org.eclipse.swt.graphics.Point(e.x, e.y));
        org.eclipse.swt.widgets.TreeItem selection = tree.getSelection()[0];
        if (selection.equals(object)) {
            gotoSelectedElement();
        }
    }

    private java.lang.Object getSelectedElement() {
        if (viewer == null) {
            return null;
        }
        return ((org.eclipse.jface.viewers.IStructuredSelection) (viewer.getSelection())).getFirstElement();
    }

    public void addDisposeListener(org.eclipse.swt.events.DisposeListener listener) {
        getShell().addDisposeListener(listener);
    }

    public void addFocusListener(org.eclipse.swt.events.FocusListener listener) {
        getShell().addFocusListener(listener);
    }

    public org.eclipse.swt.graphics.Point computeSizeHint() {
        // Note that it already has the persisted size if persisting is enabled.
        return getShell().getSize();
    }

    public void dispose() {
        close();
    }

    @java.lang.Override
    protected org.eclipse.swt.graphics.Point getDefaultSize() {
        return new org.eclipse.swt.graphics.Point(400, 300);
    }

    public boolean isFocusControl() {
        if (viewer.getControl().isFocusControl() || filterText.isFocusControl()) {
            return true;
        }
        return false;
    }

    public void removeDisposeListener(org.eclipse.swt.events.DisposeListener listener) {
        getShell().removeDisposeListener(listener);
    }

    public void removeFocusListener(org.eclipse.swt.events.FocusListener listener) {
        getShell().removeFocusListener(listener);
    }

    public void setBackgroundColor(org.eclipse.swt.graphics.Color background) {
        applyBackgroundColor(background, getContents());
    }

    public void setFocus() {
        getShell().forceFocus();
        filterText.setFocus();
    }

    public void setForegroundColor(org.eclipse.swt.graphics.Color foreground) {
        applyForegroundColor(foreground, getContents());
    }

    public void setInformation(java.lang.String information) {
        // See IInformationControlExtension2
    }

    public void setLocation(org.eclipse.swt.graphics.Point location) {
        /* If the location is persisted, it gets managed by PopupDialog - fine. Otherwise, the location is
        computed in Window#getInitialLocation, which will center it in the parent shell / main
        monitor, which is wrong for two reasons:
        - we want to center over the editor / subject control, not the parent shell
        - the center is computed via the initalSize, which may be also wrong since the size may 
          have been updated since via min/max sizing of AbstractInformationControlManager.
        In that case, override the location with the one computed by the manager. Note that
        the call to constrainShellSize in PopupDialog.open will still ensure that the shell is
        entirely visible.
         */
        if ((getPersistLocation() == false) || (getDialogSettings() == null)) {
            getShell().setLocation(location);
        }
    }

    public void setSize(int width, int height) {
        getShell().setSize(width, height);
    }

    public void setSizeConstraints(int maxWidth, int maxHeight) {
        // Ignore
    }

    public void setVisible(boolean visible) {
        if (visible) {
            open();
        } else {
            saveDialogBounds(getShell());
            getShell().setVisible(false);
        }
    }

    public boolean hasContents() {
        if ((viewer == null) || (viewer.getInput() == null)) {
            return false;
        }
        return true;
    }

    public void setInput(java.lang.Object input) {
        if (input != null) {
            viewer.setSelection(new org.eclipse.jface.viewers.StructuredSelection(input));
        }
    }

    public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
        // Note: We do not reuse the dialog
        viewer = null;
        filterText = null;
    }

    @java.lang.Override
    protected org.eclipse.swt.widgets.Control createTitleControl(org.eclipse.swt.widgets.Composite parent) {
        org.eclipse.swt.widgets.Composite control = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NONE);
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout(1, false);
        layout.marginHeight = 0;
        control.setLayout(layout);
        control.setLayoutData(new org.eclipse.swt.layout.GridData(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.FILL, true, true));
        // Applies only to dialog title - not body.  See createDialogArea
        // Create the text widget
        createUIWidgetFilterText(control);
        // Add listeners to the text widget
        createUIListenersFilterText();
        // Return the text widget
        return control;
    }

    private void createUIWidgetFilterText(org.eclipse.swt.widgets.Composite parent) {
        // Create the widget
        filterText = new org.eclipse.swt.widgets.Text(parent, org.eclipse.swt.SWT.NONE);
        // Set the font
        org.eclipse.swt.graphics.GC gc = new org.eclipse.swt.graphics.GC(parent);
        gc.setFont(parent.getFont());
        org.eclipse.swt.graphics.FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();
        // Create the layout
        org.eclipse.swt.layout.GridData data = new org.eclipse.swt.layout.GridData(org.eclipse.swt.layout.GridData.FILL_HORIZONTAL);
        data.heightHint = org.eclipse.jface.dialogs.Dialog.convertHeightInCharsToPixels(fontMetrics, 1);
        data.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
        data.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
        filterText.setLayoutData(data);
    }

    /**
     */
    private void gotoSelectedElement() {
        java.lang.Object selectedElement = getSelectedElement();
        if (selectedElement == null) {
            return;
        }
        dispose();
    }

    private void createUIListenersFilterText() {
        filterText.addKeyListener(new org.eclipse.swt.events.KeyListener() {
            public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
                if (e.keyCode == 0xd) {
                    // Return key was pressed
                    gotoSelectedElement();
                } else if (e.keyCode == org.eclipse.swt.SWT.ARROW_DOWN) {
                    // Down key was pressed
                    viewer.getTree().setFocus();
                } else if (e.keyCode == org.eclipse.swt.SWT.ARROW_UP) {
                    // Up key was pressed
                    viewer.getTree().setFocus();
                } else if (e.character == 0x1b) {
                    // Escape key was pressed
                    dispose();
                }
            }

            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
                // NO-OP
            }
        });
        // Handle text modify events
        filterText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
            public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
                java.lang.String text = ((org.eclipse.swt.widgets.Text) (e.widget)).getText();
                int length = text.length();
                if (length > 0) {
                    // Append a '*' pattern to the end of the text value if it
                    // does not have one already
                    if (text.charAt(length - 1) != '*') {
                        text = text + '*';
                    }
                    // Prepend a '*' pattern to the beginning of the text value
                    // if it does not have one already
                    if (text.charAt(0) != '*') {
                        text = '*' + text;
                    }
                }
                // Set and update the pattern
                setMatcherString(text, true);
            }
        });
    }

    /**
     * Sets the patterns to filter out for the receiver.
     * <p>
     * The following characters have special meaning: ? => any character * => any string
     * </p>
     *
     * @param pattern
     * 		the pattern
     * @param update
     * 		<code>true</code> if the viewer should be updated
     */
    private void setMatcherString(java.lang.String pattern, boolean update) {
        namePatternFilter.setPattern(pattern);
        if (update) {
            stringMatcherUpdated();
        }
    }

    /**
     * The string matcher has been modified. The default implementation refreshes the view and selects the first matched
     * element
     */
    private void stringMatcherUpdated() {
        // Refresh the tree viewer to re-filter
        viewer.getControl().setRedraw(false);
        viewer.refresh();
        viewer.expandAll();
        selectFirstMatch();
        viewer.getControl().setRedraw(true);
    }

    protected org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage getTaskEditorPage() {
        org.eclipse.ui.IWorkbenchPage activePage = window.getActivePage();
        if (activePage == null) {
            return null;
        }
        org.eclipse.ui.IEditorPart editorPart = activePage.getActiveEditor();
        org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage taskEditorPage = null;
        if (editorPart instanceof org.eclipse.mylyn.tasks.ui.editors.TaskEditor) {
            org.eclipse.mylyn.tasks.ui.editors.TaskEditor taskEditor = ((org.eclipse.mylyn.tasks.ui.editors.TaskEditor) (editorPart));
            org.eclipse.ui.forms.editor.IFormPage formPage = taskEditor.getActivePageInstance();
            if (formPage instanceof org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage) {
                taskEditorPage = ((org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage) (formPage));
            }
        }
        return taskEditorPage;
    }

    /**
     * Selects the first element in the tree which matches the current filter pattern.
     */
    private void selectFirstMatch() {
        org.eclipse.swt.widgets.Tree tree = viewer.getTree();
        java.lang.Object element = findFirstMatchToPattern(tree.getItems());
        if (element != null) {
            viewer.setSelection(new org.eclipse.jface.viewers.StructuredSelection(element), true);
        } else {
            viewer.setSelection(org.eclipse.jface.viewers.StructuredSelection.EMPTY);
        }
    }

    /**
     *
     * @param items
     * @return  */
    private java.lang.Object findFirstMatchToPattern(org.eclipse.swt.widgets.TreeItem[] items) {
        // Match the string pattern against labels
        org.eclipse.jface.viewers.ILabelProvider labelProvider = ((org.eclipse.jface.viewers.ILabelProvider) (viewer.getLabelProvider()));
        // Process each item in the tree
        for (org.eclipse.swt.widgets.TreeItem item : items) {
            java.lang.Object element = item.getData();
            // Return the element if it matches the pattern
            if (element != null) {
                java.lang.String label = labelProvider.getText(element);
                if (namePatternFilter.wordMatches(label)) {
                    return element;
                }
            }
            // Recursively check the elements children for a match
            element = findFirstMatchToPattern(item.getItems());
            // Return the child element match if found
            if (element != null) {
                return element;
            }
        }
        // No match found
        return null;
    }
}