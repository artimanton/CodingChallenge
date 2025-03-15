package app.bettermetesttask.movies.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.ObserveMoviesUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

class MoviesViewModel @Inject constructor(
    private val observeMoviesUseCase: ObserveMoviesUseCase,
    private val likeMovieUseCase: AddMovieToFavoritesUseCase,
    private val dislikeMovieUseCase: RemoveMovieFromFavoritesUseCase,
    private val adapter: MoviesAdapter
) : ViewModel() {

    private val moviesMutableFlow: MutableStateFlow<MoviesState> = MutableStateFlow(MoviesState.Initial)
    private val navigationMutableFlow = MutableSharedFlow<NavigationEvent>()

    val moviesStateFlow: StateFlow<MoviesState>
        get() = moviesMutableFlow.asStateFlow()

    val navigationFlow: SharedFlow<NavigationEvent>
        get() = navigationMutableFlow.asSharedFlow()

    fun loadMovies() {
        viewModelScope.launch {
            moviesMutableFlow.emit(MoviesState.Loading)
            observeMoviesUseCase()
                .catch { error ->
                    moviesMutableFlow.emit(MoviesState.Error(error.message ?: "Unknown error"))
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            moviesMutableFlow.emit(MoviesState.Loaded(result.data))
                            adapter.submitList(result.data)
                        }
                        is Result.Error -> {
                            moviesMutableFlow.emit(MoviesState.Error(result.error.message ?: "Unknown error"))
                        }
                    }
                }
        }
    }

    fun likeMovie(movie: Movie) {
        viewModelScope.launch {
            try {
                if (!movie.liked) {
                    likeMovieUseCase(movie.id)
                } else {
                    dislikeMovieUseCase(movie.id)
                }
            } catch (e: Exception) {
                moviesMutableFlow.emit(MoviesState.Error(e.message ?: "Error updating favorites"))
            }
        }
    }

    fun openMovieDetails(movie: Movie) {
        viewModelScope.launch {
            navigationMutableFlow.emit(NavigationEvent.OpenMovieDetails(movie.id, movie.liked))
        }
    }
}

sealed class NavigationEvent {
    data class OpenMovieDetails(val movieId: Int, val isLiked: Boolean) : NavigationEvent()
}