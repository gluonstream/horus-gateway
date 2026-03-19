# My Gateway has Multiple Personalities (and so do its Redirects)

Ever had that moment when you realize your Spring Cloud Gateway is doing a great job at routing, but after a successful OAuth2 login, it just... forgets where the user came from? It's like inviting someone to a multi-room party, but everyone who goes to the bar is redirected to the kitchen when they're done. 

That's exactly what was happening here. We've got a blog and a main app living under the same gateway roof, and we needed to make sure our users ended up in the right place after logging in.

Let's break down the last three steps we took to fix this and make our gateway a bit more sophisticated.

### 1. The "Build Once, Run Anywhere (Even on my M1)" Fix

Before we could get fancy with redirects, we needed to make sure we could actually build the thing. If you're developing on an Apple Silicon (M1/M2) Mac but deploying to a Linux/AMD64 server, you know the struggle is real.

We added a `Dockerfile` and a `build-gateway.sh` script that uses `docker buildx` to ensure we're targeting the right platform (`linux/amd64`). 
I also updated `build.gradle.kts` to set the `imagePlatform` to `linux/amd64` for `bootBuildImage` before resorting on a Dockerfile but that seems futile.

### 2. Routing by Host: "Who Are You Again?"

In Spring Cloud Gateway, you can route based on many things, but the `Host` header is one of the most powerful for multi-tenant or multi-service setups. 
We added a specific route for our blog frontend:

```java
.route("blog-fe", p -> p.host("blog.s4v3.net", "blog.s4v3.local")
        .uri(feBlogUrl))
```

This tells the gateway: "If the request is coming for `blog.s4v3.net` (or the local dev version), send it to the blog's frontend URL." Simple, right? But wait, there's a catch...

### 3. The Grand Finale: `hostBasedSuccessHandler`

Here's the problem: when a user hits the blog and needs to log in, they get sent to the OIDC provider (like Keycloak). After they successfully authenticate, Spring Security usually redirects them to a single `successRedirectUrl`. 

If they started at `blog.s4v3.net`, they definitely don't want to end up at the main app's home page after logging in. They want to go back to the blog!

Enter the `hostBasedSuccessHandler`. Instead of a static redirect, we wrote a custom `ServerAuthenticationSuccessHandler` that inspects the `Host` header of the current exchange:

```java
private ServerAuthenticationSuccessHandler hostBasedSuccessHandler() {
    return (webFilterExchange, authentication) -> {
        var exchange = webFilterExchange.getExchange();
        String host = exchange.getRequest().getURI().getHost();

        // Determine the target URL based on the host
        String targetUrl = successRedirectUrl;
        if (host != null && (host.contains("blog.s4v3.net") || host.contains("blog.s4v3.local"))) {
            targetUrl = successBlogRedirectUrl;
        }

        // Perform the redirect
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create(targetUrl));
        return response.setComplete();
    };
}
```

Now, the gateway checks the host:
- Is it the blog? Great, send them to `successBlogRedirectUrl`.
- Anything else? Send them to the default `successRedirectUrl`.

We then plugged this into our `SecurityWebFilterChain`:

```java
.oauth2Login(oauth2 -> oauth2
        .authenticationSuccessHandler(hostBasedSuccessHandler())
)
```

And just like that, our gateway developed a memory!

### Summary of what we did:
1.  **Fixed the build**: Cross-platform Docker builds for M1/M2 devs.
2.  **Smart Routing**: Added host-based routing for the blog.
3.  **Dynamic Redirects**: Implemented `hostBasedSuccessHandler` so users don't get lost after logging in.

Happy coding, and may your redirects always find their way home!
