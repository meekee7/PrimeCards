# PrimeCards

Implementation of the card game Prime Cards.

# Game Rules

 1. There are two players. Each player is given a hand of 10 cards, numbered 1 to 10.
 1. The first player puts a prime-numbered card from his hand on the table, creating the card stack.
 1. The second player now puts one of his cards on the stack. The chosen card must keep _the sum of all stack cards_ a prime number. The selected card itself need not be prime.
 1. The players now take turns playing their cards as in the previous step, always keeping the stack sum a prime number.
 1. The first player unable to make a valid move loses. The other player wins.
 
Prime numbers are defined as usual: a natural number with exactly two divisors, 1 and itself. 0 and 1 are not prime numbers.

## Example game

| Player | Played | Stack Sum |
|--------|--------|-----------|
| Alice  | 7      | 7         |
| Bob    | 6      | 13        |
| Alice  | 6      | 19        |
| Bob    | 10     | 29        |
| Alice  | 2      | 31        |

Bob has no moves left. Alice wins.


# Analysis and Strategy

## Basics Facts
 - There are 108 game paths in total.
   - 41 lead to a win for the first player.
   - 67 lead to a win for the second player.
 - The longest game has 11 plys.
 - The shortest game has 4 plys.  
 - Including a starting node, the game tree has a total of 397 nodes.
 - The highest final game score is 67.
 - The lowest final game score is 17.

## Forced win

The first player can force a win.
1. Alice starts the game with the 5.
2. If Bob answers that 5 with a 2, then Alice must _not_ respond to that with a 6.
3. In all other situations, Alice can play any card valid in that moment. All remaining paths will lead to her victory.

Conversely this means that there is no general forced winning strategy for the second player. However, in some specific game situations, Bob can indeed force a win.

# Origin

Prime Cards is adapted from a minigame in the adventure game "Chaos am Set".
