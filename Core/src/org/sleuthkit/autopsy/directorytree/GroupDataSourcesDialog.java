/*
 * Autopsy Forensic Browser
 * 
 * Copyright 2011-2018 Basis Technology Corp.
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
package org.sleuthkit.autopsy.directorytree;

import javax.swing.JFrame;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 */
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
final class GroupDataSourcesDialog extends javax.swing.JDialog {

    boolean shouldGroupByDataSource = false;
    
    /**
     * Creates new form GroupDataSourcesDialog
     */
    @NbBundle.Messages({"# {0} - dataSourceCount",
                    "GroupDataSourcesDialog.groupDataSources.text=This case contains {0} data sources."})
    GroupDataSourcesDialog(int dataSourceCount) {
        super((JFrame) WindowManager.getDefault().getMainWindow());
        initComponents();
        dataSourceCountLabel.setText(Bundle.GroupDataSourcesDialog_groupDataSources_text(dataSourceCount));
    }
    
    /**
     * Display the dialog.
     */
    void display() {
        setModal(true);
        setSize(getPreferredSize());
        setLocationRelativeTo(this.getParent());
        setAlwaysOnTop(false);
        pack();
        setVisible(true);
    }
    
    boolean groupByDataSourceSelected() {
        return shouldGroupByDataSource;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dataSourceCountLabel = new javax.swing.JLabel();
        queryLabel = new javax.swing.JLabel();
        yesButton = new javax.swing.JButton();
        noButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(GroupDataSourcesDialog.class, "GroupDataSourcesDialog.title")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dataSourceCountLabel, org.openide.util.NbBundle.getMessage(GroupDataSourcesDialog.class, "GroupDataSourcesDialog.dataSourceCountLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(queryLabel, org.openide.util.NbBundle.getMessage(GroupDataSourcesDialog.class, "GroupDataSourcesDialog.queryLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(yesButton, org.openide.util.NbBundle.getMessage(GroupDataSourcesDialog.class, "GroupDataSourcesDialog.yesButton.text")); // NOI18N
        yesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yesButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(noButton, org.openide.util.NbBundle.getMessage(GroupDataSourcesDialog.class, "GroupDataSourcesDialog.noButton.text")); // NOI18N
        noButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(queryLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dataSourceCountLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(yesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(noButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataSourceCountLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(queryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(yesButton)
                    .addComponent(noButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void yesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yesButtonActionPerformed
        shouldGroupByDataSource = true;
        dispose();
    }//GEN-LAST:event_yesButtonActionPerformed

    private void noButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noButtonActionPerformed
        shouldGroupByDataSource = false;
        dispose();
    }//GEN-LAST:event_noButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel dataSourceCountLabel;
    private javax.swing.JButton noButton;
    private javax.swing.JLabel queryLabel;
    private javax.swing.JButton yesButton;
    // End of variables declaration//GEN-END:variables
}