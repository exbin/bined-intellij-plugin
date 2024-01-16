/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.exbin.xbup.operation.undo;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.xbup.operation.Command;

/**
 * Undo support handler.
 *
 * @author ExBin Project (http://exbin.org)
 */
public interface XBUndoHandler {

    boolean canRedo();

    boolean canUndo();

    void clear();

    /**
     * Performs revert to sync point.
     *
     * @throws java.lang.Exception exception
     */
    void doSync() throws Exception;

    /**
     * Adds new step into command list.
     *
     * @param command command
     * @throws java.lang.Exception exception
     */
    void execute(@Nonnull Command command) throws Exception;

    /**
     * Adds new step into command list without executing it.
     *
     * @param command command
     */
    void addCommand(@Nonnull Command command);

    @Nullable
    List<Command> getCommandList();

    long getCommandPosition();

    long getMaximumUndo();

    long getSyncPoint();

    long getUndoMaximumSize();

    long getUsedSize();

    /**
     * Performs single redo step.
     *
     * @throws java.lang.Exception exception
     */
    void performRedo() throws Exception;

    /**
     * Performs multiple redo step.
     *
     * @param count count of steps
     * @throws Exception exception
     */
    void performRedo(int count) throws Exception;

    /**
     * Performs single undo step.
     *
     * @throws java.lang.Exception exception
     */
    void performUndo() throws Exception;

    /**
     * Performs multiple undo step.
     *
     * @param count count of steps
     * @throws Exception exception
     */
    void performUndo(int count) throws Exception;

    /**
     * Performs undo or redo operation to reach given position.
     *
     * @param targetPosition desired position
     * @throws java.lang.Exception exception
     */
    void setCommandPosition(long targetPosition) throws Exception;

    void setSyncPoint(long syncPoint);

    void setSyncPoint();

    void addUndoUpdateListener(@Nonnull XBUndoUpdateListener listener);

    void removeUndoUpdateListener(@Nonnull XBUndoUpdateListener listener);
}
