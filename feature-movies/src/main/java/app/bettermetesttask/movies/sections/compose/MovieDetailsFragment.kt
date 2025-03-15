package app.bettermetesttask.movies.sections.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.featurecommon.injection.utils.Injectable
import app.bettermetesttask.featurecommon.injection.viewmodel.SimpleViewModelProviderFactory
import coil3.compose.AsyncImage
import javax.inject.Inject
import javax.inject.Provider

class MovieDetailsFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelProvider: Provider<MovieDetailsViewModel>

    private val viewModel by viewModels<MovieDetailsViewModel> {
        SimpleViewModelProviderFactory(viewModelProvider)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val movie by viewModel.movieFlow.collectAsState()
                MovieDetailsScreen(
                    movie = movie,
                    onBackClick = { findNavController().navigateUp() },
                    onLikeClick = { viewModel.toggleLike() }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val movieId = arguments?.getInt("movieId") ?: -1
        viewModel.loadMovie(movieId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieDetailsScreen(
    movie: Movie?,
    onBackClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = movie?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Bask")
                    }
                },
                actions = {
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            if (movie?.liked == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Add to Favorites"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (movie != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AsyncImage(
                    model = movie.posterPath,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = movie.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = movie.description,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieDetailsScreenPreviewLoaded() {
    MovieDetailsScreen(
        movie = Movie(
            id = 1,
            title = "Movie #0",
            description = "Some movie description #0",
            posterPath = null,
            liked = true
        ),
        onBackClick = {},
        onLikeClick = {}
    )
}