package app.bettermetesttask.movies.injection

import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.GetMovieUseCase
import app.bettermetesttask.domainmovies.interactors.ObserveMoviesUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import app.bettermetesttask.featurecommon.injection.scopes.FragmentScope
import app.bettermetesttask.movies.sections.MoviesAdapter
import app.bettermetesttask.movies.sections.MoviesViewModel
import app.bettermetesttask.movies.sections.compose.MovieDetailsViewModel
import dagger.Module
import dagger.Provides

@Module
class MoviesScreenModule {

    @Provides
    @FragmentScope
    fun provideMoviesViewModel(
        observeMoviesUseCase: ObserveMoviesUseCase,
        addMovieToFavoritesUseCase: AddMovieToFavoritesUseCase,
        removeMovieFromFavoritesUseCase: RemoveMovieFromFavoritesUseCase,
        adapter: MoviesAdapter
    ): MoviesViewModel {
        return MoviesViewModel(
            observeMoviesUseCase,
            addMovieToFavoritesUseCase,
            removeMovieFromFavoritesUseCase,
            adapter
        )
    }

    @Provides
    @FragmentScope
    fun provideMovieDetailsViewModel(
        getMovieUseCase: GetMovieUseCase,
        addMovieToFavoritesUseCase: AddMovieToFavoritesUseCase,
        removeMovieFromFavoritesUseCase: RemoveMovieFromFavoritesUseCase
    ): MovieDetailsViewModel {
        return MovieDetailsViewModel(
            getMovieUseCase,
            addMovieToFavoritesUseCase,
            removeMovieFromFavoritesUseCase
        )
    }

    @Provides
    @FragmentScope
    fun provideMoviesAdapter(): MoviesAdapter {
        return MoviesAdapter()
    }
}