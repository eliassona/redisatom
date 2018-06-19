# redisatom

A Clojure library that implements redis as a durable atom.


## Usage

### Clojure
Add the following line to your leinigen dependencies:
```clojure
[redisatom "0.1.0-SNAPSHOT"]
```



```clojure
=> (use 'redisatom.core)
=> (import 'redis.clients.jedis.Jedis)
=> (def ra (redis-atom (Jedis.)))
=> (def atom-for-a-key (ra "key"))
=> (def a (ra "key"))
=> @a
nil
=> (reset! a 0)
0
=> (swap! a inc)
1
```
There is also a function for iterating the keys with a lazy-seq.

```clojure
=> (def j (Jedis.)) 
=> (def ra (redis-atom j))
=> (dotimes [i 10] (reset! (ra (str i)) "some value")) ;insert some values into redis
=> (redis-keys j)
("0" "6" "3" "9" "1" "4" "8" "7" "5" "2")
```

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
