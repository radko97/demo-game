package com.example.demogame

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.example.demogame.databinding.ActivityGameBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.random.Random


class GameActivity: AppCompatActivity(){

    //gives access to xml elements
    lateinit var binding: ActivityGameBinding

    //VARIABLES THAT TRACK CURRENT GAME VALUES
    var currentRound = 0
    private var cardOpen = false
    var timerRunning = false
    private var diceRolled = false
    var time = 0
    var currentLevel = -1
    //current number of jokers
    var jokerArray: IntArray = intArrayOf(0,0,0,0)
    lateinit var cardManager: CardManager
    private lateinit var currentCard: Card
    private lateinit var levelList: ArrayList<Int>
    private lateinit var gameDB: TinyDB
    private lateinit var mediaplayer: MediaPlayer
    private lateinit var stopwatch: StopWatch

    private var cardsLoaded = false

    //TODO: GAME PARAMETERS -> MOVE LATER

    //base number of jokers for init
    //player1joker, player1warps, player2jokers, player2warps
    private val jokerStartArray: IntArray = intArrayOf(1,1,1,1)

    //base rng arrays
    private val rngArrayDefault: IntArray = intArrayOf(1,2,3,4,5,6)
    private val rngArrayBigger: IntArray = intArrayOf(3,5,7,10,12,15)
    private val rngArrayHuge: IntArray = intArrayOf(5,7,10,14,18,20)

    //inner class for stopwatch
    inner class StopWatch: CountDownTimer((time*1000).toLong(), 1000){

        override fun onTick(millisRemaining: Long) {
            binding.timeCounterView?.text = (millisRemaining /1000).toString()
            time-=1
            if (millisRemaining in 4001..5000){
                mediaplayer!!.start()
            }
        }
        override fun onFinish() {
            Log.d("msg","time over")
            time=0
            timerRunning=false
        }
    }

    private fun showFailedLoadPopup(){
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val view =layoutInflater.inflate(R.layout.popup_couldntcloadcards,null)
        builder.setView(view)
        builder.setCanceledOnTouchOutside(false)
        val buttonRetry = view.findViewById<Button>(R.id.buttonRetry)
        buttonRetry.setOnClickListener {
            loadCardsFromServer()
            builder.dismiss()
        }
        builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize TinyDB and CardManager
        gameDB = TinyDB(applicationContext)
        cardManager = gameDB.getObject("manager", CardManager::class.java) as CardManager
        levelList = ArrayList(gameDB.getListInt("levels").sorted())

        // Load the cards in the background thread
        loadCardsFromServer()

        // Initialize binding
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set player names
        binding.player1Textgame!!.text = cardManager.player1name
        binding.player2Textgame!!.text = cardManager.player2name

        // Initialize level list
        currentLevel = levelList[0]
        setVisibilityForLevelViews()

        // Set initial visibility for various views
        binding.stopWatch!!.visibility = View.GONE
        binding.timeCounterView!!.visibility = View.GONE
        binding.diceView!!.visibility = View.GONE
        binding.diceAmountView!!.visibility = View.GONE
        binding.joker1View!!.visibility = View.GONE
        binding.joker2View!!.visibility = View.GONE
        binding.jokers1textView!!.visibility = View.GONE
        binding.jokers2textView!!.visibility = View.GONE
        binding.timeWarp1!!.visibility = View.GONE
        binding.timeWarp2!!.visibility = View.GONE
        binding.timeWarpsTextView1!!.visibility = View.GONE
        binding.timeWarpsTextView2!!.visibility = View.GONE

        // Initialize jokers
        for (i in jokerStartArray.indices) {
            repeat(jokerStartArray[i]) {
                addJoker(i)
            }
        }
        // Set click listeners for jokers
        binding.joker1View!!.setOnClickListener { useJoker(0) }
        binding.jokers1textView!!.setOnClickListener { useJoker(0) }
        binding.timeWarp1!!.setOnClickListener { useJoker(1) }
        binding.timeWarpsTextView1!!.setOnClickListener { useJoker(1) }
        binding.joker2View!!.setOnClickListener { useJoker(2) }
        binding.jokers2textView!!.setOnClickListener { useJoker(2) }
        binding.timeWarp2!!.setOnClickListener { useJoker(3) }
        binding.timeWarpsTextView2!!.setOnClickListener { useJoker(3) }
    }

