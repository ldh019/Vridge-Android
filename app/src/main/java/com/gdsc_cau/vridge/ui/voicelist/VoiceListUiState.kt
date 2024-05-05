package com.gdsc_cau.vridge.ui.voicelist

import com.gdsc_cau.vridge.data.models.Voice

sealed interface VoiceListUiState {
    data object Loading : VoiceListUiState
    data class Empty(
        val isRecording: Boolean = false
    ) : VoiceListUiState
    data class Success(
        val isRecording: Boolean = false,
        val voiceList: List<Voice>
    ) : VoiceListUiState
}
