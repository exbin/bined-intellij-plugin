/*
 * Copyright (C) ExBin Project
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
package org.exbin.bined.delta;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.exbin.bined.delta.list.DefaultDoublyLinkedList;
import org.exbin.bined.delta.list.DoublyLinkedItem;
import org.exbin.utils.binary_data.BinaryData;

/**
 * Repository of delta segments.
 *
 * @version 0.2.0 2018/04/27
 * @author ExBin Project (https://exbin.org)
 */
public class SegmentsRepository {

    @Nonnull
    private final Map<FileDataSource, DataSegmentsMap> fileSources = new HashMap<>();
    @Nonnull
    private final Map<MemoryDataSource, DataSegmentsMap> memorySources = new HashMap<>();

    @Nonnull
    private final List<DeltaDocument> documents = new ArrayList<>();
    /**
     * Limit for save processing in bytes.
     */
    private static final int PROCESSING_LIMIT = 4096;

    public SegmentsRepository() {
    }

    @Nonnull
    public FileDataSource openFileSource(@Nonnull File sourceFile) throws IOException {
        FileDataSource fileSource = new FileDataSource(sourceFile);
        fileSources.put(fileSource, new DataSegmentsMap());
        return fileSource;
    }

    @Nonnull
    public FileDataSource openFileSource(@Nonnull File sourceFile, @Nonnull FileDataSource.EditationMode editationMode) throws IOException {
        FileDataSource fileSource = new FileDataSource(sourceFile, editationMode);
        fileSources.put(fileSource, new DataSegmentsMap());
        return fileSource;
    }

    public void closeFileSource(@Nonnull FileDataSource fileSource) {
        // TODO
        fileSource.close();
    }

    @Nonnull
    public MemoryDataSource openMemorySource() {
        MemoryDataSource memorySource = new MemoryDataSource();
        memorySources.put(memorySource, new DataSegmentsMap());
        return memorySource;
    }

    public void closeMemorySource(@Nonnull MemoryDataSource memorySource) {
        // TODO
        memorySource.clear();
    }

    /**
     * Creates empty delta document.
     *
     * @return delta document
     */
    @Nonnull
    public DeltaDocument createDocument() {
        DeltaDocument document = new DeltaDocument(this);
        documents.add(document);
        return document;
    }

    /**
     * Creates delta document for given file source.
     *
     * @param fileSource file source
     * @return delta document
     * @throws IOException if input/output error
     */
    @Nonnull
    public DeltaDocument createDocument(FileDataSource fileSource) throws IOException {
        DeltaDocument document = new DeltaDocument(this, fileSource);
        documents.add(document);
        return document;
    }

    /**
     * Saves document to it's source file and update all documents.
     *
     * @param savedDocument document to save
     * @throws java.io.IOException if input/output error
     */
    public void saveDocument(@Nonnull DeltaDocument savedDocument) throws IOException {
        FileDataSource fileSource = savedDocument.getFileSource();

        // Create save transformation
        Map<DataSegment, Long> saveMap = createSaveTransformation(savedDocument);

        // Apply transformation to other documents
        for (DeltaDocument document : documents) {
            if (document != savedDocument) {
                applySaveMap(document, saveMap, fileSource);
            }
        }

        DefaultDoublyLinkedList<DataSegment> segments = savedDocument.getSegments();

        // Save all non-overlapping segments
        List<DataArea> releasedSegments = new LinkedList<>();
        {
            DataSegment segment = segments.first();
            long segmentDocumentPosition = 0;
            while (segment != null) {
                processSegmentForSave(segment, fileSource, segmentDocumentPosition, savedDocument, saveMap, releasedSegments);

                segmentDocumentPosition += segment.getLength();
                segment = savedDocument.getSegment(segmentDocumentPosition);
            }
        }

        // Handle all released segments
        while (!releasedSegments.isEmpty()) {
            DataArea dataArea = releasedSegments.remove(releasedSegments.size() - 1);
            DataSegment segment = savedDocument.getSegment(dataArea.startFrom);
            while (segment != null) {
                long segmentPosition = saveMap.get(segment);
                if (segmentPosition > dataArea.startFrom + dataArea.length) {
                    break;
                }

                DataSegment nextSegment = segment.getNext();
                if (!(segment instanceof SpaceSegment)) {
                    long segmentDocumentPosition = saveMap.get(segment);
                    processSegmentForSave(segment, fileSource, segmentDocumentPosition, savedDocument, saveMap, releasedSegments);
                }

                segment = nextSegment;
            }
        }

        // Save all remaining segments
        // Loads overlaping areas to memory before next segment is saved
        {
            DataSegment segment = segments.first();
            long segmentDocumentPosition = 0;
            while (segment != null) {
                if (!(segment instanceof SpaceSegment)) {
                    long currentSegmentDocumentPosition = saveMap.get(segment);
                    long currentSegmentLength = segment.getLength();
                    long processed = 0;
                    while (currentSegmentLength > 0) {
                        long length = currentSegmentLength;
                        if (length > PROCESSING_LIMIT) {
                            length = PROCESSING_LIMIT;
                        }

                        saveSegmentSection(currentSegmentDocumentPosition + processed, length, fileSource, saveMap, savedDocument);

                        currentSegmentLength -= length;
                        processed += length;
                        if (currentSegmentLength > 0) {
                            DataSegment nextSegment = savedDocument.getSegment(segmentDocumentPosition + processed);
                            if (nextSegment != null) {
                                saveMap.put(nextSegment, currentSegmentDocumentPosition + processed);
                            }
                        }
                    }
                }

                segmentDocumentPosition += segment.getLength();
                segment = savedDocument.getSegment(segmentDocumentPosition);
            }
        }

        // Update document segments
        long fileLength = savedDocument.getDataSize();
        savedDocument.clear();
        DataSegment fullFileSegment = createFileSegment(fileSource, 0, fileLength);
        savedDocument.getSegments().add(fullFileSegment);
        savedDocument.setDataLength(fileLength);
        fileSource.setFileLength(fileLength);
        fileSource.clearCache();
    }

