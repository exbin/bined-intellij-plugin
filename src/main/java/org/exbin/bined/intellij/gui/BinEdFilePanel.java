package org.exbin.bined.intellij.gui;

import org.exbin.bined.CodeType;
import org.exbin.bined.swing.extended.ExtCodeArea;
import org.exbin.framework.App;
import org.exbin.framework.action.api.ActionModuleApi;
import org.exbin.framework.action.api.ComponentActivationService;
import org.exbin.framework.bined.BinEdFileHandler;
import org.exbin.framework.bined.gui.BinEdComponentPanel;
import org.exbin.framework.bined.gui.BinaryStatusPanel;
import org.exbin.framework.frame.api.FrameModuleApi;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.JPanel;
import java.awt.BorderLayout;

@ParametersAreNonnullByDefault
public class BinEdFilePanel extends JPanel {

    private BinEdFileHandler fileHandler;
    private BinEdToolbarPanel toolbarPanel = new BinEdToolbarPanel();
    private BinaryStatusPanel statusPanel = new BinaryStatusPanel();

    public BinEdFilePanel() {
        super(new BorderLayout());
        ActionModuleApi actionModule = App.getModule(ActionModuleApi.class);
        FrameModuleApi frameModule = App.getModule(FrameModuleApi.class);
        ComponentActivationService componentActivationService = frameModule.getFrameHandler().getComponentActivationService();
        add(toolbarPanel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.SOUTH);
    }

    public void setFileHandler(BinEdFileHandler fileHandler) {
        this.fileHandler = fileHandler;
        BinEdComponentPanel componentPanel = fileHandler.getComponent();
        ExtCodeArea codeArea = fileHandler.getCodeArea();
        toolbarPanel.setTargetComponent(componentPanel);
        toolbarPanel.setCodeAreaControl(new BinEdToolbarPanel.Control() {
            @Nonnull
            @Override public CodeType getCodeType() {
                return codeArea.getCodeType();
            }

            @Override
            public void setCodeType(CodeType codeType) {
                codeArea.setCodeType(codeType);
            }

            @Override
            public boolean isShowUnprintables() {
                return codeArea.isShowUnprintables();
            }

            @Override
            public void setShowUnprintables(boolean showUnprintables) {
                codeArea.setShowUnprintables(showUnprintables);
            }

            @Override
            public void repaint() {
                codeArea.repaint();
            }
        });

        add(componentPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
