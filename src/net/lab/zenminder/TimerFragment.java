package net.lab.zenminder;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.util.HashMap;

public class TimerFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener
{
    static int DEFAULT_TIME = 20 * 60 * 1000;

    private static final String TAG = "ZenMinder.TimerFragment";

    // Finite state machine transition table infrastructure
    private class Action { void act() { }; }
    private class StateEvent extends Pair<State, Event> {
        StateEvent(State state, Event event) { super(state, event); };
    }
    private class Transition extends Pair<State, Action> {
        Transition(State state, Action action) { super(state, action); };
    }
    private HashMap<StateEvent, Transition> mTransitionTable = new HashMap<StateEvent, Transition>();

    private void addTransition(State from, Event event, State to, Action action) {
        addTransition(new StateEvent(from, event), new Transition(to, action));
    }

    private void addTransition(State from, Event event) {
        addTransition(new StateEvent(from, event), null);
    }

    private void addTransition(StateEvent pair, Transition transition) {
        if (mTransitionTable.containsKey(pair))
            throw new RuntimeException("duplicate transition");
        mTransitionTable.put(pair, transition);
    }

    private void handleEvent(Event event) {
        Log.d(TAG, "handleEvent " + event + " from " + mState);

        StateEvent pair = new StateEvent(mState, event);

        if (!mTransitionTable.containsKey(pair)) {
            Log.d(TAG, "unhandled transition");
            throw new RuntimeException("unhandled transition");
        }

        Transition transition = mTransitionTable.get(pair);
        if (transition != null) {
            transition.second.act();
            mState = transition.first;
        }
    }


    // States and events for the FSM
    private enum State { READY, RUNNING, STOPPED, PAUSED, FINISHED };
    private enum Event { CLICK_COUNTER, LONGCLICK_COUNTER, TICK, PAUSE, RESUME, FINISH };

    // All valid transitions appear here. Other transitions will throw an exception.
    private void buildTransitionTable() {
        // From READY state
        addTransition( State.READY, Event.CLICK_COUNTER, State.RUNNING,
                new Action() { void act() {
                        mCounterView.setLongClickable(false);
                        startTimer();
                    }});

        addTransition( State.READY, Event.LONGCLICK_COUNTER, State.READY,
                new Action() { void act() {
                        SessionLengthDialogBuilder dialog = new SessionLengthDialogBuilder(getActivity(), DEFAULT_TIME / 60 / 1000) {
                            public void onOK(int value) {
                                // TODO store this in prefs
                                if (value > 0) {
                                    DEFAULT_TIME = value * 60 * 1000;
                                } else {
                                    // testing mode
                                    DEFAULT_TIME = 5000;
                                }
                                resetTimeRemaining();
                                updateCounterView();
                            }};
                        dialog.show();
                    }});

        addTransition( State.READY, Event.PAUSE );

        addTransition( State.READY, Event.RESUME, State.READY,
                new Action() { void act() {
                        updateCounterView();
                    }});

        // From RUNNING state
        addTransition( State.RUNNING, Event.CLICK_COUNTER, State.STOPPED,
                new Action() { void act() {
                        stopTimer();
                        mCounterView.startAnimation(mBlinkAnimation);
                    }});

        addTransition( State.RUNNING, Event.TICK, State.RUNNING,
                new Action() { void act() {
                        // decrement of mTimeRemaining is handled elsewhere
                        updateCounterView();
                    }});

        addTransition( State.RUNNING, Event.PAUSE, State.PAUSED,
                new Action() { void act() {
                        stopTimer();
                    }});

        addTransition( State.RUNNING, Event.RESUME );

        addTransition( State.RUNNING, Event.FINISH, State.FINISHED,
                new Action() { void act() {
                        stopTimer();
                        mCounterView.setText(getString(R.string.timer_done));
                        recordSession();
                        playDoneSound();
                    }});

        // From STOPPED state
        addTransition( State.STOPPED, Event.CLICK_COUNTER, State.RUNNING,
                new Action() { void act() {
                        mCounterView.clearAnimation();
                        startTimer();
                    }});

        addTransition( State.STOPPED, Event.PAUSE );

        addTransition( State.STOPPED, Event.RESUME, State.STOPPED,
                new Action() { void act() {
                        updateCounterView();
                        mCounterView.startAnimation(mBlinkAnimation);
                    }});

        // From PAUSED state
        addTransition( State.PAUSED, Event.RESUME, State.RUNNING,
                new Action() { void act() {
                        updateCounterView();
                        startTimer();
                    }});

        // From FINISHED state
        addTransition( State.FINISHED, Event.CLICK_COUNTER, State.READY,
                new Action() { void act() {
                        mCounterView.setLongClickable(true);
                        resetTimeRemaining();
                        updateCounterView();
                    }});

        addTransition( State.FINISHED, Event.PAUSE );

        addTransition( State.FINISHED, Event.RESUME, State.FINISHED,
                new Action() { void act() {
                        mCounterView.setText(getString(R.string.timer_done));
                    }});
    }

    private static final int TICK_INTERVAL = 250;

    private TextView mCounterView;
    private Animation mBlinkAnimation;

    private CountDownTimer mCountdown;
    private long mTimeRemaining;
    private long mTimeTotal;
    private State mState = State.READY;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setRetainInstance(true);

        mBlinkAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.blink);

        buildTransitionTable();
        resetTimeRemaining();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.timer_fragment, container, false);

        mCounterView = (TextView) view.findViewById(R.id.timer_counter);
        mCounterView.setOnClickListener(this);
        mCounterView.setOnLongClickListener(this);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn()) handleEvent(Event.PAUSE);
    }

    @Override
    public void onStart() {
        super.onStart();
        handleEvent(Event.RESUME);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.timer_counter:
                handleEvent(Event.CLICK_COUNTER);
                break;
            default:
                throw new RuntimeException("unknown click view id " + view.getId());
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.timer_counter:
                handleEvent(Event.LONGCLICK_COUNTER);
                break;
            default:
                throw new RuntimeException("unknown long click view id " + view.getId());
        }
        return true;
    }

    void resetTimeRemaining() {
        // TODO get this from prefs
        mTimeRemaining = DEFAULT_TIME;
        mTimeTotal = mTimeRemaining;
    }

    void startTimer() {
        mCountdown = new CountDownTimer(mTimeRemaining, TICK_INTERVAL) {
            public void onTick(long remaining) {
                mTimeRemaining = remaining;
                handleEvent(Event.TICK);
            }

            public void onFinish() {
                handleEvent(Event.FINISH);
            }
        };
        mCountdown.start();
    }

    void stopTimer() {
        mCountdown.cancel();
        mCountdown = null;
    }

    void recordSession() {
        // TODO log the session in a database
    }

    private void updateCounterView() {
        long remaining = ( mTimeRemaining + 999 ) / 1000; // round seconds up
        long minutes = remaining / 60;
        String string;
        if (minutes > 0) {
            string = String.format("%d%s", minutes, getString(R.string.minutes_suffix));
        } else {
            long seconds = remaining % 60;
            string = String.format("%d%s", seconds, getString(R.string.seconds_suffix));
        }
        mCounterView.setText(string);
    }

    void playDoneSound() {
        // TODO get this from prefs
        playURI("android.resource://net.lab.zenminder/" + R.raw.warm_gong);
    }

    void playURI(String uri) {
        MediaPlayer player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) { mp.release(); }});
        try {
            player.setDataSource(getActivity(), Uri.parse(uri));
            player.prepare();
            player.start();
        } catch (IOException e) {
            throw new RuntimeException(e); // declaring exceptions is stupid
        }
    }
}
