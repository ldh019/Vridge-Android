package com.gdsc_cau.vridge.ui.talk

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdsc_cau.vridge.data.models.Tts
import com.gdsc_cau.vridge.data.repository.TalkRepository
import com.gdsc_cau.vridge.ui.record.LOG_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class TalkViewModel @Inject constructor(private val repository: TalkRepository) : ViewModel() {
    private val _talks = MutableStateFlow<List<Tts>>(emptyList())

    val talks: StateFlow<List<Tts>>
        get() = _talks

    private var player: MediaPlayer? = null

    lateinit var vid: String
        private set

    val _dummyTalkStateData = mutableStateListOf<VoiceState>().apply {
        add(VoiceState.VOICE_READY)
        add(VoiceState.VOICE_READY)
        add(VoiceState.VOICE_READY)
        add(VoiceState.VOICE_READY)
    }

    val _dummyTts = mutableStateListOf<Tts>().apply {
        add(Tts(id = "1", text = "안녕하세요 반갑습니다", timestamp = 0L))
        add(Tts(id = "2", text = "요리를 하며 창의력을 발휘하는 것을 즐깁니다.", timestamp = 0L))
        add(Tts(id = "3", text = "건강을 유지하기 위해 저는 규칙적으로 운동을 합니다.", timestamp = 0L))
        add(Tts(id = "4", text = "오늘 날씨가 참 좋네요.", timestamp = 0L))
    }

    fun addDummyTts(text: String) {
        _dummyTts.add(Tts(id = (_dummyTts.size + 1).toString(), text = text, timestamp = 0L))
    }

    fun addDummyTtsState() {
        _dummyTalkStateData.add(VoiceState.VOICE_LOADING)
        viewModelScope.launch {
            delay(1000)
            _dummyTalkStateData[_dummyTalkStateData.size - 1] = VoiceState.VOICE_READY
        }
    }

    fun setVid(vid: String) {
        this.vid = vid
    }

    fun onPlay(start: Boolean, ttsId: String = "") = if (start) {
        startPlaying(ttsId)
    } else {
        stopPlaying()
    }

    private fun startPlaying(ttsId: String) {
        viewModelScope.launch {
            val url = repository.getTtsUrl(vid, ttsId)

            player = MediaPlayer().apply {
                try {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(url)
                    prepare()
                    start()
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "prepare() failed")
                }
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    fun createTts(text: String) {
        viewModelScope.launch {
            repository.createTts(text, vid)
        }
    }

    fun getTalks() {
        viewModelScope.launch {
            _talks.emit(repository.getTalks(vid))
        }
    }

    fun getTtsState(ttsId: String) = flow {
        viewModelScope.launch {
            this@flow.emit(repository.getTtsState(vid, ttsId))
        }
    }
}
