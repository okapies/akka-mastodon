# akka-mastodon
[Mastodon](https://github.com/tootsuite/mastodon) client based on [Akka Streams](http://doc.akka.io/docs/akka/current/scala/stream/index.html).

## Usage
```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.github.okapies.akka.mastodon.Timeline
import com.github.okapies.akka.mastodon.scaladsl.MastodonClient

implicit val system = ActorSystem()
implicit val materializer = ActorMaterializer()
implicit val executionContext = system.dispatcher

val mastodonHost = "https://..."
val accessToken = "..."

MastodonClient(mastodonHost, accessToken)
  .timelines(Timeline.home())
  .runForeach { status => println(status) }
  .andThen { _ =>
      Http().shutdownAllConnectionPools().onComplete(_ => system.terminate())
  }
```
