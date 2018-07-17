package com.github.anrimian.simplemusicplayer.data.repositories.music.folders;

import android.text.format.DateUtils;

import com.github.anrimian.simplemusicplayer.data.utils.folders.NodeData;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.Date;
import java.util.List;

public class FolderNode extends NodeData {

    private final String fullPath;
    private int compositionsCount;

    private Date newestCreateDate;//TODO process deletion, modify changes
    private Date latestCreateDate;

    FolderNode(String fullPath) {
        this.fullPath = fullPath;
    }

    @Override
    public boolean onNodesAdded(List<NodeData> nodes) {
        boolean updated = false;
        for (NodeData nodeData: nodes) {
            if (nodeData instanceof CompositionNode) {
                compositionsCount++;
                recalculateDate(nodeData);
                updated = true;
            }
        }
        return updated;
    }

    @Override
    public boolean onNodesRemoved(List<NodeData> nodes, List<NodeData> allNodes) {
        boolean updated = false;
        boolean dateChanged = false;

        for (NodeData nodeData: nodes) {
            if (nodeData instanceof CompositionNode) {
                compositionsCount--;

                Composition composition = ((CompositionNode) nodeData).getComposition();
                Date dateAdded = composition.getDateAdded();
                if (newestCreateDate.equals(dateAdded)) {
                    dateChanged = true;
                    newestCreateDate = null;
                }
                if (latestCreateDate.equals(dateAdded)) {
                    dateChanged = true;
                    latestCreateDate = null;
                }

                updated = true;
            }
        }
        if (dateChanged) {
            for (NodeData nodeData: allNodes) {
                if (nodeData instanceof CompositionNode) {
                    recalculateDate(nodeData);
                }
            }
        }

        return updated;
    }

    public String getFullPath() {
        return fullPath;
    }

    public int getCompositionsCount() {
        return compositionsCount;
    }

    public Date getNewestCreateDate() {
        return newestCreateDate;
    }

    public Date getLatestCreateDate() {
        return latestCreateDate;
    }

    @Override
    public String toString() {
        return "FolderNode{" +
                "fullPath='" + fullPath + '\'' +
                ", compositionsCount=" + compositionsCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderNode that = (FolderNode) o;

        return fullPath != null ? fullPath.equals(that.fullPath) : that.fullPath == null;
    }

    @Override
    public int hashCode() {
        return fullPath != null ? fullPath.hashCode() : 0;
    }

    private void recalculateDate(NodeData nodeData) {
        if (nodeData instanceof CompositionNode) {
            Composition composition = ((CompositionNode) nodeData).getComposition();
            Date dateAdded = composition.getDateAdded();
            if (newestCreateDate == null || dateAdded.compareTo(newestCreateDate) > 0) {
                newestCreateDate = dateAdded;
            }
            if (latestCreateDate == null || dateAdded.compareTo(latestCreateDate) < 0) {
                latestCreateDate = dateAdded;
            }
        }
    }
}