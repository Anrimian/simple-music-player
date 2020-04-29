package com.github.anrimian.musicplayer.data.controllers.music.players;

import utils.TestDataProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompositeMediaPlayerTest {

    private final AppMediaPlayer player1 = mock(AppMediaPlayer.class);
    private final AppMediaPlayer player2 = mock(AppMediaPlayer.class);

    private CompositeMediaPlayer compositeMediaPlayer;

    private InOrder inOrder = Mockito.inOrder(player1, player2);

    private final PublishSubject<PlayerEvent> player1EventSubject = PublishSubject.create();
    private final PublishSubject<PlayerEvent> player2EventSubject = PublishSubject.create();

    private final PublishSubject<Long> player1PositionSubject = PublishSubject.create();
    private final PublishSubject<Long> player2PositionSubject = PublishSubject.create();

    @Before
    public void setUp() {
        when(player1.getTrackPositionObservable()).thenReturn(player1PositionSubject);
        when(player1.getEventsObservable()).thenReturn(player1EventSubject);


        when(player2.getTrackPositionObservable()).thenReturn(player2PositionSubject);
        when(player2.getEventsObservable()).thenReturn(player2EventSubject);

        compositeMediaPlayer = new CompositeMediaPlayer(() -> player1, () -> player2);
    }

    @Test
    public void testPlayersSwitch() {
        Composition composition = TestDataProvider.fakeComposition(0);

        compositeMediaPlayer.prepareToPlay(composition, 0L);
        inOrder.verify(player1).prepareToPlay(eq(composition), eq(0L));

        player1EventSubject.onNext(new ErrorEvent(ErrorType.UNSUPPORTED, composition));
        inOrder.verify(player1).release();
        inOrder.verify(player2).prepareToPlay(eq(composition), eq(0L));
    }

    @Test
    public void testAllPlayersNotWorking() {
        Composition composition = TestDataProvider.fakeComposition(0);

        TestObserver<PlayerEvent> eventsObserver = compositeMediaPlayer.getEventsObservable().test();

        compositeMediaPlayer.prepareToPlay(composition, 0L);
        inOrder.verify(player1).prepareToPlay(eq(composition), eq(0L));

        player1EventSubject.onNext(new ErrorEvent(ErrorType.UNSUPPORTED, composition));
        inOrder.verify(player1).release();
        inOrder.verify(player2).prepareToPlay(eq(composition), eq(0L));

        player2EventSubject.onNext(new ErrorEvent(ErrorType.UNSUPPORTED, composition));

        eventsObserver.assertValue(new ErrorEvent(ErrorType.UNSUPPORTED, composition));

        Composition composition2 = TestDataProvider.fakeComposition(2);

        compositeMediaPlayer.prepareToPlay(composition2, 0L);
        inOrder.verify(player2).release();
        inOrder.verify(player1).prepareToPlay(eq(composition2), eq(0L));
    }

    @Test
    public void testPlayersSwitchWithPosition() {
        Composition composition = TestDataProvider.fakeComposition(0);

        compositeMediaPlayer.prepareToPlay(composition, 0L);
        inOrder.verify(player1).prepareToPlay(eq(composition), eq(0L));

        player1PositionSubject.onNext(100L);

        player1EventSubject.onNext(new ErrorEvent(ErrorType.UNSUPPORTED, composition));
        inOrder.verify(player1).release();
        inOrder.verify(player2).prepareToPlay(eq(composition), eq(100L));
    }
}