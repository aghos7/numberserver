#A simple solution to a silly message queue problem:

In Java, write a server application that opens a socket on port 4000 and processes input from at most 5 clients concurrently. Clients will connect and write any number of 9 digit numbers, separated by newline sequences native to the server, and then close the connection. Your server must then write these numbers to a file "numbers.log" in no particular order.

In addition:
- No duplicates should be written to the log file.
- The log file should be cleared when the server starts.
- The numbers must have exactly 9 digits which may include leading zeros.
- The leading zeroes may be stripped when writing to the log our console.
- You do not need to pad the numbers with leading zeros when writing the log.
- Once every 10 seconds print to standard output of the count of numbers that have been received and the number of duplicates since the last report; e.g., "received 50 numbers, 2 duplicates"
- Any data that does not conform to a valid line of input should be discarded and the client connection terminated quietly.
- If a client writes a single line with only the word "terminate" then the server will exit immediately.

Your primary considerations are:
- It should work correctly
- The solution should be simple
- The code should be descriptive and easy to read
- It should be optimized for maximum throughput
    
    
Requirements:
- jdk 1.7
- maven - Make sure maven is setup to use java version 1.7 (check with mvn -v)
- junit - Test framework
    
Assumptions:
- The total number of numbers received will not exceed the maximum value supported by a long
- The server should shutdown gracefully when a "terminate" message is received
- The 9 digit numbers will be positive or negative

Building:
`mvn clean compile`