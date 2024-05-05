package com.gdsc_cau.vridge.ui.voicelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdsc_cau.vridge.data.models.Voice
import com.gdsc_cau.vridge.data.repository.UserRepository
import com.gdsc_cau.vridge.data.repository.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class VoiceListViewModel @Inject constructor(
    private val repository: VoiceRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _errorFlow = MutableSharedFlow<Throwable>()
    val errorFlow: SharedFlow<Throwable> get() = _errorFlow

    private val _uiState = MutableStateFlow<VoiceListUiState>(VoiceListUiState.Loading)
    val uiState: StateFlow<VoiceListUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                flow { emit(repository.getVoiceList()) },
                flow { emit(userRepository.getRecordingStatus()) }) { voiceList, isRecording ->
                if (voiceList.isEmpty())
                    VoiceListUiState.Empty(isRecording = isRecording)
                else
                    VoiceListUiState.Success(
                        voiceList = voiceList,
                        isRecording = isRecording
                    )
            }.catch { throwable ->
                _errorFlow.emit(throwable)
            }.collect { _uiState.value = it }
        }
    }

    fun synthesize(voiceList: List<String>, name: String, pitch: Float) {
        viewModelScope.launch {
            try {
                val state = _uiState.value

                if (state !is VoiceListUiState.Success) return@launch
                _uiState.value = VoiceListUiState.Success(
                    voiceList = state.voiceList + repository.synthesize(voiceList, name, pitch.roundToInt())
                )
            } catch (e: Exception) {
                _errorFlow.emit(e)
            }
        }
    }

    fun removeRecordingVoice() {
        viewModelScope.launch {
            try {
                if (repository.removeRecordingVoice()) {
                    _uiState.combine(flow {emit(userRepository.getRecordingStatus())}) { state, isRecording ->
                        if (state is VoiceListUiState.Success) {
                            VoiceListUiState.Success(
                                voiceList = state.voiceList,
                                isRecording = isRecording
                            )
                        } else {
                            VoiceListUiState.Empty(isRecording = isRecording)
                        }
                    }.catch { throwable ->
                        _errorFlow.emit(throwable)
                    }.collect { _uiState.value = it }
                }
            } catch (e: Exception) {
                _errorFlow.emit(e)
            }
        }
    }
}
