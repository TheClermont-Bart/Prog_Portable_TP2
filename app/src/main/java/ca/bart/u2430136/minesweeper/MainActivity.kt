package ca.bart.u2430136.minesweeper

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.core.view.children
import ca.bart.u2430136.minesweeper.databinding.ActivityMainBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cell(var exposed: Boolean = false, var flag: Boolean = false, var isMine: Boolean = false) : Parcelable

@Parcelize
data class Model(val grid: Array<Cell>) : Parcelable

class MainActivity : Activity() {

    companion object {
        const val TAG = "MainActivity"
        const val NB_COLUMNS = 10
        const val NB_ROWS = 10
        const val KEY_MINES = "Mines"
        const val KEY_MODEL = "Model"
    }

    var mines = 10
        get() = field
        set(value) {
            field = value
            refresh()
        }


    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    var model = Model(Array(NB_COLUMNS * NB_ROWS) { Cell() })

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            mines = savedInstanceState.getInt(KEY_MINES, 10)
            (savedInstanceState.getParcelable(KEY_MODEL) as? Model)?.let {
                model = it
            }
        }

        binding.newGame.setOnClickListener {
            newGame()
        }

        binding.grid.children.forEachIndexed { index:Int, button: View ->

            button.setOnClickListener {
                onButtonClicked(index)
            }

            button.setOnLongClickListener {
                onButtonLongClicked(index)
                true // prevents regular click
            }
        }
        placeMines()
        refresh()
    }

    fun onButtonLongClicked(index: Int){
        val (x, y) = index.toCoords()
        Log.d(TAG, "onButtonClicked(index=$index, x=$x, y=$y)")

        if (model.grid[index].flag){
            model.grid[index].flag = false
            return}else if(!model.grid[index].flag){
            model.grid[index].flag = true}

        //val howManyExposedNeighbors = getNeighbors(index).count { model.grid[it].flag }
        //Log.d(TAG, "howManyExposedNeighbors = $howManyExposedNeighbors")

        refresh()
    }

    fun newGame(){
        model = Model(Array(NB_COLUMNS * NB_ROWS) { Cell() })
        mines = 10
        placeMines()
        refresh()
    }

    fun placeMines(){
        val random = java.util.Random()
        var minesPlaced = 0

        while (minesPlaced < mines) {
            val randomIndex = random.nextInt(NB_COLUMNS * NB_ROWS)
            if (!model.grid[randomIndex].isMine) {
                model.grid[randomIndex].isMine = true
                minesPlaced++
            }
        }
    }

    fun onButtonClicked(index:Int) {

        val (x, y) = index.toCoords()
        Log.d(TAG, "onButtonClicked(index=$index, x=$x, y=$y)")

        if (model.grid[index].exposed)
            return

        if(model.grid[index].flag)
            return

        model.grid[index].exposed = true




        val howManyExposedNeighbors = getNeighbors(index).count { model.grid[it].exposed }
        Log.d(TAG, "howManyExposedNeighbors = $howManyExposedNeighbors")


        getNeighbors(index).forEach { onButtonClicked(it) }

        refresh()
    }

    private fun getNeighbors(index: Int): List<Int> {

        val (x, y) = index.toCoords()
        return listOf(
            Pair(x - 1, y - 1),
            Pair(x, y - 1),
            Pair(x + 1, y - 1),
            Pair(x - 1, y),
            Pair(x + 1, y),
            Pair(x - 1, y + 1),
            Pair(x, y + 1),
            Pair(x + 1, y + 1)
        ).mapNotNull { it.toIndex() }
    }


    fun refresh() {

        binding.minesCounter.text = getString(R.string.mines_count, mines)

        (binding.grid.children zip model.grid.asSequence()).forEach { (button, cell) ->

            button.setBackgroundResource(
                if (cell.exposed && cell.isMine)
                    R.drawable.btn_down_mine
                else if (cell.exposed)
                    R.drawable.btn_down
                else if (cell.flag)
                    R.drawable.btn_flag
                else R.drawable.btn_up
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(KEY_MINES, mines)

        outState.putParcelable(KEY_MODEL,model)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        mines = savedInstanceState.getInt(KEY_MINES, 0)

        (savedInstanceState.getParcelable(KEY_MODEL) as? Model)?.let {
            model = it
        }

    }

    private fun Int.toCoords() = Pair(this % NB_COLUMNS, this / NB_COLUMNS)

    private fun Pair<Int, Int>.toIndex() =
        if (this.first < 0 || this.first >= NB_COLUMNS ||
            this.second < 0 || this.second >= NB_ROWS)
            null
        else
            this.second * NB_COLUMNS + this.first


}