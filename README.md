# json-parser

## How to use

First of all, download the dependencies, leiningen and midje.
Here are the links that will surely help.
https://leiningen.org/
https://github.com/marick/Midje

After that, just clone the repo, the main functions to use are json-value and read-json-from-file. 

## Introduction

Hello, so this will be a brief introduction to a project of mine. This is a json parser written completely in Clojure. 

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

![json parser drawio](https://github.com/user-attachments/assets/e2565928-2059-4cdc-8f13-f184710dacca)

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
[JsonObject: ["ab" JsonNumber: 34.4], ["cd" JsonString: "this is a string"], ["ef" JsonBool: true], ["gh" JsonBool: false], ["ij" JsonNull: nil], ["kl" JsonArray: [JsonNumber: 1 JsonNumber: 5 JsonNumber: 2 JsonNumber: 4 JsonNumber: 6 JsonArray: [JsonBool: true JsonBool: false JsonBool: false]]], ["mn" JsonObject: ["op" JsonNumber: 63.4], ["qr" JsonString: "string once again"], ["st" JsonObject: ["uv" JsonBool: false], ["wx" JsonBool: true]]], ["yz" JsonNumber: 0.003]]

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
