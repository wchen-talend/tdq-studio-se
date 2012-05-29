// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.talend.dataprofiler.core.helper.WorkspaceResourceHelper;
import org.talend.dq.nodes.SourceFileRepNode;
import org.talend.dq.nodes.SourceFileSubFolderNode;
import org.talend.repository.model.IRepositoryNode;
import org.talend.utils.sugars.ReturnCode;

/**
 * DOC xqliu class global comment. Detailled comment
 */
@PrepareForTest({ WorkspaceResourceHelper.class })
public class TOPRepositoryServiceTest {

    @Rule
    public PowerMockRule powerMockRule = new PowerMockRule();

    /**
     * Test method for
     * {@link org.talend.dataprofiler.core.service.TOPRepositoryService#sourceFileOpening(org.talend.repository.model.RepositoryNode)}
     * .
     */
    @Test
    public void testSourceFileOpening() {
        // test for SourceFileRepNode
        SourceFileRepNode fileNodeMock = mock(SourceFileRepNode.class);
        String nodeLabel = "nodeLabel"; //$NON-NLS-1$
        when(fileNodeMock.getLabel()).thenReturn(nodeLabel);

        stub(method(WorkspaceResourceHelper.class, "sourceFileHasBeenOpened", SourceFileRepNode.class)).toReturn(Boolean.TRUE); //$NON-NLS-1$

        TOPRepositoryService service = new TOPRepositoryService();

        assertTrue(service.sourceFileOpening(fileNodeMock));
    }

    /**
     * Test method for
     * {@link org.talend.dataprofiler.core.service.TOPRepositoryService#sourceFileOpening(org.talend.repository.model.RepositoryNode)}
     * .
     */
    @Test
    public void testSourceFileOpening2() {
        // test for SourceFileSubFolderNode
        SourceFileSubFolderNode folderNodeMock = mock(SourceFileSubFolderNode.class);

        SourceFileRepNode nodeMock = mock(SourceFileRepNode.class);
        List<IRepositoryNode> nodeList = new ArrayList<IRepositoryNode>();
        nodeList.add(nodeMock);

        when(folderNodeMock.getChildren()).thenReturn(nodeList);

        boolean ok = Boolean.TRUE;
        String msg = "msg"; //$NON-NLS-1$
        ReturnCode rc = new ReturnCode(msg, ok);

        stub(method(WorkspaceResourceHelper.class, "checkSourceFileNodeOpening", SourceFileRepNode.class)).toReturn(rc); //$NON-NLS-1$

        TOPRepositoryService service = new TOPRepositoryService();

        assertTrue(service.sourceFileOpening(folderNodeMock));
    }
}
