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
package org.talend.dataprofiler.core.ui.views.provider;

import java.sql.Driver;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.utils.platform.PluginChecker;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.database.JavaSqlFactory;
import org.talend.core.model.metadata.builder.database.dburl.SupportDBUrlType;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.dataprofiler.core.ImageLib;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.manager.DQStructureManager;
import org.talend.dataprofiler.core.ui.exchange.ExchangeCategoryRepNode;
import org.talend.dataprofiler.core.ui.exchange.ExchangeComponentRepNode;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.reports.AnalysisMap;
import org.talend.dataquality.reports.TdReport;
import org.talend.dataquality.rules.MatchRuleDefinition;
import org.talend.dq.helper.RepositoryNodeHelper;
import org.talend.dq.helper.SqlExplorerUtils;
import org.talend.dq.nodes.AnalysisRepNode;
import org.talend.dq.nodes.DBCatalogRepNode;
import org.talend.dq.nodes.DBColumnFolderRepNode;
import org.talend.dq.nodes.DBColumnRepNode;
import org.talend.dq.nodes.DBConnectionFolderRepNode;
import org.talend.dq.nodes.DBConnectionRepNode;
import org.talend.dq.nodes.DBSchemaRepNode;
import org.talend.dq.nodes.DBTableFolderRepNode;
import org.talend.dq.nodes.DBTableRepNode;
import org.talend.dq.nodes.DBViewFolderRepNode;
import org.talend.dq.nodes.DBViewRepNode;
import org.talend.dq.nodes.DFColumnFolderRepNode;
import org.talend.dq.nodes.DFColumnRepNode;
import org.talend.dq.nodes.DFConnectionRepNode;
import org.talend.dq.nodes.DFTableRepNode;
import org.talend.dq.nodes.DQRepositoryNode;
import org.talend.dq.nodes.JrxmlTempleteRepNode;
import org.talend.dq.nodes.PatternRepNode;
import org.talend.dq.nodes.RecycleBinRepNode;
import org.talend.dq.nodes.ReportAnalysisRepNode;
import org.talend.dq.nodes.ReportRepNode;
import org.talend.dq.nodes.RuleRepNode;
import org.talend.dq.nodes.SourceFileRepNode;
import org.talend.dq.nodes.SysIndicatorDefinitionRepNode;
import org.talend.dq.nodes.hadoopcluster.HDFSOfHCConnectionNode;
import org.talend.dq.nodes.hadoopcluster.HadoopClusterConnectionRepNode;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.resource.EResourceConstant;
import org.talend.utils.exceptions.MissingDriverException;
import orgomg.cwm.foundation.softwaredeployment.DataManager;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * @author rli
 */
public class DQRepositoryViewLabelProvider extends AdapterFactoryLabelProvider implements IFontProvider {

    /**
     *
     */

    private static Font italicFont;

    private Logger log = Logger.getLogger(DQRepositoryViewLabelProvider.class);

    public DQRepositoryViewLabelProvider() {
        super(MNComposedAdapterFactory.getAdapterFactory());

        if (null == italicFont) {
            italicFont = Display.getDefault().getSystemFont();
            FontData[] exfds = italicFont.getFontData();
            if (exfds.length > 0) {
                FontData fd = exfds[0];
                fd.setStyle(SWT.ITALIC);
                italicFont = new Font(italicFont.getDevice(), fd);
            }
        }
    }