    private fun loadCardsFromServer() {
        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                cardManager.loadCards(levelList)
            }

            if (success) {
                // Update the UI with the loaded cards (e.g., populate a RecyclerView)
                Log.d("TAG", "Succesfully loaded cards")
                startGame()
            } else {
                showFailedLoadPopup()
                Log.d("TAG", "Could not load cards")
            }
        }
    }

    private fun startGame() {
        cardsLoaded = true
        // Initialize values with first card
        cardOpen = false
        currentCard = cardManager.pullCard(currentLevel)
        time = currentCard.time
        binding.cardTextView!!.text = currentCard.text
    }

    private fun setVisibilityForLevelViews() {
        // Set visibility for level views
        if (currentLevel != 0) binding.level0View!!.visibility = View.GONE
        if (currentLevel != 1) binding.level1View!!.visibility = View.GONE
        if (currentLevel != 2) binding.level2View!!.visibility = View.GONE
        if (currentLevel != 3) binding.level3View!!.visibility = View.GONE
    }

    //overwrite back button to ask if game should really end (onBackPressed)

    override fun onBackPressed() {
        //pause time during popup
        if (timerRunning) {
            pauseStopwatch()
        }

        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val view =layoutInflater.inflate(R.layout.popup_leavegame,null)
        builder.setView(view)
        //builder.setCanceledOnTouchOutside(false)
        val buttonLeave = view.findViewById<Button>(R.id.buttonLeaveYes)
        val buttonStay = view.findViewById<Button>(R.id.buttonStayNo)
        buttonLeave.setOnClickListener {
            builder.dismiss()
            super.onBackPressed()
        }
        buttonStay.setOnClickListener {
            builder.dismiss()
            //resume time
            if (timerRunning) resumeStopwatch()
        }

        builder.show()
    }

    //click handler functions

    //open rule book popup
    fun openRules(view: View){
        if (timerRunning) pauseStopwatch()
        val showPopUp = PopUpFragmentRules()
        showPopUp.show(supportFragmentManager,"showPopUp")

        // Listen for when the popup closes
        supportFragmentManager.setFragmentResultListener("popupClosed", this) { _, _ ->
            resumeStopwatch()  // Call your function
        }
    }

    // Open the next card
    fun turnCard(view: View){

        if (!cardsLoaded){
            Toast.makeText(this, "Loading cards... please hold on!",Toast.LENGTH_LONG).show()
            Timer().schedule(timerTask {
                if (cardsLoaded) runOnUiThread { turnCard(view) } // If the cards get loaded after delay, actually turn the card
            },
                2000)
            return
        }

        if (!cardOpen){
            cardOpen = true
            if (time>0){
                showStopwatch()
            }
        }
        view.visibility = View.GONE
        if (currentCard.rng > 0){
            showDice()
        }
    }



    fun finishRound(view: View){

        // Don't do anything if card is not open
        if (!cardOpen){
            Toast.makeText(this, "Tap card to view task!",Toast.LENGTH_LONG).show()
            return
        }

        //check popup if timer>0 -> time still running or not over
        if (time > 0){
            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
            val popupView =layoutInflater.inflate(R.layout.popup_skiptimer,null)
            builder.setView(popupView)
            //builder.setCanceledOnTouchOutside(false)
            val buttonLeave = popupView.findViewById<Button>(R.id.buttonYesNextRound)
            val buttonStay = popupView.findViewById<Button>(R.id.buttonStayInRound)
            buttonLeave.setOnClickListener {
                builder.dismiss()
                actuallyFinishRound()
            }
            buttonStay.setOnClickListener {
                builder.dismiss()
            }

            builder.show()
        }
        else{
            actuallyFinishRound()
        }
    }
    //Actual next round functionality
    fun actuallyFinishRound(){

        // Handle jokers from previous round
        handleJokerCode(currentCard.jokerCode)

        binding.cardBack?.visibility = View.VISIBLE
        //note that card was put on backside
        cardOpen = false
        //allow dice to be rolled again and hide them
        diceRolled = false
        hideDice()

        // Hide stopwatch and set timer to not running
        hideStopwatch()

        // Re-init media player
        mediaplayer = MediaPlayer.create(this, R.raw.countdown5)

        //if round has been finished despite ticking time
        if (timerRunning){
            pauseStopwatch()
        }

        //count up round
        currentRound += 1
        countUpLevel()

        // Load here new card from database
        currentCard = cardManager.pullCard(currentLevel)
        if (currentCard.id == 0){
            Log.d("Tag", "No more fitting cards on this level")
            // Try switching the level
            if (currentLevel != levelList.last()){
                currentLevel = levelList.indexOf(levelList[currentLevel] + 1)
                Log.d("TAG", "Counted up level")
                setVisibilityForLevelViews()
            }
            else{
                Log.d("TAG","No more fitting cards.") //TODO: additional handling?
            }
        }
        time = currentCard.time
        binding.cardTextView!!.text = currentCard.text

    }

    private fun countUpLevel(){
        // If necessary count up the level

        val levelsInPlay = levelList.size

        // TODO: allow short, middle and long games (settings)
        val totalRounds = 20

        val roundsPerLevel = totalRounds / levelsInPlay

        if (currentRound == totalRounds){
            // Prompt to finish game, but allow playing further
            finishGame()
            return
        }
        if (currentRound < totalRounds && currentRound % roundsPerLevel == 0){
            if (currentLevel != levelList.last()){
                currentLevel = levelList.indexOf(levelList[currentLevel] + 1)
                Log.d("TAG", "Counted up level")
                setVisibilityForLevelViews()
            }
            else{
                Log.d("TAG", "Already at highest level")
            }

        }

    }

    private fun finishGame(){
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val view =layoutInflater.inflate(R.layout.popup_leavegame,null)
        builder.setView(view)
        //builder.setCanceledOnTouchOutside(false)
        val buttonLeave = view.findViewById<Button>(R.id.buttonLeaveYes)
        val buttonStay = view.findViewById<Button>(R.id.buttonStayNo)
        buttonLeave.setOnClickListener {
            builder.dismiss()
            super.onBackPressed()
        }
        buttonStay.setOnClickListener {
            builder.dismiss()
        }

        builder.show()
    }


    //STOPWATCH HANDLERS

    fun showStopwatch(){
        binding.stopWatch?.visibility = View.VISIBLE
        binding.timeCounterView?.text = time.toString()
        binding.timeCounterView?.visibility = View.VISIBLE
    }

    private fun hideStopwatch(){
        if (timerRunning){
            timerRunning = false
            stopwatch.cancel()
        }
        binding.timeCounterView?.visibility = View.GONE
        binding.stopWatch?.visibility = View.GONE
    }

    fun startStopwatch(view: View){

        //only start a timer if it's not already running
        if (timerRunning){
            return
        }
        timerRunning=true
        stopwatch = StopWatch()
        mediaplayer = MediaPlayer.create(this, R.raw.countdown5)
        stopwatch.start()
    }

    fun pauseStopwatch(){
        stopwatch.cancel()
    }

    fun resumeStopwatch(){
        stopwatch = StopWatch()
        stopwatch.start()
    }

    //DICE HANDLERS
    private fun showDice() {
        binding.diceView!!.visibility = View.VISIBLE
    }

    private fun hideDice(){
        binding.diceView!!.visibility = View.GONE
        binding.diceAmountView!!.visibility = View.GONE
    }

    fun rollDice(view: View){

        //if dice already rolled in this round, cant roll again
        if (diceRolled){
             return
        }
        diceRolled=true

        var rngArray: IntArray = intArrayOf()
        if (currentCard.rng == 1) rngArray = rngArrayDefault
        else if (currentCard.rng == 2) rngArray = rngArrayBigger
        else if (currentCard.rng == 3) rngArray = rngArrayHuge

        binding.diceAmountView?.visibility=View.VISIBLE
        object : CountDownTimer(2400, 200){
            override fun onTick(millisRemaining: Long) {
                binding.diceAmountView?.text = rngArray[Random(System.currentTimeMillis()).nextInt(rngArray.size)].toString()

            }
            override fun onFinish() {
                Log.d("TAG","Dice roll finished")
                binding.diceAmountView?.text = rngArray[Random(System.currentTimeMillis()).nextInt(rngArray.size)].toString()

            }
        }.start()

    }

    //JOKER HANDLERS
    //which is from 0,1,2,3 and gives which joker is being added/ used
    fun addJoker(which: Int){
        //joker1
        if (which==0){
            jokerArray[0]++
            binding.jokers1textView?.text = jokerArray[0].toString()
            binding.jokers1textView?.setTextColor(getColor(R.color.backgroundcolorrecyclerpicked))
            Timer().schedule(timerTask { binding.jokers1textView?.setTextColor(getColor(R.color.black))}, 2000)

            //if this is the first joker added for player1
            if (jokerArray[0]==1){
                val params = binding.joker1View?.layoutParams as ConstraintLayout.LayoutParams
                params.startToStart = binding.player1Textgame!!.id
                //...and there is no time warp
                if (jokerArray[1]==0){
                    //...set jokers to top of player1
                    params.bottomToTop = binding.player1Textgame!!.id
                }
                //there is already a time warp
                else{ //set position to top of the time warp
                    params.bottomToTop = binding.timeWarp1!!.id
                }
                binding.joker1View!!.requestLayout()
                binding.joker1View!!.visibility = View.VISIBLE
                binding.jokers1textView!!.requestLayout()
                binding.jokers1textView!!.visibility = View.VISIBLE
            }
        }
        //timewarp1
        if (which==1){
            jokerArray[1]++
            binding.timeWarpsTextView1?.text = jokerArray[1].toString()
            binding.timeWarpsTextView1?.setTextColor(getColor(R.color.backgroundcolorrecyclerpicked))
            Timer().schedule(timerTask { binding.timeWarpsTextView1?.setTextColor(getColor(R.color.black))}, 2000)


            //if this was the first time warp for player1
            if (jokerArray[1]==1){

                val params = binding.timeWarp1?.layoutParams as ConstraintLayout.LayoutParams
                params.startToStart = binding.player1Textgame!!.id
                //...and there is no joker
                if (jokerArray[0]==0){
                    //...set time warps to top of player1
                    params.bottomToTop = binding.player1Textgame!!.id
                }
                //there is already a joker
                else{ //set to top of that joker
                    params.bottomToTop = binding.joker1View!!.id
                }
                binding.timeWarp1!!.requestLayout()
                binding.timeWarp1!!.visibility = View.VISIBLE
                binding.timeWarpsTextView1!!.requestLayout()
                binding.timeWarpsTextView1!!.visibility = View.VISIBLE
            }
        }

        //joker2
        if (which==2){
            jokerArray[2]++
            binding.jokers2textView?.text = jokerArray[2].toString()
            binding.jokers2textView?.setTextColor(getColor(R.color.backgroundcolorrecyclerpicked))
            Timer().schedule(timerTask { binding.jokers2textView?.setTextColor(getColor(R.color.black))}, 2000)

            //if this is the first joker added for player2
            if (jokerArray[2]==1){
                val params = binding.joker2View?.layoutParams as ConstraintLayout.LayoutParams
                params.startToStart = binding.player2Textgame!!.id
                //...and there is no time warp for player 2
                if (jokerArray[3]==0){
                    //...set jokers to top of player2
                    params.bottomToTop = binding.player2Textgame!!.id
                }
                //there is already a time warp
                else{ //set position to top of the time warp
                    params.bottomToTop = binding.timeWarp2!!.id
                }
                binding.joker2View!!.requestLayout()
                binding.joker2View!!.visibility = View.VISIBLE
                binding.jokers2textView!!.requestLayout()
                binding.jokers2textView!!.visibility = View.VISIBLE
            }
        }
        //timewarp2
        if (which==3){
            jokerArray[3]++
            binding.timeWarpsTextView2?.text = jokerArray[3].toString()
            binding.timeWarpsTextView2?.setTextColor(getColor(R.color.backgroundcolorrecyclerpicked))
            Timer().schedule(timerTask { binding.timeWarpsTextView2?.setTextColor(getColor(R.color.black))}, 2000)

            //if this was the first time warp for player2
            if (jokerArray[3]==1){

                val params = binding.timeWarp2?.layoutParams as ConstraintLayout.LayoutParams
                params.startToStart = binding.player2Textgame!!.id
                //...and there is no joker for player2
                if (jokerArray[2]==0){
                    //...set time warps to top of player1
                    params.bottomToTop = binding.player2Textgame!!.id
                }
                //there is already a joker
                else{ //set to top of that joker
                    params.bottomToTop = binding.joker2View!!.id
                }
                binding.timeWarp2!!.requestLayout()
                binding.timeWarp2!!.visibility = View.VISIBLE
                binding.timeWarpsTextView2!!.requestLayout()
                binding.timeWarpsTextView2!!.visibility = View.VISIBLE
            }
        }

    }

    fun useJoker(which: Int){
        if (jokerArray[which]==0){
            throw java.lang.Exception("It shouldn't be possible to use this joker")
        }

        if (!cardOpen){
            Toast.makeText(this, "You can't use a joker before seeing the task!", Toast.LENGTH_SHORT).show()
            return
        }

        fun actuallyUseJoker(which: Int){
            jokerArray[which]--
            //jokers of this type dropped to 0 -> hide it
            if (jokerArray[which]==0){
                when (which) {
                    0 -> {
                        binding.joker1View!!.visibility = View.GONE
                        binding.jokers1textView!!.visibility= View.GONE
                    }
                    1 -> {
                        binding.timeWarp1!!.visibility = View.GONE
                        binding.timeWarpsTextView1!!.visibility = View.GONE
                    }
                    2 -> {
                        binding.joker2View!!.visibility = View.GONE
                        binding.jokers2textView!!.visibility= View.GONE
                    }
                    3 -> {
                        binding.timeWarp2!!.visibility = View.GONE
                        binding.timeWarpsTextView2!!.visibility= View.GONE
                    }
                }
            }
            //still jokers -> only update text
            else{
                when (which) {
                    0 -> {
                        binding.jokers1textView!!.text = jokerArray[which].toString()
                    }
                    1 -> {
                        binding.timeWarpsTextView1!!.text = jokerArray[which].toString()
                    }
                    2 -> {
                        binding.jokers2textView!!.text = jokerArray[which].toString()
                    }
                    3 -> {
                        binding.timeWarpsTextView2!!.text = jokerArray[which].toString()
                    }
                }

            }
        }

        //open joker...
        if (which==0 || which==2){

            //pause during popup since user could change their mind
            if (timerRunning) {
                pauseStopwatch()
            }

            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()

            val view =layoutInflater.inflate(R.layout.popup_usejoker,null)
            builder.setView(view)
            //builder.setCanceledOnTouchOutside(false)
            val buttonUse = view.findViewById<Button>(R.id.buttonYesUseJoker)
            val buttonDismiss = view.findViewById<Button>(R.id.buttonNoDontUseJoker)
            buttonUse.setOnClickListener {
                actuallyUseJoker(which)
                builder.dismiss()

                //Finish the round if joker has been used
                actuallyFinishRound()
            }
            buttonDismiss.setOnClickListener {
                builder.dismiss()
                if (timerRunning) {
                    resumeStopwatch()
                }

            }
            builder.show()
        }
        //..or time warp pop up
        else if (which==1 || which==3){

            //if time=0 send toast you cant use

            if (time==0) {
                Toast.makeText(
                    this,
                    "You can only use time warps on cards with a time!",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            //if timer already running, send toast you cant use while timer is running
            if (timerRunning){
                Toast.makeText(
                    this,
                    "You can't use time warp after time has been started!",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
            val view =layoutInflater.inflate(R.layout.popup_usetimewarp,null)
            builder.setView(view)
            //builder.setCanceledOnTouchOutside(false)
            val buttonDouble = view.findViewById<Button>(R.id.buttonYesDoubleTime)
            val buttonHalve = view.findViewById<Button>(R.id.buttonYesHalveTime)
            val buttonDismiss = view.findViewById<Button>(R.id.buttonNoDontUseJoker)
            buttonDouble.setOnClickListener {
                actuallyUseJoker(which)
                time*=2
                //update stopwatch
                showStopwatch()
                builder.dismiss()
            }
            buttonHalve.setOnClickListener {
                actuallyUseJoker(which)
                time/=2
                //update stopwatch
                showStopwatch()
                builder.dismiss()
            }
            buttonDismiss.setOnClickListener {
                builder.dismiss()
            }
            builder.show()
        }


    }
    // 1 -> joker for player1
    // 2 -> timewarp for player1
    // 3 -> challenge for a joker
    // 4 -> challenge for a timewarp
    // 5 -> player1 gets joker with challenge
    // 6 -> player1r gets timewarp with challenge
    // negative number means for player2. for reversible cards only.
    private fun handleJokerCode(code: Int) {
        when (code) {
            0 -> return
            1 -> addJoker(0)
            2 -> addJoker(1)
            -1 -> addJoker(2)
            -2 -> addJoker(3)
            3, 4 -> handleJokerCodeWhoGetsJoker(code)
            5 -> handleJokerCodeConfirmGetJoker(0)
            6 -> handleJokerCodeConfirmGetJoker(1)
            -5 -> handleJokerCodeConfirmGetJoker(2)
            -6 -> handleJokerCodeConfirmGetJoker(3)
        }
    }

    private fun handleJokerCodeWhoGetsJoker(code: Int) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val view = layoutInflater.inflate(R.layout.popup_whogetsjoker, null)
        builder.setView(view)

        val buttonOne: Button = view.findViewById(R.id.buttonPlayer1GetsJoker)
        val buttonTwo: Button = view.findViewById(R.id.buttonPlayer2GetsJoker)
        val buttonNoOne: Button = view.findViewById(R.id.buttonNoOneGetsJoker)

        buttonOne.text = cardManager.player1name
        buttonTwo.text = cardManager.player2name

        buttonOne.setOnClickListener {
            if (code == 3) addJoker(0)
            if (code == 4) addJoker(1)
            builder.dismiss()
        }

        buttonTwo.setOnClickListener {
            if (code == 3) addJoker(2)
            if (code == 4) addJoker(3)
            builder.dismiss()
        }

        buttonNoOne.setOnClickListener {
            builder.dismiss()
        }

        builder.show()
    }

    private fun handleJokerCodeConfirmGetJoker(which: Int) {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog).create()
        val view = layoutInflater.inflate(R.layout.popup_getjoker, null)
        builder.setView(view)

        val buttonGet: Button = view.findViewById(R.id.buttonYesGetJoker)
        val buttonDontGet: Button = view.findViewById(R.id.buttonNoDontGetJoker)

        buttonGet.setOnClickListener {
            addJoker(which)
            builder.dismiss()
        }

        buttonDontGet.setOnClickListener {
            builder.dismiss()
        }

        builder.show()
    }


}
