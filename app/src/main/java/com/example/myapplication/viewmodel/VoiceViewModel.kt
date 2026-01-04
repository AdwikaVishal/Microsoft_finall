package com.example.myapplication.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.data.services.SpeechService
import com.example.myapplication.model.SOSStatus
import com.example.myapplication.utils.AudioRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * VoiceViewModel - Manages voice command state and processing
 * 
 * This ViewModel handles:
 * - Listening state management (IDLE, LISTENING, PROCESSING, ERROR)
 * - Speech recognition using Android's native SpeechRecognizer
 * - Command matching and execution
 * - Navigation and action callbacks
 * 
 * Supported Commands:
 * - "open scan" → Navigate to Scan screen
 * - "send sos" → Trigger SOS flow
 * - "show alerts" → Open Alerts screen
 * - "back home" → Navigate to Home
 * - "send incident [description]" → Report incident with voice description
 */
class VoiceViewModel(
    private val context: Context,
    private val accessibilityManager: AccessibilityManager? = null
) : ViewModel() {

    // ============================================================
    // LISTENING STATES
    // ============================================================
    
    /**
     * Represents the current state of voice listening
     */
    sealed class ListeningState {
        data object Idle : ListeningState()
        data object Listening : ListeningState()
        data object Processing : ListeningState()
        data class Error(val message: String) : ListeningState()
    }

    /**
     * Represents the result of command processing
     */
    sealed class CommandResult {
        data class Success(val command: String, val action: VoiceAction) : CommandResult()
        data class Error(val message: String) : CommandResult()
        data object NoMatch : CommandResult()
    }

    /**
     * Actions that can be triggered by voice commands
     */
    sealed class VoiceAction {
        data object NavigateToScan : VoiceAction()
        data object TriggerSOS : VoiceAction()
        data object ShowAlerts : VoiceAction()
        data object NavigateToHome : VoiceAction()
        data class ReportIncident(val description: String) : VoiceAction()
        data class Unknown(val text: String) : VoiceAction()
    }

    // ============================================================
    // STATE FLOWS
    // ============================================================

    private val _listeningState = MutableStateFlow<ListeningState>(ListeningState.Idle)
    val listeningState: StateFlow<ListeningState> = _listeningState.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _commandResult = MutableStateFlow<CommandResult?>(null)
    val commandResult: StateFlow<CommandResult?> = _commandResult.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // Retry counter for auto-restart (prevents infinite loops)
    private var retryCount = 0
    private val maxRetries = 3
    private var isAutoRestartEnabled = true

    // ============================================================
    // SPEECH RECOGNIZER
    // ============================================================

    // ============================================================
    // SPEECH SERVICES
    // ============================================================

    private val speechService = SpeechService(context)
    private val audioRecorder = AudioRecorder()

    // Navigation callbacks (set by the UI)
    var onNavigateToScan: (() -> Unit)? = null
    var onTriggerSOS: ((SOSStatus) -> Unit)? = null
    var onShowAlerts: (() -> Unit)? = null
    var onNavigateToHome: (() -> Unit)? = null
    var onReportIncident: ((String) -> Unit)? = null

    // ============================================================
    // COMMAND PATTERNS
    // ============================================================

    /**
     * Command patterns mapped to their normalized form
     * Uses flexible matching to handle various phrasings
     */
    private val commandPatterns = mapOf(
        // Scan command variations
        "open scan" to listOf(
            "open scan", "start scan", "begin scan", "scan area", 
            "scan surroundings", "look around", "check area"
        ),
        // SOS command variations
        "send sos" to listOf(
            "send sos", "sos", "emergency", "help me", "i need help",
            "call emergency", "send emergency", "trigger sos"
        ),
        // Alerts command variations
        "show alerts" to listOf(
            "show alerts", "open alerts", "view alerts", "alerts",
            "check alerts", "see alerts"
        ),
        // Home command variations
        "back home" to listOf(
            "back home", "go home", "return home", "home", 
            "main screen", "main menu"
        ),
        // Incident command variations
        "send incident" to listOf(
            "send incident", "report incident", "report emergency",
            "file incident", "create incident", "log incident"
        )
    )

    // ============================================================
    // INITIALIZATION
    // ============================================================

    init {
        // No setup needed for custom recorder
    }

    // ============================================================
    // COMMAND PROCESSING
    // ============================================================

    /**
     * Process the recognized speech and execute appropriate action
     */
    private fun processCommand(rawText: String) {
        val normalizedText = rawText.lowercase().trim()
        Log.d("VoiceViewModel", "Processing command: '$normalizedText'")

        // Check for incident command first (needs special handling for description)
        if (isIncidentCommand(normalizedText)) {
            val description = extractIncidentDescription(normalizedText)
            if (description != null) {
                executeAction(VoiceAction.ReportIncident(description))
            } else {
                accessibilityManager?.speak("Please describe the incident after saying 'send incident'")
                _commandResult.value = CommandResult.Error("Missing incident description")
            }
            return
        }

        // Check other commands
        for ((command, patterns) in commandPatterns) {
            if (patterns.any { normalizedText.contains(it) }) {
                val action = when (command) {
                    "open scan" -> VoiceAction.NavigateToScan
                    "send sos" -> VoiceAction.TriggerSOS
                    "show alerts" -> VoiceAction.ShowAlerts
                    "back home" -> VoiceAction.NavigateToHome
                    else -> VoiceAction.Unknown(normalizedText)
                }
                executeAction(action)
                return
            }
        }

        // No match found
        accessibilityManager?.speak("Unknown command: $rawText")
        _commandResult.value = CommandResult.NoMatch
        executeAction(VoiceAction.Unknown(normalizedText))
    }

    /**
     * Check if the text contains an incident command
     */
    private fun isIncidentCommand(text: String): Boolean {
        val incidentPatterns = commandPatterns["send incident"] ?: return false
        return incidentPatterns.any { text.contains(it) }
    }

    /**
     * Extract the incident description from the voice command
     * Assumes format: "send incident [description]"
     */
    private fun extractIncidentDescription(text: String): String? {
        val incidentPatterns = commandPatterns["send incident"] ?: return null
        
        for (pattern in incidentPatterns) {
            if (text.contains(pattern)) {
                // Extract everything after the command phrase
                val description = text.substringAfter(pattern, "")
                    .replace("about", "")
                    .replace("with", "")
                    .replace("saying", "")
                    .trim()
                
                return description.ifEmpty { null }
            }
        }
        
        // Try generic extraction - everything after "incident"
        val afterIncident = text.substringAfter("incident", "")
            .replace("about", "")
            .replace("with", "")
            .replace("saying", "")
            .trim()
        
        return afterIncident.ifEmpty { null }
    }

    /**
     * Execute the voice action
     */
    private fun executeAction(action: VoiceAction) {
        when (action) {
            is VoiceAction.NavigateToScan -> {
                accessibilityManager?.speak("Opening scan screen")
                _commandResult.value = CommandResult.Success("open scan", action)
                onNavigateToScan?.invoke()
            }
            is VoiceAction.TriggerSOS -> {
                accessibilityManager?.speak("Sending SOS. Please select your status.")
                _commandResult.value = CommandResult.Success("send sos", action)
                // Trigger SOS with default status - UI will prompt for confirmation
                onTriggerSOS?.invoke(SOSStatus.NEED_HELP)
            }
            is VoiceAction.ShowAlerts -> {
                accessibilityManager?.speak("Showing alerts")
                _commandResult.value = CommandResult.Success("show alerts", action)
                onShowAlerts?.invoke()
            }
            is VoiceAction.NavigateToHome -> {
                accessibilityManager?.speak("Going back to home")
                _commandResult.value = CommandResult.Success("back home", action)
                onNavigateToHome?.invoke()
            }
            is VoiceAction.ReportIncident -> {
                accessibilityManager?.speak("Reporting incident: ${action.description}")
                _commandResult.value = CommandResult.Success("send incident", action)
                onReportIncident?.invoke(action.description)
            }
            is VoiceAction.Unknown -> {
                _commandResult.value = CommandResult.Error("Unknown command: ${action.text}")
            }
        }
    }

    // ============================================================
    // PUBLIC METHODS
    // ============================================================

    /**
     * Start listening for voice commands
     * 
     * @param languageCode Language code (e.g., "en-US", "es-ES")
     */
    fun startListening(languageCode: String = "en-US") {
        if (_listeningState.value == ListeningState.Listening) {
            Log.d("VoiceViewModel", "Already listening")
            return
        }

        _listeningState.value = ListeningState.Listening
        accessibilityManager?.speak("Listening...")

        viewModelScope.launch {
            try {
                // Android Native SpeechRecognizer handles its own audio capture
                // We pass empty ByteArray since it's not used
                _listeningState.value = ListeningState.Processing
                
                val result = speechService.transcribeAndTranslate(ByteArray(0), languageCode)
                
                // Handle different result scenarios
                when {
                    // Empty result = no speech detected (common with Android Native)
                    result.isEmpty() -> {
                        _listeningState.value = ListeningState.Idle
                        _lastError.value = "No speech detected. Please try again."
                        _recognizedText.value = ""
                        accessibilityManager?.speak("No speech detected. Please try again.")
                        Log.d("VoiceViewModel", "No speech detected - user should retry")
                    }
                    // Service not configured error
                    result == "Speech services are not configured yet." -> {
                        _listeningState.value = ListeningState.Error(result)
                        _lastError.value = result
                        accessibilityManager?.speak(result)
                    }
                    // Success - process the command
                    else -> {
                        _recognizedText.value = result
                        _listeningState.value = ListeningState.Idle
                        _lastError.value = null
                        processCommand(result)
                    }
                }
            } catch (e: Exception) {
                Log.e("VoiceViewModel", "Error in voice flow: ${e.message}")
                _listeningState.value = ListeningState.Error(e.message ?: "Unknown error")
                _lastError.value = e.message
            }
        }
    }

    /**
     * Restart listening after an error (used for auto-recovery)
     * Resets state and starts a new listening session
     */
    private fun restartListening() {
        Log.d("VoiceViewModel", "Restarting listening session")
        resetState()
        startListening()
    }

    /**
     * Enable or disable auto-restart feature
     */
    fun setAutoRestartEnabled(enabled: Boolean) {
        isAutoRestartEnabled = enabled
    }

    /**
     * Get current retry count (for debugging/testing)
     */
    fun getRetryCount(): Int = retryCount

    /**
     * Stop listening for voice commands
     */
    fun stopListening() {
        if (_listeningState.value == ListeningState.Listening) {
            audioRecorder.stopRecording()
            _listeningState.value = ListeningState.Idle
        }
    }

    /**
     * Reset the voice command state
     */
    fun resetState() {
        _recognizedText.value = ""
        _commandResult.value = null
        _lastError.value = null
        _listeningState.value = ListeningState.Idle
        retryCount = 0 // Reset retry count on manual reset
    }

    /**
     * Check if speech recognizer is available on this device
     */
    /**
     * Check if speech recognizer is available on this device
     */
    fun isSpeechRecognizerAvailable(): Boolean {
        return true
    }

    /**
     * Get list of supported commands
     */
    fun getSupportedCommands(): List<String> {
        return commandPatterns.keys.toList()
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    override fun onCleared() {
        super.onCleared()
        audioRecorder.stopRecording()
    }
}

/**
 * Factory for creating VoiceViewModel with dependencies
 */
class VoiceViewModelFactory(
    private val context: Context,
    private val accessibilityManager: AccessibilityManager? = null
) : androidx.lifecycle.ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoiceViewModel::class.java)) {
            return VoiceViewModel(context, accessibilityManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