    @Override
    public Image getImage(Object element) {
        Image image = super.getImage(element);

        if (element instanceof IRepositoryNode) {
            IRepositoryNode node = (IRepositoryNode) element;
            IRepositoryViewObject viewObject = node.getObject();
            ENodeType type = node.getType();
            if (node instanceof ReportAnalysisRepNode) {
                image = ImageLib.getImage(ImageLib.ANALYSIS_OBJECT);
            }
            if (element instanceof RecycleBinRepNode) {
                image = ImageLib.getImage(ImageLib.RECYCLEBIN_EMPTY);
            } else if (type.equals(ENodeType.SYSTEM_FOLDER)) {
                String label = viewObject.getLabel();
                if (label.equals(EResourceConstant.DATA_PROFILING.getName())) {
                    image = ImageLib.getImage(ImageLib.DATA_PROFILING);
                } else if (label.equals(EResourceConstant.METADATA.getName())) {
                    image = ImageLib.getImage(ImageLib.METADATA);
                } else if (node instanceof DBConnectionFolderRepNode) {
                    image = ImageLib.getImage(ImageLib.CONNECTION);
                } else if (label.equals(EResourceConstant.FILEDELIMITED.getName())) {
                    image = ImageLib.getImage(ImageLib.FILE_DELIMITED);
                } else if (label.equals(EResourceConstant.HADOOP_CLUSTER.getName())) {
                    image = ImageLib.getImage(ImageLib.HADOOP_CLUSTER);
                } else if (label.equals(EResourceConstant.LIBRARIES.getName())) {
                    image = ImageLib.getImage(ImageLib.LIBRARIES);
                } else if (label.equals(EResourceConstant.EXCHANGE.getName())) {
                    image = ImageLib.getImage(ImageLib.EXCHANGE);
                } else {
                    image = ImageLib.getImage(ImageLib.FOLDERNODE_IMAGE);
                }
            } else if (type.equals(ENodeType.SIMPLE_FOLDER)) {
                image = ImageLib.getImage(ImageLib.FOLDERNODE_IMAGE);
            } else if (type.equals(ENodeType.REPOSITORY_ELEMENT)) {
                // TDQ-7560 when the image is a overlay image,use originalImageName to get the corresponding one.
                String originalImageName = null;
                if (node instanceof DBConnectionRepNode) {
                    originalImageName = ImageLib.TD_DATAPROVIDER;
                    if (!isSupportedConnection(node) || isNeedAddDriverConnection(node)) {
                        image = ImageLib.createErrorIcon(originalImageName).createImage();
                    } else if (isInvalidJDBCConnection(node)) {
                        image = ImageLib.createInvalidIcon(originalImageName).createImage();
                    } else {
                        image = ImageLib.getImage(ImageLib.TD_DATAPROVIDER);
                    }
                } else if (node instanceof DFConnectionRepNode) {
                    originalImageName = ImageLib.FILE_DELIMITED;
                } else if (node instanceof AnalysisRepNode) {
                    originalImageName = ImageLib.ANALYSIS_OBJECT;
                    image = addWarnIconIfNeeded(node, originalImageName);
                } else if (node instanceof ReportRepNode) {
                    originalImageName = ImageLib.REPORT_OBJECT;
                    image = addWarnIconIfNeeded(node, originalImageName);
                } else if (node instanceof SysIndicatorDefinitionRepNode) {
                    originalImageName = ImageLib.IND_DEFINITION;
                } else if (node instanceof PatternRepNode) {
                    originalImageName = ImageLib.PATTERN_REG;
                } else if (node instanceof RuleRepNode) {
                    if (((RuleRepNode) node).getRule() instanceof MatchRuleDefinition) {
                        originalImageName = ImageLib.MATCH_RULE_ICON;
                    } else {
                        originalImageName = ImageLib.DQ_RULE;
                    }
                } else if (node instanceof SourceFileRepNode) {
                    originalImageName = ImageLib.SOURCE_FILE;
                } else if (node instanceof HadoopClusterConnectionRepNode) {
                    originalImageName = ImageLib.HADOOP_CLUSTER;
                } else if (node instanceof HDFSOfHCConnectionNode) {
                    originalImageName = ImageLib.HDFS;
                } else if (node instanceof ExchangeCategoryRepNode || node instanceof ExchangeComponentRepNode) {
                    originalImageName = ImageLib.EXCHANGE;
                } else if (node instanceof RepositoryNode) {
                    // MOD qiongli 2011-1-18 get image for nodes in recycle bin
                    Image imageNode = getImageByContentType((RepositoryNode) node);
                    if (image != null) {
                        image = imageNode;
                    }
                }
                if (originalImageName != null
                        && !(node instanceof DBConnectionRepNode || node instanceof AnalysisRepNode || node instanceof ReportRepNode)) {
                    image = ImageLib.getImage(originalImageName);
                }
                // MOD klliu 2010-04-11 20468: Unfolder "exchange",get many NPE
                // exchange folder did not contain viewObject.
                if (viewObject != null) {
                    // MOD yyi 2011-04-07 19696: "Lock element"
                    ERepositoryStatus status = ProxyRepositoryFactory.getInstance().getStatus(viewObject);

                    Context ctx = CoreRuntimePlugin.getInstance().getContext();
                    RepositoryContext rc = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);

                    // TUP-1918:for offline mode,its item is locked by default but need show the default image. This
                    // will be enhanced later by TDI-29265.
                    if (rc.isEditableAsReadOnly()) {
                        if (status == ERepositoryStatus.LOCK_BY_USER) {
                            status = ERepositoryStatus.DEFAULT;
                        }
                    }

                    if (ERepositoryStatus.DEFAULT != status && originalImageName != null) {
                        if (ERepositoryStatus.LOCK_BY_USER == status) {
                            image = ImageLib.createLockedByOwnIcon(originalImageName).createImage();
                        } else if (ERepositoryStatus.LOCK_BY_OTHER == status) {
                            image = ImageLib.createLockedByOtherIcon(originalImageName).createImage();
                        }
                    }
                }
            } else if (type.equals(ENodeType.TDQ_REPOSITORY_ELEMENT)) {
                if (node instanceof DBCatalogRepNode) {
                    image = ImageLib.getImage(ImageLib.CATALOG);
                } else if (node instanceof DBSchemaRepNode) {
                    image = ImageLib.getImage(ImageLib.SCHEMA);
                } else if (node instanceof DBTableFolderRepNode) {
                    image = ImageLib.getImage(ImageLib.FOLDERNODE_IMAGE);
                } else if (node instanceof DBViewFolderRepNode) {
                    image = ImageLib.getImage(ImageLib.FOLDERNODE_IMAGE);
                } else if (node instanceof DBTableRepNode || node instanceof DFTableRepNode) {
                    image = ImageLib.getImage(ImageLib.TABLE);
                } else if (node instanceof DBViewRepNode) {
                    image = ImageLib.getImage(ImageLib.VIEW);
                } else if (node instanceof DBColumnRepNode) {
                    if (((DBColumnRepNode) node).isKey()) {
                        image = ImageLib.getImage(ImageLib.PK_COLUMN);
                    } else {
                        image = ImageLib.getImage(ImageLib.TD_COLUMN);
                    }
                } else if (node instanceof DFColumnRepNode) {
                    image = ImageLib.getImage(ImageLib.TD_COLUMN);
                } else if (node instanceof DBColumnFolderRepNode || node instanceof DFColumnFolderRepNode) {
                    image = ImageLib.getImage(ImageLib.FOLDERNODE_IMAGE);
                } else if (node instanceof JrxmlTempleteRepNode) {
                    image = ImageLib.getImage(ImageLib.XML_DOC);
                }
            }
        }
        // ~

