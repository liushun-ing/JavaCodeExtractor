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
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.editors;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.mylyn.commons.ui.compatibility.CommonThemes;
import org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport;
import org.eclipse.mylyn.internal.tasks.ui.OptionsProposalProvider;
import org.eclipse.mylyn.internal.tasks.ui.PersonProposalLabelProvider;
import org.eclipse.mylyn.internal.tasks.ui.PersonProposalProvider;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.LabelsAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent.EventKind;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.themes.IThemeManager;
/**
 *
 * @author Steffen Pingel
 * @since 3.0
 */
// TODO EDITOR rename to AttributeUiToolkit?
public class AttributeEditorToolkit {
    private final org.eclipse.swt.graphics.Color colorIncoming;

    private org.eclipse.swt.widgets.Menu menu;

    private org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine renderingEngine;

    private final org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport textSupport;

    @java.lang.Deprecated
    public org.eclipse.ui.handlers.IHandlerActivation contentAssistHandlerActivation;

    AttributeEditorToolkit(org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport textSupport) {
        this.textSupport = textSupport;
        org.eclipse.ui.themes.IThemeManager themeManager = org.eclipse.ui.PlatformUI.getWorkbench().getThemeManager();
        colorIncoming = themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_INCOMING_BACKGROUND);
    }

    public void adapt(org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor editor) {
        if (editor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.LabelsAttributeEditor) {
            org.eclipse.swt.widgets.Control control = editor.getControl();
            org.eclipse.jface.fieldassist.IContentProposalProvider contentProposalProvider = createContentProposalProvider(editor);
            if (contentProposalProvider != null) {
                org.eclipse.ui.fieldassist.ContentAssistCommandAdapter adapter = createContentAssistCommandAdapter(control, contentProposalProvider);
                adapter.setAutoActivationCharacters(null);
                adapter.setProposalAcceptanceStyle(org.eclipse.jface.fieldassist.ContentProposalAdapter.PROPOSAL_REPLACE);
            }
        } else if (((editor.getControl() instanceof org.eclipse.swt.widgets.Text) || (editor.getControl() instanceof org.eclipse.swt.custom.CCombo)) || (editor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor)) {
            org.eclipse.swt.widgets.Control control = (editor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor) ? ((org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor) (editor)).getText() : editor.getControl();
            if (control == null) {
                // fall back in case getText() returns null
                control = editor.getControl();
            }
            if ((!editor.isReadOnly()) && hasContentAssist(editor.getTaskAttribute())) {
                org.eclipse.jface.fieldassist.IContentProposalProvider contentProposalProvider = createContentProposalProvider(editor);
                org.eclipse.jface.viewers.ILabelProvider labelPropsalProvider = createLabelProposalProvider(editor.getTaskAttribute());
                if ((contentProposalProvider != null) && (labelPropsalProvider != null)) {
                    org.eclipse.ui.fieldassist.ContentAssistCommandAdapter adapter = createContentAssistCommandAdapter(control, contentProposalProvider);
                    adapter.setLabelProvider(labelPropsalProvider);
                    adapter.setProposalAcceptanceStyle(org.eclipse.jface.fieldassist.ContentProposalAdapter.PROPOSAL_REPLACE);
                    if (editor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor) {
                        ((org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor) (editor)).setContentAssistCommandAdapter(adapter);
                    }
                }
            }
        } else if (editor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor) {
            org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor richTextEditor = ((org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor) (editor));
            boolean spellCheck = hasSpellChecking(editor.getTaskAttribute());
            final org.eclipse.jface.text.source.SourceViewer viewer = richTextEditor.getViewer();
            textSupport.install(viewer, spellCheck);
            if ((!editor.isReadOnly()) && (richTextEditor.getMode() == org.eclipse.mylyn.internal.tasks.ui.editors.RepositoryTextViewerConfiguration.Mode.TASK_RELATION)) {
                installContentAssistControlDecoration(viewer.getControl());
            }
            installMenu(viewer.getControl());
        } else {
            final org.eclipse.jface.text.TextViewer viewer = org.eclipse.mylyn.commons.workbench.editors.CommonTextSupport.getTextViewer(editor.getControl());
            if (viewer != null) {
                textSupport.install(viewer, false);
                installMenu(viewer.getControl());
            }
        }
        // for outline
        org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil.setMarker(editor.getControl(), editor.getTaskAttribute().getId());
        editor.decorate(getColorIncoming());
    }

    org.eclipse.ui.fieldassist.ContentAssistCommandAdapter createContentAssistCommandAdapter(org.eclipse.swt.widgets.Control control, org.eclipse.jface.fieldassist.IContentProposalProvider proposalProvider) {
        return new org.eclipse.ui.fieldassist.ContentAssistCommandAdapter(control, getContentAdapter(control), proposalProvider, org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new char[0], true);
    }

    private org.eclipse.jface.fieldassist.IControlContentAdapter getContentAdapter(org.eclipse.swt.widgets.Control control) {
        if (control instanceof org.eclipse.swt.widgets.Combo) {
            return new org.eclipse.jface.fieldassist.ComboContentAdapter();
        } else if (control instanceof org.eclipse.swt.widgets.Text) {
            return new org.eclipse.jface.fieldassist.TextContentAdapter();
        }
        return null;
    }

    private void installMenu(final org.eclipse.swt.widgets.Control control) {
        if (menu != null) {
            control.setMenu(menu);
            control.addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
                public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                    control.setMenu(null);
                }
            });
        }
    }

    /**
     * Adds content assist to the given text field.
     *
     * @param text
     * 		text field to decorate.
     * @param proposalProvider
     * 		instance providing content proposals
     * @return the ContentAssistCommandAdapter for the field.
     */
    private org.eclipse.jface.fieldassist.ControlDecoration installContentAssistControlDecoration(org.eclipse.swt.widgets.Control control) {
        org.eclipse.jface.fieldassist.ControlDecoration controlDecoration = new org.eclipse.jface.fieldassist.ControlDecoration(control, org.eclipse.swt.SWT.TOP | org.eclipse.swt.SWT.LEFT);
        controlDecoration.setShowOnlyOnFocus(true);
        org.eclipse.jface.fieldassist.FieldDecoration contentProposalImage = org.eclipse.jface.fieldassist.FieldDecorationRegistry.getDefault().getFieldDecoration(org.eclipse.jface.fieldassist.FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
        controlDecoration.setImage(contentProposalImage.getImage());
        org.eclipse.ui.keys.IBindingService bindingService = ((org.eclipse.ui.keys.IBindingService) (org.eclipse.ui.PlatformUI.getWorkbench().getService(org.eclipse.ui.keys.IBindingService.class)));
        controlDecoration.setDescriptionText(org.eclipse.osgi.util.NLS.bind(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.AttributeEditorToolkit_Content_Assist_Available__X_, bindingService.getBestActiveBindingFormattedFor(org.eclipse.ui.fieldassist.ContentAssistCommandAdapter.CONTENT_PROPOSAL_COMMAND)));
        return controlDecoration;
    }

    /**
     * Creates an IContentProposalProvider to provide content assist proposals for the given attribute.
     *
     * @param editor
     * 		editor for which to provide content assist.
     * @return the IContentProposalProvider.
     */
    org.eclipse.jface.fieldassist.IContentProposalProvider createContentProposalProvider(org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor editor) {
        org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute = editor.getTaskAttribute();
        java.util.Map<java.lang.String, java.lang.String> proposals = attribute.getTaskData().getAttributeMapper().getOptions(attribute);
        if ((editor instanceof org.eclipse.mylyn.internal.tasks.ui.editors.LabelsAttributeEditor) && (!attribute.getMetaData().getKind().equals(org.eclipse.mylyn.tasks.core.data.TaskAttribute.KIND_PEOPLE))) {
            return new org.eclipse.mylyn.internal.tasks.ui.OptionsProposalProvider(proposals, org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_MULTI_SELECT.equals(attribute.getMetaData().getType()));
        }
        return new org.eclipse.mylyn.internal.tasks.ui.PersonProposalProvider(null, attribute.getTaskData(), proposals);
    }

    private org.eclipse.jface.viewers.ILabelProvider createLabelProposalProvider(org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute) {
        return new org.eclipse.mylyn.internal.tasks.ui.PersonProposalLabelProvider();
    }

    void dispose() {
        // FIXME textSupport.deactivateHandlers();
    }

    public org.eclipse.swt.graphics.Color getColorIncoming() {
        return colorIncoming;
    }

    org.eclipse.swt.widgets.Menu getMenu() {
        return menu;
    }

    /**
     * Subclasses that support HTML preview of ticket description and comments override this method to return an
     * instance of AbstractRenderingEngine
     *
     * @return <code>null</code> if HTML preview is not supported for the repository (default)
     * @since 2.1
     */
    public org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine getRenderingEngine(org.eclipse.mylyn.tasks.core.data.TaskAttribute attribute) {
        return renderingEngine;
    }

    /**
     * Called to check if there's content assist available for the given attribute.
     *
     * @param attribute
     * 		the attribute
     * @return true if content assist is available for the specified attribute.
     */
    private boolean hasContentAssist(org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        java.lang.String type = taskAttribute.getMetaData().getType();
        if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_PERSON.equals(type)) {
            return true;
        } else if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_TASK_DEPENDENCY.equals(type)) {
            return true;
        }
        return false;
    }

    boolean hasSpellChecking(org.eclipse.mylyn.tasks.core.data.TaskAttribute taskAttribute) {
        java.lang.String type = taskAttribute.getMetaData().getType();
        if (org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_LONG_RICH_TEXT.equals(type) || org.eclipse.mylyn.tasks.core.data.TaskAttribute.TYPE_SHORT_RICH_TEXT.equals(type)) {
            return true;
        }
        return false;
    }

    void setMenu(org.eclipse.swt.widgets.Menu menu) {
        this.menu = menu;
    }

    public void setRenderingEngine(org.eclipse.mylyn.tasks.ui.editors.AbstractRenderingEngine renderingEngine) {
        this.renderingEngine = renderingEngine;
    }

    /**
     * Adds input validation to an attribute editor and a controlDecoration if invalid
     *
     * @since 3.5
     */
    public static void createValidator(final org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor attributeEditor, org.eclipse.swt.widgets.Control control, final org.eclipse.jface.dialogs.IInputValidator validator) {
        org.eclipse.core.runtime.Assert.isNotNull(validator);
        org.eclipse.core.runtime.Assert.isNotNull(control);
        org.eclipse.core.runtime.Assert.isNotNull(attributeEditor);
        final org.eclipse.jface.fieldassist.ControlDecoration decoration = new org.eclipse.jface.fieldassist.ControlDecoration(control, org.eclipse.swt.SWT.BOTTOM | org.eclipse.swt.SWT.LEFT);
        decoration.setMarginWidth(2);
        org.eclipse.jface.fieldassist.FieldDecoration errorDecoration = org.eclipse.jface.fieldassist.FieldDecorationRegistry.getDefault().getFieldDecoration(org.eclipse.jface.fieldassist.FieldDecorationRegistry.DEC_ERROR);
        decoration.setImage(errorDecoration.getImage());
        decoration.hide();
        final org.eclipse.mylyn.tasks.core.data.TaskDataModelListener validationListener = new org.eclipse.mylyn.tasks.core.data.TaskDataModelListener() {
            @java.lang.Override
            public void attributeChanged(org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent event) {
                if (event.getTaskAttribute().equals(attributeEditor.getTaskAttribute())) {
                    java.lang.String validationMessage = validator.isValid(attributeEditor.getTaskAttribute().getValue());
                    if (validationMessage == null) {
                        decoration.hide();
                    } else {
                        decoration.setDescriptionText(validationMessage);
                        decoration.show();
                    }
                }
            }
        };
        attributeEditor.getModel().addModelListener(validationListener);
        control.addDisposeListener(new org.eclipse.swt.events.DisposeListener() {
            public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
                decoration.dispose();
                attributeEditor.getModel().removeModelListener(validationListener);
            }
        });
        validationListener.attributeChanged(new org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent(attributeEditor.getModel(), org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent.EventKind.CHANGED, attributeEditor.getTaskAttribute()));
    }
}