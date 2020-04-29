package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.interactors.TestBusinessDataProvider.currentItem;
import static com.github.anrimian.musicplayer.domain.interactors.TestBusinessDataProvider.getFakeCompositions;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.GAIN;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS_SHORTLY;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PAUSE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.STOP;
import static com.github.anrimian.musicplayer.domain.models.player.error.ErrorType.IGNORED;
import static com.github.anrimian.musicplayer.domain.models.player.error.ErrorType.NOT_FOUND;
import static com.github.anrimian.musicplayer.domain.models.player.error.ErrorType.UNKNOWN;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 17.04.2018.
 */
public class MusicPlayerInteractorTest {

    private MusicPlayerController musicPlayerController = mock(MusicPlayerController.class);
    private SettingsRepository settingsRepository = mock(SettingsRepository.class);
    private PlayQueueRepository playQueueRepository = mock(PlayQueueRepository.class);
    private LibraryRepository musicProviderRepository = mock(LibraryRepository.class);
    private SystemMusicController systemMusicController = mock(SystemMusicController.class);
    private SystemServiceController systemServiceController = mock(SystemServiceController.class);
    private UiStateRepository uiStateRepository = mock(UiStateRepository.class);
    private Analytics analytics = mock(Analytics.class);

    private MusicPlayerInteractor musicPlayerInteractor;

    private PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private BehaviorSubject<PlayQueueEvent> currentCompositionSubject = BehaviorSubject.createDefault(currentItem(0));
    private PublishSubject<AudioFocusEvent> audioFocusSubject = PublishSubject.create();
    private PublishSubject<Object> noisyAudioSubject = PublishSubject.create();
    private PublishSubject<Integer> volumeSubject = PublishSubject.create();

    private TestObserver<PlayerState> playerStateSubscriber;

    private InOrder inOrder = Mockito.inOrder(playQueueRepository,
            musicPlayerController,
            musicProviderRepository,
            systemServiceController);

    @Before
    public void setUp() {
        when(playQueueRepository.setPlayQueue(any())).thenReturn(Completable.complete());
        when(playQueueRepository.setPlayQueue(any(), anyInt())).thenReturn(Completable.complete());
        when(playQueueRepository.getCurrentQueueItemObservable())
                .thenReturn(currentCompositionSubject);
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(1));

        when(musicPlayerController.getEventsObservable()).thenReturn(playerEventSubject);

        when(musicProviderRepository.writeErrorAboutComposition(any(), any()))
                .thenReturn(Completable.complete());
        when(musicProviderRepository.deleteComposition(any())).thenReturn(Completable.complete());

        when(systemMusicController.requestAudioFocus()).thenReturn(audioFocusSubject);
        when(systemMusicController.getAudioBecomingNoisyObservable()).thenReturn(noisyAudioSubject);
        when(systemMusicController.getVolumeObservable()).thenReturn(volumeSubject);

        when(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled()).thenReturn(true);

        musicPlayerInteractor = new MusicPlayerInteractor(musicPlayerController,
                settingsRepository,
                systemMusicController,
                systemServiceController,
                playQueueRepository,
                musicProviderRepository,
                uiStateRepository,
                analytics);

        playerStateSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();
    }

    @Test
    public void startPlayingTest() {
        musicPlayerInteractor.startPlaying(getFakeCompositions(), 0);

        verify(playQueueRepository).setPlayQueue(getFakeCompositions(), 0);
        verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        verify(musicPlayerController, never()).resume();
        playerStateSubscriber.assertValues(IDLE);
    }

    @Test
    public void playWithoutPreparingTest() {
        musicPlayerInteractor.play();

        verify(musicPlayerController).prepareToPlay(any(), anyLong());
        verify(musicPlayerController).resume();
        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onPlayFinishedTest() {
        musicPlayerInteractor.play();

        Composition composition = getFakeCompositions().get(0);
        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition), anyLong());
        inOrder.verify(musicPlayerController).resume();

        playerEventSubject.onNext(new FinishedEvent(composition));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        Composition nextComposition = getFakeCompositions().get(1);
        inOrder.verify(musicPlayerController).prepareToPlay(eq(nextComposition), anyLong());

        playerEventSubject.onNext(new PreparedEvent(nextComposition));

        inOrder.verify(musicPlayerController).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onPlayToEndTest() {
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(0));
        
        musicPlayerInteractor.play();

        Composition composition = getFakeCompositions().get(0);
        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition), anyLong());
        inOrder.verify(musicPlayerController).resume();

        playerEventSubject.onNext(new FinishedEvent(composition));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        inOrder.verify(musicPlayerController, never()).resume();
        inOrder.verify(musicPlayerController).stop();

        playerStateSubscriber.assertValues(IDLE, PLAY, STOP);
    }

    @Test
    public void onPlayToEndWithRepeatPlayListModeTest() {
        when(playQueueRepository.skipToNext()).thenReturn(Single.just(0));
        when(settingsRepository.getRepeatMode()).thenReturn(RepeatMode.REPEAT_PLAY_LIST);

        musicPlayerInteractor.play();

        Composition composition = getFakeCompositions().get(0);
        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition), anyLong());
        inOrder.verify(musicPlayerController).resume();

        playerEventSubject.onNext(new FinishedEvent(composition));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));
        Composition nextComposition = getFakeCompositions().get(1);
        inOrder.verify(musicPlayerController).prepareToPlay(eq(nextComposition), anyLong());

        playerEventSubject.onNext(new PreparedEvent(nextComposition));
        inOrder.verify(musicPlayerController).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void pauseTest() {
        musicPlayerInteractor.play();
        musicPlayerInteractor.pause();

        verify(musicPlayerController).resume();
        verify(musicPlayerController).prepareToPlay(any(), anyLong());
        verify(musicPlayerController).pause();
        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onCompositionPlayFinishedInStopState() {
        musicPlayerInteractor.play();
        musicPlayerInteractor.stop();

        inOrder.verify(musicPlayerController).prepareToPlay(any(), anyLong());
        inOrder.verify(musicPlayerController).resume();
        inOrder.verify(musicPlayerController).stop();

        Composition composition = getFakeCompositions().get(0);
        playerEventSubject.onNext(new FinishedEvent(composition));

        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));

        Composition nextComposition = getFakeCompositions().get(1);
        inOrder.verify(musicPlayerController).prepareToPlay(eq(nextComposition), anyLong());
        inOrder.verify(musicPlayerController, never()).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, STOP);
    }

    @Test
    public void onCompositionUnknownErrorTest() {
        musicPlayerInteractor.play();

        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(false));

        Composition composition = getFakeCompositions().get(0);
        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition), anyLong());

        playerEventSubject.onNext(new ErrorEvent(UNKNOWN, composition));

        inOrder.verify(musicProviderRepository)
                .writeErrorAboutComposition(CorruptionType.UNKNOWN, getFakeCompositions().get(0));
        inOrder.verify(playQueueRepository).skipToNext();

        currentCompositionSubject.onNext(currentItem(1));

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(1)), anyLong());

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onCompositionIgnoredErrorTest() {
        musicPlayerInteractor.play();

        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(false));

        Composition composition = getFakeCompositions().get(0);
        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition), anyLong());

        playerEventSubject.onNext(new ErrorEvent(IGNORED, composition));

        inOrder.verify(musicPlayerController).pause();

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition), anyLong());

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE, PLAY);
    }

    @Test
    public void onCompositionUnknownErrorInEndTest() {
        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(false));

        musicPlayerInteractor.play();

        Composition composition = getFakeCompositions().get(0);
        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition), anyLong());
        inOrder.verify(musicPlayerController).resume();

        playerEventSubject.onNext(new ErrorEvent(UNKNOWN, composition));

        inOrder.verify(musicProviderRepository)
                .writeErrorAboutComposition(CorruptionType.UNKNOWN, getFakeCompositions().get(0));
        inOrder.verify(playQueueRepository).skipToNext();

        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(true));

        currentCompositionSubject.onNext(currentItem(1));
        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(1)), anyLong());

        playerEventSubject.onNext(new ErrorEvent(UNKNOWN, composition));
        inOrder.verify(musicPlayerController, never()).resume();
        playerStateSubscriber.assertValues(IDLE, PLAY, STOP);
    }

    @Test
    public void onCompositionDeletedErrorTest() {
        when(playQueueRepository.isCurrentCompositionAtEndOfQueue()).thenReturn(Single.just(false));

        musicPlayerInteractor.play();

        Composition composition = getFakeCompositions().get(0);
        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition), anyLong());
        inOrder.verify(musicPlayerController).resume();

        playerEventSubject.onNext(new ErrorEvent(NOT_FOUND, composition));

        inOrder.verify(musicProviderRepository)
                .writeErrorAboutComposition(CorruptionType.NOT_FOUND, getFakeCompositions().get(0));
        inOrder.verify(playQueueRepository).skipToNext();

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onAudioFocusNotGainedTest() {
        when(systemMusicController.requestAudioFocus()).thenReturn(null);

        musicPlayerInteractor.play();

        verify(musicPlayerController, never()).resume();
        playerStateSubscriber.assertValues(IDLE);
    }

    @Test
    public void onAudioFocusLossAndGainTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        audioFocusSubject.onNext(LOSS);

        inOrder.verify(musicPlayerController).pause();

        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController).resume();
        inOrder.verify(systemServiceController).startMusicService();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE, PLAY);
    }

    @Test
    public void onAudioFocusLossShortlyAndGainTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        audioFocusSubject.onNext(LOSS_SHORTLY);

        inOrder.verify(musicPlayerController).setVolume(0.5f);

        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController).setVolume(1f);

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onAudioFocusLossShortlyAndGainWithoutDecreaseVolumeTest() {
        when(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled()).thenReturn(false);

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        audioFocusSubject.onNext(LOSS_SHORTLY);

        inOrder.verify(musicPlayerController, never()).setVolume(0.5f);

        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController).setVolume(1f);

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onAudioBecomingNoisyTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        noisyAudioSubject.onNext(new Object());

        inOrder.verify(musicPlayerController).pause();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onAudioFocusLossThenBecomingNoisyAndGainFocusTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        audioFocusSubject.onNext(LOSS);

        inOrder.verify(musicPlayerController).pause();

        noisyAudioSubject.onNext(new Object());
        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController, never()).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onAudioBecomingNoisyThenLossFocusAndGainFocusTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        noisyAudioSubject.onNext(new Object());

        inOrder.verify(musicPlayerController).pause();

        audioFocusSubject.onNext(LOSS);
        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController, never()).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onCurrentCompositionDeletedChangeTest() {
        musicPlayerInteractor.play();

        currentCompositionSubject.onNext(new PlayQueueEvent(null));

        verify(musicPlayerController).stop();
        playerStateSubscriber.assertValues(IDLE, PLAY, STOP);
    }

    @Test
    public void skipToPreviousTest() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        when(settingsRepository.getSkipConstraintMillis()).thenReturn(15);
        when(musicPlayerController.getTrackPosition()).thenReturn(10L);

        musicPlayerInteractor.skipToPrevious();

        inOrder.verify(playQueueRepository).skipToPrevious();

        when(settingsRepository.getSkipConstraintMillis()).thenReturn(15);
        when(musicPlayerController.getTrackPosition()).thenReturn(30L);

        musicPlayerInteractor.skipToPrevious();

        inOrder.verify(musicPlayerController).seekTo(0);
    }

    @Test
    public void testVolumeSetToSilentWhilePlay() {
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(getFakeCompositions().get(0)), anyLong());
        inOrder.verify(musicPlayerController).resume();

        volumeSubject.onNext(0);

        inOrder.verify(musicPlayerController).pause();
    }
}