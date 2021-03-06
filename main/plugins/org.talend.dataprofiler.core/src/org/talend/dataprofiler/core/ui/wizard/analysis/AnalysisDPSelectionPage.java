// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.wizard.analysis;

import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.dataprofiler.core.ui.dialog.provider.DBTablesViewLabelProvider;
import org.talend.dataprofiler.core.ui.filters.DQFolderFliter;
import org.talend.dataprofiler.core.ui.filters.EMFObjFilter;
import org.talend.dataprofiler.core.ui.filters.RecycleBinFilter;
import org.talend.dataprofiler.core.ui.filters.TDQEEConnectionFolderFilter;
import org.talend.dataprofiler.core.ui.views.provider.ResourceViewContentProvider;
import org.talend.dq.helper.RepositoryNodeHelper;

/**
 * DOC mzhao class global comment. This class provide abstract methods for client to add different filter and listener.
 */
public abstract class AnalysisDPSelectionPage extends AbstractAnalysisWizardPage {

    private TreeViewer fViewer;

    protected ILabelProvider fLabelProvider;

    protected ITreeContentProvider fContentProvider;

    private String nameLabTxt = null;

    private boolean multiSelect;

    public AnalysisDPSelectionPage(String labText, ResourceViewContentProvider contentProvider) {
        init("", "", contentProvider, labText); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public AnalysisDPSelectionPage(String title, String message, String labText, ResourceViewContentProvider contentProvider) {
        init(title, message, contentProvider, labText);
    }

    public AnalysisDPSelectionPage(String title, String message, String labText, ResourceViewContentProvider contentProvider,
            boolean multiSelect) {
        init(title, message, contentProvider, labText);
        this.multiSelect = multiSelect;
    }

    private void init(String title, String message, ResourceViewContentProvider contentProvider, String labText) {
        setTitle(title); //$NON-NLS-1$
        setMessage(message); //$NON-NLS-1$
        setPageComplete(false);
        nameLabTxt = labText;
        fLabelProvider = new DBTablesViewLabelProvider();
        fContentProvider = contentProvider;
    }

    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);

        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText(nameLabTxt); //$NON-NLS-1$

        createMetaDataTree(container);
        setControl(container);
        addFilters(new EMFObjFilter(), new DQFolderFliter(true), new TDQEEConnectionFolderFilter(), new RecycleBinFilter());
        addListeners();
    }

    private void createMetaDataTree(Composite parent) {
        Composite treeContainer = new Composite(parent, SWT.NONE);
        treeContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
        treeContainer.setLayout(new FillLayout());
        int style = this.multiSelect ? SWT.BORDER | SWT.MULTI : SWT.BORDER;
        fViewer = new TreeViewer(treeContainer, style);
        fViewer.setContentProvider(fContentProvider);
        fViewer.setLabelProvider(fLabelProvider);
        fViewer.setInput(RepositoryNodeHelper.getRootNode(ERepositoryObjectType.METADATA, true));
        // MOD gdbu 2011-7-25 bug : 23220
        ((ResourceViewContentProvider) fContentProvider).setTreeViewer(fViewer);
        // ~23220
    }

    protected abstract void addListeners();

    protected void addFilters(ViewerFilter... filters) {
        for (ViewerFilter filter : filters) {
            fViewer.addFilter(filter);
        }
    }

    protected void addListener(IDoubleClickListener doubleClickListener) {
        fViewer.addDoubleClickListener(doubleClickListener);
    }

    protected void addListener(ISelectionChangedListener selectionChangedListener) {
        fViewer.addSelectionChangedListener(selectionChangedListener);
    }

}
