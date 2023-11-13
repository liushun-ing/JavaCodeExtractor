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
 *     Raphael Ackermann - spell checking support on bug 195514
 *     Jingwen Ou - extensibility improvements
 *     David Green - fix for bug 256702
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui.editors;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.mylyn.commons.ui.FillWidthLayout;
import org.eclipse.mylyn.commons.ui.compatibility.CommonThemes;
import org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.themes.IThemeManager;
/**
 * A text attribute editor that can switch between a editor, preview and source view.
 *
 * @author Raphael Ackermann
 * @author Steffen Pingel
 * @author Jingwen Ou
 */
public class RichTextEditor {
    public enum State {

        DEFAULT,
        BROWSER,
        EDITOR,
        PREVIEW;}

    public static class StateChangedEvent {
        public org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.State state;
    }

    public interface StateChangedListener {
        public void stateChanged(org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedEvent event);
    }

    public class ViewSourceAction extends org.eclipse.jface.action.Action {
        public ViewSourceAction() {
            super(Messages.RichTextAttributeEditor_Viewer_Source, org.eclipse.swt.SWT.TOGGLE);
            setChecked(false);
            setEnabled(false);
        }

        @java.lang.Override
        public void run() {
            if (isChecked()) {
                showDefault();
            } else {
                showEditor();
            }
            if (editorLayout != null) {
                org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.reflow(editorLayout.topControl);
            }
            org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.setChecked(isChecked());
        }
    }

    private static final java.lang.String KEY_TEXT_VERSION = "org.eclipse.mylyn.tasks.ui.textVersion";// $NON-NLS-1$


    private org.eclipse.mylyn.internal.tasks.ui.editors.BrowserPreviewViewer browserViewer;

    private org.eclipse.ui.contexts.IContextActivation contextActivation;

    private final org.eclipse.ui.contexts.IContextService contextService;

    private org.eclipse.swt.widgets.Control control;

    private org.eclipse.jface.text.source.SourceViewer defaultViewer;

    private org.eclipse.swt.widgets.Composite editorComposite;

    private org.eclipse.swt.custom.StackLayout editorLayout;

    private org.eclipse.jface.text.source.SourceViewer editorViewer;

    private final org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension extension;

    private org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode mode;

    private org.eclipse.jface.text.source.SourceViewer previewViewer;

    boolean readOnly;

    private org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine renderingEngine;

    private final org.eclipse.mylyn.tasks.core.TaskRepository repository;

    private boolean spellCheckingEnabled;

    private final int style;

    private org.eclipse.ui.forms.widgets.FormToolkit toolkit;

    private final org.eclipse.jface.action.IAction viewSourceAction;

    private java.lang.String text;

    /**
     * Changed each time text is updated.
     */
    private int textVersion;

    private final org.eclipse.core.runtime.ListenerList stateChangedListeners = new org.eclipse.core.runtime.ListenerList(org.eclipse.core.runtime.ListenerList.IDENTITY);

    private final org.eclipse.mylyn.tasks.core.ITask task;

    @java.lang.Deprecated
    public RichTextEditor(org.eclipse.mylyn.tasks.core.TaskRepository repository, int style) {
        this(repository, style, null, null, null);
    }

    @java.lang.Deprecated
    public RichTextEditor(org.eclipse.mylyn.tasks.core.TaskRepository repository, int style, org.eclipse.ui.contexts.IContextService contextService, org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension extension) {
        this(repository, style, contextService, extension, null);
    }

    public RichTextEditor(org.eclipse.mylyn.tasks.core.TaskRepository repository, int style, org.eclipse.ui.contexts.IContextService contextService, org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorExtension extension, org.eclipse.mylyn.tasks.core.ITask task) {
        this.repository = repository;
        this.style = style;
        this.contextService = contextService;
        this.extension = extension;
        this.text = "";// $NON-NLS-1$

        this.viewSourceAction = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.ViewSourceAction();
        setMode(org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.DEFAULT);
        this.task = task;
    }

