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
package org.eclipse.mylyn.internal.tasks.ui.views;
import org.eclipse.mylyn.commons.ui.compatibility.CommonThemes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.IThemeManager;
/**
 * Derived from JUnitProgressBar.
 *
 * @author Mik Kersten
 */
public class WorkweekProgressBar extends org.eclipse.swt.widgets.Canvas {
    private static final int DEFAULT_HEIGHT = 5;

    private int currentTickCount = 0;

    private int maxTickCount = 0;

    private int colorBarWidth = 0;

    private final org.eclipse.swt.graphics.Color completedColor;

    private final org.eclipse.swt.widgets.Composite parent;

    public WorkweekProgressBar(org.eclipse.swt.widgets.Composite parent) {
        super(parent, org.eclipse.swt.SWT.NONE);
        this.parent = parent;
        parent.addControlListener(new org.eclipse.swt.events.ControlAdapter() {
            @java.lang.Override
            public void controlResized(org.eclipse.swt.events.ControlEvent e) {
                colorBarWidth = scale(currentTickCount);
                redraw();
            }
        });
        addPaintListener(new org.eclipse.swt.events.PaintListener() {
            public void paintControl(org.eclipse.swt.events.PaintEvent e) {
                paint(e);
            }
        });
        org.eclipse.ui.themes.IThemeManager themeManager = org.eclipse.ui.PlatformUI.getWorkbench().getThemeManager();
        completedColor = themeManager.getCurrentTheme().getColorRegistry().get(org.eclipse.mylyn.commons.ui.compatibility.CommonThemes.COLOR_COMPLETED_TODAY);
    }

    public void setMaximum(int max) {
        maxTickCount = max;
    }

    public void reset() {
        currentTickCount = 0;
        maxTickCount = 0;
        colorBarWidth = 0;
        redraw();
    }

    public void reset(int ticksDone, int maximum) {
        currentTickCount = ticksDone;
        maxTickCount = maximum;
        colorBarWidth = scale(ticksDone);
        computeSize(org.eclipse.swt.SWT.DEFAULT, org.eclipse.swt.SWT.DEFAULT, true);
        redraw();
    }

    private void paintStep(int startX, int endX) {
        org.eclipse.swt.graphics.GC gc = new org.eclipse.swt.graphics.GC(this);
        setStatusColor(gc);
        org.eclipse.swt.graphics.Rectangle rect = getClientArea();
        startX = java.lang.Math.max(1, startX);
        gc.fillRectangle(startX, 1, endX - startX, rect.height - 2);
        gc.dispose();
    }

    private void setStatusColor(org.eclipse.swt.graphics.GC gc) {
        gc.setBackground(completedColor);
    }

    private int scale(int value) {
        if (maxTickCount > 0) {
            // TODO: should probably get own client area, not parent's
            org.eclipse.swt.graphics.Rectangle r = parent.getClientArea();
            if (r.width != 0) {
                return java.lang.Math.max(0, (value * (r.width - 2)) / maxTickCount);
            }
        }
        return value;
    }

    private void drawBevelRect(org.eclipse.swt.graphics.GC gc, int x, int y, int w, int h, org.eclipse.swt.graphics.Color topleft, org.eclipse.swt.graphics.Color bottomright) {
        gc.setForeground(topleft);
        gc.drawLine(x, y, (x + w) - 1, y);
        gc.drawLine(x, y, x, (y + h) - 1);
        gc.setForeground(bottomright);
        gc.drawLine(x + w, y, x + w, y + h);
        gc.drawLine(x, y + h, x + w, y + h);
    }

    private void paint(org.eclipse.swt.events.PaintEvent event) {
        org.eclipse.swt.graphics.GC gc = event.gc;
        org.eclipse.swt.widgets.Display disp = getDisplay();
        org.eclipse.swt.graphics.Rectangle rect = getClientArea();
        gc.fillRectangle(rect);
        drawBevelRect(gc, rect.x, rect.y, rect.width - 1, rect.height - 1, disp.getSystemColor(org.eclipse.swt.SWT.COLOR_WIDGET_NORMAL_SHADOW), disp.getSystemColor(org.eclipse.swt.SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
        setStatusColor(gc);
        colorBarWidth = java.lang.Math.min(rect.width - 2, colorBarWidth);
        gc.fillRectangle(1, 1, colorBarWidth, rect.height - 2);
    }

    @java.lang.Override
    public org.eclipse.swt.graphics.Point computeSize(int wHint, int hHint, boolean changed) {
        checkWidget();
        org.eclipse.swt.graphics.Point size = new org.eclipse.swt.graphics.Point(parent.getSize().x, org.eclipse.mylyn.internal.tasks.ui.views.WorkweekProgressBar.DEFAULT_HEIGHT);// parent.getSize().y);

        if (wHint != org.eclipse.swt.SWT.DEFAULT) {
            size.x = wHint;
        }
        if (hHint != org.eclipse.swt.SWT.DEFAULT) {
            size.y = hHint;
        }
        return size;
    }

    public void setCount(int count) {
        currentTickCount++;
        int x = colorBarWidth;
        colorBarWidth = scale(currentTickCount);
        if (currentTickCount == maxTickCount) {
            colorBarWidth = getClientArea().width - 1;
        }
        paintStep(x, colorBarWidth);
    }

    public void step(int failures) {
        currentTickCount++;
        int x = colorBarWidth;
        colorBarWidth = scale(currentTickCount);
        if (currentTickCount == maxTickCount) {
            colorBarWidth = getClientArea().width - 1;
        }
        paintStep(x, colorBarWidth);
    }
}