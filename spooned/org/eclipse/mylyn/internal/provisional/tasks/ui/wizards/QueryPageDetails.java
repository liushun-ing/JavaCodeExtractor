/**
 * *****************************************************************************
 * Copyright (c) 2015 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.provisional.tasks.ui.wizards;
public class QueryPageDetails {
    private final boolean needsTitle;

    private final java.lang.String queryUrlPart;

    private final java.lang.String pageTitle;

    private final java.lang.String pageDescription;

    private final java.lang.String urlPattern;

    private final java.lang.String queryAttributeName;

    public QueryPageDetails(boolean needsTitle, java.lang.String queryUrlPart, java.lang.String pageTitle, java.lang.String pageDescription, java.lang.String urlPattern, java.lang.String queryAttributeName) {
        super();
        this.needsTitle = needsTitle;
        this.queryUrlPart = queryUrlPart;
        this.pageTitle = pageTitle;
        this.pageDescription = pageDescription;
        this.urlPattern = urlPattern;
        this.queryAttributeName = queryAttributeName;
    }

    public boolean needsTitle() {
        return needsTitle;
    }

    public java.lang.String getQueryUrlPart() {
        return queryUrlPart;
    }

    public java.lang.String getPageTitle() {
        return pageTitle;
    }

    public java.lang.String getPageDescription() {
        return pageDescription;
    }

    public java.lang.String getUrlPattern() {
        return urlPattern;
    }

    public java.lang.String getQueryAttributeName() {
        return queryAttributeName;
    }
}