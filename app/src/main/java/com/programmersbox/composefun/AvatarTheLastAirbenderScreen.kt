package com.programmersbox.composefun

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.*
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import androidx.room.*
import coil.compose.AsyncImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.io.IOException
import java.util.concurrent.TimeUnit


class AvatarViewModel(private val db: AvatarDatabase) : ViewModel() {

    @OptIn(ExperimentalPagingApi::class)
    val pager =
        Pager(
            PagingConfig(
                pageSize = 21,
                enablePlaceholders = true
            ),
            remoteMediator = AvatarRemoteMediator(20, db, AvatarApiService())
        ) { db.avatarDao().pagingSource() }
            .flow
            .cachedIn(viewModelScope)

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ATLAScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AvatarDatabase.getInstance(context) }
    val vm: AvatarViewModel = viewModel { AvatarViewModel(db) }
    val data = vm.pager.collectAsLazyPagingItems()

    ScaffoldTop(
        screen = Screen.AvatarScreen,
        navController = navController
    ) { p ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = data.loadState.refresh == LoadState.Loading),
            onRefresh = { data.refresh() },
            indicatorPadding = p
        ) {
            LazyColumn(
                contentPadding = p,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(data) {
                    if (it != null) {
                        AvatarCard(it)
                    } else {
                        AvatarPlaceholderCard()
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun AvatarCard(item: ATLACharacter) {
    Card {
        ListItem(
            text = { Text(item.name.orEmpty()) },
            secondaryText = { Text(item.affiliation.orEmpty()) },
            icon = {
                Surface(shape = CircleShape) {
                    AsyncImage(
                        model = item.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(75.dp)
                    )
                }
            }
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun AvatarPlaceholderCard() {
    Card {
        ListItem(
            text = {
                Text(
                    "",
                    modifier = Modifier.placeholder(true, color = MaterialTheme.colors.primaryVariant, highlight = PlaceholderHighlight.shimmer())
                )
            },
            secondaryText = {
                Text(
                    "",
                    modifier = Modifier.placeholder(true, color = MaterialTheme.colors.primaryVariant, highlight = PlaceholderHighlight.shimmer())
                )
            },
            icon = {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .border(1.dp, color = Color.Black, shape = CircleShape)
                        .size(50.dp)
                        .placeholder(true, color = MaterialTheme.colors.primaryVariant, highlight = PlaceholderHighlight.shimmer())
                ) {}
            }
        )
    }
}

@OptIn(ExperimentalPagingApi::class)
class AvatarRemoteMediator(
    private val pageCount: Int,
    private val database: AvatarDatabase,
    private val networkService: AvatarApiService
) : RemoteMediator<Int, ATLACharacter>() {
    private val userDao = database.avatarDao()

    private var lastUpdated = 0L
    private var page = 1

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ATLACharacter>
    ): MediatorResult {
        return try {
            // The network load method takes an optional after=<user.id>
            // parameter. For every page after the first, pass the last user
            // ID to let it continue from where it left off. For REFRESH,
            // pass null to load the first page.
            val loadKey = when (loadType) {
                LoadType.REFRESH -> {
                    page = 1
                    null
                }
                // In this example, you never need to prepend, since REFRESH
                // will always load the first page in the list. Immediately
                // return, reporting end of pagination.
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull() ?: return MediatorResult.Success(endOfPaginationReached = true)

                    // You must explicitly check if the last item is null when
                    // appending, since passing null to networkService is only
                    // valid for initial load. If lastItem is null it means no
                    // items were loaded after the initial REFRESH and there are
                    // no more items to load.

                    lastItem._id
                }
            }

            // Suspending network load via Retrofit. This doesn't need to be
            // wrapped in a withContext(Dispatcher.IO) { ... } block since
            // Retrofit's Coroutine CallAdapter dispatches on a worker
            // thread.
            val response = networkService.getCharacters(pageCount, page).orEmpty()
            page++
            lastUpdated = System.currentTimeMillis()
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    userDao.clearAll()
                }

                // Insert new users into database, which invalidates the
                // current PagingData, allowing Paging to present the updates
                // in the DB.
                userDao.insertCharacters(response)
            }

            MediatorResult.Success(endOfPaginationReached = response.isEmpty())
        } catch (e: IOException) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
        return if (System.currentTimeMillis() - lastUpdated >= cacheTimeout) {
            // Cached data is up-to-date, so there is no need to re-fetch
            // from the network.
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            // Need to refresh cached data from network; returning
            // LAUNCH_INITIAL_REFRESH here will also block RemoteMediator's
            // APPEND and PREPEND from running until REFRESH succeeds.
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
}

@Database(
    entities = [ATLACharacter::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
@TypeConverters(Converters::class)
abstract class AvatarDatabase : RoomDatabase() {

    abstract fun avatarDao(): AvatarDao

    companion object {

        @Volatile
        private var INSTANCE: AvatarDatabase? = null

        fun getInstance(context: Context): AvatarDatabase =
            INSTANCE ?: synchronized(this) { INSTANCE ?: buildDatabase(context).also { INSTANCE = it } }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, AvatarDatabase::class.java, "avatar.db").build()
    }
}

@Dao
interface AvatarDao {

    @Insert
    suspend fun insertCharacter(item: ATLACharacter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(item: List<ATLACharacter>)

    @Query("SELECT * FROM avatar_character")
    fun pagingSource(): PagingSource<Int, ATLACharacter>

    @Delete
    suspend fun deleteCharacter(item: ATLACharacter)

    @Query("DELETE FROM avatar_character")
    suspend fun clearAll()

}

object Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> = value.fromJson<List<String>>().orEmpty()

    @TypeConverter
    fun fromList(list: List<String>?): String = list.toJson()
}

@Entity(tableName = "avatar_character")
data class ATLACharacter(
    @PrimaryKey
    val _id: String,
    val allies: List<String>?,
    val enemies: List<String>?,
    val photoUrl: String?,
    val name: String?,
    val affiliation: String?
)

class AvatarApiService {
    companion object {
        private const val BASE_URL = "https://last-airbender-api.herokuapp.com"
    }

    suspend fun getCharacters(perPage: Int, page: Int) =
        getApi<List<ATLACharacter>>("$BASE_URL/api/v1/characters?perPage=$perPage&page=$page")
}