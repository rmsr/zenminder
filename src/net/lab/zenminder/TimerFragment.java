package net.lab.zenminder;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.util.HashMap;

public class TimerFragment extends Fragment implements OnClickListener
{
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
    private enum Event { BUTTON_START, TICK, PAUSE, RESUME, FINISH };

    // All valid transitions appear here. Other transitions will throw an exception.
    private void buildTransitionTable() {
        // From READY state
        addTransition( State.READY, Event.BUTTON_START, State.RUNNING,
                new Action() { void act() {
                        mStartButton.setText(getString(R.string.timer_pause));
                        startTimer();
                    }});

        addTransition( State.READY, Event.PAUSE );

        addTransition( State.READY, Event.RESUME, State.READY,
                new Action() { void act() {
                        mStartButton.setText(getString(R.string.timer_start));
                        updateCounterView();
                    }});

        // From RUNNING state
        addTransition( State.RUNNING, Event.BUTTON_START, State.STOPPED,
                new Action() { void act() {
                        stopTimer();
                        mCounterView.startAnimation(mBlinkAnimation);
                        mStartButton.setText(getString(R.string.timer_resume));
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
                        mStartButton.setText(getString(R.string.timer_reset));
                        mCounterView.setText(getString(R.string.timer_done));
                        recordSession();
                        playSound();
                    }});

        // From STOPPED state
        addTransition( State.STOPPED, Event.BUTTON_START, State.RUNNING,
                new Action() { void act() {
                        mStartButton.setText(getString(R.string.timer_pause));
                        mCounterView.clearAnimation();
                        startTimer();
                    }});

        addTransition( State.STOPPED, Event.PAUSE );

        addTransition( State.STOPPED, Event.RESUME, State.STOPPED,
                new Action() { void act() {
                        mStartButton.setText(getString(R.string.timer_resume));
                        updateCounterView();
                        mCounterView.startAnimation(mBlinkAnimation);
                    }});

        // From PAUSED state
        addTransition( State.PAUSED, Event.RESUME, State.RUNNING,
                new Action() { void act() {
                        mStartButton.setText(getString(R.string.timer_pause));
                        updateCounterView();
                        startTimer();
                    }});

        // From FINISHED state
        addTransition( State.FINISHED, Event.BUTTON_START, State.READY,
                new Action() { void act() {
                        resetTimeRemaining();
                        mStartButton.setText(getString(R.string.timer_start));
                        updateCounterView();
                    }});

        addTransition( State.FINISHED, Event.PAUSE );

        addTransition( State.FINISHED, Event.RESUME, State.FINISHED,
                new Action() { void act() {
                        mStartButton.setText(getString(R.string.timer_start));
                        mCounterView.setText(getString(R.string.timer_done));
                    }});
    }

    private static final int DEFAULT_TIME = 20 * 60 * 1000;
    private static final int TICK_INTERVAL = 250;

    private TextView mCounterView;
    private Button mStartButton;
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

        mStartButton = (Button) view.findViewById(R.id.timer_button_start);
        mStartButton.setOnClickListener(this);

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
            case R.id.timer_button_start:
                handleEvent(Event.BUTTON_START);
                break;
            default:
                Log.d(TAG, "unknown click view id " + view.getId());
                break;
        }
    }

    void resetTimeRemaining() {
        mTimeRemaining = DEFAULT_TIME; // TODO retrieve this from settings
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
        // TODO store the results somewhere
    }

    private void updateCounterView() {
        long remaining = ( mTimeRemaining + 999 ) / 1000; // round seconds up
        long minutes = remaining / 60;
        long seconds = remaining % 60;
        mCounterView.setText(String.format("%02d:%02d", minutes, seconds));
    }

    void playSound() {
        MediaPlayer player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        player.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) { mp.release(); }});
        try {
            player.setDataSource(getActivity(),
                    Uri.parse("android.resource://net.lab.zenminder/" + R.raw.warm_gong));
            player.prepare();
            player.start();
        } catch (IOException e) {
            throw new RuntimeException(e); // declaring exceptions is stupid
        }
    }

}
