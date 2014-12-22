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
package org.talend.dataprofiler.core.ui.wizard.database;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.core.model.metadata.builder.database.dburl.SupportDBUrlStore;
import org.talend.core.model.metadata.builder.database.dburl.SupportDBUrlType;
import org.talend.core.model.metadata.builder.util.DatabaseConstant;
import org.talend.cwm.db.connection.ConnectionUtils;
import org.talend.cwm.db.connection.IXMLDBConnection;
import org.talend.cwm.db.connection.MdmWebserviceConnection;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.editor.composite.MultipleSelectionCombo;
import org.talend.dataprofiler.core.ui.utils.UIMessages;
import org.talend.dataprofiler.core.ui.wizard.AbstractWizardPage;
import org.talend.dataprofiler.core.ui.wizard.urlsetup.URLSetupControl;
import org.talend.dataprofiler.core.ui.wizard.urlsetup.URLSetupControlFactory;
import org.talend.dq.analysis.parameters.DBConnectionParameter;
import org.talend.utils.sugars.ReturnCode;

/**
 * This class reused the class in QuantumPlugin.
 * 
 */
public class DatabaseWizardPage extends AbstractWizardPage {

    protected static Logger log = Logger.getLogger(DatabaseWizardPage.class);

    /* use this to paint a more helpful UI for the JDBC URL */
    private SupportDBUrlType lastTimeDBType;

    private String userid;

    private String password;

    private String connectionURL = null;

    private Label jdbcLabel;

    private Text jdbcUrl;

    private URLSetupControl urlSetupControl;

    private Composite container;

    private Button checkButton;

    // ADD xqliu 2010-03-03 feature 11412
    private Button retrieveAllButton;

    private DBConnectionParameter connectionParam;

    private boolean dbTypeSwitchFlag = false;

    private Text username;

    private Text passwordText;

    private boolean mdmFlag = false;

    // MOD mzhao bug 12379, 2010-04-05, avoid replicated codes for the same functionality.
    private DatabaseConnectionWizard databaseConnectionWizard = null;

    public DatabaseWizardPage(DatabaseConnectionWizard databaseConnectionWizard) {
        this.databaseConnectionWizard = databaseConnectionWizard;
    }

    // ~bug 12379
    public void setMdmFlag(boolean mdm) {
        this.mdmFlag = mdm;
    }