    private void processSegmentForSave(@Nonnull DataSegment segment, @Nonnull FileDataSource fileSource, long segmentDocumentPosition, @Nonnull DeltaDocument savedDocument, @Nonnull Map<DataSegment, Long> saveMap, @Nonnull List<DataArea> releasedSegments) {
        boolean saveSegment = true;
        boolean hasOverlaps = hasFileOverlaps(segmentDocumentPosition, segment, fileSource);
        if (!hasOverlaps) {
            if (segment instanceof FileSegment) {
                FileSegment fileSegment = (FileSegment) segment;
                long segmentLength = segment.getLength();
                FileDataSource source = fileSegment.getSource();
                if (source == savedDocument.getFileSource() && segmentDocumentPosition == fileSegment.getStartPosition()) {
                    // Segment points to the original position, replace with empty space placeholder without saving
                    SpaceSegment spaceSegment = new SpaceSegment(segmentLength);
                    savedDocument.replaceSegment(segmentDocumentPosition, spaceSegment);
                    saveMap.put(spaceSegment, segmentDocumentPosition);
                    saveSegment = false;
                } else {
                    releasedSegments.add(new DataArea(segment.getStartPosition(), segmentLength));
                }
            }

            if (saveSegment) {
                // Save segment to file and replace it with placeholder
                saveSegment(fileSource, segmentDocumentPosition, segment);
                SpaceSegment spaceSegment = new SpaceSegment(segment.getLength());
                savedDocument.replaceSegment(segmentDocumentPosition, spaceSegment);
                saveMap.put(spaceSegment, segmentDocumentPosition);
            }
        }
    }

    private boolean hasFileOverlaps(long startPosition, @Nonnull DataSegment segment, @Nonnull FileDataSource fileSource) {
        DataSegmentsMap segmentsMap = fileSources.get(fileSource);
        SegmentRecord record = segmentsMap.focusFirstOverlay(startPosition, segment.getLength());
        while (record != null && record.getStartPosition() < startPosition + segment.getLength()) {
            if (record.dataSegment == segment || (record.getStartPosition() + record.getLength() <= startPosition)) {
                record = record.next;
            } else {
                return true;
            }
        }

        return false;
    }

