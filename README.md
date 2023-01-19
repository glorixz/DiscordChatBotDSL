# CPSC 410 Group Project: Discord Bot DSL

Contributors: Gloria Zhang, Heyisen Li, Catherine Li, Sam Hsu

The DSL is designed for Discord users with some programming experience who want to create their own Discord Bot. The domain is on creating inputs and responses between users and the Bot.

## DSL specification

### Logical flow of the program
The first part of any Discord Bot program consists of the defined behaviour. Bot creators define the behaviour through language features such as _CONVO_, _COUNTER_ and _INPUT_ variables, and _WHILE_ loops. The latter part of the program must have at least one _TRIGGERPHRASE_ to enable conversations between the Bot and users.

### Language features
The DSL interacts with one user at a time, and doesn't remember previous interactions. The **user** is the Discord member who's currently having a conversation with the Bot. In other words, the user is the member who sent the trigger phrase.

The language has the following features:
1. Send users content, including
    1. Simple messages
    2. Question-response sequences called _PROMPT_
2. Set variables of type _COUNTER_ (integers) and _INPUT_ (_PROMPT_ responses)
3. _WHILE loops_ that check for true/false conditions
4. _IF, ELSEIF, and ELSE_ statements
5. Create _TRIGGERPHRASEs_ to start conversations
6. Define repeatable conversations with _CONVOs_, and start them using START
7. Access metadata about the user

#### Sending messages
To send an simple message:
```
BOT: "Hello!"
```

To mention the user:
```
BOT: "Hello!" + @User
```

Messages can also incorporate _COUNTER_ and _INPUT_ variables:
```
BOT: num + " is the number I want " + @User
```

#### Sending a question and saving user response in INPUT variables
The Bot can ask users a question and gather their response.

```
BOT: "What's your DOB?"
USER: input_name
BOT: "Your DOB is " + input_name
```

In the example above, the Bot prompts the user for their DOB, and replies back with the DOB. 

BOT defines a question that the Bot sends to the user. To receive the user's response and store it in an _INPUT_ variable, creators must define `USER: variable_name` immediately after a BOT. If `USER:` is not defined, the previous question will be taken as a regular message.

#### COUNTER variables
_COUNTER_ are integers. Bot creators can initialize _COUNTER_ with a starting value. Creators can also INCREMENT and DECREMENT _COUNTER_ variables by 1.

```
COUNTER num = 5
INCREMENT num
```

#### WHILE loops
WHILE loops are used to repeat actions depending on conditions that evaluate to true or false.

The conditions within WHILE loops have the following keywords:
1. For logical groupings 
    1. AND
    2. OR
2. For single condition
    1. IS
    2. NOT
3. Boolean operators
   1. TRUE
   2. FALSE

#### IF statements
IF statements are used to define different behaviours, depending on some condition.

AND, OR, IS, and NOT keywords found in WHILE loops are also used to evaluate conditions within IF statements.

```
COUNTER num = 5

IF(num IS "5") {
    BOT: "num is 5!"
}
ELSEIF(NOT num IS "10" AND NOT num IS "7") {
    BOT: "num is not 10 or 7!"
}
ELSE {
    BOT: "num is 10 or 7!"
}
```

#### Infinite WHILE loops
To define behaviour that is recurs until a certain condition is met, an infinite WHILE loop can be used.
In practice, WHILE shouldn't be infinite - otherwise your Bot will be stuck in a loop! Define a point at which the Bot should exit out of the WHILE loop. 

```
WHILE(TRUE) {
    BOT: "Guess my name"
    USER: bot_name

    IF(bot_name IS "Bot") {
        STOP
    }
}
```

#### TRIGGERPHRASE
_TRIGGERPHRASE_ allows the Bot to listen for a specific message in the channel and start a conversation accordingly.

To begin a conversation with the Bot, @Bot is mentioned at the start of every message. This means for the _TRIGGERPHRASE_, you only need to specify the message that follows "@Bot". 

For example, to define "@Bot say hello" as the start of a new conversation, the Bot creator can define:

```
TRIGGERPHRASE("say hello") {
    BOT: "Hello!"
}

COUNTER num = 5
TRIGGERPHRASE("high " + num) {
   BOT: "high five!"
}
```

#### CONVO
_CONVO_ are methods. They allow Bot creators to define reusable conversations. Conversations can trigger other conversations.

```
CONVO yay {
    BOT: "yay"
    START goodjob
}

CONVO goodjob {
    BOT: "good job!"
}

TRIGGERPHRASE("I won!") {
    START yay
}

TRIGGERPHRASE("I lost!") {
    START yay
}
```

#### @User variable
@User refers to the user's username.
@User.role is a special keyword for the user's role within a server on Discord. 
Similarly, use @User.id to reference user ID.

### Examples

```
CONVO hello {
   BOT: "Hello" + @User
   BOT: "What's your name?"
   USER: input_name
   BOT: "Your name is " + input_name
   START goodbye
}

CONVO goodbye (
   BOT: "Time to go"
   BOT: "OK?"
   USER: response
   IF (response IS "Yes") {
      BOT: "Bye"
   }
   ELSE {
      BOT: "Never mind"
      START hello
   }
)

TRIGGERPHRASE (“say hello”) {
   START hello
} 
```

```
CONVO guess-number {
   BOT: “Guess what number I’m thinking?”
   BOT: "Enter number"
   USER: input_number

   WHILE(NOT input_number IS “2” OR NOT input_number IS “4”) {
      BOT: “Enter number”
      USER: input_number
   }

TRIGGERPHRASE (“guess number”) {
   START guess-number
} 
```

```
COUNTER brownie-points = 0

CONVO how-cool-r-u {
   IF (@User.role IS “admin” ) {
      BOT: “Hi, your role is admin”
      INCREMENT brownie-points
   }
   ELSEIF (@User.role IS “brownie”) {
      BOT: “whatever”
      INCREMENT brownie-points
      INCREMENT brownie-points
   }
   ELSE {
      BOT: “Hi, you’re not an admin...or a brownie”
      DECREMENT brownie-points
   }

   BOT: brownie-points
}

TRIGGERPHRASE(“rate me”) {
   START how-cool-r-u
} 
```

## Using the DSL program
After cloning this project, create the `\gen` folder by right-clicking on `.g4` files in the `\parser` folder and using Antlr to generate parser and lexer files.

Run `Main` found under the package `ui` to run the DSL program. Upon successful completion, `bot.js` will be generated under the folder `bot`.

`bot.js` has the generated code needed to run the Bot. It uses `Node.js` and `npm`. Make sure you have `Node.js` installed [here](https://nodejs.org/en/download/). To install the packages used to run the Bot, run `npm install` in terminal while under the `bot` directory.

After you have `Node.js` and the appropriate packages installed, simply run `bot.js` to run the program.
