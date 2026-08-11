package se.radioapp.app.cast

enum class CastReceiverMode {
    CUSTOM,
    DEFAULT_MEDIA_RECEIVER;

    companion object {
        fun parse(value: String): CastReceiverMode? = when (value.trim().uppercase()) {
            "CUSTOM" -> CUSTOM
            "DEFAULT", "DEFAULT_MEDIA_RECEIVER" -> DEFAULT_MEDIA_RECEIVER
            else -> null
        }
    }
}

class CastReceiverConfiguration private constructor(
    val mode: CastReceiverMode,
    private val customReceiverApplicationId: String?,
    private val invalidModeValue: String?,
) {
    val receiverLabel: String
        get() = when (mode) {
            CastReceiverMode.CUSTOM -> "RadioApp Custom Receiver"
            CastReceiverMode.DEFAULT_MEDIA_RECEIVER -> "Google Default Media Receiver"
        }

    val isPlaybackConfigured: Boolean
        get() = invalidModeValue == null &&
            (mode == CastReceiverMode.DEFAULT_MEDIA_RECEIVER || customReceiverApplicationId != null)

    val configurationError: String?
        get() = when {
            invalidModeValue != null -> "Unknown CAST_RECEIVER_MODE: $invalidModeValue"
            mode == CastReceiverMode.CUSTOM && customReceiverApplicationId == null -> "Custom receiver not configured"
            else -> null
        }

    fun receiverApplicationId(defaultReceiverApplicationId: String): String = when (mode) {
        CastReceiverMode.DEFAULT_MEDIA_RECEIVER -> defaultReceiverApplicationId
        CastReceiverMode.CUSTOM -> customReceiverApplicationId ?: defaultReceiverApplicationId
    }

    companion object {
        const val APP_ID_PLACEHOLDER = "REPLACE_ME"

        fun from(rawMode: String, rawCustomReceiverApplicationId: String?): CastReceiverConfiguration {
            val parsedMode = CastReceiverMode.parse(rawMode)
            val appId = rawCustomReceiverApplicationId
                ?.trim()
                ?.takeUnless { it.isBlank() || it == APP_ID_PLACEHOLDER }
            return CastReceiverConfiguration(
                mode = parsedMode ?: CastReceiverMode.CUSTOM,
                customReceiverApplicationId = appId,
                invalidModeValue = rawMode.takeIf { parsedMode == null },
            )
        }
    }
}
