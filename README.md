# Gitlab Time Tracker

![Main Page](TimeTracker.png)

The Gitlab Time Tracker is a GUI tool for recording the amount of time you spend on GitLab issues
using the built-in time spend functionality (i.e. `/spend 30m` on an issue). You just click "start" on
an issue for a project you're a part of and the tracker will begin recording the amount of time you spend.
When you click "stop", it immediately records the time spent on the issue in GitLab.

Pull requests are welcome! This project uses Coroutines and TornadoFX.

## Getting started

First things first, head on over to the [releases page](https://github.com/emanguy/GitlabTimeTracker/releases) for this repo and download the latest version of the
application.

First, go to Gitlab. This should work both for the public version of GitLab and for private versions.

1. Copy down the base URL for your Gitlab instance. You'll need this during login. For example: `https://www.gitlab.com`.
2. Go to your user settings in GitLab, then "Personal Access Tokens".
3. Create a new personal access token that does not expire with the `api` scope. Copy down the value of your token. Don't worry, Gitlab Time Tracker will remember this after first login.
4. Double-click GitlabTimeTracker-X.X.X.jar to launch the application, and enter your URL and API token to log in.

## Building and running locally

You can build and run the app off your local machine using the included `gradlew` script for your machine. **Java 1.8 JDK is required.**

To run:

    ./gradlew run
    
To build an executable jar:

    ./gradlew shadowJar