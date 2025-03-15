package app.bettermetesttask.movies.sections.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.GetMovieUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MovieDetailsViewModel @Inject constructor(
    private val getMovieUseCase: GetMovieUseCase,
    private val likeMovieUseCase: AddMovieToFavoritesUseCase,
    private val dislikeMovieUseCase: RemoveMovieFromFavoritesUseCase
) : ViewModel() {

    private val _movieFlow = MutableStateFlow<Movie?>(null)
    val movieFlow: StateFlow<Movie?> = _movieFlow.asStateFlow()

    fun loadMovie(movieId: Int, isLiked: Boolean) {
        viewModelScope.launch {
            when (val result = getMovieUseCase(movieId)) {
                is Result.Success -> _movieFlow.emit(result.data.copy(liked = isLiked))
                is Result.Error -> {
                    TODO()
                }
            }
        }
    }

    fun toggleLike() {
        val currentMovie = _movieFlow.value ?: return
        viewModelScope.launch {
            try {
                if (currentMovie.liked) {
                    dislikeMovieUseCase(currentMovie.id)
                } else {
                    likeMovieUseCase(currentMovie.id)
                }
                _movieFlow.emit(currentMovie.copy(liked = !currentMovie.liked))
            } catch (e: Exception) {
                TODO()
            }
        }
    }
} 