    /**
     * Processes section of the segment for overlaps and then save it.
     *
     * @param savePosition start of the section
     * @param saveLength length of the section
     */
    private void saveSegmentSection(long savePosition, long saveLength, @Nonnull FileDataSource fileSource, @Nonnull Map<DataSegment, Long> saveMap, @Nonnull DeltaDocument savedDocument) {
        DataSegment segment = savedDocument.getSegment(savePosition);
        Long segmentSavePosition = saveMap.get(segment);
        if (segmentSavePosition == null) {
            throw new IllegalStateException("Unexpected missing save position");
        }
        long segmentDocumentPosition = segmentSavePosition;
        long sectionStart = savePosition - segmentDocumentPosition;
        DataSegmentsMap segmentsMap = fileSources.get(fileSource);
        SegmentRecord firstRecord = segmentsMap.focusFirstOverlay(segmentDocumentPosition + sectionStart, saveLength);
        while (firstRecord != null && (!saveMap.containsKey(firstRecord.dataSegment) || firstRecord.dataSegment == segment)) {
            firstRecord = firstRecord.next;
            if (firstRecord != null && firstRecord.getStartPosition() >= segmentDocumentPosition + sectionStart + saveLength) {
                firstRecord = null;
                break;
            }
        }

        if (firstRecord != null) {
            SegmentRecord record = firstRecord.next;
            if (record != null && record.getStartPosition() >= segmentDocumentPosition + sectionStart + saveLength) {
                record = null;
            }

            if (record != null) {
                record = segmentsMap.focusFirstOverlay(segmentDocumentPosition + sectionStart, saveLength);
                do {
                    if (record == null || (record.getStartPosition() >= segmentDocumentPosition + sectionStart + saveLength)) {
                        break;
                    }
                    SegmentRecord nextRecord = record.next;
                    if (record.dataSegment != segment) {
                        long overlapLength = record.getLength();
                        long overlapStart = 0;
                        if (segmentDocumentPosition + sectionStart > record.getStartPosition()) {
                            overlapStart = segmentDocumentPosition + sectionStart - record.getStartPosition();
                            overlapLength -= overlapStart;
                        }
                        if (record.getStartPosition() + overlapStart + overlapLength > segmentDocumentPosition + sectionStart + saveLength) {
                            overlapLength = segmentDocumentPosition + sectionStart + saveLength - firstRecord.getStartPosition() - overlapStart;
                        }
                        if (overlapLength > 0) {
                            preloadSegmentSection(record.dataSegment, overlapStart, overlapLength, fileSource, saveMap, savedDocument);
                        }
                    }
                    record = nextRecord;
                } while (record != null);
            } else {
                long overlapLength = firstRecord.getLength();
                long overlapStart = 0;
                if (segmentDocumentPosition + sectionStart > firstRecord.getStartPosition()) {
                    overlapStart = segmentDocumentPosition + sectionStart - firstRecord.getStartPosition();
                    overlapLength -= overlapStart;
                }
                if (firstRecord.getStartPosition() + overlapStart + overlapLength > segmentDocumentPosition + sectionStart + saveLength) {
                    overlapLength = segmentDocumentPosition + sectionStart + saveLength - firstRecord.getStartPosition() - overlapStart;
                }
                if (overlapLength > 0) {
                    long overlapPosition = saveMap.get(firstRecord.dataSegment);
                    preloadSegmentSection(firstRecord.dataSegment, overlapStart, overlapLength, fileSource, saveMap, savedDocument);
                    // TODO: Replace recursion with iteration
                    saveSegmentSection(overlapPosition + overlapStart, overlapLength, fileSource, saveMap, savedDocument);
                }
            }
        }

        // Process all segments in given section and save them to file
        long length = saveLength;
        long processed = 0;
        while (length > 0) {
            DataSegment savedSegment = savedDocument.getSegment(savePosition + processed);
            long savedSegmentPosition = segmentDocumentPosition + processed;
            long overlapLength = savedSegmentPosition + savedSegment.getLength() - savePosition;
            long overlapStart = savePosition - savedSegmentPosition;

            if (!(savedSegment instanceof SpaceSegment)) {
                if (!(savedSegment instanceof FileSegment && ((FileSegment) savedSegment).getSource() == fileSource && savedSegmentPosition == savedSegment.getStartPosition())) {
                    // Save only if there is actual change
                    saveSegment(fileSource, savedSegmentPosition, savedSegment, overlapStart, overlapLength);
                }
                DataSegment originalSegment = savedDocument.getSegment(savedSegmentPosition + overlapStart);
                saveMap.remove(originalSegment);
                savedDocument.replaceSegment(savedSegmentPosition + overlapStart, new SpaceSegment(overlapLength));
                if (savedSegmentPosition + overlapStart + overlapLength < segment.getLength()) {
                    DataSegment followingSegment = savedDocument.getSegment(savedSegmentPosition + overlapStart + overlapLength);
                    if (followingSegment != null) {
                        saveMap.put(followingSegment, savedSegmentPosition + overlapStart + overlapLength);
                    }
                }
            }

            processed += overlapLength;
            length -= overlapLength;
        }
    }

    /**
     * Loads part of the file segment into memory for saving.
     *
     * @param segment segment
     * @param sectionStart section start
     * @param sectionLength section length
     * @param fileSource file source
     */
    private void preloadSegmentSection(@Nonnull DataSegment segment, long sectionStart, long sectionLength, @Nonnull FileDataSource fileSource, @Nonnull Map<DataSegment, Long> saveMap, @Nonnull DeltaDocument savedDocument) {
        Long segmentDocumentPosition = saveMap.get(segment);
        if (segmentDocumentPosition == null) {
            // Segment is from other document, should be converted elsewhere - skip
            return;
        }
        if (segment == null || (!(segment instanceof FileSegment)) || ((FileSegment) segment).getSource() != fileSource) {
            throw new IllegalArgumentException("Segment is not valid for preloading");
        }

        MemorySegment preloadedSegment = createMemorySegment();
        preloadedSegment.setLength(sectionLength);
        preloadedSegment.getSource().insert(0, savedDocument, segmentDocumentPosition + sectionStart, sectionLength);
        savedDocument.replaceSegment(segmentDocumentPosition + sectionStart, preloadedSegment);
        saveMap.put(preloadedSegment, segmentDocumentPosition + sectionStart);
        DataSegment afterSegment = savedDocument.getSegment(segmentDocumentPosition + sectionStart + sectionLength);
        if (afterSegment != null) {
            saveMap.put(afterSegment, segmentDocumentPosition + sectionStart + sectionLength);
        }
    }

    private void saveSegment(@Nonnull FileDataSource fileSource, long targetPosition, @Nonnull DataSegment segment) {
        saveSegment(fileSource, targetPosition, segment, 0, segment.getLength());
    }

