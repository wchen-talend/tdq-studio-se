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
package org.talend.dataquality.record.linkage.grouping.swoosh;

import java.util.Map;

import org.talend.dataquality.record.linkage.utils.SurvivorShipAlgorithmEnum;

/**
 * A concreate class that hold all parameters that is needed by the swoosh algorithm.
 */
public class SurvivorShipAlgorithmParams {

    /**
     * The order in array is conform to the matching key order choosen.
     */
    private SurvivorshipFunction[] surviorShipAlgos;

    private Map<Integer, SurvivorshipFunction> defaultSurviorshipRules;

    /**
     * Getter for surviorShipAlgos.
     * 
     * @return the surviorShipAlgos
     */
    public SurvivorshipFunction[] getSurviorShipAlgos() {
        return this.surviorShipAlgos;
    }

    /**
     * Sets the surviorShipAlgos.
     * 
     * @param surviorShipAlgos the surviorShipAlgos to set
     */
    public void setSurviorShipAlgos(SurvivorshipFunction[] surviorShipAlgos) {
        this.surviorShipAlgos = surviorShipAlgos;
    }

    /**
     * Getter for defaultSurviorshipRules.
     * 
     * @return the defaultSurviorshipRules
     */
    public Map<Integer, SurvivorshipFunction> getDefaultSurviorshipRules() {
        return this.defaultSurviorshipRules;
    }

    /**
     * Sets the defaultSurviorshipRules.
     * 
     * @param defaultSurviorshipRules the defaultSurviorshipRules to set
     */
    public void setDefaultSurviorshipRules(Map<Integer, SurvivorshipFunction> defaultSurviorshipRules) {
        this.defaultSurviorshipRules = defaultSurviorshipRules;
    }

    public class SurvivorshipFunction {

        SurvivorShipAlgorithmEnum survivorShipFunction;

        // Will be usefull when the function is SurvivorShipAlgorithmEnum.MOST_TRUSTED_SOURCE.
        String parameter;

        /**
         * Getter for survivorShipFunction.
         * 
         * @return the survivorShipFunction
         */
        public SurvivorShipAlgorithmEnum getSurvivorShipFunction() {
            return this.survivorShipFunction;
        }

        /**
         * Sets the survivorShipFunction.
         * 
         * @param survivorShipFunction the survivorShipFunction to set
         */
        public void setSurvivorShipFunction(SurvivorShipAlgorithmEnum survivorShipFunction) {
            this.survivorShipFunction = survivorShipFunction;
        }

        /**
         * Getter for parameter.
         * 
         * @return the parameter
         */
        public String getParameter() {
            return this.parameter;
        }

        /**
         * Sets the parameter.
         * 
         * @param parameter the parameter to set
         */
        public void setParameter(String parameter) {
            this.parameter = parameter;
        }

    }
}
