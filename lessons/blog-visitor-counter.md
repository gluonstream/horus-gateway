# How We Built a Visitor Counter for Our Blog (And Accidentally Created a Time Machine)

## The Problem

"We need a visitor counter for our blog," said the product manager, sipping their morning coffee with the confidence of someone who has never written a line of code in their life.

"Sure," we said, because we're masochists.

## The Requirements

1. Count every visitor to the blog (global counter)
2. Track unique sessions (because apparently "unique visitors" is a thing)
3. Make it work with Redis (because we already use Redis for sessions, so why not pile more onto the fire?)
4. Make it fast (no latency added to page loads)
5. Make it fail-safe (if Redis dies, the blog should still work)

## The Architecture

We're running Spring Cloud Gateway with Spring Session Data Redis. The gateway sits in front of both our main app and our blog.

### The Flow

```
User Browser → Gateway → Blog Frontend
                  ↓
            Redis (sessions + counters)
```

## The First Attempt (aka "The Naive Approach")

Our first implementation looked something like this:

```java
exchange.getSession()
    .flatMap(session -> {
        // Increment counters
        return Mono.zip(
            redisTemplate.increment(GLOBAL_KEY),
            redisTemplate.increment(SESSION_KEY + sessionId)
        );
    })
    .then(chain.filter(exchange));
```

This worked beautifully... in theory. In practice? The counter stayed at 0. Why? Because reactive programming is a cruel mistress.

The problem: `.then()` executes immediately after the upstream completes, not when the Redis operations finish. The response was sent before Redis even got the memo.

## The Second Attempt (aka "Fire and Pray")

"Let's just run it after the chain!" we shouted, throwing all caution to the wind.

```java
return chain.filter(exchange)
    .then(Mono.fromRunnable(() -> {
        redisTemplate.increment(GLOBAL_KEY).subscribe();
    }).subscribeOn(Schedulers.boundedElastic()));
```

This worked! But adding latency. Every blog request now took an extra 5-10ms waiting for Redis.

## The Final Solution (aka "The Smart Way")

Thanks to our colleague who pointed out: "Why are we maintaining our own counter when Redis already stores sessions?"

The answer was beautiful in its simplicity:

```java
return redisTemplate.keys("spring:session:sessions:*")
    .count()
    .map(count -> Map.of("uniqueSessions", count))
    .flatMap(result -> ServerResponse.ok()...);
```

We literally just count the Spring Session keys that Redis already maintains. No extra counters. No extra Redis calls on every request. Just one query when someone asks "how many visitors do we have?"

### The API

```
GET /api/counter
{"uniqueSessions": 42}
```

Simple. Clean. No latency added to page loads.

## Key Lessons Learned

### 1. Reactive Chains Don't Wait For You

In reactive programming, `subscribe()` doesn't wait for completion. The chain continues immediately. If you need something to finish before the response, use `.then()` properly or consider fire-and-forget with `subscribeOn()`.

### 2. Don't Recreate What's Already There

We spent 3 iterations building our own session counter when Redis was already tracking sessions the whole time. Sometimes the best code is the code you don't write.

### 3. Use the Right Thread Pool

Running blocking operations on the Netty event loop will kill your gateway. Use `Schedulers.boundedElastic()` for fire-and-forget I/O operations.

### 4. Fail-Safe Design

If Redis is down, the blog should still work. We added `.onErrorResume()` to continue the filter chain even if Redis operations fail.

## The Result

- **0ms added latency** to blog requests
- **Accurate unique visitor count** (via Spring Session keys)
- **Fail-safe** (Redis downtime won't break the blog)
- **Simple code** (50 lines vs 200 lines)

## Credits

Thanks to:
- The product manager who asked for a "simple counter"
- The Spring Session documentation (for existing session tracking)
- Stack Overflow (for teaching us about `subscribeOn()`)
- Coffee (essential ingredient in all software engineering)
