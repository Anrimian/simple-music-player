package com.github.anrimian.musicplayer.domain.interactors.sync.models;

public abstract class RunningSyncState {

    public static class Idle extends RunningSyncState {

    }

    public abstract static class Running extends RunningSyncState {
        private RemoteRepositoryType repositoryType;

        public Running(RemoteRepositoryType repositoryType) {
            this.repositoryType = repositoryType;
        }

        public RemoteRepositoryType getRepositoryType() {
            return repositoryType;
        }
    }

    public static class GetRemoteMetadata extends Running {

        public GetRemoteMetadata(RemoteRepositoryType repositoryType) {
            super(repositoryType);
        }
    }

    public static class GetRemoteFileTable extends Running {

        public GetRemoteFileTable(RemoteRepositoryType repositoryType) {
            super(repositoryType);
        }
    }

    public static class CollectLocalFileInfo extends RunningSyncState {
    }

    public static class CalculateChanges extends RunningSyncState {
    }

    public static class SaveRemoteFileMetadata extends Running {

        public SaveRemoteFileMetadata(RemoteRepositoryType repositoryType) {
            super(repositoryType);
        }
    }

    public static class SaveLocalFileTable extends RunningSyncState {
    }

    public static class ScheduleFileTasks extends RunningSyncState {
    }

    public static class Error extends RunningSyncState {
        private Throwable throwable;

        public Error(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }
}
