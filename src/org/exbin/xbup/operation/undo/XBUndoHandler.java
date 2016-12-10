/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.exbin.xbup.operation.undo;

import java.util.List;
import org.exbin.xbup.operation.Command;

/**
 * Undo support handler.
 *
 * @version 0.2.0 2016/01/24
 * @author ExBin Project (http://exbin.org)
 */
public interface XBUndoHandler {

    boolean canRedo();

    boolean canUndo();

    void clear();

    /**
     * Performs revert to sync point.
     *
     * @throws java.lang.Exception
     */
    void doSync() throws Exception;

    /**
     * Adds new step into command list.
     *
     * @param command command
     * @throws java.lang.Exception
     */
    void execute(Command command) throws Exception;

    /**
     * Adds new step into command list without executing it.
     *
     * @param command command
     */
    void addCommand(Command command);

    List<Command> getCommandList();

    long getCommandPosition();

    long getMaximumUndo();

    long getSyncPoint();

    long getUndoMaximumSize();

    long getUsedSize();

    /**
     * Performs single redo step.
     *
     * @throws java.lang.Exception
     */
    void performRedo() throws Exception;

    /**
     * Performs multiple redo step.
     *
     * @param count count of steps
     * @throws Exception
     */
    void performRedo(int count) throws Exception;

    /**
     * Performs single undo step.
     *
     * @throws java.lang.Exception
     */
    void performUndo() throws Exception;

    /**
     * Performs multiple undo step.
     *
     * @param count count of steps
     * @throws Exception
     */
    void performUndo(int count) throws Exception;

    /**
     * Performs undo or redo operation to reach given position.
     *
     * @param targetPosition desired position
     * @throws java.lang.Exception
     */
    void setCommandPosition(long targetPosition) throws Exception;

    void setSyncPoint(long syncPoint);

    void setSyncPoint();

    void addUndoUpdateListener(XBUndoUpdateListener listener);

    void removeUndoUpdateListener(XBUndoUpdateListener listener);
}