    private void saveSegment(@Nonnull FileDataSource fileSource, long targetPosition, @Nonnull DataSegment segment, long segmentOffset, long segmentLimit) {
        RandomAccessFile accessFile = fileSource.getAccessFile();
        try {
            if (segment instanceof MemorySegment) {
                MemorySegment memorySegment = (MemorySegment) segment;
                MemoryDataSource source = memorySegment.getSource();

                accessFile.seek(targetPosition);
                long sectionPosition = memorySegment.getStartPosition() + segmentOffset;
                long sectionLength = segmentLimit;
                byte[] buffer = new byte[PROCESSING_LIMIT];
                while (sectionLength > 0) {
                    int length = sectionLength < PROCESSING_LIMIT ? (int) sectionLength : PROCESSING_LIMIT;
                    source.copyToArray(sectionPosition, buffer, 0, length);
                    accessFile.write(buffer, 0, length);
                    sectionPosition += length;
                    sectionLength -= length;
                }
            } else {
                FileSegment fileSegment = (FileSegment) segment;
                FileDataSource source = fileSegment.getSource();
                RandomAccessFile sourceFile = source.getAccessFile();

                long sectionPosition = fileSegment.getStartPosition() + segmentOffset;
                long sectionLength = segmentLimit;
                long sectionProcessed = 0;

                if (source == fileSource && targetPosition > sectionPosition && sectionPosition + sectionLength >= targetPosition) {
                    // Saved segment overlaps itself, reverse writting is needed
                    byte[] buffer = new byte[PROCESSING_LIMIT];
                    while (sectionLength > 0) {
                        int length = sectionLength < PROCESSING_LIMIT ? (int) sectionLength : PROCESSING_LIMIT;
                        int toProcess = length;
                        sourceFile.seek(sectionPosition + sectionLength - length);
                        while (toProcess > 0) {
                            int red = sourceFile.read(buffer, length - toProcess, toProcess);
                            toProcess -= red;
                        }
                        accessFile.seek(targetPosition + sectionLength - length);
                        accessFile.write(buffer, 0, length);

                        sectionLength -= length;
                        sectionProcessed += length;
                    }
                } else {
                    byte[] buffer = new byte[PROCESSING_LIMIT];
                    while (sectionLength > 0) {
                        int length = sectionLength < PROCESSING_LIMIT ? (int) sectionLength : PROCESSING_LIMIT;
                        sourceFile.seek(sectionPosition + sectionProcessed);
                        length = sourceFile.read(buffer, 0, length);
                        accessFile.seek(targetPosition + sectionProcessed);
                        accessFile.write(buffer, 0, length);
                        sectionLength -= length;
                        sectionProcessed += length;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SegmentsRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Nonnull
    private Map<DataSegment, Long> createSaveTransformation(@Nonnull DeltaDocument savedDocument) {
        Map<DataSegment, Long> transformation = new HashMap<>();
        DefaultDoublyLinkedList<DataSegment> segments = savedDocument.getSegments();
        long position = 0;
        for (DataSegment segment : segments) {
            transformation.put(segment, position);
            position += segment.getLength();
        }

        return transformation;
    }

    /**
     * Transforms all file segments to after save location.
     *
     * Process all segments in given document and for file segments from the
     * target document transform all overlaying parts to new positions.
     *
     * @param document document to process
     * @param saveMap save transformation map
     * @param fileSource saved file file source
     */
    private void applySaveMap(@Nonnull DeltaDocument document, @Nonnull Map<DataSegment, Long> saveMap, @Nonnull FileDataSource fileSource) {
        DataSegmentsMap segmentsMap = fileSources.get(fileSource);
        long documentPosition = 0;
        DataSegment segment = document.getSegment(0);
        while (segment != null) {
            DataSegment nextSegment = segment.getNext();
            long segmentLength = segment.getLength();
            if (segment instanceof FileSegment && ((FileSegment) segment).getSource() == fileSource) {
                long segmentPosition = segment.getStartPosition();
                long segmentEnd = segmentPosition + segmentLength;
                long processed = 0;

                // Split segment by saved file segments
                SegmentRecord record = segmentsMap.focusFirstOverlay(segmentPosition, segmentLength);
                while (record != null && processed < segmentLength && record.getStartPosition() <= segmentEnd) {
                    Long savePosition = saveMap.get(record.dataSegment);
                    if (savePosition != null && record.getStartPosition() + record.getLength() >= segmentPosition + processed) {
                        // Replace segment for file segment pointing to after-save position
                        long replacedLength = record.getLength();
                        long replacedPosition = record.getStartPosition();
                        if (segmentPosition > replacedPosition) {
                            replacedLength -= segmentPosition - replacedPosition;
                            replacedPosition = segmentPosition;
                        }
                        if (replacedPosition + replacedLength > segmentEnd) {
                            replacedLength = segmentEnd - replacedPosition;
                        }

                        if (replacedLength > 0) {
                            long replacedOffset = replacedPosition - segmentPosition;
                            if (processed < replacedOffset) {
                                preloadDocumentSection(document, documentPosition + processed, replacedOffset - processed);
                            }

                            if (savePosition + replacedOffset != segmentPosition) {
                                FileSegment newSegment = createFileSegment(fileSource, savePosition + replacedOffset, replacedLength);
                                document.remove(documentPosition + replacedOffset, replacedLength);
                                document.insertSegment(documentPosition + replacedOffset, newSegment);
                            }
                            processed = replacedOffset + replacedLength;
                        }
                    }

                    record = segmentsMap.records.nextTo(record);
                    if (record == null) {
                        break;
                    }
                }

                if (processed < segmentLength) {
                    preloadDocumentSection(document, documentPosition + processed, segmentLength - processed);
                }
            }

            documentPosition += segmentLength;
            segment = nextSegment;
        }
        document.clearCache();
    }

    private void preloadDocumentSection(@Nonnull DeltaDocument document, long documentPosition, long sectionLength) {
        MemorySegment preloadedSegment = createMemorySegment();
        preloadedSegment.setLength(sectionLength);
        preloadedSegment.getSource().insert(0, document, documentPosition, sectionLength);
        document.replaceSegment(documentPosition, preloadedSegment);
    }

    /**
     * Creates new file segment on given file source.
     *
     * @param fileSource file source
     * @param startPosition start position
     * @param length length
     * @return file segment
     */
    @Nonnull
    public FileSegment createFileSegment(@Nonnull FileDataSource fileSource, long startPosition, long length) {
        FileSegment fileSegment = new FileSegment(fileSource, startPosition, length);
        DataSegmentsMap segmentsMap = fileSources.get(fileSource);
        segmentsMap.add(fileSegment);
        return fileSegment;
    }

    public void dropFileSegment(@Nonnull FileSegment fileSegment) {
        DataSegmentsMap segmentsMap = fileSources.get(fileSegment.getSource());
        segmentsMap.remove(fileSegment);
    }

    @Nonnull
    public MemorySegment createMemorySegment() {
        return createMemorySegment(openMemorySource(), 0, 0);
    }

    /**
     * Creates new memory segment on given memory source.
     *
     * @param memorySource memory source
     * @param startPosition start position
     * @param length length
     * @return memory segment
     */
    @Nonnull
    public MemorySegment createMemorySegment(@Nonnull MemoryDataSource memorySource, long startPosition, long length) {
        if (startPosition + length > memorySource.getDataSize()) {
            memorySource.setDataSize(startPosition + length);
        }

        MemorySegment memorySegment = new MemorySegment(memorySource, startPosition, length);
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        segmentsMap.add(memorySegment);
        return memorySegment;
    }

    @Nonnull
    public void updateSegment(@Nonnull DataSegment segment, long position, long length) {
        if (segment instanceof MemorySegment) {
            DataSegmentsMap segmentsMap = memorySources.get(((MemorySegment) segment).getSource());
            segmentsMap.updateSegment(segment, position, length);
        } else {
            DataSegmentsMap segmentsMap = fileSources.get(((FileSegment) segment).getSource());
            segmentsMap.updateSegment(segment, position, length);
        }
    }

    public void updateSegmentLength(@Nonnull DataSegment segment, long length) {
        if (segment instanceof MemorySegment) {
            DataSegmentsMap segmentsMap = memorySources.get(((MemorySegment) segment).getSource());
            segmentsMap.updateSegmentLength(segment, length);
        } else {
            DataSegmentsMap segmentsMap = fileSources.get(((FileSegment) segment).getSource());
            segmentsMap.updateSegmentLength(segment, length);
        }
    }

    public void dropMemorySegment(@Nonnull MemorySegment memorySegment) {
        DataSegmentsMap segmentsMap = memorySources.get(memorySegment.getSource());
        segmentsMap.remove(memorySegment);
    }

    public void dropSegment(@Nonnull DataSegment segment) {
        if (segment instanceof FileSegment) {
            dropFileSegment((FileSegment) segment);
        } else if (segment instanceof MemorySegment) {
            dropMemorySegment((MemorySegment) segment);
        }
    }

    public void dropDocument(@Nonnull DeltaDocument document) {
        for (DataSegment segment : document.getSegments()) {
            dropSegment(segment);
        }
        document.clear();
        documents.remove(document);
    }

    /**
     * Sets byte to given segment.
     *
     * Handles shared memory between multiple segments.
     *
     * @param memorySegment memory segment
     * @param segmentPosition relative position to segment start
     * @param value value to set
     */
    public void setMemoryByte(@Nonnull MemorySegment memorySegment, long segmentPosition, byte value) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, segmentPosition, 1);

        long sourcePosition = memorySegment.getStartPosition() + segmentPosition;
        if (segmentPosition >= memorySegment.getLength()) {
            segmentsMap.updateSegmentLength(memorySegment, segmentPosition + 1);
            if (sourcePosition >= memorySource.getDataSize()) {
                memorySource.setDataSize(sourcePosition + 1);
            }
        }
        memorySource.setByte(memorySegment.getStartPosition() + segmentPosition, value);
    }

    public void insertMemoryData(@Nonnull MemorySegment memorySegment, long segmentPosition, @Nonnull BinaryData insertedData) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, segmentPosition, 0);

        long sourcePosition = memorySegment.getStartPosition() + segmentPosition;
        shiftSegments(memorySegment, sourcePosition, insertedData.getDataSize());
        memorySource.insert(sourcePosition, insertedData);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + insertedData.getDataSize());
    }

