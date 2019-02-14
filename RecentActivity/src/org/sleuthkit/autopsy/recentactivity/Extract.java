/*
 *
 * Autopsy Forensic Browser
 *
 * Copyright 2012-2019 Basis Technology Corp.
 *
 * Copyright 2012 42six Solutions.
 * Contact: aebadirad <at> 42six <dot> com
 * Project Contact/Architect: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.recentactivity;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.MessageNotifyUtil;
import org.sleuthkit.autopsy.coreutils.SQLiteDBConnect;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.autopsy.ingest.IngestModule.IngestModuleException;
import org.sleuthkit.datamodel.Blackboard;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

@Messages({"Extract.indexError.message=Failed to index artifact for keyword search.",
    "Extract.noOpenCase.errMsg=No open case available.",
    "# {0} - the module name",
    "Extractor.errPostingArtifacts=Error posting {0} artifacts to the blackboard."})
abstract class Extract {

    protected static final Logger logger = Logger.getLogger(Extract.class.getName());

    protected Case currentCase;
    protected SleuthkitCase tskCase;
    protected Blackboard blackboard;
    protected FileManager fileManager;

    private final ArrayList<String> errorMessages = new ArrayList<>();
    boolean dataFound = false;
    protected String moduleName;

    /**
     * Returns the name of the inheriting class
     *
     * @return Gets the moduleName
     */
    protected String getName() {
        return moduleName;
    }

    final void init() throws IngestModuleException {
        try {
            currentCase = Case.getCurrentCaseThrows();
            tskCase = currentCase.getSleuthkitCase();
            blackboard = tskCase.getBlackboard();
            fileManager = currentCase.getServices().getFileManager();
        } catch (NoCurrentCaseException ex) {
            throw new IngestModuleException(Bundle.Extract_noOpenCase_errMsg(), ex);
        }
        configExtractor();
    }

    /**
     * Override to add any module-specific configuration
     *
     * @throws IngestModuleException
     */
    void configExtractor() throws IngestModuleException {
    }

    abstract void process(Content dataSource, IngestJobContext context);

    void complete() {
    }

    /**
     * Returns a List of string error messages from the inheriting class
     *
     * @return errorMessages returns all error messages logged
     */
    List<String> getErrorMessages() {
        return errorMessages;
    }

    /**
     * Adds a string to the error message list
     *
     * @param message is an error message represented as a string
     */
    protected void addErrorMessage(String message) {
        errorMessages.add(message);
    }

    /** Generic method for adding a blackboard artifact to the blackboard and
     * indexing it
     *
     * @param type         is a blackboard.artifact_type enum to determine which
     *                     type the artifact should be
     * @param content      is the Content object that needs to have the artifact
     *                     added for it
     * @param bbattributes is the collection of blackboard attributes that need
     *                     to be added to the artifact after the artifact has
     *                     been created
     *
     * @return The newly-created artifact, or null on error
     */
    protected BlackboardArtifact addArtifact(BlackboardArtifact.ARTIFACT_TYPE type, Content content, Collection<BlackboardAttribute> bbattributes) {
        try {
            BlackboardArtifact bbart = content.newArtifact(type);
            bbart.addAttributes(bbattributes);
            // index the artifact for keyword search
            this.indexArtifact(bbart);
            return bbart;
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Error while trying to add an artifact", ex); //NON-NLS
        }
        return null;
    }

    /**
     * Method to index a blackboard artifact for keyword search
     *
     * @param bbart Blackboard artifact to be indexed
     */
    void indexArtifact(BlackboardArtifact bbart) {
        try {
            Blackboard blackboard = Case.getCurrentCaseThrows().getSleuthkitCase().getBlackboard();
            // index the artifact for keyword search
            blackboard.postArtifact(bbart, getName());
        } catch (Blackboard.BlackboardException ex) {
            logger.log(Level.SEVERE, "Unable to index blackboard artifact " + bbart.getDisplayName(), ex); //NON-NLS
            MessageNotifyUtil.Notify.error(Bundle.Extract_indexError_message(), bbart.getDisplayName());
        } catch (NoCurrentCaseException ex) {
            logger.log(Level.SEVERE, "Exception while getting open case.", ex); //NON-NLS
            MessageNotifyUtil.Notify.error(Bundle.Extract_noOpenCase_errMsg(), bbart.getDisplayName());
        }
    }

    /**
     * Returns a List from a result set based on sql query. This is used to
     * query sqlite databases storing user recent activity data, such as in
     * firefox sqlite db
     *
     * @param path  is the string path to the sqlite db file
     * @param query is a sql string query that is to be run
     *
     * @return list is the ArrayList that contains the resultset information in
     *         it that the query obtained
     */
    protected List<HashMap<String, Object>> dbConnect(String path, String query) {

        String connectionString = "jdbc:sqlite:" + path; //NON-NLS
        try (SQLiteDBConnect tempdbconnect = new SQLiteDBConnect("org.sqlite.JDBC", connectionString); //NON-NLS
                ResultSet temprs = tempdbconnect.executeQry(query);) {
            return this.resultSetToArrayList(temprs);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error while trying to read into a sqlite db." + connectionString, ex); //NON-NLS
            errorMessages.add(NbBundle.getMessage(this.getClass(), "Extract.dbConn.errMsg.failedToQueryDb", getName()));
            return Collections.<HashMap<String, Object>>emptyList();
        }
    }

    /**
     * Returns a List of AbstractFile objects from TSK based on sql query.
     *
     * @param results is the resultset that needs to be converted to an
     *                arraylist
     *
     * @return list returns the arraylist built from the converted resultset
     */
    private List<HashMap<String, Object>> resultSetToArrayList(ResultSet results) throws SQLException {
        ResultSetMetaData metaData = results.getMetaData();
        int columns = metaData.getColumnCount();
        List<HashMap<String, Object>> list = new ArrayList<>(50);
        while (results.next()) {
            HashMap<String, Object> row = new HashMap<>(columns);
            for (int i = 1; i <= columns; ++i) {
                if (results.getObject(i) == null) {
                    row.put(metaData.getColumnName(i), "");
                } else {
                    row.put(metaData.getColumnName(i), results.getObject(i));
                }
            }
            list.add(row);
        }

        return list;
    }

    public boolean foundData() {
        return dataFound;
    }
}
