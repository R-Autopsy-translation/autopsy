/*
 * Autopsy Forensic Browser
 *
 * Copyright 2019 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
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
package org.sleuthkit.autopsy.communications.relationships;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.datamodel.NodeProperty;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.CommunicationsManager;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * ChildFactory that creates createKeys and nodes from a given selectionInfo for
 * only emails, call logs and messages.
 *
 */
final class ThreadChildNodeFactory extends ChildFactory<BlackboardArtifact> {

    private static final Logger logger = Logger.getLogger(ThreadChildNodeFactory.class.getName());

    private SelectionInfo selectionInfo;
    
    private final Action preferredAction;
    
    /**
     * Construct a new ThreadChildNodeFactory from the currently selectionInfo
     *
     * @param preferredAction SelectionInfo object for the currently selected
     *                      accounts
     */
    
    ThreadChildNodeFactory(Action preferredAction) {
        this.preferredAction = preferredAction;
    }
    
    /**
     * Updates the current instance of selectionInfo and calls the refresh method.
     * 
     * @param selectionInfo New instance of the currently selected accounts
     */
    public void refresh(SelectionInfo selectionInfo) {
        this.selectionInfo = selectionInfo;
        refresh(true);
    }
    
    /**
     * Creates a list of Keys (BlackboardArtifact) for only messages for the
     * currently selected accounts.  
     *
     * @param list List of BlackboardArtifact to populate
     *
     * @return True on success
     */
    @Override
    protected boolean createKeys(List<BlackboardArtifact> list) {
        if(selectionInfo == null) {
            return true;
        }
        
        try {
            final Set<Content> relationshipSources = selectionInfo.getRelationshipSources();
            createRootMessageKeys(list, relationshipSources) ;
        } catch (TskCoreException ex) {
            logger.log(Level.SEVERE, "Failed to load relationship sources.", ex); //NON-NLS
            return false;
        }

        return true;
    }
    
    /**
     * Adds only BlackboardArtifact objects to the list where are the earliest
     * message in a message thread (based on threadID).  If there are "unthreaded"
     * messages (messages that do not have a threadID) one representitive artifact
     * will be added to the list and dealt with a node creation time.
     * 
     * @param list List to populate with BlackboardArtifact keys
     * @param relationshipSources Set of Content objects
     * @return True on success
     * @throws TskCoreException 
     */
    private boolean createRootMessageKeys(List<BlackboardArtifact> list, Set<Content> relationshipSources) throws TskCoreException{
        Map<String, BlackboardArtifact> rootMessageMap = new HashMap<>();
        for(Content content: relationshipSources) {
            if(!(content instanceof BlackboardArtifact)) {
                continue;
            }
            
            BlackboardArtifact bba = (BlackboardArtifact) content;
            BlackboardArtifact.ARTIFACT_TYPE fromID = BlackboardArtifact.ARTIFACT_TYPE.fromID(bba.getArtifactTypeID());

            if (fromID == BlackboardArtifact.ARTIFACT_TYPE.TSK_EMAIL_MSG
                    || fromID == BlackboardArtifact.ARTIFACT_TYPE.TSK_CALLLOG
                    || fromID == BlackboardArtifact.ARTIFACT_TYPE.TSK_MESSAGE) {

                // We want email and message artifacts that do not have "threadIDs" to appear as one thread in the UI
                // To achive this assign any artifact that does not have a threadID
                // the "UNTHREADED_ID"
                // All call logs will default to a single call logs thread
                String threadID;
                if (fromID == BlackboardArtifact.ARTIFACT_TYPE.TSK_CALLLOG) {
                    threadID = MessageNode.CALL_LOG_ID;
                } else {
                    threadID = MessageNode.UNTHREADED_ID;
                }
                BlackboardAttribute attribute = bba.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_THREAD_ID));

                if(attribute != null) {
                    threadID = attribute.getValueString();
                } 

                BlackboardArtifact tableArtifact = rootMessageMap.get(threadID);
                if(tableArtifact == null) {
                    rootMessageMap.put(threadID, bba);          
                } else {
                    // Get the date of the message
                    BlackboardAttribute tableAttribute = tableArtifact.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_SENT));
                    attribute = bba.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_SENT));

                    // put the earliest message into the table
                    if(tableAttribute != null 
                            && attribute != null 
                            && tableAttribute.getValueLong() > attribute.getValueLong()) {
                        rootMessageMap.put(threadID, bba);
                    }
                }
            }
        }

        for(BlackboardArtifact bba: rootMessageMap.values()) {
             list.add(bba);
        }
            
        return true;
    }

    @Override
    protected Node createNodeForKey(BlackboardArtifact bba) {
        BlackboardAttribute attribute = null;
        try {
            attribute = bba.getAttribute(new BlackboardAttribute.Type(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_THREAD_ID)); 
        } catch (TskCoreException ex) {
            logger.log(Level.WARNING, String.format("Unable to get threadID for artifact: %s", bba.getName()), ex);
        } 
        
        if (attribute != null) {
            return new ThreadNode(bba, attribute.getValueString(), preferredAction);
        } else {
            if (bba.getArtifactTypeID() == BlackboardArtifact.ARTIFACT_TYPE.TSK_CALLLOG.getTypeID()) {
                return new CallLogNode();
            } else {
                // Only one of these should occur.
                return new UnthreadedNode();
            }
        }         
    }
    
    /**
     * This node represents the "call log" thread.
     */
    final class CallLogNode extends AbstractNode {
        /**
         * Construct an instance of a CallLogNode.
         */
        CallLogNode() {
            super(Children.LEAF);
            setDisplayName("Call Logs");
            this.setIconBaseWithExtension("org/sleuthkit/autopsy/communications/images/unthreaded.png" );
        }
        
         @Override
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Sheet.Set sheetSet = sheet.get(Sheet.PROPERTIES);
            if (sheetSet == null) {
                sheetSet = Sheet.createPropertiesSet();
                sheet.put(sheetSet);
            }
            
            // Give this node a threadID of "CALL_LOG_ID"
            sheetSet.put(new NodeProperty<>("ThreadID", "ThreadID","",MessageNode.CALL_LOG_ID));
            
            return sheet;
        }
    }
    
    /**
     * This node represents the "unthreaded" thread.
     */
    final class UnthreadedNode extends AbstractNode {
        /**
         * Construct an instance of UnthreadNode.
         */
        UnthreadedNode() {
            super(Children.LEAF);
            setDisplayName("Unthreaded");
            this.setIconBaseWithExtension("org/sleuthkit/autopsy/communications/images/unthreaded.png" );
        }
        
         @Override
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Sheet.Set sheetSet = sheet.get(Sheet.PROPERTIES);
            if (sheetSet == null) {
                sheetSet = Sheet.createPropertiesSet();
                sheet.put(sheetSet);
            }
            
            // Give this node a threadID of "UNTHEADED_ID"
            sheetSet.put(new NodeProperty<>("ThreadID", "ThreadID","",MessageNode.UNTHREADED_ID));
            
            return sheet;
        }
    }
}