    private org.eclipse.jface.text.source.SourceViewer configure(final org.eclipse.jface.text.source.SourceViewer viewer, org.eclipse.jface.text.Document document, boolean readOnly) {
        // do this before setting the document to not require invalidating the presentation
        org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.installHyperlinkPresenter(viewer, repository, task, getMode());
        updateDocument(viewer, document, readOnly);
        if (readOnly) {
            if (extension != null) {
                // setting view source action
                viewer.getControl().setData(org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.VIEW_SOURCE_ACTION, viewSourceAction);
                viewer.getControl().addFocusListener(new org.eclipse.swt.events.FocusAdapter() {
                    @java.lang.Override
                    public void focusGained(org.eclipse.swt.events.FocusEvent e) {
                        org.eclipse.mylyn.internal.tasks.ui.commands.ViewSourceHandler.setChecked(getViewer() == defaultViewer);
                    }
                });
            }
        } else {
            installListeners(viewer);
            viewer.getControl().setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
        }
        // enable cut/copy/paste
        org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport.setTextViewer(viewer.getTextWidget(), viewer);
        viewer.setEditable(!readOnly);
        viewer.getTextWidget().setFont(getFont());
        if (toolkit != null) {
            toolkit.adapt(viewer.getControl(), false, false);
        }
        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.addScrollListener(viewer.getTextWidget());
        return viewer;
    }

