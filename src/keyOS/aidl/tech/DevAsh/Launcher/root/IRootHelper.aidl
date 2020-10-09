package tech.DevAsh.Launcher.root;

interface IRootHelper {

    void goToSleep();

    void sendKeyEvent(int code, int action, int flags, long downTime, long eventTime);
}