    private PropertyChangeListener listener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
            if (PluginConstant.CONNECTION_URL_PROPERTY.equals(event.getPropertyName())) {
                DatabaseWizardPage.this.setConnectionURL((String) event.getNewValue());
                DatabaseWizardPage.this.updateButtonState();
            }
        }
    };

    public void createControl(Composite parent) {
        connectionParam = (DBConnectionParameter) getParameter();
        setPageComplete(false);

        Composite comp = new Composite(parent, SWT.NULL);

        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        comp.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 0;

        GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        data.horizontalSpan = 1;
        Composite tempComp = new Composite(comp, SWT.NULL);
        tempComp.setLayoutData(data);
        GridLayout tempLayout = new GridLayout();
        tempComp.setLayout(tempLayout);
        tempLayout.numColumns = 2;

        Label label = new Label(tempComp, SWT.NULL);
        label.setText(DefaultMessagesImpl.getString("DatabaseWizardPage.login")); //$NON-NLS-1$
        username = new Text(tempComp, SWT.BORDER | SWT.SINGLE);

        GridData fullHorizontal = new GridData(GridData.FILL_HORIZONTAL);
        username.setLayoutData(fullHorizontal);
        username.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent event) {
                String userId = ((Text) event.getSource()).getText();
                setUserid(userId);
                updateButtonState();
            }
        });

        label = new Label(tempComp, SWT.NULL);
        label.setText(DefaultMessagesImpl.getString("DatabaseWizardPage.password")); //$NON-NLS-1$
        // MOD zshen for bug 11931
        passwordText = new Text(tempComp, SWT.BORDER | SWT.SINGLE | SWT.PASSWORD);
        fullHorizontal = new GridData(GridData.FILL_HORIZONTAL);
        passwordText.setLayoutData(fullHorizontal);
        passwordText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent event) {
                String passwordStr = ((Text) event.getSource()).getText();
                setPassword(passwordStr);
                updateButtonState();
            }
        });

        label = new Label(tempComp, SWT.NULL);
        label.setText(DefaultMessagesImpl.getString("DatabaseWizardPage.DBType")); //$NON-NLS-1$
        final Combo dbTypeCombo = new Combo(tempComp, SWT.READ_ONLY);
        dbTypeCombo.setItems(SupportDBUrlStore.getInstance().getDBTypes());
        dbTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dbTypeCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // MOD yyi 2010-05-04 10494
                String selectedDBKey = dbTypeCombo.getText().trim();

                SupportDBUrlType dbType = SupportDBUrlType.getDBTypeByKey(selectedDBKey);
                if (dbType == null) {
                    MessageDialog.openWarning(
                            null,
                            DefaultMessagesImpl.getString("DatabaseWizardPage.UnsupportedTitle"), DefaultMessagesImpl.getString("DatabaseWizardPage.UnsupportedDriver")); //$NON-NLS-1$ //$NON-NLS-2$
                } else {

                    switch (dbType) {
                    case ODBCDEFAULTURL:
                        updateButtonState();
                        break;
                    case GENERICJDBCDEFAULTURL:
                        updateStatus(IStatus.WARNING, UIMessages.MSG_SELECT_GENERIC_JDBC);
                        break;
                    case SQLITE3DEFAULTURL:
                        username.setEnabled(false);
                        passwordText.setEnabled(false);
                        break;
                    case NETEZZADEFAULTURL:
                        MessageDialog.openWarning(null, DefaultMessagesImpl.getString("DatabaseWizardPage.UnsupportedTitle"), //$NON-NLS-1$
                                DefaultMessagesImpl.getString("DatabaseWizardPage.UnsupportedNetezzaDriver")); //$NON-NLS-1$
                        dbTypeCombo.setText(SupportDBUrlType.GENERICJDBCDEFAULTURL.getDBKey());
                        break;

                    default:
                        username.setEnabled(true);
                        passwordText.setEnabled(true);
                        break;
                    }

                    String selectedItem = ((Combo) e.getSource()).getText();
                    setDBType(selectedItem);
                    dbTypeSwitchFlag = true;

                    rebuildJDBCControls(SupportDBUrlStore.getInstance().getDBUrlType(selectedItem));
                }

            }

        });

        // ADD xqliu 2010-03-01 feature 11412
        label = new Label(tempComp, SWT.NULL);
        label.setText(""); //$NON-NLS-1$
        retrieveAllButton = new Button(tempComp, SWT.CHECK);
        retrieveAllButton.setLayoutData(new GridData(GridData.FILL_BOTH));
        retrieveAllButton.setText(DefaultMessagesImpl.getString("DatabaseWizardPage.retrieveAllMetadata")); //$NON-NLS-1$
        retrieveAllButton.setToolTipText(DefaultMessagesImpl.getString("DatabaseWizardPage.retrieveAllMetadata")); //$NON-NLS-1$
        retrieveAllButton.setSelection(connectionParam.isRetrieveAllMetadata());
        retrieveAllButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                connectionParam.setRetrieveAllMetadata(retrieveAllButton.getSelection());
            }

        });
        // ~11412

        String defalutItem = SupportDBUrlType.MYSQLDEFAULTURL.getDBKey();
        if (mdmFlag) {
            defalutItem = SupportDBUrlType.MDM.getDBKey();
            dbTypeCombo.setEnabled(false);
        } else {
            dbTypeCombo.remove(SupportDBUrlType.MDM.getDBKey());
        }
        dbTypeCombo.setText(defalutItem);
        setDBType(defalutItem);
        lastTimeDBType = SupportDBUrlStore.getInstance().getDBUrlType(dbTypeCombo.getText());

        checkButton = new Button(comp, SWT.NULL);
        GridData buttonData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        buttonData.heightHint = 25;
        buttonData.widthHint = 100;
        checkButton.setLayoutData(buttonData);
        checkButton.setText(DefaultMessagesImpl.getString("DatabaseWizardPage.check")); //$NON-NLS-1$
        checkButton.setToolTipText(DefaultMessagesImpl.getString("DatabaseWizardPage.checkConnection")); //$NON-NLS-1$
        checkButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ReturnCode code = checkDBConnection();
                if (code.isOk()) {
                    MessageDialog.openInformation(
                            getShell(),
                            DefaultMessagesImpl.getString("DatabaseWizardPage.checkConnections"), DefaultMessagesImpl.getString("DatabaseWizardPage.checkSuccessful")); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    MessageDialog.openInformation(
                            getShell(),
                            DefaultMessagesImpl.getString("DatabaseWizardPage.checkConnectionss"), DefaultMessagesImpl.getString("DatabaseWizardPage.checkFailure") //$NON-NLS-1$ //$NON-NLS-2$
                                    + code.getMessage());
                }
            }

        });

        this.container = comp;
        setControl(comp);

        // MOD xqliu 2010-03-04 feature 11412
        SupportDBUrlType defaultSupportDBUrlType = this.mdmFlag ? SupportDBUrlType.MDM : SupportDBUrlType.MYSQLDEFAULTURL;
        rebuildJDBCControls(defaultSupportDBUrlType);
        this.connectionParam.setDbName(defaultSupportDBUrlType.getDBName());
        // ~11412

        String tempUserid = this.connectionParam.getParameters().getProperty(TaggedValueHelper.USER);
        if (tempUserid != null) {
            this.userid = tempUserid;
            this.username.setText(this.userid);
        }
        String tempPassword = this.connectionParam.getParameters().getProperty(TaggedValueHelper.PASSWORD);
        if (tempPassword != null) {
            this.password = tempPassword;
            this.passwordText.setText(this.password);
        }
        String tempURL = this.connectionParam.getJdbcUrl();
        if (tempURL != null) {
            this.connectionURL = tempURL;
            // passwordText.setText(password);
        } else {
            // System.out.println("NULL URL");
        }
    }

    /**
     * @param container
     */
    private void createStandardJDBCWidgets(Composite container) {
        setConnectionURL(""); //$NON-NLS-1$

        this.jdbcLabel = new Label(container, SWT.NULL);
        this.jdbcLabel.setText("url"); //$NON-NLS-1$

        this.jdbcUrl = new Text(container, SWT.BORDER | SWT.SINGLE);
        this.jdbcUrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.jdbcUrl.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent event) {
                setConnectionURL(((Text) event.getSource()).getText());
                updateButtonState();
            }
        });

        updateButtonState();
    }

    private ReturnCode checkDBConnection() {
        connectionParam.getParameters().setProperty(TaggedValueHelper.DATA_FILTER, ""); //$NON-NLS-1$
        // ADD xqliu 2010-04-01 bug 12379. MOD mzhao 2010-04-05, avoid replicated codes for same function.
        ReturnCode rt = databaseConnectionWizard.checkMetadata();
        if (!rt.isOk()) {
            return rt;
        }
        // ~12379
        // MOD xqliu 2009-12-17 check for a mdm database
        if (mdmFlag) {
            IXMLDBConnection mdmConnection = new MdmWebserviceConnection(connectionParam.getJdbcUrl(),
                    connectionParam.getParameters());
            ReturnCode retcode = mdmConnection.checkDatabaseConnection();
            this.urlSetupControl.getDataFilterCombo().setEnabled(fillComboContent(mdmConnection).isOk());
            return retcode;
        }

        if (this.connectionParam.getDriverPath() != null) {
            CorePlugin corePlugin = CorePlugin.getDefault();
            ReturnCode rc = new ReturnCode();
            // String[] driverpaths =
            // this.connectionParam.getDriverPath().split(";");
            Driver externalDriver = null;
            String errorMsg = ""; //$NON-NLS-1$
            try {
                externalDriver = createGenericJDBC(this.connectionParam.getDriverPath(),
                        this.connectionParam.getDriverClassName());
            } catch (Exception e1) {
                errorMsg = e1.getLocalizedMessage();
            }
            if (externalDriver != null) {
                Connection connection = null;
                try {
                    DriverManager.registerDriver(externalDriver);
                    // MOD xqliu 2009-02-03 bug 5261
                    connection = ConnectionUtils.createConnectionWithTimeout(externalDriver, this.connectionParam.getJdbcUrl(),
                            this.connectionParam.getParameters());
                    if (connection == null) {
                        rc.setOk(false);
                        rc.setMessage(DefaultMessagesImpl.getString("DatabaseWizardPage.connIsNULL")); //$NON-NLS-1$
                    }
                } catch (Exception e) {
                    rc.setOk(false);
                    rc.setMessage(e.getLocalizedMessage());
                    log.error(e, e);
                } finally {
                    ConnectionUtils.closeConnection(connection);
                }
            } else {
                rc.setOk(false);
                rc.setMessage(errorMsg);
            }
            return rc;
        } else {
            ReturnCode returnCode = ConnectionUtils.checkConnection(this.connectionParam.getJdbcUrl(),
                    this.connectionParam.getDriverClassName(), this.connectionParam.getParameters());
            return returnCode;
        }
    }

    private Driver createGenericJDBC(String driverJars, String driverName) throws Exception {
        Driver driver = null;
        String[] driverJarPath = driverJars.split(";"); //$NON-NLS-1$
        try {
            int driverCount = 0;
            URL[] driverUrl = new URL[driverJarPath.length];
            for (String dirverpath : driverJarPath) {
                driverUrl[driverCount++] = new File(dirverpath).toURL();
            }
            URLClassLoader cl = URLClassLoader.newInstance(driverUrl, Thread.currentThread().getContextClassLoader());
            Class c = cl.loadClass(driverName);
            driver = (Driver) c.newInstance();
        } catch (Exception ex) {
            log.error(ex, ex);
            throw ex;
        }
        return driver;
    }

    /**
     * 
     */
    private void rebuildJDBCControls(SupportDBUrlType dbType) {
        if (SupportDBUrlType.MDM.equals(dbType)) {
            dbType.setDBName(DatabaseConstant.MDM_DBNAME);
        }
        Point windowSize = getShell().getSize();
        Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        if (URLSetupControlFactory.hasControl(dbType)) {

            disposeOfCurrentJDBCControls();

            this.urlSetupControl = URLSetupControlFactory.create(dbType, this.container, connectionParam, this);
            GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
            data.horizontalSpan = 2;
            this.urlSetupControl.setLayoutData(data);

            if (connectionURL != null && !dbTypeSwitchFlag) {
                urlSetupControl.setConnectionURL(connectionURL);
            } else {
                setConnectionURL(this.urlSetupControl.getConnectionURL());
            }

            this.urlSetupControl.addPropertyChangeListener(this.listener);

            resizeWindow(windowSize, oldSize);

        } else if (this.jdbcLabel == null || this.jdbcUrl == null) {

            disposeOfCurrentJDBCControls();
            createStandardJDBCWidgets(this.container);

            resizeWindow(windowSize, oldSize);

        }

        this.container.layout();
        this.container.setVisible(true);
        this.container.redraw();
        if (dbType.getLanguage().trim().equals("Generic JDBC")) { //$NON-NLS-1$
            // updateEditableState();
        } else {
            updateButtonState();
        }
    }

    /**
     * @param windowSize
     * @param oldSize
     */
    private void resizeWindow(Point windowSize, Point oldSize) {
        Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (newSize.y > windowSize.y) {
            getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
        }
    }

    private void disposeOfCurrentJDBCControls() {
        if (this.jdbcUrl != null) {
            this.jdbcUrl.dispose();
            this.jdbcUrl = null;
        }
        if (this.jdbcLabel != null) {
            this.jdbcLabel.dispose();
            this.jdbcLabel = null;
        }
        if (this.urlSetupControl != null) {
            this.urlSetupControl.removePropertyChangeListener(this.listener);
            this.urlSetupControl.dispose();
            this.urlSetupControl = null;
        }
        // if (this.checkButton != null) {
        // this.checkButton.dispose();
        // this.checkButton = null;
        // }
    }

    /**
     * 
     * DOC zhaoxinyi Comment method "updateEditableState".
     */

    private void updateEditableState() {
        String user = connectionParam.getParameters().getProperty(TaggedValueHelper.USER);
        String password = connectionParam.getParameters().getProperty(TaggedValueHelper.PASSWORD);
        boolean isPasswordBlank = password != null && !password.trim().equals(""); //$NON-NLS-1$
        boolean isUserBlank = user != null && !user.trim().equals(""); //$NON-NLS-1$
        boolean isUrlBlank = connectionParam.getJdbcUrl() != null && !connectionParam.getJdbcUrl().trim().equals(""); //$NON-NLS-1$
        boolean isDriverNameBlank = connectionParam.getDriverClassName() != null
                && !connectionParam.getDriverClassName().trim().equals(""); //$NON-NLS-1$
        boolean isDriverFileBlank = connectionParam.getDriverPath() != null && !connectionParam.getDriverPath().equals(""); //$NON-NLS-1$
        boolean isComplete = isPasswordBlank && isUserBlank && isDriverNameBlank && isUrlBlank && isDriverFileBlank;
        if (isComplete) {
            checkButton.setEnabled(!isComplete);
            setPageComplete(!isComplete);
        }
        setPageComplete(isComplete);
    }

    public void updateButtonState() {
        boolean complete = true;
        String dbTypeName = this.connectionParam.getSqlTypeName();
        if (!SupportDBUrlType.MSSQLDEFAULTURL.getDBKey().equals(dbTypeName)) {
            if (SupportDBUrlType.GENERICJDBCDEFAULTURL.getDBKey().equals(dbTypeName)) {
                // deal with generic jdbc;
                String driverName = this.connectionParam.getDriverClassName() == null ? "" : this.connectionParam //$NON-NLS-1$
                        .getDriverClassName();
                String connURL = this.connectionParam.getJdbcUrl() == null ? "" : this.connectionParam.getJdbcUrl(); //$NON-NLS-1$
                // MOD xqliu 2009-12-03 bug 10247
                complete = !("".equals(driverName) || "".equals(connURL)); //$NON-NLS-1$ //$NON-NLS-2$
                // String userName = this.userid == null ? "" : this.userid; //$NON-NLS-1$
                // If ("".equals(driverName) || "".equals(connURL)) { //$NON-NLS-1$ //$NON-NLS-2$
                // complete = false;
                // } else {
                // complete = SupportDBUrlType.SQLITE3DEFAULTURL.getDbDriver().equals(driverName) ? true : !"".equals(userName); //$NON-NLS-1$
                // }
                // ~
            } else if (SupportDBUrlType.SQLITE3DEFAULTURL.getDBKey().equals(dbTypeName)) {
                // deal with sqlite;
                String filename = this.connectionParam.getFilePath();
                complete &= filename != null && !filename.trim().equals(""); //$NON-NLS-1$
            } else if (SupportDBUrlType.ODBCDEFAULTURL.getDBKey().equals(dbTypeName)) {
                // deal with Generic ODBC;
                complete &= true;
            } else {
                complete &= this.userid != null && !this.userid.trim().equals(""); //$NON-NLS-1$
            }
        }
        if (checkButton != null) {
            checkButton.setEnabled(complete);
        }
        setPageComplete(complete);
    }

    /**
     * @return Returns the userid.
     */
    public String getUserid() {
        return this.userid;
    }

    /**
     * @param userid The userid to set.
     */
    public void setUserid(String userid) {
        if (userid != null && !userid.equals(this.userid)) {
            this.userid = userid;
            this.connectionParam.getParameters().setProperty(TaggedValueHelper.USER, userid);
        }
    }

    public void setDBType(String dbType) {
        this.connectionParam.setSqlTypeName(dbType);
        this.connectionParam.setDriverClassName(SupportDBUrlStore.getInstance().getDBUrlType(dbType).getDbDriver());
    }

    /**
     * @param connectionURL The connectionURL to set.
     */
    private void setConnectionURL(String connectionURL) {
        if (connectionURL != null && !connectionURL.equals(this.connectionURL)) {
            this.connectionURL = connectionURL;
            this.connectionParam.setJdbcUrl(connectionURL);
        }
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        if (password != null && !password.equals(this.password)) {
            this.password = password;
            this.connectionParam.getParameters().setProperty(TaggedValueHelper.PASSWORD, password);
        }
    }

    @Override
    public void dispose() {
        if (this.checkButton != null) {
            this.checkButton.dispose();
            this.checkButton = null;
        }
    }

    public boolean updateLoginPassEnable(boolean enable) {
        username.setEnabled(enable);
        passwordText.setEnabled(enable);
        return enable;
    }

    private ReturnCode fillComboContent(IXMLDBConnection mdmConnection) {
        ReturnCode rc = new ReturnCode(false);

        MultipleSelectionCombo dataFilterCombo = this.urlSetupControl.getDataFilterCombo();
        dataFilterCombo.removeAll();
        try {
            Iterator<String> iter = mdmConnection.getConnectionContent().iterator();

            while (iter.hasNext()) {
                dataFilterCombo.add(iter.next());
            }
            if (dataFilterCombo.getItemCount() > 0) {
                rc.setOk(true);
            }
        } catch (Exception exception) {
            log.error(exception);
        }
        return rc;
    }
}
