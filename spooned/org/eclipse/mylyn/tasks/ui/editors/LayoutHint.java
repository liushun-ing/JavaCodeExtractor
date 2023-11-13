/**
 * *****************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
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
/**
 * A layout hint for attribute editors.
 *
 * @author Steffen Pingel
 * @since 3.0
 */
public class LayoutHint {
    /**
     *
     * @since 3.0
     */
    public static final int DEFAULT_PRIORITY = 10;

    /**
     *
     * @since 3.0
     */
    public enum ColumnSpan {

        MULTIPLE,
        SINGLE;}

    /**
     *
     * @since 3.0
     */
    public enum RowSpan {

        MULTIPLE,
        SINGLE;}

    /**
     *
     * @since 3.0
     */
    public org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan rowSpan;

    /**
     *
     * @since 3.0
     */
    public org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan columnSpan;

    /**
     *
     * @since 3.0
     */
    public LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan rowHint, org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan columnHint) {
        this.rowSpan = rowHint;
        this.columnSpan = columnHint;
    }

    /**
     * Clones <code>source</code>. Constructs a layout hint with a priority of {@link #DEFAULT_PRIORITY}, if
     * <code>source</code> is null.
     *
     * @param source
     * 		the layout hint to clone or <code>null</code>
     * @since 3.1
     */
    public LayoutHint(org.eclipse.mylyn.tasks.ui.editors.LayoutHint source) {
        if (source != null) {
            this.rowSpan = source.rowSpan;
            this.columnSpan = source.columnSpan;
        }
    }

    /**
     * Constructs a layout hint with a priority of {@link #DEFAULT_PRIORITY}.
     *
     * @since 3.1
     */
    public LayoutHint() {
    }

    /**
     * Returns a priority based on the size of the layout hint. The bigger the size the bigger the returned priority.
     * <p>
     * The priority is used to layout attribute editors.
     *
     * @since 3.0
     */
    public int getPriority() {
        if ((columnSpan == null) || (columnSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan.SINGLE)) {
            if ((rowSpan == null) || (rowSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE)) {
                return org.eclipse.mylyn.tasks.ui.editors.LayoutHint.DEFAULT_PRIORITY;
            } else {
                return org.eclipse.mylyn.tasks.ui.editors.LayoutHint.DEFAULT_PRIORITY * 2;
            }
        } else if ((rowSpan == null) || (rowSpan == org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan.SINGLE)) {
            return org.eclipse.mylyn.tasks.ui.editors.LayoutHint.DEFAULT_PRIORITY * 3;
        } else {
            return org.eclipse.mylyn.tasks.ui.editors.LayoutHint.DEFAULT_PRIORITY * 4;
        }
    }
}