        return image;
    }

    /**
     * if it is needed,add a over warning icon..eg., it is empty analysis or report; imported a MDM analysis or report.
     * 
     * @param image
     * @param node
     * @param originalImageName
     * @return
     */
    private Image addWarnIconIfNeeded(IRepositoryNode node, String originalImageName) {
        ModelElement modEle = RepositoryNodeHelper.getResourceModelElement(node);
        ERepositoryObjectType objectType = node.getObjectType();

        if (modEle != null) {
            if (ERepositoryObjectType.TDQ_ANALYSIS_ELEMENT == objectType) {
                Analysis analysis = (Analysis) modEle;
                EList<ModelElement> analysedElements = analysis.getContext().getAnalysedElements();
                DataManager connection = analysis.getContext().getConnection();
                if (analysedElements.isEmpty() || connection instanceof MDMConnection) {
                    return ImageLib.createInvalidIcon(originalImageName).createImage();
                }
            } else if (ERepositoryObjectType.TDQ_REPORT_ELEMENT == objectType) {
                TdReport report = (TdReport) modEle;
                EList<AnalysisMap> analysisMap = report.getAnalysisMap();
                if (analysisMap.isEmpty()) {
                    return ImageLib.createInvalidIcon(originalImageName).createImage();
                }
                for (AnalysisMap anaMap : report.getAnalysisMap()) {
                    Analysis analysis = anaMap.getAnalysis();
                    if (analysis != null) {
                        DataManager connection = analysis.getContext().getConnection();
                        if (connection instanceof MDMConnection) {
                            return ImageLib.createInvalidIcon(originalImageName).createImage();
                        }
                    }
                }
            }
        }
        return ImageLib.getImage(originalImageName);
    }

    @Override
    public String getText(Object element) {
        try {
            if (element != null && element instanceof IRepositoryNode) {
                IRepositoryNode node = (IRepositoryNode) element;

                if (node instanceof DBConnectionRepNode) {
                    if (!isSupportedConnection(node)) {
                        return node.getObject().getLabel() + PluginConstant.UNSUPPORTED;
                    }
                }
                if (node instanceof DQRepositoryNode) {
                    return node.getDisplayText();
                }

                String label = node.getObject().getLabel();
                if (label.startsWith(DQStructureManager.PREFIX_TDQ)) {
                    return label.substring(4, label.length());
                } else if (label.equals(EResourceConstant.METADATA.getName())) {
                    return label.substring(0, 1).toUpperCase() + label.substring(1);
                }
                return node.getObject() == null ? PluginConstant.EMPTY_STRING : label;
            }
        } catch (MissingDriverException e) {
            if (PluginChecker.isOnlyTopLoaded()) {
                MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        DefaultMessagesImpl.getString("ResourceViewContentProvider.warining"), //$NON-NLS-1$
                        e.getErrorMessage());
            } else {
                log.error(e, e);
            }
        }
        String text = super.getText(element);
        return PluginConstant.EMPTY_STRING.equals(text) ? DefaultMessagesImpl.getString("DQRepositoryViewLabelProvider.noName") : text; //$NON-NLS-1$
    }

    /**
     * 
     * DOC qiongli Comment method "getImageByContentType".
     * 
     * @param repositoryNode
     * @return
     */
    private Image getImageByContentType(RepositoryNode repositoryNode) {
        ERepositoryObjectType type = (ERepositoryObjectType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);
        if (type == null) {
            return null;
        }
        if (type == ERepositoryObjectType.TDQ_ANALYSIS_ELEMENT) {
            return ImageLib.getImage(ImageLib.ANALYSIS_OBJECT);
        } else if (type == ERepositoryObjectType.METADATA_CONNECTIONS) {
            return ImageLib.getImage(ImageLib.TD_DATAPROVIDER);
        } else if (type == ERepositoryObjectType.METADATA_FILE_DELIMITED) {
            return ImageLib.getImage(ImageLib.FILE_DELIMITED);
        } else if (type == ERepositoryObjectType.TDQ_REPORT_ELEMENT) {
            return ImageLib.getImage(ImageLib.REPORT_OBJECT);
        } else if (type == ERepositoryObjectType.TDQ_INDICATOR_ELEMENT) {
            return ImageLib.getImage(ImageLib.IND_DEFINITION);
        } else if (type == ERepositoryObjectType.TDQ_PATTERN_ELEMENT) {
            return ImageLib.getImage(ImageLib.PATTERN_REG);
        } else if (type == ERepositoryObjectType.TDQ_RULES) {
            return ImageLib.getImage(ImageLib.DQ_RULE);
        } else if (type == ERepositoryObjectType.TDQ_RULES_PARSER) {
            return ImageLib.getImage(ImageLib.DQ_RULE);
        } else if (type == ERepositoryObjectType.TDQ_RULES_SQL) {
            return ImageLib.getImage(ImageLib.DQ_RULE);
        } else if (type == ERepositoryObjectType.TDQ_RULES_MATCHER) {
            return ImageLib.getImage(ImageLib.MATCH_RULE_ICON);
        } else if (type == ERepositoryObjectType.TDQ_SOURCE_FILE_ELEMENT) {
            return ImageLib.getImage(ImageLib.SOURCE_FILE);
        } else if (type == ERepositoryObjectType.TDQ_EXCHANGE) {
            return ImageLib.getImage(ImageLib.EXCHANGE);
        } else if (type == ERepositoryObjectType.FOLDER) {
            return ImageLib.getImage(ImageLib.FOLDERNODE_IMAGE);
        } else if (type == ERepositoryObjectType.TDQ_JRAXML_ELEMENT) {
            return ImageLib.getImage(ImageLib.XML_DOC);
        }
        return null;

    }

    private boolean isSupportedConnection(IRepositoryNode repNode) {
        ERepositoryObjectType objectType = repNode.getObjectType();

        if (objectType == ERepositoryObjectType.METADATA_CONNECTIONS) {
            ConnectionItem connectionItem = (ConnectionItem) repNode.getObject().getProperty().getItem();
            if (connectionItem.getConnection() instanceof DatabaseConnection) {
                String databaseType = ((DatabaseConnection) connectionItem.getConnection()).getDatabaseType();
                List<String> tdqSupportDBType = MetadataConnectionUtils.getTDQSupportDBTemplate();
                return tdqSupportDBType.contains(databaseType);
            }
        }

        return false;
    }

    private boolean isNeedAddDriverConnection(IRepositoryNode repNode) {
        ERepositoryObjectType objectType = repNode.getObjectType();
        boolean isNeed = false;
        if (objectType == ERepositoryObjectType.METADATA_CONNECTIONS) {
            ConnectionItem connectionItem = (ConnectionItem) repNode.getObject().getProperty().getItem();
            if (connectionItem.getConnection() instanceof DatabaseConnection) {
                DatabaseConnection dbConn = (DatabaseConnection) (connectionItem.getConnection());
                String dbType = dbConn.getDatabaseType();
                String driverClassName = JavaSqlFactory.getDriverClass(dbConn);
                if (dbType == null || driverClassName == null || PluginConstant.EMPTY_STRING.equals(driverClassName)) {
                    return true;
                }

                // firstly,find driver from cache.if not find,load jar by lib folder then find driver from
                // SQLExplorer driver.
                if (!EDatabaseTypeName.HIVE.getXmlName().equalsIgnoreCase(dbType)) {
                    Driver driver = MetadataConnectionUtils.getDriverCache().get(driverClassName);
                    if (driver != null) {
                        return false;
                    }
                    isNeed = SqlExplorerUtils.getDefault().needAddDriverConnection(dbConn);
                }
            }

        }
        return isNeed;
    }

    /**
     * ADD qiongli TDQ-5801 if it is a invalid jdbc connection.
     * 
     * @param repNode
     * @return
     */
    private boolean isInvalidJDBCConnection(IRepositoryNode repNode) {
        Property property = repNode.getObject().getProperty();
        if (property != null && property.getItem() != null) {
            DatabaseConnectionItem connectionItem = (DatabaseConnectionItem) property.getItem();
            if (connectionItem != null) {
                DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
                String databaseType = connection.getDatabaseType();
                if (databaseType.equalsIgnoreCase(SupportDBUrlType.GENERICJDBCDEFAULTURL.getDBKey())
                        && ((connection.getDriverJarPath() == null) || (connection.getDriverJarPath()).trim().equals(
                                PluginConstant.EMPTY_STRING))) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * yyi 2011-04-14 20362:connection modified
     * 
     * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider#getFont(java.lang.Object)
     */
    @Override
    public Font getFont(Object element) {
        boolean changeURL = false;
        if (element instanceof IRepositoryNode) {
            IRepositoryNode node = (IRepositoryNode) element;
            ENodeType type = node.getType();

            if (type.equals(ENodeType.REPOSITORY_ELEMENT)) {
                if (node instanceof DBConnectionRepNode) {
                    ConnectionItem connectionItem = (ConnectionItem) node.getObject().getProperty().getItem();
                    if (connectionItem.getConnection() instanceof DatabaseConnection) {
                        changeURL = ConnectionHelper.isUrlChanged(connectionItem.getConnection());
                    }
                }
            }
        }

        return changeURL ? italicFont : super.getFont(element);
    }

}
