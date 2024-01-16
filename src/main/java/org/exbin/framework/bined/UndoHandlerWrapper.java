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
package org.exbin.framework.bined;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.bined.operation.BinaryDataCommand;
import org.exbin.bined.operation.BinaryDataOperationException;
import org.exbin.bined.operation.undo.BinaryDataUndoHandler;
import org.exbin.bined.operation.undo.BinaryDataUndoUpdateListener;
import org.exbin.xbup.operation.Command;
import org.exbin.xbup.operation.undo.XBUndoHandler;
import org.exbin.xbup.operation.undo.XBUndoUpdateListener;

/**
 * Undo handler wrapper.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class UndoHandlerWrapper implements XBUndoHandler {

    private BinaryDataUndoHandler handler;
    private final Map<XBUndoUpdateListener, BinaryDataUndoUpdateListener> listenersMap = new HashMap<>();

    public UndoHandlerWrapper() {
    }

    public void setHandler(@Nullable BinaryDataUndoHandler newHandler) {
        if (handler != null) {
            for (BinaryDataUndoUpdateListener listener : listenersMap.values()) {
                handler.removeUndoUpdateListener(listener);
            }
        }

        this.handler = newHandler;
        for (BinaryDataUndoUpdateListener listener : listenersMap.values()) {
            handler.addUndoUpdateListener(listener);
        }
    }

    @Override
    public boolean canRedo() {
        return handler != null ? handler.canRedo() : false;
    }

    @Override
    public boolean canUndo() {
        return handler != null ? handler.canUndo() : false;
    }

    @Override
    public void clear() {
        handler.clear();
    }

    @Override
    public void doSync() throws Exception {
        handler.doSync();
    }

    @Override
    public void execute(Command cmnd) throws Exception {
        handler.execute(new BinaryCommandWrapper(cmnd));
    }

    @Override
    public void addCommand(Command cmnd) {
        handler.addCommand(new BinaryCommandWrapper(cmnd));
    }

    @Nonnull
    @Override
    public List<Command> getCommandList() {
        List<Command> result = new ArrayList<>();
        if (handler != null) {
            handler.getCommandList().forEach((command) -> {
                result.add(new CommandWrapper(command));
            });
        }

        return result;
    }

    @Override
    public long getCommandPosition() {
        return handler != null ? handler.getCommandPosition() : 0;
    }

    @Override
    public long getMaximumUndo() {
        return handler != null ? handler.getMaximumUndo() : 0;
    }

    @Override
    public long getSyncPoint() {
        return handler != null ? handler.getSyncPoint() : 0;
    }

    @Override
    public long getUndoMaximumSize() {
        return handler != null ? handler.getUndoMaximumSize() : 0;
    }

    @Override
    public long getUsedSize() {
        return handler != null ? handler.getUsedSize() : 0;
    }

    @Override
    public void performRedo() throws Exception {
        handler.performRedo();
    }

    @Override
    public void performRedo(int i) throws Exception {
        handler.performRedo(i);
    }

    @Override
    public void performUndo() throws Exception {
        handler.performUndo();
    }

    @Override
    public void performUndo(int i) throws Exception {
        handler.performUndo(i);
    }

    @Override
    public void setCommandPosition(long l) throws Exception {
        handler.setCommandPosition(l);
    }

    @Override
    public void setSyncPoint(long l) {
        handler.setSyncPoint(l);
    }

    @Override
    public void setSyncPoint() {
        handler.setSyncPoint();
    }

    @Override
    public void addUndoUpdateListener(final XBUndoUpdateListener listener) {
        BinaryDataUndoUpdateListener binaryListener = new BinaryDataUndoUpdateListener() {
            @Override
            public void undoCommandPositionChanged() {
                listener.undoCommandPositionChanged();
            }

            @Override
            public void undoCommandAdded(BinaryDataCommand bdc) {
                listener.undoCommandAdded(new CommandWrapper(bdc));
            }
        };
        listenersMap.put(listener, binaryListener);
        if (handler != null) {
            handler.addUndoUpdateListener(binaryListener);
        }
    }

    @Override
    public void removeUndoUpdateListener(XBUndoUpdateListener listener) {
        BinaryDataUndoUpdateListener binaryListener = listenersMap.remove(listener);
        if (handler != null) {
            handler.removeUndoUpdateListener(binaryListener);
        }
    }

    @ParametersAreNonnullByDefault
    private static class CommandWrapper implements Command {

        private final BinaryDataCommand command;

        public CommandWrapper(BinaryDataCommand command) {
            this.command = command;
        }

        @Nonnull
        @Override
        public String getCaption() {
            return command.getCaption();
        }

        @Override
        public void execute() throws Exception {
            command.execute();
        }

        @Override
        public void use() {
            command.use();
        }

        @Override
        public void redo() throws Exception {
            command.redo();
        }

        @Override
        public void undo() throws Exception {
            command.undo();
        }

        @Override
        public boolean canUndo() {
            return command.canUndo();
        }

        @Override
        public void dispose() throws Exception {
            command.dispose();
        }

        @Nonnull
        @Override
        public Optional<Date> getExecutionTime() {
            return command.getExecutionTime();
        }
    }

    @ParametersAreNonnullByDefault
    private static class BinaryCommandWrapper implements BinaryDataCommand {

        private final Command command;

        public BinaryCommandWrapper(Command command) {
            this.command = command;
        }

        @Nonnull
        @Override
        public String getCaption() {
            return command.getCaption();
        }

        @Override
        public void execute() throws BinaryDataOperationException {
            try {
                command.execute();
            } catch (Exception ex) {
                throw new BinaryDataOperationException(ex);
            }
        }

        @Override
        public void use() {
            command.use();
        }

        @Override
        public void redo() throws BinaryDataOperationException {
            try {
                command.redo();
            } catch (Exception ex) {
                throw new BinaryDataOperationException(ex);
            }
        }

        @Override
        public void undo() throws BinaryDataOperationException {
            try {
                command.undo();
            } catch (Exception ex) {
                throw new BinaryDataOperationException(ex);
            }
        }

        @Override
        public boolean canUndo() {
            return command.canUndo();
        }

        @Override
        public void dispose() throws BinaryDataOperationException {
            try {
                command.dispose();
            } catch (Exception ex) {
                throw new BinaryDataOperationException(ex);
            }
        }

        @Nonnull
        @Override
        public Optional<Date> getExecutionTime() {
            return command.getExecutionTime();
        }
    }
}
