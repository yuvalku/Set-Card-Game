# Set-Card-Game

## Motivation
This project presents an implementation of the fast-paced multiplayer card game 'Set'. Developed in Java, this code underscores our comprehension of multi-threaded programming. Each player and the dealer are executed as autonomous threads, demonstrating our proficiency in managing concurrent operations.
Considerable focus has been placed on ensuring concurrent safety and correctness. Given the shared resources among different threads, attention was paid to employing a variety of techniques to handle potential concurrency issues effectively.

## Overview
In this project we implemented a modified version of the game “Set”.
A description of the original game can be found here: [Set Card Game](https://en.wikipedia.org/wiki/Set_(card_game)). <br/>
You can also watch this [video](https://www.youtube.com/watch?v=NzXDfSFQ1c0) for a more intuitive understanding of the game.
However, keep in mind that we use slightly different rules in this implementation.
### Our version of the game
The game contains a deck of 81 cards. Each card contains a drawing with four features (color,
number, shape, shading). <br/>
The game starts with 12 drawn cards from the deck that are placed on a 3x4 grid on the table. <br/>
The goal of each player is to find a combination of three cards from the cards on the table that
are said to make up a “legal set”. <br/>
A “legal set” is defined as a set of 3 cards, that for each one of the four features — color,
number, shape, and shading — the three cards must display that feature as either: <br/> (a) all the
same, or: <br/> (b) all different. <br/> In other words, for each feature the three cards must avoid having
two cards showing one version of the feature and the remaining card showing a different
version. <br/>
The possible values of the features are:
+ The color: red, green or purple.
+ The number of shapes: 1, 2 or 3.
+ The geometry of the shapes: squiggle, diamond or oval.
+ The shading of the shapes: solid, partial or empty.

The players play together simultaneously on the table, trying to find a legal set of 3 cards. They
do so by placing tokens on the cards, and once they place the third token, they should ask the
dealer to check if the set is legal. <br/>
If the set is not legal, the player gets a penalty, freezing his ability of removing or placing his
tokens for a specified time period. <br/>
If the set is a legal set, the dealer will discard the cards that form the set from the table, replace
them with 3 new cards from the deck and give the successful player one point. In this case the
player also gets frozen although for a shorter time period. <br/>
To keep the game more interesting and dynamic, and in case no legal sets are currently available
on the table, once every minute the dealer collects all the cards from the table, reshuffles the
deck and draws them anew. <br/>
The game will continue for as long as there is a legal set to be found in the remaining cards (that are
either on table or in the deck). When there is no legal set left, the game will end and the player
with the most points will be declared as the winner! <br/>
Each player controls 12 unique keys on the keyboard as follows. The default keys are:
+ Player A: 

| Q | W | E | R |
| - | - | - | - |
| A | S | D | F |
| Z | X | C | V |

+ Player B:

| U | I | O | P |
| - | - | - | - |
| J | K | L | ; |
| M | , | . | / |

The keys layout is the same as the table's cards slots (3x4), and each key press dispatches the
respective player’s action, which is either to place or remove a token from the card in that slot -
if a card is not present/present there.


## How To Run
This project uses maven as its build tool. Make sure you have it installed on your machine. <br/>
To run, simply navigate to the Set-Card-Game directory through the terminal and run **mvn clean compile exec:java**