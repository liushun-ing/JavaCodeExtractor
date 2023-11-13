/**
 * *****************************************************************************
 * Copyright (c) 2010 Andreas Hoehmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Hoehmann - initial API and implementation
 *     Tasktop Technologies - improvements
 * *****************************************************************************
 */
package org.eclipse.mylyn.tasks.ui.editors;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;
/**
 * Resolver to resolve variables from the active task.
 * <p>
 * This resolver can handle the following variables:
 * <ul>
 * <li><code>{@link #TYPE_ACTIVE_TASK_PREFIX}<code> - the prefix for the active task (if any) or null</li>
 * <li><code>{@link #TYPE_ACTIVE_TASK_KEY}<code> - the prefix for the active task (if any) or null</li>
 * </ul>
 * </p>
 * <h4>Adding Additional Variables</h4>
 * <p>
 * First add another template-resolver (for detail information see <a href=
 * "http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/extension-points/org_eclipse_ui_editors_templates.html"
 * >here</a>) for the <code>org.eclipse.mylyn.internal.ide.ui.editors.templates</code>.
 *
 * <pre>
 * &lt;extension point="org.eclipse.ui.editors.templates"
 *            id="org.eclipse.mylyn.internal.ide.ui.editors.templates"&gt;
 *   &lt;resolver
 *      class="org.eclipse.mylyn.internal.ide.ui.TasksTemplateVariableResolver"
 *      contextTypeId="<b>java</b>"
 *      description="%MylynTemplateVariableResolver.<b>activeTaskId.description</b>"
 *      name="<b>Active Task ID</b>"
 *      type="<b>activeTaskKey</b>"&gt;
 *   &lt;/resolver&gt;
 * &lt;/extension&gt;
 * </pre>
 *
 * You have to define/change the:
 * <ul>
 * <li><b>contextTypeId</b> - you should define an resolver for the "java" and the "javadoc" context and maybe more
 * contexts.</li>
 * <li><b>type</b> - this is the property which can be used by users in there templates</li>
 * <li><b>description</b> - this is the localized description for the resolver, have a look into
 * <code>plugin.properties</code></li>
 * <li><b>name</b> - the display name of the resolver (for internal usage only)</li>
 * </ul>
 * </p>
 * <p>
 * The you must put your resolver code here.
 * <ol>
 * <li>add a new constant for the resolver-<b>type</b> ("activeTaskKey")</li>
 * <li>check your type in {@link #resolve(TemplateContext)} and resolve the type, return <code>null</code> if nothing
 * can resolved, always <b>trim</b> your result</li>
 * </ol>
 * </p>
 * <p>
 * <i>Each returned variable should be trimmed to avoid avoid unnecessary spaces between the resolved variables, i.e.
 * ("${activeTaskPrefix}${activeTaskKey}" should become "task2" and not "task 2").</i>
 * </p>
 *
 * @author Andreas Hoehmann
 * @author Steffen Pingel
 * @since 3.7
 */
public class TaskTemplateVariableResolver extends org.eclipse.jface.text.templates.TemplateVariableResolver {
    /**
     * Resolver type to provide the ID of the active task.
     */
    public static final java.lang.String TYPE_ACTIVE_TASK_KEY = "activeTaskKey";// $NON-NLS-1$


    /**
     * Resolver type to provide the prefix (i.e. "bug") of the active task.
     */
    public static final java.lang.String TYPE_ACTIVE_TASK_PREFIX = "activeTaskPrefix";// $NON-NLS-1$


    /**
     * {@inheritDoc }
     */
    @java.lang.Override
    protected java.lang.String resolve(final org.eclipse.jface.text.templates.TemplateContext context) {
        final java.lang.String type = getType();
        if (org.eclipse.mylyn.tasks.ui.editors.TaskTemplateVariableResolver.TYPE_ACTIVE_TASK_KEY.equalsIgnoreCase(type)) {
            final org.eclipse.mylyn.tasks.core.ITask activeTask = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask();
            if (activeTask != null) {
                java.lang.String taskKey = activeTask.getTaskKey();
                if (taskKey == null) {
                    // use the task-id, i.e. for a local task, such a task doesn't have a task-key
                    taskKey = activeTask.getTaskId();
                }
                if (taskKey != null) {
                    return taskKey.trim();
                }
            }
        } else if (org.eclipse.mylyn.tasks.ui.editors.TaskTemplateVariableResolver.TYPE_ACTIVE_TASK_PREFIX.equalsIgnoreCase(type)) {
            final org.eclipse.mylyn.tasks.core.ITask activeTask = org.eclipse.mylyn.tasks.ui.TasksUi.getTaskActivityManager().getActiveTask();
            if (activeTask != null) {
                java.lang.String taskPrefix = org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal.getTaskPrefix(activeTask.getConnectorKind());
                if (taskPrefix != null) {
                    return taskPrefix;
                }
            }
        }
        return null;
    }
}