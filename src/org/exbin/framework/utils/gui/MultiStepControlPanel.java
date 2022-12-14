/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.framework.utils.gui;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.framework.utils.LanguageUtils;
import org.exbin.framework.utils.OkCancelListener;
import org.exbin.framework.utils.WindowUtils;
import org.exbin.framework.utils.handler.MultiStepControlHandler;

/**
 * Multi-step control panel for options dialogs.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class MultiStepControlPanel extends javax.swing.JPanel implements MultiStepControlHandler.MultiStepControlService {

    private final java.util.ResourceBundle resourceBundle;
    private MultiStepControlHandler handler;
    private OkCancelListener okCancelListener;

    public MultiStepControlPanel() {
        this(LanguageUtils.getResourceBundleByClass(MultiStepControlPanel.class));
    }

    public MultiStepControlPanel(java.util.ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        initComponents();

        okCancelListener = new OkCancelListener() {
            @Override
            public void okEvent() {
                performClick(MultiStepControlHandler.ControlActionType.FINISH);
            }

            @Override
            public void cancelEvent() {
                performClick(MultiStepControlHandler.ControlActionType.CANCEL);
            }
        };
    }

    public void setHandler(MultiStepControlHandler handler) {
        this.handler = handler;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        finishButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        finishButton.setText(resourceBundle.getString("finishButton.text")); // NOI18N
        finishButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishButtonActionPerformed(evt);
            }
        });

        nextButton.setText(resourceBundle.getString("nextButton.text")); // NOI18N
        nextButton.setEnabled(false);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        previousButton.setText(resourceBundle.getString("previousButton.text")); // NOI18N
        previousButton.setEnabled(false);
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(resourceBundle.getString("cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(previousButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(finishButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(finishButton)
                    .addComponent(nextButton)
                    .addComponent(previousButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void finishButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishButtonActionPerformed
        if (handler != null) {
            handler.controlActionPerformed(MultiStepControlHandler.ControlActionType.FINISH);
        }
    }//GEN-LAST:event_finishButtonActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        if (handler != null) {
            handler.controlActionPerformed(MultiStepControlHandler.ControlActionType.NEXT);
        }
    }//GEN-LAST:event_nextButtonActionPerformed

    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        if (handler != null) {
            handler.controlActionPerformed(MultiStepControlHandler.ControlActionType.PREVIOUS);
        }
    }//GEN-LAST:event_previousButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (handler != null) {
            handler.controlActionPerformed(MultiStepControlHandler.ControlActionType.CANCEL);
        }
    }//GEN-LAST:event_cancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton finishButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void performClick(MultiStepControlHandler.ControlActionType actionType) {
        switch (actionType) {
            case FINISH: {
                WindowUtils.doButtonClick(finishButton);
                break;
            }
            case CANCEL: {
                WindowUtils.doButtonClick(cancelButton);
                break;
            }
            case NEXT: {
                WindowUtils.doButtonClick(nextButton);
                break;
            }
            case PREVIOUS: {
                WindowUtils.doButtonClick(previousButton);
                break;
            }
            default:
                throw new IllegalStateException("Illegal action type " + actionType.name());
        }
    }

    @Nonnull
    @Override
    public OkCancelListener getOkCancelListener() {
        return okCancelListener;
    }

    @Nonnull
    @Override
    public MultiStepControlHandler.MultiStepControlEnablementListener createEnablementListener() {
        return (MultiStepControlHandler.ControlActionType actionType, boolean enablement) -> {
            switch (actionType) {
                case FINISH: {
                    finishButton.setEnabled(enablement);
                    break;
                }
                case CANCEL: {
                    cancelButton.setEnabled(enablement);
                    break;
                }
                case NEXT: {
                    nextButton.setEnabled(enablement);
                    break;
                }
                case PREVIOUS: {
                    previousButton.setEnabled(enablement);
                    break;
                }
                default:
                    throw new IllegalStateException("Illegal action type " + actionType.name());
            }
        };
    }
}
