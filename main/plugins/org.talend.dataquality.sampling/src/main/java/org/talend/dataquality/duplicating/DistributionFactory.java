// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.duplicating;

import org.apache.commons.math3.distribution.AbstractIntegerDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.GeometricDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;

public class DistributionFactory {

    public static AbstractIntegerDistribution createDistribution(String name, double expectation) {
        AbstractIntegerDistribution distro = null;
        if (expectation < 2) {
            throw new IllegalArgumentException("The expectation value must be greater than or equals to 2"); //$NON-NLS-1$
        }
        if (name.equals("BERNOULLI")) { //$NON-NLS-1$
            distro = new BinomialDistribution((int) ((expectation - 2) * 2), 0.5);
        } else if (name.equals("GEOMETRIC")) { //$NON-NLS-1$
            distro = new GeometricDistribution(1 / (expectation - 1));
        } else if (name.equals("POISSON")) { //$NON-NLS-1$
            distro = new PoissonDistribution(expectation - 2 + java.lang.Double.MIN_VALUE);
        }
        return distro;
    }

    public static AbstractIntegerDistribution createDistribution(String name, double expectation, long seed) {
        AbstractIntegerDistribution distro = createDistribution(name, expectation);
        distro.reseedRandomGenerator(seed);
        return distro;
    }
}
