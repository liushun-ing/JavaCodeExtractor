/**
 * *****************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.ui.PlatformUiUtil;
import org.eclipse.mylyn.commons.ui.compatibility.CommonThemes;
import org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.wizards.NewAttachmentWizardDialog;
import org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.TaskFormPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
/**
 *
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class EditorUtil {
    private static final int PAGE_H_SCROLL_INCREMENT = 64;

    public static final int HEADER_COLUMN_MARGIN = 6;

    // public static final String DATE_FORMAT = "yyyy-MM-dd";
    // 
    // public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    static final java.lang.String KEY_MARKER = "marker";// $NON-NLS-1$


    static final java.lang.String KEY_TEXT_VIEWER = "textViewer";// $NON-NLS-1$


    public static final int MAXIMUM_HEIGHT = 140;

    public static final int MAXIMUM_WIDTH = 300;

    // XXX why is this required?
    public static final org.eclipse.swt.graphics.Font TEXT_FONT = org.eclipse.jface.resource.JFaceResources.getDefaultFont();

    public static final java.lang.String KEY_TOGGLE_TO_MAXIMIZE_ACTION = "maximizeAction";// $NON-NLS-1$


    static boolean canDoGlobalAction(java.lang.String actionId, org.eclipse.jface.text.TextViewer textViewer) {
        if (actionId.equals(org.eclipse.ui.actions.ActionFactory.CUT.getId())) {
            return textViewer.canDoOperation(org.eclipse.jface.text.ITextOperationTarget.CUT);
        } else if (actionId.equals(org.eclipse.ui.actions.ActionFactory.COPY.getId())) {
            return textViewer.canDoOperation(org.eclipse.jface.text.ITextOperationTarget.COPY);
        } else if (actionId.equals(org.eclipse.ui.actions.ActionFactory.PASTE.getId())) {
            return textViewer.canDoOperation(org.eclipse.jface.text.ITextOperationTarget.PASTE);
        } else if (actionId.equals(org.eclipse.ui.actions.ActionFactory.DELETE.getId())) {
            return textViewer.canDoOperation(org.eclipse.jface.text.ITextOperationTarget.DELETE);
        } else if (actionId.equals(org.eclipse.ui.actions.ActionFactory.UNDO.getId())) {
            return textViewer.canDoOperation(org.eclipse.jface.text.ITextOperationTarget.UNDO);
        } else if (actionId.equals(org.eclipse.ui.actions.ActionFactory.REDO.getId())) {
            return textViewer.canDoOperation(org.eclipse.jface.text.ITextOperationTarget.REDO);
        } else if (actionId.equals(org.eclipse.ui.actions.ActionFactory.SELECT_ALL.getId())) {
            return textViewer.canDoOperation(org.eclipse.jface.text.ITextOperationTarget.SELECT_ALL);
        }
        return false;
    }

    /**
     *
     * @deprecated use {@link CommonTextSupport#canPerformAction(String, Control)} instead
     */
    @java.lang.Deprecated
    public static boolean canPerformAction(java.lang.String actionId, org.eclipse.swt.widgets.Control focusControl) {
        return org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport.canPerformAction(actionId, focusControl);
    }

    /**
     *
     * @deprecated use {@link CommonTextSupport#doAction(String, Control)} instead
     */
    @java.lang.Deprecated
    public static void doAction(java.lang.String actionId, org.eclipse.swt.widgets.Control focusControl) {
        org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport.doAction(actionId, focusControl);
    }

    private static org.eclipse.swt.widgets.Control findControl(org.eclipse.swt.widgets.Composite composite, java.lang.String key) {
        if (!composite.isDisposed()) {
            for (org.eclipse.swt.widgets.Control child : composite.getChildren()) {
                if (key.equals(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getMarker(child))) {
                    return child;
                }
                if (child instanceof org.eclipse.swt.widgets.Composite) {
                    org.eclipse.swt.widgets.Control found = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.findControl(((org.eclipse.swt.widgets.Composite) (child)), key);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Scroll to a specified piece of text
     *
     * @param control
     * 		The StyledText to scroll to
     */
    public static void focusOn(org.eclipse.ui.forms.widgets.ScrolledForm form, org.eclipse.swt.widgets.Control control) {
        int pos = 0;
        control.setEnabled(true);
        control.setFocus();
        control.forceFocus();
        while ((control != null) && (control != form.getBody())) {
            pos += control.getLocation().y;
            control = control.getParent();
        } 
        pos = pos - 60;// form.getOrigin().y;

        if (!form.getBody().isDisposed()) {
            form.setOrigin(0, pos);
        }
    }

    static java.text.DateFormat getDateFormat() {
        return java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM);
    }

    static java.lang.String formatDate(java.util.Date date) {
        return org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getDateFormat().format(date);
    }

    static java.lang.String formatDateTime(java.util.Date date) {
        return org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getDateTimeFormat().format(date);
    }

    static java.text.DateFormat getDateTimeFormat() {
        return java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT);
    }

    public static org.eclipse.swt.widgets.Control getFocusControl(org.eclipse.ui.forms.editor.IFormPage page) {
        if (page == null) {
            return null;
        }
        org.eclipse.ui.forms.IManagedForm form = page.getManagedForm();
        if (form == null) {
            return null;
        }
        org.eclipse.swt.widgets.Control control = form.getForm();
        if ((control == null) || control.isDisposed()) {
            return null;
        }
        org.eclipse.swt.widgets.Display display = control.getDisplay();
        org.eclipse.swt.widgets.Control focusControl = display.getFocusControl();
        if ((focusControl == null) || focusControl.isDisposed()) {
            return null;
        }
        return focusControl;
    }

    public static java.lang.String getMarker(org.eclipse.swt.widgets.Widget widget) {
        return ((java.lang.String) (widget.getData(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.KEY_MARKER)));
    }

    /**
     *
     * @deprecated use {@link CommonTextSupport#getTextViewer(Widget)} instead
     */
    @java.lang.Deprecated
    public static org.eclipse.jface.text.TextViewer getTextViewer(org.eclipse.swt.widgets.Widget widget) {
        return org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport.getTextViewer(widget);
    }

    public static org.eclipse.mylyn.internal.tasks.ui.wizards.NewAttachmentWizardDialog openNewAttachmentWizard(final org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage page, org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode mode, org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource source) {
        org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper mapper = page.getModel().getTaskData().getAttributeMapper();
        org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute = mapper.createTaskAttachment(page.getModel().getTaskData());
        final org.eclipse.mylyn.internal.tasks.ui.wizards.NewAttachmentWizardDialog dialog = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.openNewAttachmentWizard(page.getSite().getShell(), page.getTaskRepository(), page.getTask(), attribute, mode, source);
        dialog.getShell().addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
            public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                if (dialog.getReturnCode() == org.eclipse.jface.window.Window.OK) {
                    page.getTaskEditor().refreshPages();
                }
            }
        });
        return dialog;
    }

    /**
     * Selects the given object in the editor.
     *
     * @param o
     * 		The object to be selected.
     * @param highlight
     * 		Whether or not the object should be highlighted.
     */
    public static boolean reveal(org.eclipse.ui.forms.widgets.ScrolledForm form, java.lang.String key) {
        org.eclipse.swt.widgets.Control control = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.findControl(form.getBody(), key);
        if (control != null) {
            // expand all children
            if (control instanceof org.eclipse.ui.forms.widgets.ExpandableComposite) {
                org.eclipse.ui.forms.widgets.ExpandableComposite ex = ((org.eclipse.ui.forms.widgets.ExpandableComposite) (control));
                if (!ex.isExpanded()) {
                    org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(ex, true);
                }
            }
            // expand all parents of control
            org.eclipse.swt.widgets.Composite comp = control.getParent();
            while (comp != null) {
                if (comp instanceof org.eclipse.ui.forms.widgets.Section) {
                    if (!((org.eclipse.ui.forms.widgets.Section) (comp)).isExpanded()) {
                        ((org.eclipse.ui.forms.widgets.Section) (comp)).setExpanded(true);
                    }
                } else if (comp instanceof org.eclipse.ui.forms.widgets.ExpandableComposite) {
                    org.eclipse.ui.forms.widgets.ExpandableComposite ex = ((org.eclipse.ui.forms.widgets.ExpandableComposite) (comp));
                    if (!ex.isExpanded()) {
                        org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(ex, true);
                    }
                    // HACK: This is necessary
                    // due to a bug in SWT's ExpandableComposite.
                    // 165803: Expandable bars should expand when clicking anywhere
                    // https://bugs.eclipse.org/bugs/show_bug.cgi?taskId=165803
                    if ((ex.getData() != null) && (ex.getData() instanceof org.eclipse.swt.widgets.Composite)) {
                        ((org.eclipse.swt.widgets.Composite) (ex.getData())).setVisible(true);
                    }
                }
                comp = comp.getParent();
            } 
            org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.focusOn(form, control);
        }
        return true;
    }

    /**
     *
     * @deprecated Use {@link CommonUiUtil#setEnabled(Composite,boolean)} instead
     */
    @java.lang.Deprecated
    public static void setEnabledState(org.eclipse.swt.widgets.Composite composite, boolean enabled) {
        org.eclipse.mylyn.commons.ui.CommonUiUtil.setEnabled(composite, enabled);
    }

    public static void setMarker(org.eclipse.swt.widgets.Widget widget, java.lang.String text) {
        widget.setData(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.KEY_MARKER, text);
    }

    /**
     *
     * @deprecated use {@link CommonTextSupport#setTextViewer(Widget, TextViewer)} instead
     */
    @java.lang.Deprecated
    public static void setTextViewer(org.eclipse.swt.widgets.Widget widget, org.eclipse.jface.text.TextViewer textViewer) {
        org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport.setTextViewer(widget, textViewer);
    }

    /**
     * Programmatically expand the provided ExpandableComposite, using reflection to fire the expansion listeners (see
     * bug#70358)
     *
     * @param comp
     * @deprecated Use {@link CommonFormUtil#setExpanded(ExpandableComposite,boolean)} instead
     */
    @java.lang.Deprecated
    public static void toggleExpandableComposite(boolean expanded, org.eclipse.ui.forms.widgets.ExpandableComposite comp) {
        org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.setExpanded(comp, expanded);
    }

    /**
     *
     * @deprecated Use {@link CommonFormUtil#disableScrollingOnFocus(ScrolledForm)} instead
     */
    @java.lang.Deprecated
    public static void disableScrollingOnFocus(org.eclipse.ui.forms.widgets.ScrolledForm form) {
        org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.disableScrollingOnFocus(form);
    }

    /**
     *
     * @deprecated Use {@link CommonFormUtil#ensureVisible(Control)} instead
     */
    @java.lang.Deprecated
    public static void ensureVisible(org.eclipse.swt.widgets.Control control) {
        org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.ensureVisible(control);
    }

    // copied from Section.reflow()
    public static void reflow(org.eclipse.swt.widgets.Control control) {
        org.eclipse.swt.widgets.Composite c = control.getParent();
        while (c != null) {
            c.setRedraw(false);
            c = c.getParent();
            if (c instanceof org.eclipse.ui.forms.widgets.SharedScrolledComposite) {
                break;
            }
        } 
        c = control.getParent();
        while (c != null) {
            c.layout(true);
            c = c.getParent();
            if (c instanceof org.eclipse.ui.forms.widgets.SharedScrolledComposite) {
                ((org.eclipse.ui.forms.widgets.SharedScrolledComposite) (c)).reflow(true);
                break;
            }
        } 
        c = control.getParent();
        while (c != null) {
            c.setRedraw(true);
            c = c.getParent();
            if (c instanceof org.eclipse.ui.forms.widgets.SharedScrolledComposite) {
                break;
            }
        } 
    }

    public static org.eclipse.swt.widgets.Composite getLayoutAdvisor(org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage page) {
        org.eclipse.swt.widgets.Composite layoutAdvisor = page.getEditorComposite();
        do {
            layoutAdvisor = layoutAdvisor.getParent();
        } while (!((layoutAdvisor instanceof org.eclipse.swt.custom.CTabFolder) || layoutAdvisor.getClass().getName().equals("org.eclipse.e4.ui.widgets.CTabFolder")) );// $NON-NLS-1$

        return layoutAdvisor.getParent();
    }

    /**
     * Recursively sets the menu of all children of <code>composite</code>.
     *
     * @deprecated Use {@link CommonUiUtil#setMenu(Composite,Menu)} instead
     */
    @java.lang.Deprecated
    public static void setMenu(org.eclipse.swt.widgets.Composite composite, org.eclipse.swt.widgets.Menu menu) {
        org.eclipse.mylyn.commons.ui.CommonUiUtil.setMenu(composite, menu);
    }

    /**
     *
     * @deprecated use {@link RowLayout#center} instead
     */
    @java.lang.Deprecated
    public static void center(org.eclipse.swt.layout.RowLayout rowLayout) {
        try {
            java.lang.reflect.Field field = org.eclipse.swt.layout.RowLayout.class.getDeclaredField("center");// $NON-NLS-1$

            field.set(rowLayout, java.lang.Boolean.TRUE);
        } catch (java.lang.Throwable e) {
            // ignore
        }
    }

    public static org.eclipse.swt.widgets.Composite createBorder(org.eclipse.swt.widgets.Composite composite, final org.eclipse.ui.forms.widgets.FormToolkit toolkit, boolean paintBorder) {
        // create composite to hold rounded border
        final org.eclipse.swt.widgets.Composite roundedBorder = toolkit.createComposite(composite);
        if (paintBorder) {
            roundedBorder.addPaintListener(new org.eclipse.swt.events.PaintListener() {
                public void paintControl(org.eclipse.swt.events.PaintEvent e) {
                    e.gc.setForeground(toolkit.getColors().getBorderColor());
                    org.eclipse.swt.graphics.Point size = roundedBorder.getSize();
                    e.gc.drawRoundRectangle(0, 2, size.x - 1, size.y - 5, 5, 5);
                }
            });
            roundedBorder.setLayout(org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().margins(4, 6).create());
        } else {
            roundedBorder.setLayout(org.eclipse.jface.layout.GridLayoutFactory.fillDefaults().margins(0, 6).create());
        }
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.BEGINNING).hint(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.MAXIMUM_WIDTH, org.eclipse.swt.SWT.DEFAULT).grab(true, false).applyTo(roundedBorder);
        return roundedBorder;
    }

    public static org.eclipse.swt.widgets.Composite createBorder(org.eclipse.swt.widgets.Composite composite, final org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        return org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.createBorder(composite, toolkit, true);
    }

    public static org.eclipse.swt.graphics.Font setHeaderFontSizeAndStyle(org.eclipse.swt.widgets.Control text) {
        org.eclipse.jface.layout.GridDataFactory.fillDefaults().align(org.eclipse.swt.SWT.FILL, org.eclipse.swt.SWT.CENTER).grab(true, false).applyTo(text);
        // .hint(EditorUtil.MAXIMUM_WIDTH, SWT.DEFAULT)
        float sizeFactor = 1.1F;
        org.eclipse.swt.graphics.Font initialFont = text.getFont();
        org.eclipse.swt.graphics.FontData[] fontData = initialFont.getFontData();
        for (org.eclipse.swt.graphics.FontData element : fontData) {
            element.setHeight(((int) (element.getHeight() * sizeFactor)));
            element.setStyle(element.getStyle() | org.eclipse.swt.SWT.BOLD);
        }
        final org.eclipse.swt.graphics.Font textFont = new org.eclipse.swt.graphics.Font(text.getDisplay(), fontData);
        text.setFont(textFont);
        text.addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
            public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                textFont.dispose();
            }
        });
        org.eclipse.swt.graphics.Color color = org.eclipse.ui.PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_COMPLETED);
        text.setForeground(color);
        return textFont;
    }

    public static org.eclipse.swt.layout.GridData getTextControlLayoutData(org.eclipse.mylyn.tasks.ui.editors.TaskFormPage page, org.eclipse.swt.widgets.Control control, boolean expandVertically) {
        final org.eclipse.swt.layout.GridData gd = new org.eclipse.swt.layout.GridData();
        // wrap text at this margin, see comment below
        int width = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.getEditorWidth(page);
        // the goal is to make the text viewer as big as the text so it does not require scrolling when first drawn
        // on screen
        org.eclipse.swt.graphics.Point size = control.computeSize(width, org.eclipse.swt.SWT.DEFAULT, true);
        gd.widthHint = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.MAXIMUM_WIDTH;
        gd.minimumWidth = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.MAXIMUM_WIDTH;
        gd.horizontalAlignment = org.eclipse.swt.SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        // limit height to be avoid dynamic resizing of the text widget:
        // MAXIMUM_HEIGHT < height < MAXIMUM_HEIGHT * 3
        // gd.minimumHeight = AbstractAttributeEditor.MAXIMUM_HEIGHT;
        gd.heightHint = java.lang.Math.min(java.lang.Math.max(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.MAXIMUM_HEIGHT, size.y), org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.MAXIMUM_HEIGHT * 3);
        if (expandVertically) {
            gd.verticalAlignment = org.eclipse.swt.SWT.FILL;
            gd.grabExcessVerticalSpace = true;
        }
        return gd;
        // shrink the text control if the editor width is reduced, otherwise the text field will always keep it's original
        // width and will cause the editor to have a horizonal scroll bar
        // composite.addControlListener(new ControlAdapter() {
        // @Override
        // public void controlResized(ControlEvent e) {
        // int width = sectionComposite.getSize().x;
        // Point size = descriptionTextViewer.getTextWidget().computeSize(width, SWT.DEFAULT, true);
        // // limit width to parent widget
        // gd.widthHint = width;
        // // limit height to avoid dynamic resizing of the text widget
        // gd.heightHint = Math.min(Math.max(DESCRIPTION_HEIGHT, size.y), DESCRIPTION_HEIGHT * 4);
        // sectionComposite.layout();
        // }
        // });
    }

    private static int getEditorWidth(org.eclipse.mylyn.tasks.ui.editors.TaskFormPage page) {
        int widthHint = 0;
        if ((page.getManagedForm() != null) && (page.getManagedForm().getForm() != null)) {
            widthHint = page.getManagedForm().getForm().getClientArea().width - 90;
        }
        if (((widthHint <= 0) && (page.getEditor().getEditorSite() != null)) && (page.getEditor().getEditorSite().getPage() != null)) {
            // EditorAreaHelper editorManager = ((WorkbenchPage) page.getEditor().getEditorSite().getPage()).getEditorPresentation();
            // if (editorManager != null && editorManager.getLayoutPart() != null) {
            // widthHint = editorManager.getLayoutPart().getControl().getBounds().width - 90;
            // }
            // XXX e4.0 this does not work, the composite is always null
            org.eclipse.swt.widgets.Composite composite = ((org.eclipse.ui.internal.WorkbenchPage) (page.getEditor().getEditorSite().getPage())).getClientComposite();
            if (composite != null) {
                widthHint = composite.getBounds().width - 90;
            }
        }
        if (widthHint <= 0) {
            widthHint = org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.MAXIMUM_WIDTH;
        }
        return widthHint;
    }

    public static org.eclipse.swt.layout.GridLayout createSectionClientLayout() {
        org.eclipse.swt.layout.GridLayout layout = new org.eclipse.swt.layout.GridLayout();
        layout.marginHeight = 0;
        // leave 1px for borders
        layout.marginTop = 2;
        // spacing if a section is expanded
        layout.marginBottom = 8;
        return layout;
    }

    public static void initializeScrollbars(org.eclipse.ui.forms.widgets.ScrolledForm form) {
        // initialize scroll bars
        org.eclipse.swt.widgets.ScrollBar hbar = form.getHorizontalBar();
        if (hbar != null) {
            hbar.setIncrement(org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.PAGE_H_SCROLL_INCREMENT);
        }
    }

    public static java.lang.String removeColon(java.lang.String label) {
        label = label.trim();
        return label.endsWith(":") ? label.substring(0, label.length() - 1) : label;// $NON-NLS-1$

    }

    public static void scroll(org.eclipse.swt.custom.ScrolledComposite scomp, int xoffset, int yoffset) {
        org.eclipse.swt.graphics.Point origin = scomp.getOrigin();
        org.eclipse.swt.graphics.Point contentSize = scomp.getContent().getSize();
        int xorigin = origin.x + xoffset;
        int yorigin = origin.y + yoffset;
        xorigin = java.lang.Math.max(xorigin, 0);
        xorigin = java.lang.Math.min(xorigin, contentSize.x - 1);
        yorigin = java.lang.Math.max(yorigin, 0);
        yorigin = java.lang.Math.min(yorigin, contentSize.y - 1);
        scomp.setOrigin(xorigin, yorigin);
    }

    public static void addScrollListener(final org.eclipse.swt.widgets.Scrollable textWidget) {
        textWidget.addMouseWheelListener(new org.eclipse.swt.events.MouseWheelListener() {
            public void mouseScrolled(org.eclipse.swt.events.MouseEvent event) {
                org.eclipse.swt.custom.ScrolledComposite form = org.eclipse.ui.internal.forms.widgets.FormUtil.getScrolledComposite(textWidget);
                if (form != null) {
                    org.eclipse.swt.widgets.ScrollBar verticalBar = textWidget.getVerticalBar();
                    if (event.count < 0) {
                        // scroll form down
                        if ((verticalBar == null) || ((verticalBar.getSelection() + verticalBar.getThumb()) == verticalBar.getMaximum())) {
                            org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.scroll(form, 0, form.getVerticalBar().getIncrement());
                        }
                    } else // scroll form up
                    if ((verticalBar == null) || (verticalBar.getSelection() == verticalBar.getMinimum())) {
                        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.scroll(form, 0, -form.getVerticalBar().getIncrement());
                    }
                }
            }
        });
    }

    public static void addScrollListener(final org.eclipse.swt.custom.CCombo combo) {
        if (org.eclipse.mylyn.commons.ui.PlatformUiUtil.usesMouseWheelEventsForScrolling()) {
            combo.addListener(org.eclipse.swt.SWT.MouseWheel, new org.eclipse.swt.widgets.Listener() {
                public void handleEvent(org.eclipse.swt.widgets.Event event) {
                    if (event.count > 0) {
                        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.handleScrollUp(combo, event);
                    } else if (event.count < 0) {
                        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.handleScrollDown(combo, event);
                    }
                }
            });
        } else {
            combo.addListener(org.eclipse.swt.SWT.KeyDown, new org.eclipse.swt.widgets.Listener() {
                public void handleEvent(org.eclipse.swt.widgets.Event event) {
                    if (event.keyCode == org.eclipse.swt.SWT.ARROW_UP) {
                        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.handleScrollUp(combo, event);
                    } else if (event.keyCode == org.eclipse.swt.SWT.ARROW_DOWN) {
                        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.handleScrollDown(combo, event);
                    }
                }
            });
        }
    }

    private static void handleScrollUp(org.eclipse.swt.custom.CCombo combo, org.eclipse.swt.widgets.Event event) {
        if (combo.isFocusControl()) {
            // could be a legitimate key event, let CCombo handle it
            return;
        }
        org.eclipse.swt.custom.ScrolledComposite form = org.eclipse.ui.internal.forms.widgets.FormUtil.getScrolledComposite(combo);
        if (form != null) {
            org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.scroll(form, 0, -form.getVerticalBar().getIncrement());
            event.doit = false;
        }
    }

    private static void handleScrollDown(org.eclipse.swt.custom.CCombo combo, org.eclipse.swt.widgets.Event event) {
        if (combo.isFocusControl()) {
            // could be a legitimate key event, let CCombo handle it
            return;
        }
        org.eclipse.swt.custom.ScrolledComposite form = org.eclipse.ui.internal.forms.widgets.FormUtil.getScrolledComposite(combo);
        if (form != null) {
            org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.scroll(form, 0, form.getVerticalBar().getIncrement());
            event.doit = false;
        }
    }

    public static void setTitleBarForeground(org.eclipse.ui.forms.widgets.ExpandableComposite composite, org.eclipse.swt.graphics.Color color) {
        try {
            java.lang.reflect.Field field = org.eclipse.ui.forms.widgets.ExpandableComposite.class.getDeclaredField("titleBarForeground");// $NON-NLS-1$

            field.setAccessible(true);
            field.set(composite, color);
        } catch (java.lang.Exception e) {
            composite.setTitleBarForeground(color);
        }
    }
}