package com.programmersbox.composefun

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.properties.Delegates
import kotlin.random.Random

private fun List<Card>.toSum() = sortedByDescending { if (it.value > 10) 10 else it.value }
    .fold(0) { v, c -> v + if (c.value == 1 && v + 11 < 22) 11 else if (c.value == 1) 1 else if (c.value > 10) 10 else c.value }

class BlackjackStats {
    var winCount by mutableStateOf(0)
    var loseCount by mutableStateOf(0)
    var drawCount by mutableStateOf(0)
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("blackjack")
val WIN_COUNT = intPreferencesKey("wins")
val LOSE_COUNT = intPreferencesKey("loses")
val DRAW_COUNT = intPreferencesKey("draws")

val CARD_SPACING = floatPreferencesKey("spacing")

@Composable
fun Int.animateAsState() = animateIntAsState(targetValue = this).value

@SuppressLint("FlowOperatorInvokedInComposition")
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun Blackjack(navController: NavController) {
    val playerHand = remember { mutableStateListOf<Card>() }
    val dealerHand = remember { mutableStateListOf<Card>() }
    var cardCount by remember { mutableStateOf(52) }

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    val dataStore = LocalContext.current.dataStore
    val totalWins by dataStore.data.map { it[WIN_COUNT] ?: 0 }.collectAsState(initial = 0)
    val totalLoses by dataStore.data.map { it[LOSE_COUNT] ?: 0 }.collectAsState(initial = 0)
    val totalDraws by dataStore.data.map { it[DRAW_COUNT] ?: 0 }.collectAsState(initial = 0)

    val originalSpace by dataStore.data.map { it[CARD_SPACING] ?: 0f }.collectAsState(initial = 0f)
    var cardSpacing by remember(originalSpace) { mutableStateOf(originalSpace) }

    fun updateTotalStat(key: Preferences.Key<Int>, total: Int) = scope.launch { dataStore.edit { it[key] = total + 1 } }

    val stats = remember { BlackjackStats() }
    var playing by remember { mutableStateOf(false) }
    var dealing by remember { mutableStateOf(false) }

    val deck = remember {
        val d = Deck.defaultDeck()
        d.shuffle()
        d.addDeckListener {
            onDraw { _, size ->
                if (size == 0) {
                    d.addDeck(Deck.defaultDeck())
                    d.shuffle()
                }
                cardCount = size
            }
        }
        d
    }

    fun winCheck() {
        val pSum = playerHand.toSum()
        val dSum = dealerHand.toSum()

        val state = when {
            pSum > 21 && dSum <= 21 -> {
                stats.loseCount++
                updateTotalStat(LOSE_COUNT, totalLoses)
                "Busted and Lost"
            }
            dSum > 21 && pSum <= 21 -> {
                stats.winCount++
                updateTotalStat(WIN_COUNT, totalWins)
                "Win and Dealer Busted"
            }
            pSum in (dSum + 1)..21 -> {
                stats.winCount++
                updateTotalStat(WIN_COUNT, totalWins)
                "Win"
            }
            dSum in (pSum + 1)..21 -> {
                stats.loseCount++
                updateTotalStat(LOSE_COUNT, totalLoses)
                "Lose"
            }
            dSum == pSum && dSum <= 21 && pSum <= 21 -> {
                stats.drawCount++
                updateTotalStat(DRAW_COUNT, totalDraws)
                "Got a Draw"
            }
            else -> {
                stats.drawCount++
                updateTotalStat(DRAW_COUNT, totalDraws)
                "Got a Draw"
            }
        }

        scope.launch { scaffoldState.snackbarHostState.showSnackbar("You $state", duration = SnackbarDuration.Short) }
    }

    /*val currentOnBack by rememberUpdatedState(onBack)
    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (scaffoldState.drawerState.isOpen) scope.launch { scaffoldState.drawerState.close() } else currentOnBack()
            }
        }
    }

    DisposableEffect(backDispatcher) {
        backDispatcher?.addCallback(backCallback)
        onDispose { backCallback.remove() }
    }*/

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } },
                title = { Text("Dealer has: ${dealerHand.toSum().animateAsState()}") },
                actions = { Text("${cardCount.animateAsState()} card(s) left") }
            )
        },
        bottomBar = { BottomAppBar { Text("Player has: ${playerHand.toSum().animateAsState()}", style = MaterialTheme.typography.h6) } },
        drawerContent = {
            Scaffold(topBar = { TopAppBar(title = { Text("Stats") }) }) {
                Column(
                    modifier = Modifier
                        .padding(5.dp)
                        .padding(it),
                ) {
                    val typography = MaterialTheme.typography.body1
                    Text("Times won: ${stats.winCount}", style = typography)
                    Text("Times lost: ${stats.loseCount}", style = typography)
                    Text("Times drawn: ${stats.drawCount}", style = typography)
                    Divider()
                    Text("Total Times won: $totalWins", style = typography)
                    Text("Total Times lost: $totalLoses", style = typography)
                    Text("Total Times drawn: $totalDraws", style = typography)
                    Button(
                        onClick = {
                            updateTotalStat(WIN_COUNT, -1)
                            updateTotalStat(LOSE_COUNT, -1)
                            updateTotalStat(DRAW_COUNT, -1)
                        }
                    ) { Text("Reset Saved Stats", style = MaterialTheme.typography.button) }

                    Divider()
                    Spacer(Modifier.padding(5.dp))

                    Text("Card Spacing: ${cardSpacing.roundToInt()}", style = typography)

                    Slider(
                        value = cardSpacing,
                        onValueChange = { v -> cardSpacing = v },
                        steps = 50,
                        valueRange = 0f..50f,
                        onValueChangeFinished = { scope.launch { dataStore.edit { s -> s[CARD_SPACING] = cardSpacing } } }
                    )
                }
            }
        }
    ) { p ->
        Column(
            modifier = Modifier
                .padding(p)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Hand(dealerHand, -cardSpacing.dp)

            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Button(
                    onClick = {
                        dealing = true
                        playerHand.clear()
                        dealerHand.clear()
                        scope.launch {
                            drawCard(playerHand, deck)
                            drawCard(dealerHand, deck)
                            drawCard(playerHand, deck, false)
                            playing = true
                            dealing = false
                        }
                    },
                    enabled = !playing && !dealing
                ) { Text("Play Again", style = MaterialTheme.typography.button) }

                Button(
                    onClick = {
                        playing = false
                        scope.launch {
                            while (dealerHand.toSum() < 17) drawCard(dealerHand, deck)
                            winCheck()
                        }
                    },
                    enabled = playing
                ) { Text("Stay", style = MaterialTheme.typography.button) }

                Button(
                    onClick = {
                        playerHand.add(deck.draw())
                        if (playerHand.toSum() > 21) {
                            playing = false
                            winCheck()
                        }
                    },
                    enabled = playerHand.toSum() <= 21 && playing
                ) { Text("Hit", style = MaterialTheme.typography.button) }
            }

            Hand(playerHand, -cardSpacing.dp)
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun Hand(h: SnapshotStateList<Card>, spacedBy: Dp = 0.dp) = LazyRow(
    modifier = Modifier.heightIn(min = 150.dp),
    horizontalArrangement = Arrangement.spacedBy(spacedBy)
) { items(h) { PlayingCard(card = it, modifier = Modifier.padding(5.dp)) } }

suspend fun drawCard(hand: SnapshotStateList<Card>, deck: Deck<Card>, delay: Boolean = true) {
    hand.add(deck.draw())
    if (delay) delay(500)
}

@ExperimentalMaterialApi
@Composable
fun PlayingCard(card: Card, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(7.dp),
        elevation = 5.dp,
        modifier = Modifier
            .size(100.dp, 150.dp)
            .then(modifier),
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = card.toSymbolString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                textAlign = TextAlign.Start
            )
            FlowRow(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { repeat(card.value) { Text(text = card.suit.unicodeSymbol, textAlign = TextAlign.Center) } }
            Text(
                text = card.toSymbolString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

enum class CardColor { BLACK, RED }
data class Card(val value: Int, val suit: Suit) {
    val color: CardColor get() = suit.color
    val symbol: String
        get() = when (value) {
            13 -> "K"
            12 -> "Q"
            11 -> "J"
            1 -> "A"
            else -> "$value"
        }

    fun toSymbolString() = "$symbol${suit.unicodeSymbol}"
    fun toLetterString() = "$symbol${suit.symbol}"
    fun toNameString() = "$symbol of ${suit.printableName}"

    companion object {
        val RandomCard: Card get() = Card((1..13).random(), Suit.values().random())
        operator fun get(suit: Suit) = Card((1..13).random(), suit)
        operator fun get(vararg suit: Suit) = suit.map { Card((1..13).random(), it) }
        operator fun get(num: Int) = Card(num, Suit.values().random())
        operator fun get(vararg num: Int) = num.map { Card(it, Suit.values().random()) }
    }
}

enum class Suit(val printableName: String, val symbol: String, val unicodeSymbol: String, val color: CardColor) {
    SPADES("Spades", "S", "♠", CardColor.BLACK),
    CLUBS("Clubs", "C", "♣", CardColor.BLACK),
    DIAMONDS("Diamonds", "D", "♦", CardColor.RED),
    HEARTS("Hearts", "H", "♥", CardColor.RED);
}

class DeckException(message: String?) : Exception(message)

operator fun <T> Int.rangeTo(deck: AbstractDeck<T>) = deck.deck.subList(this, deck.size)

abstract class AbstractDeck<T>(cards: Iterable<T> = emptyList()) {

    constructor(vararg cards: T) : this(cards.toList())

    protected open val deckOfCards: MutableList<T> = cards.toMutableList()

    /**
     * The size of the deck
     */
    val size: Int get() = deckOfCards.size

    /**
     * An immutable version of the deck
     */
    val deck: List<T> get() = deckOfCards.toList()

    /**
     * Checks if the deck is empty
     */
    val isEmpty get() = deckOfCards.isEmpty()

    /**
     * Checks if the deck is not empty
     */
    val isNotEmpty get() = deckOfCards.isNotEmpty()

    /**
     * Gets a random card
     * # Does Not Draw #
     * @throws DeckException if deck is empty
     */
    val randomCard get() = deckOfCards.random()

    /**
     * Gets the first card in the deck
     *  # Does Not Draw #
     * @throws DeckException if deck is empty
     */
    val firstCard get() = deckOfCards.first()

    /**
     * Gets the middle card in the deck
     * # Does Not Draw #
     * @throws DeckException if deck is empty
     */
    val middleCard get() = deckOfCards[size / 2]

    /**
     * Gets the last card in the deck
     * # Does Not Draw #
     * @throws DeckException if deck is empty
     */
    val lastCard get() = deckOfCards.last()

    protected abstract fun cardAdded(vararg card: T)
    protected abstract fun cardDrawn(card: T, size: Int)
    protected abstract fun deckShuffled()

    @Suppress("UNCHECKED_CAST")
    protected fun Iterable<T>.toArray() = Array(this.count()) { i -> this.toList()[i] as Any } as Array<T>
    protected fun MutableList<T>.addCards(vararg card: T) = addAll(card).also { cardAdded(*card) }
    protected fun MutableList<T>.drawCard(index: Int = 0) = tryCatch("Deck is Empty") { removeAt(index).also { cardDrawn(it, size) } }
    protected fun MutableList<T>.removeCards(cards: Iterable<T>) = filter { it in cards }
        .let { filtered -> removeAll(filtered).also { filtered.forEach { c -> cardDrawn(c, size) } } }

    /**
     * Draws a [Card]
     * @throws DeckException if the [AbstractDeck] is Empty
     */
    @Throws(DeckException::class)
    fun draw() = deckOfCards.drawCard()

    /**
     * Draws multiple Cards!
     * @throws DeckException if the [AbstractDeck] is Empty
     */
    @Throws(DeckException::class)
    infix fun draw(amount: Int) = tryCatch("Deck is Empty") { mutableListOf<T>().apply { repeat(amount) { this += draw() } }.toList() }

    /**
     * Add [card] to the deck in the [index] location
     */
    fun addCard(index: Int, card: T) = deckOfCards.add(index, card).also { cardAdded(card) }

    /**
     * Add [cards] to the deck in the Int location
     */
    fun addCard(vararg cards: Pair<Int, T>) = cards.forEach { addCard(it.first, it.second) }

    /**
     * Add [card] to the deck
     */
    fun addCard(vararg card: T) = deckOfCards.addCards(*card)

    /**
     * Add [card] to the deck
     */
    infix fun add(card: T) = deckOfCards.addCards(card)

    /**
     * Adds all cards from [otherDeck] to this deck
     */
    infix fun addDeck(otherDeck: AbstractDeck<T>) = deckOfCards.addCards(*otherDeck.deck.toArray())

    /**
     * Add [cards] to the deck
     */
    infix fun addCards(cards: Iterable<T>) = deckOfCards.addCards(*cards.toArray())

    /**
     * Add [cards] to the deck
     */
    infix fun addCards(cards: Array<T>) = deckOfCards.addCards(*cards)

    /**
     * Gets cards from [cards]
     */
    fun getCards(vararg cards: T) = drawCards { it in cards }

    /**
     * Find cards that match the [predicate]
     */
    infix fun findCards(predicate: (T) -> Boolean) = deckOfCards.filter(predicate)

    /**
     * Find the location of [card]
     */
    infix fun findCardLocation(card: T) = deckOfCards.indexOf(card).let { if (it == -1) null else it }

    /**
     * Draws cards that match [predicate]
     */
    infix fun drawCards(predicate: (T) -> Boolean) = findCards(predicate).also { deckOfCards.removeCards(it) }

    /**
     * Removes [card]
     */
    infix fun remove(card: T) = deckOfCards.remove(card).also { if (it) cardDrawn(card, size) }

    /**
     * Sort thee deck by more than one selector
     */
    fun sortDeck(comparator: Comparator<T>) = deckOfCards.sortWith(comparator)

    /**
     * Sort the deck by one selector
     */
    fun <R> sortDeckBy(selector: (T) -> R?) where R : Comparable<R> = deckOfCards.sortBy(selector)

    /**
     * Removes [card]
     */
    fun remove(vararg card: T) = deckOfCards.filter { it in card }.let { deckOfCards.removeCards(it) }

    /**
     * Shuffles the deck
     */
    @JvmOverloads
    fun shuffle(seed: Long? = null) = deckOfCards.shuffle(seed?.let { Random(seed) } ?: Random.Default).also { deckShuffled() }

    /**
     * Truly shuffles the deck by shuffling it 7 times
     */
    @JvmOverloads
    fun trueRandomShuffle(seed: Long? = null) = repeat(7) { shuffle(seed) }

    /**
     * Completely clears the deck
     */
    fun removeAllCards() = deckOfCards.clear()

    /**
     * Reverses the order of the deck
     */
    fun reverse() = deckOfCards.reverse()

    /**
     * Randomly gets a card
     * @throws DeckException if none of the cards match the [predicate]
     */
    @JvmOverloads
    @Throws(DeckException::class)
    fun random(predicate: (T) -> Boolean = { true }) = deck.filter(predicate).tryCatch("Card Not Found") { it.random() }

    /**
     * Randomly draws a card
     * @throws DeckException if none of the cards match the [predicate]
     */
    @JvmOverloads
    @Throws(DeckException::class)
    fun randomDraw(predicate: (T) -> Boolean = { true }) = deck.filter(predicate).tryCatch("Card Not Found") { it.random().also { c -> remove(c) } }

    /**
     * Adds the cards from [deck] to this deck
     */
    operator fun invoke(deck: AbstractDeck<T>) = addDeck(deck)

    /**
     * Adds cards to the deck
     */
    operator fun invoke(vararg cards: T) = addCard(*cards)

    /**
     * Adds cards to the deck
     */
    operator fun invoke(card: Iterable<T>) = addCards(card)

    /**
     * Gets the card in the [index] of the deck
     * @throws DeckException if the [index] is outside of the deck's bounds
     */
    @Throws(DeckException::class)
    operator fun get(index: Int) = tryCatch("Index: $index, Size: $size") { deck[index] }

    /**
     * Gets a list from the deck between [range]
     * @throws DeckException if the [range] is outside the bounds of the deck
     */
    @Throws(DeckException::class)
    operator fun get(range: IntRange) = tryCatch("Index: ${range.first} to ${range.last}, Size: $size") { deck.slice(range) }

    /**
     * Gets a list from the deck between [from] and [to]
     * @throws DeckException if the range is outside the bounds of the deck
     */
    @Throws(DeckException::class)
    operator fun get(from: Int, to: Int) = tryCatch("Index: $from to $to, Size: $size") { deck.subList(from, to) }

    /**
     * Gets cards from [cards]
     */
    open operator fun get(vararg cards: T) = getCards(*cards)

    /**
     * Draws multiple Cards!
     * @throws DeckException if the card_games.Deck is Empty
     */
    @Throws(DeckException::class)
    open infix operator fun minus(amount: Int) = draw(amount)

    /**
     * Sets [index] of the deck with [card]
     */
    operator fun set(index: Int, card: T) = tryCatch("Index: $index, Size: $size") { deckOfCards[index] = card }

    /**
     * Adds [card] to this deck
     */
    operator fun plusAssign(card: T) = addCard(card).let { }

    /**
     * Adds [cards] to this deck
     */
    operator fun plusAssign(cards: Iterable<T>) = addCards(cards).let { }

    /**
     * Adds the cards from [deck] to this deck
     */
    operator fun plusAssign(deck: AbstractDeck<T>) = addDeck(deck).let { }

    /**
     * Removes [card] from the deck if it's there
     */
    open operator fun minusAssign(card: T) = remove(card).let { }

    /**
     * Draws a Card!
     * @throws DeckException if the Deck is Empty
     */
    @Throws(DeckException::class)
    open operator fun unaryMinus() = draw()

    /**
     * Returns an iterator over the elements of this object.
     */
    operator fun iterator() = deck.iterator()

    /**
     * checks if [card] is in this deck
     */
    operator fun contains(card: T) = card in deck

    /**
     * get the range from 0..[index]
     */
    operator fun rangeTo(index: Int) = deck.subList(0, index)

    /**
     * @see cutShuffle
     */
    open operator fun divAssign(cuts: Int) = cutShuffle(cuts)

    override fun equals(other: Any?): Boolean {
        return if (other is AbstractDeck<*> && size == other.size) {
            for (thisCard in deckOfCards) {
                @Suppress("UNCHECKED_CAST")
                if (thisCard !in other as AbstractDeck<T>) return false
            }
            true
        } else false
    }

    /**
     * Splits the deck into [cuts] decks, shuffles each of them, then shuffles the order oof them all, then puts them back together
     */
    @JvmOverloads
    open fun cutShuffle(cuts: Int = 2) {
        val tempDeck = splitInto(cuts)
        deckOfCards.clear()
        deckOfCards.addAll(tempDeck.shuffled().flatMap(List<T>::shuffled))
        deckShuffled()
    }

    /**
     * Cuts the deck in half then puts the bottom on the top
     */
    open fun cut() {
        val (top, bottom) = split()
        deckOfCards.clear()
        deckOfCards.addAll(listOf(bottom, top).flatten())
    }

    protected fun split() = deckOfCards.subList(0, size / 2).toList() to deckOfCards.subList(size / 2, size).toList()
    protected fun splitInto(cuts: Int) = deckOfCards.chunked(size / cuts)

    private fun <R, V> V.tryCatch(message: String?, block: (V) -> R) = try {
        block(this)
    } catch (e: Exception) {
        throw DeckException(message)
    }

    override fun hashCode(): Int = deckOfCards.hashCode()

    override fun toString(): String = "${this::class.simpleName}(size=$size, cards=$deck)"
}

@DslMarker
annotation class DeckMarker

@DslMarker
annotation class CardMarker

fun <T> Iterable<T>.toDeck(listener: (Deck.DeckListenerBuilder<T>.() -> Unit)? = null) = Deck(this, listener)
fun <T> Array<T>.toDeck(listener: (Deck.DeckListenerBuilder<T>.() -> Unit)? = null) = Deck(this.toList(), listener)

class Deck<T> : AbstractDeck<T> {

    constructor(cards: Iterable<T> = emptyList()) : super(cards)
    constructor(vararg cards: T) : super(*cards)

    constructor(vararg cards: T, listener: (DeckListenerBuilder<T>.() -> Unit)?) : this(*cards) {
        listener?.let(this::addDeckListener)
    }

    constructor(cards: Iterable<T>, listener: (DeckListenerBuilder<T>.() -> Unit)?) : this(cards) {
        listener?.let(this::addDeckListener)
    }

    constructor(cards: Iterable<T>, listener: DeckListener<T>) : this(cards) {
        addDeckListener(listener)
    }

    private var listener: DeckListener<T>? = null

    override fun cardAdded(vararg card: T) = listener?.onAdd(card.toList()) ?: Unit
    override fun cardDrawn(card: T, size: Int) = listener?.onDraw(card, size) ?: Unit
    override fun deckShuffled() = listener?.onShuffle() ?: Unit

    /**
     * Add a listener to this deck!
     */
    fun addDeckListener(listener: DeckListener<T>) {
        this.listener = listener
    }

    /**
     * Add a listener to this deck!
     */
    fun addDeckListener(listener: DeckListenerBuilder<T>.() -> Unit) {
        this.listener = DeckListenerBuilder.buildListener(listener)
    }

    /**
     * Adds a [DeckListener] to the deck
     */
    operator fun invoke(listener: DeckListenerBuilder<T>.() -> Unit) = addDeckListener(listener)

    companion object {
        /**
         * A default card_games.Deck of Playing Cards
         */
        @JvmStatic
        fun defaultDeck() = Deck(*Suit.values().map { suit -> (1..13).map { value -> Card(value, suit) } }.flatten().toTypedArray())

        /**
         * Create a deck by adding a card to it!
         */
        operator fun <T> plus(card: T) = Deck(card)
    }

    interface DeckListener<T> {
        /**
         * Listens to when cards are added to the deck
         */
        fun onAdd(cards: List<T>)

        /**
         * Listens to when the deck is shuffled
         */
        fun onShuffle()

        /**
         * Listens to when a card is drawn from the deck
         */
        fun onDraw(card: T, size: Int)
    }

    @DeckMarker
    class DeckListenerBuilder<T> private constructor() {

        private var drawCard: (T, Int) -> Unit = { _, _ -> }

        /**
         * Set the DrawListener
         */
        @DeckMarker
        fun onDraw(block: (card: T, size: Int) -> Unit) = apply { drawCard = block }

        private var addCards: (List<T>) -> Unit = {}

        /**
         * Set the AddCardListener
         */
        @DeckMarker
        fun onAdd(block: (List<T>) -> Unit) = apply { addCards = block }

        private var shuffleDeck: () -> Unit = {}

        /**
         * Set the ShuffleListener
         */
        @DeckMarker
        fun onShuffle(block: () -> Unit) = apply { shuffleDeck = block }

        private fun build() = object : DeckListener<T> {
            override fun onAdd(cards: List<T>) = addCards(cards)
            override fun onDraw(card: T, size: Int) = drawCard(card, size)
            override fun onShuffle() = shuffleDeck()
        }

        companion object {
            /**
             * Build a listener for the deck
             */
            @DeckMarker
            operator fun <T> invoke(block: DeckListenerBuilder<T>.() -> Unit) = buildListener(block)

            /**
             * Build a listener for the deck
             */
            @DeckMarker
            fun <T> buildListener(block: DeckListenerBuilder<T>.() -> Unit): DeckListener<T> = DeckListenerBuilder<T>().apply(block).build()
        }

    }

    @DeckMarker
    class DeckBuilder<T> private constructor() {

        private var deckListener: DeckListenerBuilder<T>.() -> Unit = {}

        /**
         * Set up the [DeckListener]
         */
        @DeckMarker
        fun deckListener(block: DeckListenerBuilder<T>.() -> Unit) = apply { deckListener = block }

        private val cardList = mutableListOf<T>()

        /**
         * The current cards that will be added to the deck
         */
        @CardMarker
        val cards: List<T>
            get() = cardList

        /**
         * Add a [Card] to the deck
         */
        @Suppress("unused")
        @CardMarker
        fun DeckBuilder<Card>.card(block: CardBuilder.() -> Unit) = apply { cardList.add(CardBuilder(block)) }

        /**
         * Add a [Card] to the deck
         */
        @Suppress("unused")
        @CardMarker
        fun DeckBuilder<Card>.card(value: Int, suit: Suit) = apply { cardList.add(Card(value, suit)) }

        /**
         * Add a [Card] to the deck
         */
        @Suppress("unused")
        @CardMarker
        fun DeckBuilder<Card>.card(vararg pairs: Pair<Int, Suit>) = apply { cardList.addAll(pairs.map { Card(it.first, it.second) }) }

        /**
         * Add cards to the deck
         */
        @CardMarker
        fun cards(vararg cards: T) = apply { cardList.addAll(cards) }

        /**
         * Add a deck to the deck
         */
        @CardMarker
        fun deck(deck: Deck<T>) = apply { cardList.addAll(deck.deckOfCards) }

        /**
         * Add cards to the deck
         */
        @CardMarker
        fun cards(cards: Iterable<T>) = apply { cardList.addAll(cards) }

        private fun build() = Deck(cardList, DeckListenerBuilder.buildListener(deckListener))

        companion object {
            /**
             * Build a deck using Kotlin DSL!
             */
            @DeckMarker
            operator fun <T> invoke(block: DeckBuilder<T>.() -> Unit) = buildDeck(block)

            /**
             * Build a deck using Kotlin DSL!
             */
            @DeckMarker
            fun <T> buildDeck(block: DeckBuilder<T>.() -> Unit) = DeckBuilder<T>().apply(block).build()
        }
    }
}

@DeckMarker
class CardBuilder {
    var value by Delegates.notNull<Int>()
    var suit by Delegates.notNull<Suit>()
    private fun build() = Card(value, suit)

    companion object {
        @CardMarker
        operator fun invoke(block: CardBuilder.() -> Unit) = cardBuilder(block)

        @CardMarker
        fun cardBuilder(block: CardBuilder.() -> Unit) = CardBuilder().apply(block).build()
    }
}