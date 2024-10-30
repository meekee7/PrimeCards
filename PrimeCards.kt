package primeCards

import java.util.LinkedList
import java.util.Queue
import kotlin.random.Random

fun getPrimes(max: Int) = (2..max).filter { candidate ->
    (2..<candidate).none { candidate % it == 0 }
}

val primes = getPrimes(200).toSet()

fun Int.isPrime() = this in primes

fun List<Int>.getOptions(curSum: Int) = this.filter { (it + curSum).isPrime() }

data class GameState(val sum: Int, val playerCards: List<List<Int>>) {
    fun playCard(pos: Int, cardValue: Int): GameState {
        val newSum = this.sum + cardValue
        val newList = this.playerCards.map { it.toMutableList() }.toMutableList()
        newList[pos].remove(cardValue)
        return GameState(newSum, newList)
    }

    companion object {
        val begin = GameState(0, listOf((1..10).toList(), (1..10).toList()))
    }
}

fun Int.other() = this xor 1

interface Player {
    fun play(ownPos: Int, state: GameState): Int
}

class HumanPlayer : Player {
    override fun play(ownPos: Int, state: GameState): Int {
        println(
            "Enter card value. Sum: ${state.sum}, Cards: ${state.playerCards[ownPos]}, Options: ${
                state.playerCards[ownPos].getOptions(
                    state.sum
                )
            }"
        )
        while (true) {
            val num = readln().toIntOrNull()
            val match = state.playerCards[ownPos].find { it == num }
            if (match != null)
                return match
            else
                println("Input invalid, please try again.")
        }
    }
}

class FirstPlayer : Player {
    override fun play(ownPos: Int, state: GameState): Int =
        state.playerCards[ownPos].getOptions(state.sum).first()
}

class RandomPlayer(val random: Random) : Player {
    override fun play(ownPos: Int, state: GameState): Int =
        state.playerCards[ownPos].getOptions(state.sum).random(random)
}

fun conductGame(players: List<Player>) {
    var gameState = GameState.begin
    var i = 0
    while (true) {
        val current = i % 2
        if (gameState.playerCards[current].getOptions(gameState.sum).isEmpty()) {
            println("Player ${current.other()} wins")
            return
        }
        val choice = players[current].play(current, gameState)
        gameState = gameState.playCard(current, choice)
        println("Player $current has chosen $choice. New sum: ${gameState.sum}")
        i++
    }
}

data class QueueEntry(val turn: Int, val gameState: GameState)
data class QueueEntryHistory(val entry: QueueEntry, val history: List<Int>)

class TreeNode(val choice: Int, val depth: Int, val children: MutableList<TreeNode>, val parent: TreeNode?) {
    fun isLeaf() = this.children.isEmpty()
    val isFirst = this.depth % 2 == 0
    val isSecond = this.depth % 2 == 1

    fun allDescendants(): List<TreeNode> {
        val result = mutableListOf<TreeNode>()
        result.add(this)
        this.children.map { it.allDescendants() }.forEach(result::addAll)
        return result
    }

    fun treeSize(): Int = 1 + this.children.sumOf { it.treeSize() }

    fun getChild(value: Int) = this.children.firstOrNull { it.choice == value }
}

fun analyzeGame() {
    val results = calcGamePaths()

    println("STATS:")
    println("Total plays: ${results.size}")
    println("Longest play: ${results.maxBy { it.size }}")
    println("Shortest play: ${results.minBy { it.size }}")
    println("Highest score: ${results.maxBy { it.sum() }.sum()}")
    println("Lowest score: ${results.minBy { it.sum() }.sum()}")

    val treeRoot = makeTree(results.toList())
    println("Total tree nodes: ${treeRoot.treeSize()}")

    println("ALL PLAYS:")
    results.forEach(::println)

    printTree(treeRoot)
}

private fun calcGamePaths(): MutableSet<List<Int>> {
    val queue: Queue<QueueEntryHistory> = LinkedList()
    queue.add(QueueEntryHistory(QueueEntry(0, GameState.begin), emptyList()))
    val visited = mutableSetOf<QueueEntry>()
    val results = mutableSetOf<List<Int>>()
    while (queue.isNotEmpty()) {
        val current = queue.poll()
        if (current.entry in visited)
            continue
        visited.add(current.entry)

        val options = current.entry.gameState.playerCards[current.entry.turn].getOptions(current.entry.gameState.sum)
        if (options.isEmpty()) {
            results.add(current.history)
            continue
        }

        val other = current.entry.turn.other()
        options.forEach { choice ->
            queue.add(
                QueueEntryHistory(
                    QueueEntry(other, current.entry.gameState.playCard(current.entry.turn, choice)),
                    current.history + choice
                )
            )
        }
    }
    return results
}

fun addToTree(sequence: List<Int>, parent: TreeNode) {
    if (sequence.isEmpty())
        return
    var node = parent.getChild(sequence.first())
    if (node == null) {
        node = TreeNode(sequence.first(), parent.depth + 1, mutableListOf(), parent)
        parent.children.add(node)
    }
    addToTree(sequence.subList(1, sequence.size), node)
}

fun TreeNode.color(): String {
    return when {
        this.depth == -1 -> "lightgrey"
        this.depth % 2 == 0 -> "lightblue"
        else -> "lightpink"
    }
}

fun TreeNode.printName(): String {
    if (this.depth == -1)
        return "Start"
    val player = if (this.depth % 2 == 0) "Jo" else "Georg"
    return "$player / ${this.choice}"
}

fun printTree(node: TreeNode) {
    //Generate Dot code for GraphViz

    val sb = StringBuilder()
    sb.appendLine("digraph G {")
    sb.appendLine("node [style = \"filled\"; fontname = \"Arial\"; colorscheme = ylgnbu3;];")

    node.allDescendants().forEach {
        sb.appendLine("\"${it.hashCode()}\" [label=\"${it.printName()}\"; fillcolor=\"${it.color()}\" ];")
    }
    node.allDescendants()

    printTreeNode(node, sb)

    sb.appendLine("}")

    println("GraphViz DOT Code:")
    println(sb)
}

fun printTreeNode(node: TreeNode, sb: StringBuilder) {
    node.children.forEach {
        sb.appendLine("\"${node.hashCode()}\" -> \"${it.hashCode()}\";")
        printTreeNode(it, sb)
    }
}

fun makeTree(sequences: List<List<Int>>): TreeNode {
    val root = TreeNode(-1, -1, mutableListOf(), null)
    sequences.forEach { addToTree(it, root) }
    return root
}

fun main() {
    //analyzeGame()

    val random = Random(System.currentTimeMillis())
    conductGame(
        listOf(
            RandomPlayer(random),
            RandomPlayer(random)
        )
    )
}
