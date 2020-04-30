package com.github.anrimian.musicplayer.domain.interactors.sync.models;

public abstract class RunningSyncState {

    public static class Idle extends RunningSyncState {

    }

    public static class GetRemoteMetadata extends RunningSyncState {
        private RemoteRepositoryType repositoryType;

        public GetRemoteMetadata(RemoteRepositoryType repositoryType) {
            this.repositoryType = repositoryType;
        }

        public RemoteRepositoryType getRepositoryType() {
            return repositoryType;
        }
    }

    public static class GetRemoteFileTable extends RunningSyncState {
        private RemoteRepositoryType repositoryType;

        public GetRemoteFileTable(RemoteRepositoryType repositoryType) {
            this.repositoryType = repositoryType;
        }

        public RemoteRepositoryType getRepositoryType() {
            return repositoryType;
        }
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
