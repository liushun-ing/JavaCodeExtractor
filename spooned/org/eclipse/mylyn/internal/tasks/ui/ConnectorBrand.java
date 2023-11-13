/**
 * *****************************************************************************
 * Copyright (c) 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.mylyn.internal.tasks.ui;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.osgi.util.NLS;
public class ConnectorBrand {
    private final org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector;

    private final java.lang.String brandId;

    public ConnectorBrand(org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector connector, java.lang.String brandId) {
        this.connector = connector;
        this.brandId = brandId;
    }

    public org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector getConnector() {
        return connector;
    }

    public java.lang.String getBrandId() {
        return brandId;
    }

    @java.lang.Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (brandId == null ? 0 : brandId.hashCode());
        result = (prime * result) + (connector == null ? 0 : connector.hashCode());
        return result;
    }

    @java.lang.Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand)) {
            return false;
        }
        org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand other = ((org.eclipse.mylyn.internal.tasks.ui.ConnectorBrand) (obj));
        if (brandId == null) {
            if (other.brandId != null) {
                return false;
            }
        } else if (!brandId.equals(other.brandId)) {
            return false;
        }
        if (connector == null) {
            if (other.connector != null) {
                return false;
            }
        } else if (!connector.equals(other.connector)) {
            return false;
        }
        return true;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return org.eclipse.osgi.util.NLS.bind("<{0},{1}>", connector.getConnectorKind(), brandId);// $NON-NLS-1$

    }
}