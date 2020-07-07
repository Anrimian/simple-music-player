package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.interactors.TestBusinessDataProvider.fakeCompositionSource;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.GAIN;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS;
import static com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent.LOSS_SHORTLY;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PAUSE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.musicplayer.domain.models.player.error.ErrorType.IGNORED;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlayerInteractorTest {

    private MusicPlayerController musicPlayerController = mock(MusicPlayerController.class);
    private SettingsRepository settingsRepository = mock(SettingsRepository.class);
    private SystemMusicController systemMusicController = mock(SystemMusicController.class);
    private SystemServiceController systemServiceController = mock(SystemServiceController.class);

    private PlayerInteractor musicPlayerInteractor;

    private PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private PublishSubject<AudioFocusEvent> audioFocusSubject = PublishSubject.create();
    private PublishSubject<Object> noisyAudioSubject = PublishSubject.create();
    private PublishSubject<Integer> volumeSubject = PublishSubject.create();

    private TestObserver<PlayerState> playerStateSubscriber;

    private InOrder inOrder = Mockito.inOrder(
            musicPlayerController,
            systemServiceController);

    @Before
    public void setUp() {
        when(musicPlayerController.getEventsObservable()).thenReturn(playerEventSubject);

        when(systemMusicController.requestAudioFocus()).thenReturn(audioFocusSubject);
        when(systemMusicController.getAudioBecomingNoisyObservable()).thenReturn(noisyAudioSubject);
        when(systemMusicController.getVolumeObservable()).thenReturn(volumeSubject);

        when(settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled()).thenReturn(true);

        musicPlayerInteractor = new PlayerInteractor(musicPlayerController,
                settingsRepository,
                systemMusicController,
                systemServiceController);

        playerStateSubscriber = musicPlayerInteractor.getPlayerStateObservable()
                .test();
    }

    @Test
    public void prepareToPlayTest() {
        musicPlayerInteractor.prepareToPlay(fakeCompositionSource(0));

        verify(musicPlayerController).prepareToPlay(eq(fakeCompositionSource(0)));
        verify(musicPlayerController, never()).resume();
        playerStateSubscriber.assertValues(IDLE);
    }

    @Test
    public void pauseTest() {
        musicPlayerInteractor.play();
        musicPlayerInteractor.pause();

        verify(musicPlayerController).resume();
        verify(musicPlayerController).pause();
        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onCompositionIgnoredErrorTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.startPlaying(composition);

        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition));

        playerEventSubject.onNext(new PreparedEvent(composition));
        playerEventSubject.onNext(new ErrorEvent(IGNORED, composition));

        inOrder.verify(musicPlayerController).pause();

        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).prepareToPlay(eq(composition));

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE, PLAY);
    }

    @Test
    public void onAudioFocusNotGainedTest() {
        when(systemMusicController.requestAudioFocus()).thenReturn(null);

        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.startPlaying(composition);

        verify(musicPlayerController, never()).resume();
        playerStateSubscriber.assertValues(IDLE);
    }

    @Test
    public void onAudioFocusLossAndGainTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

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
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

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

        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume();

        audioFocusSubject.onNext(LOSS_SHORTLY);

        inOrder.verify(musicPlayerController, never()).setVolume(0.5f);

        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController).setVolume(1f);

        playerStateSubscriber.assertValues(IDLE, PLAY);
    }

    @Test
    public void onAudioBecomingNoisyTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume();

        noisyAudioSubject.onNext(new Object());

        inOrder.verify(musicPlayerController).pause();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void onAudioFocusLossThenBecomingNoisyAndGainFocusTest() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

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
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume();

        noisyAudioSubject.onNext(new Object());

        inOrder.verify(musicPlayerController).pause();

        audioFocusSubject.onNext(LOSS);
        audioFocusSubject.onNext(GAIN);

        inOrder.verify(musicPlayerController, never()).resume();

        playerStateSubscriber.assertValues(IDLE, PLAY, PAUSE);
    }

    @Test
    public void testVolumeSetToSilentWhilePlay() {
        CompositionSource composition = fakeCompositionSource(0);
        musicPlayerInteractor.prepareToPlay(composition);
        musicPlayerInteractor.play();

        inOrder.verify(musicPlayerController).resume();

        volumeSubject.onNext(0);

        inOrder.verify(musicPlayerController).pause();
    }

}