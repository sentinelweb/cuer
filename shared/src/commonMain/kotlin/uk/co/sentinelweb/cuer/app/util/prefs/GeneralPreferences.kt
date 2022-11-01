package uk.co.sentinelweb.cuer.app.util.prefs

enum class GeneralPreferences constructor(
    override val fname: String
) : Field {
    SKIP_FWD_TIME("skipFwdTime"),
    SKIP_BACK_TIME("skipBackTime"),
    LIVE_DURATION("liveDuration"),
    LIVE_DURATION_TIME("liveDurationTime"),
    LIVE_DURATION_ID("liveDurationId"),
    LOCAL_DURATION("localDuration"),
    LOCAL_DURATION_TIME("localDurationTime"),
    LOCAL_DURATION_ID("localDurationId"),
    LAST_BOTTOM_TAB("lastBottomNavTab"),
    TEST_ID("testId"),
}