# ScalatraFeatureSpec #

## Build & Run ##

```sh
$ cd ScalatraFeatureSpec
$ sbt
> jetty:start
> browse
```

If `browse` doesn't launch your browser, manually open [http://localhost:8055/](http://localhost:8055/) in your browser.


## Send Json requests ##
You can send POST and PUT messages with embedded JSON files.

In the terminal go to the src\main\resources directory, modify the 'post' and 'put' text files and run the corresponding 'curl' commands in the 'curl_commands' text file.

Exmaple:
```sh
$ cd ScalatraFeatureSpec/src/main/resources
$ vi post
$ curl -v -H "Content-Type: application/json" -X POST --data @post.txt http://localhost:8055/api/register
$ vi put
$ curl -v -H "Content-Type: application/json" -X PUT --data @put.txt http://localhost:8055/api/push
```

## Testing ##
There are two test files that will be run: "MyScalatraServletTests.scala" and "MyScalatraServletValidation.scala".
The first one uses the typical ScalatraFunSuite while the second one uses the ScalatraFeatureSpec.
You must open a console, run sbt and issue the test command. After this, you should be able to test it using IntelliJ IDEA too.
In order to run the tests, the jetty server hosting the main application must be stopped.
Therefore make sure you run the "jetty:stop" sbt command before running the tests.

```sh
$ cd ScalatraFeatureSpec
$ sbt
> test
```