    public void insertMemoryData(@Nonnull MemorySegment memorySegment, long segmentPosition, @Nonnull BinaryData insertedData, long insertedDataOffset, long insertedDataLength) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, segmentPosition, 0);

        long sourcePosition = memorySegment.getStartPosition() + segmentPosition;
        shiftSegments(memorySegment, sourcePosition, insertedDataLength);
        memorySource.insert(sourcePosition, insertedData, insertedDataOffset, insertedDataLength);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + insertedDataLength);
    }

    public void insertMemoryData(@Nonnull MemorySegment memorySegment, long segmentPosition, byte[] insertedData) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, segmentPosition, 0);

        long sourcePosition = memorySegment.getStartPosition() + segmentPosition;
        shiftSegments(memorySegment, sourcePosition, insertedData.length);
        memorySource.insert(sourcePosition, insertedData);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + insertedData.length);
    }

    public void insertMemoryData(@Nonnull MemorySegment memorySegment, long segmentPosition, @Nonnull byte[] insertedData, int insertedDataOffset, int insertedDataLength) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, segmentPosition, 0);

        long sourcePosition = memorySegment.getStartPosition() + segmentPosition;
        shiftSegments(memorySegment, sourcePosition, insertedDataLength);
        memorySource.insert(sourcePosition, insertedData, insertedDataOffset, insertedDataLength);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + insertedDataLength);
    }

    public void insertMemoryData(@Nonnull MemorySegment memorySegment, long segmentPosition, long length) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, segmentPosition, 0);

        long sourcePosition = memorySegment.getStartPosition() + segmentPosition;
        shiftSegments(memorySegment, sourcePosition, length);
        memorySource.insert(sourcePosition, length);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + length);
    }

    public void insertUninitializedMemoryData(@Nonnull MemorySegment memorySegment, long segmentPosition, long length) {
        MemoryDataSource memorySource = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(memorySource);
        detachMemoryArea(memorySegment, segmentPosition, 0);

        long sourcePosition = memorySegment.getStartPosition() + segmentPosition;
        shiftSegments(memorySegment, sourcePosition, length);
        memorySource.insertUninitialized(sourcePosition, length);
        segmentsMap.updateSegmentLength(memorySegment, memorySegment.getLength() + length);
    }

    /**
     * Detaches all other memory segments crossing given area of provided memory
     * segment.
     *
     * @param memorySegment provided memory segment
     * @param segmentPosition position
     * @param length length
     */
    public void detachMemoryArea(@Nonnull MemorySegment memorySegment, long segmentPosition, long length) {
        long sourcePosition = memorySegment.getStartPosition() + segmentPosition;
        DataSegmentsMap segmentsMap = memorySources.get(memorySegment.getSource());
        if (!segmentsMap.hasMoreSegments()) {
            return;
        }

        SegmentRecord record = segmentsMap.focusFirstOverlay(sourcePosition, length);
        while (record != null) {
            SegmentRecord nextRecord = record.getNext();
            if (record.getStartPosition() > sourcePosition + length) {
                break;
            }
            if (record.getStartPosition() + record.getLength() > sourcePosition) {
                DataSegment segment = record.dataSegment;
                if (segment != memorySegment) {
                    detachSegment((MemorySegment) segment);
                }
            }

            record = nextRecord;
        }
    }

    public void detachSegment(@Nonnull MemorySegment memorySegment) {
        MemoryDataSource source = memorySegment.getSource();
        MemoryDataSource newMemorySource = openMemorySource();
        newMemorySource.insert(0, source.copy(memorySegment.getStartPosition(), memorySegment.getLength()));
        DataSegmentsMap segmentsMap = memorySources.get(source);
        segmentsMap.remove(memorySegment);
        memorySegment.setSource(newMemorySource);
        DataSegmentsMap newSegmentsMap = memorySources.get(newMemorySource);
        newSegmentsMap.add(memorySegment);
    }

    /**
     * Shift all segments after given position in given direction except given
     * segment.
     *
     * Operation assumes there are no collisions.
     *
     * @param memorySegment memory segment to keep
     * @param position position of the shift
     * @param shift direction of the shift
     */
    private void shiftSegments(@Nonnull MemorySegment memorySegment, long position, long shift) {
        MemoryDataSource source = memorySegment.getSource();
        DataSegmentsMap segmentsMap = memorySources.get(source);
        SegmentRecord record = segmentsMap.focusFirstOverlay(position, source.getDataSize() - position);
        while (record != null) {
            SegmentRecord nextRecord = record.getNext();
            if (record.dataSegment != memorySegment && record.getStartPosition() >= position) {
                MemorySegment segment = (MemorySegment) record.dataSegment;
                segment.setStartPosition(segment.getStartPosition() + shift);
                record.maxPosition += shift;
            }
            record = nextRecord;
        }
    }

    /**
     * Creates copy of segment.
     *
     * @param segment original segment
     * @return copy of segment
     */
    @Nonnull
    public DataSegment copySegment(@Nonnull DataSegment segment) {
        if (segment instanceof MemorySegment) {
            MemorySegment memorySegment = (MemorySegment) segment;
            return createMemorySegment(memorySegment.getSource(), memorySegment.getStartPosition(), memorySegment.getLength());
        } else {
            FileSegment fileSegment = (FileSegment) segment;
            return createFileSegment(fileSegment.getSource(), fileSegment.getStartPosition(), fileSegment.getLength());
        }
    }

    /**
     * Creates copy of segment.
     *
     * @param segment original segment
     * @param offset segment area offset
     * @param length segment area length
     * @return copy of segment
     */
    @Nonnull
    public DataSegment copySegment(@Nonnull DataSegment segment, long offset, long length) {
        if (segment instanceof MemorySegment) {
            MemorySegment memorySegment = (MemorySegment) segment;
            return createMemorySegment(memorySegment.getSource(), memorySegment.getStartPosition() + offset, length);
        } else {
            FileSegment fileSegment = (FileSegment) segment;
            return createFileSegment(fileSegment.getSource(), fileSegment.getStartPosition() + offset, length);
        }
    }

    /**
     * Loads all segments from given source file to memory.
     *
     * @param fileSource file source
     */
    public void detachFileSource(@Nonnull FileDataSource fileSource) {
        for (DeltaDocument document : documents) {
            long documentPosition = 0;
            while (documentPosition < document.getDataSize()) {
                DataSegment segment = document.getSegment(documentPosition);
                long segmentLength = segment.getLength();
                if (segment instanceof FileSegment && ((FileSegment) segment).getSource() == fileSource) {
                    preloadDocumentSection(document, documentPosition, segmentLength);
                }

                documentPosition += segmentLength;
            }
        }
    }

    /**
     * Mapping of segments to data source.
     *
     * Segments are suppose to be kept ordered by start position and length with
     * max position computed.
     */
    private class DataSegmentsMap {

        @Nonnull
        private final DefaultDoublyLinkedList<SegmentRecord> records = new DefaultDoublyLinkedList<>();
        @Nullable
        private SegmentRecord pointerRecord = null;

        public DataSegmentsMap() {
        }

        private void add(@Nonnull DataSegment segment) {
            focusSegment(segment.getStartPosition(), segment.getLength());
            SegmentRecord record = new SegmentRecord();
            record.dataSegment = segment;
            addRecord(record);
        }

        /**
         * Adds record after pointer record.
         *
         * @param record record
         */
        private void addRecord(@Nonnull SegmentRecord record) {
            long startPosition = record.dataSegment.getStartPosition();
            long length = record.getLength();
            long maxPosition = startPosition + length;
            if (pointerRecord == null) {
                record.maxPosition = maxPosition;
                records.add(0, record);
                SegmentRecord nextRecord = record.next;
                while (nextRecord != null && nextRecord.maxPosition < maxPosition) {
                    nextRecord.maxPosition = maxPosition;
                    nextRecord = records.nextTo(nextRecord);
                }
            } else {
                if (pointerRecord.maxPosition > maxPosition) {
                    maxPosition = pointerRecord.maxPosition;
                } else {
                    SegmentRecord nextRecord = pointerRecord.next;
                    while (nextRecord != null && maxPosition > nextRecord.maxPosition) {
                        nextRecord.maxPosition = maxPosition;
                        nextRecord = records.nextTo(nextRecord);
                    }
                }
                record.maxPosition = maxPosition;
                records.addAfter(pointerRecord, record);
            }
        }

        private void remove(@Nonnull DataSegment segment) {
            SegmentRecord record = findRecord(segment);

            if (record.dataSegment == segment) {
                removeRecord(record);
            } else {
                throw new IllegalStateException("Segment requested for removal was not found");
            }
        }

        private void removeRecord(@Nonnull SegmentRecord record) {
            SegmentRecord prevRecord = records.prevTo(record);
            SegmentRecord nextRecord = records.nextTo(record);
            long recordEndPosition = record.getStartPosition() + record.getLength();
            records.remove(record);
            pointerRecord = prevRecord;
            long prevMaxPosition = 0;
            if (prevRecord != null) {
                prevMaxPosition = prevRecord.maxPosition;
            }

            // Update maxPosition cached values
            if (nextRecord != null && prevMaxPosition < recordEndPosition) {
                long maxPosition = nextRecord.getStartPosition() + nextRecord.getLength();
                if (prevMaxPosition > maxPosition) {
                    maxPosition = prevMaxPosition;
                }

                while (nextRecord != null && maxPosition < nextRecord.maxPosition) {
                    nextRecord.maxPosition = maxPosition;
                    nextRecord = records.nextTo(nextRecord);

                    if (nextRecord != null) {
                        long nextMaxPosition = nextRecord.getStartPosition() + nextRecord.getLength();
                        if (nextMaxPosition > maxPosition) {
                            maxPosition = nextMaxPosition;
                        }
                    }
                }
            }
        }

        private boolean hasMoreSegments() {
            return records.first() != null && records.first() != records.last();
        }

        private void updateSegment(@Nonnull DataSegment segment, long position, long length) {
            // TODO optimalization - update only affected records without removing current record
            SegmentRecord record = findRecord(segment);
            if (record.dataSegment == segment) {
                removeRecord(record);
                if (segment instanceof MemorySegment) {
                    ((MemorySegment) segment).setStartPosition(position);
                    ((MemorySegment) segment).setLength(length);
                } else {
                    ((FileSegment) segment).setStartPosition(position);
                    ((FileSegment) segment).setLength(length);
                }
                focusSegment(segment.getStartPosition(), segment.getLength());
                addRecord(record);
            } else {
                throw new IllegalStateException("Segment requested for update was not found");
            }
        }

        private void updateSegmentLength(@Nonnull DataSegment segment, long length) {
            // TODO optimalization - update only affected records without removing current record
            SegmentRecord record = findRecord(segment);
            if (record.dataSegment == segment) {
                removeRecord(record);
                if (segment instanceof MemorySegment) {
                    ((MemorySegment) segment).setLength(length);
                } else {
                    ((FileSegment) segment).setLength(length);
                }
                focusSegment(segment.getStartPosition(), segment.getLength());
                addRecord(record);
            } else {
                throw new IllegalStateException("Segment requested for update was not found");
            }
        }

        @Nullable
        private SegmentRecord findRecord(@Nonnull DataSegment segment) {
            focusSegment(segment.getStartPosition(), segment.getLength());
            SegmentRecord record = pointerRecord;
            if (record == null) {
                return null;
            }

            while (record != null && record.dataSegment != segment
                    && record.getStartPosition() == segment.getStartPosition()
                    && record.getLength() == segment.getLength()) {
                record = records.prevTo(record);
            }

            return record;
        }

        /**
         * Aligns focus pointer on last segment at given start position and
         * length or last segment before given position or null if there is no
         * such segment.
         *
         * @param startPosition start position
         * @param length length
         */
        private void focusSegment(long startPosition, long length) {
            if (pointerRecord == null) {
                pointerRecord = records.first();
            }

            if (pointerRecord == null) {
                return;
            }

            if (pointerRecord.getStartPosition() < startPosition
                    || (pointerRecord.getStartPosition() == startPosition && pointerRecord.getLength() <= length)) {
                // Forward direction traversal
                SegmentRecord record;
                do {
                    record = records.nextTo(pointerRecord);
                    if (record != null) {
                        if (record.getStartPosition() < startPosition
                                || (record.getStartPosition() == startPosition && record.getLength() <= length)) {
                            pointerRecord = record;
                        } else {
                            break;
                        }
                    }
                } while (record != null);
            } else {
                // Backward direction traversal
                while (pointerRecord.getStartPosition() > startPosition
                        || (pointerRecord.getStartPosition() == startPosition && pointerRecord.getLength() > length)) {
                    pointerRecord = records.prevTo(pointerRecord);
                    if (pointerRecord == null) {
                        break;
                    }
                }
            }
        }

        /**
         * Returns first segment record which overlays given area.
         *
         * @param startPosition start position
         * @param length length
         * @return segment record or null
         */
        @Nullable
        private SegmentRecord focusFirstOverlay(long startPosition, long length) {
            if (pointerRecord == null) {
                pointerRecord = records.first();
            }

            if (pointerRecord == null) {
                return null;
            }

            long endPosition = startPosition + length;
            if (pointerRecord.maxPosition < startPosition) {
                // Forward direction traversal
                while (pointerRecord != null) {
                    SegmentRecord nextRecord = records.nextTo(pointerRecord);
                    if (nextRecord != null && nextRecord.maxPosition < startPosition) {
                        pointerRecord = nextRecord;
                    } else {
                        if (pointerRecord.getStartPosition() < endPosition) {
                            return pointerRecord;
                        }

                        break;
                    }
                }
            } else {
                // Backward direction traversal
                while (pointerRecord != null) {
                    SegmentRecord nextRecord = records.prevTo(pointerRecord);
                    if (nextRecord != null && nextRecord.maxPosition >= startPosition) {
                        pointerRecord = nextRecord;
                    } else {
                        if (pointerRecord.getStartPosition() < endPosition) {
                            return pointerRecord;
                        }

                        break;
                    }
                }
            }

            return null;
        }
    }

    /**
     * Internal structure for segment and cached maximum position.
     */
    private static class SegmentRecord implements DoublyLinkedItem<SegmentRecord> {

        @Nullable
        SegmentRecord prev = null;
        @Nullable
        SegmentRecord next = null;

        @Nonnull
        DataSegment dataSegment;
        long maxPosition;

        @Nullable
        @Override
        public SegmentRecord getNext() {
            return next;
        }

        public long getStartPosition() {
            return dataSegment.getStartPosition();
        }

        public long getLength() {
            return dataSegment.getLength();
        }

        @Override
        public void setNext(@Nullable SegmentRecord next) {
            this.next = next;
        }

        @Nullable
        @Override
        public SegmentRecord getPrev() {
            return prev;
        }

        @Override
        public void setPrev(@Nullable SegmentRecord prev) {
            this.prev = prev;
        }
    }

    private static final class DataArea {

        long startFrom;
        long length;

        public DataArea(long startFrom, long length) {
            this.startFrom = startFrom;
            this.length = length;
        }
    }
}
