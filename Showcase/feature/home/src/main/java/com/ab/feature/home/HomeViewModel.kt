package com.ab.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ab.common.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(): ViewModel() {

    private val _effectChannel = Channel<Route>(Channel.BUFFERED)
    val effectChannel = _effectChannel.receiveAsFlow()


     fun onIntent(intent: HomeIntent){
         viewModelScope.launch {
             when (intent) {
                 is HomeIntent.OnItemClicked -> {
                     _effectChannel.send(intent.route)
                 }
             }
         }
    }

}