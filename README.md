# Reactive Spring Boot using coroutines

Just a quick demo with a comparison to Java how to do things the reactive way in Kotlin using Spring Boot.

The main advantages of choosing the reactive stack of Spring Boot (WebFlux) is better performance both in terms 
of resources your app actually needs (using netty instead of a servlet container) and the throughput (number of 
parallel requests your server can handle goes up by factor 4~5). You can also do things the non-blocking way
(for example, speaking to 2 different services at the same time) very easily.

The downside when using Java ist that you have to get a basic understanding of functional reactive programming
using its promise-like data types (Mono, Flux). Kotlin Coroutines allow you to stick to a pseudo-imperative way of
doing things (similar to async/await in many programming languages).

This repo contains a simple app that stores todos in a MongoDB exposing a simple CRUD REST API written both in Kotlin
and in Java.
