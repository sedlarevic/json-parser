# json-parser

## How to use

First of all, download the dependencies, leiningen and midje.
Here are the links that will surely help.
https://leiningen.org/
https://github.com/marick/Midje

After that, just clone the repo, the main functions to use are json-value and read-json-from-file. 

## Introduction

Hello, so this will be a brief introduction to a project of mine. This is a JSON parser written completely in Clojure. 

### So what do I mean when i say JSON parser?
JSON parser, essentialy, takes a string,a text as an input and provides output, which are parsed JSON objects. 
This may seem confusing at first, so I will explain to you what is a parser, what is JSON, and then, I will explain the jist of this project, how the functions work together in unity, and come around to make a JSON parser.
#### Parser
A parser is an algorithm designed in order to take a sequence of tokens and decide whether that sequence is valid in the considered grammar. 
It's the process of breaking apart of data in one well-known consistent format into a format that the program can use for its purpose.
#### JSON
JavaScript Object Notation (JSON) is a standard text-based format for representing structured data based on JavaScript object syntax. It is commonly used for transmitting data in web applications (e.g., sending some data from the server to the client, so it can be displayed on a web page, or vice versa).

JSON Syntax Rules
Data is in name/value pairs
Data is separated by commas
Curly braces hold objects
Square brackets hold arrays

The example shown below defines an employees object, an array of 3 employee objects:
{
"employees":[
    {"firstName":"John", "lastName":"Doe"},
    {"firstName":"Anna", "lastName":"Smith"},
    {"firstName":"Peter", "lastName":"Jones"}
]
}

We will now move to the JSON parser and its' functions.

## JSON Parser

I think the best approach to explaining this project is through one main picture. Please, pay attention to the picture below.

![json parser drawio](https://github.com/user-attachments/assets/b0faace9-cce8-4e0f-8d35-539bf6fe836d)

As you can see from the picture, the main parser consists of multiple smaller parsers, which parse some JSON object.
When input is sent to the main parser, the parser pushes the whole input string to smaller parsers, to check if there is some JSON object to be parsed from the stringv (1.). If there is, the smaller parser sends that value to the main parser (json-value), and the main parser stores that value (3.). If there is no JSON object to be parsed, the main parser tries the remove the separator, which separates JSON objects (,). If the separator can be removed, main parser sends back string to be further parsed (3.). If, albeit, the remove-separator function returns nil, and the main function already received nil from all the smaller parsers, that means that there is no more string to be parsed, and returns the stored output value (4.).

This is the heart of the project, the main part of it.

In this example, you can see how it works:

Input:
{"ab" : 34.4, "cd": "this is a string", "ef": true, "gh"  : false,
"ij": null, "kl": [1,5,2,4,6,[true,false,false]],
"mn": {"op": 63.4, "qr": "string once again", "st" :
{"uv" : false, "wx" : true}}, "yz": 0.003}

Output:
{:ab 34.4, :cd "this is a string", :ef true, :gh false, :ij nil, :kl [1 5 2 4 6 [true false false]], :mn {:op 63.4, :qr "string once again", :st {:uv false, :wx true}}, :yz 0.003}

### Reading from file

Reading JSON from file is also supported. The function that provides that is called read-json-from-file, and the only thing that should be provided to that function is file path.

### Parse-char & Parse-string

The backbones of this project are these two functions, and their role is really simple. Parse a character from a string, and parse a substring from a string, or return nil.
Almost every "smaller" parser uses these two functions.

Parse-char takes 2 inputs. First input is string that should be parsed. Second input is the character that should be the output of the parsing, the one that should be parsed from the string. So, the first character from a string.
Parse-string also takes 2 inputs. The string that should be parsed, and the expected output from a string that is parsed.

Here are the illustrations of the way they work:

Parse-char:

![parse-char drawio](https://github.com/user-attachments/assets/d395c436-afd3-48a3-bfed-b45beea3387b)

Parse-string:

![parse-string drawio](https://github.com/user-attachments/assets/f9aad61f-354d-4f6f-b4d7-555a4f396d79)

## Motivation

This is a project for a class subject on my masters' studies called "Tools and methods of software engineering and artifical intelligence", where we are mainly supposed to use functional programming language, Clojure. As I was not used to writing code in functional languages, I wanted to make a project that will be challenging, but still fun, so I could experience the functional paradigm in its' full potential. I was looking for concepts, ideas, projects that I could induge in, and chose JSON parser at the end, mainly because it seemed fun, but saw an opportunity for myself to learn a lot.

## Benchmarking

I would like to clarify that this json parser is not currently intended to be used for production purposes. The main goal was not optimization, it was to have a project finished, so the goal in the future will be to optimize it as much as it is possible, though it uses "recur" which has built-in optimizations in of itself. Also, the limitations of this parser is that there is no support for string escaping yet.

For the sake of benchmarking, I've taken Cheshire to do my tests, as it is really performant. Here are the results:

My JSON parser:

Evaluation count : 1062 in 6 samples of 177 calls.
             Execution time mean : 575.349882 µs
    Execution time std-deviation : 9.058773 µs
   Execution time lower quantile : 567.193299 µs ( 2.5%)
   Execution time upper quantile : 588.552965 µs (97.5%)
                   Overhead used : 6.564701 ns
                   
Benchmarking Cheshire parser:

Evaluation count : 16026 in 6 samples of 2671 calls.
             Execution time mean : 37.746428 µs
    Execution time std-deviation : 264.711451 ns
   Execution time lower quantile : 37.461144 µs ( 2.5%)
   Execution time upper quantile : 37.999831 µs (97.5%)
                   Overhead used : 6.564701 ns
                   
As you can see, my parser is drastically slower than Cheshire. Mostly because I've made my own custom records, which add a layer of abstraction. Also, I've made my own parsers, which parse text letter-by-letter, which makes the solution slower. That means there is a lot of room for improvement!
Also, a lot of recursive calls, coming from function mapv, can cause a big bottleneck, if the json file is large.

## License

Copyright © 2024 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
