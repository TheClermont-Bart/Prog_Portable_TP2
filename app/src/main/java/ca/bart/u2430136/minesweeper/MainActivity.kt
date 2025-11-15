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
data class Cell(var exposed: Boolean = false, var flag: Boolean = false) : Parcelable

@Parcelize
data class Model(val grid: Array<Cell>) : Parcelable

class MainActivity : Activity() {

    companion object {
        const val TAG = "MainActivity"
        const val NB_COLUMNS = 10
        const val NB_ROWS = 10
        const val KEY_MINES = "Mines"
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


        binding.grid.children.forEachIndexed { index:Int, button: View ->

            button.setOnClickListener {
                onButtonClicked(index)
            }

            button.setOnLongClickListener {
                onButtonLongClicked(index)
                true // prevents regular click
            }
        }

        refresh()
    }

    fun onButtonLongClicked(index: Int){
        val (x, y) = index.toCoords()
        Log.d(TAG, "onButtonClicked(index=$index, x=$x, y=$y)")

        if (model.grid[index].flag)
            return

        model.grid[index].flag = true

        val howManyExposedNeighbors = getNeighbors(index).count { model.grid[it].flag }
        Log.d(TAG, "howManyExposedNeighbors = $howManyExposedNeighbors")


        //getNeighbors(index).forEach { onButtonClicked(it) }


        mines--
        refresh()
    }

    fun onButtonClicked(index:Int) {

        val (x, y) = index.toCoords()
        Log.d(TAG, "onButtonClicked(index=$index, x=$x, y=$y)")

        if (model.grid[index].exposed)
            return

        model.grid[index].exposed = true




        val howManyExposedNeighbors = getNeighbors(index).count { model.grid[it].exposed }
        Log.d(TAG, "howManyExposedNeighbors = $howManyExposedNeighbors")


        //getNeighbors(index).forEach { onButtonClicked(it) }


        mines--
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
                if (cell.exposed)
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
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        mines = savedInstanceState.getInt(KEY_MINES, 0)
    }

    private fun Int.toCoords() = Pair(this % NB_COLUMNS, this / NB_COLUMNS)

    private fun Pair<Int, Int>.toIndex() =
        if (this.first < 0 || this.first >= NB_COLUMNS ||
            this.second < 0 || this.second >= NB_ROWS)
            null
        else
            this.second * NB_COLUMNS + this.first


}