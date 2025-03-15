package app.bettermetesttask.movies.sections.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.featurecommon.injection.utils.Injectable
import app.bettermetesttask.featurecommon.injection.viewmodel.SimpleViewModelProviderFactory
import app.bettermetesttask.movies.R
import app.bettermetesttask.movies.sections.MoviesState
import app.bettermetesttask.movies.sections.MoviesViewModel
import app.bettermetesttask.movies.sections.NavigationEvent
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class MoviesComposeFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelProvider: Provider<MoviesViewModel>

    private val viewModel by viewModels<MoviesViewModel> {
        SimpleViewModelProviderFactory(
            viewModelProvider
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val viewState by viewModel.moviesStateFlow.collectAsState()
                MoviesComposeScreen(
                    moviesState = viewState,
                    likeMovie = { movie -> viewModel.likeMovie(movie) },
                    onMovieClick = { movie -> viewModel.openMovieDetails(movie) },
                    viewLoaded = { viewModel.loadMovies() },
                    onRetryClick = { viewModel.loadMovies() },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigationFlow.collectLatest { event ->
                when (event) {
                    is NavigationEvent.OpenMovieDetails -> {
                        findNavController().navigate(R.id.movieDetailsFragment, Bundle().apply {
                            putInt("movieId", event.movieId)
                            putBoolean("isLiked", event.isLiked)
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun MoviesComposeScreen(
    moviesState: MoviesState,
    likeMovie: (Movie) -> Unit,
    viewLoaded: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    onRetryClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewLoaded()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when (moviesState) {
            MoviesState.Initial -> {}
            is MoviesState.Loaded -> {
                LazyColumn {
                    items(
                        items = moviesState.movies,
                        key = { it.id }
                    ) { movie ->
                        MovieItem(
                            movie = movie,
                            onLikeClicked = { likeMovie(movie) },
                            onMovieClicked = { onMovieClick(movie) }
                        )
                    }
                }
            }

            MoviesState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MoviesState.Error -> {
                ErrorView(
                    message = moviesState.message,
                    onRetryClick = onRetryClick
                )
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = Color.Red,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = onRetryClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun MovieItem(
    movie: Movie,
    onLikeClicked: () -> Unit,
    onMovieClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onMovieClicked),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = movie.posterPath,
                contentDescription = "Movie Poster",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = movie.title, fontSize = 18.sp, color = Color.Black)
                Text(text = movie.description, fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = onLikeClicked) {
                Icon(
                    imageVector = if (movie.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like Button",
                    tint = if (movie.liked) Color.Red else Color.Gray
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
private fun PreviewsMoviesComposeScreen() {
    MoviesComposeScreen(MoviesState.Loaded(
        List(20) { index ->
            Movie(
                index,
                "Title $index",
                "Overview $index",
                null,
                liked = index % 2 == 0,
            )
        }
    ), likeMovie = {}, viewLoaded = {}, onRetryClick = {}, onMovieClick = {})
}