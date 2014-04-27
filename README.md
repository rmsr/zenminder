Zen Minder
==========

Meditation assistant Android app. GPLv3 license. Donation-supported.

https://github.com/rmsr/zenminder

Architecture
------------

Uses the android-support-v7-appcompat library, for action bar and fragments support.

The timer is implemented as a fragment, which preserves state across activity interruptions. State
is not kept across app restarts.

A finite state machine with a transition table is the core logic of the timer fragment, to keep all
flow logic centralized.

Features
========

Current
-------

+ Start, pause meditation session
+ Sound a gong when done

Upcoming
--------

See the github issues list for planned features.

Blue Sky
--------

More ambitious future plans:

+ Beeminder / google docs integration
+ Multiple presets for different meditation lengths or types
+ Periodic bamboo fountain sound as meditation focus or breath pacing
+ Scheduled meditation alarms
+ Randomized mindfulness reminder alarms