    /**
     * Configures annotation model for spell checking.
     */
    private void updateDocument(org.eclipse.jface.text.source.SourceViewer viewer, org.eclipse.jface.text.Document document, boolean readOnly) {
        if (new java.lang.Integer(this.textVersion).equals(viewer.getData(org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.KEY_TEXT_VERSION))) {
            // already up-to-date, skip re-loading of the document
            return;
        }
        if (readOnly) {
            viewer.setDocument(document);
        } else {
            org.eclipse.jface.text.source.AnnotationModel annotationModel = new org.eclipse.jface.text.source.AnnotationModel();
            viewer.showAnnotations(false);
            viewer.showAnnotationsOverview(false);
            org.eclipse.jface.text.source.IAnnotationAccess annotationAccess = new org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess();
            final org.eclipse.ui.texteditor.SourceViewerDecorationSupport support = new org.eclipse.ui.texteditor.SourceViewerDecorationSupport(viewer, null, annotationAccess, org.eclipse.ui.editors.text.EditorsUI.getSharedTextColors());
            java.util.Iterator<?> e = new org.eclipse.ui.texteditor.MarkerAnnotationPreferences().getAnnotationPreferences().iterator();
            while (e.hasNext()) {
                support.setAnnotationPreference(((org.eclipse.ui.texteditor.AnnotationPreference) (e.next())));
            } 
            support.install(org.eclipse.ui.editors.text.EditorsUI.getPreferenceStore());
            viewer.getTextWidget().addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
                public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                    support.uninstall();
                }
            });
            // viewer.getTextWidget().setIndent(2);
            viewer.setDocument(document, annotationModel);
        }
        viewer.setData(org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.KEY_TEXT_VERSION, this.textVersion);
    }

    public void createControl(org.eclipse.swt.widgets.Composite parent, org.eclipse.ui.forms.widgets.FormToolkit toolkit) {
        this.toolkit = toolkit;
        int style = this.style;
        if ((!isReadOnly()) && ((style & org.eclipse.swt.SWT.NO_SCROLL) == 0)) {
            style |= org.eclipse.swt.SWT.V_SCROLL;
        }
        if ((extension != null) || (renderingEngine != null)) {
            editorComposite = new org.eclipse.swt.widgets.Composite(parent, org.eclipse.swt.SWT.NULL);
            editorLayout = new org.eclipse.swt.custom.StackLayout() {
                @java.lang.Override
                protected org.eclipse.swt.graphics.Point computeSize(org.eclipse.swt.widgets.Composite composite, int hint, int hint2, boolean flushCache) {
                    return topControl.computeSize(hint, hint2, flushCache);
                }
            };
            editorComposite.setLayout(editorLayout);
            setControl(editorComposite);
            if (extension != null) {
                if (isReadOnly()) {
                    editorViewer = extension.createViewer(repository, editorComposite, style, createHyperlinkDetectorContext());
                } else {
                    editorViewer = extension.createEditor(repository, editorComposite, style, createHyperlinkDetectorContext());
                    editorViewer.getTextWidget().addFocusListener(new org.eclipse.swt.events.FocusListener() {
                        public void focusGained(org.eclipse.swt.events.FocusEvent e) {
                            setContext();
                        }

                        public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                            unsetContext();
                        }
                    });
                    editorViewer.getTextWidget().addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
                        public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                            unsetContext();
                        }
                    });
                }
                configure(editorViewer, new org.eclipse.jface.text.Document(getText()), isReadOnly());
                show(editorViewer.getControl());
            } else {
                defaultViewer = createDefaultEditor(editorComposite, style);
                configure(defaultViewer, new org.eclipse.jface.text.Document(getText()), isReadOnly());
                show(defaultViewer.getControl());
            }
            if ((!isReadOnly()) && ((style & org.eclipse.swt.SWT.NO_SCROLL) == 0)) {
                editorComposite.setData(org.eclipse.ui.forms.widgets.FormToolkit.KEY_DRAW_BORDER, org.eclipse.ui.forms.widgets.FormToolkit.TREE_BORDER);
            }
            viewSourceAction.setEnabled(true);
        } else {
            defaultViewer = createDefaultEditor(parent, style);
            configure(defaultViewer, new org.eclipse.jface.text.Document(getText()), isReadOnly());
            setControl(defaultViewer.getControl());
            viewSourceAction.setEnabled(false);
        }
    }

    @java.lang.SuppressWarnings({ "rawtypes" })
    private org.eclipse.core.runtime.IAdaptable createHyperlinkDetectorContext() {
        return new org.eclipse.core.runtime.IAdaptable() {
            public java.lang.Object getAdapter(java.lang.Class adapter) {
                if (adapter == org.eclipse.mylyn.tasks.core.TaskRepository.class) {
                    return repository;
                }
                if (adapter == org.eclipse.mylyn.tasks.core.ITask.class) {
                    return task;
                }
                return null;
            }
        };
    }

    private org.eclipse.jface.text.source.SourceViewer createDefaultEditor(org.eclipse.swt.widgets.Composite parent, int styles) {
        org.eclipse.jface.text.source.SourceViewer defaultEditor = new org.eclipse.jface.text.source.SourceViewer(parent, null, styles | org.eclipse.swt.SWT.WRAP);
        org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration viewerConfig = new org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration(repository, task, isSpellCheckingEnabled() && (!isReadOnly()));
        viewerConfig.setMode(getMode());
        defaultEditor.configure(viewerConfig);
        return defaultEditor;
    }

    private org.eclipse.mylyn.internal.tasks.ui.editors.BrowserPreviewViewer getBrowserViewer() {
        if ((editorComposite == null) || (renderingEngine == null)) {
            return null;
        }
        if (browserViewer == null) {
            browserViewer = new org.eclipse.mylyn.internal.tasks.ui.editors.BrowserPreviewViewer(getRepository(), renderingEngine);
            browserViewer.createControl(editorComposite, toolkit);
        }
        return browserViewer;
    }

    public org.eclipse.swt.widgets.Control getControl() {
        return control;
    }

    public org.eclipse.jface.text.source.SourceViewer getDefaultViewer() {
        if (defaultViewer == null) {
            defaultViewer = createDefaultEditor(editorComposite, style);
            configure(defaultViewer, new org.eclipse.jface.text.Document(getText()), isReadOnly());
            // fixed font size
            defaultViewer.getTextWidget().setFont(org.eclipse.jface.resource.JFaceResources.getFontRegistry().get(org.eclipse.jface.resource.JFaceResources.TEXT_FONT));
            // adapt maximize action
            defaultViewer.getControl().setData(EditorUtil.KEY_TOGGLE_TO_MAXIMIZE_ACTION, editorViewer.getControl().getData(EditorUtil.KEY_TOGGLE_TO_MAXIMIZE_ACTION));
            // adapt menu to the new viewer
            installMenu(defaultViewer.getControl(), editorViewer.getControl().getMenu());
        }
        return defaultViewer;
    }

    public org.eclipse.jface.text.source.SourceViewer getEditorViewer() {
        return editorViewer;
    }

    private org.eclipse.swt.graphics.Font getFont() {
        if (mode == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.DEFAULT) {
            org.eclipse.ui.themes.IThemeManager themeManager = org.eclipse.ui.PlatformUI.getWorkbench().getThemeManager();
            org.eclipse.swt.graphics.Font font = themeManager.getCurrentTheme().getFontRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.FONT_EDITOR_COMMENT);
            return font;
        } else {
            return EditorUtil.TEXT_FONT;
        }
    }

    public org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode getMode() {
        return mode;
    }

    /**
     *
     * @return The preview source viewer or null if there is no extension available or the attribute is read only
     */
    private org.eclipse.jface.text.source.SourceViewer getPreviewViewer() {
        if (extension == null) {
            return null;
        }
        // construct as needed
        if (previewViewer == null) {
            // previewer should always have a vertical scroll bar if it's editable
            int previewViewerStyle = style;
            if (getEditorViewer() != null) {
                previewViewerStyle |= org.eclipse.swt.SWT.V_SCROLL;
            }
            previewViewer = extension.createViewer(repository, editorComposite, previewViewerStyle, createHyperlinkDetectorContext());
            configure(previewViewer, new org.eclipse.jface.text.Document(getText()), true);
            // adapt maximize action
            previewViewer.getControl().setData(EditorUtil.KEY_TOGGLE_TO_MAXIMIZE_ACTION, editorViewer.getControl().getData(EditorUtil.KEY_TOGGLE_TO_MAXIMIZE_ACTION));
            installMenu(previewViewer.getControl(), editorViewer.getControl().getMenu());
            // set the background color in case there is an incoming to show
            previewViewer.getTextWidget().setBackground(editorComposite.getBackground());
        }
        return previewViewer;
    }

    public org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine getRenderingEngine() {
        return renderingEngine;
    }

    public org.eclipse.mylyn.tasks.core.TaskRepository getRepository() {
        return repository;
    }

    public java.lang.String getText() {
        return this.text;
    }

    public org.eclipse.jface.text.source.SourceViewer getViewer() {
        if (editorLayout == null) {
            return defaultViewer;
        }
        if ((defaultViewer != null) && (editorLayout.topControl == defaultViewer.getControl())) {
            return defaultViewer;
        } else if ((previewViewer != null) && (editorLayout.topControl == previewViewer.getControl())) {
            return previewViewer;
        } else {
            return editorViewer;
        }
    }

    public org.eclipse.jface.action.IAction getViewSourceAction() {
        return viewSourceAction;
    }

    public boolean hasBrowser() {
        return renderingEngine != null;
    }

    public boolean hasPreview() {
        return (extension != null) && (!isReadOnly());
    }

    public static org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration installHyperlinkPresenter(org.eclipse.jface.text.source.ISourceViewer viewer, org.eclipse.mylyn.tasks.core.TaskRepository repository, org.eclipse.mylyn.tasks.core.ITask task, org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode mode) {
        org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration configuration = new org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration(repository, task, false);
        configuration.setMode(mode);
        // do not configure viewer, this has already been done in extension
        org.eclipse.mylyn.internal.tasks.ui.editors.AbstractHyperlinkTextPresentationManager manager;
        if (mode == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.DEFAULT) {
            manager = new org.eclipse.mylyn.internal.tasks.ui.editors.HighlightingHyperlinkTextPresentationManager();
            manager.setHyperlinkDetectors(configuration.getDefaultHyperlinkDetectors(viewer, null));
            manager.install(viewer);
            manager = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskHyperlinkTextPresentationManager();
            manager.setHyperlinkDetectors(configuration.getDefaultHyperlinkDetectors(viewer, org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.TASK));
            manager.install(viewer);
        } else if (mode == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.TASK_RELATION) {
            manager = new org.eclipse.mylyn.internal.tasks.ui.editors.TaskHyperlinkTextPresentationManager();
            manager.setHyperlinkDetectors(configuration.getDefaultHyperlinkDetectors(viewer, org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.TASK_RELATION));
            manager.install(viewer);
        }
        return configuration;
    }

    private void installListeners(final org.eclipse.jface.text.source.SourceViewer viewer) {
        viewer.addTextListener(new org.eclipse.jface.text.ITextListener() {
            public void textChanged(org.eclipse.jface.text.TextEvent event) {
                // filter out events caused by text presentation changes, e.g. annotation drawing
                java.lang.String value = viewer.getTextWidget().getText();
                if (!RichTextEditor.this.text.equals(value)) {
                    RichTextEditor.this.text = value;
                    RichTextEditor.this.textVersion++;
                    viewer.setData(org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.KEY_TEXT_VERSION, RichTextEditor.this.textVersion);
                    valueChanged(value);
                    org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil.ensureVisible(viewer.getTextWidget());
                }
            }
        });
        // ensure that tab traverses to next control instead of inserting a tab character unless editing multi-line text
        if (((style & org.eclipse.swt.SWT.MULTI) != 0) && (mode != org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.DEFAULT)) {
            viewer.getTextWidget().addListener(org.eclipse.swt.SWT.Traverse, new org.eclipse.swt.widgets.Listener() {
                public void handleEvent(org.eclipse.swt.widgets.Event event) {
                    switch (event.detail) {
                        case org.eclipse.swt.SWT.TRAVERSE_TAB_NEXT :
                        case org.eclipse.swt.SWT.TRAVERSE_TAB_PREVIOUS :
                            event.doit = true;
                            break;
                    }
                }
            });
        }
    }

    private void installMenu(final org.eclipse.swt.widgets.Control control, org.eclipse.swt.widgets.Menu menu) {
        if (menu != null) {
            control.setMenu(menu);
            control.addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
                public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                    control.setMenu(null);
                }
            });
        }
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isSpellCheckingEnabled() {
        return spellCheckingEnabled;
    }

    private void setContext() {
        if (contextService == null) {
            return;
        }
        if (contextActivation != null) {
            contextService.deactivateContext(contextActivation);
            contextActivation = null;
        }
        if ((contextService != null) && (extension.getEditorContextId() != null)) {
            contextActivation = contextService.activateContext(extension.getEditorContextId());
        }
    }

    private void setControl(org.eclipse.swt.widgets.Control control) {
        this.control = control;
    }

    public void setMode(org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode mode) {
        org.eclipse.core.runtime.Assert.isNotNull(mode);
        this.mode = mode;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setRenderingEngine(org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine renderingEngine) {
        this.renderingEngine = renderingEngine;
    }

    public void setSpellCheckingEnabled(boolean spellCheckingEnabled) {
        this.spellCheckingEnabled = spellCheckingEnabled;
    }

    public void setText(java.lang.String value) {
        this.text = value;
        this.textVersion++;
        org.eclipse.jface.text.source.SourceViewer viewer = getViewer();
        if (viewer != null) {
            viewer.getDocument().set(value);
        }
    }

    /**
     * Brings <code>control</code> to top.
     */
    private void show(org.eclipse.swt.widgets.Control control) {
        // no extension is available
        if (editorComposite == null) {
            return;
        }
        editorLayout.topControl = control;
        if (editorComposite.getParent().getLayout() instanceof org.eclipse.mylyn.commons.ui.FillWidthLayout) {
            ((org.eclipse.mylyn.commons.ui.FillWidthLayout) (editorComposite.getParent().getLayout())).flush();
        }
        editorComposite.layout();
        control.setFocus();
        fireStateChangedEvent();
    }

    protected void fireStateChangedEvent() {
        if (stateChangedListeners.isEmpty()) {
            return;
        }
        org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedEvent event = new org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedEvent();
        if ((defaultViewer != null) && (defaultViewer.getControl() == editorLayout.topControl)) {
            event.state = org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.State.DEFAULT;
        } else if ((editorViewer != null) && (editorViewer.getControl() == editorLayout.topControl)) {
            event.state = org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.State.EDITOR;
        } else if ((previewViewer != null) && (previewViewer.getControl() == editorLayout.topControl)) {
            event.state = org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.State.PREVIEW;
        } else if ((browserViewer != null) && (browserViewer.getControl() == editorLayout.topControl)) {
            event.state = org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.State.BROWSER;
        }
        java.lang.Object[] listeners = stateChangedListeners.getListeners();
        for (java.lang.Object listener : listeners) {
            ((org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedListener) (listener)).stateChanged(event);
        }
    }

    /**
     * Brings <code>viewer</code> to top.
     */
    private void show(org.eclipse.jface.text.source.SourceViewer viewer) {
        // WikiText modifies the document therefore, set a new document every time a viewer is changed to synchronize content between viewers
        // ensure that editor has an annotation model
        updateDocument(viewer, new org.eclipse.jface.text.Document(getText()), !viewer.isEditable());
        show(viewer.getControl());
    }

    public void showBrowser() {
        org.eclipse.mylyn.internal.tasks.ui.editors.BrowserPreviewViewer viewer = getBrowserViewer();
        viewer.update(getText());
        if (viewer != null) {
            show(viewer.getControl());
        }
    }

    public void showDefault() {
        show(getDefaultViewer());
    }

    public void showEditor() {
        if (getEditorViewer() != null) {
            show(getEditorViewer());
        } else {
            show(getDefaultViewer());
        }
    }

    private void showPreview(boolean sticky) {
        if ((!isReadOnly()) && (getPreviewViewer() != null)) {
            show(getPreviewViewer());
        }
    }

    public void showPreview() {
        showPreview(true);
    }

    private void unsetContext() {
        if (contextService == null) {
            return;
        }
        if (contextActivation != null) {
            contextService.deactivateContext(contextActivation);
            contextActivation = null;
        }
    }

    protected void valueChanged(java.lang.String value) {
    }

    public void enableAutoTogglePreview() {
        if ((!isReadOnly()) && (getPreviewViewer() != null)) {
            final org.eclipse.swt.events.MouseAdapter listener = new org.eclipse.swt.events.MouseAdapter() {
                private boolean toggled;

                @java.lang.Override
                public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
                    if ((!toggled) && (e.count == 1)) {
                        // delay switching in case user intended to select text
                        org.eclipse.swt.widgets.Display.getDefault().timerExec(org.eclipse.swt.widgets.Display.getDefault().getDoubleClickTime(), new java.lang.Runnable() {
                            public void run() {
                                if ((previewViewer.getTextWidget() == null) || previewViewer.getTextWidget().isDisposed()) {
                                    return;
                                }
                                if (previewViewer.getTextWidget().getSelectionCount() == 0) {
                                    int offset = previewViewer.getTextWidget().getCaretOffset();
                                    showEditor();
                                    editorViewer.getTextWidget().setCaretOffset(offset);
                                    // only do this once, let the user manage toggling from then on
                                    toggled = true;
                                }
                            }
                        });
                    }
                }
            };
            previewViewer.getTextWidget().addMouseListener(listener);
            // editorViewer.getTextWidget().addFocusListener(new FocusAdapter() {
            // @Override
            // public void focusLost(FocusEvent e) {
            // if (!previewSticky) {
            // showPreview(false);
            // }
            // }
            // });
        }
    }

    /**
     * Sets the background color for all instantiated viewers
     *
     * @param color
     */
    public void setBackground(org.eclipse.swt.graphics.Color color) {
        if ((editorComposite != null) && (!editorComposite.isDisposed())) {
            editorComposite.setBackground(color);
            for (org.eclipse.swt.widgets.Control child : editorComposite.getChildren()) {
                child.setBackground(color);
            }
        }
    }

    public void addStateChangedListener(org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedListener listener) {
        stateChangedListeners.add(listener);
    }

    public void removeStateChangedListener(org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedListener listener) {
        stateChangedListeners.remove(listener);
    }
}