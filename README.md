# Insight Data Engineering Coding Challenge: Anomaly Detection

This is a Java implementation of the coding challenge provided [here](https://github.com/InsightDataScience/anomaly_detection).

## Table of Content

- [Pre-Requisite](#pre-requisite)
- [How to Run](#how-to-run)
- [RxJava and Streaming API](#rxjava-and-streaming-api)
- [Batch Log vs Stream Log](#batch-log-vs-stream-log)
- [Graph](#graph)
- [Batch Log Processing](#batch-log-processing)
- [Stream Log Processing](#stream-log-processing)
- [Mean and Standard Deviation Calculations](#mean-and-standard-deviation-calculations)
- [Other Approaches](#other-approaches)
- [Future Improvements](#future-improvements)

## Pre-Requisite

*You need to have Java SDK 1.8 installed on your machine in order to build and run this application.*

This application is build using [Gradle](https://gradle.org/), which will handle all dependencies and download them through remote repositories if none currently exists. Here are the dependencies of this project, in case you are curious:

- [RxJava](https://github.com/ReactiveX/RxJava) 2.1.1 for reactive programming, especially for streaming input logs.
- [JCommander](http://jcommander.org/) 1.71 for CLI arguments parsing.
- [Google AutoValue](https://github.com/google/auto/tree/master/value) for immutable value class annotation processors.
- [Jackson](https://github.com/FasterXML/jackson) for JSON string <-> Object conversion.

## How to Run

Clone or download this repository and simply run

```bash
$ ./run.sh
```

The first time that you run this command, it will build a new Java application and put inside the `build` folder. Subsequent runs will be faster as it will not need to rebuild the application.

*Currently there's no `run` script that can clean up the build. If you need to clean and rebuild, you need to either run `./gradlew clean` or simply remove the `build` directory.*

When you run the application, you will see some messages showing up in your console similar to this one

```bash
Running anomaly-detection
Finish purchase batch process.
Finish relationship batch process.
Finish stream process.
```

Once you see the "Finish stream process" message, it means that the application has finish parsing the processing the stream log and the output file (`flagged_purchases.json`) has been generated. **The program will not terminate even after the processes are finished.** I designed it this way to mimic a streaming API where we subscribe to a streaming log and keep the connection alive. To terminate the program, simply press Ctrl-C.

To run the `insight_testsuite`, I have modified the `run_tests.sh` script so that it also copies Gradle configurations to the temp project folder.

## RxJava and Streaming API

Reactive programming is a good paradigm to work with a streaming API. As streams emit data, subscribers react to the signal. This push-based policy is in contrast with Java [Stream](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html) interface's pull-based policy introduced in Java 8. I opted to use RxJava as the reactive library for this purpose and you can see in the code that I have utilized Observables and Subjects in many places, especially for input log read streams. I push each event onto an observer and emit them through a subject for later subscriptions. I have also utilized Scheduler interface to handle threads. Although currently the application is run on a single thread, Rx provides a very simple way to switch between threads via `observeOn` and `subscribeOn` operators.

I did not use Rx everywhere in the program. It is good for asynchronous behavior and immutable data. However, several parts in our application are mutable states, which comes as a trade-off for performance.

- For input log read and data transformations, use Rx.
- For other parts such as User's social graphs and purchase list, use Java built-in data structures.

## Batch Log vs Stream Log

There are a few key differences and assumptions that make processing these two logs not the same.

**Batch Log**. In the real world, events can come from multiple sources. These sources may not be in sync and therefore the aggregate log from them can be in the wrong order. We need to sort the events by timestamp first. This is necessary also because we will only keep track of a certain number of latest purchases. The order of befriend/unfriend events are also important.

Fortunately, batch log processing is not latency-sensitive. This assumption comes from the widely used Lambda/Kappa data architectures where there is a stream process that can take care of real-time situations. So we can afford some sorting performance hit in the batch process.

There can be a speed-up where we can do things in parallel, however. As you will see in the next sections, the purchase events and the relationship events (befriend & unfriend) can be processed independently, i.e. the operations can be on different threads.

**Stream Log**. Since we have to process the event in stream log in real-time, we can (and generally have to) assume that the events come in timestamp order. The process has to assume that the current state at that moment in time is correct. This helps us avoid dealing with sorting. However, this means that the state of this social network has to be up-to-date for every event. That is we cannot consider purchase events and relationship events independently.

## Graph

We represent the social network in question as an un-directional graph. This is the `UserNetwork` class in the code, which extends the generic class `SocialNetwork<Type>`. Each node in the graph contains user's data (`UserData` class in the code) and a set of its neighbors. Each user data contains a user ID and a list of its latest purchases. We only maintain T latest purchases for each user and choose a linked list (double-ended queue) as the data structure to maintain this list, sorting the purchase data by timestamp. The reason is that we don't have any requirements to do a random access for this list nor we need to modify or delete each purchase data. (Purchase data is immutable.) We only need to push new purchase data to the end of the list while popping out older ones at the other end to keep only T. This ensures that the insertion takes O(1) time while also keeping the purchase list sorted by timestamp.

A node also has an ID which uniquely identifies it. In fact, the set of neighbors each node maintains consists of IDs not the node references. To determine which ID points to which node, we also maintain an internal hash map from an ID to a node reference. A lookup only takes a constant time, though we sacrifices some memory trade-off with the hash map. The reason we need this is because a user is specified in the event log by a unique ID, so we need a way to identify a node by its ID. And even though IDs are numbers in the log, we use them as strings rather than integers to support more ID format in the future.

Notice that a node doesn't store any purchase information of other nodes. We only calculate "nearby" friends and other required information during runtime. As you will see in the next sections, this can be done in linear time and offer a memory efficiency and an easy-to-understand code compared to other approaches.

## Batch Log Processing

Batch log is only used for initialization and pre-processing. No flagged purchases required. Since a node only stores its own purchase history, we can consider the purchase events and the relationship (befriend/unfriend) events in the batch log independently. We use the purchase events to populate each node with its latest purchase data, while the relationship events are used to connect the nodes (initializing the neighbor set). This means that we can utilize two threads where both read from the batch log and filter either purchase or relationship events.

As mentioned above, the batch log may not be in the right order. To address this, for purchase events, we group the events by user ID. For each group, we sort the purchases by timestamp and only take T latest ones to populate the user node. For relationship events, we sort them by timestamp first and adding each one to the neighbor set of the corresponding users. Adding and removing elements from a hash set takes constant time.

If the out-of-timestamp-order assumption can be relaxed, the batch process can be very fast, taking only linear time. For each purchase group (by user ID), we simply take the last T purchases (easily achieved from stream by using Rx `takeLast` operator). We also don't need to sort the relationship events.

## Stream Log Processing

As mentioned above, we can assume that the stream events come in the correct order. We cannot group the purchase or relationship events and batch process them like in the batch log since we need to calculate real-time data based on the state of the graph at that moment in time. There are two main tasks we need to achieve for each purchase events: finding "nearby" friends of depth D and querying T latest purchases of this group of friends.

Notice that we only calculate these properties when a purchase event comes in. We leave the befriend and unfriend events simple by just updating the neighbor set in constant time. It does not really matter when in time you calculate these properties since this is supposed to be from a streaming API and we are streaming out the result as well. Whether you do the calculations when the purchase comes in or do them when the relationship events come in, it will still take the same amount of time. The more important factor here is that if you do the calculations during relationship events, you need to store the resulting data somewhere to be queried when the purchase comes in. Instead of trading off memories and complicating the code, we keep the code simple and calculate the mean and standard deviations when the purchase events come in.

#### Finding "nearby" friends of depth D

This is a classic bread-first-search algorithm. We maintain a queue to keep to-be-visited nodes. We start at the buyer node, push it into the queue and mark it as visited. For each iteration, we poll from the queue, add its friends to the queue and mark each of them as visited. Note that we need to add an end-of-level signal element into the queue as well to keep track of the number of levels we have gone down. The whole operation takes O(N) time where N is the number of nodes within depth D of the current buyer node.

#### Querying T latest purchases of "nearby" group

At this point we already have the group of "nearby" friends, each holding their own purchase history. Suppose there are N friends in this group, then we have N purchase history with T purchase data each sorted by timestamp. In a nutshell, we are trying to merge N sorted lists into a new sorted list and take only the first T elements.

To achieve this, we utilize the Priority Queue with the timestamp as the comparator. The underlying data structure of the PriorityQueue class in Java is a heap. This allows us to retrieve the min element in constant time (polling from the queue) and insert element in the queue in log(N) time (it should be the log of the size of the queue but we will demonstrate that the size is never larger than N).

First, we convert each of the N list into a singly-linked list. We then insert the head of each list into the Priority Queue, sorted by timestamp. For each iteration, we poll the head of the queue, put it in the result list, and add the next purchase data node from its original list. This is the reason why we need to convert each list into a linked list first, so we know which list each element in the queue comes from. We do only T iterations to get the T latest purchases for the "nearby" friends group.

As you can see, the size of the Priority Queue never exceeds N. This means that each insertion take O(log N) time. Inserting the heads of each list into the queue, however, takes only O(N) time as that amounts to the same as heapifying a list of N elements. After that we only do atmost T insertions. So overall, this operation takes O(N + Tlog N) time. If we consider T to be constant, then this is a linear-time operation.

## Mean and Standard Deviation Calculations

After getting the list of latest purchases from the "nearby" group, calculating the mean and sd is simple. The operation takes O(T) time and if we think of T as a constant, this is a constant operation.

## Other Approaches

We could very well store the latest purchases from the "nearby" group in each node. This will make querying the mean and sd of this list very fast for each purchase event, although we also need to update this same list for each of the node in its "nearby" group too (which takes O(N) time). Overall, it still takes a linear-time operation to complete a purchase event process. To make things worse, maintaining this "group" latest purchase list in each node also means that we need to do extra work during the befriend and unfriend events. Updating a relationship means we need to do a few BFS traversals to update these "group" latest purchase lists of every node affected. All in all, this complicates the code, adds more memory, and does not speed up the application that much.

## Future Improvements

1. Use dependency injection framework such as [Dagger](https://google.github.io/dagger/) to better organize code and make it easier to test.
