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
package org.talend.dataprofiler.core.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.FileEditorInput;
import org.talend.core.model.properties.Property;
import org.talend.dataprofiler.core.ui.pref.EditorPreferencePage;
import org.talend.dataprofiler.core.ui.utils.TOPChartUtils;
import org.talend.dq.helper.PropertyHelper;

/**
 * DOC rli class global comment. Detailled comment
 */
public abstract class AbstractFormPage extends FormPage {

    protected boolean isDirty = false;

    protected FormToolkit toolkit;

    protected CommonFormEditor currentEditor;

    protected Boolean foldingState;

    private int sectionCount = 0;

    private List<ExpandableComposite> expandCompositeList;

    public AbstractFormPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        this.toolkit = this.getEditor().getToolkit();
        this.currentEditor = (CommonFormEditor) editor;
        this.expandCompositeList = new ArrayList<ExpandableComposite>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.editor.FormPage#initialize(org.eclipse.ui.forms.editor.FormEditor)
     */
    @Override
    public void initialize(FormEditor editor) {
        super.initialize(editor);

        initFoldingState();
    }

    /**
     * DOC bZhou Comment method "createSection".
     * 
     * @param form
     * @param parent
     * @param title
     * @return
     */
    public Section createSection(final ScrolledForm form, Composite parent, String title) {
        return createSection(form, parent, title, null);
    }

    /**
     * DOC bZhou Comment method "createSection".
     * 
     * @param form
     * @param parent
     * @param title
     * @param description
     * @return
     */
    public Section createSection(final ScrolledForm form, Composite parent, String title, String description) {
        int style = Section.DESCRIPTION | Section.TWISTIE | Section.TITLE_BAR;

        if (description == null) {
            style = Section.TWISTIE | Section.TITLE_BAR;
        }

        Section section = toolkit.createSection(parent, style);

        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

        section.addExpansionListener(new ExpansionAdapter() {

            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }

        });

        section.setText(title);
        section.setDescription(description);

        if (foldingState == null) {
            section.setExpanded(sectionCount == 0);
        } else {
            section.setExpanded(foldingState);
        }

        registerSection(section);

        sectionCount++;

        return section;
    }

    /**
     * DOC bZhou Comment method "initFoldingState".
     */
    private void initFoldingState() {
        int foldType = EditorPreferencePage.getCurrentFolding();

        switch (foldType) {
        case EditorPreferencePage.FOLDING_1:
            foldingState = true;
            break;
        case EditorPreferencePage.FOLDING_2:
            foldingState = false;
            break;
        default:
        }
    }

    /**
     * DOC bZhou Comment method "registerSection".
     * 
     * @param composite
     */
    public void registerSection(ExpandableComposite composite) {
        expandCompositeList.add(composite);
        currentEditor.getToolBar().getEditorBarWrap().addExpandableComposite(composite);
    }

    /**
     * Getter for expandCompositeList.
     * 
     * @return the expandCompositeList
     */
    public List<ExpandableComposite> getExpandCompositeList() {
        return expandCompositeList;
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || isDirty;
    }

    protected Property getProperty() {
        Property property = null;
        if (getEditorInput() instanceof AbstractItemEditorInput) {
            AbstractItemEditorInput editorInput = (AbstractItemEditorInput) getEditorInput();
            if (editorInput.getItem() != null) {
                property = editorInput.getItem().getProperty();
            }
        } else if (getEditorInput() instanceof FileEditorInput) {
            FileEditorInput editorInput = (FileEditorInput) getEditorInput();
            property = PropertyHelper.getProperty(editorInput.getFile());
        }

        return property;
    }

    /**
     * DOC bZhou Comment method "setDirty".
     * 
     * @param isDirty
     */
    public abstract void setDirty(boolean isDirty);

    /**
     * Added TDQ-9797 if show the chart or not
     */
    protected boolean canShowChart() {
        return (!EditorPreferencePage.isHideGraphics() && TOPChartUtils.getInstance().isTOPChartInstalled());
    }
}
