package se.radioapp.app.data.settings

data class RadioSettings(val defaultP4ChannelId: String = DEFAULT_P4_CHANNEL_ID) {
    companion object { const val DEFAULT_P4_CHANNEL_ID = "p4-malmo" }
}
