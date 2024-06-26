package com.gdsc_cau.vridge.ui.record

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface RecordUiState {
    @Immutable
    data object Loading : RecordUiState

    @Immutable
    data class Success(
        val index: Int,
        val text: String,
        val size: Int,
        val state: RecordState
    ) : RecordUiState
}
